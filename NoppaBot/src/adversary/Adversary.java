/*
 * Created on 10.12.2014
 * @author verkel
 */
package adversary;

import java.io.*;
import java.time.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.*;

import org.jibble.pircbot.*;

import it.sauronsoftware.cron4j.*;
import noppabot.*;
import noppabot.GenerateItemList.TestResult;
import noppabot.spawns.*;
import noppabot.spawns.Instant;
import noppabot.spawns.dice.*;
import noppabot.spawns.instants.*;


public class Adversary extends PircBot implements INoppaEventListener {
	
	public static final File cachedRankingListPath = new File("results.cache");
	public static final String LEAVE_TAUNT = "You haven't seen the last of me!";
	
	private final String botNick;
	private final String channel;
	private final String server;
	private final boolean debug;
	private final boolean executeDebugStuff;
	
	private Properties properties;
	private Random rnd = new Random();
	
	private void loadProperties() throws IOException {
		properties = new Properties();
		BufferedReader r = new BufferedReader(new FileReader("Adversary.properties"));
		properties.load(r);
		r.close();
	}

	public static final Pattern commandAndArgument = Pattern.compile("^(\\w+)(?:\\s+(.+)?)?");
	
	// Pircbot onMessage callback and scheduled tasks should synchronize on this lock
	private Object lock = new Object();
	
	private Scheduler scheduler = new Scheduler();
	private Map<String, TestResult> rankingList;
	private Optional<PowerupTask> grabTask = Optional.empty();
	private boolean quitting = false;
	
	private INoppaBot noppaBot;
	
	public Adversary(INoppaBot bot) throws Exception {
		super();
		
		this.noppaBot = bot;
		loadProperties();
		botNick = properties.getProperty("nick");
		channel = properties.getProperty("channel");
		server = properties.getProperty("server");
		debug = Boolean.parseBoolean(properties.getProperty("debug"));
		executeDebugStuff = Boolean.parseBoolean(properties.getProperty("executeDebugStuff"));
		
		loadCachedRankingList();
		
		setVerbose(false); // Print stacktraces, but also useless server messages
		setLogin(botNick);
		setName(botNick);
		
		if (executeDebugStuff) debugStuff();
		
		scheduler.start();
		connect(server);
	}
	
	private void loadCachedRankingList() {
		try {
			ObjectInputStream stream = new ObjectInputStream(new FileInputStream(cachedRankingListPath));
			rankingList = (TreeMap<String, TestResult>)stream.readObject();
			stream.close();
		}
		catch (ClassNotFoundException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void onConnect() {
		System.out.println("Adversary: Connected to the IRC server");
	}
	
	private void debugStuff() {
	}
	
	public void handleConsoleCommand(String input) {
		try {
				Matcher commandMatcher = commandAndArgument.matcher(input);
				if (!commandMatcher.matches()) return;
				String cmd = commandMatcher.group(1);
				String args = commandMatcher.group(2);
				
				synchronized (lock) {
					handleConsoleCommand(cmd, args);
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
		else if (cmd.equals("say")) {
			if (args == null) System.out.println("Need message as an argument");
			else sendChannel(args);
		}
		else if (cmd.equals("join")) {
			join();
		}
		else if (cmd.equals("story")) {
			sendChannel("You have made it through the dice roll purgatory.");
			sendChannelFormat("Now you must face the demon king of the dice, the %s!", 
				Color.custom(botNick, Colors.RED));
			sendChannel("Winner... takes the roll!");
		}
		else {
			System.out.println("Unknown command: " + cmd);
		}
		
		return false;
	}
	
	public void join() {
		joinChannel(channel);
	}
	
	public void part() {
		partChannel(channel, LEAVE_TAUNT);
	}
	
	public void quit(String reason) {
		quitting = true;
		scheduler.stop();
		quitServer(reason);
		dispose();
	}
	
	public void sendChannelFormat(String msg, Object... args) {
		sendChannel(String.format(msg, args));
	}
	
	public void sendChannel(String msg) {
		sendMessage(channel, msg);
	}

	@Override
	public void powerupSpawned(Powerup<?> powerup) {
		if (isOnChannel()) {
			if (powerup instanceof BasicDie) dieSpawned((BasicDie)powerup);
			else if (powerup instanceof Instant) instantSpawned((Instant)powerup);
		}
	}

	@Override
	public void rollPeriodStarted() {
		if (isOnChannel()) {
			// Schedule roll
			Calendar rollTime = randomRollTime();
			String pattern = DateTimeUtils.toSchedulingPattern(rollTime);
			DelegatedTask rollTask = new DelegatedTask(rollTime.getTime(), () -> {
				roll();
			});
			rollTask.id = scheduler.schedule(pattern, rollTask);
			if (debug) System.out.println("Scheduled roll: " + rollTask);
		}
	}
	
	private List<String> rollPreTaunts = Arrays.asList(
		"You can't hope to defeat me!",
		"I've got the touch, I've got the power!",
		"I have sacrificed a heap of rolling professionals in preparation of this roll!",
		"Master Die? Pffft. Try ADVERSARY DIE instead!",
		"UNLIMITED ROLL POWER!!",
		"Have you played this game Death Roll-y? It's supposedly very good.",
		"No one will ever catch me rolling as badly as you do!",
		"There are no clever roll tricks that can help you now.",
		"I once owned a dog that rolled dice better than you.",
		"They see me rollin', they hatin'",
		"I will roll you all up into my giant d100! The King of all Cosmos will be surely pleased.",
		String.format("Hey, look over there! It's a %s! *Manipulates the die trajectory using forbidden magics*"
			, Color.instant(DicePirate.NAME))
	);
	
	private void roll() {
		if (isOnChannel()) {
			sendChannelFormat(getRandom(rollPreTaunts));
			sendChannel("roll");
		}
	}

	@Override
	public void settleTieStarted(Set<String> tiebreakers) {
		if (tiebreakers.contains(botNick)) rollPeriodStarted();
	}

	private void dieSpawned(BasicDie die) {
		String dieName = die.name();
		dieName = substitute(dieName);
		
		TestResult ranking = rankingList.get(dieName);
		if (ranking == null) {
			if (die instanceof ImitatorDie) { // Ignored stuff
				return;
			}
			else { // Unexpected missing ranking
				System.err.printf("No ranking for %s, I won't take it\n", die.name());
				return;
			}
		}
		
		if (!isPlanningToGrab()) {
			if (!hasPowerup()) {
				if (isInteresting(die, ranking.ev)) scheduleGrab(die, ranking);
			}
			else { // Maybe drop current die
				Powerup current = noppaBot.getPowerups().get(botNick);
				TestResult currentRanking = rankingList.get(current.name());
				if (currentRanking != null && ranking.ev > currentRanking.ev && canDrop()) {
					if (debug) System.out.printf("%s is better than what I have (%s)\n", die, current);
					if (isInteresting(die, ranking.ev)) scheduleGrab(die, ranking);
				}
				else if (debug) {
					if (ranking.ev <= currentRanking.ev) System.out.printf("%s is worse than what I have (%s)\n", die, current);
					else if (!canDrop()) System.out.printf("Cannot grab %s as I've used my drop\n", die);
				}
			}
		}
		else if (debug) {
			System.out.printf("I'm planning to grab %s, so I won't consider %s\n",
				grabTask.get().spawn, die);
		}
	}

	private boolean isInteresting(Powerup powerup, double ev) {
		double interestRoll = rnd.nextDouble() * 100d;
		double hurryFactor = getHurryFactor();
		double interestThreshold = (2 * ev - 50) * hurryFactor; // this is okay
		boolean interesting = interestRoll < interestThreshold;
		if (debug) System.out.printf("%s %s: interestRoll = %.2f, threshold = %.2f, hurryFactor=%.2f\n",
			powerup, interesting ? "is interesting" : "is not interesting", interestRoll, interestThreshold, hurryFactor);
		return interesting;
	}
	
	private void instantSpawned(Instant instant) {
		if (instant instanceof DicemonTrainer) {
			DicemonTrainer trainer = (DicemonTrainer)instant;
			// still not 100% grabbed with hurry factor, but pretty often
			boolean interesting = isInteresting(trainer, 100); 
			if (hasPowerup() && getPowerup().isUpgradeable() && interesting) {
				scheduleGrab(instant);
			}
			else if (debug) {
				if (!hasPowerup())
					System.out.println("I don't have an item so I don't need a trainer");
			}
		}
	}

	private void scheduleGrab(Instant instant) {
		doScheduleGrab(instant, (ISpawnable spawn) -> {
			grab((Instant)spawn);
			this.grabTask = Optional.empty();
		});
	}
	
	private void scheduleGrab(BasicDie die, TestResult ranking) {
		doScheduleGrab(die, spawn -> {
			grab((Powerup)spawn, ranking);
			this.grabTask = Optional.empty();
		});
	}
	
	private void doScheduleGrab(Powerup powerup, Consumer<ISpawnable> task) {
		Calendar grabTime = randomGrabTime();
		String pattern = DateTimeUtils.toSchedulingPattern(grabTime);
		PowerupTask grabTask = new PowerupTask(grabTime.getTime(), powerup, task);
		grabTask.id = scheduler.schedule(pattern, grabTask);
		this.grabTask = Optional.of(grabTask);
		if (debug) System.out.println("Scheduled grab: " + grabTask);
	}

	private Calendar randomGrabTime() {
		Calendar time = Calendar.getInstance();
		time.add(Calendar.MINUTE, rnd.nextInt(20));
		return time;
	}
	
	private Calendar randomRollTime() {
		Calendar time = Calendar.getInstance();
		int minutes = 1 + rnd.nextInt(8);
		if (minutes == 5) minutes = 4; // avoid collision with autoroll
		time.add(Calendar.MINUTE, minutes);
		return time;
	}

	private String substitute(String dieName) {
		if (dieName.equals(FastDie.NAME)) {
			dieName = "Fast Die<br><small>(rolled within 10 s)</small>";
		}
		else if (dieName.equals(MasterDie.NAME)) { // The Master Die
			dieName = "Master Die";
		}
		else if (dieName.equals(HumongousDie.NAME)) {
			dieName = "Humongous Die";
		}
		return dieName;
	}

	private List<String> dieGrabPreTaunts = Arrays.asList(
		"I'll take that!",
		"That's a nice die.",
		"I'll add that to my collection.",
		"DIE DIE DIE! %1$s, that is.",
		"That thing has the roll EV of %3$.2f. Pretty nice, yes?",
		"I don't really need that to beat you, but I'll take it anyway."
		);
	
	private List<String> dieGrabPostTaunts = Arrays.asList(
		"Now it's mine.",
		"Eeexcellent!",
		"Next time grab faster, puny opponents.",
		"You know that upgrades into %2$s? With that, I will surely beat you!",
		"I don't always grab dice, but when I do, I pick the most unfair ones.",
		"You probably didn't know what that does. I'll tell you: %4$s");

	private List<String> grabCommands = Arrays.asList("grab", "get", "take");
	
	private void grab(Powerup<?> powerup, TestResult ranking) {
		if (!isAvailable(powerup)) return;
		
		boolean drop = hasPowerup();
		if (drop && !canDrop()) return;
		
		boolean preTaunt = rnd.nextBoolean();
		if (preTaunt) dieGrabTaunt(dieGrabPreTaunts, powerup, ranking);
		if (drop) sendChannel("drop");
		doGrab(powerup);
		if (!preTaunt) dieGrabTaunt(dieGrabPostTaunts, powerup, ranking);
	}
	
	private void grab(Instant instant) {
		// Abort silently if the item is taken now
		if (!isAvailable(instant)) return;
		
		doGrab(instant);
	}

	private boolean isAvailable(Powerup<?> powerup) {
		return noppaBot.getAvailablePowerups().contains(powerup);
	}

	private void doGrab(Powerup<?> powerup) {
		sendChannelFormat("%s %s", getRandom(grabCommands), powerup.name());
	}

	private void dieGrabTaunt(List<String> taunts, Powerup<?> powerup, TestResult ranking) {
		sendChannelFormat(getRandom(taunts),
			powerup.nameColored(), Color.evolvedPowerup(ranking.evolvesTo), ranking.ev, ranking.description);
	}
	
	private <T> T getRandom(List<T> list) {
		int i = rnd.nextInt(list.size());
		return list.get(i);
	}
	
	public class DelegatedTask extends Task implements Comparable<DelegatedTask> {
		public Runnable task;
		public Date time;
		public String id;
		
		public DelegatedTask(Date time, Runnable task) {
			this.time = time;
			this.task = task;
		}
		
		@Override
		public void execute(TaskExecutionContext context) {
			synchronized (lock) {
				scheduler.deschedule(id);
				task.run();
			}
		}
		
		@Override
		public String toString() {
			return String.format("[%tR] %s", time, super.toString());
		}
		
		@Override
		public int compareTo(DelegatedTask other) {
			return this.time.compareTo(other.time);
		}
	}
	
	public class PowerupTask extends Task implements Comparable<PowerupTask>, IColorStrConvertable {
		public ISpawnable spawn;
		public Consumer<ISpawnable> task;
		public Date time;
		public String id;
		
		public PowerupTask(Date time, ISpawnable spawn, Consumer<ISpawnable> task) {
			this.time = time;
			this.spawn = spawn;
			this.task = task;
		}
		
		@Override
		public void execute(TaskExecutionContext context) {
			synchronized (lock) {
				scheduler.deschedule(id);
				task.accept(spawn);
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
		public int compareTo(PowerupTask other) {
			return this.time.compareTo(other.time);
		}
	}
	
	@Override
	protected void onDisconnect() {
		if (quitting) dispose();
		else {
			System.out.println("Adversary: Disconnected from server. Trying to reconnect every 15 minute.");
			ReconnectTask rt = new ReconnectTask();
			final String id = scheduler.schedule("*/15 * * * *", rt);
			rt.id = id;
		}
	}
	
	public class ReconnectTask extends Task {
		public String id;
		
		@Override
		public void execute(TaskExecutionContext context) {
			synchronized (lock) {
				try {
					if (!isConnected()) {
						System.out.println("Adversary: Now reconnecting");
						reconnect();
//						joinChannel(channel);
						System.out.println("Adversary: Successfully reconnected!");
					}
					else scheduler.deschedule(id);
				}
				catch (Exception e) {
					System.out.println("Adversary: Reconnect failed: " + e.getMessage());
				}
			}
		}
	}
	
	private boolean canGrab() {
		return !isPlanningToGrab() && !hasPowerup();
	}

	private boolean isPlanningToGrab() {
		return grabTask.isPresent();
	}

	private boolean hasPowerup() {
		return noppaBot.getPowerups().containsKey(botNick);
	}
	
	private Powerup getPowerup() {
		return noppaBot.getPowerups().get(botNick);
	}
	
	private boolean canDrop() {
		return noppaBot.hasFavor(botNick) || noppaBot.getRules().canDropItems.get();
	}

	public double getHurryFactor() {
		LocalDateTime spawnStart = noppaBot.getSpawnStartTime();
		LocalDateTime spawnEnd = noppaBot.getSpawnEndTime();
		LocalDateTime now = LocalDateTime.now();
		Duration day = Duration.between(spawnStart, spawnEnd);
		Duration passed = Duration.between(spawnStart, now);
		double dayCompletion = (double)passed.getSeconds() / (double)day.getSeconds();
		return dayCompletion * 2d;
	}
	
	public boolean isOnChannel() {
		for (String c : getChannels()) {
			if (c.equals(channel)) return true;
		}
		return false;
	}

	@Override
	public void rollPeriodEnded() {
		if (isOnChannel()) {
			part();
		}
	}
}
