package reactive.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactive.CustomRuntimeException;
import reactive.model.Event;
import reactive.model.WrapperA;
import reactive.model.WrapperB;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static reactor.core.scheduler.Schedulers.parallel;

//@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableAspectJAutoProxy
@SpringBootApplication
@RestController
public class ReactiveServiceApplication {

    // example monos and fluxes
    private static Mono<String> monoEmpty = Mono.empty();
    private static Mono<String> monoEmpty2 = Mono.empty();
    private static Mono<String> monoDataStr = Mono.just("JSA");
    private static Mono<String> monoDataStr2 = Mono.just("JSA2");
    private static Mono<Object> monoError = Mono.error(new CustomRuntimeException("monoError: Mono Error"));
    private static Mono<Object> monoError2 = Mono.error(new CustomRuntimeException("monoError2: Mono Error"));

    private static Flux<String> fluxEmpty = Flux.empty();
    private static Flux<Integer> fluxDataInt = Flux.just(1, 10, 100, 1000);
    private static Flux<Integer> fluxDataInt2 = Flux.just(-1, -10, -100, -1000);
    private static Flux<String> fluxDataIntAsStr = Flux.just("1", "10", "100", "1000");
    private static Flux<String> fluxDataStr = Flux.just("Abondon ", "all ", "hope ", "who...");
    private static Flux<String> fluxDataStr2 = Flux.just("Abondon2 ", "all2 ", "hope2 ", "who2...");
    private static Flux<String> fluxDataStr3 = Flux.just("Abondon3 ", "all3 ", "hope3 ", "who3...");
    private static List<String> listStr = Arrays.asList("l-Abondon ", "l-all ", "l-hope ", "l-who..."); // fized sized list: cannot be modified
    private static List<String> listStr2 = Arrays.asList("l-Abondon2 ", "l-all2 ", "l-hope2 ", "l-who2..."); // fized sized list: cannot be modified
    private static Flux<String> fluxFromList = Flux.fromIterable(listStr);
    private static Flux<Long> fluxInterval = Flux.interval(Duration.ofMillis(2000));
    private static Flux<Object> fluxError = Flux.error(new CustomRuntimeException("fluxError: Flux Error"));
    private static Flux<Event> fluxDataEvents = createEventsFlux();
    private static List<Event> listDataEvents = createEventsList();
    private static List<Event> listDataEventsCreatedFromAsList =
            Arrays.asList(new Event(1, new Date()), new Event(2, new Date())); // asList returns a fixed-sized list that cannot be modified in any way
    private static Flux<String> fluxStringWithInterval = Flux.zip(fluxDataStr, fluxInterval).map(Tuple2::getT1);
    private static Flux<Integer> fluxIntegerWithInterval = Flux.zip(fluxDataInt, fluxInterval).map(Tuple2::getT1);
    private static Flux<Object> fluxWithErrorAtTheEnd = Flux.concat(fluxDataStr, fluxError);

    //////////////////////////////////////////////////////////////////////////////////////////
    public static void main(String[] args) {
        SpringApplication.run(ReactiveServiceApplication.class, args);

//        testBoolean();
//        testReduce();
//        testFluxMerge();
//        testFlatMapWithWindow();
//        getFibonacciSequence();
//        getFluxFibonacciSequence();
//        testFluxGroupBy();
//        testExceptionsAndErrors();
//        switchIfEmptyTests();
//        repeatWhenEmptyTest();
//        testCallableAndDefer();
//        useMultipleSubscribesToTheSameFlux();
//        imperativeCodeUsingDataFromPipeline();
//        oneLongPipelineSeparatedByImperativeCode();
//        testImperativeReactiveCombination();
        testEnrichImperativeWithDataFromReactivePipeline();
//        testMonosAndFluxes();
//        thenTests();
//        testFlux();
//        filterStuff();
//        switchIfEmptyTests();

    }
    //////////////////////////////////////////////////////////////////////////////////////////


    // endpoints for this rest service

    @GetMapping("/events/{id}")
    public Mono<Event> eventById(@PathVariable long id) {
        System.out.println("==> eventById: return flux");
        return Mono.just(new Event(id, new Date()));
    }

    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Event> events() {
        System.out.println("==> Infinite events: return flux...");
        Flux<Event> eventflux = Flux.fromStream(Stream.generate(() -> new Event(System.currentTimeMillis(), new Date())));
        Flux<Long> durationFlux = Flux.interval(Duration.ofSeconds(2));
        return Flux.zip(eventflux, durationFlux).map(Tuple2::getT1);
    }

    @GetMapping(value = "/eventsAsJson", produces = MediaType.APPLICATION_JSON_VALUE)
    Flux<Event> eventsAsJson() {
        System.out.println("==> events as json: returning flux<Event>...");
//        Flux<Long> durationFlux = Flux.interval(Duration.ofSeconds(2));
        return fluxDataEvents;
    }

    @GetMapping(value = "/eventsAsJsonResponseEntity", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<Event>> eventsAsJsonResponseEntity() {
        System.out.println("==> events as json: returning Mono<ResponseEntity<Event>...");
        return Mono.just(ResponseEntity.ok(createOneEvent()));
    }

    @GetMapping(value = "/eventsAsJsonResponseEntityFlux", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<Flux<Event>>> eventsAsJsonResponseEntityFlux() {
        System.out.println("==> events as json: returning Mono<ResponseEntity<Flux<Event>>>...");
        return Mono.just(ResponseEntity.ok(createEventsFlux()));
    }

    @GetMapping(value = "/eventsAsJsonResponseEntityList", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<List<Event>>> eventsAsJsonResponseEntityList() {
        System.out.println("==> events as json: returning Mono<ResponseEntity<Flux<Event>>>...");
        return Mono.just(ResponseEntity.ok(createEventsList()));
    }

    @GetMapping(value = "/eventsAsJsonFluxResponseEntityList", produces = MediaType.APPLICATION_JSON_VALUE)
    Flux<ResponseEntity<List<Event>>> eventsAsJsonFluxResponseEntityList() {
        System.out.println("==> events as json: returning Mono<ResponseEntity<Flux<Event>>>...");
        return Flux.just(ResponseEntity.ok(createEventsList()));
    }

    // Should not return reactive errors back from controller. spring will capture the reactive error and converts to 403. Return instead responseentity (see next)
    @GetMapping(value = "/eventsReturnExceptionReturnTypeFlux", produces = MediaType.APPLICATION_JSON_VALUE)
    Flux<String> eventsReturnExceptionReturnTypeFlux() throws Exception {
        System.out.println("==> events as json: return type is Mono<String> but returning mono.error...");
        return Flux.error(new CustomRuntimeException("getMonoError: CustomeRunTimeEception"));
    }

    // exact the same thing as above just return tupe is changed to responseentity to control the error being sent back.
    @GetMapping(value = "/eventsReturnExceptionReturnTypeResponseEntityWithType", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<Event>> eventsReturnExceptionReturnTypeResponseEntityWithType() {
        System.out.println("==> eventsReturnExceptionReturnTypeResponseEntityWithType: returning ResponseEntity with badRequest status and a body (which does not make sense)...");
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Event()));
    }

    // no type specified for ResponseEntity means everything can be passed (no compile time type-checking is enforced)
    @GetMapping(value = "/eventsReturnExceptionReturnTypeResponseEntityWOType", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity> eventsReturnExceptionReturnTypeResponseEntityWOType() {
        System.out.println("==> eventsReturnExceptionReturnTypeResponseEntity-W-OType: returning ResponseEntity (no type) with created status and a body...");
        return Mono.just(ResponseEntity.status(HttpStatus.CREATED).body(new Event()));
    }

    // no body returned. Status is converted automaticlly to 200
    @GetMapping(value = "/eventsReturnMonoVoid", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<Void> eventsReturnMonoVoid(){
        System.out.println("==> eventsReturnExceptionReturnTypeResponseEntity-W-OType: returning ResponseEntity (no type) with created status and a body...");
        return Mono.empty();
    }

    // Mono<Void> means no data will be sent back, onlyu status code, which can only be controlled using @ResponseStatus annotation.
    @GetMapping(value = "/eventsReturnMonoVoid2", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CONFLICT)
    Mono<Void> eventsReturnMonoVoid2(){
        System.out.println("==> eventsReturnExceptionReturnTypeResponseEntity-W-OType: returning ResponseEntity (no type) with created status and a body...");
        return Mono.empty();
    }


//    @GetMapping(value = "/eventsAsJsonServerResponse", produces = MediaType.APPLICATION_JSON_VALUE)
//    Mono<ServerResponse> eventsAsJsonServerResponse() {
//        System.out.println("==> events as json: returning flux<Event>...");
////        Flux<Long> durationFlux = Flux.interval(Duration.ofSeconds(2));
//        return ServerResponse.ok().contentType(APPLICATION_JSON).bodyValue(createOneEvent());
//    }

    // returning Mono<List<Event>> is the same as returning Flux<Event>. Both result in a json response body with array of event objects
    @GetMapping(value = "/eventsAsJson2", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<List<Event>> eventsAsJson2() {
        System.out.println("==> events as json: returning Mono<List<Event>>...");
//        Flux<Long> durationFlux = Flux.interval(Duration.ofSeconds(2));
        return fluxDataEvents.collectList();
    }

    // If Media-type is json, then, even if flux elements come with delay, api will wait at the border for all elements to arrive before returning them.
    @GetMapping(value = "/eventsAsJsonWithDelay", produces = MediaType.APPLICATION_JSON_VALUE)
    Flux<Event> eventsAsJsonWithDelay() {
        System.out.println("==> events as json: returning flux<Event>...");
        Flux<Long> durationFlux = Flux.interval(Duration.ofSeconds(2));
        return Flux.zip(fluxDataEvents, durationFlux).map(Tuple2::getT1);
    }

    @GetMapping(value = "/textsAsStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<String> textsAsStream() {
        System.out.println("==> Texts: return flux as stream...");
        Flux<Long> durationFlux = Flux.interval(Duration.ofSeconds(2));
        return Flux.zip(fluxFromList, durationFlux).map(Tuple2::getT1);
    }

    @GetMapping(value = "/textsAsJson", produces = MediaType.APPLICATION_JSON_VALUE)
    Flux<String> textsAsJson() {
        System.out.println("==> Texts: return flux as json...");
//        Flux<Long> durationFlux = Flux.interval(Duration.ofSeconds(2));
//        return Flux.zip(fluxFromList, durationFlux).map(Tuple2::getT1);
        return fluxFromList;
    }

    @GetMapping(value = "/textsAsJson2", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<List<String>> textsAsJson2() {
        System.out.println("==> Texts: return flux as json...");
//        Flux<Long> durationFlux = Flux.interval(Duration.ofSeconds(2));
//        return Flux.zip(fluxFromList, durationFlux).map(Tuple2::getT1);
        return fluxFromList.collectList();
    }

    @GetMapping(value = "/monoCustomRuntimeException", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<Object> getMonoCustomRuntimeException() { // not working: returning 403
        System.out.println("==> Returning CustomRuntimeException from annotation based mvc service");
        return monoError; // returns a customruntimeexception
    }

    @GetMapping(value = "/getFluxCustomRuntimeException", produces = MediaType.APPLICATION_JSON_VALUE)
    Flux<Object> getFluxCustomRuntimeException() {
        System.out.println("==> Returning CustomRuntimeException from annotation based mvc service");
        return fluxError; // returns a CustomRuntimeException
    }

    // test merge: the result is a combines (interleaved) stream. There is no guarantee in which order elements will arrive.
    private static void testFluxMerge() {
        fluxDataStr2.mergeWith(fluxDataStr.delayElements(Duration.ofSeconds(2))).subscribe(System.out::println); // since the second stream has delay, the first one will finish first before the second starts.
        System.out.println("\n\n");
        // the following data will come before the above stream because above stream has delays
        fluxDataInt.mergeWith(fluxDataInt2).subscribe(System.out::println); // data from the 2 streams will come randomly
    }

    /////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////

    private static void testFlux() {
        System.out.println("==> testFlux:");
        // from one flux type to another
        Flux<Integer> integerFlux = fluxDataIntAsStr
//                .log()
                .map(Integer::parseInt);

        fluxDataIntAsStr
                .log()
                .map(Integer::parseInt)
                .subscribe(System.out::println);

        System.out.println("==> testFlux: 2");

        integerFlux
                .subscribe(System.out::println);

        System.out.println("==> fluxDataStr operations test: ");
        fluxDataStr
                .take(1)
                .single()
                .subscribe(System.out::println);

        System.out.println("==> fluxDataStr operations test: 2");
        fluxDataStr
                .map(res -> new Event())
                .subscribe(System.out::println);

        System.out.println("==> fluxDataStr operations test: 3");
        fluxDataStr
                .map(res -> createEventsList())
                .take(1)
                .subscribe(System.out::println);

        System.out.println("==> Flux concatWith: concat 2 fluxes");
        Flux<String> fluxConcatonated = fluxDataStr.concatWith(Flux.fromIterable(getListStr()));// concat a flux with another flux
        System.out.println("==> flux size: " + fluxConcatonated.count().block());

        System.out.println("==> Flux string element changes");
        fluxDataStr2
                .map(res -> {
                    res += "can it";
                    return getString(res);
                })
                .subscribe(System.out::println);

        System.out.println("==> Flux event element changes");
        fluxDataEvents
                .map(res -> {
                    res.setId(5);
                    return res;
                })
                .subscribe(System.out::println);

        System.out.println("==> Can an external list be modified (side-effect=?");
        fluxDataEvents
                .map(event -> modifyExternalList())
//                .map(event -> addToList(String.valueOf(event.getId())))
                .subscribe(System.out::println);

    }

    /////////////////////////////////////////////////////////////////////////////////////

    // a quick example of how mono/flux works
    private static void testMonosAndFluxes() {
        System.out.println("\n\n==> Empty Mono: ");
        Mono.empty().subscribe(System.out::println);

        System.out.println("==> Mono.just:");
        Mono.just("JSA")
                .map(item -> "Mono item: " + item);

        System.out.println("==> Empty Flux:");
        Flux.empty()
                .subscribe(System.out::println);

        System.out.println("\n==> Flux from List:");
        List<String> list = Arrays.asList("This ", "is ", "another ", "example");
        Flux.fromIterable(list)
                .map(item -> item.toLowerCase())
                .subscribe(System.out::print);

        System.out.println("\n\n==> Flux emits increasing values each 100ms with delay");
        Flux.interval(Duration.ofMillis(100))
                .map(item -> "tick: " + item)
                .take(3)
                .subscribe(System.out::println);

        try {
            System.out.println("==> waiting 2 secs... (for duration async flux to complete)");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("==> Mono emits an Exception:");
        Mono.error(new CustomRuntimeException("==> Mono"))
                .doOnError(e -> {System.out.println("inside Mono doOnError()");})
                .subscribe(System.out::println);

        System.out.println("==> Flux emits an Exception:");
        Flux.error(new CustomRuntimeException("==> Flux"))
                .doOnError(e -> {System.out.println("inside Flux doOnError()");})
                .subscribe(System.out::println);

        System.out.println("==> The End.");
    }

    /////////////////////////////////////////////////////////////////////////////////////

    private static void testBoolean() {
        boolean bol = true; // defined as boolean, which means no values other than true or false can compile, unless value is being converted from e.g. string
        System.out.println("bol: " + bol);

        bol = Boolean.valueOf(null);
        System.out.println("bol: " + bol);

    }
    private static void testReduce() {
        // reduce only take BiFunction for all its methods: end result is a single object that is changed based on the incoming elements comparison.
        Supplier<Integer> supplier = () -> 9; // no input. Only output.
        Mono<Integer> integerMono;
        integerMono = fluxDataIntAsStr // 1, 10, 100, 1000
//                .log()
                .map(Integer::parseInt)
                .log()
                .reduce((x1, x2) -> {
                    System.out.println("==> x1: " + x1 + "      x2: " + x2);
                    System.out.println("==> target: " + (x1+x2));
                    return x1+x2;
                }); // 1 + 10 = 11, then 11 + 100 = 111, and finally 111 + 1000 = 1111 as final
                                            // result which is of same type as emitted items
        System.out.println("==> reduce flux into a sum using reduce(BiFunction)");
        integerMono.subscribe(System.out::println);

        integerMono = fluxDataIntAsStr // 1, 10, 100, 1000
                .map(Integer::parseInt)
                .reduce(5, (x1, x2) -> x1+x2); // Initial is added to the first value
        System.out.println("==> reduce flux into a sum using reduce(initialValue, BiFunction)");
        integerMono.subscribe(System.out::println);

        integerMono = fluxDataIntAsStr // 1, 10, 100, 1000
                .map(Integer::parseInt)
                .reduceWith(supplier, (x1, x2) -> {
                    System.out.println("==> Supplier: " + supplier.get() + "    x1: " + x1 + "      x2: " + x2);
                    System.out.println("==> target: " + (x1+x2));
                    return x1+x2;
                }); // Initial is added to the first value
        System.out.println("==> reduce flux into a sum using reduce(supplier, BiFunction)");
        integerMono.subscribe(System.out::println);

    }

    /////////////////////////////////////////////////////////////////////////////////////

    // test exceptions and errors
    private static void testExceptionsAndErrors() {
        System.out.println("\n==> testExceptionsAndErrors: enter");

//        System.out.println("==> onRrrorResume(another publisher)");
//        fluxWithErrorAtTheEnd
//                .map(o -> o) // will call this as long as there is data (4 times)
//                .doOnError(throwable -> System.out.println("doOnError: 5. element was flux.error which got caught here. onErrorResume after this..."))
//                .onErrorResume(throwable -> {
//                    return fluxDataStr2;
//                })
//                .subscribe(System.out::println);

        Flux<String> fluxDataStrPlusError = fluxDataStr.concatWith(getMonoErrorRuntimeException())
                .doOnError(throwable -> System.out.println("Error encountered...")); // just a consumer. Does not retrun a value. Used for logging usuallyu.

//        // onErrorReturn
//        System.out.println("==> onRrrorReturn(string): capture error and returns data");
//        fluxDataStrPlusError
//                .onErrorReturn("Error is captured and a string is returned in stead downstream")
//                .log()
//                .subscribe();

//        // if error is of type runtimeexception
//        System.out.println("==> onRrrorReturn(type, string)");
//        fluxDataStrPlusError
//                .onErrorReturn(RuntimeException.class,
//                "Runtimeexcpetion error detected: Only return this String if the error type is runtimeexception")
//                .log()
//                .subscribe();

//        // if error is of type customRuntimeexception
//        System.out.println("==> onRrrorReturn(customruntimeexception, string): because runtimeexception is not customexception, pipeline is terminated");
//        fluxDataStrPlusError
//                .onErrorReturn(CustomRuntimeException.class, // this will not run because the error is runtime and not customruntime
//                "CustomRuntimeexcpetion error detected: Only return this is the type is customruntimeexception")
//                .log() // will not run
//                .subscribe();

        //-------------------------------------
        // onErrorResume
        // if an error is encountered and the error is of type RTE, then return fluxDataStr2 publisher
        System.out.println("==> onRrrorResume(predicate, function)");
        fluxDataStrPlusError
                .onErrorResume(throwable -> throwable instanceof RuntimeException, throwable -> fluxDataStr2)
                .log()
                .subscribe();
    }

    /////////////////////////////////////////////////////////////////////////////////////

    public static void thenTests() {

        System.out.println("\n==> then: after flux that emits data");
        fluxDataStr
                .map(s -> s)
                .log()
                .then() // then waits for flux oncomplete to arrive to then and then emits oncomplete itself downstream (which only sees one oncomplete).
                        // Basically discards all upstream onNext signals: only error is let downstream untouched.
                .log()
                .map(aVoid -> aVoid)
                .subscribe();

        System.out.println("\n==> then: Flux.just (4 elements): ");
        Flux.just("This ", "is ", "an ", "example")
                .log()
                .map(item -> item.toUpperCase())
                .log()
                .then() // will block all onNext signals. Will only allow onCOmplete and onError.
                .log()
                .subscribe(System.out::print);

        System.out.println("\n==> thenEmpty: flux of 4 events as source, then, thenEmpty");
        fluxDataEvents  // 4 events
                .log()
                .map(event -> "mapped to an string")
                .log()
                .thenEmpty(Mono.empty())// why is this needed?
                .log()
                .map(aVoid -> {
                    System.out.println("==> Will not be invoked");
                    return aVoid;
                })
                .log()
                .subscribe();

        System.out.println("\n==> thenMany(): flux of 4 events as source, then, thenMany");
        fluxDataEvents  // 4 events
                .log()
                .map(event -> "mapped to an string")
                .log()
                .thenMany(fluxDataStr)// flux (abandon all hope who...): ignores all onNexts from previous streams. When onComplete is detected, thenMany emits elements
                .log()      // from fluxdataStr adn then plays the last onComplete.
                .subscribe(System.out::println);

        System.out.println("\n==> thenEmpty(Mono.never): flux followed by thenEmpty(mono.never)");
        fluxDataStr
                .map(s -> s)
                .log()
                .thenEmpty(Mono.never())// one onComplete is emitted. Mono.never does not emit anything.
                                        // Only thenEmpty will emit a single onComplete after upstream source finishes with onComplete.
                .log()
                .map(aVoid -> aVoid)
                .subscribe();

        System.out.println("\n==> thenEmpty: flux followed by thenEmpty(Mono.empty)");
        fluxDataStr
                .map(s -> s)
                .log()
                .thenEmpty(Mono.empty()) // 2 onCOmplete is emitted, one by Mono.empty and one by thenEmpty
                .log()
                .map(aVoid -> aVoid)
                .subscribe();

        System.out.println("\n==> then: Flux that emits only one error:");
        fluxError
                .then()  // will only allow onComplete and onError signals. WIll block and ignore all onNext signals.
                .log()
                .onErrorResume(err -> {
                    System.out.println("==> onErrorResume: " + err);
                    return Mono.empty();
                })
                .subscribe();

    }
    // creating stuff section:

    /////////////////////////////////////////////////////////////////////////////////////

    public static Flux<Event> createEventsFlux() {
        return Flux.just(
                new Event(System.currentTimeMillis(), new Date()),
                new Event(System.currentTimeMillis(), new Date()),
                new Event(System.currentTimeMillis(), new Date()),
                new Event(System.currentTimeMillis(), new Date())
        );
    }

    /////////////////////////////////////////////////////////////////////////////////////

    public static Event createOneEvent() {
        return new Event(1, new Date());
    }

    public static List<Event> createEventsList() {
        List<Event> events = new ArrayList<>();
        events.addAll(Arrays.asList(
            new Event(System.currentTimeMillis(), new Date()),
                    new Event(System.currentTimeMillis(), new Date()),
                    new Event(System.currentTimeMillis(), new Date()),
                    new Event(System.currentTimeMillis(), new Date())
        ));
        return events;
    }

    /////////////////////////////////////////////////////////////////////////////////////

    private static String getString(String str) {
        str += "can it";
        Event event = new Event(10, new Date());
//        return str + " changed";
//        return String.valueOf(event.getId());
        String strtmp = listStr.get(0);
        strtmp += "hello";

        System.out.println("Here -> " + listStr.get(0));
        return strtmp;
    }

    /////////////////////////////////////////////////////////////////////////////////////

    private static String addToList(String str) {
        String str1 = str;
//        listStr2.add("Hello"); // will not work because aslist returns a fixed sized list that cannot be modified in any way
        System.out.println("==> list size: " + listStr.size());
        return "anything";
    }

    private static Event modifyExternalList() {
        listDataEvents.get(0).setId(122);
        listDataEvents.add(new Event());
        System.out.println("==> post modification size: " + listDataEvents.size());
//        listDataEventsCreatedFromAsList.add(new Event(200, new Date())); // will not work because asList return fized sized list
        System.out.println("==> post modification asList size: " + listDataEventsCreatedFromAsList.size());

        return listDataEvents.get(0);
    }

    private static List<String> getListStr() {
        return listStr;
    }

    /////////////////////////////////////////////////////////////////////////////////////

    private static void switchIfEmptyTests() {
        System.out.println("==> switchIfEmptyTests: enter");

        System.out.println("\n==> test 1: return a new publisher from inside sie (Mono.just(blabla)))...");
        Mono<String> monoStr1 = monoEmpty;
        monoStr1.log()
                .doOnNext(s -> System.out.println("==> doOnNext should not be called"))
                .doOnTerminate(() -> {
                    System.out.println("==> doOnTeminate is called 1");
                })
                .map(s -> {
            System.out.println("==> map should not be called: there is no data present");
            return s;
            })
                .doOnTerminate(() -> {
                    System.out.println("==> doOnTeminate is called 2");
                })
            .switchIfEmpty(Mono.just("Publisher: Mono.just(string) from side sie")).log().subscribe();  // look at the signals and see an onNext("data is not present")

        System.out.println("\n==> test 2: sie return a new string publisher...");
        Mono<String> stringMono = fluxEmpty  // expects mono as return type
                .log()
                .filter(res -> true) // no data, so filter is not run. Filter is like map.
                .map(w -> "Should not be invoked")
                .next()
//                .take(1)
//                .single() // single should nto be used where flux or mono can be empty because then single will throw exception. switch will be useless here.
                .switchIfEmpty(Mono.defer(() -> {
                    System.out.println("==> inside switchifempty");
                    return Mono.just(getSomeString());
                }));// defer means: dont run the inside of switchifempty unless subscribe is called first. Otherwise switchifempty take mono as value and triggers it anyway
        stringMono.subscribe();

        System.out.println("\n==> test 3: switchIfEmpty returning string flux publisher which is mapped into integer 1 in the next step");
        fluxEmpty
                .doOnNext(s -> System.out.println("===>>> doOnOnext is called"))
                .doOnComplete(() -> {
                    System.out.println("======>>>>> complete is called");
                })
                .map(res -> createOneEvent()) // if you do: .map(() -> 1) the compiler will say: cannot infer functional interface type
                .log()
                .map(res -> "")
//                .take(1)
//                .single()
                .switchIfEmpty(monoDataStr2)
                .map(s -> 1)
                .log()
                .subscribe();

        System.out.println("\n==> test 4: switchIfEmpty returning error. The subsequent map should not run. onErrorReturn(5) will run");
        fluxEmpty
                .map(res -> createOneEvent()) // if you do: .map(() -> 1) the compiler will say: cannot infer functional interface type
                .log()
                .map(res -> "anything")
//                .take(1)
//                .single()
                .switchIfEmpty(Mono.error(new CustomRuntimeException("")))
                .map(s -> 1)
                .onErrorReturn(5)
                .onErrorReturn(6) // this should not run because the step before returns publisher and not error
                .log()
                .subscribe();

        System.out.println("\n==> test 5: switchIfEmpty returning new object type publisher");
        fluxEmpty
                .map(res -> createOneEvent()) // if you do: .map(() -> 1) the compiler will say: cannot infer functional interface type
                .log()
                .map(res -> "anything")
                .log()
//                .take(1)
//                .single()
                .map(res -> new ArrayList())
                .log()
                .switchIfEmpty( Flux.just(new ArrayList()))
                .log()
                .subscribe();

        System.out.println("\n==> test 6: switchIfEmpty returning string flux publisher");
        fluxEmpty
                .log()
                .filter(res -> true)
                .map(w -> "this")
                .switchIfEmpty(Flux.just(getSomeString()))
                .log()
                .subscribe();

        System.out.println("\n==> test 7: switchIfEmpty assigned to a Mono-variable");
        Mono<String> monoSwitchIfEmpty = fluxEmpty
                .log()
                .filter(res -> true)
                .map(w -> "this")
                .next()
                .switchIfEmpty(Mono.just(getSomeString()));
        monoSwitchIfEmpty.log().subscribe();

        System.out.println("\n==> test 7: switchIfEmpty assigned to a Mono-variable");
        fluxDataStr2
                .map(s -> s)
                .switchIfEmpty(Mono.defer(() -> {
                    System.out.println("==> Will not be invoked");
                    return Mono.just("another publisher is started here");
                }))
                .filter(s -> false)
                .switchIfEmpty(Mono.defer(() -> {
                    System.out.println("==> Now it is invoked because filter is used in stead of map and filter return false for all items");
                    return Mono.just("another publisher is started here");
                }))
                .subscribe();
    } // switchifemptytest

    private static void getStringFromMonoSubscribe(String str) {
        System.out.println("==> str from mono.subscribe: " + str);
    }

    private static void testFilterStuff() {
        System.out.println("Filter stuff...");

        Predicate<Event> predicate1 = a -> true;
        Predicate<Event> predicate2 = a -> {
            System.out.println("==> Predicate 2: predefined");
            return true;
        };

        fluxDataEvents
                .filter(a -> true)
                .log()
//                .filter(a -> {  // or use this one
//                    return true;
//                })
//                .filter(predicate1)  // or use this one instead
                .filter(predicate2)
                .map(m -> new Event())
                .subscribe();
        fluxDataEvents.subscribe();

    }


    /////////////////////////////////////////////////////////////////////////////////////

    public static void testEnrichImperativeWithDataFromReactivePipeline() {
        List<Integer> integers = new ArrayList<>();
        List<Integer> integersDataComingOverTime = new ArrayList<>();

//        fluxDataInt.map(integer -> {
//            integers.add(integer);
//            return integer;
//        }).subscribe();
//
//        for (int i = 0; i< integers.size(); i++) {
//            System.out.println("==> enriched: " + integers.get(i));
//        }

        // now with flux coming over time
        fluxIntegerWithInterval.map(integer -> {
            integersDataComingOverTime.add(integer);
            return integer;
        }).subscribe();

        System.out.println("==> enriched (data over time): size: " + integersDataComingOverTime.size());

        for (int i = 0; i< integersDataComingOverTime.size(); i++) {
            System.out.println("==> enriched (data over time): " + integersDataComingOverTime.get(i));
        }

    }


    public static void testImperativeReactiveCombination() {
        Flux.just(1,2,3)
                .flatMap(integer -> Flux.just(4,5,6)
                        .map(integer1 -> integer + integer1)
                ).subscribe(System.out::println);

        int i = -1;

//        fluxIntegerWithInterval
//                .map(integer -> {
//                    // everything in this block is sequential. Once data becomes available, it will run sequentially.
//                    System.out.println("==> before for-loop: " + integer);
//                    int i1 = 0;
//                    for (int i2 = 0; i2 < 10; i2++)
//                        System.out.print(".");
//
//                    // subscribe (= get) another flux
//                    System.out.println("\n==> after for-loop: " + integer);
//                    // following flux will arrive independently from the parent flux. Nothing to do with each other, even if one is nested inside the other.
//                    // if you want to coordiante their data, then the 2 sources must be merged/concat/zipped.
//                    fluxStringWithInterval
//                            .map(s -> {
//                                System.out.println("        ==> inner flux: " + s);
//                                return s;
//                            }).subscribe();
//
//                    return 100;
//                }).subscribe(System.out::println);

//        fluxDataInt
//                .flatMap(integer -> {
//                    return fluxStringWithInterval;
//                }).subscribe(System.out::println);

//        Flux.interval(Duration.ofMillis(3000))
//                .flatMap(integer -> {
////                    System.out.println("==> blabla");
//                    return fluxStringWithInterval;
//                }).subscribe(System.out::println);

        // only zip can sequentially pair elements todather
//        fluxDataIntAsStr.zipWith(fluxStringWithInterval)
//                .subscribe(System.out::println);

        List<Integer> integers = List.of(1,2,3);
        Flux.fromIterable(integers).zipWith(fluxIntegerWithInterval).subscribe(System.out::println);

        System.out.println("i = " + i);
    }
    // main thread goes thru enter and exit in no time. then another thread executes the pipeline as data becomes available.
    public static void oneLongPipelineSeparatedByImperativeCode() {
        System.out.println("==> oneLongPipelineSeapratedByImperativeCode: Enter...");

        Flux<String> flux1 = fluxStringWithInterval.map(str -> "hello1 " + str); // assigned to a var and stored.
        for (int i=0;i<10;i++) {
            // do something imperative
            System.out.print(i + " ");
            System.out.println();
        }

        Flux<String> flux2 = flux1.map(str -> "hello2 " + str);
        for (int i=0;i<10;i++) {
            // do something  more imperative: call a method
            getSomeString();
        }
        flux2.subscribe(System.out::println);
        System.out.println("==> oneLongPipelineSeapratedByImperativeCode: Exit...");
    }

    public static void useMultipleSubscribesToTheSameFlux() {
        Flux<String> fluxCopy = fluxDataStr2;
        fluxCopy.subscribe(System.out::println);
        Flux<String> fluxCopy1 = fluxCopy;
        fluxCopy1.subscribe(System.out::println);
        fluxDataStr2.subscribe(System.out::println);
        fluxDataStr2.map(str -> "-- ").subscribe(System.out::println);

    }

    /////////////////////////////////////////////////////////////////////////////////////

    // combination reactive and non-reactive code: non-reactive code is using reactive data: it has to wait
    public static void imperativeCodeUsingDataFromPipeline() {
        ArrayList<String> arr = new ArrayList<>();
        final String findHope = null;
        System.out.println("==> imperativeCodeUsingDataFromPipeline: Enter...");
        fluxStringWithInterval.map(str -> "hello1 " + str); // this pipeline is lost because it is not returned (subscribed to).
        Flux<String> flux1 = fluxStringWithInterval.map(str -> "hello1 " + str); // assigned to a var and stored.

        // side effect: extract some data from the pipeline: only arraylist can be used.
        Flux<String> flux2 = flux1
                .map(str -> {
                    if (str.contains("hope")) {
                        arr.add(str);
                }
            return str; // returning the same input as output, so flux2 is the same as flux1: this goes into a new flux ofcourse.
        }
        );
        for (int i=0;i<10;i++) {
            // do something  more imperative: call a method
            getSomeString();
        }
        flux2.doOnComplete(() -> {
            System.out.println("Now findHope is here: " + arr.size());
        }).subscribe(System.out::println);
        System.out.println("==> findHope: " + (arr.size() > 0 ? arr.get(0) : "nothing"));
        System.out.println("==> imperativeCodeUsingDataFromPipeline: Exit...");
    }

    /////////////////////////////////////////////////////////////////////////////////////

    // Mono<wrapperObjectA<Flux<Event>>>  -> Mono<wrapperObjectA<Event>>
    private static void xx() {
        WrapperA wrapperA = new WrapperA();
        Flux<Event> fluxEvent = wrapperA.getFluxEvent();
        Mono<WrapperA> monoWrapperA = Mono.just(wrapperA);
        Flux<Event> eventsFlux = createEventsFlux();

        WrapperB<Flux<Event>> fluxWrapperB = new WrapperB<>();
        fluxWrapperB.setT(createEventsFlux());
        Mono<WrapperB<Flux<Event>>> wrapperBMono = Mono.just(fluxWrapperB);
//        wrapperBMono.map(a -> {
//            WrapperB<List<Event>> eventWrapperB = new WrapperB<>();
//            eventWrapperB.setT(a.getT().collectList());
//            return ev
//        });

    }

    /////////////////////////////////////////////////////////////////////////////////////

    private static void testCallableAndDefer() {
        // defer and callable instruct the cpu not to run the code they wrap until subscribe is called on the pipeline.
        // useful when eager code is mixed with lazy code.
        Mono.defer(() -> getMonoFromEagerLazyCodeMix()) // runs the method for each subscription, including the eager code.
//        getMonoFromEagerLazyCodeMix()
                .repeat(3)
                .map(o -> {
                    return "1 " + o;
                })
                .subscribe(System.out::println);
    }

    /////////////////////////////////////////////////////////////////////////////////////

    private static void repeatWhenEmptyTest() {
        int count = 0;
//        Mono.defer(() -> getDelayedMonoInteger(0))
        getDelayedMonoInteger(0)
//        Mono.just(1)
//                .repeat(3)
//                .repeatWhen(v -> Flux.range(1, 5))
//                .repeatWhen(v -> Flux.just(1, 2))
//                .repeatWhen(v -> {
//                            System.out.println("-> A new subscription initiated...");
//                            return Flux.just(1, 2);
//                        }
//                )

//                .repeatWhen(v -> Flux.empty())
//                .repeatWhen(v -> Mono.empty())
                .repeatWhenEmpty(longFlux -> {
                    System.out.println("==> repeatWhenEmpty...");
                        return Flux.range(1, 4);
                        }
                )
//                .repeatWhenEmpty(longFlux -> {
//                    System.out.println("==> inside repeatwhenempty...: ");
//                    return Flux.just(1,2,3);
//                }
//                )
                .log()
                .switchIfEmpty(Mono.just(-5))
                .subscribe(o -> {
                    System.out.println("==> hello: " + o);
                });


        AtomicInteger counter = new AtomicInteger();
        Mono<String> source = Mono.defer(() -> {
                    System.out.println("==> source subscription");
                    return (counter.getAndIncrement()) < 3 ? Mono.empty() : Mono.just("test-data");
                }
        );
        List<Long> iterations = new ArrayList<>();
//        AssertSubscriber<String> ts = AssertSubscriber.create();
//        source.log().repeatWhenEmpty(1000, o -> o.doOnNext(iterations::add)).subscribe(System.out::println); // also valid: doOnNext returns a flux based on input params.
        source.log().repeatWhenEmpty(1000, o -> Flux.range(1, 10)).subscribe(System.out::println); // as long as source emits empty and flux parameter emits data, subscription is repeated.
//        source.log().repeatWhenEmpty(1000, o -> Mono.just(45)).subscribe(System.out::println);

//        monoEmpty
//                .repeatWhenEmpty(longFlux -> {
//                    System.out.println("==> here");
////                    return Mono.just("==> repeatWhenEmpty");
//                    return Mono.empty();
//                }
//                )
//                .log()
//                .subscribe();
    }

    /////////////////////////////////////////////////////////////////////////////////////

    // groupby test
    private static void testFluxGroupBy() {
        System.out.println("==> testFluxGroupBy: using fibonacci numbers as flux values");

        // take the first 20 fb numbers, then group them according divisibility by 2, 3, 5, 7 and others.
        getFluxFibonacciSequence().take(20)
                .groupBy(i -> {
                    List<Integer> divisors= Arrays.asList(2,3,5,7); // these are the keys to groupby
                    Optional<Integer> divisor = divisors.stream().filter(d -> i % d == 0).findFirst();
                    return divisor.map(x -> "Divisible by "+x).orElse("Others");

                })
                .concatMap(x -> { // a simple map that keeps order and just unwraps the inner (grouped)flux
                    System.out.println("\n"+x.key());
                    return x;
                })
                .subscribe(x -> System.out.print(" "+x));

        System.out.println("\n===============================");
        // % is modulu operation, which returns the remainder of the division being performed.
        Flux.range(1, 5)
                .groupBy(k -> {
                    System.out.println("==> element: " + k + "   modulus 3: " + (k%3));
                    return k % 3;
                })
                .log()
                .delayElements(Duration.ofMillis(2000))
                .flatMap(g -> {
                    System.out.println("\nKey: "+g.key());
                    return g;
                })
                .subscribe(integer -> System.out.println("==> subscribe: " + integer));

        System.out.println("\n===============================");
        // take only the first group and flatten it and print it. Keys: 1,2,0, in this order
        Flux.range(1, 10)
                .groupBy(k -> k % 3)
                .take(1)
                .flatMap(g -> g)
                .subscribe(System.out::println);

        System.out.println("\n===============================");
        // negate the values before inserting into the group
        Flux.range(1, 10)
                .groupBy(k -> k % 2, v -> -v)
                .flatMap(g -> g)
                .subscribe(System.out::println);

        System.out.println("\n===============================");
        // prefetch is like: have at least 2 items to work with from the inner source.
        Flux.range(0, 20)
                .groupBy(i -> i % 5)
                .concatMap(v -> v, 2)
                .subscribe(System.out::println);

        System.out.println("\n===============================");
        Flux.range(1, 10)
                .groupBy(k -> k % 2)
                .concatMap(g -> g)
                .subscribe(System.out::println);

        System.out.println("\n===============================");
        Flux.<Integer>empty()
                .groupBy(v -> v)
                .switchIfEmpty(
                    Flux.just(1, 2)
                            .groupBy(k -> 1))
                .flatMap(k -> k)
                .subscribe(System.out::println);

        System.out.println("\n===============================");
        Flux.range(1, 1_000)
                .groupBy(v -> 1)
                .flatMap(g -> g)
                .subscribe(System.out::println);

    }

    /////////////////////////////////////////////////////////////////////////////////////

    public static void testFlatMapWithWindow() {
        Flux<String> fluxStr = Flux.just("a", "b", "c", "d", "e", "f", "g", "h", "i");
        Flux<String> fluxStrList = Flux.fromIterable(List.of("1", "2", "3", "4", "5"));

        System.out.println("\n==> testFlapMap: flatmap(): sequentially processes input flux on main-thread (does not keep order)");
        fluxStr
                .flatMap(s -> Flux.just(s))
                .subscribe(System.out::println);

        System.out.println("\n==> testFlapMap: concatMap(): sequentially processes input flux on main-thread (keeps order)");
        fluxStr
                .concatMap(s -> Flux.just(s))
                .subscribe(System.out::println);

        System.out.println("\n==> testFlapMap: flatmap() + window: sequentially processes input flux on main-thread");
        // batch the flux into sub-fluxex before processing them. Window cuts the flux into sub-fluxes
        fluxStr
                .window(3) // window creates sub-fluxes to be processed one sub-flux at a time, but sequentially using the main-thread
                .flatMap(s -> {
                    return s.map(s1 -> {
                        return convertCharToUpperCase(s1);
                    });
                })
        .subscribe(System.out::println);

        System.out.println("\n==> testFlapMap: flatmap() + window: Same things as above");
        // batch the flux into sub-fluxex before processing them. Window cuts the flux into sub-fluxes
        fluxStr
                .window(3) // window's output is a flux and not string, that is why flatmap expects flux as input here
                .flatMap(s -> s.map(s1 -> convertCharToUpperCase(s1)))
                .subscribe(System.out::println);

        System.out.println("==> testFlapMap: flatmap() + window: processes input flux in parallel (subscribeOn(parallel())) on different threads, but out of order");
        fluxStr
                .window(3) // window creates sub-fluxes to be processed one sub-flux at a time
                .flatMap(s -> {
                    return s.map(s1 -> {
                        return convertCharToUpperCase(s1);
                    }).subscribeOn(parallel());
                })
                .subscribe(System.out::println);

        System.out.println("\n==> Waiting 3 secs...\n");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("==> testFlapMap: flatMapSequential(): processes input flux in parallel on different threads, and in order");
        fluxStr
                .window(3) // window creates sub-fluxes to be processed one sub-flux at a time
                .flatMapSequential(s -> {
                    return s.map(s1 -> {
                        return convertCharToUpperCase(s1);
                    }).subscribeOn(parallel());
                })
                .subscribe(System.out::println);

    }
    /////////////////////////////////////////////////////////////////////////////////////////////////
    public static Mono<Integer> getDelayedMonoInteger(int count) {
        System.out.println("\n==> getDelayedMonoInteger: enter: " + count);
//        return Mono.just(1);
        if (count <= 5) {
            try {
                System.out.println("==> waiting...");
                Thread.sleep(1000);
                System.out.println("==> wait ended...");
            } catch (InterruptedException e) {
                System.out.println("====> Error: interupted");
                return Mono.just(-1);
            }
            System.out.println("==> returning Mono...");
            return Mono.empty(); // this is the chosen source for subsequent subscriptions if any (e.g. in case of repeat)
//            return Mono.just(7); // this is the chosen source for subsequent subscriptions if any (e.g. in case of repeat)
        }
//        else {
//            System.out.println("==> returning Mono.just(1)");
//            return Mono.just(1);
//        }
        return Mono.just(5);
    }

    /////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////

    private static void printSomething() {
        System.out.print("==> Print something...");
    }

    private static Mono<?> getMonoFromEagerLazyCodeMix() {
        System.out.println("==> getMonoFromEagerLazyCodeMix: enter");
        return Mono.just("Eager/lazy code");
    }
    private static String getSomeString() {
        System.out.println("==> getSomeString: returning: Hello from getSomeString");
        return "Hello from getSomeString";
    }

    private static Mono<String> getMonoErrorRuntimeException() {
        // customruntimeexception extends runtimeexception
        return Mono.error(new RuntimeException("getMonoErrorRuntimeException: runtimeexception"));
    }

    private static Mono<String> getMonoErrorCustomRuntimeException() {
        // customruntimeexception extends runtimeexception
        return Mono.error(new CustomRuntimeException("getMonoErrorCustomRuntimeException: CustomRunTimeEception"));
    }

    private static Flux<String> getFluxErrorRuntimeException() {
        // customruntimeexception extends runtimeexception
        return Flux.error(new RuntimeException("getFluxErrorRuntimeException: runtimeexception"));
    }

    private static Flux<String> getFluxErrorCustomRuntimeException() {
        // customruntimeexception extends runtimeexception
        return Flux.error(new CustomRuntimeException("getFluxErrorCustomRuntimeException: CustomRunTimeException"));
    }

    private static List<Integer> getFibonacciSequence() {
        List<Integer> fb = new ArrayList<>();
        for (int i=0;i<20;i++) { // generate the first 20 fb numbers
            if (i<2)
                fb.add (i);
            else {
                fb.add(fb.get(i-1) + fb.get(i-2));
            }
            System.out.println("==> " + fb.get(i));
        }
        return fb;
    }

    private static Flux<Long> getFluxFibonacciSequence() {
        return Flux.generate(() -> Tuples.<Long,
                Long>of(0L, 1L), (state, sink) -> {
            if (state.getT1() < 0)
                sink.complete();
            else
                sink.next(state.getT1());
            return Tuples.of(state.getT2(), state.getT1() + state.getT2());
        });
    }

    public static List<String> convertCharToUpperCase(String s) {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return List.of(s.toUpperCase(), Thread.currentThread().getName());
    }
}

