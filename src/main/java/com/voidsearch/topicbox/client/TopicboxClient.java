package com.voidsearch.topicbox.client;

import static org.jboss.netty.channel.Channels.*;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.*;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executors;

public class TopicboxClient {

    private static final int DEFAULT_PORT = 80;
    private final URI uri;

    protected ResponseCallback<String> responseCallback = new ResponseCallback<String>();

    public TopicboxClient(URI uri) {
        this.uri = uri;
    }

    public ResponseCallback<String> getCallback() {
        return responseCallback;
    }

    public void run() {

        ClientBootstrap bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()
                )
        );

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = pipeline();
                pipeline.addLast("codec", new HttpClientCodec());
                pipeline.addLast("inflater", new HttpContentDecompressor());
                pipeline.addLast("handler", new TopicboxClientHandler(responseCallback));
                return pipeline;
            }
        });

        ChannelFuture future = bootstrap.connect(new InetSocketAddress(uri.getHost(), (uri.getPort() == -1) ? DEFAULT_PORT : uri.getPort()));

        Channel channel = future.awaitUninterruptibly().getChannel();

        if (!future.isSuccess()) {
            future.getCause().printStackTrace();
            bootstrap.releaseExternalResources();
            return;
        }

        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath());

        request.setHeader(HttpHeaders.Names.HOST, uri.getHost());
        request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
        request.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);

        channel.write(request);
        channel.getCloseFuture().awaitUninterruptibly();
        bootstrap.releaseExternalResources();

    }

}
