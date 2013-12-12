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
	
	public static final Spawner<Powerup> allPowerups;
	public static final Spawner<Powerup> diceStormPowerups;
	public static final Spawner<Event> allEvents;
	public static final Spawner<Event> allEventsMinusFourthWall;
	
//	private static final NavigableSet<Powerup> upgradeablePowerups = new TreeSet<Powerup>();
	
//	private static int lastPowerupIndex = -1;
//	private static int lastEventIndex = -1;
	

	static {
		primes.addAll(Arrays.asList(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59,
			61, 67, 71, 73, 79, 83, 89, 97));
		
		List<Integer> lowDice = Arrays.asList(4, 6, 8, 10, 12, 20);
		dice = new ArrayList<Integer>();
		dice.addAll(lowDice);
		dice.add(100);
		
		List<Powerup> allPowerupsList = new ArrayList<Powerup>();
		List<Powerup> diceStormPowerupsList = new ArrayList<Powerup>();
		List<Event> allEventsList = new ArrayList<Event>();
		List<Event> allEventsMinusFourthWallList = new ArrayList<Event>();
		
		// Apprentice die is not put here, but can be spawned regardless if there are master dies
		allPowerupsList.addAll(Arrays.asList(new BagOfDice(), new DicemonTrainer(),
			new DicePirate(), new Diceteller(), new EnchantedDie(), new ExtremeDie(), new FastDie(),
			new GroundhogDie(), new LuckyDie(), new MasterDie(), new PolishedDie(), new PrimalDie(),
			new RollingProfessional(), new VolatileDie(), new WeightedDie()));

//		upgradeablePowerups.addAll(allPowerupsList);
//		upgradeablePowerups.removeAll(Arrays.asList(new DicemonTrainer(), new DicePirate(), new Diceteller(), new GroundhogDie()));
		
		diceStormPowerupsList.addAll(allPowerupsList);
		diceStormPowerupsList.removeAll(Arrays.asList(new DicemonTrainer(), new DicePirate()));
		
		allEventsList.addAll(Arrays.asList(new CounterfeitDice(), new DiceMutation(), new DiceStorm(), new FourthWallBreaks()));
		
		allEventsMinusFourthWallList.addAll(allEventsList);
		allEventsMinusFourthWallList.remove(new FourthWallBreaks());
		
		allPowerups = new Spawner<Powerup>(allPowerupsList);
		diceStormPowerups = new Spawner<Powerup>(diceStormPowerupsList);
		allEvents = new Spawner<Event>(allEventsList);
		allEventsMinusFourthWall = new Spawner<Event>(allEventsMinusFourthWallList);
	}

	public static Object getRandomPowerupOrEvent(NoppaBot bot, Spawner<Powerup> spawnPowerups, Spawner<Event> spawnEvents) {
		if (spawnEvents != null && powerupRnd.nextFloat() < 0.08f) {
			return getRandomEvent(spawnEvents);
		}
		else {
			return getRandomPowerup(bot, spawnPowerups);
		}
	}
	
	public static Powerup getRandomPowerup(NoppaBot bot, Spawner<Powerup> spawnPowerups) {
		Powerup powerup = spawnPowerups.spawn();
		powerup.initialize(bot);
		return powerup;
	}
	
	public static Event getRandomEvent(Spawner<Event> spawnEvents) {
		Event event = spawnEvents.spawn();
		return event;
	}
	
	public static interface ISpawnable {
		public float getSpawnChance();
	}
	
	public static abstract class Powerup implements ISpawnable {
		
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
		
		/**
		 * Should the powerup be put into player's item slot
		 */
		public boolean isCarried() {
			return true;
		}
		
		public boolean canPickUp(INoppaBot bot, String nick) {
			if (isCarried() && bot.getPowerups().containsKey(nick)) {
				bot.sendChannelFormat("%s: you already have the %s.", nick, bot.getPowerups().get(nick));
				return false;
			}
			
			return true;
		}
		
//		/**
//		 * Cost or value of the die
//		 */
//		public abstract int getCost();
		
		@Override
		public float getSpawnChance() {
			return 1;
		}

		@Override
		public String toString() {
			return getName();
		}
		
//		@Override
//		public int compareTo(Powerup p) {
//			return cmp(getCost(), p.getCost());
//		}
//		
//		private static int cmp(int c1, int c2) {
//			return c1 > c2 ? 1 : c1 < c2 ? -1 : 0;
//		}
//		
//		@Override
//		public boolean equals(Object obj) {
//			// This is useful for the upgradeablePowerups set
//			return this.getClass().equals(obj.getClass());
//		}
	}
	
	public static abstract class Event implements ISpawnable {
		public abstract void run(INoppaBot bot);
		
		public abstract String getName();

		@Override
		public String toString() {
			return getName();
		}
		
		@Override
		public float getSpawnChance() {
			return 1;
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
		
		@Override
		public float getSpawnChance() {
			return 0.75f;
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
			
			bot.insertApprenticeDice();
		}

		@Override
		public int onContestRoll(INoppaBot bot, String nick, int roll) {
			int uncappedResult = bot.getRollFor(nick, 200);
			int result = capResult(uncappedResult);
			bot.sendChannelFormat("%s rolls d200 with the MASTER DIE...", nick);
			if (uncappedResult > 100) {
				bot.sendChannelFormat("%s rolls %d (= 100)! %s", nick, uncappedResult, bot.grade(result));
			}
			else bot.sendDefaultContestRollMessage(nick, result);
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
		
		@Override
		public float getSpawnChance() {
			return 0.33f;
		}
	}

	public static class FastDie extends Powerup {

		@Override
		public void onSpawn(INoppaBot bot) {
			bot.sendChannel("The fast die appears!");
		}

		@Override
		public void onExpire(INoppaBot bot) {
			bot.sendChannelFormat("... the fast die was too fast for you.");
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
		
		
		@Override
		public float getSpawnChance() {
			return 0.50f;
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
			bot.sendChannelFormat("%s grabs the groundhog die and ensures that history will repeat itself.", nick);
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
		
		
		@Override
		public float getSpawnChance() {
			return 0.75f;
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

		@Override
		public void onSpawn(INoppaBot bot) {
			bot.sendChannel("A Diceteller appears!");
		}

		@Override
		public void onExpire(INoppaBot bot) {
			bot.sendChannelFormat("... no one seemed to believe the diceteller could see future rolls in a crystal ball. " +
				"The ball was of wrong shape, anyway. Maybe next time she brings crystal dice?");
		}

		@Override
		public void onPickup(INoppaBot bot, String nick) {
			int result = bot.peekRollFor(nick);
			bot.sendChannelFormat(
				"The diceteller, glancing at his crystal ball, whispers to %s: \"Your next roll will be %d.\"",
				nick, result);
		}

		@Override
		public boolean isCarried() {
			return false;
		}
		
		@Override
		public String getName() {
			return "Diceteller";
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
			Map<String, Powerup> powerups = bot.getPowerups();
			bot.sendChannelFormat("The DicePirate will plunder dice and other shiny things for 100 gold dubloons! %s gladly pays him.", nick);
			Random rnd = new Random();
			Set<String> owners = new TreeSet<String>(powerups.keySet());
			int size = owners.size();
			String targetOwner = null;
			if (size > 0) {
				int itemIndex = rnd.nextInt(size);
				int i = 0;
				for (String owner : owners) {
					if (i == itemIndex) {
						targetOwner = owner;
						break;
					}
					i++;
				}
			}
			
			if (targetOwner == null) {
				bot.sendChannelFormat("There was no loot in sight for the DicePirate and he just runs off with your gold.");
				powerups.remove(nick);
			}
			else {
				Powerup stolenPowerup = powerups.remove(targetOwner);
//				stolenPowerup = clonePowerup(bot, stolenPowerup); // onPickup() stuff aren't carried anymore, so no need to clone
				powerups.put(nick, stolenPowerup);
				bot.sendChannelFormat("The dice pirate looted %s's %s!", targetOwner, stolenPowerup);
				if (stolenPowerup instanceof Diceteller) stolenPowerup.onPickup(bot, nick);
			}
		}

		@Override
		public String getName() {
			return "DicePirate";
		}
		
		@Override
		public boolean isCarried() {
			return false;
		}
		
		@Override
		public float getSpawnChance() {
			return 0.75f;
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
	
	public static class DicemonTrainer extends Powerup {
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
				if (upgradeablePowerups.contains(powerup)) {
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
			Powerup newPowerup = upgrade(bot, oldPowerup);
			bot.getPowerups().put(nick, newPowerup);
			bot.sendChannelFormat("The trainer says: \"I will help you to unlock the hidden potential in your dice!\"");
			bot.sendChannelFormat("%s's %s evolved into %s!", nick, oldPowerup, newPowerup);
		}

		@Override
		public String getName() {
			return "Dicemon Trainer";
		}
		
		@Override
		public float getSpawnChance() {
			return 2f;
		}
	}
	
	// DupedDie -- choose the better one of two rolls
	
	// --------------------------------------------------------------------------
	// Dice events (happen automatically without grabbing)
	// --------------------------------------------------------------------------
	
	public static class DiceStorm extends Event {
		@Override
		public void run(INoppaBot bot) {
			bot.sendChannelFormat("Suddenly, a DiceStorm breaks out! It's now raining dice!");
			
			int count = 3 + powerupRnd.nextInt(2);
			for (int i = 0; i < count; i++) {
				bot.scheduleSpawn(null, diceStormPowerups, null);
			}
		}
		
		@Override
		public String getName() {
			return "Dice Storm";
		}
	}

	public static class CounterfeitDice extends Event {
		@Override
		public void run(INoppaBot bot) {
			bot.sendChannelFormat("This just in! Counterfeit dice with fewer sides than advertised " +
				"have been spotted on the market. Buyer beware!");
			
			List<String> owners = getRandomDiceOwners(bot);
			int affected = 0;
			for (String owner : owners) {
				Powerup oldPowerup = bot.getPowerups().get(owner);
				Powerup newPowerup = downgrade(bot, oldPowerup);
				if (newPowerup != null) {
					bot.sendChannelFormat("%s's %s turned out to be the %s...", owner, oldPowerup, newPowerup);
					bot.getPowerups().put(owner, newPowerup);
					affected++;
				}
			}
			
			if (affected == 0) bot.sendChannel("Gladly no-one had those faulty dice.");
		}
		
		@Override
		public String getName() {
			return "Counterfeit Dice";
		}
	}
	
	public static class DiceMutation extends Event {
		@Override
		public void run(INoppaBot bot) {
			bot.sendChannelFormat("Breaking news! The last batch of dice from the PRO Corp. seems to" +
				" have been subjected to gamma radiation during the manufacturing process. Dice owners are" +
				" advised to monitor any sudden changes in their dice!");
			
			List<String> owners = getRandomDiceOwners(bot);
			int affected = 0;
			for (String owner : owners) {
				Powerup oldPowerup = bot.getPowerups().get(owner);
				Powerup newPowerup = upgrade(bot, oldPowerup);
				if (newPowerup != null) {
					bot.sendChannelFormat("%s's %s mutated into the %s!", owner, oldPowerup, newPowerup);
					bot.getPowerups().put(owner, newPowerup);
					affected++;
				}
			}
			
			if (affected == 0) bot.sendChannel("Turns out nothing happened. What did you expect, a Spider-Die?");
		}
		
		@Override
		public String getName() {
			return "Dice Mutation";
		}
	}
	
	public static class FourthWallBreaks extends Event {
		@Override
		public void run(INoppaBot bot) {
			bot.sendChannelFormat("Out of nowhere, a violent wind knocks down the DiceMaster's screen! " +
				"From behind the screen, you catch a glimpse of notes written by the DM:");
			String info = bot.remainingSpawnsInfo();
			if (info.equals("")) info = "... seems that nothing of interest was in the notes!";
			bot.sendChannel(info);
		}
		
		@Override
		public String getName() {
			return "Fourth Wall Breaks";
		}
	}
	
	private static int capResult(int roll) {
		return Math.min(100, roll);
	}
	
	private static List<String> getRandomDiceOwners(INoppaBot bot) {
		ArrayList<String> owners = new ArrayList<String>(bot.getPowerups().keySet());
		Collections.shuffle(owners);
		int removeCount = powerupRnd.nextInt(owners.size());
		return owners.subList(0, owners.size() - removeCount);
	}
}
