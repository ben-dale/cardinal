package uk.co.ridentbyte.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ExtractedCommandsTest {

    @Test
    public void shouldReturnASingleCommand() {
        // Given
        ExtractedCommands testSubject = new ExtractedCommands("hello #{world}");

        // When
        List<Command> result = testSubject.all();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCommand()).isEqualTo("world");
    }

    @Test
    public void shouldReturnTwoCommands() {
        // Given
        ExtractedCommands testSubject = new ExtractedCommands("#{hello} world #{sailor}");

        // When
        List<Command> result = testSubject.all();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCommand()).isEqualTo("hello");
        assertThat(result.get(1).getCommand()).isEqualTo("sailor");
    }

    @Test
    public void shouldReturnAnEmptyList() {
        // Given
        ExtractedCommands testSubject = new ExtractedCommands("this shouldn't match anything");

        // When
        List<Command> result = testSubject.all();

        // Then
        assertThat(result).hasSize(0);
    }

    @Test
    public void shouldHandleNullData() {
        // Given
        ExtractedCommands testSubject = new ExtractedCommands(null);

        // When
        List<Command> result = testSubject.all();

        // Then
        assertThat(result).hasSize(0);
    }
}
