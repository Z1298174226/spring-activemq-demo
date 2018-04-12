package springreceiver;

import javax.jms.Message;
import javax.jms.MessageListener;

public class TimingMessageListener implements MessageListener {

    private MessageListener delegate;

    public void setDelegate(final MessageListener delegate) {
        this.delegate = delegate;
    }

    public MessageListener getDelegate() {
        return delegate;
    }

    @Override
    public void onMessage(final Message message) {
        long start = System.currentTimeMillis();
        try {
            delegate.onMessage(message);
        } finally {
            long time = System.currentTimeMillis() - start;
            System.out.println("Message Listener took: " + time + " ms");
        }
    }
}
