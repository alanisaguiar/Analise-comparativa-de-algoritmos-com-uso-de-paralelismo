import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;

public class ChartGenerator {

    public static void generateAllCharts(
        String summaryCsv,
        String chartsDirectory
    ) throws Exception {

        Files.createDirectories(Path.of(chartsDirectory));

        List<SummaryRow> rows = readSummary(summaryCsv);

        generateMethodComparison(
            rows,
            chartsDirectory + "/comparacao_metodos"
        );

        generateThreadComparison(
            rows,
            chartsDirectory + "/comparacao_threads"
        );

        generateSpeedupChart(
            rows,
            chartsDirectory + "/speedup"
        );

        System.out.println(
            "Gráficos criados na pasta: " + chartsDirectory
        );
    }

    private static List<SummaryRow> readSummary(
        String summaryCsv
    ) throws Exception {

        List<String> lines = Files.readAllLines(
            Path.of(summaryCsv),
            StandardCharsets.UTF_8
        );

        List<SummaryRow> rows = new ArrayList<>();

        for (int index = 1; index < lines.size(); index++) {
            String line = lines.get(index).trim();

            if (line.isEmpty()) {
                continue;
            }

            String[] columns = line.split(",");

            SummaryRow row = new SummaryRow(
                columns[0],
                columns[1],
                Integer.parseInt(columns[2]),
                Long.parseLong(columns[3]),
                Double.parseDouble(columns[4]),
                Double.parseDouble(columns[5]),
                Double.parseDouble(columns[6]),
                Double.parseDouble(columns[7]),
                Double.parseDouble(columns[8]),
                Double.parseDouble(columns[9]),
                Double.parseDouble(columns[10])
            );

            rows.add(row);
        }

        return rows;
    }

    private static void generateMethodComparison(
        List<SummaryRow> rows,
        String outputPath
    ) throws Exception {

        CategoryChart chart =
            new CategoryChartBuilder()
                .width(1200)
                .height(700)
                .title("Comparacao entre os metodos")
                .xAxisTitle("Arquivo")
                .yAxisTitle("Tempo medio em ms")
                .build();

        chart.getStyler().setLegendPosition(
            Styler.LegendPosition.InsideNW
        );

        chart.getStyler().setAvailableSpaceFill(0.85);
        chart.getStyler().setOverlapped(false);

        List<String> fileNames = getFileNames(rows);

        List<Double> serialValues = new ArrayList<>();
        List<Double> cpuValues = new ArrayList<>();
        List<Double> gpuValues = new ArrayList<>();

        for (String fileName : fileNames) {
            serialValues.add(
                findAverage(
                    rows,
                    fileName,
                    "SerialCPU",
                    1
                )
            );

            cpuValues.add(
                findBestParallelCpuAverage(
                    rows,
                    fileName
                )
            );

            gpuValues.add(
                findAverage(
                    rows,
                    fileName,
                    "ParallelGPU",
                    0
                )
            );
        }

        chart.addSeries(
            "SerialCPU",
            simplifyFileNames(fileNames),
            serialValues
        );

        chart.addSeries(
            "Melhor ParallelCPU",
            simplifyFileNames(fileNames),
            cpuValues
        );

        chart.addSeries(
            "ParallelGPU",
            simplifyFileNames(fileNames),
            gpuValues
        );

        BitmapEncoder.saveBitmap(
            chart,
            outputPath,
            BitmapEncoder.BitmapFormat.PNG
        );
    }

    private static void generateThreadComparison(
        List<SummaryRow> rows,
        String outputPath
    ) throws Exception {

        XYChart chart =
            new XYChartBuilder()
                .width(1200)
                .height(700)
                .title("Impacto da quantidade de threads")
                .xAxisTitle("Numero de threads")
                .yAxisTitle("Tempo medio em ms")
                .build();

        chart.getStyler().setLegendPosition(
            Styler.LegendPosition.InsideNE
        );

        chart.getStyler().setMarkerSize(8);

        List<String> fileNames = getFileNames(rows);

        for (String fileName : fileNames) {
            List<Integer> threads = new ArrayList<>();
            List<Double> times = new ArrayList<>();

            for (SummaryRow row : rows) {
                if (
                    row.fileName.equals(fileName)
                        && row.algorithm.equals("ParallelCPU")
                ) {
                    threads.add(row.threads);
                    times.add(row.averageMs);
                }
            }

            chart.addSeries(
                simplifyFileName(fileName),
                threads,
                times
            );
        }

        BitmapEncoder.saveBitmap(
            chart,
            outputPath,
            BitmapEncoder.BitmapFormat.PNG
        );
    }

    private static void generateSpeedupChart(
        List<SummaryRow> rows,
        String outputPath
    ) throws Exception {

        XYChart chart =
            new XYChartBuilder()
                .width(1200)
                .height(700)
                .title("Speedup da CPU paralela")
                .xAxisTitle("Número de threads")
                .yAxisTitle("Speedup")
                .build();

        chart.getStyler().setLegendPosition(
            Styler.LegendPosition.InsideNE
        );

        chart.getStyler().setMarkerSize(8);

        List<String> fileNames = getFileNames(rows);

        for (String fileName : fileNames) {
            List<Integer> threads = new ArrayList<>();
            List<Double> speedups = new ArrayList<>();

            for (SummaryRow row : rows) {
                if (
                    row.fileName.equals(fileName)
                        && row.algorithm.equals("ParallelCPU")
                ) {
                    threads.add(row.threads);
                    speedups.add(row.speedup);
                }
            }

            chart.addSeries(
                simplifyFileName(fileName),
                threads,
                speedups
            );
        }

        BitmapEncoder.saveBitmap(
            chart,
            outputPath,
            BitmapEncoder.BitmapFormat.PNG
        );
    }

    private static List<String> getFileNames(
        List<SummaryRow> rows
    ) {
        Set<String> names = new LinkedHashSet<>();

        for (SummaryRow row : rows) {
            names.add(row.fileName);
        }

        return new ArrayList<>(names);
    }

    private static List<String> simplifyFileNames(
        List<String> fileNames
    ) {
        List<String> simplified = new ArrayList<>();

        for (String fileName : fileNames) {
            simplified.add(
                simplifyFileName(fileName)
            );
        }

        return simplified;
    }

    private static String simplifyFileName(
        String fileName
    ) {
        return fileName
            .replace(".txt", "")
            .replaceAll("-\\d+$", "");
    }

    private static double findAverage(
        List<SummaryRow> rows,
        String fileName,
        String algorithm,
        int threads
    ) {
        for (SummaryRow row : rows) {
            if (
                row.fileName.equals(fileName)
                    && row.algorithm.equals(algorithm)
                    && row.threads == threads
            ) {
                return row.averageMs;
            }
        }

        throw new IllegalStateException(
            "Resultado não encontrado para "
                + fileName
                + ", algoritmo "
                + algorithm
                + ", threads "
                + threads
        );
    }

    private static double findBestParallelCpuAverage(
        List<SummaryRow> rows,
        String fileName
    ) {
        double bestAverage = Double.MAX_VALUE;

        for (SummaryRow row : rows) {
            if (
                row.fileName.equals(fileName)
                    && row.algorithm.equals("ParallelCPU")
                    && row.averageMs < bestAverage
            ) {
                bestAverage = row.averageMs;
            }
        }

        if (bestAverage == Double.MAX_VALUE) {
            throw new IllegalStateException(
                "Nenhum resultado ParallelCPU encontrado para "
                    + fileName
            );
        }

        return bestAverage;
    }

    private static class SummaryRow {

        private final String fileName;
        private final String algorithm;
        private final int threads;
        private final long occurrences;
        private final double averageMs;
        private final double medianMs;
        private final double minimumMs;
        private final double maximumMs;
        private final double standardDeviation;
        private final double speedup;
        private final double efficiency;

        private SummaryRow(
            String fileName,
            String algorithm,
            int threads,
            long occurrences,
            double averageMs,
            double medianMs,
            double minimumMs,
            double maximumMs,
            double standardDeviation,
            double speedup,
            double efficiency
        ) {
            this.fileName = fileName;
            this.algorithm = algorithm;
            this.threads = threads;
            this.occurrences = occurrences;
            this.averageMs = averageMs;
            this.medianMs = medianMs;
            this.minimumMs = minimumMs;
            this.maximumMs = maximumMs;
            this.standardDeviation = standardDeviation;
            this.speedup = speedup;
            this.efficiency = efficiency;
        }
    }
}