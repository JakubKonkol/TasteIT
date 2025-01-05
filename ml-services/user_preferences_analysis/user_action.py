from dataclasses import dataclass
from typing import Dict, Any


@dataclass
class UserAction:
    action_type: str
    user_id: str
    post_id: str
    metadata: Dict[str, Any]
    timestamp: str