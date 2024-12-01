package pl.jakubkonkol.tasteitserver.service;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.model.LDAModel;
import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.model.Recipe;

import java.io.*;
import java.util.*;

@Service
public class LdaService {
    private final MongoTemplate mongoTemplate;

    public LdaService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void trainLdaModel(List<Post> posts) throws IOException {
        InstanceList instances = preprocessPosts(posts);

        ParallelTopicModel model = new ParallelTopicModel(10);
        model.addInstances(instances);
        model.setNumThreads(4);
        model.setNumIterations(1000);
        model.estimate();

        saveModel(model, mongoTemplate, "Default_LDA_Model");
    }

    // Użycie modelu LDA do przewidywania tematów dla pojedynczego postu
    public Map<String, Double> inferPostTopics(Post post, ParallelTopicModel model) {
        TopicInferencer inferencer = model.getInferencer();
        Instance instance = preprocessSinglePost(post);
        double[] topicDistribution = inferencer.getSampledDistribution(instance, 10, 1, 5);

        Map<String, Double> topicMap = new HashMap<>();
        for (int i = 0; i < topicDistribution.length; i++) {
            topicMap.put("Topic " + i, topicDistribution[i]);
        }
        return topicMap;
    }

    private InstanceList preprocessPosts(List<Post> posts) throws IOException {
        List<Instance> instances = new ArrayList<>();

        // Pipeline do przetwarzania
        ArrayList<cc.mallet.pipe.Pipe> pipeList = new ArrayList<>();

        // Tokenizacja na małe litery
        pipeList.add(new TokenSequenceLowercase());

        // Załaduj plik stopwords.txt z zasobów
        InputStream stopwordsStream = getClass().getClassLoader().getResourceAsStream("stopwords.txt");

        if (stopwordsStream == null) {
            throw new IOException("Plik stopwords.txt nie został znaleziony w katalogu resources.");
        }

        // Tworzymy tymczasowy plik z zawartości InputStream
        File stopwordsFile = createTempFileFromInputStream(stopwordsStream);

        // Usuwanie stop słów
        pipeList.add(new StopwordsPipe(stopwordsFile));

        // Dodajemy klasy do tokenizacji i generowania 'Alphabet'
        pipeList.add(new cc.mallet.pipe.TokenSequence2FeatureSequence());  // Dodajemy konwersję tokenów do cech

        // SerialPipes łączące wszystkie powyższe operacje
        SerialPipes serialPipes = new SerialPipes(pipeList);

        // Utworzenie InstanceList
        InstanceList instanceList = new InstanceList(serialPipes);

        for (Post post : posts) {
            Recipe recipe = post.getRecipe();

            if (recipe != null && recipe.getSteps() != null && !recipe.getSteps().isEmpty()) {
                StringBuilder contentBuilder = new StringBuilder();
                recipe.getSteps().entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(entry -> contentBuilder.append(entry.getValue()).append(". "));

                String content = contentBuilder.toString().trim();

                // Konwertujemy content na TokenSequence
                TokenSequence tokenSequence = new TokenSequence();
                String[] tokens = content.split("\\s+"); // Dzielimy na słowa po białych znakach
                for (String token : tokens) {
                    tokenSequence.add(new Token(token));
                }

                // Tworzymy instancję
                Instance instance = new Instance(tokenSequence, null, null, null);
                instanceList.addThruPipe(instance);
            }
        }

        return instanceList;
    }


    // Przetwarzanie pojedynczego postu na instancję
    private Instance preprocessSinglePost(Post post) {
        Recipe recipe = post.getRecipe();

        if (recipe == null || recipe.getSteps() == null || recipe.getSteps().isEmpty()) {
            return new Instance("", null, null, null);
        }

        StringBuilder contentBuilder = new StringBuilder();
        recipe.getSteps().entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey()) // Sortowanie po numerach kroków
                .forEach(entry -> contentBuilder.append(entry.getValue()).append(". "));

        String content = contentBuilder.toString().trim();

        return new Instance(content, null, null, null);
    }

    private File createTempFileFromInputStream(InputStream inputStream) throws IOException {
        // Tworzenie tymczasowego pliku
        File tempFile = File.createTempFile("stopwords", ".txt");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
        }

        return tempFile;
    }

    private void saveModel(ParallelTopicModel model, MongoTemplate mongoTemplate, String modelName) {
        // Wyodrębnij słowa dla każdego tematu
        Map<Integer, List<String>> topicWords = new HashMap<>();
        for (int topic = 0; topic < model.getNumTopics(); topic++) {
            ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
            TreeSet<IDSorter> sortedWords = topicSortedWords.get(topic);
            List<String> words = sortedWords.stream()
                    .limit(10) // Limituj do 10 najważniejszych słów dla tematu
                    .map(idSorter -> model.getAlphabet().lookupObject(idSorter.getID()).toString())
                    .toList();
            topicWords.put(topic, words);
        }

        // Utwórz dokument LDAModel
        LDAModel ldaModel = LDAModel.builder()
                .modelId(UUID.randomUUID().toString()) // Unikalny identyfikator modelu
                .modelName(modelName)
                .numTopics(model.getNumTopics())
                .topicWords(topicWords)
                .createdDate(new Date())
                .build();

        // Zapisz model do MongoDB
        mongoTemplate.save(ldaModel);
        System.out.println("Model zapisany do MongoDB pod nazwą: " + modelName);
    }
}
