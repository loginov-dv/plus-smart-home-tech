package telemetry.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import telemetry.collector.KafkaCollectorProducer;
import telemetry.collector.exception.HandlerNotFoundException;
import telemetry.collector.model.rpc.hub.HubEventHandler;
import telemetry.collector.model.rpc.sensor.SensorEventHandler;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
public class EventRpcController extends CollectorControllerGrpc.CollectorControllerImplBase {
    private final KafkaCollectorProducer producer;
    private final String sensorsTopic;
    private final String hubsTopic;
    private final Map<SensorEventProto.PayloadCase, SensorEventHandler> sensorEventHandlers;
    private final Map<HubEventProto.PayloadCase, HubEventHandler> hubEventHandlers;

    @Autowired
    public EventRpcController(Set<SensorEventHandler> sensorEventHandlerSet,
                              Set<HubEventHandler> hubEventHandlerSet,
                              KafkaCollectorProducer producer,
                              @Value("${telemetry.collector.kafka.sensors.topic}") String sensorsTopic,
                              @Value("${telemetry.collector.kafka.hubs.topic}") String hubsTopic) {
        this.sensorEventHandlers = sensorEventHandlerSet.stream()
                .collect(Collectors.toMap(
                        SensorEventHandler::getMessageType,
                        Function.identity()
                ));
        this.hubEventHandlers = hubEventHandlerSet.stream()
                .collect(Collectors.toMap(
                        HubEventHandler::getMessageType,
                        Function.identity()
                ));

        log.info("Topic for sensors messages: {}", sensorsTopic);
        log.info("Topic for hubs messages: {}", hubsTopic);

        this.producer = producer;
        this.sensorsTopic = sensorsTopic;
        this.hubsTopic = hubsTopic;
    }

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        log.debug("Received sensor event message: {}", request);

        try {
            if (sensorEventHandlers.containsKey(request.getPayloadCase())) {
                SensorEventHandler sensorEventHandler = sensorEventHandlers.get(request.getPayloadCase());
                SpecificRecordBase specificRecordBase = sensorEventHandler.toAvro(request);

                log.debug("Sending sensor event message: {}", specificRecordBase);
                producer.send(sensorsTopic, request.getHubId(), specificRecordBase);
            } else {
                log.warn("Couldn't find handler for sensor event: {}", request.getPayloadCase());
                throw new HandlerNotFoundException("Couldn't find handler for sensor event: " + request.getPayloadCase());
            }

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error(e.getMessage());
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        log.debug("Received hub event message: {}", request);

        try {
            if (hubEventHandlers.containsKey(request.getPayloadCase())) {
                HubEventHandler hubEventHandler = hubEventHandlers.get(request.getPayloadCase());
                SpecificRecordBase specificRecordBase = hubEventHandler.toAvro(request);

                log.debug("Sending hub event message: {}", specificRecordBase);
                producer.send(hubsTopic, request.getHubId(), specificRecordBase);
            } else {
                log.warn("Couldn't find handler for hub event: {}", request.getPayloadCase());
                throw new HandlerNotFoundException("Couldn't find handler for hub event: " + request.getPayloadCase());
            }

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error(e.getMessage());
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}
