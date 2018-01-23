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
	
	private final List<MagicCardDefinition> lands;

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
