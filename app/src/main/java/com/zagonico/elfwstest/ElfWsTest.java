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

import android.content.Context;
import android.net.Uri;
import android.util.ArrayMap;

import com.zagonico.elfws.ElfWsClient;

import java.util.Map;

public class ElfWsTest extends ElfWsClient {
    public enum WsAction {
        NONE,
        GET_ONLY_JSON,
        POST_ONLY_JSON,
        POST_FILE_JSON
    }

    /** Constructor are not inherited... */
    public ElfWsTest(String url) {
        super(url);
    }

    private void configureActionGetBase(String param1)
    {
        resetAllRequests();

        Map<String,String> map = new ArrayMap<>();
        map.put("param1", param1);

        addGet(map);
    }

    public void configureActionGetJson(String param1)
    {
        configureActionGetBase(param1);

        Map<String,String> map = new ArrayMap<>();
        map.put("action", "get-only-json");

        addGet(map);
    }

    public void configureActionGetFile(String param1)
    {
        configureActionGetBase(param1);

        Map<String,String> map = new ArrayMap<>();
        map.put("action", "get-only-file");

        addGet(map);
    }

    private void configureActionPostBase(String param1)
    {
        resetAllRequests();

        Map<String,String> map = new ArrayMap<>();
        map.put("param1", param1);

        addPost(map);
    }

    public void configureActionPostJson(String param1)
    {
        configureActionPostBase(param1);

        Map<String,String> map = new ArrayMap<>();
        map.put("action", "post-only-json");

        addPost(map);
    }

    public void configureActionPostFile(String param1)
    {
        configureActionPostBase(param1);

        Map<String,String> map = new ArrayMap<>();
        map.put("action", "post-only-file");

        addPost(map);
    }

    public void configureActionFileBase(Context context, String paramGet, String paramPost, Uri file1, Uri file2)
    {
        resetAllRequests();

        Map<String,String> map = new ArrayMap<>();
        map.put("getparam", paramGet);
        addGet(map);

        map = new ArrayMap<>();
        map.put("param1", paramPost);
        addPost(map);

        addFile(context, file1, null, "file1");
        addFile(context, file2, null, "file2");
    }

    public void configureActionFileJson(Context context, String paramGet, String paramPost, Uri file1, Uri file2) {
        configureActionFileBase(context, paramGet, paramPost, file1, file2);

        Map<String, String> map = new ArrayMap<>();
        map.put("action", "post-file-json");
        addPost(map);
    }

    public void configureActionFileFile(Context context, String paramGet, String paramPost, Uri file1, Uri file2) {
        configureActionFileBase(context, paramGet, paramPost, file1, file2);

        Map<String, String> map = new ArrayMap<>();
        map.put("action", "post-file-file");
        addPost(map);
    }
}
