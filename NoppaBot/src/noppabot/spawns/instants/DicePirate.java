/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.instants;

import java.util.*;

import noppabot.Color;
import noppabot.spawns.*;


public class DicePirate extends Instant {

	public static final String NAME = "DicePirate";
	
	public static final InstantSpawnInfo info = new InstantSpawnInfo() {

		@Override
		public Instant create() {
			return new DicePirate();
		}
		
		@Override
		public double spawnChance() {
			return 0.75;
		}
		
		@Override
		public boolean spawnInDiceStormPowerups() {
			return false;
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
		bot.sendChannelFormat("... the DicePirate smells better loot elsewhere and leaves.");
	}
	
	@Override
	public boolean canPickUp(String nick, boolean verbose) {
		if (bot.getPowerups().containsKey(nick)) { // Has item
			Powerup powerup = bot.getPowerups().get(nick);
			if (verbose) bot.sendChannelFormat("%s: you already have the %s! You wouldn't be able to carry any " +
				"loot stolen by the pirate.", Color.nick(nick), powerup.nameWithDetailsColored());
			return false;
		}
		// Maybe disallow picking if the pirate would not have anything to steal? This is debatable.
		
		else return true;
	}

	@Override
	public void onPickup() {
		Map<String, Powerup> powerups = bot.getPowerups();
		String targetOwner = getRandomTargetOwner(powerups);
		
		bot.sendChannelFormat("The %s will plunder dice and other shiny things for 100 gold dubloons! " +
			"%s gladly pays him.", nameColored(), ownerColored);
		
		if (targetOwner == null) {
			bot.sendChannelFormat("There was no loot in sight for the %s and he just runs off with your gold.", nameColored());
			powerups.remove(owner);
		}
		else {
			Powerup stolenPowerup = powerups.remove(targetOwner);
			powerups.put(owner, stolenPowerup);
			stolenPowerup.setOwner(owner);
			bot.sendChannelFormat("%s's %s was stolen by the pirate!", 
				Color.nick(targetOwner), stolenPowerup.nameColored());
		}
	}

	private String getRandomTargetOwner(Map<String, Powerup> powerups) {
		Map<String, Powerup> filteredPowerups = filterPowerups(powerups);
		String targetOwner = doGetRandomTargetOwner(filteredPowerups);
		return targetOwner;
	}

	private Map<String, Powerup> filterPowerups(Map<String, Powerup> powerups) {
		// Filter powerups, remove ones that the pirate owner could not grab
		Map<String, Powerup> powerupsFiltered = new TreeMap<String, Powerup>(powerups);
		for (Iterator<Powerup> it = powerupsFiltered.values().iterator(); it.hasNext();) {
			Powerup powerup = it.next();
			if (!powerup.canPickUp(owner, false)) it.remove();
		}
		return powerupsFiltered;
	}

	private String doGetRandomTargetOwner(Map<String, Powerup> powerups) {
		Set<String> owners = new TreeSet<String>(powerups.keySet());
		int size = owners.size();
		String targetOwner = null;
		if (size > 0) {
			int itemIndex = Powerups.powerupRnd.nextInt(size);
			int i = 0;
			for (String owner : owners) {
				if (i == itemIndex) {
					targetOwner = owner;
					break;
				}
				i++;
			}
		}
		return targetOwner;
	}

	@Override
	public String name() {
		return NAME;
	}
}
