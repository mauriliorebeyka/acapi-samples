package rebeyka.acapi.samples;

import com.rebeyka.acapi.actionables.Actionable;
import com.rebeyka.acapi.actionables.MoveCardActionable;
import com.rebeyka.acapi.actionables.gameflow.EndTurnActionable;
import com.rebeyka.acapi.builders.GameSetup;
import com.rebeyka.acapi.check.Checker;
import com.rebeyka.acapi.entities.Card;
import com.rebeyka.acapi.entities.Deck;
import com.rebeyka.acapi.entities.Game;
import com.rebeyka.acapi.entities.Player;
import com.rebeyka.acapi.entities.Types;
import com.rebeyka.acapi.entities.gameflow.Play;
import com.rebeyka.acapi.entities.gameflow.Trigger;
import com.rebeyka.acapi.view.VisibilityType;

public class LoveLetter extends GameSetup {

	public LoveLetter() {
		super(2, 6);
	}

	@Override
	public String getDescription() {
		return "Love Letter";
	}

	@Override
	public void playerSetup(Player player) {
		Game game = player.getGame();
		player.getDeck("hand");
		player.getAttribute("favour", Types.INT).setValue(0);
		player.getAttribute("spy", Types.BOOLEAN).setValue(false);
		player.getAttribute("handmain", Types.BOOLEAN).setValue(false);

		Actionable drawCard = new MoveCardActionable("draw", player.getDeck("hand"), game.NOBODY.getDeck("main"));
		Play.Builder builder = new Play.Builder().actionable(drawCard).name("draw").origin(player).game(game)
				.condition(Checker.whenPlayable().happened("EndTurn").last().isActivePlayer());
		player.getPlays().add(builder.build());
		player.getPlays().add(new Play.Builder().actionable(new EndTurnActionable(false)).name("end turn")
				.origin(player).condition(Checker.whenPlayable().isActivePlayer()).build());

		game.registerAfterTrigger(new Trigger(player.getPlay("draw"), "EndTurn"));
	}

	@Override
	public void gameSetup(Game game) {
		Deck mainDeck = game.NOBODY.getDeck("Main");
		mainDeck.setVisibilityType(VisibilityType.PRIVATE);

		Card guard1 = game.createCard("guard1", game.NOBODY);
		mainDeck.add(guard1);
		Card guard2 = game.createCard("guard2", game.NOBODY);
		mainDeck.add(guard2);

	}

	public static void main(String[] args) {
		GameSetup gameSetup = new LoveLetter();
		gameSetup.addPlayer("player 1");
		gameSetup.addPlayer("Player 2");
		Game game = gameSetup.newGame();
		game.getGameFlow().getCurrentPlayer();
		game.declarePlay(game.getGameFlow().getCurrentPlayer().getPlay("draw"));
		game.declarePlay(game.getGameFlow().getCurrentPlayer().getPlay("end turn"));
		game.executeAll();
		game.getPlayers().stream().forEach(p -> System.out.println(p.getDeck("hand").getAll()));
	}

}
