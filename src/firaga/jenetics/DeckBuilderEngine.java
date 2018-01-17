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

import java.util.ArrayList;

import io.jenetics.Genotype;
import io.jenetics.IntegerGene;
import io.jenetics.Phenotype;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStart;
import io.jenetics.util.Factory;
import io.jenetics.util.ISeq;

public class DeckBuilderEngine {
	
	// TODO max number
	private final Factory<Genotype<IntegerGene>> GTF;
	private final int cardPoolSize;
	
	// TODO proper card pool type
	public DeckBuilderEngine(final ArrayList<Integer> cardPool) {
		cardPoolSize = cardPool.size();
		GTF = Genotype.of(DeckChromosome.of(cardPoolSize));
	}
	
	private static Integer fitness(final Genotype<IntegerGene> gt) {
		// TODO stub
		// Fitness value will already be saved in a file,
		// so just read it.
		return 0;
	}
	
	private EvolutionStart<IntegerGene, Integer>
	start(final int populationSize, final long generation) {
		final ISeq<Phenotype<IntegerGene, Integer>> population =
				GTF.instances()
					.map(gt -> Phenotype.of(gt, generation, DeckBuilderEngine::fitness))
					.limit(populationSize)
					.collect(ISeq.toISeq());
		return EvolutionStart.of(population, generation);
	}
	
	private EvolutionResult<IntegerGene, Integer>
	evolve(final EvolutionStart<IntegerGene, Integer> start) {
		// TODO stub
		// Play games and save results to file in this step
		return null;
	}
	
}
