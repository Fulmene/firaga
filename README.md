# FiraGA
FiraGA is a collectible card game deck builder based on Genetic Algorithm.
It uses [Jenetics](http://jenetics.io) as the Genetic Algorithm implementation, and a card game playing AI system to playtest the deck and determine the fitness value of a deck.

Currently, it uses [Magarena](https://magarena.github.io)'s AI and rule enforcement system to test the deck.

FiraGA requires Java 8 or greater.

## Building
Clone the repository and its submodules. Build Magarena inside lib/magarena first.
```
./gradlew build
```

## Usage
```
./gradlew run -PrunArgs=[args]
```
Where [args] is comma-separated
```
--format	-f	FORMAT	The format name. In Magarena, the format name is the one shown in selection menu like "Standard" or "Modern"
--color		-c	COLOR	The color of the deck to build. COLOR is either C, which means a colorless deck, or a combination of WUBRG which tells that the deck contains which color: white(W), blue(U), black(B), red(R), and green(G).
--savedir	-d	SAVEDIR	The save location of the output deck. Decks generated during each generation will be saved. Defaults to "output_decks".
```

### Benchmark decks
Benchmark decks must be stored inside Magarena directory: `lib/magarena/release/Magarena/decks/`

The name of the deck file must be in the form: `Benchmark_[FORMAT_NAME]_[DECK_NAME].dec`

### Development
During this development phase, the format for testing the capability of FiraGA will be the Ixalan Standard format.
