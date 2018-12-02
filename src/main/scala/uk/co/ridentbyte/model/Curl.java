package uk.co.ridentbyte.model;

import uk.co.ridentbyte.Cardinal;

import java.util.List;

public class Curl {

    private String uri, verb, body;
    private List<String> headers;
    private scala.collection.immutable.List<EnvironmentVariable> envVars;

    public Curl(String uri, String verb, String body, List<String> headers, scala.collection.immutable.List<EnvironmentVariable> envVars) {
        this.uri = uri;
        this.verb = verb;
        this.body = body;
        this.headers = headers;
        this.envVars = envVars;
    }

    public String toCommand() {
        StringBuilder builder = new StringBuilder("curl -i \\\n");

        for (String header : this.headers) {
            String processedString = new RequestString(header, this.envVars, Cardinal.vocabulary()).process();
            builder.append("-H '");
            builder.append(processedString);
            builder.append("' \\\n");
        }

        if (this.body != null && !this.body.isEmpty()) {
            String processedString = new RequestString(this.body, this.envVars, Cardinal.vocabulary()).process();
            builder.append("-d '");
            builder.append(processedString);
            builder.append("' \\\n");
        }

        String processedString = new RequestString(this.uri, this.envVars, Cardinal.vocabulary()).process();
        builder.append("-X ");
        builder.append(this.verb);
        builder.append(" ");
        builder.append(processedString);
        return builder.toString();
    }

}
