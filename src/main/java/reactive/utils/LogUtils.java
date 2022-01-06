package reactive.utils;

import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

public class LogUtils {

    public static ExchangeFilterFunction logRequest() {
        return (clientRequest, next) -> {
            System.out.println("Request: Method: " + clientRequest.method() + "    url: " + clientRequest.url());
            System.out.println("Headers:");
            clientRequest.headers()
                    .forEach((name, values) -> values.forEach(value -> System.out.println(name + ": " + value)));
            return next.exchange(clientRequest);
        };
    }

    public static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            System.out.println("Response: " + clientResponse.headers().asHttpHeaders().get("property-header"));
            return Mono.just(clientResponse);
        });
    }

    // kotlin version
//    class MyFilter : ExchangeFilterFunction {
//
//        override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
//            return next.exchange(request).flatMap { response ->
//
//                    // log whenever you want here...
//                    println("request: ${request.url()}, response: ${response.statusCode()}")
//
//                Mono.just(response)
//            }
//        }
//    }

}
