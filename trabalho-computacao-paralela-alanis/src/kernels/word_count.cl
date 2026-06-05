__kernel void count_words(
    __global const int *word_hashes,
    const int target_hash,
    __global int *results,
    const int total_words
) {
    int id = get_global_id(0);

    if (id < total_words) {
        results[id] =
            word_hashes[id] == target_hash ? 1 : 0;
    }
}