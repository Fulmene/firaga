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

package firaga.jenetics;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import firaga.magic.MagicDeckCreator;
import firaga.magic.MagicDuelHandler;
import firaga.magic.land.BasicLandGenerator;
import firaga.magic.land.LandGenerator;
import io.jenetics.Alterer;
import io.jenetics.GaussianMutator;
import io.jenetics.Genotype;
import io.jenetics.IntegerGene;
import io.jenetics.Selector;
import io.jenetics.TournamentSelector;
import io.jenetics.UniformCrossover;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.Factory;
import magic.model.MagicCardDefinition;
import magic.model.MagicDeck;
import magic.utility.DeckUtils;

public final class DeckBuilderEngine {
	
	// Magic related parameters
	private final List<MagicCardDefinition> spellPool;
	private final int spellPoolSize;
	private final LandGenerator landGenerator;
	private final MagicDeck[] opponentDecks;
	
	// Genetic algorithm parameters
	private final Factory<Genotype<IntegerGene>> GTF;
	private final int populationSize;
	private final double survivorFraction;
	private final Selector<IntegerGene, Integer> survivorSelector;
	private final Selector<IntegerGene, Integer> offspringSelector;
	private final Alterer<IntegerGene, Integer> alterer;
	
	public DeckBuilderEngine(final List<MagicCardDefinition> spellPool) {
		this.spellPool = spellPool;
		this.spellPoolSize = spellPool.size();
		this.landGenerator = BasicLandGenerator.getInstance();
		this.GTF = Genotype.of(DeckChromosome.of(spellPoolSize));

		this.opponentDecks = Stream.of("Benchmark_RamunapRed", "Benchmark_TemurEnergy")
				.map(name -> DeckUtils.loadDeckFromFile(DeckUtils.findDeckFile(name).toPath()))
				.toArray(MagicDeck[]::new);

		this.populationSize = 100;
		this.survivorFraction = 0.25;
		
		this.survivorSelector = new TournamentSelector<>(3);
		this.offspringSelector = new TournamentSelector<>(3);
		this.alterer = Alterer.of(
				new UniformCrossover<>(0.5, 0.5),
				new GaussianMutator<IntegerGene, Integer>(0.2));
	}
	
	public final Genotype<IntegerGene>
	run() {
		Engine<IntegerGene, Integer> engine =
				Engine.builder(this::fitness, this.GTF)
					.populationSize(populationSize)
					.maximizing()
					.survivorsFraction(survivorFraction)
					.survivorsSelector(survivorSelector)
					.offspringSelector(offspringSelector)
					.alterers(alterer)
					.executor(Executors.newFixedThreadPool(2))
					.build();
		return engine.stream().limit(3).collect(EvolutionResult.toBestGenotype());
	}
	
	private final Integer fitness(final Genotype<IntegerGene> gt) {
		MagicDeck deck = MagicDeckCreator.getMagicDeck(this.spellPool, gt, this.landGenerator);
		return Arrays.stream(opponentDecks).map(opp -> MagicDuelHandler.getDuelScore(deck, opp)).reduce(Integer::sum).orElse(0);
	}
	
}
