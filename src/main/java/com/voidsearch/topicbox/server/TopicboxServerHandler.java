package com.voidsearch.topicbox.server;

import com.voidsearch.topicbox.lda.TextCorpus;
import com.voidsearch.topicbox.lda.TopicModel;
import com.voidsearch.topicbox.lda.TopicModelGenerator;
import com.voidsearch.topicbox.lda.TopicModelTaskManager;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.handler.codec.http.websocketx.*;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

public class TopicboxServerHandler extends SimpleChannelUpstreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(TopicboxServer.class.getName());
    private static final String DOCUMENT_ROOT = "/webapp";
    private static final String WEBSOCKET_URI = "/ws";

    private static final MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();

    private WebSocketServerHandshaker handshaker;
    private ObjectMapper mapper = new ObjectMapper();

    private TopicModelTaskManager topicModelTaskManager = TopicModelTaskManager.getInstance();

    public enum WebsocketRequests {
        LOAD_TOPICS,                    // load available topics for given model
        LOAD_DATA,                      // load data samples for given model
        SUBMIT_LDA_TASK                 // submit data for model estimation
    }

    public enum WebsocketResponses {
        TASK_NAME,                      // name of active task
        MODEL_NOT_AVAILABLE             // no model available corresponding to given dataset
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

        Object msg = e.getMessage();

        if (msg instanceof HttpRequest) {
            handleHttpRequest(ctx, (HttpRequest)msg, e);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        } else {
            sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
        }

    }

    /**
     * handle HTTP request at service port
     *
     * @param ctx
     * @param req
     * @throws Exception
     */
    private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest req, MessageEvent e) throws Exception {
        if (req.getMethod() == HttpMethod.GET) {
            if (req.getUri().startsWith(DOCUMENT_ROOT)) {
                handleHttpStaticFileGET(ctx, req, e);
            } else if (req.getUri().startsWith(WEBSOCKET_URI)) {
                handleHttpWebSocketHandshake(ctx, req, e);
            } else {
                handleHttpGET(ctx, req, e);
            }
            return;
        } else if (req.getMethod() == HttpMethod.POST) {
            handleHttpPOST(ctx, req, e);
            return;
        } else {
            sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }
    }

    /**
     * handle static file http GET request
     *
     * @param ctx
     * @param req
     */
    private void handleHttpStaticFileGET(ChannelHandlerContext ctx, HttpRequest req, MessageEvent e) {

        if (logger.isInfoEnabled()) {
            logger.info("got query :  " + req.getUri());
        }

        File fileRequested = new File(req.getUri().substring(1));
        if (fileRequested.isDirectory()) {
            fileRequested = new File(fileRequested, "index.html");
        }

        if (!fileRequested.exists()) {
            sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }

        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

        try {

            RandomAccessFile raf = new RandomAccessFile(fileRequested, "r");
            response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, raf.length());

            response.setHeader(HttpHeaders.Names.CONTENT_TYPE, getContentType(fileRequested));

            Channel ch = e.getChannel();
            ch.write(response);

            final FileRegion region = new DefaultFileRegion(raf.getChannel(), 0, raf.length());
            ChannelFuture writeFuture = ch.write(region);

            writeFuture.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    region.releaseExternalResources();
                }
            });

            if (!HttpHeaders.isKeepAlive(req)) {
                writeFuture.addListener(ChannelFutureListener.CLOSE);
            }

        } catch (FileNotFoundException ex) {
            sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        } catch (IOException ex) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

    }

    /**
     * get valid content type corresponding to given file
     *
     * @param f - file descriptor
     * @return
     */
    public String getContentType(File f) {
        if (f.getName().endsWith(".css")) {
            return "text/css";
        } else {
            return mimeTypesMap.getContentType(f);
        }
    }

    /**
     * handle websocket http handshake
     *
     * @param ctx
     * @param req
     * @param e
     */
    private void handleHttpWebSocketHandshake(ChannelHandlerContext ctx, HttpRequest req, MessageEvent e) {

        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                "ws://" + req.getHeader(HttpHeaders.Names.HOST) + WEBSOCKET_URI, null, false);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            wsFactory.sendUnsupportedWebSocketVersionResponse(ctx.getChannel());
        } else {
            handshaker.handshake(ctx.getChannel(), req).addListener(WebSocketServerHandshaker.HANDSHAKE_LISTENER);
        }

    }

    /**
     * handle regular get query
     * 
     * @param ctx
     * @param req
     * @param e
     */
    private void handleHttpGET(ChannelHandlerContext ctx, HttpRequest req, MessageEvent e) {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
    } 
    
    private void handleHttpPOST(ChannelHandlerContext ctx, HttpRequest req, MessageEvent e) {
        // TBD
    }

    /**
     * handle websocket query at service port
     * (used by topicbox ui)
     *
     * @param ctx
     * @param frame
     * @throws Exception
     */
    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {

        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.getChannel(), (CloseWebSocketFrame)frame);
            return;
        } else if (frame instanceof PingWebSocketFrame) {
            ctx.getChannel().write(new PongWebSocketFrame(frame.getBinaryData()));
            return;
        } else if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        }

        String request = ((TextWebSocketFrame)frame).getText();

        if (logger.isInfoEnabled()) {
            logger.info(String.format("request: %s", request));
        }

        if (request.startsWith(WebsocketRequests.LOAD_TOPICS.toString())) {
            
            String datasetName = request.substring(WebsocketRequests.LOAD_TOPICS.toString().length() + 1);

            if (topicModelTaskManager.getModelCount() == 0 || !topicModelTaskManager.containsModel(datasetName)) {
                ctx.getChannel().write(new TextWebSocketFrame(WebsocketResponses.MODEL_NOT_AVAILABLE.toString()));
                return;
            }

            TopicModel model = topicModelTaskManager.getModel(datasetName);
            Object[][] topKeywords = model.getModelTopKeywords();

            String rsp = mapper.writeValueAsString(topKeywords);

            ctx.getChannel().write(new TextWebSocketFrame(rsp));
            ctx.getChannel().close();

            return;

        } else if (request.startsWith(WebsocketRequests.LOAD_DATA.toString())) {

            String datasetName = request.substring(WebsocketRequests.LOAD_DATA.toString().length() + 1);

            if (topicModelTaskManager.getModelCount() == 0 || !topicModelTaskManager.containsModel(datasetName)) {
                ctx.getChannel().write(new TextWebSocketFrame(WebsocketResponses.MODEL_NOT_AVAILABLE.toString()));
                return;
            }

            TopicModelGenerator modelGenerator = topicModelTaskManager.getGenerator(datasetName);
            TextCorpus corpus = modelGenerator.getCorpus();

            // draw docs from training sample - TODO : replace this
            Object[][] data = modelGenerator.getModel().inferTopics(corpus.getDocs(100));

            String rsp = mapper.writeValueAsString(data);

            ctx.getChannel().write(new TextWebSocketFrame(rsp));
            ctx.getChannel().close();

            return;

        } else if (request.startsWith(WebsocketRequests.SUBMIT_LDA_TASK.toString())) {

            String taskName = request.split(":")[1];

            topicModelTaskManager.submitTask(taskName);

            ctx.getChannel().write(new TextWebSocketFrame(WebsocketResponses.TASK_NAME.toString()+taskName));

            TopicModelGenerator modelGenerator = topicModelTaskManager.getGenerator(taskName);

            while (!modelGenerator.modelComplete()) {
                if (modelGenerator.running()) {
                    ctx.getChannel().write(new TextWebSocketFrame("model estimation in progress... " +
                            " expected time to complete : "
                            + modelGenerator.getModel().getExpectedCompletionTime()/1000 + " sec"));
                } else {
                    ctx.getChannel().write(new TextWebSocketFrame("initializing model generator ..."));
                }
                Thread.sleep(1000);
            }

            ctx.getChannel().write(new TextWebSocketFrame("model estimation complete ..."));
            ctx.getChannel().close();

            return;

        }

        ctx.getChannel().write(new TextWebSocketFrame(request.toUpperCase()));
    }

    /**
     * send http error response
     *
     * @param ctx
     * @param status
     */
    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
        response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.setContent(ChannelBuffers.copiedBuffer(status.toString() + "\r\n", CharsetUtil.UTF_8));
        ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
    }

}
