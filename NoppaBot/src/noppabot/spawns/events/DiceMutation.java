/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.events;

import java.util.List;

import noppabot.INoppaBot;
import noppabot.spawns.*;


public class DiceMutation extends Event {
	@Override
	public void run(INoppaBot bot) {
		bot.sendChannelFormat("Breaking news! The last batch of dice from the PRO Corp. seems to" +
			" have been subjected to gamma radiation during the manufacturing process. Dice owners are" +
			" advised to monitor any sudden changes in their dice!");
		
		List<String> owners = bot.getRandomPowerupOwners();
		int affected = 0;
		for (String owner : owners) {
			Powerup oldPowerup = bot.getPowerups().get(owner);
			if (oldPowerup.isUpgradeable()) {
				String oldName = oldPowerup.nameColored();
				Powerup newPowerup = oldPowerup.upgrade();
				String newName = newPowerup.nameColored();
				String descr = newPowerup.getUpgradeDescription();
				bot.sendChannelFormat("%s's %s mutated into %s! %s", owner, oldName, newName, descr);
				bot.getPowerups().put(owner, newPowerup);
				affected++;
			}
		}
		
		if (affected == 0) bot.sendChannel("Turns out nothing happened. What did you expect, a Spider-Die?");
	}
	
	@Override
	public String name() {
		return "Dice Mutation";
	}
}