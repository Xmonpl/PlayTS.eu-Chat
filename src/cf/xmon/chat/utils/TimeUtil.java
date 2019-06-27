package cf.xmon.chat.utils;

import org.jetbrains.annotations.Contract;

public enum TimeUtil
{
    TICK("TICK", 0, "TICK", 0, "TICK", 0, 50, 50),
    MILLISECOND("MILLISECOND", 1, "MILLISECOND", 1, "MILLISECOND", 1, 1, 1),
    SECOND("SECOND", 2, "SECOND", 2, "SECOND", 2, 1000, 1000),
    MINUTE("MINUTE", 3, "MINUTE", 3, "MINUTE", 3, 60000, 60),
    HOUR("HOUR", 4, "HOUR", 4, "HOUR", 4, 3600000, 60),
    DAY("DAY", 5, "DAY", 5, "DAY", 5, 86400000, 24),
    WEEK("WEEK", 6, "WEEK", 6, "WEEK", 6, 604800000, 7);

    public static final int MPT = 50;
    private final int time;
    private final int timeMulti;

    @Contract(pure = true)
    private TimeUtil(final String s3, final int n3, final String s2, final int n2, final String s, final int n, final int time, final int timeMulti) {
        this.time = time;
        this.timeMulti = timeMulti;
    }

    @Contract(pure = true)
    public int getMulti() {
        return this.timeMulti;
    }

    @Contract(pure = true)
    public int getTime() {
        return this.time;
    }

    @Contract(pure = true)
    public int getTick() {
        return this.time / 50;
    }

    @Contract(pure = true)
    public int getTime(final int multi) {
        return this.time * multi;
    }

    @Contract(pure = true)
    public int getTick(final int multi) {
        return this.getTick() * multi;
    }
}
