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

import static java.lang.ProcessBuilder.Redirect;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;
import magic.ai.MagicAIImpl;
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

    private static final int NR_OF_GAMES = 10;

    private static final MagicAIImpl AI_TYPE = MagicAIImpl.MCTSC;
    private static final int AI_LEVEL = 1;
    private static final int MAX_TIME = 3600000;

    public static Integer getDuelScore(final String... paths) {
        Process p = null;
        Scanner ans = null;
        int score = 0;
        try {
            p = new ProcessBuilder(
                    "java",
                    "-Xms256M",
                    "-Xmx2G",
                    "-noverify",
                    "-Dmagarena.dir=lib/magarena/release",
                    "-cp",
                    System.getProperty("java.class.path"),
                    "firaga.magic.MagicDuelHandler",
                    paths[0],
                    paths[1])
                .inheritIO()
                .redirectOutput(Redirect.PIPE)
                .start();
            if (p.waitFor() == 0) {
                ans = new Scanner(p.getInputStream());
                score = ans.nextInt();
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (p != null)
                p.destroy();
            if (ans != null)
                ans.close();
        }
        return score;
    }

    private static Integer runDuel(final MagicDeck... decks) {

        if (decks.length != 2) throw new IllegalArgumentException("MagicDuelHandler.getDuelScore only accepts 2 decks");
        if (decks[0] == null || decks[1] == null) throw new NullPointerException();

        decks[0].setDeckType(DeckType.Custom);
        decks[1].setDeckType(DeckType.Custom);

        final DuelConfig config = DuelConfig.getInstance();
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
            final HeadlessGameController controller = new HeadlessGameController(game, MAX_TIME);
            controller.runGame();
        }

        return duel.getGamesWon();
    }

    public static void main(String[] args) {
        ProgressReporter reporter = new ProgressReporter();
        MagicSystem.initialize(reporter);

        if (args.length != 2) {
            System.err.println("MagicDuelHandler must be run with exactly two decks.");
            System.exit(1);
        }

        MagicDeck[] decks = Arrays.stream(args)
            .map(Paths::get)
            .map(DeckUtils::loadDeckFromFile)
            .toArray(MagicDeck[]::new);

        System.out.println(runDuel(decks));

    }

}
