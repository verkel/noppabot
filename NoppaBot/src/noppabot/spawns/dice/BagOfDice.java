/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.dice;

import java.util.*;

import noppabot.spawns.*;
import noppabot.spawns.Spawner.SpawnInfo;

import com.google.common.collect.*;

public class BagOfDice extends BasicPowerup {
	
	public static final BasicPowerupSpawnInfo info = new BasicPowerupSpawnInfo() {

		@Override
		public BasicPowerup create() {
			return new BagOfDice();
		}
		
	};

	@Override
	public SpawnInfo<?> spawnInfo() {
		return info;
	}
	
	private static final List<Integer> dice;
	
	static {
		List<Integer> lowDice = Arrays.asList(4, 6, 8, 10, 12, 20);
		dice = new ArrayList<Integer>();
		dice.addAll(lowDice);
		dice.add(100);
	}

	private SortedMultiset<Integer> diceBag = TreeMultiset.create();

	private SortedMultiset<Integer> grabSomeDice(int count) {
		SortedMultiset<Integer> grabbed = TreeMultiset.create();
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
	public void onInitialize() {
		int count = 1 + Powerups.powerupRnd.nextInt(8);
		diceBag.addAll(grabSomeDice(count));
	}
	
	@Override
	public void onSpawn() {
		bot.sendChannelFormat("A %s appears!", nameColored());
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat("%s grabs and opens the %s! It contains the following dice: %s", 
			ownerColored, nameColored(), bagToString());

	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... who wants some random dice bag anyway. There could be anything in there.");
	}

	@Override
	public int onContestRoll() {
		StringBuilder buf = new StringBuilder();
		boolean first = true;
		int result = 0;
		for (int die : diceBag) {
			int subroll = bot.getRoll(owner, die);
			result += subroll;
			if (!first) buf.append(" + ");
			buf.append(subroll);
			first = false;
		}
		String sumStr = buf.toString();
		String resultStr = resultStr(result);
		result = clamp(result);

		bot.sendChannelFormat("%s rolls with the dice (%s). %s = %s", ownerColored, bagToString(), sumStr, resultStr);
//		System.out.println(result);
		return result;
	}
	
	@Override
	public String name() {
		return "Bag of Dice";
	}
	
	@Override
	public String details() {
		return bagToString();
	}
	
	@Override
	public int sides() {
		return diceBag.lastEntry().getElement();
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
	public class BagOfManyDice extends EvolvedPowerup {
		
		private String manyMany = "Many";
		private SortedMultiset<Integer> newDice;
		
		public BagOfManyDice() {
			super(BagOfDice.this);
			putMoreDice();
		}
		
		private void putMoreDice() {
			newDice = grabSomeDice(2);
			diceBag.addAll(newDice);
		}
		
		@Override
		public int onContestRoll() {
			return BagOfDice.this.onContestRoll();
		}
		
		@Override
		public String name() {
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
		public String getUpgradeDescription() {
			return String.format("You look inside the bag, and it now contains two additional dice: %s",
				diceToString(newDice));
		}
	}
}