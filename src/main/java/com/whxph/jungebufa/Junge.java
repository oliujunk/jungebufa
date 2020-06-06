package com.whxph.jungebufa;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * @author liujun
 */
@Component
public class Junge {

    private static final Logger LOGGER = LoggerFactory.getLogger(Junge.class);

    @Resource
    private ReceiveHandler receiveHandler;

    @Resource
    private IdleStateTrigger idleStateTrigger;

    @SuppressWarnings("unused")
    @NacosValue(value = "${junge.id}", autoRefreshed = true)
    private String id;

    @SuppressWarnings("unused")
    @NacosValue(value = "${junge.pm25}", autoRefreshed = true)
    private Float pm25;

    @SuppressWarnings("unused")
    @NacosValue(value = "${junge.pm10}", autoRefreshed = true)
    private Float pm10;

    @SuppressWarnings("unused")
    @NacosValue(value = "${junge.noise}", autoRefreshed = true)
    private Float noise;

    @SuppressWarnings("unused")
    @NacosValue(value = "${junge.windDirection}", autoRefreshed = true)
    private Float windDirection;

    @SuppressWarnings("unused")
    @NacosValue(value = "${junge.windSpeed}", autoRefreshed = true)
    private Float windSpeed;

    @SuppressWarnings("unused")
    @NacosValue(value = "${junge.temperature}", autoRefreshed = true)
    private Float temperature;

    @SuppressWarnings("unused")
    @NacosValue(value = "${junge.humidity}", autoRefreshed = true)
    private Float humidity;

    @SuppressWarnings("unused")
    @NacosValue(value = "${junge.token}", autoRefreshed = true)
    private String token;

    private Channel channel;

    public void start() {
        connect();
    }

    @Scheduled(cron = "0 */4 * * * ?")
    public void update() {
        String head = "8888";
        String sjlx = "00";
        String bysj = "0000000000000000";
        String end = "0304";
        String[] deviceIds = id.split(",");
        for (String deviceId : deviceIds) {
            try {
                String xxbm = RandomStringUtils.randomNumeric(8);
                String data = "000000";
                String hexVal;
                DecimalFormat df = new DecimalFormat("0.0");
                //PM2.5
                hexVal = Integer.toHexString((int) ((int) ((new Float(df.format(pm25)) + Math.random() * 10 - 5)) * 10 + Math.random() * 10));
                data += StringUtils.leftPad(hexVal, 4, "0");
                //PM10
                hexVal = Integer.toHexString((int) ((int) ((new Float(df.format(pm10)) + Math.random() * 10 - 5)) * 10 + Math.random() * 10));
                data += StringUtils.leftPad(hexVal, 4, "0");
                //噪声
                hexVal = Integer.toHexString((int) ((int) (new Float(df.format(noise)) * 10) + Math.random() * 10));
                data += StringUtils.leftPad(hexVal, 4, "0");
                //风向
                hexVal = Integer.toHexString((int) ((int) (new Float(df.format(windDirection)) * 10) + Math.random() * 10));
                data += StringUtils.leftPad(hexVal, 4, "0");
                //风速
                hexVal = Integer.toHexString((int) ((int) (new Float(df.format(windSpeed)) * 10) + Math.random() * 10));
                data += StringUtils.leftPad(hexVal, 4, "0");
                //温度
                hexVal = Integer.toHexString((int) ((int) (new Float(df.format(temperature)) * 10) + Math.random() * 10));
                data += StringUtils.leftPad(hexVal, 4, "0");
                //湿度
                hexVal = Integer.toHexString((int) ((int) (new Float(df.format(humidity)) * 10) + Math.random() * 10));
                data += StringUtils.leftPad(hexVal, 4, "0");

                data += "0000000000000000";
                //数据采集起始时间
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
                data += LocalDateTime.now().minusMinutes(5).format(dtf);

                //数据采集终止时间
                data += LocalDateTime.now().format(dtf);
                //数据发送时间
                data += LocalDateTime.now().format(dtf);

                String temp = head + xxbm + deviceId + token + sjlx + data + bysj;

                byte xorValue = getXor(ByteBufUtil.decodeHexDump(temp));
                String xorStr = String.format("%02X", xorValue);
                String message = temp + xorStr + end;
                if (channel.isActive()) {
                    channel.writeAndFlush(Unpooled.copiedBuffer(ByteBufUtil.decodeHexDump(message)));
                    LOGGER.info("[{}]: {}", deviceId, message);
                } else {
                    LOGGER.error("[{}]:发送异常", deviceId);
                }
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                LOGGER.error("[{}]:发送异常", deviceId, e);
            }
        }
    }

    private byte getXor(byte[] data) {
        byte temp = data[0];
        for (int i = 1; i < data.length; i++) {
            temp ^= data[i];
        }
        return temp;
    }

    private void connect() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new IdleStateHandler(0, 10, 0, TimeUnit.SECONDS));
                    pipeline.addLast(idleStateTrigger);
                    pipeline.addLast(receiveHandler);
                }
            });

            ChannelFuture channelFuture = bootstrap.connect("119.164.253.229", 8888).sync();
            channel = channelFuture.channel();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        } finally {
            workerGroup.shutdownGracefully();
            try {
                TimeUnit.SECONDS.sleep(5);
                LOGGER.info("重新连接");
                if (channel == null || !channel.isActive()) {
                    connect();
                }
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }
}
