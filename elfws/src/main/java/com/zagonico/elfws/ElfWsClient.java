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

import android.content.Context;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Base64;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Class that allows to communicate with a web service.
 *
 * <code>
 *     final Activity local = this;
 *
 *     ElfWsClient client = new ElfWsClient("http://mydomain.com/ws");
 *     client.addGet(map);
 *     client.setCallback(new ElfWsCallback {
 *        public boolean processResult(ElfWsResponse response) {
 *           runOnUiThread(new Runnable() {
 *              public void run() {
 *                 Toast.makeText(local, response.get, Toast.LENGTH_LONG).show();
 *              }
 *           });
 *        }
 *     });
 *
 *     
 * </code>
 *
 * @author zagonico
 * @version 1.0
 */
public class ElfWsClient implements Runnable {
    /**
     * Debug mode, is true the exceptions are printed. This can be set and read with
     * {@link #setDebugMode(boolean) setDebugMode} and {@link #isDebug() isDebug} methods.
     */
    private boolean DEBUG_MODE = false;

    /**
     * Processing status. When {@link #run() run} executes it is set to <code>true</code> so that
     * all the other methods of the class won't change the class status until the current request
     * is complete. It is
     */
    private boolean PROCESSING = false;

    public enum ElfWsAction {
        GET,
        POST,
        JSON_UPLOAD,
        FILE_UPLOAD,
    };

    List<ElfWsAction> actions;
    Map<String, String> getParameters;
    Map<String, String> postParameters;

    JSONObject jsonToUpload;

    List<String> stringToUpload_fieldName;
    List<String> stringToUpload_fileName;
    List<String> stringToUpload_mime;
    List<byte[]> stringToUpload;

    private String url;
    private ElfWsCallback callback;

    public ElfWsClient() {
        this(null, null);
    }

    /**
     * Constructor where the url of ws is specified.
     * @param url the ws url
     */
    public ElfWsClient(String url) {
        this(url, null);
    }

    /**
     * Constructor where the url of ws and the callback that will elaborate the response are specified.
     * @param url the ws url
     * @param callback the callback that will elaborate the response
     */
    public ElfWsClient(String url, ElfWsCallback callback) {
        this.url = url;
        this.callback = callback;
        resetAllRequests();
    }

    /**
     * The url to which the requests will be sent.
     * @return String url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Set the url to which the requests will be sent.
     * @param url string
     */
    public void setUrl(String url) {
        if (PROCESSING) throw new IllegalThreadStateException("Cannot change parameters while performing a http request");
        this.url = url;
    }

    /**
     * The callback to which the response will be passed
     * @return ElfWsCallback
     */
    public ElfWsCallback getCallback() {
        return callback;
    }

    /**
     * Set the {@link com.zagonico.elfws.ElfWsCallback ElfWsCallback} that will process the
     * {@link com.zagonico.elfws.ElfWsResponse ElfWsResponse} produced by the elaboration of the
     * current request.
     *
     * @param callback
     *        instance of {@link com.zagonico.elfws.ElfWsCallback ElfWsCallback}
     */
    public void setCallback(ElfWsCallback callback) {
        if (PROCESSING) throw new IllegalThreadStateException("Cannot change parameters while performing a http request");
        this.callback = callback;
    }

    /**
     * Clean current pending requests.
     */
    public void resetAllRequests() {
        resetAllRequests(false);
    }

    /**
     * The <code>skipCheck</code> parameter is only used when in a {@link #run()} execution
     */
    protected void resetAllRequests(boolean skipCheck) {
        if (PROCESSING && !skipCheck) throw new IllegalThreadStateException("Cannot change parameters while performing a http request");

        actions = new ArrayList<>();
        getParameters = null;
        postParameters = null;
        jsonToUpload = null;
        stringToUpload_fieldName = new ArrayList<>();
        stringToUpload_fileName = new ArrayList<>();
        stringToUpload_mime = new ArrayList<>();
        stringToUpload = new ArrayList<>();
    }

    /**
     * Reset specific subrequests.
     *
     * @param get
     *        if true reset get parameters
     *
     * @param post
     *        if true reset post parameters
     *
     * @param file
     *        if true reset files from File source
     *
     * @param json
     *        if true reset json
     */
    public void resetRequest(boolean get, boolean post, boolean file, boolean json) {
        if (PROCESSING) throw new IllegalThreadStateException("Cannot change parameters while performing a http request");

        if (get) {
            actions.remove(ElfWsAction.GET);
            getParameters = null;
        }
        if (post) {
            actions.remove(ElfWsAction.POST);
            postParameters = null;
        }
        if (file) {
            actions.remove(ElfWsAction.FILE_UPLOAD);
            stringToUpload_fieldName = new ArrayList<>();
            stringToUpload_fileName = new ArrayList<>();
            stringToUpload_mime = new ArrayList<>();
            stringToUpload = new ArrayList<>();
        }
        if (json) {
            actions.remove(ElfWsAction.JSON_UPLOAD);
            jsonToUpload = null;
        }
    }


    /**
     * Add GET parameters to current request. Calls of this method are cumulative
     *
     * @param args
     *        get params to be added
     *
     * @return true is correctly enqueued, false if enqueuement fails (actually returns always true)
     */
    public boolean addGet(Map<String, String> args) {
        if (PROCESSING) throw new IllegalThreadStateException("Cannot change parameters while performing a http request");

        if (!actions.contains(ElfWsAction.GET)) actions.add(ElfWsAction.GET);

        if (getParameters != null)
            getParameters.putAll(args);
        else
            getParameters = args;

        return true;
    }

    /**
     * Add POST parameters to current request. Calls of this method are cumulative.
     *
     * @param args
     *        get params to be added
     *
     * @return true is correctly enqueued, false if enqueuement fails (actually returns always true)
     */
    public boolean addPost(Map<String, String> args) {
        if (PROCESSING) throw new IllegalThreadStateException("Cannot change parameters while performing a http request");

        if (!actions.contains(ElfWsAction.POST)) actions.add(ElfWsAction.POST);

        if (postParameters != null)
            postParameters.putAll(args);
        else
            postParameters = args;

        return true;
    }

    /**
     * Enqueue a file to current request (from File).
     *
     * @param file
     *        the file to be posted
     *
     * @param fileName
     *        file name for this file. If null uses <code>file</code>'s filename
     *
     * @param fieldName
     *        field name for this file
     *
     * @return true is correctly enqueued, false if enqueuement fails
     */
    public boolean addFile(File file, String fileName, String fieldName) {
        if (PROCESSING) throw new IllegalThreadStateException("Cannot change parameters while performing a http request");

        if (file == null || !file.exists() || file.isDirectory()) return false;

        if (!actions.contains(ElfWsAction.FILE_UPLOAD)) actions.add(ElfWsAction.FILE_UPLOAD);

        try {
            byte[] content = ElfWsUtil.readBinaryFile(file.getAbsolutePath());
            stringToUpload.add(content);
            stringToUpload_fieldName.add(fieldName);
            stringToUpload_fileName.add(fileName != null ? fileName : file.getName());
            stringToUpload_mime.add(ElfWsUtil.getMimeType(file.getAbsolutePath()));
            return true;
        }
        catch (Exception e) {
            if (DEBUG_MODE) e.printStackTrace();
        }

        return false;
    }

    /**
     * Enqueue a file to current request (from String).
     *
     * @param content
     *        the file content to be posted
     *
     * @param fileName
     *        file name for this file
     *
     * @param fieldName
     *        field name for this file
     *
     * @param mime
     *        mime for this file
     *
     * @return true is correctly enqueued, false if enqueuement fails
     */
    public boolean addFile(String content, String fileName, String fieldName, String mime) {
        if (PROCESSING) throw new IllegalThreadStateException("Cannot change parameters while performing a http request");

        if (content == null) return false;

        if (!actions.contains(ElfWsAction.FILE_UPLOAD)) actions.add(ElfWsAction.FILE_UPLOAD);

        stringToUpload.add(content.getBytes());
        stringToUpload_fieldName.add(fieldName);
        stringToUpload_fileName.add(fileName);
        stringToUpload_mime.add(mime);

        return true;
    }

    /**
     * Enqueue a file to current request (from InputStream).
     *
     * @param inputStream
     *        inputStream with the file content to be posted
     *
     * @param fileName
     *        file name for this file
     *
     * @param fieldName
     *        field name for this file
     *
     * @param mime
     *        mime for this file
     *
     * @return true is correctly enqueued, false if enqueuement fails
     */
    public boolean addFile(InputStream inputStream, String fileName, String fieldName, String mime) {
        if (PROCESSING) throw new IllegalThreadStateException("Cannot change parameters while performing a http request");

        if (inputStream == null) return false;

        if (!actions.contains(ElfWsAction.FILE_UPLOAD)) actions.add(ElfWsAction.FILE_UPLOAD);

        try {
            stringToUpload.add(ElfWsUtil.readTextFromInputStream(inputStream));
            stringToUpload_fieldName.add(fieldName);
            stringToUpload_fileName.add(fileName);
            stringToUpload_mime.add(mime);
        }
        catch (Exception e) {
            if (DEBUG_MODE) e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Enqueue a file to current request (from Uri). If <code>fileName</code> is null it uses the
     * <code>file</code>'s filename. Context is necessary to solve the Uri.
     *
     * @param context
     *        context to resolve uri properties
     *
     * @param file
     *        the file to be posted
     *
     * @param fileName
     *        file name for this file
     *
     * @param fieldName
     *        field name for this file
     *
     * @return true is correctly enqueued, false if enqueuement fails
     */
    public boolean addFile(Context context, Uri file, String fileName, String fieldName) {
        if (PROCESSING) throw new IllegalThreadStateException("Cannot change parameters while performing a http request");

        if (file == null) return false;

        if (!actions.contains(ElfWsAction.FILE_UPLOAD)) actions.add(ElfWsAction.FILE_UPLOAD);

        try {
            byte[] content = ElfWsUtil.readTextFromInputStream(context.getContentResolver().openInputStream(file));
            stringToUpload.add(content);
            stringToUpload_fieldName.add(fieldName);
            stringToUpload_fileName.add(fileName!=null ? fileName : ElfWsUtil.getInfoFromUri(context, file, OpenableColumns.DISPLAY_NAME));
            stringToUpload_mime.add(ElfWsUtil.getMimeFromUri(context, file));
        }
        catch (Exception e) {
            if (DEBUG_MODE) e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean isPost() {
        return actions.contains(ElfWsAction.POST);
    }

    private boolean isUpload() {
        return actions.contains(ElfWsAction.FILE_UPLOAD)
                || actions.contains(ElfWsAction.JSON_UPLOAD);
    }

    /**
     * Make the request to address using data specified by previous addPost, addGet and addFile.
     *
     * @param address
     *        base url to which the request will be sent
     *
     * @return ElfWsResponse
     */
    public ElfWsResponse httpRequest(String address) {
        return httpRequest(address, false);
    }

    /** This protected method as a skip option for {@link #run()} method. */
    protected ElfWsResponse httpRequest(String address, boolean skipCheck) {
        if (PROCESSING && !skipCheck) throw new IllegalThreadStateException("Cannot change parameters while performing a http request");

        ElfWsResponse elfWsResponse = null;

        try {
            if (actions.contains(ElfWsAction.GET)) {
                address = ElfWsUtil.addGetsToAddress(address, getParameters);
            }

            URL url = new URL(address);

            HttpURLConnection conn;
            if (address.startsWith("https:"))
                conn = (HttpsURLConnection) url.openConnection();
            else
                conn = (HttpURLConnection) url.openConnection();

            if (isPost() || isUpload()) {
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
            }
            else {
                conn.setDoOutput(false);
                conn.setRequestMethod("GET");
            }
            conn.setDoInput(true);
            conn.setUseCaches(false);

            String boundary = "------"+System.currentTimeMillis();
            if (isUpload()) {       // with files it needs to be multipart
                conn.setRequestProperty("content-type", "multipart/form-data;; boundary="+boundary); //boundary="+boundary
            }
            else if (isPost()) {    // normal form post
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            }
            conn.setRequestProperty( "cache-control", "no-cache" );
            conn.setRequestProperty( "Accept", "*/*" );

            if (isUpload() || isPost()) {
                conn.setRequestProperty("Expect", "100-continue");
                conn.setRequestProperty("Connection", "close");

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

                writePost(writer, boundary);

                writeFiles(writer, boundary);

                // last boundary
                writer.write("--" + boundary + "--\r\n");
                writer.flush();
                writer.close();
            }

            String line, response = "";
            BufferedReader reader = new BufferedReader(new
                    InputStreamReader(conn.getInputStream()));
            while ((line = reader.readLine()) != null) {
                response += ("".equals(response) ? "" : "\n") + line;
            }

            String contentDisp = conn.getHeaderField("Content-Disposition");
            // raw = "attachment; filename=abc.jpg"
            String fileName = "";
            if (contentDisp != null && contentDisp.contains("=")) {
                fileName = contentDisp.split("=")[1];
                fileName = fileName.substring(1, fileName.length()-1);
            }

            String contentType = conn.getHeaderField("Content-Type");
            // raw = "attachment; filename=abc.jpg"
            String mime = "";
            if (contentDisp != null && contentDisp.contains(":")) {
                mime = contentType.split(":")[1];
                mime = mime.substring(1, mime.length()-1);
            }

            elfWsResponse = new ElfWsResponse(fileName, mime, response);

            reader.close();
        }
        catch (Exception e) {
            if (DEBUG_MODE) e.printStackTrace();
        }

        resetAllRequests(skipCheck);

        return elfWsResponse;
    }

    private void writePost(OutputStreamWriter writer, String boundary) {
        try {
            if (!isUpload()) {
                writer.write(ElfWsUtil.concatenateParameters(postParameters));
            }
            else {
                for (String key : postParameters.keySet()) {
                    String value = postParameters.get(key);

                    writer.write("--" + boundary + "\r\n");
                    writer.write("Content-Disposition: form-data; name=\"" + key + "\"\r\n");
                    writer.write("\r\n");
                    writer.write(value+"\r\n");
                }
            }
        }
        catch (Exception e) {
            if (DEBUG_MODE) e.printStackTrace();
        }
    }

    private void writeFiles(OutputStreamWriter writer, String boundary) {
        try {
            if (jsonToUpload != null) {
                writer.write("--" + boundary + "\r\n");
                writer.write("Content-Disposition: form-data; name=\"jsonData\"\r\n");
                writer.write("Content-Type: application/json; charset=utf-8\r\n");
                writer.write("\r\n");
                String content = "";
                try {
                    content = jsonToUpload.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                writer.write(content);
                writer.write("\r\n");
            }

            if (stringToUpload != null && stringToUpload.size()>0) {
                for (int i=0; i<stringToUpload.size(); i++) {
                    byte[] content = stringToUpload.get(i);
                    String basename = stringToUpload_fieldName.get(i);
                    String filename = stringToUpload_fileName.get(i);
                    String mime = stringToUpload_mime.get(i);

                    writer.write("--" + boundary + "\r\n");
                    writer.write("Content-Disposition: form-data; name=\"" +
                            basename + "\"; filename=\"" +
                            filename + "\"" + "\r\n");
                    writer.write("Content-Type: " + mime + "\r\n");
                    writer.write("\r\n");
                    byte[] encoded = Base64.encode(content, Base64.DEFAULT);
                    writer.write(new String(encoded));
                    writer.write("\r\n");
                }
            }
        }
        catch (Exception e) {
            if (DEBUG_MODE) e.printStackTrace();
        }
    }

    /**
     * Return the debug mode status.
     * @return boolean
     */
    public boolean isDebug() {
        return DEBUG_MODE;
    }

    /**
     * Set the debug mode status
     * @param enabled
     *        enables or disables the debug mode
     */
    public void setDebugMode(boolean enabled) {
        DEBUG_MODE = enabled;
    }

    /**
     * Executes the request currently defined in the class status and passes the {@link com.zagonico.elfws.ElfWsResponse ElfWsResponse} to
     * the {@link com.zagonico.elfws.ElfWsCallback ElfWsCallback} possibly configured.
     */
    @Override
    public void run() {
        if (url == null || "".equals(url)) throw new IllegalStateException("No url specified");

        PROCESSING = true;

        ElfWsResponse response = httpRequest(url, true);

        if (callback != null) {
            try {
                callback.processResponse(response);
            }
            catch (Exception e) {
                if (DEBUG_MODE) e.printStackTrace();
            }
        }

        PROCESSING = false;
    }

    /**
     * Executes the request currently defined in the class status in a separated thread.
     */
    public void executeRequest() {
        Thread thread = new Thread(this);
        thread.start();
    }
}
