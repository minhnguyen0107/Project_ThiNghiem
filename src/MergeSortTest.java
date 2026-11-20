package src;

import java.util.Arrays;
import java.util.Random;

public class MergeSortTest {
    public static void main(String[] args) {
        Random rnd = new Random(1);
        int[] cutoffs = {0, 1, 2, 3, 7, 10, 16, 50, 1000};
        int checks = 0;
        for (int cutoff : cutoffs) {
            for (int n = 0; n <= 300; n++) {
                check(randomArray(rnd, n, Integer.MAX_VALUE), cutoff);
                check(randomArray(rnd, n, 5), cutoff);
                int[] asc = new int[n], desc = new int[n];
                for (int i = 0; i < n; i++) { asc[i] = i; desc[i] = n - i; }
                check(asc, cutoff);
                check(desc, cutoff);
                checks += 4;
            }
            check(randomArray(rnd, 100_000, Integer.MAX_VALUE), cutoff);
            checks++;
        }
        System.out.println("OK: " + checks + " test passed.");
    }

    private static int[] randomArray(Random rnd, int n, int bound) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = rnd.nextInt(bound);
        return a;
    }

    private static void check(int[] a, int cutoff) {
        int[] expected = a.clone();
        Arrays.sort(expected); 
        MergeSort.sort(a, cutoff);
        if (!Arrays.equals(a, expected)) {
            throw new AssertionError("Sai kết quả: n=" + a.length + ", cutoff=" + cutoff);
        }
    }
}

