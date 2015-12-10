/*
 * Created on 27.10.2013
 * @author verkel
 */
package noppabot.spawns;

import java.util.*;

import noppabot.INoppaBot;
import noppabot.spawns.BasicPowerup.BasicPowerupSpawnInfo;
import noppabot.spawns.Event.EventSpawnInfo;
import noppabot.spawns.Spawner.LastSpawn;
import noppabot.spawns.dice.*;
import noppabot.spawns.events.*;
import noppabot.spawns.instants.*;

public class Powerups {

	public static Random powerupRnd = new Random();

	public static final Spawner<BasicPowerup> allPowerups;
	public static final Spawner<BasicPowerup> firstPowerup;
	public static final Spawner<BasicPowerup> diceStormPowerups;
	public static final Spawner<BasicPowerup> diceBrosPowerups;
	public static final Spawner<Event> allEvents;
	public static final Spawner<Event> allEventsMinusFourthWall;
	public static final Spawner<Event> lateEvents;
	
	static {
		List<BasicPowerupSpawnInfo> allPowerupInfos = Arrays.asList(
			BagOfDice.info,
			DicemonTrainer.info,
			DiceBros.info,
			DicePirate.info,
			DiceRecycler.info,
			Diceteller.info,
			DivisibleDie.info,
			EnchantedDie.info,
			ExtremeDie.info,
			FastDie.info,
			ImitatorDie.info,
			HumongousDie.info,
			LuckyDie.info,
			MasterDie.info,
			PokerDealer.info,
			PolishedDie.info,
			PrimalDie.info,
			RollingProfessional.info,
			SteadyDie.info,
			SwapperDie.info,
			TrollingProfessional.info,
			VariableDie.info,
			WeightedDie.info
		);
		
		List<EventSpawnInfo> allEventInfos = new ArrayList<>(Arrays.asList(
			DiceMutation.info,
			DiceStorm.info,
			FavorRefresh.info,
			FourthWallBreaks.info,
			RollPrediction.info
		));
		allEventInfos.addAll(RulesChange.allInfos);
		
		LastSpawn<BasicPowerup> lastPowerup = new LastSpawn<BasicPowerup>();
		LastSpawn<Event> lastEvent = new LastSpawn<Event>();
		
		allPowerups = Spawner.create(allPowerupInfos, lastPowerup, i -> i.spawnInAllPowerups());
		firstPowerup = Spawner.create(allPowerupInfos, lastPowerup, i -> i.spawnInFirstPowerups());
		diceStormPowerups = Spawner.create(allPowerupInfos, lastPowerup, i -> i.spawnInDiceStormPowerups());
		diceBrosPowerups = Spawner.create(allPowerupInfos, lastPowerup, i -> i.spawnInDiceBrosPowerups());
		allEvents = Spawner.create(allEventInfos, lastEvent, i -> i.spawnInAllEvents());
		allEventsMinusFourthWall = Spawner.create(allEventInfos, lastEvent, i -> i != FourthWallBreaks.info);
		lateEvents = Spawner.create(allEventInfos, lastEvent, i -> i.spawnInLateEvents());
	}
	
	public static ISpawnable getRandomPowerupOrEvent(INoppaBot bot, Spawner<BasicPowerup> spawnPowerups, Spawner<Event> spawnEvents) {
		if (spawnEvents != null && powerupRnd.nextFloat() < 0.10f) {
			return getRandomEvent(spawnEvents);
		}
		else {
			return getRandomPowerup(bot, spawnPowerups);
		}
	}
	
	public static Powerup getRandomPowerup(INoppaBot bot, Spawner<BasicPowerup> spawnPowerups) {
		Powerup powerup = getRandomBasicPowerup(bot, spawnPowerups);
		if (bot.getRules().upgradedSpawns.get()) powerup = powerup.upgrade();
		return powerup;
	}
	
	public static BasicPowerup getRandomBasicPowerup(INoppaBot bot, Spawner<BasicPowerup> spawnPowerups) {
		BasicPowerup powerup = spawnPowerups.spawn();
		powerup.initialize(bot);
		return powerup;
	}
	
	public static Event getRandomEvent(Spawner<Event> spawnEvents) {
		Event event = spawnEvents.spawn();
		return event;
	}
}
