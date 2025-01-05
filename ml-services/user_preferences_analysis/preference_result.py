from dataclasses import dataclass
from typing import List, Dict, Any


@dataclass
class PreferenceResult:
    user_id: str
    matched_clusters: List[Dict[str, Any]]
    preference_score: float