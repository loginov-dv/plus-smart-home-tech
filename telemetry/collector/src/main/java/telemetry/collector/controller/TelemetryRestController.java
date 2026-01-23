package telemetry.collector.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import telemetry.collector.model.rest.hub.HubEvent;
import telemetry.collector.model.rest.sensor.SensorEvent;
import telemetry.collector.service.TelemetryRestService;

@Deprecated
@RequiredArgsConstructor
@Slf4j
public class TelemetryRestController {
    private final TelemetryRestService telemetryService;

    @PostMapping("/sensors")
    public void collectSensorEvent(@Valid @RequestBody SensorEvent sensorEvent) {
        log.debug("POST /events/sensors: {}", sensorEvent);
        log.debug("Sensor event type: {}", sensorEvent.getType());
        telemetryService.sendSensorEvent(sensorEvent);
    }

    @PostMapping("/hubs")
    public void collectHubEvent(@Valid @RequestBody HubEvent hubEvent) {
        log.debug("POST /events/hubs: {}",hubEvent);
        log.debug("Hub event type: {}", hubEvent.getType());
        telemetryService.sendHubEvent(hubEvent);
    }
}
