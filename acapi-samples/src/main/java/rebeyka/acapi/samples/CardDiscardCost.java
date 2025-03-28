package rebeyka.acapi.samples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.rebeyka.acapi.actionables.MoveCardActionable;
import com.rebeyka.acapi.actionables.SimpleCostActionable;
import com.rebeyka.acapi.actionables.ThrowDiceSetActionable;
import com.rebeyka.acapi.actionables.WinningConditionByAttributeRank;
import com.rebeyka.acapi.builders.GameSetup;
import com.rebeyka.acapi.builders.PlayBuilder;
import com.rebeyka.acapi.entities.Card;
import com.rebeyka.acapi.entities.Cost;
import com.rebeyka.acapi.entities.Deck;
import com.rebeyka.acapi.entities.Game;
import com.rebeyka.acapi.entities.Play;
import com.rebeyka.acapi.entities.Player;
import com.rebeyka.acapi.entities.SimpleIntegerAttribute;
import com.rebeyka.acapi.random.DieBuilder;
import com.rebeyka.entitities.cost.PlayableSequenceCost;

public class CardDiscardCost extends GameSetup {

	@Override
	public String getDescription() {
		return "simple test for a play that discard cards from the hand while played";
	}

	@Override
	public void defineWinningCondition(Game game) {
		game.setGameEndActionable(new WinningConditionByAttributeRank(game, "VP"));
	}

	@Override
	public void createDefaultAttributes(Player player) {
		
		Deck hand = new Deck("HAND");
		player.getDecks().put("HAND", hand);
		player.getDecks().put("DISCARD", new Deck("DISCARD"));
		
		
	}

	@Override
	public List<Play> createPlays(Game game, Player player) {
		Cost discardCost = new PlayableSequenceCost("value");
		MoveCardActionable move = new MoveCardActionable("MOVE", player, "HAND", "DISCARD");
		SimpleCostActionable discardCostActionable = new SimpleCostActionable("DISCARD_COST", player, discardCost, move);
		discardCost.setCostActionable(discardCostActionable);
		for (int i=0; i<10; i++) {
			Card card = new Card(Integer.toString(i));
			card.setAttribute("value", new SimpleIntegerAttribute(i));
			ThrowDiceSetActionable<Integer> throwOneDieActionable = new ThrowDiceSetActionable<Integer>("Throw One Dice",
					DieBuilder.buildBasicDiceSet(1, 6), player);
			MoveCardActionable move2 = new MoveCardActionable("MOVE", card, "HAND", "DISCARD");
			PlayBuilder builder = new PlayBuilder().withId("PLAY "+i).withCost(discardCost).withOrigin(card).withActionables(List.of(throwOneDieActionable,move2));
			Play play = new Play(builder);
			card.setPlays(List.of(play));
			player.getDeck("HAND").add(card);
			card.setGame(game);
		}
		return Collections.EMPTY_LIST;
	}

	@Override
	public void createCommonTriggers(Game game) {
		// TODO Auto-generated method stub
		
	}

	public static void main(String[] args) {
		GameSetup gameSetup = new CardDiscardCost();
		gameSetup.addPlayer("Player");
		Game game = gameSetup.newGame();
		System.out.println(game.findDeck("HAND").getCards());
		System.out.println(game.findDeck("DISCARD").getCards());
		Player player = game.findPlayer("Player");
		Play play = game.findPlay(player, "PLAY 1");
		game.declarePlay(player, play);
		List<Card> hand = player.getDeck("HAND").getCards();
		game.setSelectedChoices(List.of(hand.get(8),hand.get(9)));
		game.executeAll();
		System.out.println(game.findDeck("HAND").getCards());
		System.out.println(game.findDeck("DISCARD").getCards());
	}
	
}
