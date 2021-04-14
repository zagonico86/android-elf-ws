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
package com.zagonico.elfwstest;

import android.util.Base64;

import com.zagonico.elfws.auth.ElfWsAuth;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

public class MyCustomAuth implements ElfWsAuth {
    private String password;

    public MyCustomAuth(String password) {
        this.password = password;
    }

    @Override
    public boolean beforeRequest() {
        return true;
    }

    @Override
    public boolean modifyConnection(HttpURLConnection conn) {
        conn.setRequestProperty("X-My-Auth", password);
        return true;
    }

    @Override
    public String error() {
        return null;
    }
}