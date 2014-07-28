/*
 * Created on 28.7.2014
 * @author verkel
 */
package noppabot.spawns;

import noppabot.DiceRoll;


public abstract class BasicDie extends BasicPowerup<DiceRoll> {

	/**
	 * Roll d100 and return the result
	 */
	public DiceRoll roll() {
		return roll(100);
	}
	
	/**
	 * Say the appropriate roll message and return the modified roll
	 * 
	 * @return a modified roll
	 */
	@Override
	public DiceRoll onContestRoll() {
		return bot.doRoll(owner, 100);
	}
	
	public DiceRoll roll(int sides) {
		return bot.getRoll(owner, sides);
	}
}
