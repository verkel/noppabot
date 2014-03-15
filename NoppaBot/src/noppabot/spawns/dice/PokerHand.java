/*
 * Created on 14.3.2014
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.Color;
import noppabot.spawns.*;
import ca.ualberta.cs.poker.*;


public class PokerHand extends BasicPowerup {
	
//	private static HandEvaluator evaluator = new HandEvaluator();

	private Hand hand;
	
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

//	@Override
//	public void onPickup() {
//		bot.sendChannelFormat("%s grabs the cards. They are: %s", ownerColored, nameColored(), cardsToString());
//	}
	
	@Override
	public int onContestRoll() {
		try {
			Hand tableCards = bot.getPokerTable().cards;
			Hand combinedHand = new Hand(hand);
			for (int i = 1; i <= tableCards.size(); i++) {
				boolean added = combinedHand.addCard(tableCards.getCard(i));
				if (!added) System.out.printf("card n. %d was not added\n", i);
			}
//			combinedHand = evaluator.getBest5CardHand(combinedHand);
			combinedHand.sort();
			int handRank = HandEvaluator.rankHand(combinedHand);
			String handDesc = HandEvaluator.nameHand(handRank);
			
			int rankPoints = handRank / HandEvaluator.ID_GROUP_SIZE * 20;
			int bonusPoints = HandEvaluator.getSecondaryGrade(handRank); // My hack...
			int result = rankPoints + bonusPoints;
			String resultStr = resultStr(result);
			result = clamp(result);
			
//			bot.sendChannelFormat("%s reveals the cards: %s. Combined with the table cards, %s has %s, %s!",
//				owner, hand.toString(), owner, combinedHand.toString(), handDesc);
			bot.sendChannelFormat("%s reveals the cards: %s. Combined with the table cards, %s has %s!",
				owner, hand.toString(), owner, Color.emphasize(handDesc));
			bot.sendChannelFormat("%s's hand is worth %d + %d = %s points!", ownerColored, 
				rankPoints, bonusPoints, resultStr);
			
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
	
	@Override
	public String name() {
		return "Poker Hand";
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
	
	private void takeCards(int n) {
		Deck deck = bot.getPokerTable().deck;
		for (int i = 0; i < n && deck.cardsLeft() > 0; i++) {
			hand.addCard(deck.deal());
		}
		hand.sort();
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
	
	public class BetterHand extends EvolvedPowerup {
		private int gen = 0;
		
		private String[] names = {
			"Better Hand", "Much Improved Hand", "Third Hand's the Charm", "Sick Hand", "Hand of Long Ancestry", 
			"Hand of Leftover Cards", "Hand of Pure Desperation", "The One Hand... not really"
		};
		
		public BetterHand() {
			super(PokerHand.this);
			redraw();
		}
		
		private void redraw() {
			returnCards();
			takeCards(2);
		}
		
		@Override
		public boolean isUpgradeable() {
			return true;
		}
		
		@Override
		public Powerup upgrade() {
			redraw();
			gen++;
			return this;
		}
		
		@Override
		public String name() {
			return names[gen % names.length];
		}
		
		@Override
		public String getUpgradeDescription() {
			return String.format("While the trainer distracts the poker dealer, you swap your previous cards to: %s", hand);
		}
	}
}
