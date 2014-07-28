/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.DiceRoll;
import noppabot.spawns.*;
import noppabot.spawns.Spawner.SpawnInfo;


public class EnchantedDie extends BasicDie {

	public static final BasicPowerupSpawnInfo info = new BasicPowerupSpawnInfo() {

		@Override
		public BasicPowerup create() {
			return new EnchantedDie();
		}
		
		@Override
		public double spawnChance() {
			return 0.75;
		}
	};
	
	@Override
	public SpawnInfo<?> spawnInfo() {
		return info;
	}
	
	public static final int bonus = 15;

	@Override
	public void onSpawn() {
		bot.sendChannelFormat("An %s appears!", nameColored());
	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... the spell on the enchanted die expires and nobody really wants it anymore.");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat("%s grabs the %s and feels a tingling sensation at the fingertips.", 
			ownerColored, nameColored());
	}

	@Override
	public DiceRoll onContestRoll() {
		DiceRoll roll = roll();
		DiceRoll result = roll.add(bonus);
		bot.sendChannelFormat(
			"The enchanted die grants %s either eternal fame and fortune, or a substantial roll bonus. %s chooses the latter.",
			owner, owner);
		bot.sendChannelFormat("%s rolls %s + %s = %s! %s", ownerColored, roll, bonus,
			resultStr(result), bot.grade(result));
		return result;
	}

	@Override
	public String name() {
		return "Enchanted Die";
	}
	
	@Override
	public int sides() {
		return 100;
	}
	
	@Override
	public boolean isUpgradeable() {
		return true;
	}
	
	@Override
	public Powerup upgrade() {
		return new PotentDie();
	}
	
	// Upgrade
	public class PotentDie extends EvolvedDie {
		private static final int bonus = EnchantedDie.bonus + 5;
		
		public PotentDie() {
			super(EnchantedDie.this);
		}
		
		@Override
		public DiceRoll onContestRoll() {
			DiceRoll roll = roll();
			DiceRoll result = roll.add(bonus);
			bot.sendChannelFormat("The potent die grants the utmost magical advantage for %s's roll!",
				owner);
			bot.sendChannelFormat("%s rolls %s + %s = %s! %s", owner, roll, bonus, resultStr(result),
				bot.grade(result));
			return result;
		}
		
		@Override
		public String name() {
			return "Potent Die";
		}
		
		@Override
		public String getUpgradeDescription() {
			return String.format("It is now enchanted with the most potent magics, granting it +%s total roll bonus!", bonus);
		}
	}
}