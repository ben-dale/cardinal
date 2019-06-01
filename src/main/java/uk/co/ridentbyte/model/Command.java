package uk.co.ridentbyte.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


// TODO
// - This class doesn't handle the "#{ }" container anymore so this needs to be
//   moved up to the calling class
// - This class needs to detect the different types the different functions take
//   so it can act differently based on argument types.
//   class ArgumentType(value: String), case class Function(value: String), case class Constant(value: String)
public class Command {

    private String command;

    private Pattern lowerFunction = Pattern.compile("(?:lower\\((.*)\\))");
    private Pattern upperFunction = Pattern.compile("(?:upper\\((.*)\\))");
    private Pattern capitaliseFunction = Pattern.compile("(?:capitalise\\((.*)\\))");
    private Pattern randomFunction = Pattern.compile("(?:random\\((.*)\\))");
    private Pattern loremFunction = Pattern.compile("(?:lorem\\((.*)\\))");
    private Pattern randomBetweenFunction = Pattern.compile("(?:randomBetween\\((.*)\\))");
    private Pattern rawString = Pattern.compile("(?:^\"(.*)\"$)");

    public Command(String command) {
        this.command = command;
    }

    public String process(Map<String, Supplier<String>> variables, Vocabulary vocabulary) {
        return process(variables, vocabulary, command);
    }

    private String process(Map<String, Supplier<String>> variables, Vocabulary vocabulary, String stackedCommand) {
        if (variables.containsKey(stackedCommand)) {
            String value = variables.get(stackedCommand).get();
            return process(variables, vocabulary, value);
        } else if (stackedCommand.matches(lowerFunction.pattern())) {
            Matcher matcher = lowerFunction.matcher(stackedCommand);
            matcher.find();
            return process(variables, vocabulary, matcher.group(1)).toLowerCase();
        } else if (stackedCommand.matches(upperFunction.pattern())) {
            Matcher matcher = upperFunction.matcher(stackedCommand);
            matcher.find();
            return process(variables, vocabulary, matcher.group(1)).toUpperCase();
        } else if (stackedCommand.matches(capitaliseFunction.pattern())) {
            Matcher matcher = capitaliseFunction.matcher(stackedCommand);
            matcher.find();
            return capitalise(process(variables, vocabulary, matcher.group(1)));
        } else if (stackedCommand.matches(rawString.pattern())) {
            Matcher matcher = rawString.matcher(stackedCommand);
            matcher.find();
            return process(variables, vocabulary, matcher.group(1));
        } else if (stackedCommand.matches(randomFunction.pattern())) {
            Matcher matcher = randomFunction.matcher(stackedCommand);
            matcher.find();
            List<String> processedValues =
                    Arrays.stream(matcher.group(1).split(","))
                            .map(v -> process(variables, vocabulary, v.trim()))
                            .collect(Collectors.toList());
            return random(processedValues);
        } else if (stackedCommand.matches(loremFunction.pattern())) {
            Matcher matcher = loremFunction.matcher(stackedCommand);
            matcher.find();
            String processedArguments = process(variables, vocabulary, matcher.group(1));
            if (processedArguments.matches("[0-9]+")) {
                return lorem(Integer.parseInt(processedArguments), vocabulary.loremIpsum);
            } else {
                return command;
            }
        } else if (stackedCommand.matches(randomBetweenFunction.pattern())) {
            Matcher matcher = randomBetweenFunction.matcher(stackedCommand);
            matcher.find();
            String[] splitArgs = matcher.group(1).split(",");
            if (splitArgs.length == 2) {
                List<String> processedArgs = Arrays.stream(splitArgs)
                        .map((arg) -> process(variables, vocabulary, arg.trim()))
                        .collect(Collectors.toList());
                if (processedArgs.get(0).matches("[0-9]+") && processedArgs.get(1).matches("[0-9]+")) {
                    int min = Math.min(Integer.parseInt(processedArgs.get(0)), Integer.parseInt(processedArgs.get(1)));
                    int max = Math.max(Integer.parseInt(processedArgs.get(0)), Integer.parseInt(processedArgs.get(1)));
                    return Integer.toString(ThreadLocalRandom.current().nextInt(min, max + 1));
                } else {
                    return command;
                }
            } else {
                return command;
            }
        } else {
            return stackedCommand;
        }
    }

    private String capitalise(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    private String random(List<String> values) {
        return values.get(new Random().nextInt(values.size()));
    }

    private String lorem(int i, Words loremIpsum) {
        return String.join(" ", loremIpsum.first(i));
    }

    public String getCommand() {
        return command;
    }
}
