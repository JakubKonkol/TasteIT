from dataclasses import dataclass
from typing import List, Dict

@dataclass
class ClusterInfo:
    cluster_id: int
    name: str
    main_topics: List[str]
    keyword_weights: Dict[str, float]
    post_count: int
