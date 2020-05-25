package no.met.kvalobs.kl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

public class Matrics {
    static class HitMiss {
        private int hit;
        private int miss;
        private String name;

        HitMiss(String name) {
            hit = 0;
            miss = 0;
            this.name = name;
        }

        public float hitRatio() {
            long r = hit + miss;

            if (r != 0)
                return ((float) hit / (float) r)*100;
            else
                return 0;
        }

        public void countHit(boolean hit) {
            if (hit)
                this.hit++;
            else
                this.miss++;
        }

        public void clear() {
            hit=0;
            miss =0;
        }

        public PrintStream print(PrintStream out) {
            return out.printf("%s: %6.2f %%  (%d, %d)\n", name, hitRatio(), hit, miss);
        }
    }

    private final int DelayBetweenWrites = 1; // minutes
    private HitMiss filter = new HitMiss("StationFilter");
    private HitMiss param = new HitMiss("StationParamFilter");
    private Path file;
    OffsetDateTime nextWrite;
    OffsetDateTime cacheClearedAt;

    public Matrics(Path outFile) {
        file = outFile;
        cacheClearedAt = OffsetDateTime.now(ZoneOffset.UTC);
        initNextWrite();
        System.err.println("Matrics writes to file: " + file);
    }

    public Matrics() {
        this(null);
    }

    synchronized public void clear() {
        filter.clear();
        param.clear();
        cacheClearedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    void initNextWrite() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        int m = now.getMinute();
        m = DelayBetweenWrites - (m % DelayBetweenWrites);
        nextWrite = now.plusMinutes(m).truncatedTo(ChronoUnit.MINUTES);
        System.err.println("Init matrics writes: " + nextWrite);
    }

    synchronized public void filterCount(boolean hit) {
        filter.countHit(hit);
        write();
    }

    synchronized public void paramCount(boolean hit) {
        this.param.countHit(hit);
        write();
    }

    private void write() {
        if (file != null) {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            if (now.isAfter(nextWrite)) {
                nextWrite = nextWrite.plusMinutes(DelayBetweenWrites);
                write(file);
            }
        }
    }

    synchronized void write(Path file) {
        try (OutputStream s = Files.newOutputStream(file, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
            PrintStream out = new PrintStream(s);
            out.println("Cache cleared: "+cacheClearedAt);
            filter.print(out);
            param.print(out);
            out.close();
        } catch (IOException ex) {
            System.out.println("ERROR: Failed to write Matrics to the file '" + file + "'.");
        }
    }
}
