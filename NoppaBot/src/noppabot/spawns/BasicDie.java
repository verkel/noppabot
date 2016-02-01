/*
 * Created on 28.7.2014
 * @author verkel
 */
package noppabot.spawns;

import noppabot.DiceRoll;


public abstract class BasicDie extends BasicPowerup<DiceRoll> {

	/**
	 * Say the appropriate roll message and return the modified roll
	 * 
	 * @return a modified roll
	 */
	@Override
	public DiceRoll onContestRoll() {
		return doNormalRoll();
	}
	
	/**
	 * Do a normal d100 roll. You may override onNormalRoll() to call this to
	 * have the die only do its contest roll during contest time.
	 */
	protected final DiceRoll doNormalRoll() {
		return bot.doRoll(owner, 100);
	}
	
	/**
	 * Generate a d100 roll
	 */
	public DiceRoll roll() {
		return roll(100);
	}
	
	/**
	 * Generate a dN roll
	 */
	public DiceRoll roll(int sides) {
		return bot.getRoll(owner, sides);
	}
}
