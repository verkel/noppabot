/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.DiceRoll;
import noppabot.spawns.*;
import noppabot.spawns.Spawner.SpawnInfo;

public class SteadyDie extends BasicDie {

	public static final int bonus = 30;
	public static final int sides = 70;

	public static final BasicPowerupSpawnInfo info = new BasicPowerupSpawnInfo() {

		@Override
		public BasicPowerup create() {
			return new SteadyDie();
		}
		
	};
	
	@Override
	public SpawnInfo<?> spawnInfo() {
		return info;
	}
	
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
	public DiceRoll onContestRoll() {
		DiceRoll roll = roll(sides);
		DiceRoll result = roll.add(bonus);
		bot.sendChannelFormat("%s rolls d%s + %s with the steady die.", owner, sides, bonus);
		bot.sendChannelFormat("%s rolls %s + %s = %s! %s", ownerColored, roll, bonus,
			resultStr(result), bot.grade(result));
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
	public class TrustyDie extends EvolvedDie {
		public static final int bonus = 50;
		public static final int sides = 50;
		
		public TrustyDie() {
			super(SteadyDie.this);
		}
		
		@Override
		public DiceRoll onContestRoll() {
			DiceRoll roll = roll(sides);
			DiceRoll result = roll.add(bonus);
			bot.sendChannelFormat("%s rolls d%s + %s with the trusty die.", owner, sides, bonus);
			bot.sendChannelFormat("%s rolls %s + %s = %s! %s", ownerColored, roll, bonus,
				resultStr(result), bot.grade(result));
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
