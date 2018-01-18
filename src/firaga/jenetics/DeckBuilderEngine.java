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

import firaga.magic.MagicDeckCreator;
import firaga.magic.MagicDuelHandler;
import firaga.magic.land.BasicLandGenerator;
import firaga.magic.land.LandGenerator;
import io.jenetics.Alterer;
import io.jenetics.AltererResult;
import io.jenetics.GaussianMutator;
import io.jenetics.Genotype;
import io.jenetics.IntegerGene;
import io.jenetics.Optimize;
import io.jenetics.Phenotype;
import io.jenetics.Selector;
import io.jenetics.TournamentSelector;
import io.jenetics.UniformCrossover;
import io.jenetics.engine.EvolutionDurations;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStart;
import io.jenetics.engine.EvolutionStream;
import io.jenetics.engine.Limits;
import io.jenetics.util.Factory;
import io.jenetics.util.ISeq;
import magic.model.MagicCardDefinition;
import magic.model.MagicDeck;

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
	private final int survivorCount;
	private final int offspringCount;
	private final Selector<IntegerGene, Integer> survivorSelector;
	private final Selector<IntegerGene, Integer> offspringSelector;
	private final Alterer<IntegerGene, Integer> alterer;
	
	public DeckBuilderEngine(final List<MagicCardDefinition> cardPool) {
		this.spellPool = cardPool;
		this.spellPoolSize = cardPool.size();
		this.landGenerator = BasicLandGenerator.getInstance();
		this.opponentDecks = new MagicDeck[0];

		this.GTF = Genotype.of(DeckChromosome.of(spellPoolSize));
		this.populationSize = 100;
		this.survivorFraction = 0.25;
		this.survivorCount = (int)(populationSize * survivorFraction);
		this.offspringCount = populationSize - survivorCount;
		
		this.survivorSelector = new TournamentSelector<>(3);
		this.offspringSelector = new TournamentSelector<>(3);
		this.alterer = Alterer.of(
				new UniformCrossover<>(0.5, 0.5),
				new GaussianMutator<IntegerGene, Integer>(0.2));
	}
	
	public final EvolutionResult<IntegerGene, Integer>
	run() {
		return EvolutionStream.of(
				() -> this.start(populationSize, 0),
				this::evolve)
				.limit(Limits.bySteadyFitness(10))
				.collect(EvolutionResult.toBestEvolutionResult());
	}
	
	private final Integer fitness(final Genotype<IntegerGene> gt) {
		MagicDeck deck = MagicDeckCreator.getMagicDeck(this.spellPool, gt, this.landGenerator);
		return Arrays.stream(opponentDecks).map(opp -> MagicDuelHandler.getDuelScore(deck, opp)).reduce(Integer::sum).orElse(0);
	}
	
	private final EvolutionStart<IntegerGene, Integer>
	start(final int populationSize, final long generation) {
		final ISeq<Phenotype<IntegerGene, Integer>> population =
				GTF.instances()
					.map(gt -> Phenotype.of(gt, generation, this::fitness))
					.limit(populationSize)
					.collect(ISeq.toISeq());
		return EvolutionStart.of(population, generation);
	}
	
	private final EvolutionResult<IntegerGene, Integer>
	evolve(final EvolutionStart<IntegerGene, Integer> start) {
		final ISeq<Phenotype<IntegerGene, Integer>> population = start.getPopulation();
		final long generation = start.getGeneration();

		final ISeq<Phenotype<IntegerGene, Integer>> offsprings = offspringSelector.select(population, offspringCount, Optimize.MAXIMUM);
		final ISeq<Phenotype<IntegerGene, Integer>> survivors = survivorSelector.select(population, survivorCount, Optimize.MAXIMUM);

		final AltererResult<IntegerGene, Integer> alterResult = alterer.alter(offsprings, generation);
		final ISeq<Phenotype<IntegerGene, Integer>> alteredOffsprings = alterResult.getPopulation();
		final int alterCount = alterResult.getAlterations();

		final ISeq<Phenotype<IntegerGene, Integer>> nextPopulation = ISeq.empty();
		nextPopulation.append(survivors);
		nextPopulation.append(alteredOffsprings);
		
		final ISeq<Phenotype<IntegerGene, Integer>> filteredNextPopulation = nextPopulation.stream()
				.filter(i -> !i.isValid())
				.collect(ISeq.toISeq());
		final int invalidCount = nextPopulation.size() - filteredNextPopulation.size();
	
		final ISeq<Phenotype<IntegerGene, Integer>> replacementPopulation = GTF.instances()
				.map(gt -> Phenotype.of(gt, generation, this::fitness))
				.limit(populationSize - nextPopulation.size())
				.collect(ISeq.toISeq());
		
		filteredNextPopulation.append(replacementPopulation);
				
		return EvolutionResult.of(
				Optimize.MAXIMUM,
				filteredNextPopulation,
				generation,
				EvolutionDurations.ZERO, // TODO stub
				0, // kill count
				invalidCount,
				alterCount);
	}
	
}
