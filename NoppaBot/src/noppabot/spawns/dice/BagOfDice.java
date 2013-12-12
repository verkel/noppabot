/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import java.util.*;

import noppabot.INoppaBot;

public class BagOfDice extends Powerup {
	
	private static final List<Integer> dice;
	
	static {
		List<Integer> lowDice = Arrays.asList(4, 6, 8, 10, 12, 20);
		dice = new ArrayList<Integer>();
		dice.addAll(lowDice);
		dice.add(100);
	}

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
//		System.out.println(result);
		return result;
	}
	
	@Override
	public String getName() {
		return "Bag of Dice";
	}
}