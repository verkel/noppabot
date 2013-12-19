/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.spawns.*;



public class PolishedDie extends BasicPowerup {

	public static final int bonus = 5;

	@Override
	public void onSpawn() {
		bot.sendChannelFormat("A %s appears!", getNameColored());
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat("%s grabs the %s.", ownerColored, getNameColored());
	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... the polished die fades away.");
	}

	@Override
	public int onContestRoll(int roll) {
		int result = roll + bonus;
		String resultStr = resultStr(result);
		result = clamp(result);
		bot.sendChannelFormat("The polished die adds a nice bonus to %s's roll.", owner);
		bot.sendChannelFormat("%s rolls %d + %d = %s! %s", ownerColored, roll, bonus, resultStr,
			bot.grade(result));
		return result;
	}

	@Override
	public String getName() {
		return "Polished Die";
	}
	
	@Override
	public boolean isUpgradeable() {
		return true;
	}
	
	@Override
	public Powerup upgrade() {
		return new VeryPolishedDie();
	}
	
	// Upgrade
	public class VeryPolishedDie extends EvolvedPowerup {
		private String name = "Very Polished Die";
		private int bonus = PolishedDie.bonus + 10;
		
		public VeryPolishedDie() {
			super(PolishedDie.this);
		}
		
		@Override
		public int onContestRoll(int roll) {
			int result = roll + bonus;
			String resultStr = resultStr(result);
			result = clamp(result);
			bot.sendChannelFormat("The %s adds a sweet bonus to %s's roll.", name.toLowerCase(), owner);
			bot.sendChannelFormat("%s rolls %d + %d = %s! %s", ownerColored, roll, bonus, resultStr,
				bot.grade(result));
			return result;
		}
		
		@Override
		public String getName() {
			return name;
		}
		
		@Override
		public boolean isUpgradeable() {
			return true;
		}
		
		@Override
		public Powerup upgrade() {
			name = "Very " + name;
			bonus += 10;
			return this;
		}
		
		@Override
		public String getUpgradeDescription() {
			return String.format("It now has +10 further bonus, for a total of +%d!", bonus);
		}
	}
}
