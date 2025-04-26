package rebeyka.acapi.samples;

import java.util.List;
import java.util.function.Supplier;

import com.rebeyka.acapi.actionables.Actionable;
import com.rebeyka.acapi.actionables.CostActionable;
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
import com.rebeyka.acapi.entities.Player;
import com.rebeyka.acapi.entities.SimpleIntegerAttribute;
import com.rebeyka.acapi.entities.cost.PlayableSequenceCost;
import com.rebeyka.acapi.random.DieBuilder;

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
	public List<PlayBuilder> createPlays(Game game, Player player) {
		for (int i = 0; i < 10; i++) {
			Card card = new Card(Integer.toString(i));
			card.setAttribute("value", new SimpleIntegerAttribute(i));
			player.getDeck("HAND").add(card);
			card.setGame(game);
		}
		Cost discardCost = new PlayableSequenceCost("value");
		Supplier<Actionable> move = () -> new MoveCardActionable("MOVE", "HAND", "DISCARD");
		Supplier<CostActionable> discardCostActionable = () -> new SimpleCostActionable("DISCARD_COST", discardCost,
				move);

		discardCost.setCostActionable(discardCostActionable);
		Supplier<Actionable> throwOneDieActionable = () -> new ThrowDiceSetActionable<Integer>("Throw One Dice",
				DieBuilder.buildBasicDiceSet(1, 6));
		PlayBuilder builder = new PlayBuilder().withId("PLAY").withCost(discardCost).withOrigin(player)
				.withActionables(List.of(throwOneDieActionable, move));
		return List.of(builder);
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
		PlayBuilder play = game.findPlay(player, "PLAY");
		List<Card> hand = player.getDeck("HAND").getCards();
		game.declarePlay(play, List.of(hand.get(6)));
		game.setSelectedChoices(List.of(hand.get(8), hand.get(9)));
		game.executeAll();
		game.declarePlay(play, List.of(hand.get(1)));
		game.setSelectedChoices(List.of(hand.get(3), hand.get(4)));
		game.executeAll();
		System.out.println(game.findDeck("HAND").getCards());
		System.out.println(game.findDeck("DISCARD").getCards());
	}

}
