package noppabot;

import adversary.AdversaryRandom;

public class RandomDistribTester {

	private int ITERATIONS = 1000000;
	private int[] stats = new int[101];

	public static void main(String[] args) {
		new RandomDistribTester().run();
	}

	private void run() {
		AdversaryRandom ar = new AdversaryRandom();
		
		for (int i = 0; i < ITERATIONS; i++) {
			int roll = ar.roll(100).intValue();
			stats[roll]++;
		}
		
		for (int i = 0; i < stats.length; i++) {
			float percentage = (float)stats[i] / (float)ITERATIONS * 100f;
			System.out.printf("%.2f%% of %d:\t", percentage, i);
			for (int j = 0; j < percentage * 10; j++) System.out.print('=');
			System.out.println();
		}
	}

}
