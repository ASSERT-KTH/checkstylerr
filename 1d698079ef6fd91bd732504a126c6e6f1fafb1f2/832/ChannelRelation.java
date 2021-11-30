package com.hlj.netty.websocket.selector;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户id和通道id的关系
 * 直接的关系
 */
public class ChannelRelation {

    //小兵的websocket
    private static ConcurrentHashMap<String,String> clientUidChannel = new ConcurrentHashMap<>();
    private static  ConcurrentHashMap<String,String> clientChannelUid = new ConcurrentHashMap<>();

    // 用户的id 对应通道的id
    //下面是存放通道的id， 对应用户的id
    public static void addRelation(String uid,Channel channel){
        clientUidChannel.put(uid,channel.id().asShortText());
        clientChannelUid.put(channel.id().asShortText(),uid);
    }

    public static void removeRelation(Channel channel) {
        String uid = clientChannelUid.get(channel.id().asShortText());
        if (uid != null) {
            clientChannelUid.remove(channel.id().asShortText());
            clientUidChannel.remove(uid);
        }
    }

    //根据用户id获取通道id
    public static String getChannelId(String uid) {
        return clientUidChannel.get(uid);
    }

}
