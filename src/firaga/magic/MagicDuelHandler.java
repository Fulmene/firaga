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

import magic.ai.MagicAIImpl;
import magic.data.DuelConfig;
import magic.headless.HeadlessGameController;
import magic.model.DuelPlayerConfig;
import magic.model.MagicDeck;
import magic.model.MagicDuel;
import magic.model.MagicGame;
import magic.model.player.AiProfile;

public final class MagicDuelHandler {

    private static final int NR_OF_GAMES = 10;

    private static final MagicAIImpl AI_TYPE = MagicAIImpl.MCTSC;
    private static final int AI_LEVEL = 1;

    public static Integer getDuelScore(final MagicDeck... decks) {

        if (decks.length != 2) throw new IllegalArgumentException("MagicDuelHandler.getDuelScore only accepts 2 decks");
        if (decks[0] == null || decks[1] == null) throw new NullPointerException();

        final DuelConfig config = new DuelConfig();
        config.setNrOfGames(NR_OF_GAMES);

        final MagicDuel duel = new MagicDuel(config);
        duel.initialize();

        final DuelPlayerConfig[] players = new DuelPlayerConfig[2];
        for (int i = 0; i < players.length; i++) {
            players[i] = new DuelPlayerConfig(AiProfile.create("Player" + i, AI_TYPE, AI_LEVEL), null);
            players[i].setDeck(decks[i]);
        }
        duel.setPlayers(players);

        while (duel.getGamesPlayed() < duel.getGamesTotal()) {
            final MagicGame game = duel.nextGame();
            game.setArtificial(true);
            final HeadlessGameController controller = new HeadlessGameController(game, 600000);
            controller.runGame();
        }

        return duel.getGamesWon(); // TODO add more statistics
    }

}
