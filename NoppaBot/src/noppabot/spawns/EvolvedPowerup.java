/*
 * Created on 19.12.2013
 * @author verkel
 */
package noppabot.spawns;

import noppabot.*;


public abstract class EvolvedPowerup<R extends Roll> extends Powerup<R> {
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
	public final void setOwner(String owner) {
		base.setOwner(owner);
	}
	
	@Override
	public String owner() {
		return base.owner();
	}
	
	@Override
	public String ownerColored() {
		return base.ownerColored();
	}
	
	public String resultStr(Roll roll) {
		return base.resultStr(roll);
	}
	
	@Override
	public String nameColored() {
		return Color.evolvedPowerup(name());
	}
	
	@Override
	public String nameWithDetails() {
		return nameWithDetails(isIdentified(), name(), details());
	}
	
	@Override
	public String nameWithDetailsColored() {
		return nameWithDetails(isIdentified(), nameColored(), details());
	}
	
	@Override
	public String details() {
		return base.details();
	}
	
	@Override
	public int sides() {
		return base.sides();
	}
	
	@Override
	public boolean isIdentified() {
		return base.isIdentified();
	}
	
	@Override
	public void setIdentified(boolean identified) {
		base.setIdentified(identified);
	}
	
	@Override
	public INoppaBot bot() {
		return base.bot();
	}
}
