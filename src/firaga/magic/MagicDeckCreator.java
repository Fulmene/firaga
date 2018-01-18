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

package firaga.magic;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import firaga.magic.land.LandGenerator;
import io.jenetics.Genotype;
import io.jenetics.IntegerGene;
import magic.data.DeckType;
import magic.model.MagicCardDefinition;
import magic.model.MagicDeck;

public final class MagicDeckCreator {
	
	public static final MagicDeck getMagicDeck(final List<MagicCardDefinition> cardPool, final Genotype<IntegerGene> genotype, final LandGenerator landGenerator) {
		final MagicDeck deck = new MagicDeck();
		IntStream.range(0, cardPool.size())
			.forEach(i -> deck.addAll(Collections.nCopies(genotype.get(0, i).intValue(), cardPool.get(i))));
		deck.setDeckType(DeckType.Custom);
		landGenerator.addLands(deck);
		return deck;
	}
	
}
