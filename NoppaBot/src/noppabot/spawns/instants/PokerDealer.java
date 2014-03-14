/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.instants;

import noppabot.*;
import noppabot.spawns.*;
import noppabot.spawns.dice.PokerCards;
import ca.ualberta.cs.poker.Deck;

// TODO "deal" command!
public class PokerDealer extends Instant {

	private static final int MAX_PLAYERS = 3;

	public static final String NAME = "Poker Dealer";
	
	private Deck deck;
	private int cardsLeft;
	
	@Override
	public void onInitialize() {
		deck = new Deck(System.currentTimeMillis());
		deck.shuffle();
		cardsLeft = Powerups.powerupRnd.nextInt(MAX_PLAYERS) + 1;
	}

	@Override
	public boolean canPickUp(String nick, boolean verbose) {
		INoppaBot bot = bot();
		if (bot.getPowerups().containsKey(nick)) {
			Powerup powerup = bot.getPowerups().get(nick);
			if (verbose) bot.sendChannelFormat("%s: you already have the %s; you don't have any room for cards.", 
				Color.nick(nick), powerup.nameColored());
			return false;
		}
		
		return true;
	}
	
	@Override
	public void onSpawn() {
		bot.sendChannelFormat("A %s appears!", nameColored());
		String cardsLeftStr;
		if (cardsLeft == 1) cardsLeftStr = "one seat";
		else cardsLeftStr = cardsLeft + " seats";
		bot.sendChannelFormat("\"There's %s left in the poker table\", says the dealer.", cardsLeftStr);
	}

	@Override
	public void onExpire() {
		bot.sendChannelFormat("... the poker dealer walks to another table.");
	}

	@Override
	public void onPickup() {
		PokerCards cards = (PokerCards)new PokerCards(deck).initialize(bot);
		bot.getPowerups().put(owner, cards);
		cards.setOwner(owner);
		bot.sendChannelFormat("%s is dealt a hand of %s!", ownerColored, cards.nameColored());
//		bot.sendChannelFormat("The Poker Dealer deals hand of %s to %s!", cards.cardsToString(), ownerColored);
		cardsLeft--;
	}

	@Override
	public String name() {
		return NAME;
	}
	
	@Override
	public float spawnChance() {
		return 1f;
	}
}

