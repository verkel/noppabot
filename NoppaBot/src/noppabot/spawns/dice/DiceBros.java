/*
 * Created on 14.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.INoppaBot;


public class DiceBros extends Powerup {

	@Override
	public void onSpawn() {
		bot.sendChannel("The Dice bros. appear!");
	}

	@Override
	public void onExpire() {
		bot.sendChannelFormat("... the Dice bros. have some plumbing to do, and leave.");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat("%s grabs the Dice bros! They will show off their rolling skills tonight.",
			ownerColored);
	}

	@Override
	public int onContestRoll(int marioRoll) {
		int luigiRoll = bot.getRollFor(owner, 100);
		bot.sendChannelFormat("The Dice bros. roll for %s. Mario Dice rolls %d! Luigi Dice rolls %d!",
			ownerColored, marioRoll, luigiRoll);
		return chooseBetterRoll(bot, ownerColored, marioRoll, luigiRoll);
		
//		return 100 - Math.abs(marioRoll - luigiRoll);
	}

	private static int chooseBetterRoll(INoppaBot bot, String ownerColored, int marioRoll, int luigiRoll) {
		if (marioRoll > luigiRoll) {
			bot.sendChannelFormat("The bros. choose Mario's roll, %d, as %s's result. %s", 
				marioRoll, ownerColored, bot.grade(marioRoll));
			return marioRoll;
		}
		else if (luigiRoll > marioRoll) {
			bot.sendChannelFormat("The bros. choose Luigi's roll, %d, as %s's result. %s", 
				luigiRoll, ownerColored, bot.grade(luigiRoll));
			return luigiRoll;
		}
		else {
			bot.sendChannelFormat("The bros. rolled the same number! The bro code dictates " +
				"this is a cause for celebration, and that %s's roll result should be 100!", ownerColored); 
			return 100;
		}
	}
	
	@Override
	public String getName() {
		return "Dice Bros.";
	}

	
	@Override
	public boolean isUpgradeable() {
		return true;
	}
	
	@Override
	public Powerup upgrade() {
		return new SuperDiceBros(bot);
	}
	
	@Override
	public float getSpawnChance() {
		return 0.5f;
	}
	
	// Upgrade
	public static class SuperDiceBros extends Powerup {
		
		private Powerup marioItem;
		private Powerup luigiItem;

		public SuperDiceBros(INoppaBot bot) {
			marioItem = Powerups.getRandomPowerup(bot, Powerups.diceBrosPowerups);
			marioItem.setOwner("Mario");
			luigiItem = Powerups.getRandomPowerup(bot, Powerups.diceBrosPowerups);
			luigiItem.setOwner("Luigi");
		}
		
		@Override
		public int onContestRoll(int marioRoll) {
			int luigiRoll = bot.getRollFor(owner, 100);

			bot.sendChannelFormat("The Super Dice bros. roll for %s.", ownerColored);
			marioRoll = marioItem.onContestRoll(marioRoll);
			luigiRoll = luigiItem.onContestRoll(luigiRoll);
			
			return chooseBetterRoll(bot, ownerColored, marioRoll, luigiRoll);
		}
		
		@Override
		public String getName() {
			return "Super Dice Bros.";
		}
		
		@Override
		public String getUpgradeDescription() {
			return String.format("Both bros. now get special dice. Mario has the %s! Luigi has the %s!",
				marioItem, luigiItem);
		}
	}
}
