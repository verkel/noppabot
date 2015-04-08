package noppabot;

import adversary.*;
import adversary.WeightedRandom.Bias;

public class RandomDistribTester {

	private int ITERATIONS = 1000000;
	private int[] stats = new int[101];

	public static void main(String[] args) {
		new RandomDistribTester().run();
	}

	private void run() {
		WeightedRandom ar = new WeightedRandom(0.05, false);
		
		for (int i = 0; i < ITERATIONS; i++) {
			int roll = ar.roll(100).intValue();
			stats[roll]++;
		}
		
		float sum = 0f;
		float ev = 0f;
		
		for (int i = 0; i < stats.length; i++) {
			float weight = (float)stats[i] / (float)ITERATIONS;
			float percentage = weight * 100f;
			sum += percentage;
			ev += i * weight;
			System.out.printf("%.4f%% of %d:\t", percentage, i);
			for (int j = 0; j < percentage * 10; j++) System.out.print('=');
			System.out.println();
		}
		
		System.out.printf("\ntotal: %.4f%%\n", sum);
		System.out.printf("\nEV: %.1f\n", ev);
	}

}
