/*
 * Created on 29.10.2013
 * @author verkel
 */
package noppabot;

import java.io.*;
import java.util.*;

import noppabot.NoppaBot.ExpireTask;
import noppabot.NoppaBot.SpawnTask;
import noppabot.spawns.*;
import noppabot.spawns.dice.*;

public class GenerateItemList {

	private static final double REGULAR_DICE_EV = 50.50d;
	private static final String TESTER_NAME = "Tester";
	public static final File outputPath = new File("index.html");
	public static final File cachePath = new File("results.cache");
	
	private List<TestResult> results = new ArrayList<TestResult>();
	private Random rnd = new Random();
//	private static final int iterations = 1000000; // test
//	private static final int iterations = 10000000; // good accuracy
	private static final int iterations = 100000000; // excellent accuracy
	
	public Map<String, TestResult> resultsCache;
	private Mode mode;
	
	private static class TestResult implements Comparable<TestResult>, Serializable {
		public DiceType diceType;
		public String name;
		public String image;
		public String description;
		public double ev;
		public double sd;
		public String evolvesTo;
		
		public TestResult(DiceType diceType, String name, String image, String description, String evolvesTo, double ev, double sd) {
			this.diceType = diceType;
			this.name = name;
			this.image = image;
			this.description = description;
			this.evolvesTo = evolvesTo;
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
	
	public enum Mode {
		COMPUTE, COMPUTE_AND_CACHE, LOAD_CACHED
	}
	
	public static void main(String[] args) throws Exception {
		Mode mode = Mode.COMPUTE;
		String arg;
		if (args.length == 0) {
			System.out.print("Input action (compute, cache, load -- nothing for compute): ");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			arg = br.readLine();
			br.close();
		}
		else {
			arg = args[0];
		}
		
		if (arg.equals("cache")) mode = Mode.COMPUTE_AND_CACHE;
		else if (arg.equals("load")) mode = Mode.LOAD_CACHED;
		
		new GenerateItemList().run(mode);
	}

	@SuppressWarnings("unchecked")
	private void run(Mode mode) throws IOException, ClassNotFoundException {
		this.mode = mode;
		if (mode == Mode.COMPUTE) System.out.print("Computing ranking list");
		else if (mode == Mode.COMPUTE_AND_CACHE) {
			System.out.print("Computing & caching ranking list");
			resultsCache = new TreeMap<String, TestResult>();
		}
		else if (mode == Mode.LOAD_CACHED) {
			System.out.print("Loading cached ranking list");
			ObjectInputStream stream = new ObjectInputStream(new FileInputStream(cachePath));
			resultsCache = (TreeMap<String, TestResult>)stream.readObject();
			stream.close();
		}
		
		StringBuilder buf = new StringBuilder();
		buf.append("<html>\n");
		buf.append("<head><title>Item list</title></head>\n");
		buf.append("<body>\n");
		
		appendCSS(buf);
		
		buf.append("\n\n");
		buf.append("<h2>Basic Dice</h2>\n");
		buf.append("<p>Basic dice are items that boost your rolling powers. They spawn on " +
			"the channel at random times, and you can take them with the \"grab\" command. " +
			"Grabbing a die takes up your item slot &mdash; you can only have one at a time.</p>\n");
		buf.append("<p><b>EV</b> = Expected value, <b>SD</b> = Standard deviation</p>\n");
		buf.append("<table id='basicdice' class='itemtable'>\n");
		buf.append("<tr><th>Item</th><th>Name</th><th>Evolves To</th><th>EV</th><th>SD</th>" +
			"<th class=\"desc\">Description</th>\n");
		testBasicDice();
		Collections.sort(results);
		appendResults(buf);
		results.clear();
		buf.append("</table>\n");

		buf.append("<h2>Evolved Dice</h2>\n");
		buf.append("<p>These dice are evolved from the basic die you carry at the moment by grabbing " +
			"the Dicemon Trainer or by the Dice Mutation event. They don't spawn on the channel. " +
			"They are generally more powerful than the basic dice.</p>\n");
		buf.append("<table id='evolveddice' class='itemtable'>\n");
		buf.append("<tr><th>Item</th><th>Name</th><th>EV</th><th>SD</th>" +
		"<th class=\"desc\">Description</th>\n");
		testEvolvedDice();
		Collections.sort(results);
		appendResults(buf);
		results.clear();
		buf.append("</table>\n");
		
		buf.append("<h2>Instants</h2>\n");
		buf.append("<p>Instants are items (or people) which spawn on the channel and can be grabbed " +
			"like basic dice. They differ from basic dice in that their effect is activated " +
			"instantly, and not during the roll contest. They vanish after use and don't take up " +
			"your item slot.</p>\n");
		buf.append("<table id='instants' class='itemtable'>\n");
		buf.append("<tr><th>Item</th><th>Name</th><th>EV</th><th>SD</th>" +
			"<th class=\"desc\">Description</th>\n");
		listInstants();
		Collections.sort(results);
		appendResults(buf);
		buf.append("</table>\n");
		
		buf.append("<h2>Events</h2>\n");
		buf.append("<p>Events are occurrances which automatically happen without player interaction. " +
			"Events are much rarer than regular item spawns.</p>\n");
		buf.append("<table id='events' class='itemtable'>\n");
		buf.append("<tr><th>Event</th><th>Name</th>" +
		"<th class=\"desc\">Description</th>\n");
		listEvents(buf);
		buf.append("</table>\n");
		
		buf.append("\n\n");
		buf.append("</body>\n").append("</html>\n");
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));
		writer.append(buf);
		writer.close();
		
		if (mode == Mode.COMPUTE_AND_CACHE) {
			ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(cachePath));
			stream.writeObject(resultsCache);
			stream.close();
		}
		
		System.out.print(" Done!");
	}

	private void appendEvent(String name, String image, String desc, StringBuilder buf) {
		if (desc == undiscovered) buf.append("<tr class='undiscovered'>\n");
		else buf.append("<tr>\n");
		buf.append("<td>").append("<img src=\"").append("items/").append(image).append("\"> ").append("</td>\n");
		buf.append("<td class=\"name big bold\">").append(name).append("</td>\n");
		buf.append("<td class=\"desc\">").append(desc).append("</td>\n");
		buf.append("</tr>\n");
	}

	private void appendResults(StringBuilder buf) {
		for (TestResult result : results) {
			appendResult(result, buf);
		}
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
	
	class RegularDie extends BasicPowerup {

		@Override
		public String name() {
			return "Regular Die";
		}

		@Override
		public int sides() {
			return 100;
		}
	}

	private void testBasicDice() {
		testBasicPowerup("Regular Die", "RegularDie.png", regularDieDesc, "Nothing", new RegularDie(), bot);
		testBasicPowerup("Polished Die", "PolishedDie.png", polishedDieDesc, "Very Polished Die", new PolishedDie(), bot);
		testBasicPowerup("Weighted Die", "WeightedDie.png", weightedDieDesc, "Crushing Die", new WeightedDie(), bot);
		testBasicPowerup("Enchanted Die", "EnchantedDie.png", enchantedDieDesc, "Potent Die", new EnchantedDie(), bot);
		testBasicPowerup("Primal Die", "PrimalDie.png", primalDieDesc, "Tribal Die", new PrimalDie(), bot);
		testBasicPowerup("Lucky Die", "LuckyDie.png", luckyDieDesc, "Jackpot Die", new LuckyDie(), bot);
		testBasicPowerup("Master Die", "MasterDie.png", masterDieDesc, "The One Die", new MasterDie(), bot);
		testBasicPowerup("Fast Die<br><small>(rolled within 10 s)</small>", "FastDie.png",
			fastDieDesc, "Faster Die", new FastDie(), bot);
		testBasicPowerup("Extreme Die", "ExtremeDie.png", extremeDieDesc, "Daring Die", new ExtremeDie(), bot);
		
		testPowerup("Bag of Dice", "BagOfDice.png", bagOfDiceDesc, "Bag of Many Dice", new Builder() {
			@Override
			public Powerup createPowerup() {
				BagOfDice bag = new BagOfDice();
				bag.initialize(bot);
				return bag;
			};
		}, bot, false, DiceType.BASIC);
		
		addEntry("Groundhog Die", "GroundhogDie.png", groundhogDieDesc, "Self-Improving Die");
		testBasicPowerup("Rolling Professional", "RollingProfessional.png", rollingProfessionalDesc,
			"Rolling Professor", new RollingProfessional(), bot);

		addEntry("Apprentice Die", "ApprenticeDie.png", apprenticeDieDesc, "Master Die");
		testBasicPowerup("Dice Bros.", "Placeholder.png", diceBrosDesc, "Super Dice Bros", new DiceBros(), bot);
		
		testPowerup("Variable Die", "Placeholder.png", variableDieDesc, "Chaos Die", new Builder() {
			@Override
			public Powerup createPowerup() {
				VariableDie die = new VariableDie();
				die.initialize(bot);
				return die;
			};
		}, bot, false, DiceType.BASIC);
		
		// Figlet runs take time so we don't want to actually test the humongous die
		testBasicPowerup("Humongous Die", "Placeholder.png", humongousDieDesc, "Humongous Crushing Die", new RegularDie(), bot);
	}
	
	private void testEvolvedDice() {
		testEvolvedPowerup("Very Polished Die", "Placeholder.png", veryPolishedDieDesc, new PolishedDie().upgrade(), bot);
		testEvolvedPowerup("Crushing Die", "Placeholder.png", crushingDieDesc, new WeightedDie().upgrade(), bot);
		testEvolvedPowerup("Potent Die", "Placeholder.png", potentDieDesc, new EnchantedDie().upgrade(), bot);
		testEvolvedPowerup("Tribal Die", "Placeholder.png", tribalDieDesc, new PrimalDie().upgrade(), bot);
		testEvolvedPowerup("Jackpot Die", "Placeholder.png", jackpotDieDesc, new LuckyDie().upgrade(), bot);
		testEvolvedPowerup("The One Die", "Placeholder.png", theOneDieDesc, new MasterDie().upgrade(), bot);
		testEvolvedPowerup("Faster Die<br><small>(rolled immediately)</small>", "Placeholder.png",
			fasterDieDesc, new FastDie().upgrade(), bot);
		testEvolvedPowerup("Daring Die", "Placeholder.png", daringDieDesc, new ExtremeDie().upgrade(), bot);

		testPowerup("Bag of Many Dice", "Placeholder.png", bagOfManyDiceDesc, null, new Builder() {
			@Override
			public Powerup createPowerup() {
				BagOfDice bag = new BagOfDice();
				bag.initialize(bot);
				return bag.upgrade();
			};
		}, bot, false, DiceType.EVOLVED);
		
		addEntry("Self-Improving Die", "Placeholder.png", selfImprovingDieDesc, null);
		testEvolvedPowerup("Rolling Professor", "Placeholder.png", rollingProfessorDesc,
			new RollingProfessional().upgrade(), bot);

		testPowerup("Super Dice Bros", "Locked.png", superDiceBrosDesc, null, new Builder() {
			@Override
			public Powerup createPowerup() {
				DiceBros bros = new DiceBros();
				bros.initialize(bot);
				return bros.upgrade();
			};
		}, bot, false, DiceType.EVOLVED);
		
		testPowerup("Chaos Die", "Placeholder.png", chaosDieDesc, null, new Builder() {
			@Override
			public Powerup createPowerup() {
				VariableDie die = new VariableDie();
				die.initialize(bot);
				return die.upgrade();
			};
		}, bot, false, DiceType.EVOLVED);
		
		// Figlet runs take time so we don't want to actually test the humongous die
		testEvolvedPowerup("Humongous Crushing Die", "Placeholder.png", humongousCrushingDieDesc, new RegularDie(), bot); //new HumongousDie().upgrade(), bot);
	}
	
	private void listInstants() {
		addEntry("Dicemon Trainer", "Placeholder.png", dicemonTrainerDesc, null);
		addEntry("Dice Pirate", "DicePirate.png", dicePirateDesc, null);
		addEntry("Dice Recycler", "Placeholder.png", diceRecyclerDesc, null);
		testDiceteller();
		addEntry("Trolling Professional", "Placeholder.png", trollingProfessionalDesc, null);
	}
	
	private void listEvents(StringBuilder buf) {
		appendEvent("Dice Mutation", "Event.png", diceMutationDesc, buf);
		appendEvent("Dice Storm", "Event.png", diceStormDesc, buf);
		appendEvent("Fourth Wall Breaks", "Event.png", fourthWallBreaksDesc, buf);
		appendEvent("Rules Change", "Event.png", rulesChangeDesc, buf);
	}
	
	
	abstract class Builder {
		public abstract Powerup createPowerup();
	}
	
	class SingleInstance extends Builder {
		private Powerup powerup;

		public SingleInstance(Powerup powerup) {
			this.powerup = powerup;
			powerup.initialize(bot);
		}

		@Override
		public Powerup createPowerup() { 
			return powerup;
		}
	}
	
	private void addEntry(String name, String image, String description, String evolvesTo) {
		results.add(new TestResult(null, name, image, description, evolvesTo, -1, -1));
		System.out.print('.');
	}
	
	enum DiceType {
		BASIC, EVOLVED
	}
	
	private void testBasicPowerup(String name, String image, String desc, String evolvesTo, Powerup powerup, TestBot bot) {
		testPowerup(name, image, desc, evolvesTo, new SingleInstance(powerup), bot, false, DiceType.BASIC);
	}
	
	private void testEvolvedPowerup(String name, String image, String desc, Powerup powerup, TestBot bot) {
		testPowerup(name, image, desc, null, new SingleInstance(powerup), bot, false, DiceType.EVOLVED);
	}
	
	private void testPowerup(String name, String image, String desc, String evolvesTo, 
		Builder builder, TestBot bot, boolean printRolls, DiceType diceType) {
		
		double ev, sd;
		if (mode == Mode.LOAD_CACHED) {
			TestResult cachedResult = resultsCache.get(name);
			ev = cachedResult.ev;
			sd = cachedResult.sd;
		}
		else {
			ev = computePowerupEV(builder, bot, printRolls);
			System.out.print('.');
			sd = computePowerupSD(builder, bot, ev);
			System.out.print('.');
		}
		TestResult result = new TestResult(diceType, name, image, desc, evolvesTo, ev, sd);
		results.add(result);
		if (mode == Mode.COMPUTE_AND_CACHE) resultsCache.put(name, result);
	}
	
	private double computePowerupEV(Builder builder, TestBot bot, boolean printRolls) {
		double sum = 0;
		for (int i = 0; i < iterations; i++) {
			Powerup powerup = builder.createPowerup();
			int roll = powerup.onContestRoll();
			if (printRolls) System.out.println(roll);
			sum += roll;
		}
		return sum / iterations;
	}
	
	private double computePowerupSD(Builder builder, TestBot bot, double ev) {
		double sum = 0;
		for (int i = 0; i < iterations; i++) {
			Powerup powerup = builder.createPowerup();
			int roll = powerup.onContestRoll();
			double diff = roll - ev;
			sum += diff*diff;
		}
		return Math.sqrt(sum / iterations);
	}
	
	private void testDiceteller() {
		
		String name = "Diceteller";
		double ev, sd;
		if (mode == Mode.LOAD_CACHED) {
			TestResult cachedResult = resultsCache.get(name);
			ev = cachedResult.ev;
			sd = cachedResult.sd;
		}
		else {
			// EV
			double sum = 0;
			for (int i = 0; i < iterations; i++) {
				int roll = bot.getRoll(TESTER_NAME, 100);
				if (roll < REGULAR_DICE_EV) roll = bot.getRoll(TESTER_NAME, 100);
				sum += roll;
			}
			ev = sum / iterations;
			System.out.print('.');
			
			// SD
			sum = 0;
			for (int i = 0; i < iterations; i++) {
				int roll = bot.getRoll(TESTER_NAME, 100);
				if (roll < REGULAR_DICE_EV) roll = bot.getRoll(TESTER_NAME, 100);
				double diff = roll - ev;
				sum += diff*diff;
			}
			sd = Math.sqrt(sum / iterations);
			System.out.print('.');
		}
		
		TestResult result = new TestResult(null, name, "Diceteller.png", dicetellerDesc, null, ev, sd);
		results.add(result);
		if (mode == Mode.COMPUTE_AND_CACHE) resultsCache.put(name, result);
		System.out.print('.');
	}
	
	private void appendResult(TestResult r, StringBuilder buf) {
		if (r.description == undiscovered) buf.append("<tr class='undiscovered'>\n");
		else buf.append("<tr>\n");
		buf.append("<td>").append("<img src=\"").append("items/").append(r.image).append("\"> ").append("</td>\n");
		String anchor = r.name;
		if (anchor.startsWith("Faster Die")) anchor = "Faster Die"; // Fix subtitle leaking into anchor
		String nameContent = String.format("<a name='%s'>%s</a>", anchor, r.name);
		buf.append("<td class='name big bold'>").append(nameContent).append("</td>\n");
		if (r.evolvesTo != null) {
			String content = r.evolvesTo.equals("Nothing") ? "Nothing" : 
				String.format("<a href='#%s'>%s</a>", r.evolvesTo, r.evolvesTo);
			buf.append("<td class='evolves'>").append(content).append("</td>\n");
		}
		buf.append("<td class=\"big bold\">").append(r.getEv()).append("</td>\n");
		buf.append("<td class=\"big\">").append(r.getSd()).append("</td>\n");
		buf.append("<td class=\"desc\">").append(r.description).append("</td>\n");
		buf.append("</tr>\n");
	}
	
	private TestBot bot = new TestBot();
	
	class TestBot implements INoppaBot {
		private Rules rules = new Rules(this);
		
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
		public int getRoll(String nick, int sides) {
			return rnd.nextInt(sides)+1;
		}
		
		@Override
		public Map<String, Powerup> getPowerups() {
			return Collections.emptyMap();
		}
		
		@Override
		public int getSecondsAfterPeriodStart() {
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
		public String remainingSpawnsInfo() {
			return null;
		}

		@Override
		public void insertApprenticeDice() {
		}

		@Override
		public List<String> getRandomPowerupOwners() {
			return null;
		}

		@Override
		public SpawnTask scheduleSpawn(Calendar spawnTime, ISpawnable spawn) {
			return null;
		}

		@Override
		public Calendar getRollPeriodStartTime() {
			return null;
		}

		@Override
		public Calendar getSpawnEndTime() {
			return null;
		}

		@Override
		public Rules getRules() {
			return rules;
		}

		@Override
		public void onRulesChanged() {
		}

		@Override
		public Rolls getRolls() {
			return null;
		}

		@Override
		public int clampRoll(int roll) {
			return 0;
		}

		@Override
		public String rollToString(int roll) {
			return null;
		}

		@Override
		public SpawnTask scheduleRandomSpawn(Calendar spawnTime,
			Spawner<BasicPowerup> allowedPowerups, Spawner<Event> allowedEvents) {
			return null;
		}

		@Override
		public String rollToString(int roll, boolean colorRoll) {
			return null;
		}

		@Override
		public void sendDefaultContestRollMessage(String nick, int value, boolean colorNick,
			boolean colorRoll) {
		}

		@Override
		public String getDefaultContestRollMessage(String nick, int value, boolean colorNick,
			boolean colorRoll) {
			return null;
		}

		@Override
		public int doNormalRoll(String nick, int sides) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int peekRoll(String nick, int sides) {
			return 0;
		}

		@Override
		public ExpireTask scheduleExpire(Powerup powerup, Calendar expireTime) {
			return null;
		}
	}
	
	class LazyBot extends TestBot {
		@Override
		public int getSecondsAfterPeriodStart() {
			return rnd.nextInt(60);
		}
	}
	
	private static final String regularDieDesc = "Regular die is the d100.";
	private static final String dicePirateDesc = "Steals item from another contestant.";
	private static final String luckyDieDesc = "Gives a +25 bonus if the roll contains any sevens.";
	private static final String bagOfDiceDesc = "Contains 1 to 8 random dice ranging from d4 to d100." +
		" Your roll result is the summed result from throwing all the dice.";
	private static final String primalDieDesc = "Gives a +20 bonus if the <a href=\"http://www.prime-numbers.net/prime-numbers-1-100-chart.html\">roll is a prime</a>.";
	private static final String polishedDieDesc = "Gives a +5 bonus.";
	private static final String fastDieDesc = "Gives a bonus of 20 if you roll during the first 10 seconds. After that, the bonus will decrease by 1 per second.";
	private static final String groundhogDieDesc = "Repeats your yesterday's roll.";
	private static final String weightedDieDesc = "Gives a +10 bonus.";
	private static final String rollingProfessionalDesc = "Ensures your roll is at least 50.";
	private static final String dicetellerDesc = "Tells what your next roll will be.";
	private static final String enchantedDieDesc = "Gives a +15 bonus.";
	private static final String extremeDieDesc = "Changes rolls 1..10 and 90..99 into 100.";
	private static final String masterDieDesc = "Lets you roll the d150 (the result is capped into 100).";
	private static final String apprenticeDieDesc = "After a master die is rolled, the apprentice " +
		"die will roll the same result. If the apprentice die ends up in the tiebreaker round, it turns into a master die.";
	
	private static final String undiscovered = "<span class='undiscovered'>Undiscovered!</span>";
	
	private static final String diceBrosDesc = "Rolls two d100 dice and chooses higher roll as the result.";
	private static final String variableDieDesc = "Is randomly one of d80, d90, d100, ..., d150, d160.";
	private static final String dicemonTrainerDesc = "Evolves your current die into a more powerful die.";
	private static final String diceRecyclerDesc = "Trashes your current die and spawns a random new one to the ground, for anyone to grab.";
	private static final String trollingProfessionalDesc = "On pickup, creates a random item spawn which is actually a bomb. The person who picked " +
			"the trolling professional is informed which item is rigged with the bomb. The bomb has the name of a regular item; " +
			"there is 50% chance this name contains a typo. If somebody picks up the bomb, it will cause d10 + 10 damage to the contest roll.";
	private static final String humongousDieDesc = "Your opponents are intimidated by the mere sight of it.";
	
	private static final String diceMutationDesc = "Some of the currently owned items mutate into more powerful dice. " +
		"The number of mutated dice is random, and ranges from 1 to all owned dice.";
	private static final String diceStormDesc = "Spawns from 3 to 5 new items at once.";
	private static final String fourthWallBreaksDesc = "Reveals all of the events that are yet to happen today.";
	private static final String rulesChangeDesc = "Randomly <a href='#rulechanges'>changes one rule</a> for the next rolling contest.";
	
	private static final String veryPolishedDieDesc = "It has +10 further bonus, for a total of +15. May be upgraded infinitely for additional +10 bonuses.";
	private static final String crushingDieDesc = "Loses the roll bonus, but now deals d30 damage to others' rolls.";
	private static final String potentDieDesc = "Gives a +20 bonus.";
	private static final String tribalDieDesc = "In addition to primal die's effect, you get +10 bonus for every prime rolled by an opponent.";
	private static final String jackpotDieDesc = "Gives a +40 bonus if the roll contains any sevens";
	private static final String theOneDieDesc = "Lets you roll the d200 (the result is capped into 100).";
	private static final String daringDieDesc = "You roll the d30. Changes rolls 1..10 into 100.";
	private static final String bagOfManyDiceDesc = "Two additional dice are put into the dice bag. May be upgraded infinitely for more dice.";
	private static final String selfImprovingDieDesc = "Repeats your yesterday's roll +10.";
	private static final String superDiceBrosDesc = undiscovered; // = "The dice bros. get random powerups each.";
	private static final String chaosDieDesc = "Triggers a rules change. If the die is weaker than d100, the lowest roll will win tonight. If the die is stronger than d100, the roll cap of 0..100 is lifted. If the die is the d100, the roll closest to a random number will win tonight.";
	private static final String humongousCrushingDieDesc = "Deals d10 + 20 damage to others' rolls.";
	private static final String fasterDieDesc = "Gives you a 30 bonus if you roll immediately. The bonus decreases by 1 per second waited.";
	private static final String rollingProfessorDesc = "Ensures your roll is at least 70.";
}
