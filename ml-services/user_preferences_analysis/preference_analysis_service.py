from typing import List, Dict, Any
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity


class PreferenceAnalysisService:
    def __init__(self):
        # TF-IDF vectorizer helps convert text into numerical values that can be compared
        self.vectorizer = TfidfVectorizer(
            stop_words='english',           # Removes common English words like "the", "is", "at"
            max_features=1000,              # Limits vocabulary to top 1000 most frequent words
            ngram_range=(1, 2)              # Considers both single words and pairs of words
        )
        self.action_weights = {
            'LIKE_POST': 1.0,               # Base weight for likes
            'COMMENT_POST': 1.5,            # Base weight for comments
            'ADD_TO_FOODLIST': 2.0          # Base weight for add to foodlist
        }
        self.cluster_boost = 0.2  # How much to boost scores for clusters from posts user interacted with

    def analyze_user_preferences(self, user_data: Dict[str, Any], clusters_data: Dict[str, Any]) -> Dict[str, Any]:
        try:
            print("\n=== Starting Preference Analysis ===")
            print(f"Analyzing preferences for user: {user_data.get('userId')}")

            # Collect cluster IDs and their weights from user actions
            # This builds a map of how often user interacted with each cluster
            # For example: {'cluster1': 2.5, 'cluster2': 1.0}
            cluster_weights = self._get_cluster_weights_from_actions(user_data.get('actions', []), clusters_data)
            print(f"Collected cluster weights from actions: {cluster_weights}")

            # Second phase: Process text data
            # Gets all tags user has chosen for their profile
            user_tags = self._process_user_tags(user_data.get('tags', []))
            print(f"Processed user tags: {user_tags}")

            # Extracts keywords from:
            # - Post titles user interacted with
            # - Ingredients from recipes
            # - Tags from posts
            # - Comment content
            user_actions = self._process_user_actions(user_data.get('actions', []))
            print(f"Processed user actions: {user_actions}")

            # Combine all text data
            all_preferences = user_tags + user_actions
            print(f"Total preferences: {len(all_preferences)}")

            if not all_preferences:
                print("No preferences found, returning empty result")
                return self._create_empty_result(user_data.get('userId'))

            preference_text = ' '.join(all_preferences)
            print(f"Preference text: {preference_text[:200]}...")

            # Third phase: Prepare cluster data for comparison
            cluster_texts = {}
            for cluster_id, info in clusters_data.items():
                # Combine main topics and keyword weights into one text
                cluster_text = ' '.join(info.get('main_topics', []) + list(info.get('keyword_weights', {}).keys()))
                cluster_texts[cluster_id] = cluster_text
                print(f"\nCluster {cluster_id}:")
                print(f"- Name: {info.get('name')}")
                print(f"- Text: {cluster_text[:100]}...")

            if not cluster_texts:
                print("No cluster texts found, returning empty result")
                return self._create_empty_result(user_data.get('userId'))

            # Fourth phase: Calculate text similarity
            matrix = self.vectorizer.fit_transform([preference_text] + list(cluster_texts.values()))
            # This gives us base similarity scores based purely on text matching
            similarity_scores = cosine_similarity(matrix[0:1], matrix[1:])[0]
            print("\nInitial similarity scores:")
            for idx, score in enumerate(similarity_scores):
                print(f"Cluster {list(clusters_data.keys())[idx]}: {score}")

            # Fifth phase: Adjust scores based on historical interactions
            adjusted_scores = similarity_scores.copy()
            for idx, cluster_id in enumerate(clusters_data.keys()):
                if cluster_id in cluster_weights:
                    action_count = cluster_weights[cluster_id]
                    boost = self.cluster_boost * action_count
                    # If user interacted with cluster twice, adds 0.4 to similarity score
                    adjusted_scores[idx] += boost
                    print(f"Boosting cluster {cluster_id} score by {boost} (actions: {action_count})")

            print("\nAdjusted similarity scores:")
            for idx, score in enumerate(adjusted_scores):
                print(f"Cluster {list(clusters_data.keys())[idx]}: {score}")

            # Find the best matches using modified results
            matched_clusters = []
            for idx, score in enumerate(adjusted_scores):
                if score >= 0.01:  # Similarity threshold
                    cluster_id = list(clusters_data.keys())[idx]
                    cluster_info = clusters_data[cluster_id]
                    matched_clusters.append({
                        'cluster_id': cluster_id,
                        'similarity_score': float(score),
                        'cluster_name': cluster_info.get('name', '')
                    })

            matched_clusters.sort(key=lambda x: x['similarity_score'], reverse=True)
            matched_clusters = matched_clusters[:3]  # Select Top 3 clusters

            print(f"\nFound {len(matched_clusters)} matching clusters:")
            for cluster in matched_clusters:
                print(
                    f"- Cluster {cluster['cluster_id']}: {cluster['cluster_name']} (score: {cluster['similarity_score']})")

            result = {
                'user_id': user_data.get('userId'),
                'matched_clusters': matched_clusters,
                'preference_score': float(
                    np.mean([c['similarity_score'] for c in matched_clusters])) if matched_clusters else 0.0
            }

            print("\n=== Analysis Complete ===")
            return result

        except Exception as e:
            print(f"\nError in preference analysis: {str(e)}")
            import traceback
            traceback.print_exc()
            return self._create_empty_result(user_data.get('userId'))

    def _get_cluster_weights_from_actions(self, actions: List[Dict], clusters_data: Dict[str, Any]) -> Dict[str, float]:
        """Tracks how much user has interacted with each cluster"""
        cluster_weights = {}
        for action in actions:
            # Get weight for this type of action
            weight = self.action_weights.get(action.get('actionType'), 1.0)
            post = action.get('metadata', {}).get('post', {})

            # For each cluster this post belongs to
            if post and post.get('clusters'):
                for cluster_ref in post['clusters']:
                    cluster_oid = cluster_ref.get('$id', {}).get('$oid')
                    # Match MongoDB ObjectId to cluster_id
                    if cluster_oid:
                        for cluster_id, info in clusters_data.items():
                            if str(info.get('_id')) == cluster_oid:
                                # Add weighted interaction to cluster's total
                                cluster_weights[cluster_id] = cluster_weights.get(cluster_id, 0) + weight
                                break

        return cluster_weights

    def _process_user_tags(self, tags: List[Dict]) -> List[str]:
        """Processes a list of tag objects to extract and standardize their names"""
        print("\nProcessing user tags:")
        print(f"Input tags: {tags}")
        processed_tags = [
            tag.get('tagName', '').lower()              # Get tag name and convert to lowercase
            for tag in tags                             # Iterate through each tag object
            if isinstance(tag, dict)                    # Only process if it's a dictionary
               and tag.get('tagName')                   # And has a non-empty tagName
        ]
        print(f"Processed tags: {processed_tags}")
        return processed_tags

    def _process_user_actions(self, actions: List[Dict]) -> List[str]:
        """Extracts relevant keywords from user's actions"""
        print("\nProcessing user actions:")
        keywords = []
        for action in actions:
            print(f"\nProcessing action: {action.get('actionType')}")
            weight = self.action_weights.get(action.get('actionType'), 1.0)
            metadata = action.get('metadata', {})

            if action.get('actionType') == 'LIKE_POST' and metadata.get('post'):
                post = metadata['post']
                # Get words from post title
                if post.get('postMedia', {}).get('title'):
                    words = [word.lower() for word in post['postMedia']['title'].split()]
                    print(f"Adding title words: {words} (weight: {weight})")
                    # Add words multiple times based on action weight
                    keywords.extend(words * int(weight))

                # Get post tags
                if post.get('tags'):
                    tags = self._process_user_tags(post['tags'])
                    print(f"Adding post tags: {tags}")
                    keywords.extend(tags)

                # Get ingredients
                if post.get('recipe', {}).get('ingredientsWithMeasurements'):
                    ingredients = [
                        ing.get('name', '').lower()
                        for ing in post['recipe']['ingredientsWithMeasurements']
                        if ing.get('name')
                    ]
                    print(f"Adding ingredients: {ingredients}")
                    keywords.extend(ingredients)

            elif action.get('actionType') == 'COMMENT_POST' and metadata.get('commentContent'):
                words = [word.lower() for word in metadata['commentContent'].split()]
                print(f"Adding comment words: {words} (weight: {weight})")
                keywords.extend(words)

        print(f"\nFinal keywords from actions: {keywords}")
        return keywords

    def _create_empty_result(self, user_id: str) -> Dict[str, Any]:
        return {
            'user_id': user_id,
            'matched_clusters': [],
            'preference_score': 0.0
        }