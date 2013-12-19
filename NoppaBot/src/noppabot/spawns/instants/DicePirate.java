/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.instants;

import java.util.*;

import noppabot.spawns.*;


public class DicePirate extends BasicPowerup {

	@Override
	public void onSpawn() {
		bot.sendChannel("A DicePirate appears!");
	}

	@Override
	public void onExpire() {
		bot.sendChannelFormat("... the DicePirate smells better loot elsewhere and leaves.");
	}

	@Override
	public void onPickup() {
		Map<String, Powerup> powerups = bot.getPowerups();
		bot.sendChannelFormat("The DicePirate will plunder dice and other shiny things for 100 gold dubloons! %s gladly pays him.", owner);
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
			bot.sendChannelFormat("There was no loot in sight for the DicePirate and he just runs off with your gold.");
			powerups.remove(owner);
		}
		else {
			Powerup stolenPowerup = powerups.remove(targetOwner);
			powerups.put(owner, stolenPowerup);
			stolenPowerup.setOwner(owner);
			bot.sendChannelFormat("The dice pirate looted %s's %s!", targetOwner, stolenPowerup);
		}
	}

	@Override
	public String getName() {
		return "DicePirate";
	}
	
	@Override
	public boolean isCarried() {
		return false;
	}
	
	@Override
	public float getSpawnChance() {
		return 0.75f;
	}
}
