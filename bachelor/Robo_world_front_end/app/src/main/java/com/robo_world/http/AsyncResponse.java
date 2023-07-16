package com.robo_world.http;

import java.text.ParseException;

/**
 * Interface to handle async responses from http requests in main thread
 *
 * @author Blajan George
 */
public interface AsyncResponse {
    void processFinish(HttpResponse response) throws ParseException;
}
