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

package firaga;

import java.util.Arrays;

import magic.data.MagicFormat;
import magic.data.MagicPredefinedFormat;
import magic.model.MagicColor;

public final class CmdLineArgs {
	
	final MagicFormat format;
	final MagicColor[] colors;
	final String saveDir;

	public CmdLineArgs(final String[] args) {
		String formatString = null;
		String colorString = null;
		String saveDirString = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--format") || args[i].equals("-f")) {
				if (formatString == null)
					formatString = args[++i];
				else
					throw new IllegalArgumentException("Format specified more than once");
			}
			else if (args[i].equals("--color") || args[i].equals("-c")) {
				if (colorString == null)
					colorString = args[++i];
				else
					throw new IllegalArgumentException("Color specified more than once");
			}
			else if (args[i].equals("--savedir") || args[i].equals("-d")) {
				if (saveDirString == null)
					saveDirString = args[++i];
				else
					throw new IllegalArgumentException("Save directory specified more than once");
			}
			else {
				throw new IllegalArgumentException("Unknown argument: " + args[i]);
			}
		}

		this.format = parseFormat(formatString.trim());
		this.colors = parseColors(colorString.trim());
		if (saveDirString == null)
			this.saveDir = "output_decks";
		else
			this.saveDir = saveDirString;
	}
	
	public final MagicFormat getFormat() {
		return this.format;
	}

	public final MagicColor[] getColors() {
		return this.colors;
	}

	public final String getSaveDir() {
		return this.saveDir;
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
