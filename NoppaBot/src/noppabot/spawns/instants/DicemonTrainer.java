/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.instants;

import noppabot.INoppaBot;
import noppabot.spawns.dice.Powerup;

public class DicemonTrainer extends Powerup {
	public static final String NAME = "Dicemon Trainer";
	
	@Override
	public void onSpawn(INoppaBot bot) {
		bot.sendChannelFormat("A Dicemon trainer appears!");
	}
	
	@Override
	public void onExpire(INoppaBot bot) {
		bot.sendChannelFormat("... the Dicemon trainer used FLY! He flew away.");
	}
	
	@Override
	public boolean isCarried() {
		return false;
	}
	
	@Override
	public boolean canPickUp(INoppaBot bot, String nick) {
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
	public void onPickup(INoppaBot bot, String nick) {
		Powerup oldPowerup = bot.getPowerups().get(nick);
		String oldName = oldPowerup.getName();
		Powerup newPowerup = oldPowerup.upgrade(bot);
		String newName = newPowerup.getName();
		String descr = newPowerup.getUpgradeDescription(bot, nick);
		bot.getPowerups().put(nick, newPowerup);
		bot.sendChannelFormat("The trainer says: I'll unlock the hidden potential in your dice!");
		bot.sendChannelFormat("%s's %s evolved into %s! %s", nick, oldName, newName, descr);
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