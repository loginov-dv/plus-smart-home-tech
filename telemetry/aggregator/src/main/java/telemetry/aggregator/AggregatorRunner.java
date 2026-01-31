package telemetry.aggregator;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import telemetry.aggregator.service.Aggregator;

@Component
@RequiredArgsConstructor
public class AggregatorRunner implements CommandLineRunner {
    private final Aggregator aggregator;

    @Override
    public void run(String... args) throws Exception {
        aggregator.start();
    }
}
