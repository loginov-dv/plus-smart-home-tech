package telemetry.collector.service;

import telemetry.collector.model.hub.HubEvent;
import telemetry.collector.model.sensor.SensorEvent;

public interface TelemetryService {

    void sendSensorEvent(SensorEvent sensorEvent);

    void sendHubEvent(HubEvent hubEvent);
}
