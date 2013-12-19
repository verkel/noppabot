/*
 * Created on 19.12.2013
 * @author verkel
 */
package noppabot.spawns;

import noppabot.*;


public abstract class EvolvedPowerup extends Powerup {
	protected BasicPowerup base;
	
	public EvolvedPowerup(BasicPowerup base) {
		this.base = base;
	}
	
	@Override
	public final void initialize(INoppaBot bot) {
		base.initialize(bot);
	}
	
	@Override
	public int onContestRoll(int roll) {
		return base.onContestRoll(roll);
	}
	
	@Override
	public final void setOwner(String owner) {
		base.setOwner(owner);
	}
	
	protected String owner() {
		return base.owner();
	}
	
	protected String ownerColored() {
		return base.ownerColored();
	}
	
	public String resultStr(int roll) {
		return base.resultStr(roll);
	}
	
	public int clamp(int roll) {
		return base.clamp(roll);
	}
	
	@Override
	public String getNameColored() {
		return ColorStr.evolvedPowerup(getName());
	}
}
