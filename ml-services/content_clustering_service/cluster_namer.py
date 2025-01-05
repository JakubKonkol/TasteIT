from collections import Counter
from typing import Dict, Any, List

import numpy as np


class ClusterNamer:
    def __init__(self):
        self.tag_frequency = Counter()
        self.tag_associations = {}

    def update_tag_data(self, post: Dict[str, Any]):
        tags = [tag.get("tagName", "").lower().strip()
                for tag in post.get("tags", [])
                if isinstance(tag, dict) and tag.get("tagName", "").strip()]

        if not tags:
            return
        self.tag_frequency.update(tags)

        for i, tag1 in enumerate(tags):
            for tag2 in tags[i + 1:]:
                if tag1 not in self.tag_associations:
                    self.tag_associations[tag1] = Counter()
                if tag2 not in self.tag_associations:
                    self.tag_associations[tag2] = Counter()

                self.tag_associations[tag1][tag2] += 1
                self.tag_associations[tag2][tag1] += 1

    def name_cluster(self, topics: List[str], weights: Dict[str, float]) -> str:
        topic_words = set(' '.join(topics).lower().split())
        relevant_tags = []
        for word in topic_words:
            if word in self.tag_frequency:
                topic_weight = weights.get(word, 0)
                tag_freq = self.tag_frequency[word]
                combined_score = topic_weight * np.log1p(tag_freq)
                relevant_tags.append((word, combined_score))

        if relevant_tags:
            relevant_tags.sort(key=lambda x: x[1], reverse=True)
            primary_tag = relevant_tags[0][0]
            if primary_tag in self.tag_associations:
                related_tags = [
                    (tag, count)
                    for tag, count in self.tag_associations[primary_tag].items()
                    if tag in topic_words
                ]

                if related_tags:
                    related_tags.sort(key=lambda x: x[1], reverse=True)
                    if related_tags[0][0] in weights:
                        return f"{primary_tag.title()} & {related_tags[0][0].title()}"

            return primary_tag.title()

        sorted_topics = sorted(weights.items(), key=lambda x: x[1], reverse=True)
        top_two = [word.title() for word, _ in sorted_topics[:2]]
        return f"{top_two[0]} & {top_two[1]}"
