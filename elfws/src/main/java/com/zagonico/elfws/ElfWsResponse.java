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
package com.zagonico.elfws;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/**
 * Response provided by {@link com.zagonico.elfws.ElfWsClient} and usable in {@link com.zagonico.elfws.ElfWsCallback}.
 *
 * @author zagonico
 * @version 1.0
 */
public class ElfWsResponse {
    public static int TYPE_NONE = 0;
    public static int TYPE_FILE = 1;
    public static int TYPE_JSON = 2;
    public static int TYPE_JSONARRAY = 3;

    private String filename;
    private String mime;
    private String content;
    private JSONObject jsonObject;
    private JSONArray jsonArray;
    private int type;
    private int responseCode;
    private Map<String, List<String>> headers;

    public ElfWsResponse(int code, Map<String, List<String>> headers) {
        this.responseCode = code;
        this.headers = headers;
    }

    /**
     * Add to response Content-Disposition with <code>filename</code>, Content-Type with
     * <code>mime</code> and <code>content</code> body. This check for json in response.
     * @param filename
     * @param mime
     * @param content
     */
    protected void addContentInfo(String filename, String mime, String content) {
        addContentInfo(filename, mime, content, false);
    }

    /**
     * Add to response Content-Disposition with <code>filename</code>, Content-Type with
     * <code>mime</code> and <code>content</code> body. In this constructor you can skip
     * json check.
     * @param filename
     * @param mime
     * @param content
     * @param skipJsonCheck
     */
    protected void addContentInfo(String filename, String mime, String content, boolean skipJsonCheck) {
        this.filename = filename;
        this.mime = mime;
        this.content = content;
        type = TYPE_FILE;

        if (!skipJsonCheck) {
            try {
                jsonObject = new JSONObject(content);
                type = TYPE_JSON;
            } catch (JSONException ex) {
                try {
                    jsonArray = new JSONArray(content);
                    type = TYPE_JSONARRAY;
                } catch (JSONException ex1) {
                    type = TYPE_FILE;
                }
            }
        }
    }

    /**
     * Headers of the response
     * @return Map<String, List<String>>
     */
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    /**
     * Response code
     * @return int response code
     */
    public int getResponseCode() {
        return responseCode;
    }

    public boolean isError() {
        return responseCode >= HttpURLConnection.HTTP_BAD_REQUEST;
    }

    public String getFilename() {
        return filename;
    }

    public String getMime() {
        return mime;
    }

    public String getContent() {
        return content;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public JSONArray getJsonArray() {
        return jsonArray;
    }

    public int getType() {
        return type;
    }
}
