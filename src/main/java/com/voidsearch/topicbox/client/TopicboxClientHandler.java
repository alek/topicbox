package com.voidsearch.topicbox.client;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpResponse;

import org.jboss.netty.buffer.ChannelBufferIndexFinder;

public class TopicboxClientHandler extends SimpleChannelUpstreamHandler {

    private boolean readingChunks;
    ResponseCallback<String> callback;

    StringBuilder sb = new StringBuilder();

    public TopicboxClientHandler(ResponseCallback<String> callback) {
        this.callback = callback;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

        // get headers
        if (!readingChunks) {
            HttpResponse response = (HttpResponse)e.getMessage();
            if (response.isChunked()) {
                readingChunks = true;
            }
        // aggregate payload
        } else {
            HttpChunk chunk = (HttpChunk)e.getMessage();
            if (chunk.isLast()) {
                readingChunks = false;
                callback.close();
            }

            ChannelBuffer buf = chunk.getContent();

            // dispatch all entries to callback

            int startOffset = 0;
            int endOffset;

            // TODO : slow read - improve !
            while ((endOffset = buf.indexOf(startOffset, buf.capacity(), ChannelBufferIndexFinder.CRLF )) != -1) {
                for (int i=startOffset; i<endOffset; i++) {
                    sb.append((char)buf.getByte(i));
                }
                callback.addData(sb.toString());
                sb.setLength(0);
                startOffset = endOffset + 1;
            }

        }
    }

}
