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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import firaga.magic.MagicDeckCreator;
import firaga.magic.MagicDuelHandler;
import firaga.magic.land.LandGenerator;
import firaga.magic.land.LandPool;
import io.jenetics.EliteSelector;
import io.jenetics.GaussianMutator;
import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.IntegerGene;
import io.jenetics.Phenotype;
import io.jenetics.TournamentSelector;
import io.jenetics.TruncationSelector;
import io.jenetics.UniformCrossover;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import io.jenetics.util.Factory;
import io.jenetics.util.ISeq;
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
    private final String saveDir;

    // Genetic algorithm parameters
    public static final Engine.Builder<IntegerGene, Integer> DEFAULT_ENGINE_BUILDER =
        Engine.builder(gt -> 0, IntegerChromosome.of(0, 0, 1))
        .maximizing()
        .populationSize(20)
        .survivorsFraction(0.1)
        .survivorsSelector(new TruncationSelector<>(2))
        .offspringSelector(new TournamentSelector<>(3))
        .alterers(
                new UniformCrossover<>(1.0, 0.5),
                new GaussianMutator<>(0.2));

    private final Engine<IntegerGene, Integer> engine;
    private final Factory<Genotype<IntegerGene>> gtf;

    public DeckBuilderEngine(final MagicFormat format, final MagicColor... colors) {
        this(format, DEFAULT_ENGINE_BUILDER, "output_decks", colors);
    }

    public DeckBuilderEngine(final MagicFormat format, final Engine.Builder<IntegerGene, Integer> engineBuilder, final MagicColor... colors) {
        this(format, engineBuilder, "output_decks", colors);
    }

    public DeckBuilderEngine(final MagicFormat format, final String savePath, final MagicColor... colors) {
        this(format, DEFAULT_ENGINE_BUILDER, savePath, colors);
    }

    public DeckBuilderEngine(final MagicFormat format, final Engine.Builder<IntegerGene, Integer> engineBuilder, final String saveDir, final MagicColor... colors) {
        final int colorMask = Arrays.stream(colors).map(c -> c.getMask()).reduce(0, (c1, c2) -> c1|c2);
        this.spellPool = CardDefinitions.getSpellCards().stream()
            .filter(format::isCardLegal)
            .filter(card -> (card.getColorFlags() | colorMask) == colorMask)
            .collect(Collectors.toList());
        this.spellPoolSize = this.spellPool.size();
        this.landGenerator = new LandPool(format, colors);

        final String formatNameWithUnderscore = format.getName().replace(' ', '_');
        try {
            this.benchmarkDecks = Files.walk(DeckUtils.getDecksFolder())
                .filter(Files::isRegularFile)
                .filter(file -> file.getFileName().toString().startsWith("Benchmark_" + formatNameWithUnderscore))
                .map(DeckUtils::loadDeckFromFile)
                .toArray(MagicDeck[]::new);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final StringBuilder savePathBuilder = new StringBuilder();
        savePathBuilder.append(saveDir);
        savePathBuilder.append('/');
        savePathBuilder.append(formatNameWithUnderscore);
        savePathBuilder.append('/');
        Arrays.stream(colors).sequential().forEach(c -> 
                savePathBuilder.append(Character.toUpperCase(c.getSymbol())));
        savePathBuilder.append('/');
        this.saveDir = savePathBuilder.toString();

        File saveDirFile = new File(this.saveDir);
        if (!saveDirFile.exists())
            saveDirFile.mkdirs();
        else if (!saveDirFile.isDirectory())
            throw new RuntimeException("Cannot create save directory");
        else if (!saveDirFile.canWrite())
            throw new RuntimeException("Cannot write to the save directory");

        this.gtf = Genotype.of(DeckChromosome.of(this.spellPoolSize));
        this.engine = engineBuilder.copy()
            .fitnessFunction(this::fitness)
            .genotypeFactory(this.gtf)
            .build();
    }

    public final Stream<EvolutionResult<IntegerGene, Integer>>
    stream() {
        return this.engine.stream().limit(Limits.bySteadyFitness(10)).peek(this::saveDecks);
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

    private final void saveDecks(EvolutionResult<IntegerGene, Integer> result) {
        if (result.getGeneration() % 5 == 0) {
            final ISeq<Phenotype<IntegerGene, Integer>> population = result.getPopulation();
            final long generation = result.getGeneration();
            System.out.println("End generation " + generation);
            System.out.println(result.getDurations().getEvolveDuration());
            final String generationSaveDir = this.saveDir + "Generation_" + generation + "/";
            new File(generationSaveDir).mkdir();
            IntStream.range(0, population.size()).forEach(i -> {
                final MagicDeck deck = MagicDeckCreator.getMagicDeck(this.spellPool, population.get(i).getGenotype(), this.landGenerator);
                DeckUtils.saveDeck(generationSaveDir + "Deck_" + i + "_(" + population.get(i).getFitness() + ").dec", deck);
            });
        }
    }

}
