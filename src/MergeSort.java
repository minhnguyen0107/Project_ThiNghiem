package src;

public class MergeSort {
    public static void sort(int[] a, int cutoff) {
        int[] aux = new int[a.length];
        sort(a, aux, 0, a.length - 1, cutoff);
    }

    public static void sort(int[] a, int[] aux, int cutoff) {
        sort(a, aux, 0, a.length - 1, cutoff);
    }

    private static void sort(int[] a, int[] aux, int lo, int hi, int cutoff) {
        if (hi - lo + 1 <= cutoff) {
            insertionSort(a, lo, hi);
            return;
        }
        if (hi <= lo) return;
        int mid = lo + (hi - lo) / 2;
        sort(a, aux, lo, mid, cutoff);
        sort(a, aux, mid + 1, hi, cutoff);
        merge(a, aux, lo, mid, hi);
    }

    private static void merge(int[] a, int[] aux, int lo, int mid, int hi) {
        for (int k = lo; k <= hi; k++) {
            aux[k] = a[k];
        }
        int i = lo, j = mid + 1;
        for (int k = lo; k <= hi; k++) {
            if (i > mid)              a[k] = aux[j++];
            else if (j > hi)          a[k] = aux[i++];
            else if (aux[j] < aux[i]) a[k] = aux[j++];
            else                      a[k] = aux[i++];
        }
    }

    static void insertionSort(int[] a, int lo, int hi) {
        for (int i = lo + 1; i <= hi; i++) {
            int v = a[i];
            int j = i;
            while (j > lo && v < a[j - 1]) {
                a[j] = a[j - 1];
                j--;
            }
            a[j] = v;
        }
    }

    public static boolean isSorted(int[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i] < a[i - 1]) return false;
        }
        return true;
    }
    
}
