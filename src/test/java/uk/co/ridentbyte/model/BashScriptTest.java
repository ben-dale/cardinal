package uk.co.ridentbyte.model;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class BashScriptTest {

    private final Config config = new Config();
    private final LocalDateTime generatedAt = LocalDateTime.of(2011, 3, 31, 10, 56, 0);
    private final Vocabulary vocabulary = new Vocabulary(
            new Words(List.of("firstName"), new Random()),
            new Words(List.of("lastName"), new Random()),
            new Words(List.of("place"), new Random()),
            new Words(List.of("object"), new Random()),
            new Words(List.of("action"), new Random()),
            new Words(List.of("country"), new Random()),
            new Words(List.of("communication"), new Random()),
            new Words(List.of("businessEntity"), new Random()),
            new Words(List.of("lorem"), new Random()),
            new Words(List.of("emoji"), new Random())
    );

    @Test
    public void toStringGeneratesScriptWithoutThrottle() {
        var request = new CardinalRequest("uri", "verb", List.of(), "body", false, vocabulary);
        var throttle = 0;
        var bashScript = new BashScript(List.of(request), config, throttle, generatedAt);

        var result = bashScript.toString();

        var expected = "#!/usr/bin/env bash\n" +
                "\n" +
                "#Auto-generated by Cardinal - 2011-03-31T10:56:00\n" +
                "curl -i \\\n" +
                "-d 'body' \\\n" +
                "-X verb uri\n" +
                "echo";
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void toStringGeneratesScriptWithThrottle() {
        var request = new CardinalRequest("uri", "verb", List.of(), "body", false, vocabulary);
        var throttle = 100;
        var bashScript = new BashScript(List.of(request, request), config, throttle, generatedAt);

        var result = bashScript.toString();

        var expected = "#!/usr/bin/env bash\n" +
                "\n" +
                "#Auto-generated by Cardinal - 2011-03-31T10:56:00\n" +
                "curl -i \\\n" +
                "-d 'body' \\\n" +
                "-X verb uri\n" +
                "echo\n" +
                "sleep 0.1\n" +
                "curl -i \\\n" +
                "-d 'body' \\\n" +
                "-X verb uri\n" +
                "echo";
        assertThat(result).isEqualTo(expected);
    }


}