/*
 * Created on 31.3.2015
 * @author verkel
 */
package noppabot.spawns.events;

import java.util.HashMap;
import java.util.Map;

import noppabot.Color;
import noppabot.INoppaBot;
import noppabot.spawns.Event;
import adversary.WeightedRandom;
import adversary.WeightedRandom.Bias;


public class RollPrediction extends Event {
	public static final EventSpawnInfo info = new EventSpawnInfo() {

		@Override
		public Event create() {
			return new RollPrediction();
		}
		
		@Override
		public boolean spawnInLateEvents() {
			return false;
		}
		
		@Override
		public double spawnChance() {
			return 2.0;
		}
	};
	
	private static Fortunes fortunes = new Fortunes();
	
	private static final String[] goodPredictions = {
		"it's a good day to roll.",
		"your dice will fly with ease today.",
		"there is something magical in the roll atmosphere today.",
		"today the competitors will be met with exceptional luck!",
		"your rolls will reach the moon and beyond!"
	};
	
	private static final String[] badPredictions = {
		"slightly below average rolls are to be expected.",
		"you might have some difficulties in rolling today.",
		"the dice seem to be wildly misbehaving. I tried to scold them -- didn't help.",
		"some friendly fellow visited and checked weightings on your dice, he looked very professional and all!",
		"if you get to double digits today, consider buying some lottery tickets."
	};
	
	private static final Bias[] goodBiases = {
		new Bias(0.8, true),
		new Bias(0.6, true),
		new Bias(0.4, true),
		new Bias(0.2, true),
		new Bias(0.1, true),
	};
	
	private static final Bias[] badBiases = {
		new Bias(0.8, false),
		new Bias(0.6, false),
		new Bias(0.4, false),
		new Bias(0.2, false),
		new Bias(0.05, false),
	};
	
	private static final Map<Bias, Integer> evs = new HashMap<>();
	static {
		evs.put(goodBiases[0], 56);
		evs.put(goodBiases[1], 63);
		evs.put(goodBiases[2], 72);
		evs.put(goodBiases[3], 84);
		evs.put(goodBiases[4], 91);
		
		evs.put(badBiases[0], 45);
		evs.put(badBiases[1], 38);
		evs.put(badBiases[2], 29);
		evs.put(badBiases[3], 17);
		evs.put(badBiases[4], 5);
	}
	
	@Override
	public void run(INoppaBot bot) {
		bot.sendChannelFormat("It's the Diceteller's %s in today's Roll News!", Color.event("roll'o'scope"));
		String fortune = "\"" + fortunes.random();
		for (String line : fortune.split("\\n")) {
			bot.sendChannel(line);
		}
		boolean good = Math.random() < 0.5;
		WeightedRandom rnd = new WeightedRandom(0.85, false); // bias lower numbers
		int n = rnd.roll(5).total() - 1;
		Bias[] biases = good ? goodBiases : badBiases;
		String[] predictions = good ? goodPredictions : badPredictions;
		Bias bias = biases[n];
		String prediction = predictions[n];
		bot.sendChannelFormat(" Also, %s\"", Color.rulesMode(prediction));
		bot.getRules().bias.set(bias);
	}
	
	@Override
	public String name() {
		return "Roll Prediction";
	}
	
	public static int getEV(Bias b) {
		return evs.get(b);
	}
}
