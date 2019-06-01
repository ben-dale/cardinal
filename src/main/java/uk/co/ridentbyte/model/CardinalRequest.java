package uk.co.ridentbyte.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import uk.co.ridentbyte.Cardinal;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class CardinalRequest {

    public final static String csvHeaders = "request URI,request verb,request headers,request body";

    private final String uri;
    private final String verb;
    private final List<String> headers;
    private final String body;

    public CardinalRequest(String uri, String verb, List<String> headers, String body) {
        this.uri = uri;
        this.verb = verb;
        this.headers = headers;
        this.body = body;
    }

    public CardinalRequest(String uri, String verb, String[] headers, String body) {
        this.uri = uri;
        this.verb = verb;
        this.headers = Arrays.asList(headers);
        this.body = body;
    }

    public String toJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this, CardinalRequest.class);
    }

    public CardinalRequest withId(String id) {
        List<String> headersWithId =
                this.headers.stream()
                .map(h -> h.replaceAll("#\\{id\\}", id))
                .collect(Collectors.toList());

        String bodyWithId = body;
        if (bodyWithId != null) {
            bodyWithId = bodyWithId.replaceAll("#\\{id\\}", id);
        }
        return new CardinalRequest(
                this.uri.replaceAll("#\\{id\\}", id),
                this.verb,
                headersWithId,
                bodyWithId
        );
    }

    public String toCurl(Config config) {
        return new Curl(uri, verb, body, headers, config.getEnvironmentVariables()).toCommand();
    }

    public CardinalRequest processConstants(Config config) {
        List<EnvironmentVariable> vars = config.getEnvironmentVariables();
        String newUri = new RequestString(this.uri, vars, Cardinal.vocabulary).process();
        List<String> newHeaders = this.headers.stream().map((h) -> {
           return new RequestString(h, vars, Cardinal.vocabulary).process();
        }).collect(Collectors.toList());
        String newBody = this.body;
        if (newBody != null) {
            newBody = new RequestString(newBody, vars, Cardinal.vocabulary).process();
        }
        return new CardinalRequest(newUri, verb, newHeaders, newBody);
    }


    public String toCsv() {
        StringJoiner csvHeaders = new StringJoiner("\n");
        for (String header : this.headers) {
            csvHeaders.add(header.replace("\"", "\"\""));
        }

        String csvBody = this.body;
        if (csvBody == null) {
            csvBody = "";
        } else {
            csvBody = csvBody.replace("\"", "\"\"");
        }

        return "\"" + uri.replace("\"", "\"\"") + "\"," +
                "\"" + verb + "\"," +
                "\"" + csvHeaders.toString() + "\"," +
                "\"" + csvBody + "\"";
    }

    public static CardinalRequest apply(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, CardinalRequest.class);
    }


    public String getUri() {
        return uri;
    }

    public String getBody() {
        return body;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public String getVerb() {
        return verb;
    }
}
