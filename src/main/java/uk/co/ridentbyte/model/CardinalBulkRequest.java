package uk.co.ridentbyte.model;

import java.util.List;

public class CardinalBulkRequest {

    private CardinalRequest request;
    private int throttle, requestCount;
    private List<String> ids;

    public CardinalBulkRequest(CardinalRequest request, int throttle, int requestCount, List<String> ids) {
        this.request = request;
        this.throttle = throttle;
        this.requestCount = requestCount;
        this.ids = ids;
    }

    public CardinalRequest getRequest() {
        return request;
    }

    public int getThrottle() {
        return throttle;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public List<String> getIds() {
        return ids;
    }
}
