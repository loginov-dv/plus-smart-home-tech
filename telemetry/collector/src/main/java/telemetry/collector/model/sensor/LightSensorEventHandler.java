package telemetry.collector.model.sensor;

import com.google.protobuf.Timestamp;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.grpc.telemetry.event.LightSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Instant;

@Component
public class LightSensorEventHandler implements SensorEventHandler {

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.LIGHT_SENSOR;
    }

    @Override
    public SpecificRecordBase toAvro(SensorEventProto sensorEventProto) {
        LightSensorProto lightSensorProto = sensorEventProto.getLightSensor();
        LightSensorAvro lightSensorAvro = LightSensorAvro.newBuilder()
                .setLinkQuality(lightSensorProto.getLinkQuality())
                .setLuminosity(lightSensorProto.getLuminosity())
                .build();
        Timestamp timestamp = sensorEventProto.getTimestamp();

        return SensorEventAvro.newBuilder()
                .setId(sensorEventProto.getId())
                .setHubId(sensorEventProto.getHubId())
                .setTimestamp(Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()))
                .setPayload(lightSensorAvro)
                .build();
    }
}
