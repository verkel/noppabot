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

	private List<Integer> grabSomeDice(int count) {
		List<Integer> grabbed = new ArrayList<Integer>();
		for (int i = 0; i < count; i++) {
			int die = dice.get(Powerups.powerupRnd.nextInt(dice.size()));
			grabbed.add(die);
		}
		return grabbed;
	}

	private String bagToString() {
		return diceToString(diceBag);
	}
	
	private String diceToString(Collection<Integer> diceToStr) {
		StringBuilder buf = new StringBuilder();
		boolean first = true;
		for (int die : diceToStr) {
			if (!first) buf.append(", ");
			buf.append("d" + die);
			first = false;
		}
		return buf.toString();
	}

	@Override
	public void initialize(INoppaBot bot) {
		int count = 1 + Powerups.powerupRnd.nextInt(8);
		diceBag.addAll(grabSomeDice(count));
		Collections.sort(diceBag);
	}
	
	@Override
	public void onSpawn(INoppaBot bot) {
		bot.sendChannel("A bag of dice appears!");
	}

	@Override
	public void onPickup(INoppaBot bot, String nick) {
		bot.sendChannelFormat("%s grabs and opens the dice bag! It contains the following dice: %s", nick, bagToString());

	}

	@Override
	public void onExpire(INoppaBot bot) {
		bot.sendChannelFormat("... who wants some random dice bag anyway. There could be anything in there.");
	}

	@Override
	public int onContestRoll(INoppaBot bot, String nick, int roll) {
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
		result = clamp(result);

		bot.sendChannelFormat("%s rolls with the dice (%s). %s = %d", nick, bagToString(), resultStr, result);
//		System.out.println(result);
		return result;
	}
	
	@Override
	public String getName() {
		return "Bag of Dice";
	}
	
	@Override
	public boolean isUpgradeable() {
		return true;
	}
	
	@Override
	public Powerup upgrade() {
		return new BagOfManyDice();
	}
	
	// Upgrade
	public class BagOfManyDice extends Powerup {
		
		private String manyMany = "Many";
		private List<Integer> newDice;
		
		public BagOfManyDice() {
			putMoreDice();
		}
		
		private void putMoreDice() {
			newDice = grabSomeDice(2);
			diceBag.addAll(newDice);
			Collections.sort(newDice);
			Collections.sort(diceBag);
		}
		
		@Override
		public int onContestRoll(INoppaBot bot, String nick, int roll) {
			return BagOfDice.this.onContestRoll(bot, nick, roll);
		}
		
		@Override
		public String getName() {
			return "Bag of " + manyMany + " Dice";
		}
		
		@Override
		public boolean isUpgradeable() {
			return true;
		}
		
		@Override
		public Powerup upgrade() {
			manyMany = "Many " + manyMany;
			putMoreDice();
			return this;
		}
		
		@Override
		public String getUpgradeDescription(INoppaBot bot, String nick) {
			return String.format("You look inside the bag, and it now contains two additional dice: %s",
				diceToString(newDice));
		}
	}
}