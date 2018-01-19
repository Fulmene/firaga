package firaga;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import firaga.jenetics.DeckBuilderEngine;
import firaga.magic.MagicDeckCreator;
import firaga.magic.land.BasicLandGenerator;
import magic.data.CardDefinitions;
import magic.data.MagicPredefinedFormat;
import magic.model.MagicCardDefinition;
import magic.model.MagicColor;
import magic.utility.MagicSystem;
import magic.utility.ProgressReporter;

public final class Main {
	
	private static int colorMask = 0;
	
	private static final void parseArguments(final String[] args) {
		String colors = null;

		if (args.length < 1) {
			System.err.println("Usage: firaga [-c[COLORS]|--color [COLORS]]");
			System.exit(1);
		}

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--color") || args[i].startsWith("-c")) {
				if (colors == null) {
					if (args[i].startsWith("--"))
						colors = args[i+1];
					else
						colors = args[i].substring(2);
				}
				else {
					System.err.println("ERROR: Colors assigned multiple times.");
					System.exit(1);
				}
			}
		}
		
		parseColor(colors);
	}
	
	private static final void parseColor(final String colors) {
		colorMask = 0;
		final char[] upperCaseColorsArray = colors.toUpperCase().toCharArray();
		Arrays.sort(upperCaseColorsArray);
		final String sortedUpperCaseColors = new String(upperCaseColorsArray);
		if (sortedUpperCaseColors.equals("C"))
			return;
		else if (sortedUpperCaseColors.matches("(B?)(G?)(R?)(U?)(W?)")) {
			if (sortedUpperCaseColors.contains("B"))
				colorMask |= MagicColor.Black.getMask();
			if (sortedUpperCaseColors.contains("G"))
				colorMask |= MagicColor.Green.getMask();
			if (sortedUpperCaseColors.contains("R"))
				colorMask |= MagicColor.Red.getMask();
			if (sortedUpperCaseColors.contains("U"))
				colorMask |= MagicColor.Blue.getMask();
			if (sortedUpperCaseColors.contains("W"))
				colorMask |= MagicColor.White.getMask();
		}
		else {
			System.err.println("ERROR: Colors must be C (colorless) or any combination of WUBRG.");
			System.exit(1);
		}
	}

	public static final void main(final String[] args) {
		
		final long startTime = System.nanoTime();
		
		// Initialise Magarena
		ProgressReporter reporter = new ProgressReporter();
		MagicSystem.initialize(reporter);
		
		parseArguments(args);

		List<MagicCardDefinition> spellPool =
				CardDefinitions.getSpellCards().stream()
					.filter(MagicPredefinedFormat.IXALAN_STANDARD::isCardLegal)
					.filter(cdef -> (cdef.getColorFlags() | colorMask) == colorMask)
					.collect(Collectors.toList());
		DeckBuilderEngine engine = new DeckBuilderEngine(spellPool);
		System.out.println(MagicDeckCreator.getMagicDeck(spellPool, engine.run(), BasicLandGenerator.getInstance()));
		
		System.out.println("Time: " + (System.nanoTime() - startTime));
	}

}
