public class BenchmarkResult {

    private final String fileName;
    private final long fileSizeBytes;
    private final int totalWords;
    private final String targetWord;
    private final String algorithm;
    private final int threadCount;
    private final int repetition;
    private final long occurrences;
    private final double executionTimeMs;

    public BenchmarkResult(
        String fileName,
        long fileSizeBytes,
        int totalWords,
        String targetWord,
        String algorithm,
        int threadCount,
        int repetition,
        long occurrences,
        double executionTimeMs
    ) {
        this.fileName = fileName;
        this.fileSizeBytes = fileSizeBytes;
        this.totalWords = totalWords;
        this.targetWord = targetWord;
        this.algorithm = algorithm;
        this.threadCount = threadCount;
        this.repetition = repetition;
        this.occurrences = occurrences;
        this.executionTimeMs = executionTimeMs;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public int getTotalWords() {
        return totalWords;
    }

    public String getTargetWord() {
        return targetWord;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public int getRepetition() {
        return repetition;
    }

    public long getOccurrences() {
        return occurrences;
    }

    public double getExecutionTimeMs() {
        return executionTimeMs;
    }
}