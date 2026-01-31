package telemetry.analyzer.handlers.hub;

public interface HubEventHandler {
    Class<?> getType();

    void process(Object payload, String hubId);
}
