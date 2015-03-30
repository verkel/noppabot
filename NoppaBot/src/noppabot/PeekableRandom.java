/*
 * Created on 2.12.2013
 * @author verkel
 */
package noppabot;

import java.util.*;

import noppabot.PeekedRoll.Hint;

/**
 * Using this random you can peek what the next result is.
 */
public class PeekableRandom {
	private Map<Integer, PeekedRoll> peekedRolls = new TreeMap<Integer, PeekedRoll>();
	protected Random random;
	
	public PeekableRandom() {
		random = new Random();
	}

	public DiceRoll roll(int sides) {
		// Pop the peeked result for n if there is one
		if (peekedRolls.containsKey(sides)) {
			PeekedRoll pr = peekedRolls.remove(sides);
			return pr.value;
		}
		// Else return a new number
		else {
			return doRoll(sides);
		}
	}
	
	public PeekedRoll peek(int sides) {
		if (!peekedRolls.containsKey(sides)) {
			DiceRoll roll = doRoll(sides);
			peekedRolls.put(sides, new PeekedRoll(sides, roll));
		}
		
		return peekedRolls.get(sides);
	}
	
	public Hint spawnHint(int sides) {
		PeekedRoll peekedRoll = peek(sides);
		return peekedRoll.spawnHint();
	}
	
	public boolean containsHints() {
		for (PeekedRoll roll : peekedRolls.values()) {
			if (!roll.hints.isEmpty()) return true;
		}
		return false;
	}
	
	public Map<Integer, PeekedRoll> getPeekedRolls() {
		return peekedRolls;
	}
	
	public void setNextRoll(int sides, DiceRoll roll) {
		peekedRolls.put(sides, new PeekedRoll(sides, roll));
	}
	
	protected DiceRoll doRoll(int sides) {
		return new DiceRoll(doIntRoll(sides));
	}
	
	protected int doIntRoll(int sides) {
		return random.nextInt(sides) + 1;
	}
}
