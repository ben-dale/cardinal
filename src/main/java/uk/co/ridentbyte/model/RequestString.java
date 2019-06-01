package uk.co.ridentbyte.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class RequestString {

    private String content;
    private List<EnvironmentVariable> environmentVars;
    private Vocabulary vocabulary;

    public RequestString(String content, List<EnvironmentVariable> environmentVars, Vocabulary vocabulary) {
        this.content = content;
        this.environmentVars = environmentVars;
        this.vocabulary = vocabulary;
    }

    public String process() {
        String contentCopy = content;
        List<Command> extractedCommands = new ExtractedCommands(content).all();

        String guid = UUID.randomUUID().toString().split("-")[0];
        String integer = String.valueOf(Math.abs(new Random().nextInt()));
        String flt = String.valueOf(Math.abs(new Random().nextFloat()));
        String firstName = vocabulary.firstNames.random();
        String lastName = vocabulary.lastNames.random();
        String action = vocabulary.actions.random();
        String businessEntity = vocabulary.businessEntities.random();
        String communication = vocabulary.communications.random();
        String country = vocabulary.countries.random();
        String object = vocabulary.objects.random();
        String place = vocabulary.places.random();
        String emoji = vocabulary.emoji.random();

        Map<String, Supplier<String>> variables = new HashMap<>();

        variables.put("guid", () -> guid);
        variables.put("int", () -> integer);
        variables.put("float", () -> flt);
        variables.put("firstName", () -> firstName);
        variables.put("lastName", () -> lastName);
        variables.put("action", () -> action);
        variables.put("businessEntity", () -> businessEntity);
        variables.put("communication", () -> communication);
        variables.put("country", () -> country);
        variables.put("object", () -> object);
        variables.put("place", () -> place);
        variables.put("emoji", () -> emoji);
        variables.put("randomGuid()", () -> UUID.randomUUID().toString().split("-")[0]);
        variables.put("randomInt()", () -> String.valueOf(Math.abs(new Random().nextInt())));
        variables.put("randomFloat()", () -> String.valueOf(Math.abs(new Random().nextFloat())));
        variables.put("randomFirstName()", () -> vocabulary.firstNames.random());
        variables.put("randomLastName()", () -> vocabulary.lastNames.random());
        variables.put("randomAction()", () -> vocabulary.places.random());
        variables.put("randomBusinessEntity()", () -> vocabulary.businessEntities.random());
        variables.put("randomCommunication()", () -> vocabulary.communications.random());
        variables.put("randomCountry()", () -> vocabulary.countries.random());
        variables.put("randomObject()", () -> vocabulary.objects.random());
        variables.put("randomPlace()", () -> vocabulary.places.random());
        variables.put("randomEmoji()", () -> vocabulary.emoji.random());

        for (EnvironmentVariable envVar : environmentVars) {
            variables.put(envVar.getKey(), envVar::getValue);
        }

        for (Command command : extractedCommands) {
            contentCopy = contentCopy.replaceFirst(
                    Pattern.quote("#{" + command.getCommand() + "}"), command.process(variables, vocabulary)
            );
        }

        return contentCopy;
    }
}
