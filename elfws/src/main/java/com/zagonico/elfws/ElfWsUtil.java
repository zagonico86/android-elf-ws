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

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

/**
 * Class with miscellaneous methods useful in the library or working with web services.
 *
 * @author zagonico
 * @version 1.0
 */
public class ElfWsUtil {
    /**
     * Encodes the passed String as UTF-8 using an algorithm that's compatible
     * with JavaScript's <code>encodeURIComponent</code> function. Returns
     * <code>null</code> if the String is <code>null</code>.
     *
     * @param s The String to be encoded
     * @return the encoded String
     */
    public static String encodeURIComponent(String s)
    {
        String result = null;

        try
        {
            result = URLEncoder.encode(s, "UTF-8")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        }
        // This exception should never occur.
        catch (UnsupportedEncodingException e)
        {
            result = s;
        }

        return result;
    }

    /**
     * Decodes the passed UTF-8 String using an algorithm that's compatible with
     * JavaScript's <code>decodeURIComponent</code> function. Returns
     * <code>null</code> if the String is <code>null</code>.
     *
     * @param s The UTF-8 encoded String to be decoded
     * @return the decoded String
     */
    public static String decodeURIComponent(String s)
    {
        if (s == null)
        {
            return null;
        }

        String result = null;

        try
        {
            result = URLDecoder.decode(s, "UTF-8");
        }
        // This exception should never occur.
        catch (UnsupportedEncodingException e)
        {
            result = s;
        }

        return result;
    }

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        md.update(textBytes, 0, textBytes.length);
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

    public static String readTextFile(String pathname) throws IOException {

        File file = new File(pathname);
        StringBuilder fileContents = new StringBuilder((int)file.length());

        Scanner scanner = new Scanner(file);
        while(scanner.hasNextLine()) {
            fileContents.append(scanner.nextLine() + "\r\n");
        }
        return fileContents.toString();
    }

    public static byte[] readBinaryFile(String pathname) throws IOException {

        File file = new File(pathname);
        byte[] outb = new byte[((int)file.length())];
        InputStream inputStream = new FileInputStream(file);

        int offset = 0;
        inputStream.read(outb);

        return outb;
    }

    public static byte[] readTextFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        return stringBuilder.toString().getBytes();
    }

    public static String concatenateParameters(Map<String, String> params) {
        String paramsString="";

        for (String param : params.keySet()) {
            paramsString += (paramsString.equals("") ? "" : "&") + param + "=" + encodeURIComponent(params.get(param));
        }

        return paramsString;
    }

    /**
     * Add GET params to url. It manages possibly pre-existing parameters in url.
     * @param url
     * @param params
     * @return url with added params
     */
    public static String addGetsToAddress(String url, Map<String, String> params) {
        String paramsString = concatenateParameters(params);

        if (url == null) return paramsString;

        if (url.contains("?")) return url += "&" + paramsString;

        return url += "?" + paramsString;
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public static String getInfoFromUri(Context context, Uri uri, String info) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(info));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static String getMimeFromUri(Context context, Uri uri) {
        ContentResolver cR = context.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    public static String YyyymmddHHiiss(int seconds) {
        Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
        calendar.add(Calendar.SECOND, seconds);

        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return f.format(calendar.getTime());
    }
}
