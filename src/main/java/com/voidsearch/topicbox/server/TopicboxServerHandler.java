package com.voidsearch.topicbox.server;

import com.voidsearch.topicbox.lda.TextCorpus;
import com.voidsearch.topicbox.lda.TopicModel;
import com.voidsearch.topicbox.lda.TopicModelManager;
import com.voidsearch.topicbox.util.TopicboxUtil;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.netty.buffer.ChannelBuffer;
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
import java.util.Map;

public class TopicboxServerHandler extends SimpleChannelUpstreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(TopicboxServer.class.getName());
    private static final String DOCUMENT_ROOT = "/webapp";
    private static final String WEBSOCKET_URI = "/ws";

    private static final MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();

    private WebSocketServerHandshaker handshaker;
    private ObjectMapper mapper = new ObjectMapper();

    private boolean readingChunks;

    private TopicModelManager topicModelManager = TopicModelManager.getInstance();

    public enum WebsocketRequests {
        LOAD_TOPICS,                    // load available topics for given model
        LOAD_DATA,                      // load data samples for given model
        NUM_ENTRIES,                    // number of data entries to be rendered
        SUBMIT_LDA_TASK,                // submit data for model estimation
        DESCRIBE_TOPIC,                 // return topic description
        DESCRIBE_KEYWORD,               // return keyword description
        GET_KEYWORD_TOPIC_MATRIX,       // get top keyword -> topic allocation matrix
        GET_KEYWORD_COOCCURRENCE       // get keyword/topic co-occurrence matrix
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

        if (logger.isDebugEnabled()) {
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
        } catch (IOException ex) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
        }

    }

    /**
     * get valid content type corresponding to given file
     *
     * @param f - file descriptor
     * @return
     */
    private String getContentType(File f) {
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
     * handle http GET request
     * 
     * @param ctx
     * @param req
     * @param e
     */
    private void handleHttpGET(ChannelHandlerContext ctx, HttpRequest req, MessageEvent e) {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * handle http POST request
     *
     * @param ctx
     * @param req
     * @param e
     */
    private void handleHttpPOST(ChannelHandlerContext ctx, HttpRequest req, MessageEvent e) {

        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        String uri = req.getUri();

        if (!readingChunks) {

            if (req.isChunked()) {
                readingChunks = true;
            } else {
                ChannelBuffer content = req.getContent();
                if (content.readable()) {

                    // handle topic rename query
                    if(uri.startsWith("/renameTopic")) {

                        String dataset = uri.substring(uri.lastIndexOf("/") + 1);
                        Map<String, String> params = TopicboxUtil.unpackQueryParams(content.toString(CharsetUtil.UTF_8));
                        
                        if (params.containsKey("id")) {
                            String value = params.get("id");
                            int topicNumber = Integer.parseInt(value.substring(value.lastIndexOf("_")+1));
                            topicModelManager.getModel(dataset).updateTopicName(topicNumber,params.get("value"));
                        }

                        response.setContent(ChannelBuffers.copiedBuffer(params.get("value"), CharsetUtil.UTF_8));
                    }
                }
            }

        } else {
            HttpChunk chunk = (HttpChunk)e.getMessage();
            if (chunk.isLast()) {
                readingChunks = false;
            }
            // ignore chunked content for now (TODO : implement this)
        }

        ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);


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

        JsonNode request =  mapper.readTree(((TextWebSocketFrame) frame).getText());

        if (logger.isDebugEnabled()) {
            logger.info(String.format("request: %s", request));
        }

        if (requestMatch(request, WebsocketRequests.LOAD_TOPICS)) {
            handleLoadTopicRequest(ctx, request);
        } else if (requestMatch(request, WebsocketRequests.LOAD_DATA)) {
            handleLoadDataRequest(ctx, request);
        } else if (requestMatch(request, WebsocketRequests.SUBMIT_LDA_TASK)) {
            submitLDARequest(ctx, request);
        } else if (requestMatch(request, WebsocketRequests.DESCRIBE_TOPIC)) {
            handleDescribeTopicRequest(ctx, request);
        } else if (requestMatch(request, WebsocketRequests.DESCRIBE_KEYWORD)) {
            handleDescribeKeywordRequest(ctx, request);
        } else if (requestMatch(request, WebsocketRequests.GET_KEYWORD_TOPIC_MATRIX)) {
            handleGetKeywordTopicMatrixRequest(ctx, request);
        } else if (requestMatch(request, WebsocketRequests.GET_KEYWORD_COOCCURRENCE)) {
            handleGetKeywordCooccurrenceRequest(ctx, request);
        }

    }

    /**
     * check whether query matches appropriate request type
     *
     * @param request
     * @param requestType
     * @return
     */
    private boolean requestMatch(JsonNode request, WebsocketRequests requestType) {
        return request.get("request").asText().startsWith(requestType.toString());
    }

    /**
     * load topic data request
     *
     * @param ctx
     * @param request
     * @throws Exception
     */
    private void handleLoadTopicRequest(ChannelHandlerContext ctx, JsonNode request) throws Exception {

        String datasetName = request.get("dataset").asText();

        if (topicModelManager.getModelCount() == 0 || !topicModelManager.containsModel(datasetName)) {
            ctx.getChannel().write(new TextWebSocketFrame(WebsocketResponses.MODEL_NOT_AVAILABLE.toString()));
            return;
        }

        TopicModel model = topicModelManager.getModel(datasetName);
        Object[][] topKeywords = model.getModelTopKeywords();

        String rsp = mapper.writeValueAsString(topKeywords);

        ctx.getChannel().write(new TextWebSocketFrame(rsp));
        ctx.getChannel().close();

    }

    /**
     * load data classification request
     *
     * @param ctx
     * @param request
     * @throws Exception
     */
    private void handleLoadDataRequest(ChannelHandlerContext ctx, JsonNode request) throws Exception {

        String datasetName = request.get("dataset").asText();
        int numEntries = request.get("numEntries").asInt();

        if (topicModelManager.getModelCount() == 0 || !topicModelManager.containsModel(datasetName)) {
            ctx.getChannel().write(new TextWebSocketFrame(WebsocketResponses.MODEL_NOT_AVAILABLE.toString()));
            ctx.getChannel().close();
            return;
        }

        TopicModel model = topicModelManager.getModel(datasetName);
        TextCorpus corpus = model.getCorpus();

        String rsp;

        if (model.ready()) {
            // draw docs from training sample - TODO : replace this
            Object[][] data = model.inferTopics(corpus.getDocs(numEntries));
            rsp = mapper.writeValueAsString(data);
        } else {
            rsp = "model inferencer not ready";
        }

        ctx.getChannel().write(new TextWebSocketFrame(rsp));
        ctx.getChannel().close();

    }

    /**
     * submit dataset for lda estimation
     *
     * @param ctx
     * @param request
     * @throws Exception
     */
    private void submitLDARequest(ChannelHandlerContext ctx, JsonNode request) throws Exception {

        String taskName = request.get("taskName").asText();
        String taskData = request.get("taskData").asText();
        int numTopics = request.get("numTopics").asInt();

        try {
            topicModelManager.submitTask(taskName, taskData, numTopics);
        } catch (Exception e) {
            ctx.getChannel().write(new TextWebSocketFrame("error submitting task " + taskName + " : " + e.getMessage()));
            ctx.getChannel().close();
            return;
        }

        ctx.getChannel().write(new TextWebSocketFrame(WebsocketResponses.TASK_NAME.toString()+taskName));

        TopicModel model = topicModelManager.getModel(taskName);

        while (!model.ready()) {
            if (model.estimationStarted()) {
                ctx.getChannel().write(new TextWebSocketFrame("model estimation in progress... " +
                        " expected time to complete : "
                        + model.getExpectedCompletionTime()/1000 + " sec"));
            } else {
                ctx.getChannel().write(new TextWebSocketFrame("initializing model generator ..."));
            }
            Thread.sleep(1000);
        }

        ctx.getChannel().write(new TextWebSocketFrame("model estimation complete ..."));
        ctx.getChannel().close();

    }

    /**
     * get topic details
     *
     * @param ctx
     * @param request
     * @throws Exception
     */
    private void handleDescribeTopicRequest(ChannelHandlerContext ctx, JsonNode request) throws Exception {

        int topicNumber = request.get("topicNumber").asInt();
        String dataset = request.get("dataset").asText();

        TopicModel model = topicModelManager.getModel(dataset);
        String rsp = mapper.writeValueAsString(model.getTopicInfo(topicNumber));

        ctx.getChannel().write(new TextWebSocketFrame(rsp));
        ctx.getChannel().close();

    }

    /**
     * get keyword details
     *
     * @param ctx
     * @param request
     * @throws Exception
     */
    private void handleDescribeKeywordRequest(ChannelHandlerContext ctx, JsonNode request) throws Exception {

        String keyword = request.get("keyword").asText();
        String dataset = request.get("dataset").asText();

        TopicModel model = topicModelManager.getModel(dataset);
        String rsp = mapper.writeValueAsString(model.getKeywordInfo(keyword));

        ctx.getChannel().write(new TextWebSocketFrame(rsp));
        ctx.getChannel().close();

    }

    /**
     * retrieve top keyword -> topic probability matrix
     *
     * @param ctx
     * @param request
     * @throws Exception
     */
    private void handleGetKeywordTopicMatrixRequest(ChannelHandlerContext ctx, JsonNode request) throws Exception {

        String dataset = request.get("dataset").asText();
        int maxKeywordsPerTopic = request.get("maxKeywordsPerTopic").asInt();

        TopicModel model = topicModelManager.getModel(dataset);

        String rsp = mapper.writeValueAsString(model.getKeywordTopicMatrix(maxKeywordsPerTopic));

        ctx.getChannel().write(new TextWebSocketFrame(rsp));
        ctx.getChannel().close();

    }

    private void handleGetKeywordCooccurrenceRequest(ChannelHandlerContext ctx, JsonNode request) throws Exception {

        String dataset = request.get("dataset").asText();
        int maxKeywordsPerTopic = request.get("maxKeywordsPerTopic").asInt();

        TopicModel model = topicModelManager.getModel(dataset);

        String rsp = mapper.writeValueAsString(model.getCooccurrenceMatrix(maxKeywordsPerTopic));
        ctx.getChannel().write(new TextWebSocketFrame(rsp));

        ctx.getChannel().close();

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


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws java.lang.Exception {
        e.getCause().printStackTrace();
        logger.error("error executing request : " + e);
        ctx.getChannel().close();
    }

}
