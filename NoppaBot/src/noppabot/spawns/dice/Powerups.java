/*
 * Created on 27.10.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import java.util.*;

import noppabot.NoppaBot;
import noppabot.spawns.Spawner;
import noppabot.spawns.events.*;
import noppabot.spawns.instants.*;

public class Powerups {

	public static Random powerupRnd = new Random();

	public static final Spawner<Powerup> allPowerups;
	public static final Spawner<Powerup> diceStormPowerups;
	public static final Spawner<Event> allEvents;
	public static final Spawner<Event> allEventsMinusFourthWall;
	
	static {
		List<Powerup> allPowerupsList = new ArrayList<Powerup>();
		List<Powerup> diceStormPowerupsList = new ArrayList<Powerup>();
		List<Event> allEventsList = new ArrayList<Event>();
		List<Event> allEventsMinusFourthWallList = new ArrayList<Event>();
		
		// Apprentice die is not put here, but can be spawned regardless if there are master dies
		allPowerupsList.addAll(Arrays.asList(new BagOfDice(), new DicemonTrainer(),
			new DicePirate(), new Diceteller(), new EnchantedDie(), new ExtremeDie(), new FastDie(),
			new GroundhogDie(), new LuckyDie(), new MasterDie(), new PolishedDie(), new PrimalDie(),
			new RollingProfessional(), new VolatileDie(), new WeightedDie()));

		diceStormPowerupsList.addAll(allPowerupsList);
		diceStormPowerupsList.removeAll(Arrays.asList(new DicemonTrainer(), new DicePirate()));
		
		allEventsList.addAll(Arrays.asList(new DiceMutation(), new DiceStorm(), new FourthWallBreaks()));
		
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
	
	// DupedDie -- choose the better one of two rolls
}