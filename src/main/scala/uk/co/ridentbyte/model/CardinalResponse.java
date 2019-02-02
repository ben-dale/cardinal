package uk.co.ridentbyte.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.HashMap;
import java.util.Map;

public class CardinalResponse {

    private CardinalHttpResponse raw;
    private long time;

    public CardinalResponse(CardinalHttpResponse raw, long time) {
        this.raw = raw;
        this.time = time;
    }

    public String formattedBody() {
        String contentType = raw.getHeaders().get("Content-Type");
        if (contentType.contains("application/json")) {
            try {
                JsonParser parser = new JsonParser();
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                JsonElement el = parser.parse(this.raw.getBody());
                return gson.toJson(el);
            } catch (JsonSyntaxException jse) {
                return this.raw.getBody();
            }
        } else {
            return this.raw.getBody();
        }
    }

    public String toCSV() {
        String statusCodeString = "\"" + this.raw.getStatusCode() + "\"";
        String bodyString = "\"" + this.formattedBody().replace("\"", "\"\"") + "\"";
        String timeString = "\"" + this.time + "\"";
        StringBuilder headerStringBuilder = new StringBuilder();
        for (Map.Entry<String, String> header : this.raw.getHeaders().entrySet()) {
            headerStringBuilder.append(header.getKey());
            headerStringBuilder.append(":");
            headerStringBuilder.append(header.getValue());
            headerStringBuilder.append("\n");
        }
        String headersString = "\"" + headerStringBuilder.toString().replace("\"", "\"\"") + "\"";
        return statusCodeString + "," + headersString + "," + bodyString + "," + timeString;
    }

    public long getTime() {
        return this.time;
    }

    public int getStatusCode() {
        return this.raw.getStatusCode();
    }

    public Map<String, String> getHeaders() {
        return this.raw.getHeaders();
    }

    public static CardinalResponse blank() {
        return new CardinalResponse(new CardinalHttpResponse("", new HashMap<>(), 0), 0L) {
            @Override
            public String toCSV() {
                return ",,,";
            }

            @Override
            public String formattedBody() {
                return "";
            }
        };
    }

    public static String csvHeaders() {
        return "response code,response headers,response body,response time (ms)";
    }

}
