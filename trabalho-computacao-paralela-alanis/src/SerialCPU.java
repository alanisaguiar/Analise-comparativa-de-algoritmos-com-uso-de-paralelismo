public class SerialCPU {

    public static long count(String[] words, String targetWord) {
        long occurrences = 0;

        for (String word : words) {
            if (word.equals(targetWord)) {
                occurrences++;
            }
        }

        return occurrences;
    }
}