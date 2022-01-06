package reactive.server;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import reactive.CustomRuntimeException;

@Aspect
@Component
@Slf4j
// this is a behind the scenes hook to the method eventById that is specified in the execution part.
public class AspectForController {
    @Before("execution(* reactive.server.ReactiveServiceApplication.eventById(..)) && args(id)")
//    public void validateEventId(JoinPoint jp) {
    public void validateEventId(long id) {
        log.info("==> Aspect intercept...");
        if (String.valueOf(id).equals("11")) {
            log.error("==> Event id is wrong: " + id);
            throw new CustomRuntimeException("Id is wrong");
        }

//        if (String.valueOf(jp.getArgs()[0]).equals("11")) {
//            log.error("==> Event id is wrong: " + jp.getArgs()[0]);
//            throw new CustomRuntimeException("Id is wrong");
//        }
    }

}
