package smo;

import java.util.Random;

public class FunRand {
    /**
     * Generates a random value according to an exponential
     * distribution
     *
     * @param delayMean mean value
     * @return a random value according to an exponential
     * distribution
     */
    public static double Exponential(double delayMean) {
        double a = 0;
        while (a == 0) {
            a = Math.random();
        }
        a = -delayMean * Math.log(a);
        return a;
    }

    /**
     * Generates a random value according to a uniform
     * distribution
     *
     * @param min min value
     * @param max max value
     * @return a random value according to a uniform distribution
     */
    public static double Uniform(double min, double max) {
        double a = 0;
        while (a == 0) {
            a = Math.random();
        }
        a = min + a * (max - min);
        return a;
    }

    /**
     * Generates a random value according to a normal (Gauss)
     * distribution
     *
     * @param delayMean      mean value
     * @param delayDev standard deviation of normal distribution
     * @return a random value according to a normal (Gauss) distribution
     */
    public static double Normal(double delayMean, double delayDev) {
        double a;
        Random r = new Random();
        a = delayMean + delayDev * r.nextGaussian();
        return a;
    }

    /**
     * Generates a random value according to an Erlang distribution
     *
     * @param times n
     * @param delayMean  lambda
     * @return a random value according to an Erlang distribution
     */
    public static double Erlang(double times, double delayMean) {
        double product = 1.;
        for (int i = 0; i < (int) times; ++i) {
            double a = Math.random();
            while (a == 0) {
                a = Math.random();
            }
            product *= a;
        }
        return -Math.log(product) / delayMean;
    }
}