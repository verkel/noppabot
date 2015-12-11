/*
 * Created on 16.12.2013
 * @author verkel
 */
package noppabot.spawns.instants;

import java.time.*;
import java.util.*;

import noppabot.*;
import noppabot.NoppaBot.SpawnTask;
import noppabot.spawns.*;
import adversary.Adversary;


public class TrollingProfessional extends noppabot.spawns.Instant {
	
	public static final String NAME = "Trolling Professional";
	
	public static final InstantSpawnInfo info = new InstantSpawnInfo() {

		@Override
		public noppabot.spawns.Instant create() {
			return new TrollingProfessional();
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
		bot.sendChannelFormat("A %s appears!", nameColored());
	}

	@Override
	public void onExpire() {
		bot.sendChannelFormat("... the trolling professional walks away.");
	}

	@Override
	public void onPickup() {
		if (Math.random() < 0.25 && canInviteAdversary()) inviteAdversary();
		else spawnBomb();
	}

	private boolean canInviteAdversary() {
		Optional<Adversary> adv = bot.getAdversary();
		if (adv.isPresent()) {
			return !adv.get().isOnChannel();
		}
		else return false;
	}
	
	private void inviteAdversary() {
		bot.sendChannelFormat("%s grabs the %s!", owner, nameColored());
		bot.sendChannelFormat("\"Say hello to my little friend!\", the trolling professional announces.");
		Adversary adversary = bot.getAdversary().get();
		adversary.join();
	}
	
	private void spawnBomb() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime end = bot.getSpawnEndTime();
		int deltaMins = (int)Duration.between(now, end).toMinutes();
		int addMins = Powerups.powerupRnd.nextInt(deltaMins);
		LocalDateTime spawnTime = now.plusMinutes(addMins);
		Bomb bomb = new Bomb();
		bomb.initialize(bot);
		SpawnTask task = bot.scheduleSpawn(spawnTime, bomb);
		
		bot.sendChannelFormat("%s grabs the %s! %s and the trolling professional " +
			"briefly discuss about something.", owner, nameColored(), owner);
		
		bot.sendMessageFormat(owner, "Hi! I set us up the %s on %s. I suggest you don't take it.",
			Color.basicPowerup(Bomb.BOMB_NAME), task.toStringColored());
		bot.sendMessageFormat(owner, "Regards, The Trolling Professional");
	}

	@Override
	public String name() {
		return NAME;
	}
	
	public static class Bomb extends BasicDie {
		private static final int dmgSides = 10;
		private static final int dmgBonus = 10;
		
		public static final String BOMB_NAME = "BOMB!";
		
		@Override
		public SpawnInfo<?> spawnInfo() {
			return BasicPowerup.dontSpawnInfo;
		}
		
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
			"Trusty Die",
			"Weighted Die"
		};
		
		public static String[] powerupTypos = {
			"Bag of Die",
			"Dice Pros",
			"Enchaented Die",
			"Xtreme Die",
			"Fats Die",
			"Groinhog Die",
			"Humungus Die",
			"Lucky Dye",
			"Master Dei",
			"Polishd Die",
			"Staedy Die",
			"Rolling Professioanal", // I left this after rolling pro revamp... just because
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
				"it's actually a %s This can't be good for the upcoming roll.", 
				ownerColored, Color.basicPowerup(name), Color.basicPowerup(BOMB_NAME));
			name = BOMB_NAME;
		}
		
		@Override
		public void onExpire() {
			bot.sendChannelFormat("... the %s explodes! You realize it probably wasn't a %s to begin with. " +
				"Gladly no-one took it!", name, name);
		}
		
		@Override
		public DiceRoll onContestRoll() {
			DiceRoll roll = roll();
			int damageRoll = Powerups.powerupRnd.nextInt(dmgSides) + 1;
			int totalDamage = damageRoll + dmgBonus;
			DiceRoll result = roll.sub(totalDamage);
			
			sendDefaultContestRollMessage(roll);
			bot.sendChannelFormat("The bomb on %s explodes, causing %s + %s = %s damage to the roll! "
				+ "%s's roll drops down to %s.", owner, damageRoll, dmgBonus, totalDamage,
				ownerColored, resultStr(result));

			return result;
		}
		
		@Override
		public String name() {
			return name;
		}
		
		@Override
		public int sides() {
			return 100;
		}
	}
}
