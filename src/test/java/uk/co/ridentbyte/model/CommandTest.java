package uk.co.ridentbyte.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public class CommandTest {

    private Vocabulary vocabulary;

    @BeforeEach
    public void setup() {
        List<String> loremIpsum = new ArrayList<>();
        loremIpsum.add("lorem");
        loremIpsum.add("ipsum");
        vocabulary = new Vocabulary(
                new Words(new ArrayList<>(), new Random()),
                new Words(new ArrayList<>(), new Random()),
                new Words(new ArrayList<>(), new Random()),
                new Words(new ArrayList<>(), new Random()),
                new Words(new ArrayList<>(), new Random()),
                new Words(new ArrayList<>(), new Random()),
                new Words(new ArrayList<>(), new Random()),
                new Words(new ArrayList<>(), new Random()),
                new Words(loremIpsum, new Random()),
                new Words(new ArrayList<>(), new Random())
        );
    }

    @Test
    public void shouldProcessVariableWithoutAValue() {
        // Given
        String command = "hello";
        Map<String, Supplier<String>> variables = new HashMap<>();
        Command testSubject = new Command(command);

        // When
        String result = testSubject.process(variables, vocabulary);

        // Then
        assertEquals("hello", result);
    }

    @Test
    public void shouldProcessVariableWithAValue() {
        // Given
        String command = "hello";
        Map<String, Supplier<String>> variables = new HashMap<>();
        variables.put("hello", () -> "world");
        Command testSubject = new Command(command);

        // When
        String result = testSubject.process(variables, vocabulary);

        // Then
        assertEquals("world", result);
    }

    @Test
    public void shouldProcessString() {
        // Given
        String command = "\"hello\"";
        Map<String, Supplier<String>> variables = new HashMap<>();
        Command testSubject = new Command(command);

        // When
        String result = testSubject.process(variables, vocabulary);

        // Then
        assertEquals("hello", result);
    }

    @Test
    public void shouldProcessLowerFunction() {
        // Given
        String command = "lower(\"HELLO\")";
        Map<String, Supplier<String>> variables = new HashMap<>();
        Command testSubject = new Command(command);

        // When
        String result = testSubject.process(variables, vocabulary);

        // Then
        assertEquals("hello", result);
    }

    @Test
    public void shouldProcessUpperFunction() {
        // Given
        String command = "upper(\"hello\")";
        Map<String, Supplier<String>> variables = new HashMap<>();
        Command testSubject = new Command(command);

        // When
        String result = testSubject.process(variables, vocabulary);

        // Then
        assertEquals("HELLO", result);
    }

    @Test
    public void shouldProcessLowerUpperFunction() {
        // Given
        String command = "lower(upper(\"hello\"))";
        Map<String, Supplier<String>> variables = new HashMap<>();
        Command testSubject = new Command(command);

        // When
        String result = testSubject.process(variables, vocabulary);

        // Then
        assertEquals("hello", result);
    }

    @Test
    public void shouldProcessVariableWithAValueInUpperFunction() {
        // Given
        String command = "upper(hello)";
        Map<String, Supplier<String>> variables = new HashMap<>();
        variables.put("hello", () -> "world");
        Command testSubject = new Command(command);

        // When
        String result = testSubject.process(variables, vocabulary);

        // Then
        assertEquals("WORLD", result);
    }

    @Test
    public void shouldProcessCapitaliseFunctionWithRawString() {
        // Given
        String command = "capitalise(\"hello\")";
        Map<String, Supplier<String>> variables = new HashMap<>();
        Command testSubject = new Command(command);

        // When
        String result = testSubject.process(variables, vocabulary);

        // Then
        assertEquals("Hello", result);
    }

    @Test
    public void shouldProcessCapitaliseFunctionWithVariable() {
        // Given
        String command = "capitalise(hello)";
        Map<String, Supplier<String>> variables = new HashMap<>();
        variables.put("hello", () -> "world");
        Command testSubject = new Command(command);

        // When
        String result = testSubject.process(variables, vocabulary);

        // Then
        assertEquals("World", result);
    }

    @Disabled
    @Test
    public void shouldProcessCapitaliseFunctionWithVariableThatHasQuotedValue() {
        // Given
        String command = "capitalise(hello)";
        Map<String, Supplier<String>> variables = new HashMap<>();
        variables.put("hello", () -> "\"world\"");
        Command testSubject = new Command(command);

        // When
        String result = testSubject.process(variables, vocabulary);

        // Then
        assertEquals("\"world\"", result);
    }

    @Test
    public void shouldProcessRandomFunctionWithSingleRawString() {
        // Given
        String command = "random(\"hello\")";
        Map<String, Supplier<String>> variables = new HashMap<>();
        Command testSubject = new Command(command);

        // When
        String result = testSubject.process(variables, vocabulary);

        // Then
        assertEquals("hello", result);
    }

    @Test
    public void shouldProcessRandomFunctionWithTwoRawStrings() {
        // Given
        String command = "random(\"hello\", \"world\")";
        Map<String, Supplier<String>> variables = new HashMap<>();
        Command testSubject = new Command(command);

        // When
        String result = testSubject.process(variables, vocabulary);

        // Then
        assertTrue(result.equals("world") || result.equals("hello"));
    }

    @Test
    public void shouldProcessRandomFunctionWithTwoVariables() {
        // Given
        String command = "random(abc, 123)";
        Map<String, Supplier<String>> variables = new HashMap<>();
        variables.put("abc", () -> "test1");
        variables.put("123", () -> "test2");
        Command testSubject = new Command(command);

        // When
        String result = testSubject.process(variables, vocabulary);

        // Then
        assertTrue(result.equals("test1") || result.equals("test2"));
    }

    @Test
    public void shouldProcessRandomFunctionWithVariableAndRawValue() {
        // Given
        String command = "random(\"abc\", 123)";
        Map<String, Supplier<String>> variables = new HashMap<>();
        variables.put("123", () -> "test2");
        Command testSubject = new Command(command);

        // When
        String result = testSubject.process(variables, vocabulary);

        // Then
        assertTrue(result.equals("abc") || result.equals("test2"));
    }

    @Test
    public void shouldProcessRandomFunctionWithUpperFunctions() {
        // Given
        String command = "random(upper(\"abc\"), upper(\"123\"))";
        Map<String, Supplier<String>> variables = new HashMap<>();
        Command testSubject = new Command(command);

        // When
        String result = testSubject.process(variables, vocabulary);

        // Then
        assertTrue(result.equals("ABC") || result.equals("123"));
    }

    @Disabled
    @Test
    public void shouldProcessRandomFunctionWithNestedRandomFunctions() {
        // Given
        String command = "random(random(\"a\", \"b\"), random(\"d\", \"e\"))";
        Map<String, Supplier<String>> variables = new HashMap<>();
        Command testSubject = new Command(command);

        // When
        String result = testSubject.process(variables, vocabulary);

        // Then
        assertTrue(result.equals("a") || result.equals("b") || result.equals("d") || result.equals("e"));
    }

    @Test
    public void shouldProcessLoremFunction() {
        // Given
        String command = "lorem(1)";
        Map<String, Supplier<String>> variables = new HashMap<>();
        Command testSubject = new Command(command);

        // When
        String result = testSubject.process(variables, vocabulary);

        // Then
        assertEquals("lorem", result);
    }

    @Test
    public void shouldProcessLoremWithTwoFunction() {
        // Given
        String command = "lorem(2)";
        Map<String, Supplier<String>> variables = new HashMap<>();
        Command testSubject = new Command(command);

        // When
        String result = testSubject.process(variables, vocabulary);

        // Then
        assertEquals("lorem ipsum", result);
    }

    @Test
    public void shouldProcessRandomBetweenTwoDifferentNumbersInOrder() {
        // Given
        String command = "randomBetween(2, 3)";
        Map<String, Supplier<String>> variables = new HashMap<>();
        Command testSubject = new Command(command);

        // When
        String result = testSubject.process(variables, vocabulary);

        // Then
        assertTrue(result.matches("[2|3]"));
    }

    @Test
    public void shouldProcessRandomBetweenTwoDifferentNumbersOutOfOrder() {
        // Given
        String command = "randomBetween(5, 4)";
        Map<String, Supplier<String>> variables = new HashMap<>();
        Command testSubject = new Command(command);

        // When
        String result = testSubject.process(variables, vocabulary);

        // Then
        assertTrue(result.matches("[4|5]"));
    }

    @Test
    public void shouldProcessRandomBetweenTwoOfTheSameNumber() {
        // Given
        String command = "randomBetween(5, 5)";
        Map<String, Supplier<String>> variables = new HashMap<>();
        Command testSubject = new Command(command);

        // When
        String result = testSubject.process(variables, vocabulary);

        // Then
        assertEquals("5", result);
    }

    @Test
    public void shouldProcessRandomBetweenAndReturnCommandWhenInvalidValue() {
        // Given
        String command = "randomBetween(a, 5)";
        Map<String, Supplier<String>> variables = new HashMap<>();
        Command testSubject = new Command(command);

        // When
        String result = testSubject.process(variables, vocabulary);

        // Then
        assertEquals("randomBetween(a, 5)", result);
    }

}
