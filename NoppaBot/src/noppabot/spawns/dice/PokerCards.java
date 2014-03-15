/*
 * Created on 14.3.2014
 * @author verkel
 */
package noppabot.spawns.dice;

import noppabot.spawns.*;
import ca.ualberta.cs.poker.*;


public class PokerCards extends BasicPowerup {
	
	private static HandEvaluator evaluator = new HandEvaluator();

	private Deck deck;
	private Hand hand;
	
	public PokerCards(Deck deck) {
		this.deck = deck;
	}
	
	@Override
	public void onInitialize() {
		hand = new Hand();
		takeCards(2);
	}
	
	@Override
	public void onSpawn() {
		bot.sendChannelFormat("Pair of %s appear!", nameColored());
	}

	@Override
	public void onExpire() {
		sendExpireMessageFormat("... the poker cards are dealt to a game of Solitaire.");
	}

//	@Override
//	public void onPickup() {
//		bot.sendChannelFormat("%s grabs the cards. They are: %s", ownerColored, nameColored(), cardsToString());
//	}
	
	@Override
	public int onContestRoll() {
		try {
			Hand tableCards = bot.getPokerTableCards();
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
				owner, hand.toString(), owner, handDesc);
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
		return "Poker Cards";
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
		return new PocketAces();
	}
	
	private void takeCards(int n) {
		for (int i = 0; i < n && deck.cardsLeft() > 0; i++) {
			hand.addCard(deck.dealCard());
		}
		hand.sort();
	}
	
	public class PocketAces extends EvolvedPowerup {
		public PocketAces() {
			super(PokerCards.this);
			int suit = Powerups.powerupRnd.nextInt(4);
			hand = new Hand();
			hand.addCard(new Card(Card.ACE, suit));
			hand.addCard(new Card(Card.ACE, suit));
		}
		
		@Override
		public String name() {
			return "Pocket Aces";
		}
		
		@Override
		public String getUpgradeDescription() {
			return String.format("todo", 777);
		}
	}
}
