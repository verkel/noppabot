/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.spawns.*;

public class SteadyDie extends BasicPowerup {

	public static final int bonus = 30;
	public static final int sides = 70;

	@Override
	public void onSpawn() {
		bot.sendChannelFormat("A %s appears!", nameColored());
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat("%s grabs the %s.", ownerColored, nameColored());
	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... the steady die fades away.");
	}

	@Override
	public int onContestRoll() {
		int roll = roll(sides);
		int result = roll + bonus;
		String resultStr = resultStr(result);
		result = clamp(result);
		bot.sendChannelFormat("%s rolls d%d + %d with the steady die.", owner, sides, bonus);
		bot.sendChannelFormat("%s rolls %d + %d = %s! %s", ownerColored, roll, bonus, resultStr,
			bot.grade(result));
		return result;
	}

	@Override
	public String name() {
		return "Steady Die";
	}
	
	@Override
	public int sides() {
		return sides;
	}
	
	@Override
	public boolean isUpgradeable() {
		return true;
	}
	
	@Override
	public Powerup upgrade() {
		return new TrustyDie();
	}
	
	// Upgrade
	public class TrustyDie extends EvolvedPowerup {
		public static final int bonus = 50;
		public static final int sides = 50;
		
		public TrustyDie() {
			super(SteadyDie.this);
		}
		
		@Override
		public int onContestRoll() {
			int roll = roll(sides);
			int result = roll + bonus;
			String resultStr = resultStr(result);
			result = clamp(result);
			bot.sendChannelFormat("%s rolls d%d + %d with the trusty die.", owner, sides, bonus);
			bot.sendChannelFormat("%s rolls %d + %d = %s! %s", ownerColored, roll, bonus, resultStr,
				bot.grade(result));
			return result;
		}
		
		@Override
		public String name() {
			return "Trusty Die";
		}
		
		@Override
		public int sides() {
			return sides;
		}
		
		@Override
		public String getUpgradeDescription() {
			return String.format("You are guaranteed a higher roll: you now roll d%d + %d!.", sides, bonus);
		}
	}
}
