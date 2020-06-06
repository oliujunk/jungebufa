package com.whxph.jungebufa;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author liujun
 * @description 重复读取设备ID号
 * @create 2019-08-12 17:44
 */
@Component
@ChannelHandler.Sharable
public class IdleStateTrigger extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdleStateTrigger.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                ByteBuf out = Unpooled.buffer(4);
                String msg = "0D0A";
                out.writeBytes(ByteBufUtil.decodeHexDump(msg));
                ctx.writeAndFlush(out);
                LOGGER.info("心跳");
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
