/*
 * Created on 15.3.2014
 * @author verkel
 */
package noppabot;

import java.util.*;

import noppabot.spawns.Powerup;
import noppabot.spawns.dice.*;
import noppabot.spawns.dice.PokerHand.BetterHand;
import noppabot.spawns.dice.PokerHand.HandRank;
import ca.ualberta.cs.poker.*;

/**
 * Shared cards for playing poker on the side of throwing dice
 * 
 * @author verkel
 */
public class PokerTable {
	
	public Deck deck;
	public Hand flop; // First 3 cards
	public Card turn; // Fourth card
	public Card river; // Fifth card
	public Hand cards; // All the cards
	public boolean gameStarted = false;
	
	private static final int TURN_HOURS = 18;
	private static final int RIVER_HOURS = 22;
	private static final String TURN_SCHEDULE_STR = String.format("0 %d * * *", TURN_HOURS);
	private static final String RIVER_SCHEDULE_STR = String.format("0 %d * * *", RIVER_HOURS);
	
	private INoppaBot bot;
	
	
	public PokerTable(INoppaBot bot) {
		this.bot = bot;
		clear();
	}
	
	public void onDealerSpawned() {
		if (gameStarted) return;

		createDeck();
		
		tryRevealTableCards(false);
		gameStarted = true;
	}

	public void tryRevealTableCards(boolean verbose) {
		Calendar now = Calendar.getInstance();
		int hours = now.get(Calendar.HOUR_OF_DAY);
		boolean isRollPeriod = bot.getState() == INoppaBot.State.ROLL_PERIOD;
		
		revealFlop();
		
		if (isRollPeriod || hours >= TURN_HOURS) revealTurn(verbose);
		else if (!gameStarted) scheduleTurnReveal();
		
		if (isRollPeriod || hours >= RIVER_HOURS) revealRiver(verbose);
		else if (!gameStarted) scheduleRiverReveal();
	}

	public void scheduleTurnReveal() {
		TurnRevealTask task = new TurnRevealTask();
		task.id = bot.getScheduler().schedule(TURN_SCHEDULE_STR, task);
	}
	
	private class TurnRevealTask implements Runnable {
		public String id;
		
		@Override
		public void run() {
			synchronized (bot.getLock()) {
				revealTurn(true);
				bot.getScheduler().deschedule(id);
			}
		}		
	}
	
	public void scheduleRiverReveal() {
		RiverRevealTask task = new RiverRevealTask();
		task.id = bot.getScheduler().schedule(RIVER_SCHEDULE_STR, task);
	}
	
	private class RiverRevealTask implements Runnable {
		public String id;
		
		@Override
		public void run() {
			synchronized (bot.getLock()) {
				revealRiver(true);
				bot.getScheduler().deschedule(id);
			}
		}		
	}
	
	public void clear() {
		gameStarted = false;
		flop = null;
		turn = null;
		river = null;
		cards = null;
		lastHandRanks.clear();
	}
	
	public void revealFlop() {
		if (deck == null || flop != null) return;
		flop = new Hand();
		for (int i = 0; i < 3; i++) {
			Card card = deck.deal();
			flop.addCard(card);
		}
		flop.sort();
		updateCards();
	}
	
	public void revealTurn(boolean verbose) {
		if (deck == null || turn != null) return;
		turn = deck.deal();
		updateCards();
		if (verbose) {
			bot.sendChannelFormat("I'll reveal tonight's turn card!");
			bot.sendChannelFormat("The poker table now contains: %s", cardsToString());
			listHands(true);
		}
	}

	public void revealRiver(boolean verbose) {
		if (deck == null || river != null) return;
		river = deck.deal();
		updateCards();
		if (verbose) {
			bot.sendChannelFormat("I'll reveal tonight's river card!");
			bot.sendChannelFormat("The poker table now contains: %s", cardsToString());
			listHands(true);
		}
	}
	
	public String cardsToString() {
		StringBuilder sb = new StringBuilder();
		if (flop != null) {
			sb.append(flop);
		}
		if (turn != null) {
			sb.append(" + ").append(turn);
		}
		if (river != null) {
			sb.append(" + ").append(river);
		}
		return sb.toString();
	}
	
	public void createDeck() {
		deck = new Deck(System.currentTimeMillis());
		deck.shuffle();
	}
	
	private Map<String, HandRank> lastHandRanks = new HashMap<String, HandRank>();
	
	public void listHands(final boolean onTurnOrRiver) {
		boolean first = true;
		StringBuilder sb = new StringBuilder();
		for (Powerup p : bot.getPowerups().values()) {
			if (!(p instanceof PokerHand || p instanceof BetterHand)) continue;
			HandRank handRank = getHandRank(onTurnOrRiver, p);
			if (onTurnOrRiver) {
				boolean isSame = lastHandRanks.get(p.owner()).equals(handRank);
				if (isSame) continue;
				else lastHandRanks.put(p.owner(), handRank);
			}
			
			if (!first) sb.append(", ");
			sb.append(handRank.toString(true, onTurnOrRiver));
		}
		String handsStr = sb.toString();
		boolean noHands = handsStr.isEmpty();

		if (noHands) handsStr = "Nobody has any hands.";
		if (!onTurnOrRiver) handsStr = "Common cards are: " + cardsToString() + ". " + handsStr;
		
		if (onTurnOrRiver) {
			if (!noHands) {
				bot.sendChannel("Some of you got better hands!");
				bot.sendChannelFormat(handsStr);
			}
		}
		else {
			bot.sendChannelFormat(handsStr);
		}
	}
	
	private HandRank getHandRank(final boolean onTurnOrRiver, Powerup p) {
		if (p instanceof PokerHand) {
			return ((PokerHand)p).getHandRank();
		}
		else {
			return ((BetterHand)p).getHandRank();
		}
	}
	
	private void updateCards() {
		cards = new Hand();
		if (flop != null) {
			for (int i = 1; i <= flop.size(); i++) {
				cards.addCard(flop.getCard(i));
			}
		}
		if (turn != null) {
			cards.addCard(turn);
		}
		if (river != null) {
			cards.addCard(river);
		}
	}
}
