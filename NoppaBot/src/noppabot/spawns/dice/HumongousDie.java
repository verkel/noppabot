/*
 * Created on 13.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import java.io.UnsupportedEncodingException;

import noppabot.INoppaBot;
import noppabot.spawns.dice.WeightedDie.CrushingDie;

import org.gnu.jfiglet.FIGDriver;
import org.gnu.jfiglet.core.*;


public class HumongousDie extends Powerup {
	
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
	public void onSpawn(INoppaBot bot) {
		bot.sendChannel("A HUMONGOUS DIE APPEARS!");
	}

	@Override
	public void onExpire(INoppaBot bot) {
		bot.sendChannelFormat("... THE HUMONGOUS DIE COLLAPSES UNDER ITS OWN WEIGHT");
	}

	@Override
	public void onPickup(INoppaBot bot, String nick) {
		bot.sendChannelFormat("%s GRABS THE HUMONGOUS DIE", nick.toUpperCase());
	}

	@Override
	public int onContestRoll(INoppaBot bot, String nick, int roll) {
		return doContestRoll(bot, nick, roll);
	}
	
	public static int doContestRoll(INoppaBot bot, String nick, int roll) {
		sendFigletText(bot, String.format("%s  rolls  %d !", nick, roll));
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
		return 0.25f;
	}
	
	@Override
	public boolean isUpgradeable() {
		return true;
	}
	
	@Override
	public Powerup upgrade() {
		return new CrushingDie(true);
	}
}
