package com.teleone.chatuser.Models;

/**
 * Created by Hima on 9/17/2018.
 */

public class ChatModel {
    String msg , from ;

    public ChatModel(String msg, String from) {
        this.msg = msg;
        this.from = from;

    }

    public String getMsg() {
        return msg;
    }

    public String getFrom() {
        return from;
    }

}
