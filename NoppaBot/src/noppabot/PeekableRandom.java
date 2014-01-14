/*
 * Created on 2.12.2013
 * @author verkel
 */
package noppabot;

import java.util.*;

/**
 * Using this random you can peek what the next result is.
 */
public class PeekableRandom {
//	private int nextD100 = -1;
	private Map<Integer, Integer> nextRolls = new HashMap<Integer, Integer>();
	private Random random;
	
	public PeekableRandom() {
		random = new Random();
	}

	public int nextRoll(int sides) {
		// Pop the peeked result for n if there is one
		if (nextRolls.containsKey(sides)) {
			return nextRolls.remove(sides);
		}
		// Else return a new number
		else {
			return roll(sides);
		}
	}
	
	public int peek(int sides) {
		if (!nextRolls.containsKey(sides)) {
			int roll = roll(sides);
			nextRolls.put(sides, roll);
		}
		
		return nextRolls.get(sides);
	}
	
	public Map<Integer, Integer> getNextRolls() {
		return nextRolls;
	}
	
	private int roll(int sides) {
		return random.nextInt(sides) + 1;
	}
}
