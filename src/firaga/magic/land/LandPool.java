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
import java.util.List;
import java.util.stream.Collectors;

import magic.data.CardDefinitions;
import magic.data.MagicFormat;
import magic.model.MagicCardDefinition;
import magic.model.MagicColor;
import magic.model.MagicDeck;

public class LandPool implements LandGenerator {
	
	protected final List<MagicCardDefinition> lands;

	public LandPool(MagicFormat format, MagicColor... colors) {
		lands = CardDefinitions.getNonBasicLandCards()
				.filter(format::isCardLegal)
				.filter(card -> Arrays.stream(colors).map(card::getManaSource).reduce(Integer::sum).get() >= 5)
				.collect(Collectors.toList());
		if (colors.length > 0)
			lands.addAll(Arrays.stream(colors).map(CardDefinitions::getBasicLand).collect(Collectors.toList()));
		else // Use plains for colourless decks
			lands.add(CardDefinitions.getBasicLand(MagicColor.White));
	}

	@Override
	public void addLands(MagicDeck deck) {
		// TODO Work with non-basic lands
		
		BasicLandGenerator.getInstance().addLands(deck);
	}

}
