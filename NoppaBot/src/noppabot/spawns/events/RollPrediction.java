/*
 * Created on 31.3.2015
 * @author verkel
 */
package noppabot.spawns.events;

import noppabot.*;
import noppabot.spawns.Event;
import adversary.*;
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
		"the dice seem to be not working very well -- maybe your rolling certificates have expired?",
		"some friendly fellow visited and checked weightings on your dice, he looked very professional and all!",
		"if you get to double digits today, consider buying some lottery tickets."
	};
	
	private static final Bias[] goodBiases = {
		new Bias(0.9, true),
		new Bias(0.8, true),
		new Bias(0.7, true),
		new Bias(0.6, true),
		new Bias(0.5, true),
	};
	
	private static final Bias[] badBiases = {
		new Bias(0.9, false),
		new Bias(0.8, false),
		new Bias(0.7, false),
		new Bias(0.6, false),
		new Bias(0.5, false),
	};
	
	@Override
	public void run(INoppaBot bot) {
		bot.sendChannelFormat("It's the Diceteller's %s in today's Roll News!", Color.event("roll'o'scope"));
		String fortune = "\"" + fortunes.random();
		for (String line : fortune.split("\\n")) {
			bot.sendChannel(line);
		}
		boolean good = Math.random() < 0.5;
		WeightedRandom rnd = new WeightedRandom(0.75, false); // bias lower numbers
		int n = rnd.roll(5).intValue();
		Bias[] biases = good ? goodBiases : badBiases;
		String[] predictions = good ? goodPredictions : badPredictions;
		Bias bias = biases[n];
		String prediction = predictions[n];
		bot.sendChannelFormat("Also, %s\"", Color.rulesMode(prediction));
		bot.getRules().bias.set(bias);
	}
	
	@Override
	public String name() {
		return "Roll Prediction";
	}
}
