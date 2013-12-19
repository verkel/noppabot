/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.instants;

import noppabot.spawns.*;

public class DicemonTrainer extends Instant {
	public static final String NAME = "Dicemon Trainer";
	
	@Override
	public void onSpawn() {
		bot.sendChannelFormat("A Dicemon trainer appears!");
	}
	
	@Override
	public void onExpire() {
		bot.sendChannelFormat("... the Dicemon trainer used FLY! He flew away.");
	}
	
	@Override
	public boolean canPickUp(String nick) {
		if (bot.getPowerups().containsKey(nick)) { // Has item
			Powerup powerup = bot.getPowerups().get(nick);
			if (powerup.isUpgradeable()) {
				return true;
			}
			else {
				bot.sendChannelFormat("%s: The trainer says your %s is not upgradeable.", nick, powerup);
				return false;
			}
		}
		else {
			bot.sendChannelFormat("%s: The trainer says you have nothing to upgrade.", nick);
			return false;
		}
	}

	@Override
	public void onPickup() {
		Powerup oldPowerup = bot.getPowerups().get(owner);
		String oldName = oldPowerup.getNameColored();
		Powerup newPowerup = oldPowerup.upgrade();
		String newName = newPowerup.getNameColored();
		String descr = newPowerup.getUpgradeDescription();
		bot.getPowerups().put(owner, newPowerup);
		bot.sendChannelFormat("The trainer unlocks the hidden potential in your die!");
		bot.sendChannelFormat("%s's %s evolved into %s! %s", owner, oldName, newName, descr);
	}

	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public float getSpawnChance() {
		return 2f;
	}
}