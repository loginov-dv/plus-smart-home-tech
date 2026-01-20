package telemetry.collector.model.rpc.hub;

import com.google.protobuf.Timestamp;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.grpc.telemetry.event.DeviceAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import telemetry.collector.exception.UnknownTypeException;

import java.time.Instant;

@Component
public class DeviceAddedEventHandler implements HubEventHandler {

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_ADDED;
    }

    @Override
    public SpecificRecordBase toAvro(HubEventProto hubEventProto) {
        DeviceAddedEventProto deviceAddedEventProto = hubEventProto.getDeviceAdded();
        DeviceTypeAvro deviceTypeAvro;

        try {
            deviceTypeAvro = DeviceTypeAvro.valueOf(deviceAddedEventProto.getType().name());
        } catch (IllegalArgumentException ex) {
            throw new UnknownTypeException("Unknown device type: " + deviceAddedEventProto.getType().name());
        }

        DeviceAddedEventAvro deviceAddedEventAvro = DeviceAddedEventAvro.newBuilder()
                .setId(deviceAddedEventProto.getId())
                .setType(deviceTypeAvro)
                .build();
        Timestamp timestamp = hubEventProto.getTimestamp();

        return HubEventAvro.newBuilder()
                .setHubId(hubEventProto.getHubId())
                .setTimestamp(Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()))
                .setPayload(deviceAddedEventAvro)
                .build();
    }
}
