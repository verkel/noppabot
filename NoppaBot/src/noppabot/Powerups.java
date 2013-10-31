/*
 * Created on 27.10.2013
 * @author verkel
 */
package noppabot;

import java.util.*;
import java.util.Map.Entry;

public class Powerups {

	private static Random powerupRnd = new Random();

	private static final Set<Integer> primes = new HashSet<Integer>();
	private static final List<Integer> dice;
	
	private static int lastPowerupIndex = -1;

	static {
		primes.addAll(Arrays.asList(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59,
			61, 67, 71, 73, 79, 83, 89, 97));
		List<Integer> lowDice = Arrays.asList(4, 6, 8, 10, 12, 20);
		dice = new ArrayList<Integer>();
		dice.addAll(lowDice);
		dice.add(100);
	}

	public static Powerup getRandom(NoppaBot bot) {
		Powerup powerup = doGetRandom(bot);
		powerup.initialize(bot);
		return powerup;
	}
	
	private static Powerup doGetRandom(NoppaBot bot) {
		// Have a chance to spawn some apprentice dies if there is a master die
		// Don't spawn these otherwise
		int masterDieCount = bot.countPowerups(MasterDie.class);
		if (masterDieCount > 0) {
			int apprenticeDieCount = bot.countPowerups(ApprenticeDie.class);
			if (powerupRnd.nextFloat() < 0.30f / (1 + apprenticeDieCount)) {
				return new ApprenticeDie();
			}
		}
		
		// Prevent two same powerups in a row
		int rnd;
		do { rnd = powerupRnd.nextInt(16); }
		while (rnd == lastPowerupIndex);
		lastPowerupIndex = rnd;

		switch (rnd) {
			case 0: return new PolishedDie();
			case 1: return new WeightedDie();
			case 2: return new EnchantedDie();
			case 3: return new PrimalDie();
			case 4: return new LuckyDie();
			case 5: return new MasterDie();
			case 6: return new FastDie();
			case 7: return new VolatileDie();
			case 8: return new ExtremeDie();
			case 9: return new GroundhogDie();
			case 10: return new BagOfDice();
			case 11: return new RollingProfessional();
			case 12: return new Diceteller();
			case 13: return new RollerBot();
			case 14: return new DicePirate();
			case 15: return new SecretDocument();
		}

		throw new RuntimeException("Illegal powerup random index");
	}

	public static abstract class Powerup {
		
		public abstract String getName();

		public void initialize(INoppaBot bot) {
		}
		
		/**
		 * Say something when the item spawns. Don't initialize the item here, as
		 * it won't be called when a dice pirate generates a new item.
		 */
		public void onSpawn(INoppaBot bot) {
		}

		public void onPickup(INoppaBot bot, String nick) {
		}

		public void onExpire(INoppaBot bot) {
		}

		public void onRollPeriodStart(INoppaBot bot, String nick) {
		}
		
		public void onTiebreakPeriodStart(INoppaBot bot, String nick) {
		}

		public int onNormalRoll(INoppaBot bot, String nick, int roll) {
			return roll;
		}

		/**
		 * Say the appropriate roll message and return the modified roll
		 * 
		 * @return a modified roll
		 */
		public int onContestRoll(INoppaBot bot, String nick, int roll) {
			bot.sendDefaultContestRollMessage(nick, roll);
			return roll;
		}

		@Override
		public String toString() {
			return getName();
		}
	}

	public static class PolishedDie extends Powerup {

		public static final int bonus = 5;

		@Override
		public void onSpawn(INoppaBot bot) {
			bot.sendChannel("A polished die appears!");
		}

		@Override
		public void onPickup(INoppaBot bot, String nick) {
			bot.sendChannelFormat("%s grabs the polished die.", nick);
		}

		@Override
		public void onExpire(INoppaBot bot) {
			bot.sendChannelFormat("... the polished die fades away.");
		}

		@Override
		public int onContestRoll(INoppaBot bot, String nick, int roll) {
			int result = roll + bonus;
			result = capResult(result);
			bot.sendChannelFormat("The polished die adds a nice bonus to %s's roll.", nick);
			bot.sendChannelFormat("%s rolls %d + %d = %d! %s", nick, roll, bonus, result,
				bot.grade(result));
			return result;
		}

		@Override
		public String getName() {
			return "Polished Die";
		}
	}

	public static class WeightedDie extends Powerup {

		public static final int bonus = 10;

		@Override
		public void onSpawn(INoppaBot bot) {
			bot.sendChannel("A weighted die appears!");
		}

		@Override
		public void onExpire(INoppaBot bot) {
			bot.sendChannelFormat("... the weighted die falls into emptiness.");
		}

		@Override
		public void onPickup(INoppaBot bot, String nick) {
			bot.sendChannelFormat("%s grabs the weighted die.", nick);
		}

		@Override
		public int onContestRoll(INoppaBot bot, String nick, int roll) {
			int result = roll + bonus;
			result = capResult(result);
			bot.sendChannelFormat(
				"%s's weighted die is so fairly weighted, that the roll goes very smoothly!", nick);
			bot.sendChannelFormat("%s rolls %d + %d = %d! %s", nick, roll, bonus, result,
				bot.grade(result));
			return result;
		}

		@Override
		public String getName() {
			return "Weighted Die";
		}
	}

	public static class EnchantedDie extends Powerup {

		public static final int bonus = 15;

		@Override
		public void onSpawn(INoppaBot bot) {
			bot.sendChannel("An enchanted die appears!");
		}

		@Override
		public void onExpire(INoppaBot bot) {
			bot.sendChannelFormat("... the spell on the enchanted die expires and nobody really wants it anymore.");
		}

		@Override
		public void onPickup(INoppaBot bot, String nick) {
			bot.sendChannelFormat(
				"%s grabs the enchanted die and feels a tingling sensation at the fingertips.", nick);
		}

		@Override
		public int onContestRoll(INoppaBot bot, String nick, int roll) {
			int result = roll + bonus;
			result = capResult(result);
			bot.sendChannelFormat(
				"The enchanted die grants %s either eternal fame and fortune, or a substantial roll bonus. %s chooses the latter.",
				nick, nick);
			bot.sendChannelFormat("%s rolls %d + %d = %d! %s", nick, roll, bonus, result,
				bot.grade(result));
			return result;
		}

		@Override
		public String getName() {
			return "Enchanted Die";
		}
	}

	public static class PrimalDie extends Powerup {

		public static final int bonus = 20;

		@Override
		public void onSpawn(INoppaBot bot) {
			bot.sendChannel("A primal die appears!");
		}

		@Override
		public void onExpire(INoppaBot bot) {
			bot.sendChannelFormat("... nobody wanted the primal die. Now it disappears.");
		}

		@Override
		public void onPickup(INoppaBot bot, String nick) {
			bot.sendChannelFormat("%s grabs the primal die and gains knowledge about prime numbers.",
				nick);
		}

		@Override
		public int onContestRoll(INoppaBot bot, String nick, int roll) {
			if (primes.contains(roll)) {
				int result = roll + bonus;
				result = capResult(result);
				bot.sendChannelFormat(
					"%s rolls %d, which is a prime! The primal die is pleased, and adds a bonus to the roll. The final roll is %d + %d = %d.",
					nick, roll, roll, bonus, result);
				return result;
			}
			else {
				bot.sendChannelFormat(
					"%s rolls %d! It's not a prime, however, and the primal die disapproves.", nick,
					roll);
				return roll;
			}
		}

		@Override
		public String getName() {
			return "Primal Die";
		}
	}

	public static class LuckyDie extends Powerup {

		public static final int bonus = 25;

		@Override
		public void onSpawn(INoppaBot bot) {
			bot.sendChannel("A lucky die appears!");
		}

		@Override
		public void onExpire(INoppaBot bot) {
			bot.sendChannelFormat("... the lucky die rolls away.");
		}

		@Override
		public void onPickup(INoppaBot bot, String nick) {
			bot.sendChannelFormat(
				"%s grabs the lucky die and it wishes good luck for tonight's roll.", nick);
		}

		@Override
		public int onContestRoll(INoppaBot bot, String nick, int roll) {
			if (String.valueOf(roll).contains("7")) {
				int result = roll + bonus;
				result = capResult(result);
				bot.sendChannelFormat(
					"%s rolls %d! The lucky die likes sevens in numbers, so it tinkers with your roll, making it %d + %d = %d. Lucky!",
					nick, roll, roll, bonus, result);
				return result;
			}
			else {
				bot.sendChannelFormat(
					"%s rolls %d! The lucky die doesn't seem to like this number, though.", nick, roll);
				return roll;
			}
		}

		@Override
		public String getName() {
			return "Lucky Die";
		}
	}

	public static class MasterDie extends Powerup {

		@Override
		public void onSpawn(INoppaBot bot) {
			bot.sendChannel("The MASTER DIE appears!");
		}

		@Override
		public void onExpire(INoppaBot bot) {
			bot.sendChannelFormat("... nobody took the MASTER DIE?! Maybe you should learn some dice appraisal skills at the certified rolling professional.");
		}

		@Override
		public void onPickup(INoppaBot bot, String nick) {
			bot.sendChannelFormat(
				"%s wrestles for the MASTER DIE with the other contestants and barely comes on top. Surely, the MASTER DIE must be worth all the trouble!",
				nick);
		}

		@Override
		public int onContestRoll(INoppaBot bot, String nick, int roll) {
			int result = bot.getRollFor(nick, 200);
			result = capResult(result);
			bot.sendChannelFormat("%s rolls d200 with the MASTER DIE...", nick);
			bot.sendDefaultContestRollMessage(nick, result);
			rollUnusedApprenticeDies(bot, result);
			return result;
		}
		
		private void rollUnusedApprenticeDies(INoppaBot bot, int roll) {
			Map<String, Powerup> powerups = bot.getPowerups();
			for (Entry<String, Powerup> entry : powerups.entrySet()) {
				String owner = entry.getKey();
				Powerup powerup = entry.getValue();
				if (powerup instanceof ApprenticeDie && !bot.participated(owner)) {
					bot.sendChannelFormat("%s's apprentice die observes and then rolls %d.", owner, roll);
					// Don't use participate so we wont end the contest on overtime
					// and forget the master die's roll
					bot.getRolls().put(owner, roll);
				}
			}
		}

		@Override
		public String getName() {
			return "MASTER DIE";
		}
	}

	public static class FastDie extends Powerup {

		@Override
		public void onSpawn(INoppaBot bot) {
			bot.sendChannel("The fast die appears!");
		}

		@Override
		public void onExpire(INoppaBot bot) {
			bot.sendChannelFormat("... it was too fast for you.");
		}

		@Override
		public void onPickup(INoppaBot bot, String nick) {
			bot.sendChannelFormat(
				"%s quickly grabs the fast die! It asks you if you can throw it even faster when the time comes.",
				nick);
		}

		@Override
		public int onContestRoll(INoppaBot bot, String nick, int roll) {
			int seconds = bot.getSecondsAfterMidnight();
			int bonus = Math.max(0, 30 - seconds);
			int result = roll + bonus;
			result = capResult(result);
			bot.sendChannelFormat("%s waited %d seconds before rolling. The fast die awards +%d speed bonus!", 
				nick, seconds, bonus);
			bot.sendChannelFormat("%s rolls %d + %d = %d! %s", nick, roll, bonus, result, bot.grade(result));
			return result;
		}

		@Override
		public String getName() {
			return "Fast Die";
		}
	}

	public static class VolatileDie extends Powerup {

		@Override
		public void onSpawn(INoppaBot bot) {
			bot.sendChannel("The volatile die appears!");
		}

		@Override
		public void onExpire(INoppaBot bot) {
			bot.sendChannelFormat("... the volatile die grows unstable and explodes.");
		}

		@Override
		public void onPickup(INoppaBot bot, String nick) {
			bot.sendChannelFormat("%s grabs the volatile die! It glows with an aura of uncertainty.",
				nick);
		}

		@Override
		public int onContestRoll(INoppaBot bot, String nick, int roll) {
			bot.sendChannelFormat("%s rolls with the volatile die. %d! %s", nick, roll, bot.grade(roll));
			
			while (getRerollRnd() > roll) {
				roll = bot.getRollFor(nick, 100);
				bot.sendChannelFormat("%s's volatile die keeps on rolling! %d! %s", nick, roll, bot.grade(roll));
			}
			
			return roll;
		}
		
		private int getRerollRnd() {
			return powerupRnd.nextInt(100)+1;
		}

		@Override
		public String getName() {
			return "Volatile Die";
		}
	}

	public static class ExtremeDie extends Powerup {

		@Override
		public void onSpawn(INoppaBot bot) {
			bot.sendChannel("The extreme die appears!");
		}

		@Override
		public void onExpire(INoppaBot bot) {
			bot.sendChannelFormat("... the extreme die gets bored sitting still and takes off.");
		}

		@Override
		public void onPickup(INoppaBot bot, String nick) {
			bot.sendChannelFormat(
				"%s grabs the extreme die! You discuss about various subcultures popular with radical dice.",
				nick);
		}

		@Override
		public int onContestRoll(INoppaBot bot, String nick, int roll) {
			if (roll == 100) {
				bot.sendChannelFormat("%s rolls %d with the extreme die! That's the most extreme roll and the die is ecstatic!", nick, roll); 
				return roll;
			}
			else if (roll > 10 && roll < 90) {
				bot.sendChannelFormat("%s rolls %d with the extreme die! This number is quite ordinary, says the die.", nick, roll);
				return roll;
			}
			else {
				bot.sendChannelFormat("%s rolls %d with the extreme die! This number is very extremal! says the die.", nick, roll);
				bot.sendChannelFormat("The extreme die rewards %s with the most extreme roll, 100!", nick);
				return 100;
			}
		}

		@Override
		public String getName() {
			return "Extreme Die";
		}
	}

	public static class BagOfDice extends Powerup {

		private List<Integer> diceBag = new ArrayList<Integer>();

		private void grabSomeDice() {
			Random diceRnd = new Random();
			int count = 1 + diceRnd.nextInt(8);
			
			for (int i = 0; i < count; i++) {
				int die = dice.get(diceRnd.nextInt(dice.size()));
				diceBag.add(die);
			}
			Collections.sort(diceBag);
		}

		private String diceToString() {
			StringBuilder buf = new StringBuilder();
			boolean first = true;
			for (int die : diceBag) {
				if (!first) buf.append(", ");
				buf.append("d" + die);
				first = false;
			}
			return buf.toString();
		}

		@Override
		public void initialize(INoppaBot bot) {
			grabSomeDice();
		}
		
		@Override
		public void onSpawn(INoppaBot bot) {
			bot.sendChannel("A bag of dice appears!");
		}

		@Override
		public void onPickup(INoppaBot bot, String nick) {
			bot.sendChannelFormat(
				"%s grabs the bag! It should be okay to use them tonight, just this once...", nick);
		}

		@Override
		public void onExpire(INoppaBot bot) {
			bot.sendChannelFormat("... who wants some random dice bag anyway. There could be anything in there.");
		}

		@Override
		public int onContestRoll(INoppaBot bot, String nick, int roll) {
			bot.sendChannelFormat("%s opens the dice bag. It contains the following dice: %s", nick, diceToString());

			StringBuilder buf = new StringBuilder();
			boolean first = true;
			int result = 0;
			for (int die : diceBag) {
				int subroll = bot.getRollFor(nick, die);
				result += subroll;
				if (!first) buf.append(" + ");
				buf.append(subroll);
				first = false;
			}
			String resultStr = buf.toString();
			result = capResult(result);

			bot.sendChannelFormat("%s rolls with the dice. %s = %d", nick, resultStr, result);
//			System.out.println(result);
			return result;
		}
		
		@Override
		public String getName() {
			return "Bag of Dice";
		}
	}
	
	public static class GroundhogDie extends Powerup {

		@Override
		public void onSpawn(INoppaBot bot) {
			bot.sendChannel("A groundhog die appears!");
		}

		@Override
		public void onExpire(INoppaBot bot) {
			bot.sendChannelFormat("... the groundhog die will now move on.");
		}

		@Override
		public void onPickup(INoppaBot bot, String nick) {
			bot.sendChannelFormat("%s grabs the groundhog die and ensures that the history will repeat itself.", nick);
		}
		
		@Override
		public int onContestRoll(INoppaBot bot, String nick, int roll) {
			Integer lastRoll = null;
			RollRecords records = bot.loadRollRecords();
			if (records != null) {
				lastRoll = records.getOrAddUser(nick).lastRolls.peekFirst();
			}
			
			if (lastRoll != null && lastRoll > 0) {
				bot.sendChannelFormat("%s throws the groundhog die with a familiar motion.", nick);
				bot.sendChannelFormat("%s rolls %d! %s", nick, lastRoll, bot.grade(lastRoll));
				return lastRoll;
			}
			else {
				bot.sendChannel("The groundhog die fails to repeat yesterday's events.");
				return super.onContestRoll(bot, nick, roll); // Normal behaviour
			}
		}

		@Override
		public String getName() {
			return "Groundhog Die";
		}
	}
	
	public static class RollingProfessional extends Powerup {

		public static final int minRoll = 50;

		@Override
		public void onSpawn(INoppaBot bot) {
			bot.sendChannel("A certified rolling professional appears!");
		}

		@Override
		public void onExpire(INoppaBot bot) {
			bot.sendChannelFormat("... nobody seems to want to polish their skills, so the rolling professional walks away.");
		}

		@Override
		public void onPickup(INoppaBot bot, String nick) {
			bot.sendChannelFormat("The rolling professional will assist %s in tonight's roll.", nick);
		}

		@Override
		public int onContestRoll(INoppaBot bot, String nick, int roll) {
			if (roll >= minRoll) {
				bot.sendDefaultContestRollMessage(nick, roll);
				bot.sendChannelFormat(
					"The rolling professional compliments %s for the proper rolling technique.", nick);
				return roll;
			}
			else {
				bot.sendChannelFormat(
					"The rolling professional notices that %s's rolling technique needs work! "
						+ "%s was about to roll a lowly %d, but rolling pro shows how it's done and rolls %d!",
					nick, nick, roll, minRoll);
				return minRoll;
			}
		}

		@Override
		public String getName() {
			return "Rolling Professional";
		}
	}

	public static class Diceteller extends Powerup {

		private int stashedRoll;
		private boolean rerolled = false;

		@Override
		public void onSpawn(INoppaBot bot) {
			bot.sendChannel("A Diceteller appears!");
		}

		@Override
		public void onExpire(INoppaBot bot) {
			bot.sendChannelFormat("... no one seemed to believe the diceteller could see future rolls in a crystal ball. The ball was of wrong shape, anyway. Maybe next time she brings crystal dice?");
		}

		@Override
		public void onPickup(INoppaBot bot, String nick) {
			stashedRoll = bot.getRollFor(nick, 100);
			bot.sendChannelFormat(
				"The diceteller, glancing at his crystal ball, whispers to %s: \"Your next roll will be %d.\"",
				nick, stashedRoll);
		}

		@Override
		public int onNormalRoll(INoppaBot bot, String nick, int roll) {
			rerolled = true;
			
			int result = stashedRoll;
			stashedRoll = roll;
			
			return result;
		}

		@Override
		public int onContestRoll(INoppaBot bot, String nick, int roll) {
			int result = stashedRoll;
			stashedRoll = roll;

			bot.sendChannelFormat("%s rolls %d! %s", nick, result, bot.grade(result));
			if (!rerolled) {
				bot.sendChannelFormat("%s recalls this was the exact roll foretold by the Diceteller.",
					nick);
			}
			
			return result;
		}

		@Override
		public String getName() {
			return "Diceteller";
		}
	}
	
	public static class RollerBot extends Powerup {

		@Override
		public void onSpawn(INoppaBot bot) {
			bot.sendChannel("A RollerBot appears!");
		}

		@Override
		public void onExpire(INoppaBot bot) {
			bot.sendChannelFormat("... the RollerBot breaks down.");
		}

		@Override
		public void onPickup(INoppaBot bot, String nick) {
			bot.sendChannelFormat("The RollerBot will automatically roll for %s tonight.", nick);
		}
		
		@Override
		public void onRollPeriodStart(INoppaBot bot, String nick) {
			int roll = bot.getRollFor(nick, 100);
			bot.sendChannelFormat("%s's RollerBot rolls %d! %s", nick, roll, bot.grade(roll));
			bot.participate(nick, roll);
		}

		@Override
		public String getName() {
			return "RollerBot";
		}
	}

	public static class DicePirate extends Powerup {

		@Override
		public void onSpawn(INoppaBot bot) {
			bot.sendChannel("A DicePirate appears!");
		}

		@Override
		public void onExpire(INoppaBot bot) {
			bot.sendChannelFormat("... the DicePirate smells better loot elsewhere and leaves.");
		}

		@Override
		public void onPickup(INoppaBot bot, String nick) {
			bot.sendChannelFormat("The DicePirate will plunder dice and other shiny things for 100 gold dubloons! %s gladly pays him.", nick, nick);
			Random rnd = new Random();
			int size = bot.getPowerups().size();
			int itemIndex = rnd.nextInt(size);
			Set<String> owners = new TreeSet<String>(bot.getPowerups().keySet());
			owners.remove(nick);
			int i = 0;
			String targetOwner = null;
			for (String owner : owners) {
				if (i == itemIndex) {
					targetOwner = owner;
					break;
				}
				i++;
			}
			
			if (targetOwner == null) {
				bot.sendChannelFormat("There was no loot in sight for the DicePirate and he just runs off with your gold.");
			}
			else {
				Powerup stolenPowerup = bot.getPowerups().remove(targetOwner);
				try {
					stolenPowerup = stolenPowerup.getClass().newInstance();
					stolenPowerup.initialize(bot);
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
				bot.getPowerups().put(nick, stolenPowerup);
				bot.sendChannelFormat("The dice pirate looted %s's %s!", targetOwner, stolenPowerup);
				if (stolenPowerup instanceof Diceteller) stolenPowerup.onPickup(bot, nick);
			}
		}

		@Override
		public String getName() {
			return "DicePirate";
		}
	}
	
	public static class ApprenticeDie extends Powerup {

		@Override
		public void onSpawn(INoppaBot bot) {
			bot.sendChannel("An apprentice die appears!");
		}

		@Override
		public void onExpire(INoppaBot bot) {
			bot.sendChannelFormat("... the apprentice die will find no masters today.");
		}

		@Override
		public void onPickup(INoppaBot bot, String nick) {
			bot.sendChannelFormat("%s grabs the apprentice die and it looks forward to learning from the MASTER DIE.", nick);
		}
		
		@Override
		public int onContestRoll(INoppaBot bot, String nick, int roll) {
			bot.sendChannelFormat("%s rolls with the apprentice die, but it really just wanted " +
				"to see the MASTER DIE do it, first.", nick);
			return super.onContestRoll(bot, nick, roll); // Normal behaviour
		}
		
		@Override
		public void onTiebreakPeriodStart(INoppaBot bot, String nick) {
			bot.sendChannelFormat("%s's apprentice die has learned enough and evolves into a MASTER DIE!", nick);
			bot.getPowerups().put(nick, new MasterDie());
		}

		@Override
		public String getName() {
			return "Apprentice Die";
		}
	}
	
	public static class SecretDocument extends Powerup {

		@Override
		public void onSpawn(INoppaBot bot) {
			bot.sendChannel("A secret document appears!");
		}

		@Override
		public void onExpire(INoppaBot bot) {
			bot.sendChannelFormat("... wind blows the secret document away.");
		}

		@Override
		public void onPickup(INoppaBot bot, String nick) {
			bot.sendChannelFormat("%s reads the secret document, and looks puzzled.", nick);
			bot.sendMessage(nick, "You open the secret document. It contains the following information:");
			String msg = "R3VyIHBiemN5cmdyIHl2ZmcgYnMgbmFwdnJhZyBxdnByIG5lZ3ZzbnBnZiBwbmEgb3Igc2Jo" +
				"YXEgbmcgdWdnYzovL2lyZXhyeS52eHYuc3YvY2hveXZwL3p2ZnAvYmVjX2VieXlmL3ZncnpmLyAh";
			bot.sendMessage(nick, msg);
			bot.sendMessage(nick, "Maybe it is in the long-forgotten language of Noppa Elders? " +
				"You feel that if you could translate it, it could give you significant competetive advantage!");
		}
		
		@Override
		public String getName() {
			return "Secret Document";
		}
	}

	private static int capResult(int roll) {
		return Math.min(100, roll);
	}
}
