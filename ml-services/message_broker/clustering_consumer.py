import asyncio
import json
import os
from datetime import datetime
from kafka import KafkaConsumer, KafkaProducer
from content_clustering_service.content_clustering_service import ContentClusteringService

KAFKA_BROKER_URL = os.getenv("KAFKA_BROKER_URL", "localhost:29092")
REQUEST_TOPIC = "clustering-request"
RESPONSE_TOPIC = "clustering-response"


class ClusteringConsumer:
    def __init__(self):
        self.consumer = KafkaConsumer(
            REQUEST_TOPIC,
            bootstrap_servers=KAFKA_BROKER_URL,
            group_id='tasteit-group',
            auto_offset_reset='earliest',
            enable_auto_commit=True,
            max_poll_interval_ms=600000,
            value_deserializer=lambda x: json.loads(x.decode('utf-8')) if x else None
        )
        self.producer = KafkaProducer(
            bootstrap_servers=KAFKA_BROKER_URL,
            value_serializer=lambda x: json.dumps(x).encode('utf-8')
        )

        self.running = False
        self._consumer_task = None

    async def process_message(self, message):
        correlation_id = None
        try:
            posts = message.get("posts", [])
            print(f'Received {len(posts)} posts for clustering')
            correlation_id = message.get("correlationId")

            if not posts:
                print(f"No posts in message with correlationId {correlation_id}")
                return

            clustering_service = ContentClusteringService(
                n_topics=10,
                min_df=0.01,
                max_df=0.95,
                n_top_words=10
            )

            clustering_service.fit(posts)
            clustered_posts = clustering_service.predict(posts)
            cluster_summary = clustering_service.get_cluster_summary()
            for cluster_id, cluster_data in cluster_summary.items():
                cluster_document = {
                    "cluster_id": cluster_id,
                    "name": cluster_data["name"],
                    "main_topics": cluster_data["main_topics"],
                    "keyword_weights": dict(cluster_data["keyword_weights"]),
                    "post_count": cluster_data["post_count"],
                    "timestamp": datetime.now()
                }
                posts_assignments = [
                    {
                        "post_id": post.get("postId"),
                        "cluster_id": post.get("clusterId"),
                        "confidence": post.get("clusterConfidence")
                    }
                    for post in clustered_posts
                    if all(key in post for key in ["postId", "clusterId", "clusterConfidence"])
                ]

            response = {
                "correlationId": correlation_id,
                "clusters": cluster_summary,
                "posts_assignments": posts_assignments,
                "status": "success"
            }

            self.producer.send(RESPONSE_TOPIC, key=correlation_id.encode('utf-8'), value=response)
            self.producer.flush()

            print(f"Processed clustering for correlationId {correlation_id}")
        except Exception as e:
            error_response = {
                "correlationId": correlation_id,
                "status": "error",
                "message": str(e)
            }

            self.producer.send(
                RESPONSE_TOPIC,
                key=correlation_id.encode('utf-8') if correlation_id else None,
                value=error_response
            )
            self.producer.flush()

            print(f"Error processing message: {str(e)}")

    async def listen_to_messages(self):
        try:
            while self.running:
                msg_pack = self.consumer.poll(timeout_ms=1000)
                for tp, messages in msg_pack.items():
                    for message in messages:
                        if not self.running:
                            break
                        await self.process_message(message.value)

                await asyncio.sleep(0.1)
        except Exception as e:
            print(f"Kafka consumer error: {e}")
        finally:
            self.consumer.close()
            self.producer.close()


    def start(self):
        if not self.running:
            print("Clustering Consumer started.")
            self.running = True

            self._consumer_task = asyncio.create_task(self.listen_to_messages())

    def stop(self):
        print("Stopping Kafka clustering consumer...")
        self.running = False

        if self._consumer_task:
            self._consumer_task.cancel()

