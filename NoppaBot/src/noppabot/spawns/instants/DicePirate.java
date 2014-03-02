/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.instants;

import java.util.*;

import noppabot.Color;
import noppabot.spawns.*;


public class DicePirate extends Instant {

	@Override
	public void onSpawn() {
		bot.sendChannelFormat("A %s appears!", nameColored());
	}

	@Override
	public void onExpire() {
		bot.sendChannelFormat("... the DicePirate smells better loot elsewhere and leaves.");
	}
	
	@Override
	public boolean canPickUp(String nick) {
		if (bot.getPowerups().containsKey(nick)) { // Has item
			Powerup powerup = bot.getPowerups().get(nick);
			bot.sendChannelFormat("%s: you already have the %s! You wouldn't be able to carry any " +
				"loot stolen by the pirate.", Color.nick(nick), powerup.nameColored());
			return false;
		}
		else return true;
	}

	@Override
	public void onPickup() {
		Map<String, Powerup> powerups = bot.getPowerups();
		bot.sendChannelFormat("The %s will plunder dice and other shiny things for 100 gold dubloons! " +
			"%s gladly pays him.", nameColored(), ownerColored);
		Random rnd = new Random();
		Set<String> owners = new TreeSet<String>(powerups.keySet());
		int size = owners.size();
		String targetOwner = null;
		if (size > 0) {
			int itemIndex = rnd.nextInt(size);
			int i = 0;
			for (String owner : owners) {
				if (i == itemIndex) {
					targetOwner = owner;
					break;
				}
				i++;
			}
		}
		
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

	@Override
	public String name() {
		return "DicePirate";
	}
	
	@Override
	public float spawnChance() {
		return 0.75f;
	}
}
