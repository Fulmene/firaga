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
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import firaga.magic.MagicConstants;
import io.jenetics.Chromosome;
import io.jenetics.IntegerGene;
import io.jenetics.NumericChromosome;
import io.jenetics.util.ISeq;
import io.jenetics.util.RandomRegistry;

public final class DeckChromosome implements NumericChromosome<Integer, IntegerGene> {

	private final int cardPoolSize;
	private final ISeq<IntegerGene> genes;

	private static final IntegerGene GENE_PROTOTYPE = IntegerGene.of(0, MagicConstants.MAX_COPIES);

	public DeckChromosome(final int cardPoolSize) {
		this.cardPoolSize = cardPoolSize;
		this.genes = null;
	}
	
	private DeckChromosome(final ISeq<IntegerGene> genes, final int cardPoolSize) {
		this.cardPoolSize = cardPoolSize;
		this.genes = genes;
	}
	
	@Override
	public final IntegerGene getGene(int index) {
		return genes.get(index);
	}

	@Override
	public final int length() {
		return cardPoolSize;
	}

	@Override
	public final Chromosome<IntegerGene> newInstance(ISeq<IntegerGene> genes) {
		return new DeckChromosome(validateDeckSize(genes), this.cardPoolSize);
	}

	@Override
	public final ISeq<IntegerGene> toSeq() {
		return this.genes;
	}

	@Override
	public final boolean isValid() {
		final int spellCount = genes.stream().mapToInt(g -> g.intValue()).sum();
		return spellCount >= MagicConstants.MIN_SPELLS && spellCount <= MagicConstants.MAX_SPELLS;
	}

	@Override
	public final Iterator<IntegerGene> iterator() {
		return genes.iterator();
	}

	@Override
	public final Chromosome<IntegerGene> newInstance() {
		ISeq<IntegerGene> emptyGenes = ISeq.of(Collections.nCopies(this.cardPoolSize, GENE_PROTOTYPE.newInstance(0)));
		final int targetSpellCount = MagicConstants.MIN_SPELLS + RandomRegistry.getRandom().nextInt(MagicConstants.MAX_SPELLS - MagicConstants.MIN_SPELLS + 1);
		ISeq<IntegerGene> genes = addCards(emptyGenes, targetSpellCount, true);
		return new DeckChromosome(genes, this.cardPoolSize);
	}
	
	public static final ISeq<IntegerGene> validateDeckSize(final ISeq<IntegerGene> genes) {
		final int spellCount = genes.stream().mapToInt(g -> g.intValue()).sum();
		final Random random = RandomRegistry.getRandom();
		if (spellCount < MagicConstants.MIN_SPELLS) {
			final int targetSpellCount = MagicConstants.MIN_SPELLS + random.nextInt(MagicConstants.MAX_SPELLS - MagicConstants.MIN_SPELLS + 1);
			final boolean useNewCards = random.nextBoolean();
			return addCards(genes, targetSpellCount, useNewCards);
		}
		else if (spellCount > MagicConstants.MAX_SPELLS) {
			final int targetSpellCount = MagicConstants.MIN_SPELLS + random.nextInt(MagicConstants.MAX_SPELLS - MagicConstants.MIN_SPELLS + 1);
			return removeCards(genes, targetSpellCount);
		}
		else {
			return genes;
		}
	}
	
	public static final DeckChromosome of(final int cardPoolSize) {
		return new DeckChromosome(cardPoolSize);
	}
	
	private static final ISeq<IntegerGene> addCards(final ISeq<IntegerGene> genes, final int targetSpellCount, final boolean useNewCards) {
		final int[] newGenes = genes.stream().mapToInt(g -> g.intValue()).toArray();
		int spellCount = Arrays.stream(newGenes).sum();

		final int cardPoolSize = genes.size();
		final Random random = RandomRegistry.getRandom();

		List<Integer> availableCards = IntStream.range(0, cardPoolSize)
				.filter(n -> {
					return (useNewCards || newGenes[n] > 0) && newGenes[n] < MagicConstants.MAX_COPIES; })
				.boxed()
				.collect(Collectors.toList());

		while (spellCount < targetSpellCount) {
			
			// If there is no more available cards,
			// it means useNewCards is false, but all available cards are maxed.
			// So, add unused cards to availableCards.
			if (availableCards.isEmpty()) {
				IntStream.range(0, cardPoolSize)
					.filter(n -> newGenes[n] == 0)
					.forEachOrdered(availableCards::add);
			}

			final int selectedIndex = random.nextInt(availableCards.size());
			final int selected = availableCards.get(selectedIndex);
			newGenes[selected]++;
			if (newGenes[selected] == MagicConstants.MAX_COPIES)
				availableCards.remove(selectedIndex);
			spellCount++;
		}
		
		return Arrays.stream(newGenes).boxed().map(GENE_PROTOTYPE::newInstance).collect(ISeq.toISeq());
	}
	
	private static final ISeq<IntegerGene> removeCards(final ISeq<IntegerGene> genes, final int targetSpellCount) {
		final int[] newGenes = genes.stream().mapToInt(g -> g.intValue()).toArray();
		int spellCount = Arrays.stream(newGenes).sum();

		final int cardPoolSize = genes.size();
		final Random random = RandomRegistry.getRandom();

		List<Integer> availableCards = IntStream.range(0, cardPoolSize)
				.filter(n -> newGenes[n] > 0)
				.boxed()
				.collect(Collectors.toList());

		while (spellCount < targetSpellCount) {
			final int selectedIndex = random.nextInt(availableCards.size());
			final int selected = availableCards.get(selectedIndex);
			newGenes[selected]--;
			if (newGenes[selected] == 0)
				availableCards.remove(selectedIndex);
			spellCount--;
		}
	
		return Arrays.stream(newGenes).boxed().map(GENE_PROTOTYPE::newInstance).collect(ISeq.toISeq());
	}

}
