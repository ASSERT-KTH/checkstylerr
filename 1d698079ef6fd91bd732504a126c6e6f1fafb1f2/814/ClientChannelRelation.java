package com.hlj.netty.websocket.selector;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by j.sh on 27/11/2017.
 */
public class ClientChannelRelation {

    //小兵的websocket
    private static  ConcurrentHashMap<String,String> clientUidChannel = new ConcurrentHashMap<>();
    private static  ConcurrentHashMap<String,String> clientChannelUid = new ConcurrentHashMap<>();
    private  static  int i = 0;

    public static void addRelation(String uid,Channel channel){
        i = i+1;
        System.out.println(i);
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

    public static String getChannelId(String uid) {
        return clientUidChannel.get(uid);
    }

}
