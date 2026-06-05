import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

public class CsvWriter {

    private final Path csvPath;

    public CsvWriter(String filePath) throws IOException {
        this.csvPath = Path.of(filePath);

        Path parent = csvPath.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        Files.writeString(
            csvPath,
            "arquivo,tamanho_bytes,total_palavras,palavra,algoritmo,threads,repeticao,ocorrencias,tempo_ms"
                + System.lineSeparator(),
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    public void write(BenchmarkResult result) throws IOException {
        String line = String.format(
            Locale.US,
            "%s,%d,%d,%s,%s,%d,%d,%d,%.6f",
            result.getFileName(),
            result.getFileSizeBytes(),
            result.getTotalWords(),
            result.getTargetWord(),
            result.getAlgorithm(),
            result.getThreadCount(),
            result.getRepetition(),
            result.getOccurrences(),
            result.getExecutionTimeMs()
        );

        try (BufferedWriter writer = Files.newBufferedWriter(
            csvPath,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        )) {
            writer.write(line);
            writer.newLine();
        }
    }
}