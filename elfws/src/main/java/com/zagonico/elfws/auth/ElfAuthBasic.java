package com.zagonico.elfws.auth;

import android.util.Base64;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

public class ElfAuthBasic implements ElfWsAuth {
    private String base64;

    public ElfAuthBasic(String username, String password) {
        String key = username+":"+password;
        byte[] data = key.getBytes(StandardCharsets.UTF_8);
        base64 = Base64.encodeToString(data, Base64.DEFAULT);
    }

    @Override
    public boolean beforeRequest() {
        return true;
    }

    @Override
    public boolean modifyConnection(HttpURLConnection conn) {
        conn.setRequestProperty("Authorization", "Basic " + base64);
        return true;
    }

    @Override
    public String error() {
        return null;
    }
}
