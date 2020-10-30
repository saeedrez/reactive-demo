package reactive.client;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactive.model.Event;
import reactive.model.Person;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SpringBootApplication
public class ReactiveClientApplication {

    @Bean
    WebClient client() {
        return WebClient.create("http://localhost:8080");
    }

    /*
        this client connects to 2 services, one annotation based (getrequest...) and one serverResponse based, both reactive.

        annotation based service, reactive-demo (part of this service), returns:
        /events: stream
        /eventsAsJson: Flux<Event>
        /eventsAsJson2: Mono<List<Event>> which is the same as Flux<Event> when looking as the json payload in http.
        /textsAsStream
        /textsAsJson: Flux<String>
        /textsAsJson2: Mono<List<String>> which is the same as Flux<String> when looking as the json payload in http.
        /getMonoCustomRuntimeException: returns Mono.error(CustomException extends RuntimeException(message) but has a customized httperrorcode)
        /getFluxCustomRuntimeException: Same as above just with flux

        reactive-demo-router service endpoints:  all data on the serverside is converted to http status, header and JSON body by spring
        /getFluxPersons
        /getFluxIntegeres
        /getMonoPerson
        /getMonoEmpty
        /getRuntimeException: throw new runtimeexception
        /getCustomException: cannot throw back a checked-exception. So customException is captured and replaced by a customRuntimeException
        /getCustomRuntimeException: throw new CustomRuntimeException
        /getNotFoundExceptionThruServerResponse: return ServerResponse.notFound().build();
        /getBadRequestThruServerResponse: return ServerResponse.badRequest().build();
     */


    @Bean
    CommandLineRunner demo (WebClient client) {
        return args -> {
            client
                    .get()
                    .uri("/eventsAsJson")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .log()
                    .flatMapMany(cr -> cr.bodyToFlux(Event.class))
                    .subscribe(System.out::println);
        };

        /*
        return args -> {
            client
                    .get()
                    .uri("/getRuntimeException")// this is another, reactiveDemoService, which returns person and/or integer.
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(HttpStatus::is5xxServerError,
                            clientResponse -> {
                                Mono<String> errorMsg = clientResponse.bodyToMono(String.class);
                                return errorMsg.flatMap(msg -> {
                                    System.out.println("Webclient side: Error message: " + msg);
                                    return Mono.empty();
                                });
                            }
                            )
                    .bodyToMono(Person.class)
                    .subscribe(System.out::println);

        };

         */
        /*
        return args -> {
            client
                    .get()
                    .uri("/getMonoPerson")// this is another, reactiveDemoService, which returns person and/or integer.
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(Person.class)
                    .subscribe(System.out::println);

        };

         */
        /*
       return args -> {
            client
                    .get()
                    .uri("/getMonoEmpty")// this is another, reactiveDemoService, which returns person and/or integer.
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(ResponseEntity.class)
                    .subscribe();

        };

         */
/*
        return args -> {
            client
                    .get()
                    .uri("/getFluxPersons")// this is another, reactiveDemoService, which returns person and/or integer.
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToFlux(Person.class)
                    .subscribe(System.out::println);

        };
 */

        // http payload is a json with array of Event inside.
        /*
        return args -> {
            client
                    .get()
                    .uri("/eventsAsJson")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .log()
                    .flatMapMany(cr -> cr.bodyToFlux(Event.class))
                    .subscribe(System.out::println);
        };

         */
//    @Bean
//    CommandLineRunner demo (WebClient client) {
//        return args -> {
//            client
//                    .get()
//                    .uri("/eventsAsJson2")
//                    .accept(MediaType.APPLICATION_JSON)
//                    .retrieve()
//                    .bodyToFlux(Event.class)
//                    .subscribe(System.out::println);
//
//        };

        // json payload with array of event coming in. Here we choose to receive it as as an mono of array (one object) (as oppose to bodyToFlux which is easier)
//        return args -> {
//            client
//                    .get()
//                    .uri("/eventsAsJson2")
//                    .accept(MediaType.APPLICATION_JSON)
//                    .retrieve()
//                    .bodyToMono(Event[].class)
//                    .log()
////                    .flatMapMany(Flux::fromIterable)
//                    .flatMapMany(ids -> Flux.fromArray(ids))
//                    .log()
//                    .subscribe(System.out::println);
//
//        };

//        return args -> {
//            client
//                    .get()
//                    .uri("/events")
//                    .accept(MediaType.TEXT_EVENT_STREAM)
//                    .exchange()
//                    .log()
//                    .flatMapMany(cr -> cr.bodyToFlux(Event.class))
//                    .subscribe(System.out::println);
//        };
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(ReactiveClientApplication.class)
                .properties(Collections.singletonMap("server.port", "8081"))
                .run(args);
    }
}
