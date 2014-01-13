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
	public final EvolvedPowerup initialize(INoppaBot bot) {
		base.initialize(bot);
		return this;
	}
	
	@Override
	public int onContestRoll() {
		return base.onContestRoll();
	}
	
	@Override
	public int onNormalRoll() {
		return base.onNormalRoll();
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
	
	@Override
	public int roll(int sides) {
		return base.roll(sides);
	}
	
	@Override
	public INoppaBot getBot() {
		return base.getBot();
	}
}
