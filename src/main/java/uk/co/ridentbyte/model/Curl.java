package uk.co.ridentbyte.model;

import java.util.List;

public class Curl {

    private final boolean followRedirects;
    private String uri, verb, body;
    private List<String> headers;
    private List<EnvironmentVariable> envVars;
    private Vocabulary vocabulary;

    public Curl(String uri,
                String verb,
                boolean followRedirects,
                String body,
                List<String> headers,
                List<EnvironmentVariable> envVars,
                Vocabulary vocabulary) {
        this.uri = uri;
        this.verb = verb;
        this.followRedirects = followRedirects;
        this.body = body;
        this.headers = headers;
        this.envVars = envVars;
        this.vocabulary = vocabulary;
    }

    public String toCommand() {
        StringBuilder builder = new StringBuilder("curl -i \\\n");
        if (followRedirects) {
            builder.append("-L ");
        }

        for (String header : this.headers) {
            String processedString = new RequestString(header, this.envVars, vocabulary).process();
            builder.append("-H '");
            builder.append(processedString);
            builder.append("' \\\n");
        }

        if (this.body != null && !this.body.isEmpty()) {
            String processedString = new RequestString(this.body, this.envVars, vocabulary).process();
            builder.append("-d '");
            builder.append(processedString);
            builder.append("' \\\n");
        }

        String processedString = new RequestString(this.uri, this.envVars, vocabulary).process();
        builder.append("-X ");
        builder.append(this.verb);
        builder.append(" ");
        builder.append(processedString);
        return builder.toString();
    }

}
