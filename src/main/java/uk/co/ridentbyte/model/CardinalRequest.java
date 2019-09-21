package uk.co.ridentbyte.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import uk.co.ridentbyte.Cardinal;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class CardinalRequest {

    public final static String csvHeaders = "request URI,request verb,follows redirects,request headers,request body";

    private final String uri;
    private final String verb;
    private final List<String> headers;
    private final String body;
    private final boolean followRedirects;
    private Vocabulary vocabulary;

    // for gson
    public CardinalRequest() {
        uri = null;
        verb = null;
        headers = List.of();
        body = null;
        followRedirects = false;
    }

    public CardinalRequest(String uri,
                           String verb,
                           List<String> headers,
                           String body,
                           boolean followRedirects,
                           Vocabulary vocabulary) {
        this.uri = uri;
        this.verb = verb;
        this.headers = headers;
        this.body = body;
        this.followRedirects = followRedirects;
        this.vocabulary = vocabulary;
    }

    public CardinalRequest(String uri,
                           String verb,
                           String[] headers,
                           String body,
                           boolean followRedirects,
                           Vocabulary vocabulary) {
        this.uri = uri;
        this.verb = verb;
        this.headers = Arrays.asList(headers);
        this.body = body;
        this.followRedirects = followRedirects;
        this.vocabulary = vocabulary;
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
                uri.replaceAll("#\\{id\\}", id),
                verb,
                headersWithId,
                bodyWithId,
                followRedirects,
                vocabulary
        );
    }

    public String toCurl(Config config) {
        return new Curl(
                uri, verb, followRedirects, body, headers, config.getEnvironmentVariables(), vocabulary
        ).toCommand();
    }

    public CardinalRequest processConstants(Config config) {
        List<EnvironmentVariable> vars = config.getEnvironmentVariables();
        String newUri = new RequestString(uri, vars, Cardinal.vocabulary).process();
        List<String> newHeaders = headers.stream()
                .map((h) -> new RequestString(h, vars, Cardinal.vocabulary).process())
                .collect(Collectors.toList());
        String newBody = body;
        if (newBody != null) {
            newBody = new RequestString(newBody, vars, Cardinal.vocabulary).process();
        }
        return new CardinalRequest(newUri, verb, newHeaders, newBody, followRedirects, vocabulary);
    }


    public String toCsv() {
        StringJoiner csvHeaders = new StringJoiner("\n");
        for (String header : headers) {
            csvHeaders.add(header.replace("\"", "\"\""));
        }

        String csvBody = body;
        if (csvBody == null) {
            csvBody = "";
        } else {
            csvBody = csvBody.replace("\"", "\"\"");
        }

        return "\"" + uri.replace("\"", "\"\"") + "\"," +
                "\"" + verb + "\"," +
                "\"" + followRedirects + "\"," +
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

    public boolean shouldFollowRedirects() {
        return followRedirects;
    }

    @Override
    public String toString() {
        return "CardinalRequest{" +
                "uri='" + uri + '\'' +
                ", verb='" + verb + '\'' +
                ", headers=" + headers +
                ", body='" + body + '\'' +
                ", followRedirects=" + followRedirects +
                '}';
    }
}
