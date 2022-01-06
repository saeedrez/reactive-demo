package reactive;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN)
public class CustomRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -5970845585469454688L;

    public CustomRuntimeException(String type) {
        System.out.println(type + ": throw CustomException!");
    }
}