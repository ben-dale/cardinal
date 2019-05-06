package uk.co.ridentbyte.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

public class FormUrlEncoded {

    private List<String> parameters;

    public FormUrlEncoded(String value) {
        if (value != null) {
            this.parameters = Arrays.asList(value.split("&"));
        } else {
            this.parameters = new ArrayList<>();
        }
    }

    public FormUrlEncoded(String[] parameters) {
        this.parameters = Arrays.asList(parameters);
    }

    public String header() {
        return "Content-Type: application/x-www-form-urlencoded";
    }

    public String lines() {
        StringJoiner joiner = new StringJoiner("\n");
        for (String parameter : parameters) {
            joiner.add(parameter);
        }
        return joiner.toString();
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("&");
        for (String parameter : parameters) {
            joiner.add(parameter);
        }
        return joiner.toString();
    }

}
