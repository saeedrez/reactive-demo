package reactive.proxy;

import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.ProxyProvider;
import reactor.netty.tcp.TcpClient;

public class ProxyConfigUtil {

    HttpClient httpClient = HttpClient.create()
            .tcpConfiguration(tcpClient -> tcpClient
                    .proxy(proxy -> proxy
                            .type(ProxyProvider.Proxy.HTTP)
                            .host("ourproxy.com")
                            .port(8080)));

    ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

    WebClient webClient = WebClient.builder()
            .clientConnector(connector)
            .baseUrl("https://targetsite.com")
            .build();

    // kotlin version
//    var httpClient: HttpClient = HttpClient.create()
//            .tcpConfiguration { tcpClient ->
//            tcpClient
//                    .proxy { proxy ->
//            proxy
//                    .type(ProxyProvider.Proxy.HTTP)
//                    .host("10.255.237.20")
//                    .port(8887)
//    }
//    }
//    var connector = ReactorClientHttpConnector(httpClient)
//    webClient = WebClient.builder().clientConnector(connector).filter(MyFilter()).build()...

}
