package rebeyka.acapi_sample_dicewar;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import com.rebeyka.acapi.actionables.ChangeAttributeActionable;
import com.rebeyka.acapi.actionables.ThrowDiceSetActionable;
import com.rebeyka.acapi.actionables.WinningConditionByAttributeRank;
import com.rebeyka.acapi.actionables.gameflow.EndGameActionable;
import com.rebeyka.acapi.actionables.gameflow.EndTurnActionable;
import com.rebeyka.acapi.builders.GameSetup;
import com.rebeyka.acapi.builders.PlayBuilder;
import com.rebeyka.acapi.entities.Attribute;
import com.rebeyka.acapi.entities.Game;
import com.rebeyka.acapi.entities.Play;
import com.rebeyka.acapi.entities.Player;
import com.rebeyka.acapi.entities.Trigger;
import com.rebeyka.acapi.exceptions.WrongPlayerCountException;
import com.rebeyka.acapi.random.DiceSet;
import com.rebeyka.acapi.random.DieBuilder;

public class DiceWar extends GameSetup {
	
	@Override
	public String getDescription() {
		return "A simple war of dice";
	}
	
	@Override
	public void createDefaultAttributes(Player player) {
		player.getAttributes().put("VP", new Attribute<Integer>(0));
	}

	@Override
	public List<Play> createPlays(Game game, Player player) {
		ThrowDiceSetActionable<Integer> throwOneDieActionable = new ThrowDiceSetActionable<Integer>("Throw One Dice",
				DieBuilder.buildBasicDiceSet(1, 6), player);
		ChangeAttributeActionable<DiceSet<Integer>, Integer> changeAttribute = new ChangeAttributeActionable<DiceSet<Integer>, Integer>(
				"Add VP", player, "DICE_ROLL", "VP", (s, t) -> s.getValue().getSum() + t.getValue());
		Predicate<Game> playerTurn = g -> g.getGameFlow().getCurrentPlayer().equals(player);
		PlayBuilder builder = new PlayBuilder();
		builder.withOrigin(player);
		Play throwOneDiePlay = new Play(builder.withId("THROW_ONE_DICE").withCondition(playerTurn)
				.withActionables(Arrays.asList(throwOneDieActionable, changeAttribute)));
		game.registerAfterTrigger(new Trigger(g -> g.getGameFlow().getCurrentPlayer().equals(player),
				new EndTurnActionable(player), "Add VP"));
		return Arrays.asList(throwOneDiePlay);
	}

	@Override
	public void createCommonTriggers(Game game) {
		game.registerAfterTrigger(
				new Trigger(p -> p.getGameFlow().getRound() == 2, new EndGameActionable(game), "ALL"));
	}

	@Override
	public void defineWinningCondition(Game game) {
		game.setGameEndActionable(new WinningConditionByAttributeRank(game, "VP"));
	}
	
	public static void main(String[] args) throws WrongPlayerCountException {
		DiceWar diceWar = new DiceWar();
		diceWar.addPlayer("Player 1");
		diceWar.addPlayer("Player 2");
		Game game = diceWar.newGame();
		Player player1 = game.getPlayers().get(0);
		Player player2 = game.getPlayers().get(1);
		game.declarePlay(player1, player1.getPlays().get(0));
		game.executeAll();
		game.declarePlay(player2, player2.getPlays().get(0));
		game.executeAll();
	}
}
