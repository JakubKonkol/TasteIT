package pl.jakubkonkol.tasteitserver.service;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class StopwordsPipe extends Pipe {
    private Set<String> stopwords;

    public StopwordsPipe(File stopwordsFile) throws IOException {
        stopwords = new HashSet<>();
        // Wczytanie stop słów z pliku
        try (BufferedReader reader = new BufferedReader(new FileReader(stopwordsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stopwords.add(line.trim().toLowerCase()); // Dodajemy stop słowa w małych literach
            }
        }
    }

    @Override
    public Instance pipe(Instance carrier) {
        TokenSequence tokens = (TokenSequence) carrier.getData();
        TokenSequence filteredTokens = new TokenSequence();

        for (Token token : tokens) {
            if (!stopwords.contains(token.getText().toLowerCase())) {  // Porównanie bez uwzględniania wielkości liter
                filteredTokens.add(token);
            }
        }
        carrier.setData(filteredTokens);
        return carrier;
    }
}
