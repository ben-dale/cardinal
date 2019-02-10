package uk.co.ridentbyte.model;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class BashScript {

    private List<CardinalRequest> requests;
    private Config config;
    private long throttle;

    public BashScript(List<CardinalRequest> requests, Config config, int throttle) {
        this.requests = requests;
        this.config = config;
        this.throttle = throttle;
    }

    @Override
    public String toString() {
        String header = "#!/usr/bin/env bash";
        String date = "#Auto-generated " + new Date().toString();
        String delay = "";
        if (throttle > 0) {
            delay = "sleep " + throttle / 1000.0 + "\n";
        }
        String content = requests.stream().map(r -> r.toCurl(config)).collect(Collectors.joining("\necho\n" + delay)) + "\necho";
        return header + "\n\n" + date + "\n" + content;
    }
}
