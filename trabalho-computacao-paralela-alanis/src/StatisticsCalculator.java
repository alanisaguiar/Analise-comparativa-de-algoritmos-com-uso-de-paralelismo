import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsCalculator {

    public static void generateSummary(
        String inputCsv,
        String outputCsv
    ) throws IOException {

        List<String> lines = Files.readAllLines(
            Path.of(inputCsv),
            StandardCharsets.UTF_8
        );

        Map<String, List<Double>> groupedTimes =
            new LinkedHashMap<>();

        Map<String, Long> occurrencesByGroup =
            new LinkedHashMap<>();

        for (int index = 1; index < lines.size(); index++) {
            String line = lines.get(index).trim();

            if (line.isEmpty()) {
                continue;
            }

            String[] columns = line.split(",");

            String fileName = columns[0];
            String algorithm = columns[4];
            int threads = Integer.parseInt(columns[5]);
            long occurrences = Long.parseLong(columns[7]);
            double timeMs = Double.parseDouble(columns[8]);

            String key =
                fileName + ";" + algorithm + ";" + threads;

            groupedTimes
                .computeIfAbsent(key, ignored -> new ArrayList<>())
                .add(timeMs);

            occurrencesByGroup.put(key, occurrences);
        }

        Map<String, Double> serialAverages =
            calculateSerialAverages(groupedTimes);

        List<String> outputLines = new ArrayList<>();

        outputLines.add(
            "arquivo,algoritmo,threads,ocorrencias,"
                + "media_ms,mediana_ms,minimo_ms,maximo_ms,"
                + "desvio_padrao,speedup,eficiencia"
        );

        for (Map.Entry<String, List<Double>> entry
            : groupedTimes.entrySet()) {

            String key = entry.getKey();
            List<Double> times = entry.getValue();

            String[] keyParts = key.split(";");

            String fileName = keyParts[0];
            String algorithm = keyParts[1];
            int threads = Integer.parseInt(keyParts[2]);

            double average = calculateAverage(times);
            double median = calculateMedian(times);
            double minimum = Collections.min(times);
            double maximum = Collections.max(times);
            double standardDeviation =
                calculateStandardDeviation(times, average);

            double serialAverage =
                serialAverages.getOrDefault(fileName, average);

            double speedup = serialAverage / average;

            double efficiency;

            if (algorithm.equals("ParallelCPU")) {
                efficiency = speedup / threads;
            } else {
                efficiency = 1.0;
            }

            long occurrences =
                occurrencesByGroup.getOrDefault(key, 0L);

            String outputLine = String.format(
                Locale.US,
                "%s,%s,%d,%d,%.6f,%.6f,%.6f,%.6f,"
                    + "%.6f,%.6f,%.6f",
                fileName,
                algorithm,
                threads,
                occurrences,
                average,
                median,
                minimum,
                maximum,
                standardDeviation,
                speedup,
                efficiency
            );

            outputLines.add(outputLine);
        }

        Path outputPath = Path.of(outputCsv);

        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }

        Files.write(
            outputPath,
            outputLines,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        );

        System.out.println(
            "Resumo estatístico criado: " + outputCsv
        );
    }

    private static Map<String, Double> calculateSerialAverages(
        Map<String, List<Double>> groupedTimes
    ) {
        Map<String, Double> serialAverages =
            new LinkedHashMap<>();

        for (Map.Entry<String, List<Double>> entry
            : groupedTimes.entrySet()) {

            String[] keyParts = entry.getKey().split(";");

            String fileName = keyParts[0];
            String algorithm = keyParts[1];

            if (algorithm.equals("SerialCPU")) {
                double average =
                    calculateAverage(entry.getValue());

                serialAverages.put(fileName, average);
            }
        }

        return serialAverages;
    }

    private static double calculateAverage(
        List<Double> values
    ) {
        double sum = 0.0;

        for (double value : values) {
            sum += value;
        }

        return sum / values.size();
    }

    private static double calculateMedian(
        List<Double> values
    ) {
        List<Double> sortedValues =
            new ArrayList<>(values);

        Collections.sort(sortedValues);

        int size = sortedValues.size();

        if (size % 2 == 0) {
            return (
                sortedValues.get(size / 2 - 1)
                    + sortedValues.get(size / 2)
            ) / 2.0;
        }

        return sortedValues.get(size / 2);
    }

    private static double calculateStandardDeviation(
        List<Double> values,
        double average
    ) {
        double sum = 0.0;

        for (double value : values) {
            double difference = value - average;
            sum += difference * difference;
        }

        return Math.sqrt(sum / values.size());
    }
}