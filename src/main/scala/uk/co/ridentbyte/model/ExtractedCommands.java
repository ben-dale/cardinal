package uk.co.ridentbyte.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractedCommands {

    private String data;

    public ExtractedCommands(String data) {
        this.data = data;
    }

    public List<Command> all() {
        List<Command> matchedCommandStrings = new ArrayList<>();
        if (data != null) {
            Matcher matcher = Pattern.compile("(#\\{[^\\{|\\}]+\\})").matcher(data);
            while (matcher.find()) {
                matchedCommandStrings.add(new Command(matcher.group()));
            }
        }
        return matchedCommandStrings;
    }

}
