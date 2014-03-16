/*
 * Created on 15.3.2014
 * @author verkel
 */
package noppabot;

import java.util.Calendar;

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
	
	private static final int TURN_HOURS = 20;
	private static final String TURN_SCHEDULE_STR = String.format("0 %d * * *", TURN_HOURS);
	
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
		if (isRollPeriod) revealRiver(verbose);
	}

	public void scheduleTurnReveal() {
		bot.getScheduler().schedule(TURN_SCHEDULE_STR, new Runnable() {
			@Override
			public void run() {
				synchronized (bot.getLock()) {
					revealTurn(true);
				}
			}
		});
	}
	
	public void clear() {
		gameStarted = false;
		flop = null;
		turn = null;
		river = null;
		cards = null;
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
		}
	}

	public void revealRiver(boolean verbose) {
		if (deck == null || river != null) return;
		river = deck.deal();
		updateCards();
		if (verbose) {
			bot.sendChannelFormat("I'll reveal tonight's river card!");
			bot.sendChannelFormat("The poker table now contains: %s", cardsToString());
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
