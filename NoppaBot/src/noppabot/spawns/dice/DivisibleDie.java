/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.DiceRoll;
import noppabot.spawns.*;


public class DivisibleDie extends BasicDie {

	public static final BasicPowerupSpawnInfo info = new BasicPowerupSpawnInfo() {

		@Override
		public BasicPowerup create() {
			return new DivisibleDie();
		}
	};
	
	@Override
	public SpawnInfo<?> spawnInfo() {
		return info;
	}
	
	private final int divisor;
	
	public DivisibleDie() {
		divisor = 2 + Powerups.powerupRnd.nextInt(9);
		setIdentified(true);
	}
	
	@Override
	public void onSpawn() {
		bot.sendChannelFormat("A %s appears!", nameWithDetailsColored());
	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... the divisible die's attention is divided elsewhere.");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat("%s grabs the %s! You have its undivided attention.", 
			ownerColored, nameWithDetailsColored());
	}
	
	@Override
	public DiceRoll onContestRoll() {
		DiceRoll roll = roll();
		
		boolean divisible = roll.total() % divisor == 0;
		if (divisible) {
			int bonus = divisor * 5;
			DiceRoll result = roll.add(bonus);
			bot.sendChannelFormat("%s rolls %s with the divisible die! This number is divisible with %s, granting you a bonus of +%s!", 
				owner, roll, divisor, bonus);
			bot.sendChannelFormat("%s's final roll is %s + %s = %s!", ownerColored, roll, bonus,
				resultStr(result));
			return result;
		}
		else {
			bot.sendChannelFormat("%s rolls %s with the divisible die! The roll is not divisible with %s, though (the division has the remainder %s).", 
				ownerColored, resultStr(roll), divisor, roll.total() % divisor);
			return roll;
		}
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
	public String name() {
		return "Divisible Die";
	}
	
	@Override
	public String details() {
		return "/ " + divisor;
	}
	
	@Override
	public Powerup upgrade() {
		return new ModularDie();
	}

	// Upgrade
	public class ModularDie extends EvolvedDie {
		public ModularDie() {
			super(DivisibleDie.this);
		}
		
		@Override
		public DiceRoll onContestRoll() {
			DiceRoll roll = roll();
			
			int remainder = roll.total() % divisor;
			int bonus = remainder * 5;
			DiceRoll result = roll.add(bonus);
			bot.sendChannelFormat("%s rolls %s with the modular die! %s mod %s = %s, granting you +%s roll bonus!", 
				owner, roll, roll, divisor, remainder, bonus);
			bot.sendChannelFormat("%s's final roll is %s + %s = %s!", ownerColored, roll, bonus,
				resultStr(result));
			return result;
		}

		@Override
		public String name() {
			return "Modular Die";
		}
		
		@Override
		public String details() {
			return "mod " + divisor;
		}
		
		@Override
		public String getUpgradeDescription() {
			return String.format("When your roll is divided by %s, you now get bonus based on how large the remainder of the division is.", divisor);
		}
	}
}