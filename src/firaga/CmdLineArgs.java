package firaga;

import java.util.Arrays;

import magic.data.MagicFormat;
import magic.data.MagicPredefinedFormat;
import magic.model.MagicColor;

public final class CmdLineArgs {
	
	final MagicFormat format;
	final MagicColor[] colors;

	public CmdLineArgs(final String[] args) {
		String formatString = null;
		String colorString = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--format") || args[i].startsWith("-f")) {
				if (formatString == null) {
					if (args[i].startsWith("--"))
						formatString = args[++i];
					else
						formatString = args[i].substring(2);
				}
				else {
					throw new IllegalArgumentException("Format specified more than once");
				}
			}
			else if (args[i].equals("--color") || args[i].startsWith("-c")) {
				if (colorString == null) {
					if (args[i].startsWith("--"))
						colorString = args[++i];
					else
						colorString = args[i].substring(2);
				}
				else {
					throw new IllegalArgumentException("Color specified more than once");
				}
			}
			else {
				throw new IllegalArgumentException("Unknown argument: " + args[i]);
			}
		}

		this.format = parseFormat(formatString.trim());
		this.colors = parseColors(colorString.trim());
	}
	
	public final MagicFormat getFormat() {
		return this.format;
	}

	public final MagicColor[] getColors() {
		return this.colors;
	}
	
	private static final MagicFormat parseFormat(final String formatString) {
		return MagicPredefinedFormat.values().stream()
				.filter(fmt -> fmt.getName().equalsIgnoreCase(formatString))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Unknown format"));
	}
	
	private static final MagicColor[] parseColors(final String colorString) {
		final char[] lowerCaseColorsArray = colorString.toLowerCase().toCharArray();
		Arrays.sort(lowerCaseColorsArray);
		final String sortedLowerCaseColors = new String(lowerCaseColorsArray);
		if (sortedLowerCaseColors.equals("c"))
			return new MagicColor[0];
		else if (sortedLowerCaseColors.matches("b?g?r?u?w?")) {
			return sortedLowerCaseColors.chars().mapToObj(i -> (char)i)
					.map(MagicColor::getColor)
					.toArray(MagicColor[]::new);
		}
		else {
			throw new IllegalArgumentException("Colors is not be C (colorless) or any combination of WUBRG.");
		}
	}

}
