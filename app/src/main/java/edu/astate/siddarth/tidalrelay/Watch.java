package edu.astate.siddarth.tidalrelay;

final class Watch {
    static final int REQUIRED = 6;
    static final float[] HEADINGS = {285, 64, 138, 320, 102, 226, 31, 174};
    static final String[] CHANNELS = {
        "North Jetty", "Kelp Bank", "East Channel", "Bell Shoal",
        "Harbor Mouth", "Outer Sound", "Spindrift Pier", "South Light"
    };
    private static final long[] SWEEP_MS = {3400, 3300, 3200, 3100, 3000, 2900, 2800, 2700};
    private static final float[] WINDOWS = {28, 26, 24, 23, 22, 21, 20, 19};

    enum Result {
        CENTERED("Centered", 150), RECEIVED("Received", 100), MISSED("Missed", 0);
        final String label;
        final int points;

        Result(String label, int points) {
            this.label = label;
            this.points = points;
        }
    }

    private final Result[] log = new Result[HEADINGS.length];
    private int count;

    int count() { return count; }
    boolean complete() { return count == log.length; }
    int score() {
        int total = 0;
        for (int i = 0; i < count; i++) total += log[i].points;
        return total;
    }
    int received() {
        int total = 0;
        for (int i = 0; i < count; i++) if (log[i] != Result.MISSED) total++;
        return total;
    }
    boolean cleared() { return complete() && received() >= REQUIRED; }

    float sweep(long elapsed) {
        return (elapsed % SWEEP_MS[count]) * 360f / SWEEP_MS[count];
    }

    static float window(int channel, boolean practice) { return practice ? 48 : WINDOWS[channel]; }

    Result latest() { return log[count - 1]; }

    static float distance(float heading, float target) {
        return Math.abs((heading - target + 540f) % 360f - 180f);
    }

    Result transmit(float heading, boolean practice) {
        if (complete()) throw new IllegalStateException("Watch is closed");
        float error = distance(heading, HEADINGS[count]);
        Result result = error <= 7 ? Result.CENTERED
                : error <= window(count, practice) ? Result.RECEIVED : Result.MISSED;
        log[count++] = result;
        return result;
    }

    int[] snapshot() {
        int[] values = new int[count];
        for (int i = 0; i < count; i++) values[i] = log[i].ordinal();
        return values;
    }

    static Watch restore(int[] values) {
        Watch watch = new Watch();
        if (values != null) {
            for (int value : values) watch.log[watch.count++] = Result.values()[value];
        }
        return watch;
    }
}
