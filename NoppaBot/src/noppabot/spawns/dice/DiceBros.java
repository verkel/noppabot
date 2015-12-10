/*
 * Created on 14.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.*;
import noppabot.spawns.*;


public class DiceBros extends BasicDie {

	public static final BasicPowerupSpawnInfo info = new BasicPowerupSpawnInfo() {

		@Override
		public BasicPowerup create() {
			return new DiceBros();
		}
		
		@Override
		public double spawnChance() {
			return 0.5;
		}
	};
	
	@Override
	public SpawnInfo<?> spawnInfo() {
		return info;
	}
	
	@Override
	public void onSpawn() {
		bot.sendChannelFormat("The %s appear!", nameColored());
	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... the Dice bros. have some plumbing to do, and leave.");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat("%s grabs the %s! They will show off their rolling skills tonight.",
			ownerColored, nameColored());
	}

	@Override
	public DiceRoll onContestRoll() {
		/*
		 * Mario & Luigi actually don't consume your rolls. It would be hard to
		 * make Super Mario & Luigi do so, and we should keep the same
		 * behaviour with the normal guys.
		 */
		DiceRoll marioRoll = bot.getRoll("Mario", 100);
		DiceRoll luigiRoll = bot.getRoll("Luigi", 100);
		
		bot.sendChannelFormat("The Dice bros. roll for %s. Mario Dice rolls %s! Luigi Dice rolls %s!",
			owner, Color.emphasize(marioRoll), Color.emphasize(luigiRoll));
		
		return chooseBetterRoll(marioRoll, luigiRoll);
	}

	private DiceRoll chooseBetterRoll(DiceRoll marioRoll, DiceRoll luigiRoll) {
		if (marioRoll.intValue() > luigiRoll.intValue()) {
			bot.sendChannelFormat("The bros. choose Mario's roll, %s, as %s's result. %s", 
				resultStr(marioRoll), ownerColored, bot.grade(marioRoll));
			return marioRoll;
		}
		else if (luigiRoll.intValue() > marioRoll.intValue()) {
			bot.sendChannelFormat("The bros. choose Luigi's roll, %s, as %s's result. %s", 
				resultStr(luigiRoll), ownerColored, bot.grade(luigiRoll));
			return luigiRoll;
		}
		else {
			DiceRoll hundred = new DiceRoll(100);
			bot.sendChannelFormat("The bros. rolled the same number! The bro code dictates " +
				"this is a cause for celebration, and that %s's roll result should be %s!", 
				ownerColored, resultStr(hundred)); 
			return hundred;
		}
	}
	
	@Override
	public String name() {
		return "Dice Bros.";
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
		return new SuperDiceBros(bot);
	}
	
	// Upgrade
	public class SuperDiceBros extends EvolvedDie {
		
		private BasicDie marioItem;
		private BasicDie luigiItem;

		public SuperDiceBros(INoppaBot bot) {
			super(DiceBros.this);
			marioItem = (BasicDie)Powerups.getRandomBasicPowerup(bot, Powerups.diceBrosPowerups);
			marioItem.setColors(false, false);
			marioItem.setOwner("Mario");
			luigiItem = (BasicDie)Powerups.getRandomBasicPowerup(bot, Powerups.diceBrosPowerups);
			luigiItem.setColors(false, false);
			luigiItem.setOwner("Luigi");
		}
		
		@Override
		public DiceRoll onContestRoll() {
			bot.sendChannelFormat("The Super Dice bros. roll for %s.", owner);
			
			DiceRoll marioRoll = marioItem.onContestRoll();
			DiceRoll luigiRoll = luigiItem.onContestRoll();
			
			return chooseBetterRoll(marioRoll, luigiRoll);
		}
		
		@Override
		public String name() {
			return "Super Dice Bros.";
		}
		
		@Override
		public int sides() {
			//return marioItem.getSides();
			
			// Alas, we cannot make Super Mario consume your rolls.
			return 100;
		}
		
		@Override
		public String getUpgradeDescription() {
			return String.format("Both bros. now get special dice. Mario has the %s! Luigi has the %s!",
				marioItem, luigiItem);
		}
	}
}
