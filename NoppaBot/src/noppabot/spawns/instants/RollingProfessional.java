/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.instants;

import noppabot.Color;
import noppabot.spawns.*;


public class RollingProfessional extends Instant {

	private static final int proBonus = 5;
	private static final int profBonus = 10;

	private boolean professor;
	
	@Override
	public void onInitialize() {
		professor = (Powerups.powerupRnd.nextFloat() < 0.25f);
	}
	
	@Override
	public void onSpawn() {
		bot.sendChannelFormat("A %s appears!", nameColored());
	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... nobody seems to want to polish their skills, so the rolling professional walks away.");
	}

	@Override
	public boolean canPickUp(String nick) {
		int sides = bot.getPowerupSides(nick);
		int nextRoll = bot.peekRoll(nick, sides, false);
		if (nextRoll < sides) {
			return true;
		}
		else {
			bot.sendChannelFormat("%s: the %s says he has no more to teach you.", 
				Color.nick(nick), nameColored());
			bot.peekRoll(nick, sides, true); // Make the roll known -- it is the maximum roll
			return false;
		}
	}
	
	@Override
	public void onPickup() {
		int sides = bot.getPowerupSides(owner);
		int nextRoll = bot.peekRoll(owner, sides, false);
		int bonus = bonus();
		nextRoll += bonus;
		nextRoll = clamp(nextRoll);
		bot.setNextRoll(owner, sides, nextRoll);
		
		if (professor) {
			bot.sendChannelFormat("%s participates in the %s's applied dicetology lecture. " +
				"%s's next roll of d%d is improved by +%d!", 
				owner, nameColored(),  ownerColored, sides, bonus);
		}
		else {
			bot.sendChannelFormat("The %s demonstrates some rolling tricks to %s. " +
				"%s's next roll of d%d is improved by +%d!", 
				nameColored(), owner, ownerColored, sides, bonus);
		}
	}

	private int bonus() {
		if (professor) return profBonus;
		else return proBonus;
	}
	
	@Override
	public String name() {
		if (professor) return "Rolling Professor";
		else return "Rolling Professional";
	}
}