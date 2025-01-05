import asyncio
import sys
from message_broker.clustering_consumer import ClusteringConsumer
from message_broker.kafka_consumer_service import KafkaConsumerService
from message_broker.preference_analysis_consumer import PreferenceAnalysisConsumer
# Windows requires a different event loop policy
if sys.platform.lower().startswith('win'):
    asyncio.set_event_loop_policy(asyncio.WindowsSelectorEventLoopPolicy())

async def main():
    loop = asyncio.get_event_loop()
    consumer_service = KafkaConsumerService()

    # Tutaj mozemy dodac dowolna ilosc konsumerow
    # np zrobisz preference_analysis_consumer = PreferenceAnalysisConsumer()
    # consumer_service.add_consumer(preference_analysis_consumer)
    try:
        clustering_consumer = ClusteringConsumer()
        preference_analysis_consumer = PreferenceAnalysisConsumer()
        
        consumer_service.add_consumer(clustering_consumer)
        consumer_service.add_consumer(preference_analysis_consumer)
    except Exception as e:
        print(f"Error starting consumer: {str(e)}")
        print("Make sure the Kafka broker is running and the topic exists.")
        print("Exiting...")
        return

    consumer_service.setup_signal_handlers(loop)

    try:
        await consumer_service.start_consumers()
    except KeyboardInterrupt:
        print("Received keyboard interrupt, shutting down...")
        await consumer_service.shutdown()
    except Exception as e:
        print(f"Error in main loop: {str(e)}")
        await consumer_service.shutdown()

if __name__ == "__main__":
    asyncio.run(main())