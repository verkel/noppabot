/*
 * Created on 13.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import java.io.UnsupportedEncodingException;

import noppabot.INoppaBot;
import noppabot.spawns.*;
import noppabot.spawns.evolved.CrushingDie;

import org.gnu.jfiglet.FIGDriver;
import org.gnu.jfiglet.core.*;


public class HumongousDie extends BasicPowerup {
	
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
	
	@Override
	public void onSpawn() {
		bot.sendChannelFormat("A %s APPEARS!", getNameColored());
	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... THE HUMONGOUS DIE COLLAPSES UNDER ITS OWN WEIGHT");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat("%s GRABS THE %s", ownerColored.toUpperCase(), getNameColored());
	}

	@Override
	public int onContestRoll() {
		return doContestRoll(this, owner);
	}
	
	public static int doContestRoll(Powerup self, String nick) {
		int roll = self.roll();
		sendFigletText(self.getBot(), String.format("%s  rolls  %d !", nick, roll));
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
	public String getName() {
		return "HUMONGOUS DIE";
	}
	
	
	@Override
	public float getSpawnChance() {
		return 0.33f;
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
