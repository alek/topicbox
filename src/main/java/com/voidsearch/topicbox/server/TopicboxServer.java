package com.voidsearch.topicbox.server;

import static org.jboss.netty.channel.Channels.*;

import org.apache.commons.cli.*;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopicboxServer {

    private static final Logger logger = LoggerFactory.getLogger(TopicboxServer.class.getName());

    private final int port;

    /**
     * create topicbox server listening at given port
     * @param port
     */
    public TopicboxServer(int port) {
        this.port = port;
    }

    /**
     * start server thread
     */
    public void run() {

        ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool())
        );

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = pipeline();
                pipeline.addLast("decoder", new HttpRequestDecoder());
                pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
                pipeline.addLast("encoder", new HttpResponseEncoder());
                pipeline.addLast("deflater", new HttpContentCompressor());
                pipeline.addLast("handler", new TopicboxServerHandler());
                return pipeline;
            }
        });

        bootstrap.bind(new InetSocketAddress(port));

        logger.info("topicbox server listening at port : " + port);
        logger.info("webapp available at : http://localhost:" + port + "/webapp/");

    }

    /**
     * cmdline exec
     *
     * @param args
     */
    public static void main(String[] args)  {

        Options options = new Options();
        options.addOption("port", true, "topicbox service port");

        CommandLineParser parser = new PosixParser();

        try {
            CommandLine line = parser.parse(options, args);
            if (!line.hasOption("port")) {
                printHelpAndExit(options);
            } else {
                (new TopicboxServer(Integer.parseInt(line.getOptionValue("port")))).run();
            }
        } catch (Exception e) {
            e.printStackTrace();
            printHelpAndExit(options);
        }


    }
    
    public static void printHelpAndExit(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("topicbox", options);
        System.exit(1);
    }

}
