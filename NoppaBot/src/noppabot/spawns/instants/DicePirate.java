/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.instants;

import java.util.*;

import noppabot.INoppaBot;
import noppabot.spawns.dice.Powerup;


public class DicePirate extends Powerup {

	@Override
	public void onSpawn(INoppaBot bot) {
		bot.sendChannel("A DicePirate appears!");
	}

	@Override
	public void onExpire(INoppaBot bot) {
		bot.sendChannelFormat("... the DicePirate smells better loot elsewhere and leaves.");
	}

	@Override
	public void onPickup(INoppaBot bot, String nick) {
		Map<String, Powerup> powerups = bot.getPowerups();
		bot.sendChannelFormat("The DicePirate will plunder dice and other shiny things for 100 gold dubloons! %s gladly pays him.", nick);
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
			powerups.remove(nick);
		}
		else {
			Powerup stolenPowerup = powerups.remove(targetOwner);
//			stolenPowerup = clonePowerup(bot, stolenPowerup); // onPickup() stuff aren't carried anymore, so no need to clone
			powerups.put(nick, stolenPowerup);
			bot.sendChannelFormat("The dice pirate looted %s's %s!", targetOwner, stolenPowerup);
			if (stolenPowerup instanceof Diceteller) stolenPowerup.onPickup(bot, nick);
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
