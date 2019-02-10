package uk.co.ridentbyte.model;

public class CardinalRequestAndResponse {

    private CardinalRequest request;
    private CardinalResponse response;

    public CardinalRequestAndResponse(CardinalRequest request, CardinalResponse response) {
        this.request = request;
        this.response = response;
    }

    public CardinalRequest getRequest() {
        return request;
    }

    public CardinalResponse getResponse() {
        return response;
    }
}
