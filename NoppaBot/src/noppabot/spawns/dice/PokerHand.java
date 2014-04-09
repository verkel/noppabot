/*
 * Created on 14.3.2014
 * @author verkel
 */
package noppabot.spawns.dice;

import static ca.ualberta.cs.poker.HandEvaluator.*;
import noppabot.Color;
import noppabot.spawns.*;
import ca.ualberta.cs.poker.*;

import com.google.common.base.Objects;

public class PokerHand extends BasicPowerup {
	
//	private static HandEvaluator evaluator = new HandEvaluator();

	private Hand hand;
	private HandRank lastHandRank;
	private boolean handRankChanged = true;
	
	private static int[] handRankBonuses = new int[NUM_HANDS];
	
//	static {
//		handRankBonuses[HIGH] = 0;
//		handRankBonuses[PAIR] = 50;
//		handRankBonuses[TWOPAIR] = 70;
//		handRankBonuses[THREEKIND] = 80;
//		handRankBonuses[STRAIGHT] = 90;
//		handRankBonuses[FLUSH] = 95;
//		handRankBonuses[FULLHOUSE] = 100;
//		handRankBonuses[FOURKIND] = 120;
//		handRankBonuses[STRAIGHTFLUSH] = 140;
//		handRankBonuses[FIVEKIND] = 200; // Not possible without Jokers
//	}
	
	static {
		handRankBonuses[HIGH] = 0;
		handRankBonuses[PAIR] = 10;
		handRankBonuses[TWOPAIR] = 20;
		handRankBonuses[THREEKIND] = 30;
		handRankBonuses[STRAIGHT] = 40;
		handRankBonuses[FLUSH] = 50;
		handRankBonuses[FULLHOUSE] = 60;
		handRankBonuses[FOURKIND] = 100;
		handRankBonuses[STRAIGHTFLUSH] = 200;
		handRankBonuses[FIVEKIND] = 200; // Not possible without Jokers
	}
	
	public PokerHand() {
	}
	
	@Override
	public void onInitialize() {
		hand = new Hand();
		takeCards(2);
	}
	
	@Override
	public void onSpawn() {
		bot.sendChannelFormat("A %s appears!", nameColored());
	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... the poker hand is re-used in a game of Solitaire.");
	}

	@Override
	public void onPickup() {
		bot.sendChannelFormat("%s grabs the %s. It contains: %s", ownerColored, nameColored(), cardsToString());
	}
	
	@Override
	public int onContestRoll() {
		try {
			int roll = roll();
			HandRank rank = new HandRank();
			
			int rankPoints;
			if (rank.type == 0) {
				rankPoints = Math.max(0, rank.highCardBonus - 8);
			}
			else {
				rankPoints = handRankBonuses[rank.type];
			}
			int result = roll + rankPoints;
			String resultStr = resultStr(result);
			result = clamp(result);
			
//			bot.sendChannelFormat("%s reveals the cards: %s. Combined with the table cards, %s has %s, %s!",
//				owner, hand.toString(), owner, combinedHand.toString(), handDesc);
			bot.sendChannelFormat("%s reveals the cards: %s. %s has %s! This hand gives a +%d bonus to the roll.",
				owner, hand.toString(), owner, Color.emphasize(rank.name), rankPoints);
//			bot.sendChannelFormat("%s's hand is worth %d + %d = %s points!", ownerColored, 
//				rankPoints, bonusPoints, resultStr);
			
			bot.sendChannelFormat("%s rolls %s + %s = %s! %s", ownerColored(), roll, rankPoints, resultStr, bot.grade(result));
			
			return result;
		}
		catch (Exception e) {
			e.printStackTrace();
			bot.sendChannelFormat("%s: sorry, I couldn't calculate value of your hand. :( " +
				"It was: %s. Let's roll normal d100 instead.", owner, hand);
			return super.onContestRoll();
		}
	}
	
	public String cardsToString() {
		return hand.toString();
	}
	
	public HandRank getHandRank() {
		HandRank hr = new HandRank();
		handRankChanged = !Objects.equal(lastHandRank, hr);
		lastHandRank = hr;
		return hr;
	}
	
	public boolean isHandRankChanged() {
		return handRankChanged;
	}
	
	@Override
	public String name() {
		return "Poker Hand";
	}
	
	@Override
	public String details() {
		return cardsToString();
	}
	
	@Override
	public int sides() {
		return 100;
	}
	
	@Override
	public boolean isUpgradeable() {
		return true;
	}
	
	@Override
	public Powerup upgrade() {
		return new BetterHand();
	}
	
	/**
	 * Do this when the hand is replaced by another
	 */
	public void trash() {
		returnCards();
	}
	
	private void returnCards() {
		Deck deck = bot.getPokerTable().deck;
		for (int i = 1; i <= hand.size(); i++) {
			Card card = hand.getCard(i);
			deck.replaceCard(card);
		}
		deck.shuffle();
		hand.makeEmpty();
	}
	
	private void takeCards(int n) {
		Deck deck = bot.getPokerTable().deck;
		for (int i = 0; i < n && deck.cardsLeft() > 0; i++) {
			hand.addCard(deck.deal());
		}
		hand.sort();
	}
	
	public class HandRank {
		public HandRank() {
			Hand tableCards = bot.getPokerTable().cards;
			Hand combinedHand = new Hand(hand);
			for (int i = 1; i <= tableCards.size(); i++) {
				boolean added = combinedHand.addCard(tableCards.getCard(i));
				if (!added) System.out.printf("card n. %d was not added\n", i);
			}
			combinedHand.sort();
			int handRankMask = HandEvaluator.rankHand(combinedHand);
			name = HandEvaluator.nameHand(handRankMask);
			
			type = handRankMask / HandEvaluator.ID_GROUP_SIZE;
			highCardBonus = HandEvaluator.getSecondaryGrade(handRankMask);
		}
		
		public String toString(boolean listFormat, boolean now) {
			String nowStr = now ? " now" : "";
			if (listFormat) return String.format("%s%s has %s: %s!", Color.nick(Color.antiHilight(owner)),
				nowStr, cardsToString(), Color.emphasize(name));
			return String.format("%s%s has %s!", owner, nowStr, Color.emphasize(name));
		}
		
		public String name;
		public int type;
		public int highCardBonus;
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + highCardBonus;
			result = prime * result + type;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			HandRank other = (HandRank)obj;
			if (!getOuterType().equals(other.getOuterType())) return false;
			if (highCardBonus != other.highCardBonus) return false;
			if (type != other.type) return false;
			return true;
		}

		private PokerHand getOuterType() {
			return PokerHand.this;
		}
	}
	
	public class BetterHand extends EvolvedPowerup {
		private int gen = 0;
		
		private String[] names = {
			"Better Hand", "Much Improved Hand", "Third Hand's the Charm", "Sick Hand", "Master Hand",
			"The One Hand", "Hand of Long Ancestry", "Hand of Tireless Iteration"
		};
		
		public BetterHand() {
			super(PokerHand.this);
			takeHigherCards();
		}
		
		public void trash() {
			returnCards();
		}
		
		private void takeHigherCards() {
			Deck deck = bot.getPokerTable().deck;
			Hand newHand = new Hand();
			
			// For both cards
			for (int i = 1; i <= 2; i++) {
				boolean tookHigherCard = false;
				Card oldCard = hand.getCard(i);
				int rank = oldCard.getRank();
				
				// Find first rank that has available card
				while (!tookHigherCard && rank < Card.ACE) {
					rank = Math.min(rank + 1, Card.ACE);
					int prevSuit = oldCard.getSuit();
					int suit = prevSuit;
					
					// Find first suit within that rank that has available card
					do {
						Card newCard = new Card(rank, suit);
						boolean cardAvailable = deck.findCard(newCard) != -1;
						
						// Add the new card to newHand and swap cards to the deck
						if (cardAvailable) {
							deck.extractCard(newCard);
							deck.replaceCard(oldCard);
							newHand.addCard(newCard);
							tookHigherCard = true;
							break;
						}
						suit = (suit + 1) % 4;
					} while (suit != prevSuit);
				}
				
				// If we didn't take a higher card (one was not available or we had an ace),
				// just leave the old card to the newHand
				if (!tookHigherCard) newHand.addCard(oldCard);
			}
			
			hand = newHand;
		}
		
		@Override
		public boolean isUpgradeable() {
			return true;
		}
		
		@Override
		public Powerup upgrade() {
			takeHigherCards();
			gen++;
			return this;
		}
		
		@Override
		public String name() {
			return names[gen % names.length];
		}
		
		public HandRank getHandRank() {
			return PokerHand.this.getHandRank();
		}
		
		public boolean isHandRankChanged() {
			return PokerHand.this.isHandRankChanged();
		}
		
		@Override
		public String getUpgradeDescription() {
//			return String.format("Your cards increase in rank, you now have: %s", hand);
			return String.format("Your cards increase in rank. %s", getHandRank().toString(true, true));
		}
	}
}
