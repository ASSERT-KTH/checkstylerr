package com.hlj.netty.websocket.selector;

        import io.netty.channel.Channel;

        import java.util.concurrent.ConcurrentHashMap;

/**
 * 通道选择器，通过通道的id获取 通道
 */
public class ChannelSelector {

    private static ConcurrentHashMap<String,Channel> channelMap = new ConcurrentHashMap<>();

    public static void addChannel(Channel channel){
        channelMap.put(channel.id().asShortText(),channel);
    }

    public static Channel getChannel(String channelId) {
        return channelMap.get(channelId);
    }

    public static void removeChannel(Channel channel) {
        channelMap.remove(channel.id().asShortText());
        ChannelRelation.removeRelation(channel);
    }

}
