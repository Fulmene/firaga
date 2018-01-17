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

import java.util.function.Predicate;

import io.jenetics.Alterer;
import io.jenetics.AltererResult;
import io.jenetics.Genotype;
import io.jenetics.IntegerGene;
import io.jenetics.Optimize;
import io.jenetics.Phenotype;
import io.jenetics.Selector;
import io.jenetics.engine.EvolutionDurations;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStart;
import io.jenetics.engine.EvolutionStream;
import io.jenetics.util.Factory;
import io.jenetics.util.ISeq;
import magic.model.MagicCardDefinition;

public final class DeckBuilderEngine {
	
	private final ISeq<MagicCardDefinition> cardPool;
	private final int cardPoolSize;
	private final Factory<Genotype<IntegerGene>> GTF;
	
	// Genetic algorithm parameters
	private static final int populationSize = 0;
	private static final int survivorCount = 0;
	private static final int offspringCount = 0;
	private static final int maxAge = 0;
	private static final Selector<IntegerGene, Integer> survivorSelector =
			null;
	private static final Selector<IntegerGene, Integer> offspringSelector =
			null;
	private static final Alterer<IntegerGene, Integer> alterer =
			null;
	private static final Predicate<EvolutionResult<IntegerGene, Integer>> limitPredicate =
			null;
	
	public DeckBuilderEngine(final ISeq<MagicCardDefinition> cardPool) {
		this.cardPool = cardPool;
		this.cardPoolSize = cardPool.size();
		this.GTF = Genotype.of(DeckChromosome.of(cardPoolSize));
	}
	
	public final EvolutionResult<IntegerGene, Integer>
	run() {
		return EvolutionStream.of(
				() -> this.start(populationSize, 0),
				this::evolve)
				.limit(limitPredicate)
				.collect(EvolutionResult.toBestEvolutionResult());
	}
	
	private static final Integer fitness(final Genotype<IntegerGene> gt) {
		// TODO stub
		// Fitness value will already be saved in a file,
		// so just read it.
		return 0;
	}
	
	private final EvolutionStart<IntegerGene, Integer>
	start(final int populationSize, final long generation) {
		final ISeq<Phenotype<IntegerGene, Integer>> population =
				GTF.instances()
					.map(gt -> Phenotype.of(gt, generation, DeckBuilderEngine::fitness))
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

		// TODO kill old individuals
		final ISeq<Phenotype<IntegerGene, Integer>> nextPopulation = ISeq.empty();
		nextPopulation.append(survivors);
		nextPopulation.append(alteredOffsprings);
		
		final ISeq<Phenotype<IntegerGene, Integer>> invalidFilteredNextPopulation = nextPopulation.stream()
				.filter(i -> !i.isValid())
				.collect(ISeq.toISeq());
		final int invalidCount = nextPopulation.size() - invalidFilteredNextPopulation.size();
		final ISeq<Phenotype<IntegerGene, Integer>> filteredNextPopulation = invalidFilteredNextPopulation.stream()
				.filter(i -> i.getAge(generation) <= maxAge)
				.collect(ISeq.toISeq());
		final int killCount = invalidFilteredNextPopulation.size() - filteredNextPopulation.size();
		
		final ISeq<Phenotype<IntegerGene, Integer>> replacementPopulation = GTF.instances()
				.map(gt -> Phenotype.of(gt, generation, DeckBuilderEngine::fitness))
				.limit(populationSize - nextPopulation.size())
				.collect(ISeq.toISeq());
		
		filteredNextPopulation.append(replacementPopulation);
				
		// TODO create deck
		// TODO Play games and save results to file
		return EvolutionResult.of(
				Optimize.MAXIMUM,
				filteredNextPopulation,
				generation,
				EvolutionDurations.ZERO,
				killCount,
				invalidCount,
				alterCount);
	}
	
}
