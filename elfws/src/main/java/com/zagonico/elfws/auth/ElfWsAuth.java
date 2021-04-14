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

import java.net.HttpURLConnection;

/**
 * Interface for authentication methods used in the framework.
 */
public interface ElfWsAuth {
    /**
     * method executed before the http request to check if everything is ready for the request
     * (e.g., in OAuth2 here a token is required or refreshed)
     * @return true if can proceed, false if error.
     */
    boolean beforeRequest();

    /**
     * Add the necessary data to connection (e.g., headers)
     * @param conn HttpURlConnection
     * @return true if success, false if error
     */
    boolean modifyConnection(HttpURLConnection conn);

    /**
     * The error occurred if other methods return false
     * @return error description
     */
    String error();
}
