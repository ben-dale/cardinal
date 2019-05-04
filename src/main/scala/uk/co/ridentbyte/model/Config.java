package uk.co.ridentbyte.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

public class Config {

    private List<EnvironmentVariable> environmentVariables;

    public Config(List<EnvironmentVariable> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    public Config(String json) {
        Gson gson = new Gson();
        Config config = gson.fromJson(json, Config.class);
        this.environmentVariables = config.environmentVariables;
    }

    public String toJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(this));
        return gson.toJson(this);
    }

    public Config withEnvironmentVariables(List<EnvironmentVariable> environmentVariables) {
        return new Config(environmentVariables);
    }

    public List<EnvironmentVariable> getEnvironmentVariables() {
        return this.environmentVariables;
    }
}