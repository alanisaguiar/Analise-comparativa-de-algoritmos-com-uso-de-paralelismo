import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class TextProcessor {

    public static String[] readWords(String filePath) throws IOException {
        String text = Files.readString(
            Path.of(filePath),
            StandardCharsets.UTF_8
        );

        text = text.toLowerCase();

        return text.split("[^\\p{L}\\p{N}']+");
    }

    public static String normalizeWord(String word) {
        return word.toLowerCase().trim();
    }
}