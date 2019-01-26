package uk.co.ridentbyte.model;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Words {

    private List<String> values;
    private Random random;

    public Words(List<String> values, Random random) {
        this.values = values;
        this.random = random;
    }

    public String random() {
        return values.get(random.nextInt(values.size()));
    }

    public List<String> first(int n) {
        return this.values.stream().limit(n).collect(Collectors.toList());
    }

}
