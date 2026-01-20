package telemetry.collector.model.rpc.hub;

import com.google.protobuf.Timestamp;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.grpc.telemetry.event.DeviceRemovedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Instant;

@Component
public class DeviceRemovedEventHandler implements HubEventHandler {

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_REMOVED;
    }

    @Override
    public SpecificRecordBase toAvro(HubEventProto hubEventProto) {
        DeviceRemovedEventProto deviceRemovedEventProto = hubEventProto.getDeviceRemoved();
        DeviceRemovedEventAvro deviceRemovedEventAvro = DeviceRemovedEventAvro.newBuilder()
                .setId(deviceRemovedEventProto.getId())
                .build();
        Timestamp timestamp = hubEventProto.getTimestamp();

        return HubEventAvro.newBuilder()
                .setHubId(hubEventProto.getHubId())
                .setTimestamp(Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()))
                .setPayload(deviceRemovedEventAvro)
                .build();
    }
}
