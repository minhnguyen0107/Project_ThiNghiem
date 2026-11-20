package src;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Experiment {
    static final int[] CUTOFFS = buildCutoffs();
    static final String[] TYPES = {"random", "nearly_sorted", "reversed", "many_duplicates"};

    static int[] buildCutoffs() {
        List<Integer> l = new ArrayList<>();
        for (int c = 1; c <= 30; c++) l.add(c);
        for (int c : new int[]{32, 40, 50, 64, 100}) l.add(c);
        int[] r = new int[l.size()];
        for (int i = 0; i < r.length; i++) r[i] = l.get(i);
        return r;
    }

    public static void main(String[] args) throws IOException {
        String dir = args.length > 0 ? args[0] : "data";
        String out = args.length > 1 ? args[1] : "results/results.csv";
        int reps = args.length > 2 ? Integer.parseInt(args[2]) : 20;
        String[] sizeStr = (args.length > 3 ? args[3] : "10000,100000,1000000").split(",");

        Path outPath = Paths.get(out);
        if (outPath.getParent() != null) Files.createDirectories(outPath.getParent());

        printEnvironment();
        warmUp();

        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(outPath))) {
            w.println("type,n,cutoff,rep,time_ns");
            Random order = new Random(12345);

            for (String s : sizeStr) {
                int n = Integer.parseInt(s.trim());
                for (String type : TYPES) {
                    int[] original = DataGen.read(Paths.get(dir, type + "_" + n + ".txt"), n);
                    int[] work = new int[n];
                    int[] aux = new int[n];
                    System.out.printf("Running type=%s n=%d ...%n", type, n);

                    List<Integer> cutoffList = new ArrayList<>();
                    for (int c : CUTOFFS) cutoffList.add(c);

                    for (int rep = 0; rep < reps; rep++) {
                        Collections.shuffle(cutoffList, order);
                        for (int cutoff : cutoffList) {
                            System.arraycopy(original, 0, work, 0, n);
                            long t0 = System.nanoTime();
                            MergeSort.sort(work, aux, cutoff);
                            long t1 = System.nanoTime();
                            if (!MergeSort.isSorted(work)) {
                                throw new IllegalStateException("Not sorted! cutoff=" + cutoff);
                            }
                            w.println(type + "," + n + "," + cutoff + "," + rep + "," + (t1 - t0));
                        }
                    }
                    w.flush();
                }
            }
        }
        System.out.println("Done. Results written to " + out);
    }

    static void warmUp() {
        System.out.println("Warm-up...");
        Random rnd = new Random(7);
        int n = 200_000;
        int[] original = new int[n];
        for (int i = 0; i < n; i++) original[i] = rnd.nextInt();
        int[] work = new int[n];
        int[] aux = new int[n];
        for (int round = 0; round < 3; round++) {
            for (int c : CUTOFFS) {
                System.arraycopy(original, 0, work, 0, n);
                MergeSort.sort(work, aux, c);
            }
        }
    }

    static void printEnvironment() {
        System.out.println("=== Environment ===");
        System.out.println("OS:    " + System.getProperty("os.name") + " " + System.getProperty("os.version")
                + " (" + System.getProperty("os.arch") + ")");
        System.out.println("Java:  " + System.getProperty("java.version") + " / " + System.getProperty("java.vm.name"));
        System.out.println("CPUs:  " + Runtime.getRuntime().availableProcessors());
        System.out.println("MaxMem:" + Runtime.getRuntime().maxMemory() / (1024 * 1024) + " MB");
    }
}

