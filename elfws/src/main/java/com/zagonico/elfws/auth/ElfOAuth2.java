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

import com.zagonico.elfws.ElfWsUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

/**
 * Class to manage OAuth2 authentication.
 * By default uses only https, but http can be enabled with {@link #allowHttp(boolean) allowHttp}.
 */
public class ElfOAuth2 implements ElfWsAuth {
    /**
     * OAuth2 parameters
     */
    private String clientId;
    private String clientSecret;
    private String tokenAddress;
    private String addictionalPostData;

    /**
     * OAuth2 status variables
     */
    private boolean allow_http;
    private String access_token;
    private String token_type;
    private int expires_in;
    private String expiration_datetime;
    private String refresh_token;
    private String error;

    public ElfOAuth2(String clientId, String clientSecret, String tokenAddress, String addictionalPostData) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenAddress = tokenAddress;
        this.addictionalPostData = addictionalPostData;
        access_token = "";
        token_type = "";
        expires_in = 0;
        refresh_token = "";
        expiration_datetime = "";
        error = "";
        allow_http = false;
    }

    public void allowHttp(boolean value) {
        allow_http  = value;
    }

    private boolean getToken() {
        boolean result = true;

        try {
            URL url = new URL(tokenAddress);
            HttpURLConnection conn;
            // if it's not https should not be valid! but maybe in some test environment it is http...
            if (allow_http && tokenAddress.startsWith("http:"))
                conn = (HttpURLConnection) url.openConnection();
            else
                conn = (HttpsURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);

            String key = clientId+":"+clientSecret;
            byte[] data = key.getBytes(StandardCharsets.UTF_8);
            String base64 = Base64.encodeToString(data, Base64.DEFAULT);

            conn.setRequestProperty("Authorization", "Basic "+base64);
            conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty( "cache-control", "no-cache" );
            //conn.setRequestProperty( "Accept", "*/*" );

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(addictionalPostData);
            writer.flush();

            String line;
            BufferedReader reader;
            if (conn.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                line = reader.readLine();

                JSONObject jsonObject = new JSONObject(line);
                access_token = jsonObject.getString("access_token");
                token_type = jsonObject.getString("token_type");
                expires_in = jsonObject.getInt("expires_in");
                expiration_datetime = ElfWsUtil.YyyymmddHHiiss(expires_in);
                refresh_token = jsonObject.getString("refresh_token");

                result = true;
            }
            else {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));

                error = "";
                while ((line = reader.readLine()) != null) {
                    error += ("".equals(error) ? "" : "\n") + line;
                }
                result = false;
            }

            writer.close();
            reader.close();
        } catch (Exception e)
        {
            error = e.getMessage();
            e.printStackTrace();
            result = false;
        }

        return result;
    }

    private boolean refreshToken() {
        boolean result = true;

        try {
            URL url = new URL(tokenAddress);
            HttpURLConnection conn;
            // if it's not https should not be valid! but maybe in some test environment it is http...
            if (allow_http && tokenAddress.startsWith("http:"))
                conn = (HttpURLConnection) url.openConnection();
            else
                conn = (HttpsURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);

            conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty( "cache-control", "no-cache" );
            //conn.setRequestProperty( "Accept", "*/*" );

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            // grant_type=refresh_token&refresh_token=xxxx&client_id=xxxx&client_secret=xxxx
            String postData = "grant_type=refresh_token&refresh_token="+refresh_token+
                        "&client_id="+clientId+"&client_secret="+clientSecret;
            writer.write(postData);
            writer.flush();

            String line;
            BufferedReader reader;
            if (conn.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                line = reader.readLine();

                JSONObject jsonObject = new JSONObject(line);
                access_token = jsonObject.getString("access_token");
                token_type = jsonObject.getString("token_type");
                expires_in = jsonObject.getInt("expires_in");
                expiration_datetime = ElfWsUtil.YyyymmddHHiiss(expires_in);
                refresh_token = jsonObject.getString("refresh_token");

                result = true;
            }
            else {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));

                error = "";
                while ((line = reader.readLine()) != null) {
                    error += ("".equals(error) ? "" : "\n") + line;
                }
                result = false;
            }

            writer.close();
            reader.close();
        } catch (Exception e)
        {
            error = e.getMessage();
            e.printStackTrace();
            result = false;
        }

        return result;
    }

    @Override
    public boolean beforeRequest() {
        boolean result = true;

        // if we don't have a token or it is expired
        if ("".equals(access_token) || expiration_datetime.compareTo(ElfWsUtil.YyyymmddHHiiss(0)) < 0) {
            // never got a token or we don't have refresh token. Get one
            if ("".equals(access_token) || refresh_token==null || "".equals(refresh_token)) {
                result = getToken();
            }
            // it is expired, refresh (post to tokenAddress with post:
            // grant_type=refresh_token&refresh_token=xxxx&client_id=xxxx&client_secret=xxxx
            else {
                result = refreshToken();
            }
        }

        return result;
    }

    @Override
    public boolean modifyConnection(HttpURLConnection conn) {
        if ("".equals(access_token) || expiration_datetime.compareTo(ElfWsUtil.YyyymmddHHiiss(0)) < 0) {
            return false;
        }
        conn.setRequestProperty("Authorization", "Bearer " + access_token);
        return true;
    }

    @Override
    public String error() {
        if (error!=null && !"".equals(error))
            return error;

        if ("".equals(access_token)) {
            return "No access token";
        }

        if (expiration_datetime.compareTo(ElfWsUtil.YyyymmddHHiiss(0)) < 0) {
            return "token expired";
        }

        return "-";
    }
}
