package com.robo_world.http;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.robo_world.model.ErrorInformation;
import com.robo_world.model.ErrorResponse;

import java.io.IOException;
import java.text.ParseException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Async task for http request
 *
 * @author Blajan George
 */
public class HttpRequestTask extends AsyncTask<Request, Void, HttpResponse> {
    private final AsyncResponse response;

    public HttpRequestTask(AsyncResponse response) {
        this.response = response;
    }

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected HttpResponse doInBackground(Request... requests) {
        try {
            Response response = client.newCall(requests[0]).execute();
            return new HttpResponse(response.isSuccessful(), response.body().string(), response.code());
        } catch (IOException e) {
            Log.e(this.toString(), "Http request error", e);
            return new HttpResponse(false, new Gson().toJson(new ErrorResponse(new ErrorInformation("An error occurred during request", Exception.class.getTypeName()))), 501);
        }
    }

    @Override
    protected void onPostExecute(HttpResponse response) {
        try {
            this.response.processFinish(response);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
