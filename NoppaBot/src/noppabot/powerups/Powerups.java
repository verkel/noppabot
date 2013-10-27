/*
 * Created on 27.10.2013
 * @author verkel
 */
package noppabot.powerups;

import java.util.*;

import noppabot.NoppaBot;


public class Powerups {
	private static List<Powerup> powerups = new ArrayList<Powerup>();
	private static Random powerupRnd = new Random();
	
	private static final Set<Integer> primes = new HashSet<Integer>();
	private static final List<Integer> dice;
	
	static {
		primes.addAll(Arrays.asList(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97));
		dice = Arrays.asList(4, 6, 8, 10, 12, 20);
	}
	
	private static int getSecondsAfterMidnight() {
		Calendar c = Calendar.getInstance();
		long now = c.getTimeInMillis();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		long passed = now - c.getTimeInMillis();
		long secondsPassed = passed / 1000;
		return (int)secondsPassed;
	}
	
	public static Powerup getRandom() {
		if (powerups.isEmpty()) bakeMore();
		Powerup powerup = powerups.remove(powerupRnd.nextInt(powerups.size()));
		return powerup;
	}
	
	public static abstract class Powerup {
		public void onSpawn(NoppaBot bot) {
		}
		
		public void onPickup(NoppaBot bot, String nick) {
		}
		
		public void onExpire(NoppaBot bot) {
		}
		
		public void onRollPeriodStart(NoppaBot bot, String nick) {
		}
		
		public void onNormalRoll(NoppaBot bot, String nick, int roll) {
		}	
		
		/**
		 * Say the appropriate roll message and return the modified roll
		 * @return a modified roll
		 */
		public int onContestRoll(NoppaBot bot, String nick, int roll) {
			bot.sendDefaultContestRollMessage(nick, roll);
			return roll;
		}
		
		/**
		 * Should the powerup be removed now?
		 */
		public boolean shouldRemove() {
			return true;
		}
	}

	public static void bakeMore() {
		// Polished die
		powerups.add(new Powerup() {
			public static final int bonus = 5;
			
			@Override
			public void onSpawn(NoppaBot bot) {
				bot.sendChannel("A polished die appears!");
			}
			
			@Override
			public void onPickup(NoppaBot bot, String nick) {
				bot.sendChannelFormat("%s grabs the polished die.", nick);
			}
			
			@Override
			public void onExpire(NoppaBot bot) {
				bot.sendChannelFormat("The polished die fades away.");
			}
			
			@Override
			public int onContestRoll(NoppaBot bot, String nick, int roll) {
				int result = roll + bonus;
				result = capResult(result);
				bot.sendChannelFormat("The polished die adds a nice bonus to %s's roll.", nick);
				bot.sendChannelFormat("%s rolls %d + %d = %d! %s", nick, roll, bonus, result, bot.grade(result));
				return result;
			}	
		});
		
		// Weighted die
		powerups.add(new Powerup() {
			public static final int bonus = 10;
			
			@Override
			public void onSpawn(NoppaBot bot) {
				bot.sendChannel("A weighted die appears!");
			}
			
			@Override
			public void onExpire(NoppaBot bot) {
				bot.sendChannelFormat("The weighted die falls into emptiness.");
			}
			
			@Override
			public void onPickup(NoppaBot bot, String nick) {
				bot.sendChannelFormat("%s grabs the weighted die.", nick);
			}
			
			@Override
			public int onContestRoll(NoppaBot bot, String nick, int roll) {
				int result = roll + bonus;
				result = capResult(result);
				bot.sendChannelFormat("%s's weighted die is so fairly weighted, that the roll goes very smoothly!", nick);
				bot.sendChannelFormat("%s rolls %d + %d = %d! %s", nick, roll, bonus, result, bot.grade(result));
				return result;
			}	
		});
		
		// Enchanted die
		powerups.add(new Powerup() {
			public static final int bonus = 15;
			
			@Override
			public void onSpawn(NoppaBot bot) {
				bot.sendChannel("An enchanted die appears!");
			}
			
			@Override
			public void onExpire(NoppaBot bot) {
				bot.sendChannelFormat("The spell on the enchanted die expires and nobody really wants it anymore.");
			}
			
			@Override
			public void onPickup(NoppaBot bot, String nick) {
				bot.sendChannelFormat("%s grabs the enchanted die and feels a tingling sensation at the fingertips.", nick);
			}
			
			@Override
			public int onContestRoll(NoppaBot bot, String nick, int roll) {
				int result = roll + bonus;
				result = capResult(result);
				bot.sendChannelFormat("The enchanted die grants %s either eternal fame and fortune, or a substantial roll bonus. %s chooses the latter.", nick, nick);
				bot.sendChannelFormat("%s rolls %d + %d = %d! %s", nick, roll, bonus, result, bot.grade(result));
				return result;
			}	
		});
		
		// Primal die
		powerups.add(new Powerup() {
			public static final int bonus = 20;
			
			@Override
			public void onSpawn(NoppaBot bot) {
				bot.sendChannel("A primal die appears!");
			}
			
			@Override
			public void onExpire(NoppaBot bot) {
				bot.sendChannelFormat("Nobody wanted the primal die. Now it disappears.");
			}
			
			@Override
			public void onPickup(NoppaBot bot, String nick) {
				bot.sendChannelFormat("%s grabs the primal die and gains knowledge about prime numbers.", nick);
			}
			
			@Override
			public int onContestRoll(NoppaBot bot, String nick, int roll) {
				if (primes.contains(roll)) {
					int result = roll + bonus;
					result = capResult(result);
					bot.sendChannelFormat("%s rolls %d, which is a prime! The primal die is pleased, and adds a bonus to the roll. The final roll is %d + %d = %d.", nick, roll, roll, bonus, result);
					return result;
				}
				else {
					bot.sendChannelFormat("%s rolls %d! It's not a prime, however, and the primal die disapproves.", nick, roll);
					return roll;
				}
			}	
		});
		
		// Lucky die
		powerups.add(new Powerup() {
			public static final int bonus = 25;
			
			@Override
			public void onSpawn(NoppaBot bot) {
				bot.sendChannel("A lucky die appears!");
			}
			
			@Override
			public void onExpire(NoppaBot bot) {
				bot.sendChannelFormat("The lucky die runs away.");
			}
			
			@Override
			public void onPickup(NoppaBot bot, String nick) {
				bot.sendChannelFormat("%s grabs the lucky die and it wishes good luck for the tonight's roll.", nick);
			}
			
			@Override
			public int onContestRoll(NoppaBot bot, String nick, int roll) {
				if (String.valueOf(roll).contains("7")) {
					int result = roll + bonus;
					result = capResult(result);
					bot.sendChannelFormat("%s rolls %d! The lucky die likes sevens in numbers, so it tinkers with your roll, making it %d + %d = %d. Lucky!", nick, roll, roll, bonus, result);
					return result;
				}
				else {
					bot.sendChannelFormat("%s rolls %d! The lucky die doesn't seem to like this number, though.", nick, roll);
					return roll;
				}
			}	
		});
		
		// Master die
		powerups.add(new Powerup() {
			@Override
			public void onSpawn(NoppaBot bot) {
				bot.sendChannel("The MASTER DIE appears!");
			}
			
			@Override
			public void onExpire(NoppaBot bot) {
				bot.sendChannelFormat("Nobody took the MASTER DIE?! Maybe you should learn some dice appraisal skills at the certified rolling professional...");
			}
			
			@Override
			public void onPickup(NoppaBot bot, String nick) {
				bot.sendChannelFormat("%s wrestles for the MASTER DIE with the other contestants and barely comes on top. Surely, the MASTER DIE must be worth all the trouble!", nick);
			}
			
			@Override
			public int onContestRoll(NoppaBot bot, String nick, int roll) {
				int result = bot.getRollFor(nick, 200);
				result = capResult(result);
				bot.sendChannelFormat("%s rolls d200 with the MASTER DIE...", nick);
				bot.sendDefaultContestRollMessage(nick, result);
				return result;
			}	
		});
		
		// Fast die
		powerups.add(new Powerup() {
			@Override
			public void onSpawn(NoppaBot bot) {
				bot.sendChannel("The fast die appears!");
			}
			
			@Override
			public void onExpire(NoppaBot bot) {
				bot.sendChannelFormat("It was too fast for you.");
			}
			
			@Override
			public void onPickup(NoppaBot bot, String nick) {
				bot.sendChannelFormat("%s quickly grabs the fast die! It asks you if you can throw it even faster when the time comes.", nick);
			}
			
			@Override
			public int onContestRoll(NoppaBot bot, String nick, int roll) {
				int seconds = getSecondsAfterMidnight();
				int bonus = Math.max(0, 30 - seconds);
				int result = roll + bonus;
				result = capResult(result);
				bot.sendChannelFormat("%s waited %d seconds before throwing the fast die.", nick, seconds);
				bot.sendChannelFormat("%s rolls %d + %d = %d! %s", nick, roll, roll, bonus, result, bot.grade(result));
				return result;
			}	
		});
		
		// Volatile die
		powerups.add(new Powerup() {
			private int rollNumber = 0;
			private int firstRoll;
			
			@Override
			public void onSpawn(NoppaBot bot) {
				bot.sendChannel("The volatile die appears!");
			}
			
			@Override
			public void onExpire(NoppaBot bot) {
				bot.sendChannelFormat("The volatile die grows unstable and explodes.");
			}
			
			@Override
			public void onPickup(NoppaBot bot, String nick) {
				bot.sendChannelFormat("%s grabs the volatile die! It glows with an aura of uncertainty.", nick);
			}
			
			@Override
			public int onContestRoll(NoppaBot bot, String nick, int roll) {
				rollNumber++;
				if (rollNumber == 1) {
					bot.sendDefaultContestRollMessage(nick, roll);
					bot.sendChannelFormat("%s's volatile die asks: \"Is that roll good? I can let you roll again, if you want...\"", nick);
					firstRoll = roll;
				}
				else {
					String taunt = roll >= firstRoll ? "Profit!" : "Damn it :P";
					bot.sendChannelFormat("%s rerolls with the volatile die. %d! %s", nick, roll, taunt);
				}
				return roll;
			}
			
			@Override
			public boolean shouldRemove() {
				return rollNumber > 1;
			}
		});
		
		// Symmetrical die
		powerups.add(new Powerup() {
			@Override
			public void onSpawn(NoppaBot bot) {
				bot.sendChannel("The symmetrical die appears!");
			}
			
			@Override
			public void onExpire(NoppaBot bot) {
				bot.sendChannelFormat("The symmetrical die is deemed too perfect for the contest and is disqualified.");
			}
			
			@Override
			public void onPickup(NoppaBot bot, String nick) {
				bot.sendChannelFormat("%s grabs the symmetrical die! The die is so even and smooth, that you begin to question how you could've ever seen other dice as symmetrical.", nick);
			}
			
			@Override
			public int onContestRoll(NoppaBot bot, String nick, int roll) {
				if (roll >= 50) {
					bot.sendChannelFormat("%s rolls %d with the symmetrical die. It's a nice number!", nick, roll);
					return roll;
				}
				else {
					int result = 100 - roll;
					bot.sendChannelFormat("%s rolls %d with the symmetrical die. This die likes low numbers as much as high numbers, so it kindly flips your roll to %d.", nick, roll, result);
					return result;
				}
			}
		});
		
		// Bag of dice
		powerups.add(new Powerup() {
			private List<Integer> diceBag = new ArrayList<Integer>();
			
			private void grabTenDice() {
				for (int i = 0; i < 10; i++) {
					Random diceRnd = new Random();
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
			public void onSpawn(NoppaBot bot) {
				grabTenDice();
				bot.sendChannel("A bag of dice appears!");
			}
			
			@Override
			public void onPickup(NoppaBot bot, String nick) {
				bot.sendChannelFormat("%s grabs the bag! It should be okay to use them tonight, just this once...", nick);
			}
			
			@Override
			public void onExpire(NoppaBot bot) {
				bot.sendChannelFormat("Who wants some random dice bag anyway. There could be anything in there...");
			}
			
			@Override
			public int onContestRoll(NoppaBot bot, String nick, int roll) {
				bot.sendChannelFormat("%s opens the dice bag. Inside he finds: %s", nick, diceToString());
				
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
				return result;
			}
		});
		
		// Rolling professional
		powerups.add(new Powerup() {
			public static final int minRoll = 50;
			
			@Override
			public void onSpawn(NoppaBot bot) {
				bot.sendChannel("A certified rolling professional appears!");
			}
			
			@Override
			public void onExpire(NoppaBot bot) {
				bot.sendChannelFormat("Nobody seems to want to polish their skills, so the rolling professional walks away.");
			}
			
			@Override
			public void onPickup(NoppaBot bot, String nick) {
				bot.sendChannelFormat("The rolling professional will assist %s in tonight's roll.", nick);
			}
			
			@Override
			public int onContestRoll(NoppaBot bot, String nick, int roll) {
				if (roll >= minRoll) {
					bot.sendDefaultContestRollMessage(nick, roll);
					bot.sendChannelFormat("The rolling professional compliments %s for the proper rolling technique.", nick);
					return roll;
				}
				else {
					bot.sendChannelFormat("The rolling professional notices that %s's rolling technique needs work! " +
						"%s was about to roll a lowly %d, but rolling pro shows how it's done and rolls %d!",
						nick, nick, roll, minRoll);
					return minRoll;
				}
			}	
		});
		
		// Diceteller
		powerups.add(new Powerup() {
			private int stashedRoll;
			private boolean rerolled = false;
			
			@Override
			public void onSpawn(NoppaBot bot) {
				bot.sendChannel("A Diceteller appears!");
			}
			
			@Override
			public void onExpire(NoppaBot bot) {
				bot.sendChannelFormat("No one seemed to believe the diceteller could see future rolls in a crystal ball. The ball was of wrong shape, anyway. Maybe next time she brings crystal dice?");
			}
			
			@Override
			public void onPickup(NoppaBot bot, String nick) {
				stashedRoll = bot.getRollFor(nick, 100);
				bot.sendChannelFormat("The diceteller, glancing at his crystal ball, whispers to %s: \"Your next roll will be %d.\"", nick, stashedRoll);
			}
			
			@Override
			public void onNormalRoll(NoppaBot bot, String nick, int roll) {
				rerolled = true;
			}	
			
			@Override
			public int onContestRoll(NoppaBot bot, String nick, int roll) {
				if (!rerolled) {
					bot.sendChannelFormat("%s rolls %d! %s", nick, stashedRoll, bot.grade(stashedRoll));
					bot.sendChannelFormat("%s recalls this was the exact roll foretold by the Diceteller.", nick);
					return stashedRoll;
				}
				else return super.onContestRoll(bot, nick, roll); // Normal behaviour
			}	
		});
	}
	
	private static int capResult(int roll) {
		return Math.min(100, roll);
	}
}

