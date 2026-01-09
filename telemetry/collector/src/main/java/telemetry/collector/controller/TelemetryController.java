package telemetry.collector.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import telemetry.collector.model.hub.HubEvent;
import telemetry.collector.model.sensor.SensorEvent;
import telemetry.collector.service.TelemetryService;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class TelemetryController {
    private final TelemetryService telemetryService;

    @PostMapping("/sensors")
    public void collectSensorEvent(@Valid @RequestBody SensorEvent sensorEvent) {
        log.debug("POST /events/sensors: {}", sensorEvent);
        telemetryService.sendSensorEvent(sensorEvent);
    }

    @PostMapping("/hubs")
    public void collectHubEvent(@Valid @RequestBody HubEvent hubEvent) {
        log.debug("POST /events/hubs: {}",hubEvent);
        telemetryService.sendHubEvent(hubEvent);
    }
}
