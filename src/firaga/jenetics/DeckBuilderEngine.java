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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import firaga.magic.MagicDeckCreator;
import firaga.magic.MagicDuelHandler;
import firaga.magic.land.LandGenerator;
import firaga.magic.land.LandPool;
import io.jenetics.GaussianMutator;
import io.jenetics.Genotype;
import io.jenetics.IntegerGene;
import io.jenetics.TournamentSelector;
import io.jenetics.UniformCrossover;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionStream;
import io.jenetics.util.Factory;
import magic.data.CardDefinitions;
import magic.data.MagicFormat;
import magic.model.MagicCardDefinition;
import magic.model.MagicColor;
import magic.model.MagicDeck;
import magic.utility.DeckUtils;

public final class DeckBuilderEngine {
	
	// Magic related parameters
	private final List<MagicCardDefinition> spellPool;
	private final int spellPoolSize;
	private final LandGenerator landGenerator;
	private final MagicDeck[] benchmarkDecks;
	
	// Genetic algorithm parameters
	private static final Engine.Builder<IntegerGene, Integer> DEFAULT_ENGINE_BUILDER =
			Engine.builder(gt -> 0, DeckChromosome.of(0))
				.maximizing()
				.populationSize(40)
				.survivorsFraction(0.25)
				.survivorsSelector(new TournamentSelector<>(3))
				.offspringSelector(new TournamentSelector<>(3))
				.alterers(
						new UniformCrossover<>(0.5, 0.5),
						new GaussianMutator<>(0.2));

	private final Engine<IntegerGene, Integer> engine;
	private final Factory<Genotype<IntegerGene>> gtf;
	
	public DeckBuilderEngine(final MagicFormat format, final MagicColor... colors) {
		this(format, DEFAULT_ENGINE_BUILDER, colors);
	}

	public DeckBuilderEngine(final MagicFormat format, final Engine.Builder<IntegerGene, Integer> engineBuilder, final MagicColor... colors) {
		final int colorMask = Arrays.stream(colors).map(c -> c.getMask()).reduce(0, (c1, c2) -> c1|c2);
		this.spellPool = CardDefinitions.getSpellCards().stream()
				.filter(format::isCardLegal)
				.filter(card -> (card.getColorFlags() | colorMask) == colorMask)
				.collect(Collectors.toList());
		this.spellPoolSize = this.spellPool.size();
		this.landGenerator = new LandPool(format, colors);
		
		try {
			this.benchmarkDecks = Files.walk(Paths.get("resources/benchmark-decks/" + format.getName()))
					.filter(Files::isRegularFile)
					.map(DeckUtils::loadDeckFromFile)
					.toArray(MagicDeck[]::new);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		this.gtf = Genotype.of(DeckChromosome.of(this.spellPoolSize));
		this.engine = engineBuilder.copy()
				.fitnessFunction(this::fitness)
				.genotypeFactory(this.gtf)
				.build();
	}
	
	public final EvolutionStream<IntegerGene, Integer>
	stream() {
		return this.engine.stream();
	}
	
	public final List<MagicCardDefinition> getSpellPool() {
		return this.spellPool;
	}
	
	public final LandGenerator getLandGenerator() {
		return this.landGenerator;
	}
	
	private final Integer fitness(final Genotype<IntegerGene> gt) {
		MagicDeck deck = MagicDeckCreator.getMagicDeck(this.spellPool, gt, this.landGenerator);
		return Arrays.stream(benchmarkDecks).parallel().map(opp -> MagicDuelHandler.getDuelScore(deck, opp)).reduce(Integer::sum).orElse(0);
	}
	
}
