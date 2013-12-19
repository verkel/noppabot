/*
 * Created on 16.12.2013
 * @author verkel
 */
package noppabot.spawns.instants;

import java.util.*;
import java.util.concurrent.TimeUnit;

import noppabot.NoppaBot.SpawnTask;
import noppabot.spawns.dice.*;


public class TrollingProfessional extends BasicPowerup {
	@Override
	public void onSpawn() {
		bot.sendChannel("A certified trolling professional appears!");
	}

	@Override
	public void onExpire() {
		bot.sendChannelFormat("... the trolling professional walks away.");
	}

	@Override
	public void onPickup() {
		Calendar now = Calendar.getInstance();
		Calendar end = bot.getSpawnEndTime();
		long deltaMs = end.getTimeInMillis() - now.getTimeInMillis();
		int deltaMins = (int)TimeUnit.MILLISECONDS.toMinutes(deltaMs);
		int addMins = Powerups.powerupRnd.nextInt(deltaMins);
		Calendar spawnTime = (Calendar)now.clone();
		spawnTime.add(Calendar.MINUTE, addMins);
		Bomb bomb = new Bomb();
		SpawnTask task = bot.scheduleSpawn(spawnTime, bomb);
		
		bot.sendChannelFormat("%s grabs the trolling professional! %s and the trolling trofessional " +
		"briefly discuss about something.", owner, owner);
		
		bot.sendMessageFormat(owner, "Hi! I set us up the Bomb on %s. I suggest you don't take it.",
			task);
		bot.sendMessageFormat(owner, "Regards, The Trolling Professional");
	}

	@Override
	public String getName() {
		return "Trolling Professional";
	}
	
	@Override
	public boolean isCarried() {
		return false;
	}
	
	@Override
	public float getSpawnChance() {
		return 0.5f;
	}
	
	public static class Bomb extends Powerup {
		private static final int dmgSides = 10;
		private static final int dmgBonus = 10;
		
		public static String[] powerupNames = {
			"Bag of Dice",
			"Dice Bros.",
			"Enchanted Die",
			"Extreme Die",
			"Fast Die",
			"Groundhog Die",
			"Humongous Die",
			"Lucky Die",
			"Master Die",
			"Polished Die",
			"Primal Die",
			"Rolling Professional",
			"Weighted Die"
		};
		
		public static String[] powerupTypos = {
			"Bag of Doge",
			"Duke Bros.",
			"Enchaented Die",
			"Xxtreme Die",
			"Fats Die",
			"Groinhog Die",
			"Humungus Die",
			"Lucky Dye",
			"Masterr Die",
			"Polishd Dei",
			"Pirmal Die",
			"Rolling Professioanal",
			"Weigthed Die"
		};
		
		private String name;
		
		public Bomb() {
			Random rng = Powerups.powerupRnd;
			if (rng.nextFloat() < 0.5f) {
				int rnd = rng.nextInt(powerupNames.length);
				name = powerupNames[rnd];
			}
			else {
				int rnd = rng.nextInt(powerupTypos.length);
				name = powerupTypos[rnd];
			}
		}
		
		@Override
		public void onPickup() {
			bot.sendChannelFormat("%s grabs the %s! On a closer inspection, you recognize " +
				"it's actually a BOMB! This can't be good for the upcoming roll.", owner, name);
			name = "BOMB!";
		}
		
		@Override
		public void onExpire() {
			bot.sendChannelFormat("... the %s explodes! You realize it probably wasn't a %s to begin with. " +
				"Gladly no-one took it!", name, name);
		}
		
		@Override
		public int onContestRoll(int roll) {
			
			int damageRoll = Powerups.powerupRnd.nextInt(dmgSides) + 1;
			int totalDamage = damageRoll + dmgBonus;
			int result = roll - totalDamage;
			String resultStr = resultStr(result);
			result = clamp(result);
			
			bot.sendDefaultContestRollMessage(owner, roll);
			bot.sendChannelFormat("The bomb on %s explodes, causing %d + %d = %d damage to the roll! " +
				"%s's roll drops down to %s.", owner, damageRoll, dmgBonus, totalDamage, ownerColored, resultStr);
			
			return result;
		}
		
		@Override
		public String getName() {
			return name;
		}
	}
}
