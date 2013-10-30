/*
 * Created on 29.10.2013
 * @author verkel
 */
package noppabot;

import java.io.*;
import java.util.*;

import noppabot.Powerups.BagOfDice;
import noppabot.Powerups.EnchantedDie;
import noppabot.Powerups.FastDie;
import noppabot.Powerups.LuckyDie;
import noppabot.Powerups.MasterDie;
import noppabot.Powerups.PolishedDie;
import noppabot.Powerups.Powerup;
import noppabot.Powerups.PrimalDie;
import noppabot.Powerups.RollingProfessional;
import noppabot.Powerups.SymmetricalDie;
import noppabot.Powerups.VolatileDie;
import noppabot.Powerups.WeightedDie;

public class GenerateItemList {

	private static final double REGULAR_DICE_EV = 50.50d;
	private static final String TESTER_NAME = "Tester";
	public static final File path = new File("index.html");
	
	private List<TestResult> results = new ArrayList<TestResult>();
	private Random rnd = new Random();
//	private static final int iterations = 1000000; // test
//	private static final int iterations = 10000000; // good accuracy
	private static final int iterations = 100000000; // excellent accuracy
	
	class TestResult implements Comparable<TestResult> {
		public String name;
		public String image;
		public String description;
		public double ev;
		public double sd;
		
		public TestResult(String name, String image, String description, double ev, double sd) {
			this.name = name;
			this.image = image;
			this.description = description;
			this.ev = ev;
			this.sd = sd;
		}

		@Override
		public int compareTo(TestResult o) {
			double val = this.ev;
			double other = o.ev;
			return -(val < other ? -1 : (val == other ? 0 : 1));
		}
		
		public String getEv() {
			if (ev < 0) return "None";
			else return String.format(Locale.US, "%.2f", ev);
		}
		
		public String getSd() {
			if (sd < 0) return "None";
			else return String.format(Locale.US, "%.2f", sd);
		}
	}
	
	public static void main(String[] args) throws IOException {
		new GenerateItemList().run();
	}

	private void run() throws IOException {
		System.out.print("Computing ranking list");
		
		StringBuilder buf = new StringBuilder();
		buf.append("<html>\n");
		buf.append("<head><title>Item list</title></head>\n");
		buf.append("<body>\n");
		
		appendCSS(buf);
		
		buf.append("<h2>Item list</h2>\n");
		buf.append("<p><b>EV</b> = Expected value, <b>SD</b> = Standard deviation</p>\n");
		buf.append("<table>\n");
		buf.append("<tr><th>Item</th><th>Name</th><th>EV</th><th>SD</th>" +
			"<th class=\"desc\">Description</th>\n");
		
		testBasicPowerups();
		Collections.sort(results);
		
		for (TestResult result : results) {
			appendResult(result, buf);
		}
		
		buf.append("</table>\n").append("</body>\n").append("</html>\n");
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(path));
		writer.append(buf);
		writer.close();
		
		System.out.print(" Done!");
	}

	private void appendCSS(StringBuilder buf) throws IOException {
		buf.append("<style type=\"text/css\">\n");
		BufferedReader r = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("style.css")));
		String line;
		while ((line = r.readLine()) != null) {
			buf.append(line).append("\n");
		}
		r.close();
		buf.append("</style>\n");
	}
	
	class RegularDie extends Powerup {

		@Override
		public String getName() {
			return "Regular Die";
		}
	}

	private void testBasicPowerups() {
		testBasicPowerup("Regular die", "RegularDie.png", regularDieDesc, new RegularDie(), testBot);
		testBasicPowerup("Polished die", "PolishedDie.png", polishedDieDesc, new PolishedDie(), testBot);
		testBasicPowerup("Weighted die", "WeightedDie.png", weightedDieDesc, new WeightedDie(), testBot);
		testBasicPowerup("Enchanted die", "EnchantedDie.png", enchantedDieDesc, new EnchantedDie(), testBot);
		testBasicPowerup("Primal die", "PrimalDie.png", primalDieDesc, new PrimalDie(), testBot);
		testBasicPowerup("Lucky die", "LuckyDie.png", luckyDieDesc, new LuckyDie(), testBot);
		testBasicPowerup("Master die", "MasterDie.png", masterDieDesc, new MasterDie(), testBot);
		testBasicPowerup("Fast die<br><small>(rolled at 00:00:00)</small>", "FastDie.png",
			fastDieDesc, new FastDie(), testBot);
		testBasicPowerup("Fast die<br><small>(rolled randomly within 00:01)</small>", "FastDie.png",
			fastDieDesc, new FastDie(), new LazyBot());
		testBasicPowerup("Volatile die", "VolatileDie.png", volatileDieDesc, new VolatileDie(), testBot);
		testBasicPowerup("Symmetrical die", "SymmetricalDie.png", symmetricalDieDesc,
			new SymmetricalDie(), testBot);
		testBagOfDice();
		testGroundhogDie();
		testBasicPowerup("Rolling professional", "RollingProfessional.png", rollingProfessionalDesc,
			new RollingProfessional(), testBot);
		testDiceteller();
		testBasicPowerup("RollerBot", "RollerBot.png", rollerBotDesc, new RegularDie(), testBot);
		testDicePirate();
	}
	
	private void testDicePirate() {
		results.add(new TestResult("Dice pirate", "DicePirate.png", dicePirateDesc, -1, -1));
		System.out.print('.');
	}

	private void testBasicPowerup(String name, String image, String desc, Powerup powerup, TestBot bot) {
		double ev = computePowerupEV(powerup, bot);
		System.out.print('.');
		double sd = computePowerupSD(powerup, bot, ev);
		System.out.print('.');
		TestResult result = new TestResult(name, image, desc, ev, sd);
		results.add(result);
	}
	
	private double computePowerupEV(Powerup powerup, TestBot bot) {
		double sum = 0;
		for (int i = 0; i < iterations; i++) {
			int roll = bot.getRollFor(TESTER_NAME, 100);
			roll = powerup.onContestRoll(bot, TESTER_NAME, roll);
			sum += roll;
		}
		return sum / (double)iterations;
	}
	
	private double computePowerupSD(Powerup powerup, TestBot bot, double ev) {
		double sum = 0;
		for (int i = 0; i < iterations; i++) {
			int roll = bot.getRollFor(TESTER_NAME, 100);
			roll = powerup.onContestRoll(bot, TESTER_NAME, roll);
			double diff = roll - ev;
			sum += diff*diff;
		}
		return Math.sqrt(sum / (double)iterations);
	}
	
	private void testBagOfDice() {
		double iterations = GenerateItemList.iterations / 10;
		
		// EV
		double sum = 0;
		for (int i = 0; i < iterations; i++) {
			Powerup powerup = new BagOfDice();
			powerup.onSpawn(testBot);
			int roll = testBot.getRollFor(TESTER_NAME, 100);
			roll = powerup.onContestRoll(testBot, TESTER_NAME, roll);
			sum += roll;
		}
		double ev = sum / (double)iterations;
		System.out.print('.');
		
		// SD
		sum = 0;
		for (int i = 0; i < iterations; i++) {
			Powerup powerup = new BagOfDice();
			powerup.onSpawn(testBot);
			int roll = testBot.getRollFor(TESTER_NAME, 100);
			roll = powerup.onContestRoll(testBot, TESTER_NAME, roll);
			double diff = roll - ev;
			sum += diff*diff;
		}
		double sd = Math.sqrt(sum / (double)iterations);
		System.out.print('.');
		
		results.add(new TestResult("Bag of dice", "BagOfDice.png", bagOfDiceDesc, ev, sd));
	}
	
	private void testDiceteller() {
		// EV
		double sum = 0;
		for (int i = 0; i < iterations; i++) {
			int roll = testBot.getRollFor(TESTER_NAME, 100);
			if (roll < REGULAR_DICE_EV) roll = testBot.getRollFor(TESTER_NAME, 100);
			sum += roll;
		}
		double ev = sum / (double)iterations;
		System.out.print('.');
		
		// SD
		sum = 0;
		for (int i = 0; i < iterations; i++) {
			int roll = testBot.getRollFor(TESTER_NAME, 100);
			if (roll < REGULAR_DICE_EV) roll = testBot.getRollFor(TESTER_NAME, 100);
			double diff = roll - ev;
			sum += diff*diff;
		}
		double sd = Math.sqrt(sum / (double)iterations);
		System.out.print('.');
		
		results.add(new TestResult("Diceteller", "Diceteller.png", dicetellerDesc, ev, sd));
		System.out.print('.');
	}
	
	private void testGroundhogDie() {
		// EV
		double sum = 0;
		int lastRoll = 0, lastLastRoll = 0;
		for (int i = 0; i < iterations; i++) {
			int roll;
			if (lastRoll == lastLastRoll || lastRoll < REGULAR_DICE_EV) roll = testBot.getRollFor(TESTER_NAME, 100);
			else roll = lastRoll;
			sum += roll;
			lastLastRoll = lastRoll;
			lastRoll = roll;
		}
		double ev = sum / (double)iterations;
		System.out.print('.');
		
		// SD
		sum = 0;
		lastRoll = 0; lastLastRoll = 0;
		for (int i = 0; i < iterations; i++) {
			int roll;
			if (lastRoll == lastLastRoll || lastRoll < REGULAR_DICE_EV) roll = testBot.getRollFor(TESTER_NAME, 100);
			else roll = lastRoll;
			double diff = roll - ev;
			sum += diff * diff;
			lastLastRoll = lastRoll;
			lastRoll = roll;
		}
		double sd = Math.sqrt(sum / (double)iterations);
		System.out.print('.');
		
		results.add(new TestResult("Groundhog die", "GroundhogDie.png", groundhogDieDesc, ev, sd));
		System.out.print('.');
	}
	
	private void appendResult(TestResult r, StringBuilder buf) {
		buf.append("<tr>\n");
		buf.append("<td>").append("<img src=\"").append("items/").append(r.image).append("\"> ").append("</td>\n");
		buf.append("<td class=\"big bold\">").append(r.name).append("</td>\n");
		buf.append("<td class=\"big bold\">").append(r.getEv()).append("</td>\n");
		buf.append("<td class=\"big\">").append(r.getSd()).append("</td>\n");
		buf.append("<td class=\"desc\">").append(r.description).append("</td>\n");
		buf.append("</tr>\n");
	}
	
	private TestBot testBot = new TestBot();
	
	class TestBot implements INoppaBot {
		
		@Override
		public void sendDefaultContestRollMessage(String nick, int value) {
		}
		
		@Override
		public void sendChannelFormat(String msg, Object... args) {
		}
		
		@Override
		public void sendChannel(String msg) {
		}
		
		@Override
		public void participate(String nick, int rollValue) {
		}
		
		@Override
		public RollRecords loadRollRecords() {
			return null;
		}
		
		@Override
		public String grade(int value) {
			return null;
		}
		
		@Override
		public int getRollFor(String nick, int sides) {
			return rnd.nextInt(sides)+1;
		}
		
		@Override
		public Map<String, Powerup> getPowerups() {
			return null;
		}
		
		@Override
		public String getDefaultContestRollMessage(String nick, int value) {
			return null;
		}

		@Override
		public int getSecondsAfterMidnight() {
			return 0;
		}

		@Override
		public void sendMessage(String nick, String msg) {
		}

		@Override
		public void sendMessageFormat(String nick, String msg, Object... args) {
		}

		@Override
		public boolean participated(String nick) {
			return false;
		}

		@Override
		public Map<String, Integer> getRolls() {
			return null;
		}
	}
	
	class LazyBot extends TestBot {
		@Override
		public int getSecondsAfterMidnight() {
			return rnd.nextInt(60);
		}
	}
	
	private static final String regularDieDesc = "Regular die is the d100.";
	private static final String dicePirateDesc = "Dice pirate steals item from another contestant.";
	private static final String rollerBotDesc = "Roller bot rolls automatically when the contest starts.";
	private static final String luckyDieDesc = "Lucky die gives a +25 bonus if the roll contains any sevens.";
	private static final String bagOfDiceDesc = "Bag of dice contains ten random dice ranging from d4 to d20. Your roll result is the summed result from throwing all the dice.";
	private static final String primalDieDesc = "Primal die gives a +20 bonus if the roll is a prime.";
	private static final String polishedDieDesc = "Polished die gives a +5 bonus.";
	private static final String fastDieDesc = "Fast die gives a bonus of 30 - (seconds passed since the contest started). After 30 seconds the bonus will be 0.";
	private static final String groundhogDieDesc = "Groundhog die repeats your last roll.";
	private static final String weightedDieDesc = "Weighted die gives a +10 bonus.";
	private static final String rollingProfessionalDesc = "Rolling professional ensures your roll is at least 50.";
	private static final String dicetellerDesc = "Diceteller tells what your next roll will be.";
	private static final String enchantedDieDesc = "Enchanted die gives a +15 bonus.";
	private static final String volatileDieDesc = "After the initial roll, the volatile die may reroll itself. The chance for reroll is 100% - (lastRoll)%. The roll after which the volatile die stops is your result.";
	private static final String symmetricalDieDesc = "Symmetrical die flips rolls less than 50 into 100 - roll.";
	private static final String masterDieDesc = "Master die lets you roll d200 (the result is capped into 100).";
}
