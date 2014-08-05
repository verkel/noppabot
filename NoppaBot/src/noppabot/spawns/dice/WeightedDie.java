/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.DiceRoll;
import noppabot.spawns.*;
import noppabot.spawns.evolved.CrushingDie;


public class WeightedDie extends BasicDie {

	public static final int bonus = 10;
	
	public static final BasicPowerupSpawnInfo info = new BasicPowerupSpawnInfo() {

		@Override
		public BasicPowerup create() {
			return new WeightedDie();
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
	public void onExpire() {
		sendExpireMessageFormat("... the weighted die falls into emptiness.");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat("%s grabs the %s. It weighs just the right amount!", 
			ownerColored, nameColored());
	}

	@Override
	public DiceRoll onContestRoll() {
		DiceRoll roll = roll();
		DiceRoll result = roll.add(bonus);
		bot.sendChannelFormat(
			"%s's weighted die is so fairly weighted, that the roll goes very smoothly!", owner);
		bot.sendChannelFormat("%s rolls %s + %s = %s! %s", ownerColored, roll, bonus, resultStr(result),
			bot.grade(result));
		return result;
	}

	@Override
	public String name() {
		return "Weighted Die";
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
		return new CrushingDie(this, false);
	}
}