package uk.co.ridentbyte.model;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class BasicAuth {

    private String username, password;

    public BasicAuth(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String asAuthHeader() {
        String encoded = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        return "Authorization: Basic " + encoded;
    }
}
