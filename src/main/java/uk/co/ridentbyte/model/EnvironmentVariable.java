package uk.co.ridentbyte.model;

public class EnvironmentVariable {

    private String key, value;

    public EnvironmentVariable(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
