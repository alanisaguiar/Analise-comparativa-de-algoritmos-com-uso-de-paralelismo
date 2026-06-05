import java.nio.file.Files;
import java.nio.file.Path;

public class BenchmarkRunner {

    private static final String[] FILES = {
        "amostras/Dracula-165307.txt",
        "amostras/MobyDick-217452.txt",
        "amostras/DonQuixote-388208.txt"
    };

    private static final int[] THREAD_OPTIONS = {
        1, 2, 4, 8
    };

    private static final int WARMUP_EXECUTIONS = 2;
    private static final int VALID_EXECUTIONS = 5;

    private static final String TARGET_WORD = "the";

    public static void runBenchmark() throws Exception {
        CsvWriter csvWriter =
            new CsvWriter("results/resultados.csv");

        for (String filePath : FILES) {
            Path path = Path.of(filePath);

            String[] words =
                TextProcessor.readWords(filePath);

            String targetWord =
                TextProcessor.normalizeWord(TARGET_WORD);

            long fileSize = Files.size(path);

            System.out.println();
            System.out.println(
                "--------------------------------------------"
            );
            System.out.println("Arquivo: " + filePath);
            System.out.println(
                "Tamanho em bytes: " + fileSize
            );
            System.out.println(
                "Total de palavras: " + words.length
            );
            System.out.println(
                "Palavra pesquisada: " + targetWord
            );
            System.out.println(
                "--------------------------------------------"
            );

            warmUpCpu(words, targetWord);

            long serialCheck =
                SerialCPU.count(words, targetWord);

            executeSerialTests(
                path,
                fileSize,
                words,
                targetWord,
                csvWriter
            );

            executeParallelCpuTests(
                path,
                fileSize,
                words,
                targetWord,
                serialCheck,
                csvWriter
            );

            executeGpuTests(
                path,
                fileSize,
                words,
                targetWord,
                serialCheck,
                csvWriter
            );
        }

        System.out.println();
        System.out.println(
            "--------------------------------------------"
        );
        System.out.println("Benchmark concluído.");
        System.out.println(
            "Arquivo criado: results/resultados.csv"
        );
        System.out.println(
            "--------------------------------------------"
        );
    }

    private static void warmUpCpu(
        String[] words,
        String targetWord
    ) throws Exception {

        System.out.println();
        System.out.println("Aquecendo CPU...");

        int availableProcessors =
            Runtime.getRuntime().availableProcessors();

        int warmUpThreads =
            Math.min(4, availableProcessors);

        for (int execution = 0;
             execution < WARMUP_EXECUTIONS;
             execution++) {

            SerialCPU.count(
                words,
                targetWord
            );

            ParallelCPU.count(
                words,
                targetWord,
                warmUpThreads
            );
        }

        System.out.println(
            "Aquecimento da CPU concluído."
        );
    }

    private static void executeSerialTests(
        Path path,
        long fileSize,
        String[] words,
        String targetWord,
        CsvWriter csvWriter
    ) throws Exception {

        System.out.println();
        System.out.println("Executando SerialCPU...");

        for (int repetition = 1;
             repetition <= VALID_EXECUTIONS;
             repetition++) {

            long startTime =
                System.nanoTime();

            long occurrences =
                SerialCPU.count(
                    words,
                    targetWord
                );

            long endTime =
                System.nanoTime();

            double executionTimeMs =
                (endTime - startTime)
                    / 1_000_000.0;

            BenchmarkResult result =
                new BenchmarkResult(
                    path.getFileName().toString(),
                    fileSize,
                    words.length,
                    targetWord,
                    "SerialCPU",
                    1,
                    repetition,
                    occurrences,
                    executionTimeMs
                );

            csvWriter.write(result);

            System.out.printf(
                "SerialCPU repetição %d: "
                    + "%d ocorrências em %.3f ms%n",
                repetition,
                occurrences,
                executionTimeMs
            );
        }
    }

    private static void executeParallelCpuTests(
        Path path,
        long fileSize,
        String[] words,
        String targetWord,
        long serialCheck,
        CsvWriter csvWriter
    ) throws Exception {

        System.out.println();
        System.out.println("Executando ParallelCPU...");

        int availableProcessors =
            Runtime.getRuntime().availableProcessors();

        System.out.println(
            "Processadores lógicos disponíveis: "
                + availableProcessors
        );

        for (int numberOfThreads : THREAD_OPTIONS) {
            if (numberOfThreads > availableProcessors) {
                System.out.println(
                    "Teste com "
                        + numberOfThreads
                        + " threads mantido para análise, "
                        + "mesmo ultrapassando os processadores disponíveis."
                );
            }

            for (int repetition = 1;
                 repetition <= VALID_EXECUTIONS;
                 repetition++) {

                long startTime =
                    System.nanoTime();

                long occurrences =
                    ParallelCPU.count(
                        words,
                        targetWord,
                        numberOfThreads
                    );

                long endTime =
                    System.nanoTime();

                double executionTimeMs =
                    (endTime - startTime)
                        / 1_000_000.0;

                validateOccurrences(
                    serialCheck,
                    occurrences,
                    "ParallelCPU com "
                        + numberOfThreads
                        + " threads"
                );

                BenchmarkResult result =
                    new BenchmarkResult(
                        path.getFileName().toString(),
                        fileSize,
                        words.length,
                        targetWord,
                        "ParallelCPU",
                        numberOfThreads,
                        repetition,
                        occurrences,
                        executionTimeMs
                    );

                csvWriter.write(result);

                System.out.printf(
                    "ParallelCPU %d threads, repetição %d: "
                        + "%d ocorrências em %.3f ms%n",
                    numberOfThreads,
                    repetition,
                    occurrences,
                    executionTimeMs
                );
            }
        }
    }

    private static void executeGpuTests(
        Path path,
        long fileSize,
        String[] words,
        String targetWord,
        long serialCheck,
        CsvWriter csvWriter
    ) throws Exception {

        System.out.println();
        System.out.println("Aquecendo GPU...");

        ParallelGPU.count(
            words,
            targetWord
        );

        System.out.println(
            "Aquecimento da GPU concluído."
        );

        System.out.println();
        System.out.println("Executando ParallelGPU...");

        for (int repetition = 1;
             repetition <= VALID_EXECUTIONS;
             repetition++) {

            long startTime =
                System.nanoTime();

            long occurrences =
                ParallelGPU.count(
                    words,
                    targetWord
                );

            long endTime =
                System.nanoTime();

            double executionTimeMs =
                (endTime - startTime)
                    / 1_000_000.0;

            validateOccurrences(
                serialCheck,
                occurrences,
                "ParallelGPU"
            );

            BenchmarkResult result =
                new BenchmarkResult(
                    path.getFileName().toString(),
                    fileSize,
                    words.length,
                    targetWord,
                    "ParallelGPU",
                    0,
                    repetition,
                    occurrences,
                    executionTimeMs
                );

            csvWriter.write(result);

            System.out.printf(
                "ParallelGPU repetição %d: "
                    + "%d ocorrências em %.3f ms%n",
                repetition,
                occurrences,
                executionTimeMs
            );
        }
    }

    private static void validateOccurrences(
        long expectedOccurrences,
        long actualOccurrences,
        String algorithm
    ) {
        if (expectedOccurrences != actualOccurrences) {
            throw new IllegalStateException(
                "Contagem incorreta em "
                    + algorithm
                    + ". Esperado: "
                    + expectedOccurrences
                    + ", obtido: "
                    + actualOccurrences
            );
        }
    }
}