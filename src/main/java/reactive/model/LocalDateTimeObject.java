package reactive.model;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

@Data
public class LocalDateTimeObject {

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime arrivalTime;

    // the validation is only done if the field is present. Befcause notnull is not specified, it is an optional field.
    @Pattern(regexp = "^ *$|([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])[T](0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9])", message = "Wrong date-time format")
    String departureTime;

}
