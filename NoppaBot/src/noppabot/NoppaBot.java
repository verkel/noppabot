package noppabot;
import it.sauronsoftware.cron4j.*;

import java.io.*;
import java.nio.channels.FileChannel;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.regex.*;

import noppabot.PeekedRoll.Hint;
import noppabot.Rules.RollClosestToTarget;
import noppabot.StringUtils.StringConverter;
import noppabot.spawns.*;
import noppabot.spawns.BasicPowerup.BasicPowerupSpawnInfo;
import noppabot.spawns.Instant.InstantSpawnInfo;
import noppabot.spawns.Spawner.LastSpawn;
import noppabot.spawns.dice.*;
import noppabot.spawns.dice.PokerHand.BetterHand;
import noppabot.spawns.events.*;
import noppabot.spawns.instants.*;
import noppabot.spawns.instants.TrollingProfessional.Bomb;

import org.jibble.pircbot.*;

import adversary.*;
import ca.ualberta.cs.poker.Deck;

import com.google.common.collect.Iterables;

public class NoppaBot extends PircBot implements INoppaBot {

	private final String botNick;
	private final String channel;
	private final String server;
	private final File rollRecordsPath;
	private final boolean debug;
	private final boolean executeDebugStuff;
	private final boolean dryRun;
	private final boolean runAdversary;
	
	public static final Duration ROLL_PERIOD_ABOUT_TO_START_OFFSET = Duration.ofMinutes(-1);
	public static final Duration AUTOROLL_OFFSET = Duration.ofMinutes(5);
	public static final Duration ROLL_PERIOD_DURATION = Duration.ofMinutes(10);
	public static final Duration POWERUP_EXPIRY_DURATION = Duration.ofMinutes(60);
	
	public static LocalTime ROLL_PERIOD_START = LocalTime.of(22, 0);
	public static LocalTime ROLL_PERIOD_ABOUT_TO_START = ROLL_PERIOD_START.plus(ROLL_PERIOD_ABOUT_TO_START_OFFSET);
	public static LocalTime AUTOROLL_TIME = ROLL_PERIOD_START.plus(AUTOROLL_OFFSET);
	public static LocalTime ROLL_PERIOD_END = ROLL_PERIOD_START.plus(ROLL_PERIOD_DURATION);
	
	private Properties properties;
	
	private void loadProperties() throws IOException {
		properties = new Properties();
		BufferedReader r = new BufferedReader(new FileReader("NoppaBot.properties"));
		properties.load(r);
		r.close();
	}
	
	private String[] rollStartMsgs;
	private String[] rollEndMsgs;
	
	private void initMessages() {
		rollStartMsgs = new String[] {
			"Gentlemen, place your rolls!",
			"It's rolltime!",
			"A new day, time for a new roll!",
			"Who will be our lucky winner this time?",
			"Let the rolls commence!",
			"Tonight we select a new certified rolling professional!",
			"The dice are out of the bag!",
			"Experts say that rolling dice makes you 120% more handsome and/or beautiful.",
			"Everyone got their dice ready? Here we go!",
			"Winners do not use drugs. But enchanted dice, they might.",
			"The dice rolling compo starts... now!",
			botNick + "'s the name and rolling is my game!",
			"1. roll, 2. ???, 3. Profit!",
			"Make rolls, not war. Alternatively, wage war with dice.",
			botNick + " rolls 100! SUPER!! ... No wait, you guys roll now.",
			"Kill all audio and non-d100 dice! The rolling compo has begun.",
			"Here we go!",
		};
		
		rollEndMsgs = new String[] {
			"The winner has been decided! It is %s with the roll %s!",
			"Our tonight's champion has been elected! %s scored the mighty %s points!",
			"The score is settled! This time, %s won with the roll %s.",
			"The dice gods have finalized the outcome! %s won with the roll %s!",
			"The contest is over! Our lucky winner is %s with %s points!",
			"The die has been cast. Tonight, %s is the dice emperor with %s points!",
			"%s won the compo this time, with %s points. Was he lucky, or maybe he was using... the MASTER DIE?!",
			"Using dice is the best way to make decisions. %s knows this best, beating others with his roll %s!",
			"Tonight, %s was the high roller with %s points!",
			"%s beat the others with the outstanding roll of %s!",
			"%s is certified as the new local rolling professional after the superb %s roll!",
			"The dice favored %s tonight, granting the victory with %s points!",
			"The demo \"Choose 100 polys and a roll\" by %s entertained all of us tonight. "
			+ "It crushed the competition by a large margin with %s votes!",
			"%s has clicked on lots of candies, cows and cookies, and is experienced in picking the "
			+ "most profitable die, resulting the roll %s!",
		};
	}
	
	public static final Pattern dicePattern = Pattern.compile("^d([0-9]+)");
	public static final Pattern dicePatternWithCustomRoller = Pattern.compile("^d([0-9]+) ([^\\s]+)");
	public static final Pattern commandAndArgument = Pattern.compile("^(\\w+)(?:\\s+(.+)?)?");
	
	// Pircbot onMessage callback and scheduled tasks should synchronize on this lock
	private Object lock = new Object();
	
	private Rules rules;
	private Map<String, PeekableRandom> randoms = new HashMap<String, PeekableRandom>();
	private Random commonRandom = new Random();
	private Scheduler scheduler = new Scheduler();
	private State state = State.NORMAL;
	private Rolls rolls;
	private Set<String> tiebreakers = new TreeSet<String>();
	private NavigableSet<SpawnTask> spawnTasks = new TreeSet<SpawnTask>();
	private Set<ExpireTask> expireTasks = new HashSet<ExpireTask>();
	private SettleTieTimeoutTask settleTieTimeoutTask;
	private SortedSet<Powerup> availablePowerups = new TreeSet<Powerup>(new SpawnableComparator());
	private Map<String, Powerup> powerups = new TreeMap<String, Powerup>();
	private Set<String> favorsUsed = new HashSet<String>();
	private Set<String> autorolls = new HashSet<String>();
	private LocalDateTime lastTiebreakPeriodStartTime;
	private LocalDateTime rollPeriodStartTime;
	private LocalDateTime rollPeriodEndTime;
	private LocalDateTime spawnStartTime;
	private LocalDateTime spawnEndTime;
	private PokerTable pokerTable = new PokerTable(this);
	private List<INoppaEventListener> listeners = new ArrayList<>();
	private Optional<Adversary> adversary = Optional.empty();
	private boolean quitting = false;
	
	public static void main(String[] args) throws Exception {
		new NoppaBot();
	}
	
	
	public NoppaBot() throws Exception {
		super();
		
		loadProperties();
		botNick = properties.getProperty("nick");
		channel = properties.getProperty("channel");
		rollRecordsPath =  new File(properties.getProperty("rollRecordsPath"));
		server = properties.getProperty("server");
		debug = Boolean.parseBoolean(properties.getProperty("debug"));
		executeDebugStuff = Boolean.parseBoolean(properties.getProperty("executeDebugStuff"));
		dryRun = Boolean.parseBoolean(properties.getProperty("dryRun"));
		runAdversary = Boolean.parseBoolean(properties.getProperty("adversary"));
		
		setVerbose(false); // Print stacktraces, but also useless server messages
		setLogin(botNick);
		setName(botNick);
		
		rules = new Rules(this);
		rules.winCondition.addListener(wc -> rolls.onWinConditionChanged());
		rules.spawnOverride.addListener(spawnOverride -> {
			if (spawnOverride.isPresent()) onSpawnerOverridden();
		});
		rules.upgradedSpawns.addListener(upgradedSpawns -> {
			if (upgradedSpawns) onSpawnerOverridden();
		});
		
		rolls = new Rolls(this);
		if (runAdversary) {
			Adversary adv = new Adversary(this);
			adversary = Optional.of(adv);
			listeners.add(adv);
		}
		
		initMessages();
		
		scheduler.schedule(DateTimeUtils.toSchedulingPattern(ROLL_PERIOD_ABOUT_TO_START), new Runnable() {
			@Override
			public void run() {
				synchronized (lock) {
					doTasksBeforeRollPeriodStart();
				}
			}
		});
		
		scheduler.schedule(DateTimeUtils.toSchedulingPattern(ROLL_PERIOD_START), new Runnable() {
			@Override
			public void run() {
				synchronized (lock) {
					startRollPeriod();
				}
			}
		});
		
		scheduler.schedule(DateTimeUtils.toSchedulingPattern(AUTOROLL_TIME), new Runnable() {
			@Override
			public void run() {
				synchronized (lock) {
					autorollFor(autorolls);
				}
			}
		});
		
		scheduler.schedule(DateTimeUtils.toSchedulingPattern(ROLL_PERIOD_END), new Runnable() {
			@Override
			public void run() {
				synchronized (lock) {
					tryEndRollPeriod();
				}
			}
		});
		
		schedulePowerupsOfTheDay();
		scheduler.start();
		
		if (isInRollPeriod()) startRollPeriod();
		
		if (executeDebugStuff) debugStuff();
//		giveFreePowerup(); // spawn one right now
		
		if (dryRun) {
			scheduler.stop();
			System.out.println("Dry run complete. Quitting.");
		}
		else {
			connect(server);
			joinChannel(channel);
			handleConsoleCommands();
		}
	}


	@SuppressWarnings("unused")
	private void debugListCards() {
		int i = 0;
		StringBuilder[] sb = new StringBuilder[4];
		for (int j = 0; j < sb.length; j++) sb[j] = new StringBuilder();
		Deck deck = new Deck();
		while (deck.cardsLeft() > 0) {
			sb[i / 13].append(deck.deal()).append(" ");
			i++;
		}
		for (int j = 0; j < sb.length; j++) sendChannelFormat("Cards %s: %s", j, sb[j]);
	}
	
	private void debugStuff() {
//		for (int i = 0; i < 5; i++) new RulesChange().run(this);
		
		for (int i = 0; i < 10; i++) availablePowerups.add(new RollingProfessional().initialize(this));
		
//		scheduleSpawn(null, new PokerDealer().initialize(this));
		
//		BasicPowerup cards = new PokerCards().initialize(this);
//		cards.setOwner("Verkel");
//		powerups.put("Verkel", cards);
		powerups.put("kessu", new ApprenticeDie().initialize(this));
		powerups.put("frodo", new FastDie().initialize(this));
		powerups.put("bilbo", new MasterDie().initialize(this));
//		powerups.put("ADVERSARY", new WeightedDie().initialize(this).setOwner("ADVERSARY"));
//		powerups.put("PokerPro", new PokerHand().initialize(this));
//		Powerup p = new VariableDie(); p.initialize(this);
//		powerups.put("Verkel", p);
//		powerup = new DicePirate();
//		powerup.onSpawn(this);
//		rolls.put("Verkel", 100);
//		rolls.put("hassu", 78);
//		rolls.put("frodo", 78);
//		rolls.put("bilbo", 61);
//		rolls.put("noob", 3);
//		autorolls.add("hassu");
//		autorolls.add("frodo");
//		autorolls.add("bilbo");
		
//		new RulesChange().run(this);
//		rules.cappedRolls = false;
//		onRulesChanged();
		
//		availablePowerups.add(p);
		availablePowerups.add(new TrollingProfessional().initialize(this));
		availablePowerups.add(new BagOfDice().initialize(this));
		availablePowerups.add(new DicemonTrainer().initialize(this));
		availablePowerups.add(new WeightedDie().initialize(this));
		availablePowerups.add(new BagOfDice().initialize(this));
//		availablePowerups.add(new Diceteller().initialize(this));
//		availablePowerups.add(new HumongousDie().initialize(this));
//		availablePowerups.add(new HumongousDie().initialize(this));
		
//		availablePowerups.add(new ApprenticeDie().initialize(this));
		
//		availablePowerups.add(new BagOfDice().initialize(this));
//		availablePowerups.add(new BagOfDice().initialize(this));
		
//		new FourthWallBreaks().run(this);
		
		spawnAllPowerups();
		
		rules.canDropItems.set(true);
//		onRulesChanged();
		
//		setNextRoll("Verkel", 100, 99);
		
//		grabPowerup("Verkel", PokerDealer.NAME);
		
//		RulesChange.allInfos.get(6).create().run(this);
		new RollPrediction().run(this);
	}
	
	private void spawnAllPowerups() {
		for (BasicPowerup powerup : Powerups.allPowerups) {
			powerup.initialize(this);
			availablePowerups.add(powerup);
		}
	}
	
	private void handleConsoleCommands() {
		System.out.printf("Joined as %s to channel %s\n", botNick, channel);
		
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
			String input;
			boolean quit = false;
			while (!quit && (input = r.readLine()) != null) {
				Matcher commandMatcher = commandAndArgument.matcher(input);
				if (!commandMatcher.matches()) continue;
				String cmd = commandMatcher.group(1);
				String args = commandMatcher.group(2);
				
				synchronized (lock) {
					quit = handleConsoleCommand(cmd, args);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean handleConsoleCommand(String cmd, String args) {
		if (cmd.equals("quit")) {
			System.out.println("Quitting");
			if (args != null) quit(args);
			else quit("Goodbye!");
			return true;
		}
		else if (cmd.equals("help")) {
			commandsHelp();
		}
		else if (cmd.equals("startperiod")) {
			System.out.println("Starting roll period");
			startRollPeriod();
		}
		else if (cmd.equals("endperiod")) {
			System.out.println("Ending roll period");
			tryEndRollPeriod();
		}
		else if (cmd.equals("freebie")) {
			giveFreePowerup();
		}
		else if (cmd.equals("autoroll")) {
			if (args == null) System.out.println("Need nick as an argument");
			else {
				sendChannelFormat("Manually setting autoroll for %s", args);
				autorollFor(args);
			}
		}
		else if (cmd.equals("hurry")) {
			spawnNextScheduledPowerup();
		}
		else if (cmd.equals("adversary")) {
			adversary.ifPresent(adv -> adv.handleConsoleCommand(args));
		}
		else {
			System.out.println("Unknown command: " + cmd);
			commandsHelp();
		}
		
		return false;
	}

	private void commandsHelp() {
		System.out.println("Commands: help, quit, startperiod, endperiod, freebie, autoroll");
	}
	
	private void quit(String reason) {
		quitting = true;
		adversary.ifPresent(adv -> adv.quit(Adversary.LEAVE_TAUNT));
		scheduler.stop();
		quitServer(reason);
	}
	
	private void giveFreePowerup() {
		sendChannel("Spawning item manually");
		scheduleRandomSpawn(null, rules.getPowerupsSpawner(Powerups.allPowerups), null);
	}
	
	private void spawnNextScheduledPowerup() {
		SpawnTask spawnTask = spawnTasks.pollFirst();
		if (spawnTask == null) {
			System.out.println("No spawn tasks left");
		}
		else {
			sendChannel("Manually hurrying the next powerup spawn:");
			spawnTask.execute(null);
		}
	}

	private void schedulePowerupsOfTheDay() {
		Powerups.rebuildSpawnChances();
		
		LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
		computeTimes(now);
		LocalDateTime spawnTime = now;
		if (debug) {
			spawnTime = spawnStartTime;
		}

		spawnTime = incrementSpawnTime(spawnTime);
		// Prevent missing a spawn if we get 0 from incrementSpawnTime()
		spawnTime = spawnTime.plusMinutes(1);
		
		Spawner<BasicPowerup> spawnPowerups = Powerups.firstPowerup;
		Spawner<Event> spawnEvents = Powerups.allEvents;
		int n = 0;
		while (spawnTime.isBefore(spawnEndTime)) {
			if (n > 0) spawnPowerups = Powerups.allPowerups;
			if (spawnTime.getHour() >= 16) spawnEvents = Powerups.lateEvents;
			ISpawnable spawn = scheduleRandomSpawn(spawnTime, spawnPowerups, spawnEvents).spawn;
			// Only allow one 4th wall break per day
			// TODO replace this with taking a subspawner of spawnEvents when more "one-per-day" events are required
			if (spawn instanceof FourthWallBreaks) spawnEvents = Powerups.allEventsMinusFourthWall;
			spawnTime = incrementSpawnTime(spawnTime);
			n++;
		}
	}

	private void computeTimes(LocalDateTime now) {
		rollPeriodStartTime = now.with(ROLL_PERIOD_START);
		if (rollPeriodStartTime.isBefore(now)) rollPeriodStartTime = rollPeriodStartTime.plusDays(1);
		
		rollPeriodEndTime = rollPeriodStartTime.with(ROLL_PERIOD_END);
		spawnStartTime = rollPeriodEndTime.minusDays(1);
		spawnEndTime = rollPeriodStartTime.with(ROLL_PERIOD_ABOUT_TO_START);
	}

	private LocalDateTime incrementSpawnTime(LocalDateTime spawnTime) {
		int minutesRandomRange = 260 - 5 * getSpawnPeriodHoursPassed(spawnTime);
		int minutesIncr = commonRandom.nextInt(minutesRandomRange);
		return spawnTime.plusMinutes(minutesIncr);
	}
	
	private int getSpawnPeriodHoursPassed(LocalDateTime spawnTime) {
		return (int)Duration.between(spawnStartTime, spawnTime).toHours();
	}
	
	public class SpawnTask extends Task implements Comparable<SpawnTask>, IColorStrConvertable {
		public ISpawnable spawn;
		public LocalDateTime time;
		public String id;
		public ExpireTask expireTask;
		
		public SpawnTask(LocalDateTime time, ISpawnable spawn) {
			this.time = time;
			this.spawn = spawn;
		}
		
		public Powerup getPowerup() {
			if (spawn instanceof Powerup) return (Powerup)spawn;
			else return null;
		}

		@Override
		public void execute(TaskExecutionContext context) {
			synchronized (lock) {
				spawnTasks.remove(this);
				scheduler.deschedule(id);
				
				if (spawn instanceof Powerup) {
					Powerup powerup = (Powerup)spawn;
					powerup.onSpawn();
					availablePowerups.add(powerup);
					listeners.forEach(l -> l.powerupSpawned(powerup));
				}
				else {
					Event event = (Event)spawn;
					event.run(NoppaBot.this);
				}
			}
		}
		
		@Override
		public String toString() {
			return String.format("[%tR] %s", time, spawn);
		}
		
		@Override
		public String toStringColored() {
			return String.format("[%tR] %s", time, spawn.toStringColored());
		}

		@Override
		public int compareTo(SpawnTask other) {
			return this.time.compareTo(other.time);
		}
	}
	
	public class ExpireTask extends Task {
		public Powerup powerup;
		public String id;
		
		public ExpireTask(Powerup powerup) {
			this.powerup = powerup;
		}
		
		@Override
		public void execute(TaskExecutionContext context) {
			synchronized (lock) {
				expireTasks.remove(this);
				scheduler.deschedule(id);
				
				expirePowerup(powerup);
			}
		}
	}
	
	private void expirePowerup(Powerup powerup) {
		if (availablePowerups.remove(powerup)) {
			powerup.onExpire();
		}
	}

	private void expireAllPowerups() {
		for (Powerup powerup : availablePowerups) {
			powerup.onExpire();
		}
		availablePowerups.clear();
	}

	@Override
	public SpawnTask scheduleRandomSpawn(LocalDateTime spawnTime, Spawner<BasicPowerup> allowedPowerups, Spawner<Event> allowedEvents) {
		ISpawnable spawn = rules.getRandomPowerupOrEvent(NoppaBot.this, allowedPowerups, allowedEvents);
		return scheduleSpawn(spawnTime, spawn);
	}
	
	/**
	 * Schedule a powerup/event spawn or spawn it immediately
	 * 
	 * @param spawnTime Spawn time or null for spawn immediately
	 */
	@Override
	public SpawnTask scheduleSpawn(LocalDateTime spawnTime, ISpawnable spawn) {
		boolean immediateSpawn = (spawnTime == null);
		if (immediateSpawn) spawnTime = LocalDateTime.now();
		final String pattern = DateTimeUtils.toSchedulingPattern(spawnTime);

		SpawnTask spawnTask = new SpawnTask(spawnTime, spawn);
		
		if (immediateSpawn) {
			spawnTask.execute(null);
		}
		else {
			spawnTask.id = scheduler.schedule(pattern, spawnTask);
			spawnTasks.add(spawnTask);
		}
		
		Powerup powerup = spawnTask.getPowerup();
		if (powerup != null) { // Is BasicPowerup, EvolvedPowerup or Instant
			LocalDateTime expireTime = spawnTime.plus(POWERUP_EXPIRY_DURATION);
			ExpireTask expireTask = scheduleExpire(powerup, expireTime);
			spawnTask.expireTask = expireTask; // If we want to change the powerup, we want to alter the expire task too
			
			if (debug) System.out.printf("Spawning %s on %tR, expires on %tR\n", spawnTask.spawn, spawnTime, expireTime);
		}
		else if (debug) { // Print events
			System.out.printf("Spawning %s on %tR\n", spawnTask.spawn, spawnTime);
		}
		
		return spawnTask;
	}


	@Override
	public ExpireTask scheduleExpire(Powerup powerup, LocalDateTime expireTime) {
		final String expirePattern = DateTimeUtils.toSchedulingPattern(expireTime);
		
		ExpireTask expireTask = new ExpireTask(powerup);
		expireTask.id = scheduler.schedule(expirePattern, expireTask);
		expireTasks.add(expireTask);
		return expireTask;
	}
	
	private void clearPowerupTasks() {
		for (SpawnTask task : spawnTasks) {
			scheduler.deschedule(task.id);
		}
		spawnTasks.clear();
		
		for (ExpireTask task : expireTasks) {
			scheduler.deschedule(task.id);
		}
		expireTasks.clear();
	}
	
	private boolean isInRollPeriod() {
		if (debug) return true;
		
		LocalDateTime now = LocalDateTime.now();
		return now.isEqual(rollPeriodStartTime)
			|| (now.isAfter(rollPeriodStartTime) && now.isBefore(rollPeriodEndTime));
	}
	
	private boolean isOvertime() {
		return !isInRollPeriod() && state == State.ROLL_PERIOD;
	}
	
	@Override
	protected void onMessage(String channel, String sender, String login, String hostname,
		String message) {

		synchronized (lock) {
			try {
				handleMessage(sender, message);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	private void handleMessage(String sender, String message) {
		Matcher dNPattern = dicePattern.matcher(message);
		if (dNPattern.matches()) {
			String numberStr;
			String rollerNick;
			numberStr = dNPattern.group(1);
			rollerNick = sender;

			int sides = Integer.parseInt(numberStr);
			rollAndParticipate(rollerNick, sides, false);
		}
		
		Matcher commandMatcher = commandAndArgument.matcher(message);
		if (commandMatcher.matches()) {
			String cmd = commandMatcher.group(1);
			String args = commandMatcher.group(2);
			String[] argsSplit = args == null ? new String[0] : args.split("\\s+");
			
			if (cmd.equalsIgnoreCase("grab") || cmd.equalsIgnoreCase("pick") 
				|| cmd.equalsIgnoreCase("take") || cmd.equalsIgnoreCase("get")) {
				grabPowerup(sender, args);
			}
			else if (cmd.equalsIgnoreCase("roll")) {
				if (argsSplit.length == 0) rollAndParticipate(sender, 100, true);
				else if (argsSplit.length == 1) {
					String rollerNick = argsSplit[0];
					if (isOnChannel(rollerNick)) sendCustomRollerOnChannelError(rollerNick);
					else rollAndParticipate(rollerNick, 100, true);
				}
			}
			else if (cmd.equalsIgnoreCase("tells") || cmd.equalsIgnoreCase("nextrolls") || cmd.equalsIgnoreCase("predictions")) {
				if (argsSplit.length == 0) listNextRolls(sender);
				else if (argsSplit.length == 1) listNextRolls(argsSplit[0]);
			}
			
			// No-arg commands
			else if (args == null) {
				if (cmd.equalsIgnoreCase("items")) {
					listItems(false);
				}
				else if (cmd.equalsIgnoreCase("look")) {
					listAvailablePowerups(sender);
				}
				else if (cmd.equalsIgnoreCase("rolls")) {
					listRolls();
				}
				else if (cmd.equalsIgnoreCase("peek")) {
					peekNextSpawns(sender);
				}
				else if (cmd.equalsIgnoreCase("autoroll")) {
					autorollFor(sender);
				}
				else if (cmd.equalsIgnoreCase("unautoroll")) {
					removeAutoroll(sender);
				}
				else if (cmd.equalsIgnoreCase("drop")) {
					dropPowerup(sender);
				}
				else if (cmd.equalsIgnoreCase("rules")) {
					listRules(sender);
				}
				else if (cmd.equalsIgnoreCase("cards") || cmd.equalsIgnoreCase("hands")) {
					pokerTable.listHands(false);
				}
				else if (cmd.equalsIgnoreCase("train")) {
					grabPowerup(sender, DicemonTrainer.NAME);
				}
				else if (cmd.equalsIgnoreCase("deal")) {
					grabPowerup(sender, PokerDealer.NAME);
				}
				else if (cmd.equalsIgnoreCase("dicetell") || cmd.equalsIgnoreCase("tell") || cmd.equalsIgnoreCase("predict")) {
					grabPowerup(sender, Diceteller.NAME);
				}
				else if (cmd.equalsIgnoreCase("recycle") || cmd.equalsIgnoreCase("trash")) {
					grabPowerup(sender, DiceRecycler.NAME);
				}
				else if (cmd.equalsIgnoreCase("steal") || cmd.equalsIgnoreCase("loot") || cmd.equalsIgnoreCase("plunder")) {
					grabPowerup(sender, DicePirate.NAME);
				}
				else if (cmd.equalsIgnoreCase("troll")) {
					grabPowerup(sender, TrollingProfessional.NAME);
				}
				else if (cmd.equalsIgnoreCase("reveal")) {
					Powerup powerup = powerups.get(sender);
					if (powerup != null && (powerup instanceof PokerHand || powerup instanceof BetterHand)) {
						rollAndParticipate(sender, 100, true);
					}
					else {
						sendChannelFormat("%s: you don't have a poker hand", Color.nick(sender));
					}
				}
				
				// Joke commands
				else if (cmd.equalsIgnoreCase("poke")) {
					sendAction(channel, String.format("pokes %s back", sender));
				}
				else if (cmd.equalsIgnoreCase("autotroll")) {
					sendChannelFormat("%s: ok, I will grab the next %s for you... no, not really. :P",
						Color.nick(sender), Color.instant("Trolling Professional"));
				}
			}
		}
	}

	private void removeAutoroll(String nick) {
		if (autorolls.contains(nick)) {
			sendChannelFormat("%s: OK, I'll let you roll manually now.", Color.nick(nick));
			autorolls.remove(nick);
		}
		else {
			sendChannelFormat("%s: you didn't have autoroll to begin with.", Color.nick(nick));
		}
	}

	private void sendCustomRollerOnChannelError(String rollerNick) {
		sendChannelFormat("%s is on the channel, so you cannot roll for him/her", rollerNick);
	}

	private void dropPowerup(String nick) {
		boolean canDropItems = rules.canDropItems.get();
		Powerup powerup = powerups.get(nick);
		if (state != State.NORMAL && !debug) {
			sendChannelFormat("%s: you cannot drop items during the roll contest.", Color.nick(nick));
			return;
		}
		else if (powerup == null) {
			sendChannelFormat("%s: you have nothing to drop.", Color.nick(nick));
			return;
		}
		else if (!canDropItems && !hasFavorVerbose(nick)) {
			return;
		}
		
		if (!canDropItems) {
			favorsUsed.add(nick);
		}
		
		sendChannelFormat("%s drops the %s on the ground.", Color.nick(nick), 
			powerup.nameWithDetailsColored());
		powerups.remove(nick);
		availablePowerups.add(powerup);
		LocalDateTime expiryTime = LocalDateTime.now().plus(POWERUP_EXPIRY_DURATION);
		scheduleExpire(powerup, expiryTime);
	}


	private void autorollFor(String nick) {
		if (autorolls.contains(nick)) {
			sendChannelFormat("%s: you've already requested me to autoroll for you.", Color.nick(nick));
			return;
		}
		else if (!hasFavorVerbose(nick)) return;
		
		favorsUsed.add(nick);
		sendChannelFormat("%s: ok, I will roll for you tonight.", Color.nick(nick));
		autorolls.add(nick);
	}


	private void peekNextSpawns(String nick) {
		if (!hasFavorVerbose(nick)) return;
		favorsUsed.add(nick);
		
		final int peekCount = 3;
		
		Iterable<SpawnTask> nextThree = Iterables.limit(spawnTasks, peekCount);
		String spawnInfo = StringUtils.join(nextThree, ", ", new StringConverter<SpawnTask>() {
			@Override
			public String toString(SpawnTask task) {
				String str = task.toStringColored();
				ISpawnable spawn = task.spawn;
				if (spawn != null && spawn instanceof Bomb) {
					str += " ... that item seems suspicious!";
				}
				return str;
			}
		});
		boolean moreToCome = spawnTasks.size() > peekCount;
		
		StringBuilder buf = new StringBuilder();
		if (spawnTasks.isEmpty()) {
			buf.append("No further events will occur today!");
		}
		else {
			buf.append("Next up is: ");
			buf.append(spawnInfo).append(". ");
			if (moreToCome) {
				buf.append("More events will still happen, though!");
			}
			else {
				buf.append("Those will be the last events for today.");
			}
		}
		
		sendAction(channel, String.format("whispers to %s what happens next!", nick));
		sendMessage(nick, buf.toString());
	}

	private boolean hasFavorVerbose(String nick) {
		if (hasFavor(nick)) {
			sendChannelFormat("%s: you have used your favor for today", Color.nick(nick));
			return false;
		}
		
		return true;
	}

	@Override
	public boolean hasFavor(String nick) {
		return favorsUsed.contains(nick);
	}
	
	private void listItems(boolean isRollPeriodStart) {
		if (powerups.isEmpty() && !isRollPeriodStart) {
			sendChannel("Nobody has any items.");
			return;
		}
		
		if (!powerups.isEmpty() || !isRollPeriodStart) {
			StringBuilder buf = new StringBuilder();
			if (isRollPeriodStart) buf.append("Today you have collected: ");
			else buf.append("Items: ");
			boolean first = true;
			for (String nick : powerups.keySet()) {
				String powerupName = powerups.get(nick).nameWithDetailsColored();
				if (!first) buf.append(", ");
				if (!isRollPeriodStart) nick = Color.antiHilight(nick);
				buf.append(String.format("%s (%s)", powerupName, nick));
				first = false;
			}
			sendChannel(buf.toString());
		}
	}
	
	private void listRolls() {
		if (state != State.ROLL_PERIOD) {
			sendChannel("Rolls can be listed only during the roll period.");
			return;
		}

		List<Entry<String, Roll>> rollsList = rolls.orderedList();
		
		StringBuilder buf = new StringBuilder();
		buf.append("Rolls: ");
		boolean first = true;
		for (Entry<String, Roll> entry : rollsList) {
			String nick = entry.getKey();
			Roll roll = entry.getValue();
			if (!first) buf.append(", ");
			buf.append(String.format("%s %s(%s)", roll.toResultString(true, false, this), offTargetInfo(roll), Color.antiHilight(nick)));
			first = false;
		}
		if (buf.length() > 0) sendChannel(buf.toString());
		else sendChannel("Nobody has rolled yet.");
	}
	
	private String offTargetInfo(Roll roll) {
		if (rules.winCondition.get() instanceof RollClosestToTarget) {
			RollClosestToTarget winCond = (RollClosestToTarget)rules.winCondition.get();
			int rollTarget = winCond.getRollTarget();
			int dist = Math.abs(rollTarget - roll.total());
			return Color.custom("[" + dist + " off] ", Colors.WHITE);
		}
		else return "";
	}
	
	private void listAvailablePowerups(String nick) {
		if (availablePowerups.isEmpty()) {
			sendChannelFormat("%s: you see nothing of interest.", Color.nick(nick));
		}
		else {
			String items = StringUtils.joinColored(availablePowerups, ", ");
			sendChannelFormat("%s: You see: %s.", Color.nick(nick), items);
		}
	}
	
	private void listNextRolls(String nick) {
		Powerup powerup = powerups.get(nick);
		final int dieSides = powerup == null ? 100 : powerup.sides();
		PeekableRandom random = randoms.get(nick); // Don't create a new random if there isn't one
		
		if (random != null) {
			if (random.containsHints()) {
				Map<Integer, PeekedRoll> peekedRolls = random.getPeekedRolls();
				
				for (Map.Entry<Integer, PeekedRoll> entry : peekedRolls.entrySet()) {
					int sides = entry.getKey();
					PeekedRoll pr = entry.getValue();
					String sidesStr = (sides == dieSides) ? Color.custom("d" + sides, Colors.WHITE) : ("d" + sides);
					sendChannelFormat("%s's next %s roll: %s.", Color.nick(nick), sidesStr, pr.summarizeAll());
				}
				
				return;
			}
		}
		
		sendChannelFormat("No rolls are predicted for %s.", Color.nick(nick));
	}
	
	
	private void listRules(String nick) {
		String explanation = rules.getExplanation();
		sendChannelFormat("%s: %s", Color.nick(nick), explanation);
	}

	private void grabPowerup(String nick, String powerupName) {
		Powerup powerup = findPowerup(powerupName);
		
		// FIXME revise the logic, it's not very clean

		// Allow grabbing items during roll period in debug mode
		if (state != State.NORMAL && !debug) {
			sendChannelFormat("%s: you cannot grab items during the roll contest.", Color.nick(nick));
		}
		else if (availablePowerups.isEmpty()) {
			sendChannelFormat("%s: nothing to grab.", Color.nick(nick));
		}
		else if (powerup != null && canFuse(nick, powerup)) {
			fuseDice(nick, powerup);
		}
		else if (powerup != null && !powerup.canPickUp(nick, true)) {
			// canPickUp(*) will warn the user
		}
		else {
			if (powerup != null) {
				doGrabPowerup(nick, powerup);
			}
			// User didn't specify which item to grab
			else if (powerupName == null) {
				String items = StringUtils.joinColored(availablePowerups, ", ");
				sendChannelFormat("%s: There are multiple items available: %s. Specify which one you want!", 
					Color.nick(nick), items);
			}
			// Unknown item
			else {
				sendChannelFormat("%s: There is no such item available.", Color.nick(nick));
			}
		}
	}

	private void doGrabPowerup(String nick, Powerup powerup) {
		if (powerup.isCarried() || powerup.isDestroyedAfterPickup()) {
			boolean removed = availablePowerups.remove(powerup);
			if (!removed) System.err.println("Warning: could not remove powerup from the ground.");
		}
		
		powerup.setOwner(nick);
		powerup.onPickup();
		
		if (powerup.isCarried()) {
			powerups.put(nick, powerup);
		}
	}
	
	private void fuseDice(String nick, Powerup powerup) {
		availablePowerups.remove(powerup);
		DicemonTrainer.upgradeDie(this, nick, String.format(
			"You grab the %s, and fuse it and your existing die into one.",
			powerup.nameColored()));
	}


	private boolean canFuse(String nick, Powerup grabbedPowerup) {
		if(rules.diceFusion.get()) {
			if (powerups.containsKey(nick)) {
				Powerup powerup = powerups.get(nick);
				return powerup.isUpgradeable() && powerup.getClass().equals(grabbedPowerup.getClass());
			}
		}
		return false;
	}
	
	@Override
	public SortedSet<Powerup> getAvailablePowerups() {
		return availablePowerups;
	}
	
	private Powerup findPowerup(String name) {
		if (name == null) {
			if (availablePowerups.size() == 1) return availablePowerups.first();
			else return null;
		}
		
		for (Powerup powerup : availablePowerups) {
			String nameWithDetails = powerup.nameWithDetails();
			nameWithDetails = Colors.removeFormattingAndColors(nameWithDetails);
			if (nameWithDetails.toLowerCase().contains(name.toLowerCase())) return powerup;
		}
		
		return null;
	}
	
	private void rollAndParticipate(String nick, int sides, boolean withPowerup) {
		boolean rollCanParticipate = rollCanParticipate(nick);
		
		// Send error if the roll is not allowed
		rules.isRollAllowed(nick, true);
		
		// Generate a roll
		Roll roll;
		if (withPowerup && powerups.containsKey(nick)) {
			// ... with the carried power-up
			Powerup powerup = powerups.get(nick);
			if (rollCanParticipate) roll = powerup.onContestRoll();
			else roll = powerup.onNormalRoll(); // same as onContestRoll() if not overridded by powerup
		}
		else {
			// ... with the normal d100
			roll = doRoll(nick, sides);
		}
		
		if (sides == 100 && rollCanParticipate && withPowerup) {
			
			// Activate power-ups which affect opponent rolls
			roll = fireEarlyOpponentRolled(nick, roll);

			// If win condition is non-standard, tell how this roll will be scored
			rules.winCondition.get().onContestRoll(this, nick, roll);

			// Participate the roll
			participate(nick, roll);
			
			// Activate power-ups which trigger effects when opponent rolled,
			// but don't modify opponent's roll
			fireLateOpponentRolled(nick, roll);
		}
	}
	
	@Override
	public boolean rollCanParticipate(String nick) {
		boolean allowed = rules.isRollAllowed(nick, false);
		boolean participated = participated(nick);
		boolean rollOrTiebreakPeriod = isRollOrTiebreakPeriod();
		return allowed && !participated && rollOrTiebreakPeriod;
	}

	@Override
	public boolean isRollOrTiebreakPeriod() {
		return state == State.ROLL_PERIOD || state == State.SETTLE_TIE;
	}
	
	@Override
	public DiceRoll doRoll(String nick, int sides) {
		DiceRoll roll = getRoll(nick, sides);
		if (sides == 100) {
			sendDefaultContestRollMessage(nick, roll, true, rollCanParticipate(nick));
		}
		else {
			sendChannelFormat("%s rolls the d%s... %s!", Color.nick(nick), sides,
				Color.custom(String.valueOf(roll), Colors.WHITE));
		}
		return roll;
	}

	private Roll fireEarlyOpponentRolled(String nick, Roll roll) {
		// Early -- can modify the roll
		for (String powerupOwner : powerups.keySet()) {
			if (!powerupOwner.equals(nick)) {
				Powerup<?> powerup = powerups.get(powerupOwner);
				roll = powerup.onOpponentRoll(nick, roll);
			}
		}
		return roll;
	}

	private void fireLateOpponentRolled(String nick, Roll roll) {
		// Late -- the roll is now determined, do something else
		for (String powerupOwner : powerups.keySet()) {
			if (!powerupOwner.equals(nick)) {
				Powerup<?> powerup = powerups.get(powerupOwner);
				powerup.onOpponentRollLate(nick, roll);
			}
		}
	}


	@Override
	public void sendDefaultContestRollMessage(String nick, DiceRoll roll, boolean colorNick, boolean colorRoll) {
		sendChannel(getDefaultContestRollMessage(nick, roll, colorNick, colorRoll));
	}

	@Override
	public String getDefaultContestRollMessage(String nick, DiceRoll roll, boolean colorNick, boolean colorRoll) {
		String participatedMsg = participated(nick) ? 
			" You've already rolled " + participatingRoll(nick) + " though, this roll won't participate!" : "";
		String rollStr = roll.toResultString(colorRoll, true, this);
		String nickColored = colorNick ? Color.nick(nick) : nick;
		return String.format("%s rolls the d100... %s! %s%s", nickColored, rollStr, grade(roll), participatedMsg);
	}
	
	@Override
	public String grade(Roll roll) {
		int score = rules.winCondition.get().assignScore(roll);
		if (score >= 100) return "You showed us....the ULTIMATE roll!";
		else if (score >= 95) return "You are a super roller!";
		else if (score >= 90) return "Amazing!";
		else if (score >= 80) return "Nicely done!";
		else if (score >= 70) return "Quite good!";
		else if (score >= 60) return "That's ok!";
		else if (score >= 50) return "That's decent!";
		else if (score >= 40) return "Could've gone better!";
		else if (score >= 30) return "That's kind of a low roll!";
		else if (score >= 20) return "Not having much luck today? :(";
		else if (score >= 10) return "Still at two digits!";
		else if (score > 1) return "Seek some advice from your local qualified rolling professional!";
		else if (score == 1) return "Yikes!";
		else return "Huh?";
	}
	
	@Override
	public void participate(String nick, Roll rollValue) {
		if (state == State.ROLL_PERIOD && !rolls.participated(nick)) {
			rolls.put(nick, rollValue);
			if (isOvertime()) tryEndRollPeriod();
		}
		else if (state == State.SETTLE_TIE && tiebreakers.contains(nick)) {
			if (!rolls.participated(nick)) {
				rolls.put(nick, rollValue);
				tiebreakers.remove(nick);
				if (tiebreakers.isEmpty()) tryEndRollPeriod();
				else {
					String pendingTiebreakers = StringUtils.join(tiebreakers, ", ");
					sendChannelFormat("I still need a roll from %s.", pendingTiebreakers);
				}
			}
		}
	}
	
	@Override
	public boolean participated(String nick) {
		return rolls.participated(nick);
	}
	
	private Roll participatingRoll(String nick) {
		return rolls.get(nick);
	}
	

	private void doTasksBeforeRollPeriodStart() {
		expireAllPowerups();
		clearPowerupTasks();

		listItems(true);
	}
	
	private void onSpawnerOverridden() {
		if (debug) System.out.println("Spawner was overridden");
		expireAllPowerups();
		convertCarriedPowerupsToMatchSpawnOverride();
		clearPowerupTasks();
		schedulePowerupsOfTheDay();
	}
	
	private void convertCarriedPowerupsToMatchSpawnOverride() {
		if (!rules.spawnOverride.isPresent()) return;
		
		Spawner<BasicPowerup> spawnOverride = rules.spawnOverride.getValue();
		Predicate<SpawnInfo> itemPredicate = info -> 
			info instanceof BasicPowerupSpawnInfo && !(info instanceof InstantSpawnInfo);
		boolean canSpawnItems = spawnOverride.canSpawn(itemPredicate);

		// Replace everyone's item with new ones from the spawn override
		if (canSpawnItems) {
			Spawner<BasicPowerup> spawner = spawnOverride.subSpawner(itemPredicate,
				new LastSpawn<BasicPowerup>());
			
			powerups.replaceAll((owner, oldPowerup) -> {
				if (oldPowerup instanceof BasicPowerup && spawner.canSpawn((BasicPowerup)oldPowerup)) {
					return oldPowerup;
				}
				else {
					Powerup powerup = spawner.spawn();
					powerup.initialize(this);
					powerup.setOwner(owner);
					if (oldPowerup instanceof EvolvedPowerup) powerup = powerup.upgrade();
					sendChannelFormat("%s's %s was transformed into %s!", Color.nick(owner),
						oldPowerup.nameWithDetailsColored(), powerup.nameWithDetailsColored());
					return powerup;
				}
			});
		}
		// Just destroy old items
		else {
			rulesLawyerDestroyPowerups();
		}
	}


	private void rulesLawyerDestroyPowerups() {
		powerups.forEach((owner, powerup) -> sendChannelFormat(
			"%s's %s was destroyed by the rules lawyer.", owner, powerup.nameWithDetailsColored()));
		powerups.clear();
	}
	
	private void startRollPeriod() {
		state = State.ROLL_PERIOD;
		
		// Do this again, because doTasksBeforeRollPeriodStart() isn't guaranteed to occur
		clearPowerupTasks();

		sendChannel(randomRollStartMsg());
		rules.onRollPeriodStart();
		if (pokerTable.gameStarted) pokerTable.tryRevealTableCards(true);
		
		for (String nick : powerups.keySet()) {
			Powerup powerup = powerups.get(nick);
			powerup.onRollPeriodStart();
		}
		
		listeners.forEach(l -> l.rollPeriodStarted());
	}

	private void autorollFor(Set<String> autorolls) {
		
		// Filter people who have rolled
		Set<String> rollers = new TreeSet<String>(autorolls);
		rollers.removeAll(rolls.participants());
		
		if (!rollers.isEmpty()) {
			sendChannelFormat("I'll roll for: %s", StringUtils.join(rollers, ", "));
			
			for (String nick : rollers) {
				rollAndParticipate(nick, 100, true);
			}
		}
	}
	
	private void tryEndRollPeriod() {
		
		if (rolls.isEmpty()) {
			sendChannelFormat("No rolls within %s minutes. Now first roll wins, be quick!",
				ROLL_PERIOD_DURATION.toMinutes());
			state = State.ROLL_PERIOD;
			return;
		}
		else {
			List<String> winningRollers = getWinningRollers();
			if (winningRollers.size() == 1) {
				endRollPeriod(winningRollers);
			}
			else {
				startSettleTie(winningRollers);
			}
		}
	}

	private void startSettleTie(List<String> winningRollers) {
		// Deschedule previous settle tie timeout, if there is one
		if (settleTieTimeoutTask != null) {
			scheduler.deschedule(settleTieTimeoutTask.id);
			settleTieTimeoutTask = null;
		}
		
		lastTiebreakPeriodStartTime = LocalDateTime.now();
		String tiebreakersStr = StringUtils.join(winningRollers, ", ");
		Roll roll = rolls.get(winningRollers.get(0));
		String msg = String.format(
			"The roll %s was tied between %s. Roll again within %s minutes to settle the score!",
			roll.intValueRuled(this) /* don't want to display 123 (= 100) */, tiebreakersStr,
			ROLL_PERIOD_DURATION.toMinutes());
		sendChannel(msg);
		tiebreakers.addAll(winningRollers);
		
		// Rerolling won't help with cards
		if (rules.isPokerNight()) {
			rules.reset();
			rulesLawyerDestroyPowerups();
		}
		
		rolls.clear();
		
		for (String tiebreakerNick : tiebreakers) {
			Powerup powerup = powerups.get(tiebreakerNick);
			if (powerup != null) powerup.onTiebreakPeriodStart();
		}
		
		settleTieTimeoutTask = scheduleSettleTieTimeout();
		
		state = State.SETTLE_TIE;

		// Schedule autoroll for tiebreakers
		Set<String> tiebreakerAutorolls = new TreeSet<String>();
		tiebreakerAutorolls.addAll(tiebreakers);
		tiebreakerAutorolls.retainAll(autorolls);
		scheduleSettleTieAutorolls(tiebreakerAutorolls);
		
		listeners.forEach(l -> l.settleTieStarted(tiebreakers));
	}


	private SettleTieTimeoutTask scheduleSettleTieTimeout() {
		SettleTieTimeoutTask timeoutTask = new SettleTieTimeoutTask();
		LocalDateTime time = lastTiebreakPeriodStartTime;
		time = time.plus(ROLL_PERIOD_DURATION);
		final String pattern = DateTimeUtils.toSchedulingPattern(time);
		timeoutTask.id = scheduler.schedule(pattern, timeoutTask);
		return timeoutTask;
	}
	
	private class SettleTieTimeoutTask extends Task {
		public String id;
		
		@Override
		public void execute(TaskExecutionContext arg0) throws RuntimeException {
			synchronized (lock) {
				if (state == State.SETTLE_TIE) {
					sendChannel("I'm calling this now...");
					tiebreakers.clear();
					tryEndRollPeriod();
				}
				scheduler.deschedule(id);
			}
		}
	}
	
	private void scheduleSettleTieAutorolls(Set<String> autorolls) {
		SettleTieAutorollsTask autorollTask = new SettleTieAutorollsTask(autorolls);
		LocalDateTime time = lastTiebreakPeriodStartTime;
		time = time.plus(AUTOROLL_OFFSET);
		final String pattern = DateTimeUtils.toSchedulingPattern(time);
		autorollTask.id = scheduler.schedule(pattern, autorollTask);
	}
	
	private class SettleTieAutorollsTask extends Task {
		public String id;
		private Set<String> autorolls;
		
		public SettleTieAutorollsTask(Set<String> autorolls) {
			this.autorolls = autorolls;
		}
		
		@Override
		public void execute(TaskExecutionContext arg0) throws RuntimeException {
			synchronized (lock) {
				if (state == State.SETTLE_TIE) {
					autorollFor(autorolls);
				}
				scheduler.deschedule(id);
			}
		}
	}
	
	private void endRollPeriod(List<String> winningRollers) {
		String winner = winningRollers.get(0);
		Roll roll = rolls.get(winner);
		String msg = String.format(randomRollEndMsg(), Color.nick(winner), roll.toResultString(true, false, this));
		sendChannel(msg);
		
		updateRecords(winner);
		
		expireAllPowerups(); // Should have none on the ground now, but just to be safe
		rolls.clear();
		tiebreakers.clear();
		powerups.clear();
		favorsUsed.clear();
		autorolls.clear();
		pokerTable.clear();
		
		if (rules.reset()) {
			sendChannel("The non-standard rules return to normal.");
		}
		
		schedulePowerupsOfTheDay();
		
		state = State.NORMAL;
		
		listeners.forEach(l -> l.rollPeriodEnded());
	}

	
	private void updateRecords(String winner) {
		RollRecords rec = loadRollRecords();
		if (rec == null) return;
		
		// Ensure record contains every user
		for (String nick : rolls.participants()) {
			rec.getOrAddUser(nick);
		}
		
		// Increment wins for the winner
		rec.incrementWins(winner);
		
		// Add the today's roll for everyone
		for (User user : rec.users) {
			if (rolls.participated(user.nick) || userParticipatedWithAlias(user)) {
				addRollAndIncrementParticipation(user);
			}
			else {
				user.addRoll(0);
			}
		}
		
		rec.sortUsers();
		
		BufferedWriter writer = null;
		try {
			if (rollRecordsPath.exists()) {
				File old = new File(rollRecordsPath.toString() + ".old");
				copyFile(rollRecordsPath, old);
			}
			
			writer = new BufferedWriter(new FileWriter(rollRecordsPath));
			rec.save(writer);
			close(writer);
		}
		catch (IOException e) {
			e.printStackTrace();
			return;
		}
		finally {
			close(writer);
		}
	}

	private boolean userParticipatedWithAlias(User user) {
		for (String alias : user.aliases) {
			if (rolls.participated(alias)) return true;
		}
		return false;
	}


	private void addRollAndIncrementParticipation(User user) {
		Roll roll = rolls.get(user.nick);
		int intRoll = roll instanceof DiceRoll ? roll.intValueRuled(this) : 0;
		user.addRoll(intRoll); // At this point we switch to the int world
		user.participation++;
	}


	@Override
	public RollRecords loadRollRecords() {
		RollRecords rec;
		if (rollRecordsPath.exists()) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(rollRecordsPath));
				rec = RollRecords.load(reader);
			}
			catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			finally {
				close(reader);
			}
		}
		else rec = new RollRecords();
		return rec;
	}

	private String randomRollStartMsg() {
		return rollStartMsgs[commonRandom.nextInt(rollStartMsgs.length)];
	}
	
	private String randomRollEndMsg() {
		return rollEndMsgs[commonRandom.nextInt(rollEndMsgs.length)];
	}

	@Override
	public void sendChannelFormat(String msg, Object... args) {
		sendChannel(String.format(msg, args));
	}
	
	@Override
	public void sendChannel(String msg) {
		sendMessage(channel, msg);
	}
	
	@Override
	public void sendMessageFormat(String nick, String msg, Object... args) {
		sendMessage(nick, String.format(msg, args));
	}
	
	@Override
	public Map<String, Powerup> getPowerups() {
		return powerups;
	}
	
	@Override
	public Rolls getRolls() {
		return rolls;
	}
	
	@Override
	public PeekableRandom getRandomFor(String nick) {
		if (!randoms.containsKey(nick)) {
			if (adversary.isPresent() && adversary.get().getNick().equals(nick)) {
				randoms.put(adversary.get().getNick(), new WeightedRandom(0.5, true));
			}
			else {
				randoms.put(nick, new WeightedRandom(rules.bias));
			}
		}
		return randoms.get(nick);
	}
	
	@Override
	public DiceRoll getRoll(String nick, int sides) {
		PeekableRandom random = getRandomFor(nick);
		return random.roll(sides);
	}
	
	@Override
	public PeekedRoll peekRoll(String nick, int sides) {
		PeekableRandom random = getRandomFor(nick);
		return random.peek(sides);
	}
	
	@Override
	public Hint spawnRollHint(String nick, int sides) {
		PeekableRandom random = getRandomFor(nick);
		return random.spawnHint(sides);
	}
	
	@Override
	public void setNextRoll(String nick, int sides, DiceRoll roll) {
		PeekableRandom random = getRandomFor(nick);
		random.setNextRoll(sides, roll);
	}
	
	public int countPowerups(Class<? extends Powerup> type) {
		int count = 0;
		for (Powerup powerup : powerups.values()) {
			if (type.isAssignableFrom(powerup.getClass())) count++;
		}
		return count;
	}
	
	public List<String> getWinningRollers() {
		return rolls.getWinningRollers();
	}
	
	@Override
	public int getSecondsAfterPeriodStart() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime periodStart;
		if (state == State.ROLL_PERIOD) periodStart = rollPeriodStartTime;
		else if (state == State.SETTLE_TIE) periodStart = lastTiebreakPeriodStartTime;
		else throw new IllegalStateException("Not in roll or tie period");
		return (int)Duration.between(periodStart, now).getSeconds();
	}
	
	@Override
	public String remainingSpawnsInfo() {
		String info = StringUtils.joinColored(spawnTasks, ", ");
		return info;
	}
	
	@Override
	public LocalDateTime getRollPeriodStartTime() {
		return rollPeriodStartTime;
	}
	
	@Override
	public LocalDateTime getSpawnStartTime() {
		return spawnStartTime;
	}
	
	@Override
	public LocalDateTime getSpawnEndTime() {
		return spawnEndTime;
	}
	
	@Override
	public Rules getRules() {
		return rules;
	}
	
//	@Override
//	public void onRulesChanged() {
//		rolls.onRulesChanged();
//	}
	
	@Override
	public void clearFavorsUsed() {
		favorsUsed.clear();
	}
	
	/**
	 * Randomly overwrite some scheduled spawns with apprentice dice spawns
	 */
	@Override
	public void insertApprenticeDice() {
		for (SpawnTask task : spawnTasks) {
			if (task.getPowerup() != null && commonRandom.nextFloat() < 0.15f) {
				ApprenticeDie apprenticeDie = new ApprenticeDie();
				apprenticeDie.initialize(this);
				task.spawn = apprenticeDie;
				task.expireTask.powerup = apprenticeDie;
			}
		}
	}
	
	@Override
	public List<String> getRandomPowerupOwners() {
		ArrayList<String> owners = new ArrayList<String>(getPowerups().keySet());
		Collections.shuffle(owners);
		if (owners.isEmpty()) return owners;
		else {
			int removeCount = commonRandom.nextInt(owners.size());
			return owners.subList(0, owners.size() - removeCount);
		}
	}
	
	@Override
	public int getPowerupSides(String nick) {
		Powerup powerup = powerups.get(nick);
		if (powerup == null) return 100;
		else return powerup.sides();
	}
	
	@Override
	public Scheduler getScheduler() {
		return scheduler;
	}
	
	@Override
	public Object getLock() {
		return lock;
	}
	
	@Override
	public PokerTable getPokerTable() {
		return pokerTable;
	}
	
	@Override
	public State getState() {
		return state;
	}
	
	public Optional<Adversary> getAdversary() {
		return adversary;
	}
	
	public class ReconnectTask extends Task {
		public String id;
		
		@Override
		public void execute(TaskExecutionContext context) {
			synchronized (lock) {
				try {
					if (!isConnected()) {
						System.out.println("Now reconnecting");
						reconnect();
						joinChannel(channel);
						System.out.println("Successfully reconnected!");
					}
					else scheduler.deschedule(id);
				}
				catch (Exception e) {
					System.out.println("Reconnect failed: " + e.getMessage());
				}
			}
		}
	}
	
	@Override
	protected void onConnect() {
		System.out.println("Connected to the IRC server");
	}
	
	@Override
	protected void onDisconnect() {
		if (quitting) dispose();
		else {
			System.out.println("Disconnected from server. Trying to reconnect every 15 minute.");
			ReconnectTask rt = new ReconnectTask();
			final String id = scheduler.schedule("*/15 * * * *", rt);
			rt.id = id;
		}
	}

	private boolean isOnChannel(String nick) {
		org.jibble.pircbot.User[] users = getUsers(channel);
		for (org.jibble.pircbot.User user : users) {
			if (user.getNick().equals(nick)) return true;
		}
		return false;
	}
	
	public static void close(Closeable c) {
		try {
			if (c != null) c.close();
		}
		catch (IOException e) {
		}
	}
	
	public static void copyFile(File sourceFile, File destFile) throws IOException {
	    if(!destFile.exists()) {
	        destFile.createNewFile();
	    }

	    FileChannel source = null;
	    FileChannel destination = null;

	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }
	}
}
