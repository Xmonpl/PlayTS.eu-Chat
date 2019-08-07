package cf.xmon.chat.utils;

import java.util.Random;

public class RandomUtil {
    private static final Random rand;

    static {
        rand = new Random();
    }

    public static int getRandInt(final int min, final int max) throws IllegalArgumentException {
        return RandomUtil.rand.nextInt(max - min + 1) + min;
    }

    public static Double getRandDouble(final double min, final double max) throws IllegalArgumentException {
        return RandomUtil.rand.nextDouble() * (max - min) + min;
    }

    public static Float getRandFloat(final float min, final float max) throws IllegalArgumentException {
        return RandomUtil.rand.nextFloat() * (max - min) + min;
    }

    public static boolean getChance(final double chance) {
        return chance >= 100.0 || chance >= getRandDouble(0.0, 100.0);
    }

    public static int getNextInt(int i){
        return rand.nextInt(i);
    }
}
