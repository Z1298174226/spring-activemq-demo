package springreceiver;

import org.springframework.jms.core.JmsOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.jms.DeliveryMode;
import javax.jms.Message;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ServiceForwardImpl implements ServiceForward {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Resource
    private JmsOperations jmsTemplate;

    @Override
    public void forwardMessage(
            final Object payload,
            final int numberOfMessages,
            final boolean persistentMessage
    ) throws InterruptedException {
        final int threads = 4;
        final ExecutorService executor = Executors.newFixedThreadPool(threads);
        final BlockingQueue<Runnable> runningTasks = new ArrayBlockingQueue<>(threads);
        final int deliveryMode = persistentMessage ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT;
        final AtomicInteger counter = new AtomicInteger(0);
        while (counter.get() < numberOfMessages) {
            final Runnable task = new Task(
                    runningTasks,
                    counter,
                    payload,
                    deliveryMode
            );
            if (runningTasks.offer(task, 1, TimeUnit.MINUTES)) {
                counter.incrementAndGet();
                executor.submit(task);
            } else {
                logger.info("waiting for free slot...");
            }
        }
        executor.shutdown();
        if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
            logger.warning("unable to send all the messages");
        }
    }

    class Task implements Runnable {
        private final BlockingQueue<Runnable> runningTasks;
        private final AtomicInteger counter;
        private final Object payload;
        private final int deliveryMode;

        Task(
                BlockingQueue<Runnable> runningTasks,
                AtomicInteger counter,
                final Object payload,
                final int deliveryMode
        ) {
            this.runningTasks = runningTasks;
            this.counter = counter;
            this.payload = payload;
            this.deliveryMode = deliveryMode;
        }

        @Override
        public void run() {
            try {
                jmsTemplate.send("logsQueueB", session -> {
                    final Message msg = session.createMessage();
                    msg.setObjectProperty("payload", payload);
                    msg.setJMSDeliveryMode(deliveryMode);
                    return msg;
                });
            } catch (Exception e) {
                logger.log(Level.WARNING, "JMS Connection failed.", e.getMessage());
                counter.decrementAndGet(); // will try again later
            } finally {
                runningTasks.remove(this);
            }
        }
    }
}
