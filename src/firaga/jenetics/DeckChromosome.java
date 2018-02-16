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
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import firaga.magic.MagicConstants;
import io.jenetics.IntegerChromosome;
import io.jenetics.IntegerGene;
import io.jenetics.util.ISeq;
import io.jenetics.util.IntRange;
import io.jenetics.util.RandomRegistry;

public class DeckChromosome extends IntegerChromosome {

    private static final long serialVersionUID = 1L;

    private static final IntegerGene GENE_PROTOTYPE = IntegerGene.of(0, MagicConstants.MAX_COPIES);

    protected final ISeq<IntegerGene> empty;
    protected final int spellPoolSize;

    public DeckChromosome(final int spellPoolSize) {
        super(0, MagicConstants.MAX_COPIES, spellPoolSize);
        empty = ISeq.of(Collections.nCopies(spellPoolSize, GENE_PROTOTYPE.newInstance(0)));
        this.spellPoolSize = spellPoolSize;
    }

    private DeckChromosome(final ISeq<IntegerGene> genes, final int spellPoolSize) {
        super(genes, IntRange.of(spellPoolSize));
        empty = ISeq.of(Collections.nCopies(spellPoolSize, GENE_PROTOTYPE.newInstance(0)));
        this.spellPoolSize = spellPoolSize;
    }

    @Override
    public final int length() {
        return this.spellPoolSize;
    }

    @Override
    public final IntegerChromosome newInstance(ISeq<IntegerGene> genes) {
        return new DeckChromosome(validateDeckSize(genes), this.spellPoolSize);
    }

    @Override
    public IntegerChromosome newInstance() {
        final ISeq<IntegerGene> genes = addCards(this.empty, getRandomSpellCount());
        return new DeckChromosome(genes, genes.size());
    }

    public static final DeckChromosome of(final int spellPoolSize) {
        return new DeckChromosome(spellPoolSize);
    }

    private static final ISeq<IntegerGene> validateDeckSize(final ISeq<IntegerGene> genes) {
        final int spellCount = genes.stream().mapToInt(g -> g.intValue()).sum();
        if (spellCount < MagicConstants.MIN_SPELLS) {
            return addCards(genes, getRandomSpellCount());
        }
        else if (spellCount > MagicConstants.MAX_SPELLS) {
            return removeCards(genes, getRandomSpellCount());
        }
        else {
            return genes;
        }
    }

    private static final ISeq<IntegerGene> addCards(final ISeq<IntegerGene> genes, final int targetSpellCount) {
        final int[] newGenes = genes.stream().mapToInt(g -> g.intValue()).toArray();
        int spellCount = Arrays.stream(newGenes).sum();

        final int spellPoolSize = genes.size();
        final Random random = RandomRegistry.getRandom();

        List<Integer> availableCards = IntStream.range(0, spellPoolSize)
            .filter(n -> newGenes[n] == 0)
            .boxed()
            .collect(Collectors.toList());

        while (spellCount < targetSpellCount) {
            final int selectedIndex = random.nextInt(availableCards.size());
            final int selected = availableCards.get(selectedIndex);
            final int amount = Math.min(targetSpellCount - spellCount, MagicConstants.MAX_COPIES - random.nextInt(2));
            newGenes[selected] += amount;
            availableCards.remove(selectedIndex);
            spellCount += amount;
        }

        return Arrays.stream(newGenes).boxed().map(GENE_PROTOTYPE::newInstance).collect(ISeq.toISeq());
    }

    private static final ISeq<IntegerGene> removeCards(final ISeq<IntegerGene> genes, final int targetSpellCount) {
        final int[] newGenes = genes.stream().mapToInt(g -> g.intValue()).toArray();
        int spellCount = Arrays.stream(newGenes).sum();

        final int spellPoolSize = genes.size();
        final Random random = RandomRegistry.getRandom();

        List<Integer> availableCards = IntStream.range(0, spellPoolSize)
            .filter(n -> newGenes[n] > 0)
            .boxed()
            .collect(Collectors.toList());

        while (spellCount > targetSpellCount) {
            final int selectedIndex = random.nextInt(availableCards.size());
            final int selected = availableCards.get(selectedIndex);
            final int amount = Math.min(spellCount - targetSpellCount, newGenes[selected]);
            newGenes[selected] -= amount;
            availableCards.remove(selectedIndex);
            spellCount -= amount;
        }

        return Arrays.stream(newGenes).boxed().map(GENE_PROTOTYPE::newInstance).collect(ISeq.toISeq());
    }

    private static int getRandomSpellCount() {
        return MagicConstants.MIN_SPELLS + RandomRegistry.getRandom().nextInt(MagicConstants.MAX_SPELLS - MagicConstants.MIN_SPELLS + 1);
    }

}
