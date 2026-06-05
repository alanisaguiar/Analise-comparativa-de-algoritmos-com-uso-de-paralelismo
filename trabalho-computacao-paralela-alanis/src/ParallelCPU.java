import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ParallelCPU {

    public static long count(
        String[] words,
        String targetWord,
        int numberOfThreads
    ) throws Exception {

        if (numberOfThreads <= 0) {
            throw new IllegalArgumentException(
                "O número de threads deve ser maior que zero."
            );
        }

        ExecutorService executor =
            Executors.newFixedThreadPool(numberOfThreads);

        List<Future<Long>> futures = new ArrayList<>();

        int totalWords = words.length;

        int blockSize =
            (int) Math.ceil(
                (double) totalWords / numberOfThreads
            );

        for (
            int threadIndex = 0;
            threadIndex < numberOfThreads;
            threadIndex++
        ) {
            int start = threadIndex * blockSize;
            int end = Math.min(start + blockSize, totalWords);

            if (start >= totalWords) {
                break;
            }

            Callable<Long> task = () -> {
                long partialCount = 0;

                for (int index = start; index < end; index++) {
                    if (words[index].equals(targetWord)) {
                        partialCount++;
                    }
                }

                return partialCount;
            };

            futures.add(executor.submit(task));
        }

        long totalOccurrences = 0;

        for (Future<Long> future : futures) {
            totalOccurrences += future.get();
        }

        executor.shutdown();

        return totalOccurrences;
    }
}