/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package springsender;

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
public class MessagesServiceImpl implements MessagesService {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Resource
    private JmsOperations jmsTemplate;

    @Override
    public void postMessage(
            String key,
            int numberOfMessages,
            int sizeOfMessage,
            int delayMessageProcess,
            boolean messageThrowsException,
            boolean persistentMessage,
            int numberOfForwardMessages,
            int sizeOfForwardedMessage,
            boolean persistentForwardMessage
    ) {
        final int threads = 4;
        final ExecutorService executor = Executors.newFixedThreadPool(threads);
        final BlockingQueue<Runnable> runningTasks = new ArrayBlockingQueue<>(threads);
        final String payload = createPayload(sizeOfMessage);
        final int deliveryMode = persistentMessage ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT;
        final long start = System.currentTimeMillis();
        final AtomicInteger counter = new AtomicInteger(0);
        while (counter.get() < numberOfMessages) {
            final Runnable task = new Task(
                    runningTasks,
                    counter,
                    key,
                    delayMessageProcess,
                    messageThrowsException,
                    payload,
                    deliveryMode,
                    sizeOfForwardedMessage,
                    numberOfForwardMessages,
                    persistentForwardMessage
            );
            final boolean taskReady;
            try {
                taskReady = runningTasks.offer(task, 5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new InvalidStateException(e);
            }
            if (taskReady) {
                counter.incrementAndGet();
                executor.submit(task);
            } else {
                logger.info("waiting for free slot...");
            }
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                logger.warning("unable to send all the messages");
            }
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "unable to send all the messages", e);
        }
        logger.info("Messages sent in " + (System.currentTimeMillis() - start) + " ms.");
        logger.info("    number_of_messages = " + numberOfMessages);
        logger.info("    size_of_message = " + sizeOfMessage + "kb");
        logger.info("    delay_message_process = " + delayMessageProcess + " seconds");
        logger.info("    message_throws_exception = " + messageThrowsException);
        logger.info("    persistent_message = " + persistentMessage);
        logger.info("    [forward] number_of_forward_messages = " + numberOfForwardMessages);
        logger.info("    [forward] size_of_forward_message = " + sizeOfForwardedMessage + "kb");
        logger.info("    [forward] persistent_forward_message = " + persistentForwardMessage);
    }

    class Task implements Runnable {
        private final BlockingQueue<Runnable> runningTasks;
        private final AtomicInteger counter;
        private final String key;
        private final int delayMessageProcess;
        private final boolean messageThrowsException;
        private final String payload;
        private final int deliveryMode;
        private final int sizeOfForwardedMessage;
        private final int numberOfForwardMessages;
        private final boolean persistentForwardMessage;

        Task(
                final BlockingQueue<Runnable> runningTasks,
                final AtomicInteger counter,
                final String key,
                final int delayMessageProcess,
                final boolean messageThrowsException,
                final String payload,
                final int deliveryMode,
                final int sizeOfForwardedMessage,
                final int numberOfForwardMessages,
                final boolean persistentForwardMessage
        ) {
            this.runningTasks = runningTasks;
            this.counter = counter;
            this.key = key;
            this.delayMessageProcess = delayMessageProcess;
            this.messageThrowsException = messageThrowsException;
            this.payload = payload;
            this.deliveryMode = deliveryMode;
            this.sizeOfForwardedMessage = sizeOfForwardedMessage;
            this.numberOfForwardMessages = numberOfForwardMessages;
            this.persistentForwardMessage = persistentForwardMessage;
        }

        @Override
        public void run() {
            try {
                jmsTemplate.send("logsQueue", session -> {
                    final Message msg = session.createMessage();
                    msg.setStringProperty("key", key);
                    msg.setObjectProperty("payload", payload);
                    msg.setIntProperty("delayMessageProcess", delayMessageProcess);
                    msg.setBooleanProperty("messageThrowsException", messageThrowsException);
                    msg.setIntProperty("sizeOfForwardedMessage", sizeOfForwardedMessage);
                    msg.setIntProperty("numberOfForwardMessages", numberOfForwardMessages);
                    msg.setBooleanProperty("persistentForwardMessage", persistentForwardMessage);
                    msg.setJMSDeliveryMode(deliveryMode);
                    return msg;
                });
            } catch (Exception e) {
                logger.log(Level.WARNING, "JMS Connection failed. -> " + e.getMessage());
                counter.decrementAndGet(); // will try again later
            } finally {
                runningTasks.remove(this);
            }
        }
    }

    private String createPayload(int size) {
        final int sizeKb = size * 1024;
        final StringBuilder sb = new StringBuilder(sizeKb);
        for (int i = 0; i < sizeKb / 2; i++) { // divided by 2 because one char has 2 bytes
            sb.append('a');
        }
        return sb.toString();
    }

}
