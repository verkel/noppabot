/*
 * Created on 14.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.INoppaBot;
import noppabot.spawns.*;


public class DiceBros extends BasicPowerup {

	@Override
	public void onSpawn() {
		bot.sendChannel("The Dice bros. appear!");
	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... the Dice bros. have some plumbing to do, and leave.");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat("%s grabs the %s! They will show off their rolling skills tonight.",
			ownerColored, getNameColored());
	}

	@Override
	public int onContestRoll(int marioRoll) {
		int luigiRoll = bot.getRollFor(owner, 100);
		bot.sendChannelFormat("The Dice bros. roll for %s. Mario Dice rolls %d! Luigi Dice rolls %d!",
			owner, marioRoll, luigiRoll);
		return chooseBetterRoll(marioRoll, luigiRoll);
		
//		return 100 - Math.abs(marioRoll - luigiRoll);
	}

	private int chooseBetterRoll(int marioRoll, int luigiRoll) {
		if (marioRoll > luigiRoll) {
			bot.sendChannelFormat("The bros. choose Mario's roll, %s, as %s's result. %s", 
				resultStr(marioRoll), ownerColored, bot.grade(marioRoll));
			return marioRoll;
		}
		else if (luigiRoll > marioRoll) {
			bot.sendChannelFormat("The bros. choose Luigi's roll, %s, as %s's result. %s", 
				resultStr(luigiRoll), ownerColored, bot.grade(luigiRoll));
			return luigiRoll;
		}
		else {
			bot.sendChannelFormat("The bros. rolled the same number! The bro code dictates " +
				"this is a cause for celebration, and that %s's roll result should be %s!", 
				ownerColored, resultStr(100)); 
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
	public class SuperDiceBros extends EvolvedPowerup {
		
		private BasicPowerup marioItem;
		private BasicPowerup luigiItem;

		public SuperDiceBros(INoppaBot bot) {
			super(DiceBros.this);
			marioItem = Powerups.getRandomPowerup(bot, Powerups.diceBrosPowerups);
			marioItem.setColors(false, false);
			marioItem.setOwner("Mario");
			luigiItem = Powerups.getRandomPowerup(bot, Powerups.diceBrosPowerups);
			luigiItem.setColors(false, false);
			luigiItem.setOwner("Luigi");
		}
		
		@Override
		public int onContestRoll(int marioRoll) {
			int luigiRoll = bot.getRollFor(owner, 100);

			bot.sendChannelFormat("The Super Dice bros. roll for %s.", owner);
			marioRoll = marioItem.onContestRoll(marioRoll);
			luigiRoll = luigiItem.onContestRoll(luigiRoll);
			
			return chooseBetterRoll(marioRoll, luigiRoll);
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
