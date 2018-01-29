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

        final ExecutorService executor = Executors.newFixedThreadPool(16);

        try {
            final DeckBuilderEngine engine = new DeckBuilderEngine(cmdLineArgs.getFormat(), DeckBuilderEngine.DEFAULT_ENGINE_BUILDER, cmdLineArgs.getColors());
            final EvolutionStatistics<Integer, DoubleMomentStatistics> statistics = EvolutionStatistics.ofNumber(); 
            final Phenotype<IntegerGene, Integer> bestPhenotype = engine.stream().peek(statistics).collect(EvolutionResult.toBestPhenotype());
            System.out.println("Best deck: " + MagicDeckCreator.getMagicDeck(engine.getSpellPool(), bestPhenotype.getGenotype(), engine.getLandGenerator()));
            System.out.println("Score: " + bestPhenotype.getFitness());
            System.out.println(statistics);
        } finally {
            executor.shutdown();
        }
	}

}
