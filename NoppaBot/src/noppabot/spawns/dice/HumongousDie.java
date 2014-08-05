/*
 * Created on 13.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import java.io.UnsupportedEncodingException;

import noppabot.*;
import noppabot.spawns.*;
import noppabot.spawns.evolved.CrushingDie;

import org.gnu.jfiglet.FIGDriver;
import org.gnu.jfiglet.core.*;


public class HumongousDie extends BasicDie {
	
	private static FIGDriver figDriver;
	private static FIGFont font;
	
	static {
      try {
      	font = new FIGFont("standard");
			font.loadFromFile();
			figDriver = new FIGDriver();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static final BasicPowerupSpawnInfo info = new BasicPowerupSpawnInfo() {

		@Override
		public BasicPowerup create() {
			return new HumongousDie();
		}
		
		@Override
		public double spawnChance() {
			return 0.33;
		}
	};
	
	@Override
	public SpawnInfo<?> spawnInfo() {
		return info;
	}
	
	@Override
	public void onSpawn() {
		bot.sendChannelFormat("A %s APPEARS!", nameColored());
	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... THE HUMONGOUS DIE COLLAPSES UNDER ITS OWN WEIGHT");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat("%s GRABS THE %s", ownerColored.toUpperCase(), nameColored());
	}

	@Override
	public DiceRoll onContestRoll() {
		return doContestRoll(this, owner);
	}
	
	public static DiceRoll doContestRoll(Powerup self, String nick) {
		DiceRoll roll = roll(self);
		sendFigletText(self.bot(), String.format("%s  rolls  %s !", nick, roll));
		return roll;
	}

	private static DiceRoll roll(Powerup self) {
		DiceRoll roll;
		if (self instanceof BasicDie) roll = ((BasicDie)self).roll();
		else roll = ((EvolvedDie)self).roll();
		return roll;
	}
	
	private static void sendFigletText(INoppaBot bot, String text) {
		try {
			FIGure figure = figDriver.getBannerFIGure(text, font);
			for (String line : figure.getSubcharactersLines()) {
				bot.sendChannel(line);
			}
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String name() {
		return "HUMONGOUS DIE";
	}
	
	@Override
	public int sides() {
		return 100;
	}
	
	@Override
	public boolean isUpgradeable() {
		return true;
	}
	
	@Override
	public Powerup upgrade() {
		return new CrushingDie(this, true);
	}
}
