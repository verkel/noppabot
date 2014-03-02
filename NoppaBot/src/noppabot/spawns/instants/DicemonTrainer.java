/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.instants;

import noppabot.Color;
import noppabot.spawns.*;

public class DicemonTrainer extends Instant {
	public static final String NAME = "Dicemon Trainer";
	
	@Override
	public void onSpawn() {
		bot.sendChannelFormat("A %s appears!", nameColored());
	}
	
	@Override
	public void onExpire() {
		// http://bulbapedia.bulbagarden.net/wiki/Field_move_(main_series)
		// Bad Pokemon jokes continue:
		int rnd = Powerups.powerupRnd.nextInt(5);
		if (rnd == 0) bot.sendChannelFormat("... the Dicemon trainer used FLY! He flew away.");
		else if (rnd == 1) bot.sendChannelFormat("... the Dicemon trainer used DIG! He dug his way out of here.");
		else if (rnd == 2) bot.sendChannelFormat("... the Dicemon trainer used TELEPORT! He re-materialized in front of the nearest Dicemon center.");
		else if (rnd == 3) bot.sendChannelFormat("... the Dicemon trainer used SURF! He took off to the seas.");
		else if (rnd == 4) bot.sendChannelFormat("... the Dicemon trainer used FLASH! When your sight returns, he has disappeared.");
	}
	
	@Override
	public boolean canPickUp(String nick) {
		if (bot.getPowerups().containsKey(nick)) { // Has item
			Powerup powerup = bot.getPowerups().get(nick);
			if (powerup.isUpgradeable()) {
				return true;
			}
			else {
				bot.sendChannelFormat("%s: The trainer says your %s is not upgradeable.", Color.nick(nick), powerup.nameColored());
				return false;
			}
		}
		else {
			bot.sendChannelFormat("%s: The trainer says you have nothing to upgrade.", Color.nick(nick));
			return false;
		}
	}

	@Override
	public void onPickup() {
		Powerup oldPowerup = bot.getPowerups().get(owner);
		String oldName = oldPowerup.nameColored();
		Powerup newPowerup = oldPowerup.upgrade();
		String newName = newPowerup.nameColored();
		String descr = newPowerup.getUpgradeDescription();
		bot.getPowerups().put(owner, newPowerup);
		bot.sendChannelFormat("The trainer unlocks the hidden potential in your die!");
		bot.sendChannelFormat("%s's %s evolved into %s! %s", ownerColored, oldName, newName, descr);
	}

	@Override
	public String name() {
		return NAME;
	}
	
	@Override
	public float spawnChance() {
		return 2f;
	}
}