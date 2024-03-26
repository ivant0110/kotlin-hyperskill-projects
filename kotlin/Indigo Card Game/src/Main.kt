package indigo

enum class Suits(val suit: String) {
	DIAMONDS("♦"),
	HEARTS("♥"),
	SPADES("♠"),
	CLUBS("♣")
}

enum class Ranks(val rank: String) {
	ACE("A"),
	TWO("2"),
	THREE("3"),
	FOUR("4"),
	FIVE("5"),
	SIX("6"),
	SEVEN("7"),
	EIGHT("8"),
	NINE("9"),
	TEN("10"),
	JACK("J"),
	QUEEN("Q"),
	KING("K")
}

enum class Constants(var value: Int) {
	TABLE_CARDS(4),
	DEAL_CARDS(6),
	MAX_CARDS(52)
}

class Card(val rank: String, val suit: String) {
	val card = rank + suit
}

class Player(
	val name: String,
	val playingCards: MutableList<Card> = mutableListOf(),
	val cardsWon: MutableList<Card> = mutableListOf(),
	var score: Int = 0
)

class GameInfo(var gameState: String = "playing") { // "playing", "over"
	lateinit var activePlayer: Player
	lateinit var playsFirst: Player
	lateinit var wonLastHand: Player
}

var cardsDeck: MutableList<Card> = mutableListOf()

val player = Player("Player")
val computer = Player("Computer")
val table = Player("Table")
val gameInfo = GameInfo()

fun main() {
	println("Indigo Card Game")
	setFirstPlayer()
	beginGame()
	playGame()
	println("Game Over")
}

fun setFirstPlayer() {
	while (true) {
		println("Play first?")
		val playFirst = readln().lowercase()
		when (playFirst) {
			"yes" -> {
				gameInfo.playsFirst = player
				gameInfo.activePlayer = player
				break
			}

			"no" -> {
				gameInfo.playsFirst = computer
				gameInfo.activePlayer = computer
				break
			}
		}
	}
}

fun beginGame() {
	resetDeck()
	shuffleDeck()
	getCards(Constants.TABLE_CARDS.value, table)
	getCards(Constants.DEAL_CARDS.value, player)
	getCards(Constants.DEAL_CARDS.value, computer)
	println("Initial cards on the table: ${
		table.playingCards
			.joinToString(" ") { it.card }
	}"
	)
}

fun playGame() {
	while (gameInfo.gameState != "over") {
		printTableCardsMessage()
		dealCardsIfHandIsEmpty()
		val keepPlaying = playYourTurn()
		if (!keepPlaying) return
		checkScore()
		changeActivePlayer()
		checkEndingCondition()
	}
}

fun printTableCardsMessage() {
	if (table.playingCards.isEmpty()) {
		println("\nNo cards on the table")
	} else {
		val count = table.playingCards.size
		val topCard = table.playingCards.last().card
		println("\n$count cards on the table, and the top card is $topCard")
	}
}

fun dealCardsIfHandIsEmpty() {
	if (player.playingCards.size == 0 && computer.playingCards.size == 0) {
		getCards(Constants.DEAL_CARDS.value, player)
		getCards(Constants.DEAL_CARDS.value, computer)
	}
}

fun playYourTurn(): Boolean {
	when (gameInfo.activePlayer) {
		player -> {
			val sb = StringBuilder("Cards in hand: ")
			for ((index, card) in player.playingCards.withIndex()) {
				sb.append("${index + 1})${card.card} ")
			}
			println(sb.toString())
			while (true) {
				println("Choose a card to play (1-${player.playingCards.size}):")
				val inputCard = readln()
				if (inputCard == "exit") {
					exitGame()
					return false
				}
				val cardToPlay = inputCard.toIntOrNull() ?: -1
				if (cardToPlay in 1..player.playingCards.size) {
					playCard(player, cardToPlay - 1)
					break
				}
			}
		}

		computer -> {
			println(computer.playingCards.joinToString(" ") { it.card })
			if (computer.playingCards.size >= 1) {
				var cardToPlayIndex = 0

				if ((computer.playingCards.size > 1)) {
					val candidateCards = findCandidateCards()

					cardToPlayIndex = if (candidateCards.size == 1) {
						// If there is only one candidate card, put it on the table
						candidateCards.first()
					} else {
						// If there are more candidate cards, pick one at random
						candidateCards.random()
					}
				}
				println("Computer plays ${computer.playingCards[cardToPlayIndex].card}")
				playCard(computer, cardToPlayIndex)
			}
		}
	}
	return true
}

fun findCandidateCards(): List<Int> {
	val listOfIndices: MutableList<Int> = mutableListOf()

	if (table.playingCards.isEmpty()) {
		val indices = runIfNoCandidatesFound()
		listOfIndices.addAll(indices)
	} else {
		val candidateIndices = lookForCandidates()
		listOfIndices.addAll(candidateIndices)

		//  If there are cards on the table but no candidate cards
		if (listOfIndices.isEmpty()) {
			val indices = runIfNoCandidatesFound()
			listOfIndices.addAll(indices)
		}
		// If nothing of the above is applicable, then throw any of the candidate cards at random
		if (listOfIndices.isEmpty()) {
			val randomIndex = pickRandomCard()
			listOfIndices.add(randomIndex)
		}
	}
	return listOfIndices
}

fun lookForCandidates(): MutableList<Int> {
	val indices: MutableList<Int> = mutableListOf()
	val tableTopCard = table.playingCards.last()

	// Look for candidate cards with the same suite
	for ((index, card) in computer.playingCards.withIndex()) {
		if (tableTopCard.suit == card.suit) {
			indices.add(index)
		}
	}
	// If there are no cards with same suite look for cards with the same rank
	if (indices.isEmpty()) {
		for ((index, card) in computer.playingCards.withIndex()) {
			if (tableTopCard.rank == card.rank) {
				indices.add(index)
			}
		}
	}
	return indices
}

fun runIfNoCandidatesFound(): MutableList<Int> {
	val listOfIndices: MutableList<Int> = mutableListOf()
	// First look for cards with same suite
	val sameSuites = lookForCardsWithSameProperty(groupBySuite = true)
	listOfIndices.addAll(sameSuites)

	// If there are no cards with same suite look for cards with the same rank
	if (listOfIndices.isEmpty()) {
		val sameRanks = lookForCardsWithSameProperty(groupBySuite = false)
		listOfIndices.addAll(sameRanks)
	}
	//  If there are no cards with the same suit or rank, pick any card at random
	if (listOfIndices.isEmpty()) {
		val randomIndex = pickRandomCard()
		listOfIndices.add(randomIndex)
	}
	return listOfIndices
}

fun lookForCardsWithSameProperty(groupBySuite: Boolean): MutableList<Int> {
	val list: MutableList<Int> = mutableListOf()

	val countByProperty = if (groupBySuite) {
		computer.playingCards.groupingBy { it.suit }.eachCount()
	} else {
		computer.playingCards.groupingBy { it.rank }.eachCount()
	}

	for ((index, card) in computer.playingCards.withIndex()) {
		if ((countByProperty[if (groupBySuite) card.suit else card.rank] ?: 0) > 1) {
			list.add(index)
		}
	}
	return list
}

fun pickRandomCard(): Int {
	val randomCard = computer.playingCards.random()
	val randomIndex = computer.playingCards.indexOf(randomCard)
	return randomIndex
}

fun checkScore() {
	if (table.playingCards.size > 1 &&
		(table.playingCards.last().suit == table.playingCards[table.playingCards.lastIndex - 1].suit ||
				table.playingCards.last().rank == table.playingCards[table.playingCards.lastIndex - 1].rank)
	) {
		gameInfo.wonLastHand = gameInfo.activePlayer
		println("${gameInfo.activePlayer.name} wins cards")
		winCards()
		calculateScore()
		printScore()
	}
}

fun winCards() {
	gameInfo.activePlayer.cardsWon.addAll(table.playingCards)
	table.playingCards.clear()
}

fun calculateScore() {
	val playerList = listOf(player, computer)
	for (element in playerList) {
		element.score = 0
		for (card in element.cardsWon) {
			if (card.rank == Ranks.ACE.rank) element.score++
			if (card.rank == Ranks.TEN.rank) element.score++
			if (card.rank == Ranks.JACK.rank) element.score++
			if (card.rank == Ranks.QUEEN.rank) element.score++
			if (card.rank == Ranks.KING.rank) element.score++
		}
	}
	if (gameInfo.gameState == "over") {
		if (player.cardsWon.size > computer.cardsWon.size) {
			player.score += 3
		} else if (computer.cardsWon.size > player.cardsWon.size) {
			computer.score += 3
		} else {
			gameInfo.playsFirst.score += 3
		}
	}
}

fun printScore() {
	println("Score: Player ${player.score} - Computer ${computer.score}")
	println("Cards: Player ${player.cardsWon.size} - Computer ${computer.cardsWon.size}")
}

fun changeActivePlayer() {
	if (gameInfo.activePlayer == player) {
		gameInfo.activePlayer = computer
	} else if (gameInfo.activePlayer == computer) {
		gameInfo.activePlayer = player
	}
}

fun checkEndingCondition() {
	if (table.playingCards.size + player.cardsWon.size + computer.cardsWon.size == Constants.MAX_CARDS.value) {
		gameInfo.gameState = "over"
		printTableCardsMessage()
		if (table.playingCards.isNotEmpty()) {
			gameInfo.wonLastHand.cardsWon.addAll(table.playingCards)
			table.playingCards.clear()
		}
		calculateScore()
		printScore()
	}
}

fun resetDeck() {
	if (cardsDeck.isNotEmpty()) {
		cardsDeck.clear()
	}
	for (rank in Ranks.values()) {
		for (suit in Suits.values()) {
			val newCard = Card(rank.rank, suit.suit)
			cardsDeck.add(newCard)
		}
	}
}

fun shuffleDeck() {
	cardsDeck = cardsDeck.shuffled().toMutableList()
}

fun getCards(numberOfCards: Int, player: Player) {

	if (numberOfCards !in 1..Constants.MAX_CARDS.value) {
		println("Invalid number of cards.")
		return
	}
	if (numberOfCards > cardsDeck.size) {
		println("The remaining cards are insufficient to meet the request.")
		return
	}
	repeat(numberOfCards) {
		val card = cardsDeck.removeAt(cardsDeck.lastIndex)
		player.playingCards.add(card)
	}
}

fun playCard(player: Player, cardIndex: Int) {
	val card = player.playingCards.removeAt(cardIndex)
	table.playingCards.add(card)
}

fun exitGame() {
	gameInfo.gameState = "over"
}