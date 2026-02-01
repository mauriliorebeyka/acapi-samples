package rebeyka.acapi_sample_dicewar;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import com.rebeyka.acapi.actionables.Actionable;
import com.rebeyka.acapi.actionables.ChangeAttributeActionable;
import com.rebeyka.acapi.actionables.ThrowDiceSetActionable;
import com.rebeyka.acapi.actionables.WinningConditionByAttributeRank;
import com.rebeyka.acapi.actionables.gameflow.EndGameActionable;
import com.rebeyka.acapi.actionables.gameflow.EndTurnActionable;
import com.rebeyka.acapi.builders.GameSetup;
import com.rebeyka.acapi.check.Checker;
import com.rebeyka.acapi.entities.Game;
import com.rebeyka.acapi.entities.Player;
import com.rebeyka.acapi.entities.Types;
import com.rebeyka.acapi.entities.gameflow.Play;
import com.rebeyka.acapi.entities.gameflow.RankingByAttribute;
import com.rebeyka.acapi.entities.gameflow.Trigger;
import com.rebeyka.acapi.exceptions.WrongPlayerCountException;
import com.rebeyka.acapi.random.DieBuilder;

public class DiceWar extends GameSetup {

	@Override
	public String getDescription() {
		return "A simple war of dice";
	}

	@Override
	public void createDefaultAttributes(Player player) {
		player.getAttribute("VP", Types.integer()).setValue(0);
	}

	@Override
	public List<Play> createPlays(Game game, Player player) {
		Supplier<Actionable> throwOneDieActionable = () -> new ThrowDiceSetActionable<Integer>("Throw One Dice",
				DieBuilder.buildBasicDiceSet(1, 6));
		Supplier<Actionable> changeAttribute = () -> new ChangeAttributeActionable<Integer>("Add VP",
				player.getAttribute("VP", Types.integer()),
				v -> v + player.getAttribute("DICE_ROLL", Types.diceSetOf(Types.integer())).getValue().getSum());

		Play.Builder builder = new Play.Builder();
		builder.origin(player).name("THROW_ONE_DICE").condition(Checker.whenPlayable().isCurrentPlayer())
				.actionables(Arrays.asList(throwOneDieActionable, changeAttribute));
		return Arrays.asList(builder.build());
	}

	@Override
	public void createCommonTriggers(Game game) {
		Play endTurne = new Play.Builder().name("End Turn").actionable(() -> new EndTurnActionable()).build();
		game.registerAfterTrigger(new Trigger(endTurne, "Add VP"));
		Play endGame = new Play.Builder().name("End Game").game(game)
				.actionable(() -> new EndGameActionable(game)).build();
		game.registerAfterTrigger(new Trigger(Checker.whenActionable().custom(p -> p.getParent().getGame().getGameFlow().getRound() == 2), endGame, "ALL"));
	}

	@Override
	public void defineWinningCondition(Game game) {
		game.setGameEndActionable(new WinningConditionByAttributeRank(game, "VP"));
		game.setRanking(new RankingByAttribute("VP"));
	}

	public static void main(String[] args) throws WrongPlayerCountException {
		DiceWar diceWar = new DiceWar();
		diceWar.addPlayer("Player 1");
		diceWar.addPlayer("Player 2");
		Game game = diceWar.newGame();
		Player player1 = game.getPlayers().get(0);
		Player player2 = game.getPlayers().get(1);
		game.declarePlay(player1.getPlays().get(0), player1);
		game.executeAll();
		game.declarePlay(player2.getPlays().get(0), player2);
		game.executeAll();
	}

}