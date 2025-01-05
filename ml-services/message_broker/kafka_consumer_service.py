import asyncio
import signal
import sys

class KafkaConsumerService:
    def __init__(self):
        self.consumers = []
        self.running = True
        self.main_task = None

    def add_consumer(self, consumer):
        self.consumers.append(consumer)

    async def start_consumers(self):
        print("Starting kafka consumers...")
        for consumer in self.consumers:
            consumer.start()

        try:
            while True:
                await asyncio.sleep(1)
        except asyncio.CancelledError:
            pass
        finally:
            self.stop_consumers()

    def stop_consumers(self):
        for consumer in self.consumers:
            consumer.stop()
        self.running = False

    def setup_signal_handlers(self, loop):
        try:
            for sig in (signal.SIGINT, signal.SIGTERM):
                loop.add_signal_handler(
                    sig,
                    lambda: asyncio.create_task(self.shutdown(sig))
                )
        except NotImplementedError:
            if sys.platform.lower().startswith('win'):
                pass # Windows does not support signals

    async def shutdown(self, sig=None):
        print(f"Received exit signal {sig}")

        self.stop_consumers()

        tasks = [t for t in asyncio.all_tasks() if t is not asyncio.current_task()]

        for task in tasks:
            task.cancel()

        await asyncio.gather(*tasks, return_exceptions=True)

        loop = asyncio.get_running_loop()
        loop.stop()
        sys.exit(0)