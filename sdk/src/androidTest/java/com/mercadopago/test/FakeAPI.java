package com.mercadopago.test;

import com.mercadopago.util.HttpClientUtil;
import com.mercadopago.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * Created by mreverter on 1/7/16.
 */
public class FakeAPI {

    private static FakeAPI mInstance;

    public static synchronized FakeAPI getInstance() {
        if(mInstance == null) {
            mInstance = new FakeAPI();
        }
        return mInstance;
    }

    private List<QueuedResponse> queuedResponses;
    private FakeInterceptor fakeInterceptor;

    private FakeAPI() {}

    public QueuedResponse getNextResponse() {

        if(queuedResponses != null && !queuedResponses.isEmpty()) {
            return queuedResponses.remove(0);
        }
        else {
            return new QueuedResponse("", 401, "");
        }
    }

    public <T> void addResponseToQueue(T response, int statusCode, String reason) {
        if(queuedResponses == null) {
            queuedResponses = new ArrayList<>();
        }
        String jsonResponse = JsonUtil.getInstance().toJson(response);
        addResponseToQueue(jsonResponse, statusCode, reason);
    }

    public void addResponseToQueue(String jsonResponse, int statusCode, String reason) {
        if(queuedResponses == null) {
            queuedResponses = new ArrayList<>();
        }
        QueuedResponse queuedResponse = new QueuedResponse(jsonResponse, statusCode, reason);
        queuedResponses.add(queuedResponse);
    }

    public void start() {
        HttpClientUtil.setCustomClient(createClient());
    }

    public void shutDown() {
        HttpClientUtil.removeCustomClient();
        cleanQueue();
    }

    public void cleanQueue() {
        if(this.hasQueuedResponses()) {
            queuedResponses.clear();
        }
    }

    private OkHttpClient createClient() {

        okhttp3.OkHttpClient.Builder okHttpClientBuilder = new okhttp3.OkHttpClient.Builder()
                .addInterceptor(getFakeInterceptor());

        return okHttpClientBuilder.build();
    }

    private Interceptor getFakeInterceptor() {
        if(fakeInterceptor == null) {
            fakeInterceptor = new FakeInterceptor();
        }
        return fakeInterceptor;
    }

    public boolean hasQueuedResponses() {
        return queuedResponses != null && !queuedResponses.isEmpty();
    }

    public static class QueuedResponse {
        private String jsonResponse;
        private int statusCode;
        private String reason;

        public QueuedResponse(String jsonResponse, int statusCode, String reason) {
            this.jsonResponse = jsonResponse;
            this.reason = reason;
            this.statusCode = statusCode;
        }

        public String getBodyAsJson() {
            return jsonResponse;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}
