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

package springreceiver;

import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.logging.Logger;

public class LogMessage implements MessageListener {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Autowired
    private ServiceForward forward;

    @Autowired
    private Counter counter;

    private String createPayload(int size) {
        final int sizeKb = size * 1024;
        final StringBuilder sb = new StringBuilder(sizeKb);
        for (int i = 0; i < sizeKb / 2; i++) { // divided by 2 because one char has 2 bytes
            sb.append('a');
        }
        return sb.toString();
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message.getBooleanProperty("messageThrowsException")) {
                throw new RuntimeException("Ooops!");
            }
        } catch (JMSException e) {
            // no-op
        }
        final String key;
        try {
            key = message.getStringProperty("key");
        } catch (JMSException e) {
            throw new InvalidStateException(e);
        }
        try {
            final long start = System.currentTimeMillis();
            final int numberOfForwardMessages = message.getIntProperty("numberOfForwardMessages");
            final boolean persistentForwardMessage = message.getBooleanProperty("persistentForwardMessage");
            this.forward.forwardMessage(
                    createPayload(message.getIntProperty("sizeOfForwardedMessage")),
                    numberOfForwardMessages,
                    persistentForwardMessage
            );
            logger.fine("Forwarded " + numberOfForwardMessages + " messages in " + (System.currentTimeMillis() - start) + " ms.");
        } catch (InterruptedException | JMSException e) {
            throw new InvalidStateException(e);
        }
        try {
            int sleep = message.getIntProperty("delayMessageProcess"); // seconds
            if (sleep > 0) {
                Thread.sleep(sleep);
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } catch (JMSException e) {
            // no-op
        }
        this.counter.updateCounter(key);
    }

}
