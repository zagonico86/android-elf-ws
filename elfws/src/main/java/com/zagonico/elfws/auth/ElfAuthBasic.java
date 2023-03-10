/*
Copyright (c) 2021 Nicola Zago

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
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
