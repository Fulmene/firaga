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

package firaga.magic.land;

import java.util.Arrays;
import java.util.Collections;

import firaga.magic.MagicConstants;
import magic.data.CardDefinitions;
import magic.model.MagicColor;
import magic.model.MagicDeck;

public final class BasicLandGenerator implements LandGenerator {
	
	private static BasicLandGenerator instance;
	
	public static final BasicLandGenerator getInstance() {
		if (instance == null)
			instance = new BasicLandGenerator();
		return instance;
	}
	
	private final class ColorCount {
		
		private final MagicColor color;
		private final int count;
		
		private ColorCount(MagicColor color, int count) {
			this.color = color;
			this.count = count;
		}

	}

	@Override
	public final void addLands(final MagicDeck deck) {
		final ColorCount[] colorCounts =
				Arrays.stream(MagicColor.values())
				.map(color -> new ColorCount(
						color,
						deck.stream().mapToInt(c -> c.getCost().getDevotion(color)).sum()))
				.toArray(ColorCount[]::new);
		final int allColors = Arrays.stream(colorCounts).mapToInt(cc -> cc.count).sum();
		final int landCount = MagicConstants.MIN_DECK_SIZE - deck.size();

		if (allColors != 0) {
			Arrays.stream(colorCounts).forEach(cc ->
			deck.addAll(Collections.nCopies(cc.count * landCount / allColors, CardDefinitions.getBasicLand(cc.color))));
			Arrays.stream(colorCounts)
				.min((cc1, cc2) -> Integer.compare(cc1.count, cc2.count))
				.ifPresent(cc -> deck.addAll(Collections.nCopies(Math.max(landCount - deck.size(), 0), CardDefinitions.getBasicLand(cc.color))));
		}
		else {
			// Use Plains for colourless decks
			deck.addAll(Collections.nCopies(landCount, CardDefinitions.getBasicLand(MagicColor.White)));
		}
	}

}
