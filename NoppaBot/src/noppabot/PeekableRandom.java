/*
 * Created on 2.12.2013
 * @author verkel
 */
package noppabot;

import java.util.Random;

/**
 * Using this random you can peek what the next d100 roll is
 */
public class PeekableRandom {
	private int nextD100 = -1;
	private Random random;
	
	public PeekableRandom() {
		random = new Random();
	}

	public int nextInt(int n) {
		if (nextD100 != -1 && n == 100) {
			int result = nextD100;
			nextD100 = -1;
			return result;
		}
		else {
			return random.nextInt(n);
		}
	}
	
	public int peek() {
		if (nextD100 == -1) {
			nextD100 = random.nextInt(100);
		}
		return nextD100;
	}
}
