package telemetry.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Autowired;

import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import telemetry.collector.config.KafkaConfig;
import telemetry.collector.service.Collector;
import telemetry.collector.exception.HandlerNotFoundException;
import telemetry.collector.model.hub.HubEventHandler;
import telemetry.collector.model.sensor.SensorEventHandler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
public class EventRpcController extends CollectorControllerGrpc.CollectorControllerImplBase {
    private final KafkaConfig kafkaConfig;
    private final Collector collector;
    private final Map<SensorEventProto.PayloadCase, SensorEventHandler> sensorEventHandlers;
    private final Map<HubEventProto.PayloadCase, HubEventHandler> hubEventHandlers;

    @Autowired
    public EventRpcController(KafkaConfig kafkaConfig,
                              Set<SensorEventHandler> sensorEventHandlerSet,
                              Set<HubEventHandler> hubEventHandlerSet,
                              Collector collector) {
        this.kafkaConfig = kafkaConfig;
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
        this.collector = collector;

        log.info("Topic for sensors messages: {}", kafkaConfig.getTopics().getSensors());
        log.info("Topic for hubs messages: {}", kafkaConfig.getTopics().getSensors());
    }

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        log.debug("Received sensor event message: {}", request);

        try {
            if (sensorEventHandlers.containsKey(request.getPayloadCase())) {
                SensorEventHandler sensorEventHandler = sensorEventHandlers.get(request.getPayloadCase());
                SpecificRecordBase specificRecordBase = sensorEventHandler.toAvro(request);

                log.debug("Sending sensor event message: {}", specificRecordBase);
                collector.send(kafkaConfig.getTopics().getSensors(), request.getHubId(), specificRecordBase);
            } else {
                log.warn("Couldn't find handler for sensor event: {}", request.getPayloadCase());
                throw new HandlerNotFoundException("Couldn't find handler for sensor event: " + request.getPayloadCase());
            }

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error(ex.getMessage());

            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);

            ex.printStackTrace(printWriter);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(ex.getLocalizedMessage())
                            .withCause(ex)
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
                collector.send(kafkaConfig.getTopics().getHubs(), request.getHubId(), specificRecordBase);
            } else {
                log.warn("Couldn't find handler for hub event: {}", request.getPayloadCase());
                throw new HandlerNotFoundException("Couldn't find handler for hub event: " + request.getPayloadCase());
            }

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error(ex.getMessage());

            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);

            ex.printStackTrace(printWriter);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(ex.getLocalizedMessage())
                            .withCause(ex)
            ));
        }
    }
}
