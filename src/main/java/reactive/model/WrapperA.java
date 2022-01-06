package reactive.model;

import lombok.Data;
import reactive.server.ReactiveServiceApplication;
import reactor.core.publisher.Flux;

@Data
public class WrapperA {
    Flux<Event> fluxEvent = ReactiveServiceApplication.createEventsFlux();
}
