/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.instants;

import noppabot.*;
import noppabot.spawns.*;


public class RollingProfessional extends Instant {

	private static final int proBonus = 5;
	private static final int profBonus = 10;

	private boolean professor;
	
	public static final InstantSpawnInfo info = new InstantSpawnInfo() {

		@Override
		public Instant create() {
			return new RollingProfessional();
		}
		
	};
	
	@Override
	public SpawnInfo<?> spawnInfo() {
		return info;
	}
	
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
	public boolean canPickUp(String nick, boolean verbose) {
		int sides = bot.getPowerupSides(nick);
		DiceRoll nextRoll = bot.peekRoll(nick, sides).value;
		if (nextRoll.total() < sides) {
			return true;
		}
		else {
			if (verbose) bot.sendChannelFormat("%s: the %s says he has no more to teach you.", 
				Color.nick(nick), nameColored());
			return false;
		}
	}
	
	@Override
	public void onPickup() {
		int sides = bot.getPowerupSides(owner);
		DiceRoll nextRoll = bot.peekRoll(owner, sides).value;
		int bonus = bonus();
		nextRoll = nextRoll.addVisibleBonus(bonus).clamp(sides);
		bot.setNextRoll(owner, sides, nextRoll);
		
		String bonusStr = Color.visibleRollBonus("+" + bonus);
		if (professor) {
			bot.sendChannelFormat("%s participates in the %s's applied dicetology lecture. " +
				"%s's next roll of d%s is improved by %s!", 
				owner, nameColored(),  ownerColored, sides, bonusStr);
		}
		else {
			bot.sendChannelFormat("The %s demonstrates some rolling tricks to %s. " +
				"%s's next roll of d%s is improved by %s!", 
				nameColored(), owner, ownerColored, sides, bonusStr);
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