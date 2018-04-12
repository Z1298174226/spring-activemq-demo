package springreceiver;

import org.springframework.jms.core.JmsOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.jms.DeliveryMode;
import javax.jms.Message;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ServiceForwardImpl implements ServiceForward {

    @Resource
    private JmsOperations jmsTemplate;

    @Override
    public void forwardMessage(
            final Object payload,
            final int numberOfMessages,
            final boolean persistentMessage
    ) throws InterruptedException {
        final int deliveryMode = persistentMessage ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT;
        final AtomicInteger counter = new AtomicInteger(0);
        while (counter.incrementAndGet() <= numberOfMessages) {
            jmsTemplate.send("logsQueueB", session -> {
                final Message msg = session.createMessage();
                msg.setObjectProperty("payload", payload);
                msg.setJMSDeliveryMode(deliveryMode);
                return msg;
            });
        }
    }
}
