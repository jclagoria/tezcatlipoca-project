package com.random.data.adapter.outbound.provider.person.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility class for weighted random selection.
 */
public class WeightedRandom {
    /**
     * Selects a random value based on weighted probabilities.
     * @param values Array of possible values
     * @param weights Corresponding weights for each value
     * @return Selected value based on weights
     */
    public static String select(String[] values, int[] weights) {
        int total = 0;
        for (int weight : weights) {
            total += weight;
        }

        int random = ThreadLocalRandom.current().nextInt(total);
        int cum = 0;

        for (int i = 0; i < values.length; i++) {
            cum += weights[i];
            if (random < cum) {
                return values[i];
            }
        }

        return values[values.length - 1];
    }
}
