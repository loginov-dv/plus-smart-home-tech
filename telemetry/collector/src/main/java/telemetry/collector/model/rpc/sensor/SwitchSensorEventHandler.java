package telemetry.collector.model.rpc.sensor;

import com.google.protobuf.Timestamp;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SwitchSensorProto;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;

import java.time.Instant;

@Component
public class SwitchSensorEventHandler implements SensorEventHandler {

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.SWITCH_SENSOR;
    }

    @Override
    public SpecificRecordBase toAvro(SensorEventProto sensorEventProto) {
        SwitchSensorProto switchSensorProto = sensorEventProto.getSwitchSensor();
        SwitchSensorAvro switchSensorAvro = SwitchSensorAvro.newBuilder()
                .setState(switchSensorProto.getState())
                .build();
        Timestamp timestamp = sensorEventProto.getTimestamp();

        return SensorEventAvro.newBuilder()
                .setId(sensorEventProto.getId())
                .setHubId(sensorEventProto.getHubId())
                .setTimestamp(Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()))
                .setPayload(switchSensorAvro)
                .build();
    }
}
