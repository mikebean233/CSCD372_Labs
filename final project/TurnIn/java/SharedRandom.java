package michaelpeterson.finalproject;

import java.util.Random;

public class SharedRandom {
    private static Random _random = new Random();
    private SharedRandom(){}
    public static int nextInt(int bound){return _random.nextInt(bound);}
    public static long nextLong(){return _random.nextLong();}
    public static double nextDouble(){return _random.nextDouble();}
    public static float nextFloat(){return _random.nextFloat();}
}
