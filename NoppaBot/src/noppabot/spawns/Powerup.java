/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns;

import noppabot.*;

public abstract class Powerup implements ISpawnable, IColorStrConvertable {
	
	public abstract String getName();
	
	public abstract String getNameColored();
	
	public abstract Powerup initialize(INoppaBot bot);
	
	public abstract void setOwner(String owner);

	public void onRollPeriodStart() {
	}
	
	public void onTiebreakPeriodStart() {
	}

	public int onNormalRoll(int roll) {
		return roll;
	}

	/**
	 * Say the appropriate roll message and return the modified roll
	 * 
	 * @return a modified roll
	 */
	public abstract int onContestRoll(int roll);
	
	public int onOpponentRoll(String opponent, int roll) {
		return roll;
	}
	
	public void onOpponentRollLate(String opponent, int roll) {
	}
	
	public String getUpgradeDescription() {
		return "";
	}

	/**
	 * Should the powerup be put into player's item slot
	 */
	public boolean isCarried() {
		return true;
	}
	
	public boolean isUpgradeable() {
		return false;
	}
	
	public Powerup upgrade() {
		return null;
	}
	
	@Override
	public float getSpawnChance() {
		return 1;
	}

	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public String toStringColored() {
		return getNameColored();
	}

	@Override
	public boolean equals(Object obj) {
		// This is useful for removing stuff from the spawning lists
		return this.getClass().equals(obj.getClass());
	}
	
//	@Override
//	public int compareTo(Powerup p) {
//		return cmp(getCost(), p.getCost());
//	}
//	
//	private static int cmp(int c1, int c2) {
//		return c1 > c2 ? 1 : c1 < c2 ? -1 : 0;
//	}
//	

}