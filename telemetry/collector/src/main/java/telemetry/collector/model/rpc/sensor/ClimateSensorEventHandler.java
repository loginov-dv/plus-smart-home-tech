package telemetry.collector.model.rpc.sensor;

import com.google.protobuf.Timestamp;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.grpc.telemetry.event.ClimateSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Instant;

@Component
public class ClimateSensorEventHandler implements SensorEventHandler {

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.CLIMATE_SENSOR;
    }

    @Override
    public SpecificRecordBase toAvro(SensorEventProto sensorEventProto) {
        ClimateSensorProto climateSensorProto = sensorEventProto.getClimateSensor();
        ClimateSensorAvro climateSensorAvro = ClimateSensorAvro.newBuilder()
                .setTemperatureC(climateSensorProto.getTemperatureC())
                .setHumidity(climateSensorProto.getHumidity())
                .setCo2Level(climateSensorProto.getCo2Level())
                .build();
        Timestamp timestamp = sensorEventProto.getTimestamp();

        return SensorEventAvro.newBuilder()
                .setId(sensorEventProto.getId())
                .setHubId(sensorEventProto.getHubId())
                .setTimestamp(Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()))
                .setPayload(climateSensorAvro)
                .build();
    }
}
