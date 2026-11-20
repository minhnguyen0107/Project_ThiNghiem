package src;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class DataGen {
    static final long SEED = 20260101L;
    static final String[] TYPES = {"random", "nearly_sorted", "reversed", "many_duplicates"};

    public static void main(String[] args) throws IOException {
        String dir = args.length > 0 ? args[0] : "data";
        String[] sizeStr = (args.length > 1 ? args[1] : "10000,100000,1000000").split(",");
        Files.createDirectories(Paths.get(dir));

        for (String s : sizeStr) {
            int n = Integer.parseInt(s.trim());
            for (String type : TYPES) {
                int[] a = generate(type, n, new Random(SEED + n));
                write(Paths.get(dir, type + "_" + n + ".txt"), a);
                System.out.println("Wrote " + type + "_" + n + ".txt");
            }
        }
    }

    static int[] generate(String type, int n, Random rnd) {
        int[] a = new int[n];
        switch (type) {
            case "random":
                for (int i = 0; i < n; i++) a[i] = rnd.nextInt();
                break;
            case "nearly_sorted": 
                for (int i = 0; i < n; i++) a[i] = i;
                for (int k = 0; k < n / 100; k++) {
                    int i = rnd.nextInt(n), j = rnd.nextInt(n);
                    int t = a[i]; a[i] = a[j]; a[j] = t;
                }
                break;
            case "reversed":
                for (int i = 0; i < n; i++) a[i] = n - i;
                break;
            case "many_duplicates": 
                for (int i = 0; i < n; i++) a[i] = rnd.nextInt(100);
                break;
            default:
                throw new IllegalArgumentException(type);
        }
        return a;
    }

    static void write(Path p, int[] a) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(p)) {
            for (int x : a) {
                w.write(Integer.toString(x));
                w.newLine();
            }
        }
    }

    static int[] read(Path p, int n) throws IOException {
        int[] a = new int[n];
        try (java.io.BufferedReader r = Files.newBufferedReader(p)) {
            for (int i = 0; i < n; i++) a[i] = Integer.parseInt(r.readLine().trim());
        }
        return a;
    }
}

