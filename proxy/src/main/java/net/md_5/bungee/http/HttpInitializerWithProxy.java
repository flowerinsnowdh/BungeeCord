package net.md_5.bungee.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.Callback;

import javax.net.ssl.SSLEngine;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class HttpInitializerWithProxy extends ChannelInitializer<Channel>
{

    private final Callback<String> callback;
    private final boolean ssl;
    private final String host;
    private final int port;

    @Override
    protected void initChannel(Channel ch) throws Exception
    {
        if (System.getProperty("socksProxyHost") != null) {
            String socksProxyHost = System.getProperty("socksProxyHost");
            String socksProxyPort = System.getProperty("socksProxyPort");
            String socksProxyUserName = System.getProperty("socksProxyUserName");
            String socksProxyPassword = System.getProperty("socksProxyPassword");
            try {
                if (socksProxyUserName != null && socksProxyPassword != null) {
                    ch.pipeline().addLast(new Socks5ProxyHandler(new InetSocketAddress(socksProxyHost, Integer.parseInt(socksProxyPort)),
                            socksProxyUserName, socksProxyPassword
                    ));
                } else {
                    ch.pipeline().addLast(new Socks5ProxyHandler(new InetSocketAddress(socksProxyHost, Integer.parseInt(socksProxyPort))));
                }
            } catch (NumberFormatException ignored) {
            }
        } else if (System.getProperty("http.proxyHost") != null) {
            String httpProxyHost = System.getProperty("http.proxyHost");
            String httpProxyPort = System.getProperty("http.proxyPort");
            String httpProxyUserName = System.getProperty("http.proxyUserName");
            String httpProxyPassword = System.getProperty("http.proxyPassword");
            try {
                if (httpProxyUserName != null && httpProxyPassword != null) {
                    ch.pipeline().addLast(new HttpProxyHandler(new InetSocketAddress(httpProxyHost, Integer.parseInt(httpProxyPort)),
                            httpProxyUserName, httpProxyPassword));
                } else {
                    ch.pipeline().addLast(new HttpProxyHandler(new InetSocketAddress(httpProxyHost, Integer.parseInt(httpProxyPort))));
                }
            } catch (NumberFormatException ignored) {
            }
        }
        ch.pipeline().addLast( "timeout", new ReadTimeoutHandler( HttpClient.TIMEOUT, TimeUnit.MILLISECONDS ) );
        if ( ssl )
        {
            SSLEngine engine = SslContextBuilder.forClient().build().newEngine( ch.alloc(), host, port );

            ch.pipeline().addLast( "ssl", new SslHandler( engine ) );
        }
        ch.pipeline().addLast( "http", new HttpClientCodec() );
        ch.pipeline().addLast( "handler", new HttpHandler( callback ) );
    }
}
