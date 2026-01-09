package telemetry.collector.exception;

public class SendingMessageException extends RuntimeException {
    public SendingMessageException(String message) {
        super(message);
    }
}
