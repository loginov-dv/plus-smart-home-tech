package telemetry.collector.service;

import telemetry.collector.model.rest.hub.HubEvent;
import telemetry.collector.model.rest.sensor.SensorEvent;

public interface TelemetryRestService {

    void sendSensorEvent(SensorEvent sensorEvent);

    void sendHubEvent(HubEvent hubEvent);
}
