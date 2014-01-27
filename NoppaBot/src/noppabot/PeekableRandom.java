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
	// Map of sides to next roll
	private Map<Integer, Integer> nextRolls = new TreeMap<Integer, Integer>();
	// Set of sides of dice whose next roll is publicly known
	private Set<Integer> knownDice = new HashSet<Integer>();
	private Random random;
	
	public PeekableRandom() {
		random = new Random();
	}

	public int nextRoll(int sides) {
		// Pop the peeked result for n if there is one
		if (nextRolls.containsKey(sides)) {
			setKnown(sides, false);
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
	
	public void setKnown(int sides, boolean known) {
		if (known) knownDice.add(sides);
		else knownDice.remove(sides);
	}
	
	public void setNextRoll(int sides, int roll) {
		nextRolls.put(sides, roll);
	}
	
	public Map<Integer, Integer> getNextKnownRolls() {
		Map<Integer, Integer> results = new TreeMap<Integer, Integer>();
		for (Map.Entry<Integer, Integer> pair : nextRolls.entrySet()) {
			if (knownDice.contains(pair.getKey())) {
				results.put(pair.getKey(), pair.getValue());
			}
		}
		return results;
	}
	
	private int roll(int sides) {
		return random.nextInt(sides) + 1;
	}
}
