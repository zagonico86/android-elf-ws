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
