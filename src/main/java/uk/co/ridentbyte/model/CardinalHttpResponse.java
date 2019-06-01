package uk.co.ridentbyte.model;

import java.util.Map;

public class CardinalHttpResponse {

    private String body;
    private Map<String, String> headers;
    private int statusCode;

    public CardinalHttpResponse(String body, Map<String, String> headers, int statusCode) {
        this.body = body;
        this.headers = headers;
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

}
