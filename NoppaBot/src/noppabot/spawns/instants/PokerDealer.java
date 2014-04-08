/*
 * Created on 12.12.2013
 * @author verkel
 */
package noppabot.spawns.instants;

import java.util.*;

import noppabot.*;
import noppabot.spawns.*;
import noppabot.spawns.dice.*;
import noppabot.spawns.dice.PokerHand.BetterHand;
import ca.ualberta.cs.poker.Deck;

public class PokerDealer extends Instant {

	private static final int MAX_PLAYERS = 3;

	public static final String NAME = "Poker Dealer";
	
	private Deck deck;
	private int cardsLeft;
	private Set<String> peopleDealtTo = new HashSet<String>();
	
	@Override
	public void onInitialize() {
		deck = new Deck(System.currentTimeMillis());
		deck.shuffle();
		cardsLeft = Powerups.powerupRnd.nextInt(MAX_PLAYERS) + 1;
	}

	@Override
	public boolean canPickUp(String nick, boolean verbose) {
		INoppaBot bot = bot();
		
		// Disallow picking up if player has any other item than cards
		if (bot.getPowerups().containsKey(nick)) {
			Powerup powerup = bot.getPowerups().get(nick);
			if (!(powerup instanceof PokerHand)) {
				if (verbose) bot.sendChannelFormat("%s: you already have the %s; you don't have any room for cards.", 
					Color.nick(nick), powerup.nameColored());
				return false;
			}
		}
		
		// Disallow picking up if the dealer has dealt to this player
		if (peopleDealtTo.contains(nick)) {
			if (verbose) bot.sendChannelFormat("%s: I've already dealt you a hand!", Color.nick(nick));
			return false;
		}
		
		return true;
	}
	
	@Override
	public void onSpawn() {
		PokerTable table = bot.getPokerTable();
		table.onDealerSpawned();
		bot.sendChannelFormat("A %s appears!", nameColored());
		bot.sendChannelFormat("\"There's %s left in the poker table\", says the dealer.", seatsLeftStr());
		bot.sendChannelFormat("\"Today the common cards are: %s. Anyone want to play?\"", table.cardsToString());
	}

	private String seatsLeftStr() {
		String seatsLeftStr;
		if (cardsLeft == 1) seatsLeftStr = "one seat";
		else seatsLeftStr = cardsLeft + " seats";
		return seatsLeftStr;
	}
	
	@Override
	public boolean isDestroyedAfterPickup() {
		return cardsLeft == 0;
	}

	@Override
	public void onExpire() {
		bot.sendChannelFormat("... the poker dealer walks to another table.");
	}

	@Override
	public void onPickup() {
		peopleDealtTo.add(owner);
		PokerTable table = bot.getPokerTable();
		table.onDealerSpawned();
		trashPreviousCards();
		PokerHand cards = (PokerHand)new PokerHand().initialize(bot);
		bot.getPowerups().put(owner, cards);
		cards.setOwner(owner);
//		bot.sendChannelFormat("%s is dealt a hand of %s!", ownerColored, cards.nameColored());
		bot.sendChannelFormat("The %s deals hand of %s to %s! %s", 
			nameColored(), cards.cardsToString(), ownerColored, cards.getHandRank().toString(false, false));
		cardsLeft--;
		
		if (cardsLeft == 0) {
			bot.sendChannel("The dealer leaves after filling up all the seats in the table.");
		}
		else {
			bot.sendChannelFormat("\"There's still %s left in the poker table\", the dealer says.", 
				seatsLeftStr());
		}
	}

	private void trashPreviousCards() {
		Powerup previousItem = bot.getPowerups().get(owner);
		if (previousItem != null) {
			if (previousItem instanceof PokerHand) ((PokerHand)previousItem).trash();
			else if (previousItem instanceof BetterHand) ((BetterHand)previousItem).trash();
		}
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

