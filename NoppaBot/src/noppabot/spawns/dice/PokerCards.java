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
//		int roll = roll();
//		int result = roll + bonus;
//		String resultStr = resultStr(result);
//		result = clamp(result);
//		bot.sendChannelFormat(
//			"The enchanted die grants %s either eternal fame and fortune, or a substantial roll bonus. %s chooses the latter.",
//			owner, owner);
//		bot.sendChannelFormat("%s rolls %d + %d = %s! %s", ownerColored, roll, bonus, resultStr,
//			bot.grade(result));
		
//		Hand combinedHand = evaluator.getBest5CardHand(hand);
		Hand tableCards = bot.getPokerTableCards();
		Hand combinedHand = new Hand(hand);
		for (int i = 0; i < tableCards.size(); i++) {
			combinedHand.addCard(tableCards.getCard(i));
		}
		combinedHand = evaluator.getBest5CardHand(combinedHand);
		int handRank = HandEvaluator.rankHand(hand);
		String handDesc = HandEvaluator.nameHand(handRank);
		
		int result = handRank / HandEvaluator.ID_GROUP_SIZE * 15;
		String resultStr = resultStr(result);
		result = clamp(result);
		
		bot.sendChannelFormat("%s reveals the cards:%s. Combined with the table cards, %s has %s, %s! " +
			"This hand is worth %s points.", ownerColored, hand.toString(), owner, 
			combinedHand.toString(), handDesc, String.valueOf(handRank));//resultStr);
		
		return result;
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
		return new CheatersCards();
	}
	
	private void takeCards(int n) {
		for (int i = 0; i < 2 && deck.cardsLeft() > 0; i++) {
			hand.addCard(deck.dealCard());
		}
	}
	
	public class CheatersCards extends EvolvedPowerup {
		public CheatersCards() {
			super(PokerCards.this);
		}
		
		@Override
		public int onContestRoll() {
			return 0;
		}
		
		@Override
		public String name() {
			return "Cheater's Cards";
		}
		
		@Override
		public String getUpgradeDescription() {
			return String.format("todo", 777);
		}
	}
}
