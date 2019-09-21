package uk.co.ridentbyte.model;

import java.net.http.HttpClient;
import java.util.Map;

public class CardinalHttpResponse {

    private String body;
    private Map<String, String> headers;
    private int statusCode;
    private HttpClient.Version version;

    public CardinalHttpResponse(String body, Map<String, String> headers, int statusCode, HttpClient.Version version) {
        this.body = body;
        this.headers = headers;
        this.statusCode = statusCode;
        this.version = version;
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

    public String getStatusLine() {
        if (version.equals(HttpClient.Version.HTTP_1_1)) {
            return "HTTP/1.1 " + statusCode;
        }
        return "HTTP/2.0 " + statusCode;
    }

    @Override
    public String toString() {
        return "CardinalHttpResponse{" +
                "body='" + body + '\'' +
                ", headers=" + headers +
                ", statusCode=" + statusCode +
                '}';
    }
}
