/*
 * Created on 28.7.2014
 * @author verkel
 */
package noppabot.spawns;

import noppabot.DiceRoll;


public abstract class EvolvedDie extends EvolvedPowerup<DiceRoll> {

	protected BasicDie baseDie;
	
	public EvolvedDie(BasicDie base) {
		super(base);
		this.baseDie = base;
	}
	
	/**
	 * Say the appropriate roll message and return the modified roll
	 * 
	 * @return a modified roll
	 */
	@Override
	public DiceRoll onContestRoll() {
		return baseDie.onContestRoll();
	}
	
	public DiceRoll roll(int sides) {
		return baseDie.roll(sides);
	}
	
	public DiceRoll roll() {
		return baseDie.roll();
	}
}
