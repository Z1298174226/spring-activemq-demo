package springreceiver;

public interface ServiceForward {
    void forwardMessage(Object payload, int numberOfMessages, boolean persistentMessage) throws InterruptedException;
}
