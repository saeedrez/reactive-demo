package reactive.model;

import lombok.Data;
import reactor.core.publisher.Flux;

@Data
public class WrapperB<T> {
    private T t;
}
