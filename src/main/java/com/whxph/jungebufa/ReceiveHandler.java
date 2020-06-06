package com.whxph.jungebufa;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author liujun
 * @description
 * @create 2019-11-26 15:05
 */
@Component
@ChannelHandler.Sharable
public class ReceiveHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiveHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        byte[] receiveByte = Unpooled.copiedBuffer((ByteBuf) msg).array();
        StringBuilder str = new StringBuilder("[接收数据]: ");
        for (byte b : receiveByte) {
            str.append(String.format("%02X", b));
        }
        LOGGER.info(str.toString());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("连接成功");
        super.channelActive(ctx);
    }
}
