/*
 *  Copyright (C) 2018 Ada Joule
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package firaga.magic;

import java.nio.file.Paths;

import firaga.magic.land.BasicLandGenerator;
import magic.ai.MagicAIImpl;
import magic.data.CardDefinitions;
import magic.data.DeckType;
import magic.data.DuelConfig;
import magic.headless.HeadlessGameController;
import magic.model.DuelPlayerConfig;
import magic.model.MagicDeck;
import magic.model.MagicDuel;
import magic.model.MagicGame;
import magic.model.player.AiProfile;
import magic.utility.DeckUtils;
import magic.utility.MagicSystem;
import magic.utility.ProgressReporter;

public final class MagicDuelHandler {
	
	private static final int NR_OF_GAMES = 1;
	
	private static final MagicAIImpl AI_TYPE = MagicAIImpl.MCTSC;
	private static final int AI_LEVEL = 4;

	public static void runDuel(final MagicDeck... decks) {
		
		if (decks.length != 2) throw new IllegalArgumentException("MagicDuelHandler.runDuel only accepts 2 decks");
		if (decks[0] == null || decks[1] == null) throw new NullPointerException();

		final DuelConfig config = new DuelConfig();
		config.setNrOfGames(NR_OF_GAMES);
		
		final MagicDuel duel = new MagicDuel(config);
		duel.initialize();
		
		final DuelPlayerConfig[] players = new DuelPlayerConfig[2];
		for (int i = 0; i < players.length; i++) {
			players[i] = new DuelPlayerConfig(AiProfile.create(decks[i].toString(), AI_TYPE, AI_LEVEL), null);
			players[i].setDeck(decks[i]);
		}
		duel.setPlayers(players);
		
		while (duel.getGamesPlayed() < duel.getGamesTotal()) {
			System.out.println("Start game " + (duel.getGamesPlayed() + 1));
			final MagicGame game = duel.nextGame();
			game.setArtificial(true);
			final HeadlessGameController controller = new HeadlessGameController(game, 600000);
			controller.runGame();
			System.out.println("End game " + duel.getGamesPlayed());
		}

		System.out.println(
						decks[0] + "\t" +
						AI_TYPE + "\t" +
						AI_LEVEL + "\t" +
						decks[1] + "\t" +
						AI_TYPE + "\t" +
						AI_LEVEL + "\t" +
						duel.getGamesTotal() + "\t" +
						duel.getGamesWon() + "\t" +
						(duel.getGamesPlayed() - duel.getGamesWon())
				);
	}
	
	// Test
	public static final void main(final String[] args) {
		ProgressReporter reporter = new ProgressReporter();
		MagicSystem.initialize(reporter);
		final MagicDeck deck1 = new MagicDeck();
		final MagicDeck deck2 = new MagicDeck();
		deck1.add(CardDefinitions.getCard("Hazoret the Fervent"));
		deck2.add(CardDefinitions.getCard("Hazoret the Fervent"));
		deck1.setDeckType(DeckType.Custom);
		deck2.setDeckType(DeckType.Custom);
		BasicLandGenerator.getInstance().addLands(deck1);
		BasicLandGenerator.getInstance().addLands(deck2);
		//final MagicDeck deck1 = DeckUtils.loadDeckFromFile(Paths.get(DeckUtils.findDeckFile("FX_Rat.dec").toURI()));
		//final MagicDeck deck2 = DeckUtils.loadDeckFromFile(Paths.get(DeckUtils.findDeckFile("FX_Reanimator.dec").toURI()));
		runDuel(deck1, deck2);
	}

}
