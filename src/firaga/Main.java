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

package firaga;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import firaga.jenetics.DeckBuilderEngine;
import firaga.magic.MagicDeckCreator;
import io.jenetics.IntegerGene;
import io.jenetics.Phenotype;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.stat.DoubleMomentStatistics;
import magic.utility.MagicSystem;
import magic.utility.ProgressReporter;

public final class Main {

    public static final void main(final String[] args) {
        // Initialise Magarena
        ProgressReporter reporter = new ProgressReporter();
        MagicSystem.initialize(reporter);

        final CmdLineArgs cmdLineArgs = new CmdLineArgs(args);

        final int maxThreads = Runtime.getRuntime().availableProcessors();
        final ExecutorService executor = Executors.newFixedThreadPool(maxThreads);

        try {
            final Engine.Builder<IntegerGene, Integer> engineBuilder = DeckBuilderEngine.DEFAULT_ENGINE_BUILDER.copy().executor(executor);
            final DeckBuilderEngine engine = new DeckBuilderEngine(cmdLineArgs.getFormat(), engineBuilder, cmdLineArgs.getColors());
            final EvolutionStatistics<Integer, DoubleMomentStatistics> statistics = EvolutionStatistics.ofNumber(); 
            EvolutionResult<IntegerGene, Integer> result = engine.stream(0).peek(statistics).collect(EvolutionResult.toBestEvolutionResult());
            System.out.println("End level 0");
            System.out.println(statistics);

            for (int level = 1; level < 4; level++) {
                final EvolutionResult<IntegerGene, Integer> nextResult = engine.stream(result, level).peek(statistics).collect(EvolutionResult.toBestEvolutionResult());
                System.out.println("End level " + level);
                System.out.println(statistics);
                result = nextResult;
            }

        } finally {
            executor.shutdown();
        }
    }

}
