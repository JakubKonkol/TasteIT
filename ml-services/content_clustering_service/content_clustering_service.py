from typing import List, Dict, Any
import numpy as np
import spacy
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.decomposition import LatentDirichletAllocation
import logging
import re

from content_clustering_service.cluster_info import ClusterInfo
from content_clustering_service.cluster_namer import ClusterNamer


class ContentClusteringService:
    def __init__(self, n_topics: int = 15, min_df: float = 0.01, max_df: float = 0.95, n_top_words: int = 15):
        self.nlp = spacy.load("en_core_web_sm")
        self.n_topics = n_topics
        self.n_top_words = n_top_words
        self.vectorizer = CountVectorizer(
            max_df=max_df,
            min_df=min_df,
            stop_words='english',
            token_pattern=r'(?u)\b[A-Za-z]+\b'
        )
        self.lda_model = LatentDirichletAllocation(
            n_components=n_topics,
            random_state=42,
            learning_method='online',
            batch_size=100,
            max_iter=25,
            n_jobs=-1
        )
        self.cluster_info: Dict[int, ClusterInfo] = {}
        self.is_fitted = False
        self.cluster_namer = ClusterNamer()
        self.logger = logging.getLogger(__name__)

        self.all_processed_texts = []
        self.all_posts = []

    def fit(self, posts: List[Dict[str, Any]]) -> None:
        processed_texts = self._prepare_texts(posts)
        if not processed_texts:
            raise ValueError("No valid texts to process")

        self.all_processed_texts.extend(processed_texts)
        self.all_posts.extend(posts)

        # If this is the first batch, do a full match
        if not self.is_fitted:
            doc_term_matrix = self.vectorizer.fit_transform(self.all_processed_texts)
            self.lda_model.fit(doc_term_matrix)
            self.is_fitted = True
        else:
            # For subsequent batches, use partial_fit
            doc_term_matrix = self.vectorizer.transform(processed_texts)
            self.lda_model.partial_fit(doc_term_matrix)

        # Update cluster information based on all data
        self._update_cluster_info(np.array(self.vectorizer.get_feature_names_out()))

    def _extract_post_content(self, post: Dict[str, Any]) -> str:
        try:
            tags = post.get("tags", [])
            tag_names = " ".join(tag.get("tagName", "") for tag in tags if isinstance(tag, dict))

            post_media = post.get("postMedia", {})
            if not isinstance(post_media, dict):
                post_media = {}
            title = post_media.get("title", "")

            recipe = post.get("recipe", {})
            if not isinstance(recipe, dict):
                recipe = {}

            ingredients_list = recipe.get("ingredientsWithMeasurements", [])
            ingredients = " ".join(ing.get("name", "") for ing in ingredients_list if isinstance(ing, dict))

            post_text = f"{title} {tag_names} {ingredients}"
            post_text = re.sub(r'[^\w\s]', ' ', post_text)
            post_text = ' '.join(post_text.split())

            self.cluster_namer.update_tag_data(post)

            return post_text.lower()

        except Exception as e:
            self.logger.warning(f"Error extracting content from post: {str(e)}")
            return ""

    def _preprocess_text(self, text: str) -> str:
        try:
            doc = self.nlp(text)
            tokens = [token.lemma_ for token in doc if (not token.is_stop and not token.is_punct
                                                        and token.pos_ in {'NOUN', 'ADJ', 'VERB'} and len(
                        token.text) > 2 and token.text.isalpha())]
            return " ".join(tokens)
        except Exception as e:
            self.logger.warning(f"Error in text preprocessing: {str(e)}")
            return ""

    def _prepare_texts(self, posts: List[Dict[str, Any]]) -> List[str]:
        processed_texts = []
        for post in posts:
            if not isinstance(post, dict):
                continue
            post_content = self._extract_post_content(post)
            if not post_content.strip():
                continue
            processed_text = self._preprocess_text(post_content)
            if processed_text.strip():
                processed_texts.append(processed_text)
        return processed_texts

    def _update_cluster_info(self, feature_names: np.ndarray) -> None:
        for topic_idx, topic in enumerate(self.lda_model.components_):
            sorted_word_idx = topic.argsort()[:-self.n_top_words - 1:-1]
            top_words = [feature_names[i] for i in sorted_word_idx]
            word_weights = {feature_names[i]: float(topic[i]) for i in sorted_word_idx}

            total_weight = sum(word_weights.values())
            word_weights = {word: weight / total_weight for word, weight in word_weights.items()}

            cluster_name = self.cluster_namer.name_cluster(top_words, word_weights)

            self.cluster_info[topic_idx] = ClusterInfo(
                cluster_id=topic_idx,
                name=cluster_name,
                main_topics=top_words,
                keyword_weights=word_weights,
                post_count=0
            )

    def predict(self, posts: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        if not self.is_fitted:
            raise ValueError("Model must be fitted before making predictions")

        processed_texts = self._prepare_texts(posts)
        doc_term_matrix = self.vectorizer.transform(processed_texts)
        doc_topics = self.lda_model.transform(doc_term_matrix)

        clustered_posts = []
        valid_post_indices = []

        for i, post in enumerate(posts):
            if i < len(processed_texts) and processed_texts[i].strip():
                valid_post_indices.append(i)

        for i, post_idx in enumerate(valid_post_indices):
            cluster_id = int(np.argmax(doc_topics[i]))
            confidence = float(doc_topics[i][cluster_id])

            cluster_info = self.cluster_info[cluster_id]

            enriched_post = posts[post_idx].copy()
            enriched_post.update({
                'clusterId': cluster_id,
                'clusterName': cluster_info.name,
                'clusterConfidence': confidence,
                'clusterTopics': cluster_info.main_topics[:5],
                'clusterSize': cluster_info.post_count + 1
            })

            clustered_posts.append(enriched_post)
            self.cluster_info[cluster_id].post_count += 1

        return clustered_posts

    def get_cluster_summary(self) -> Dict[str, Any]:
        if not self.is_fitted:
            raise ValueError("Model must be fitted before getting cluster summary")

        return {str(cluster_id): {
            'name': info.name,
            'main_topics': info.main_topics,
            'keyword_weights': info.keyword_weights,
            'post_count': info.post_count
        } for cluster_id, info in self.cluster_info.items()}
