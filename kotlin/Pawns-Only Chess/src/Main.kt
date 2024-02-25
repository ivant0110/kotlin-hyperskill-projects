package chess

import java.util.*

enum class Pawns(val color: String, val pawn: Char) {
	WHITE("white", 'W'),
	BLACK("black", 'B'),
	EMPTY("empty", ' ')
}
data class Player(var name: String, var color: String, var pawn: Char)
data class GameInfo(
	var activePlayer: Player
)
val board: MutableList<MutableList<Char>> = mutableListOf()
val movesHistory: MutableList<Array<Int>> = mutableListOf()
val boardRange = 0..7

fun main() {
	println("Pawns-Only Chess")
	val playerNames = getPlayerNames()
	val playerOne = Player(playerNames[0], Pawns.WHITE.color, Pawns.WHITE.pawn)
	val playerTwo = Player(playerNames[1], Pawns.BLACK.color, Pawns.BLACK.pawn)
	val gameInfo = GameInfo(playerOne)
	createEmptyBoard()
	addPawnsToInitialPosition()
	printBoard()
	playGame(gameInfo, playerOne, playerTwo)
}

fun getPlayerNames(): Array<String> {
	println("First Player's name:")
	val player1 = readln().trim()
	println("Second Player's name:")
	val player2 = readln().trim()
	return arrayOf(player1, player2)
}

fun createEmptyBoard() {
	for (row in boardRange) {
		board.add(mutableListOf())
		for (col in boardRange) {
			board[row].add(Pawns.EMPTY.pawn)
		}
	}
}

fun addPawnsToInitialPosition() {
	for (col in boardRange) {
		board[1][col] = Pawns.BLACK.pawn
		board[6][col] = Pawns.WHITE.pawn
	}
}

fun printBoard() {
	val files = "abcdefgh"
	val border = "  +---+---+---+---+---+---+---+---+"
	println(border)
	var rank = 8
	for (row in boardRange) {
		var output = "$rank "
		for (col in boardRange) {
			output += "| ${board[row][col]} "
		}
		output += "|"
		rank--
		println(output)
		println(border)
	}
	var outputFiles = " "
	for (file in files) {
		outputFiles += "   $file"
	}
	println(outputFiles)
}

fun playGame(gameInfo: GameInfo, playerOne: Player, playerTwo: Player) {
	while (true) {
		println("${gameInfo.activePlayer.name}'s turn:")
		val input = readln().trim()
		if (input == "exit") {
			break
		}
		val inputValidated = validateInputFormat(input)
		if (!inputValidated) {
			println("Invalid Input")
			continue
		}
		val pawnMoved = pawnMovementLogic(input.uppercase(), gameInfo)
		if (!pawnMoved) continue

		printBoard()

		val weHaveAWinner = checkWinningCondition()
		if (weHaveAWinner) {
			val winnerColor = (gameInfo.activePlayer.color).replaceFirstChar {
				if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
			}
			println("$winnerColor Wins!")
			break
		}

		gameInfo.activePlayer = when (gameInfo.activePlayer) {
			playerOne -> playerTwo
			else -> playerOne
		}

		val weHaveAStalemate = checkStalemateCondition(gameInfo)
		if (weHaveAStalemate) {
			println("Stalemate!")
			break
		}
	}
	println("Bye!")
}

fun validateInputFormat(input: String): Boolean {
	val regex = "[a-hA-H][1-8][a-hA-H][1-8]".toRegex()
	return regex.matches(input)
}

fun pawnMovementLogic(input: String, gameInfo: GameInfo): Boolean {
	val coordinates = getBoardCoordinates(input)
	val (row1, col1, row2, col2) = coordinates
	val direction = if (gameInfo.activePlayer.color == Pawns.WHITE.color) 1 else -1
	val startRow = if (gameInfo.activePlayer.color == Pawns.WHITE.color) 6 else 1

	val pawnFound = findPawnAtStartingPosition(row1, col1, gameInfo, input)
	if (!pawnFound) return false
	// attempts pawn capture if conditions are valid
	if (row1 == row2 + 1 * direction && (col1 == col2 - 1 || col1 == col2 + 1)) {
		if (board[row2][col2] == Pawns.EMPTY.pawn) {
			return attemptEnPassantMove(gameInfo, coordinates, direction)
		} else {
			return attemptCaptureMove(row2, col2, gameInfo, coordinates)
		}
	}
	// attempts pawn move if conditions are valid
	if (col1 != col2) {
		println("Invalid Input")
		return false
	} else {
		return attemptPawnMove(coordinates, gameInfo, startRow, direction)
	}
}

fun getBoardCoordinates(input: String): Array<Int> {
	val row1 = 8 - input[1].digitToInt()
	val col1 = input[0].code - 65
	val row2 = 8 - input[3].digitToInt()
	val col2 = input[2].code - 65
	return arrayOf(row1, col1, row2, col2)
}

fun findPawnAtStartingPosition(row1: Int, col1: Int, gameInfo: GameInfo, input: String): Boolean {
	if (board[row1][col1] != gameInfo.activePlayer.pawn) {
		println("No ${gameInfo.activePlayer.color} pawn at ${input.substring(0, 2).lowercase()}")
		return false
	}
	return true
}

fun attemptEnPassantMove(gameInfo: GameInfo, coordinates: Array<Int>, direction: Int): Boolean {
	val (row1, col1, row2, col2) = coordinates
	var prevRow1 = 0
	var prevCol1 = 0
	var prevRow2 = 0
	val enemyPawn = if (gameInfo.activePlayer.color == Pawns.WHITE.color) Pawns.BLACK.pawn else Pawns.WHITE.pawn
	if (movesHistory.isNotEmpty()) {
		val moves = movesHistory.last()
		prevRow1 = moves[0]
		prevCol1 = moves[1]
		prevRow2 = moves[2]
	}

	if ((col1 in 1..6 && (board[row1][col1 + 1] == enemyPawn || board[row1][col1 - 1] == enemyPawn))
		|| (col1 == 0 && board[row1][col1 + 1] == enemyPawn) || (col1 == 7 && board[row1][col1 - 1] == enemyPawn)) {
		if ((gameInfo.activePlayer.color == Pawns.WHITE.color && row1 == 3 && (prevRow1 == 1 && prevRow2 == 3 && prevCol1 == col2))
			|| (gameInfo.activePlayer.color == Pawns.BLACK.color && row1 == 4 && (prevRow1 == 6 && prevRow2 == 4 && prevCol1 == col2))) {
			enPassant(gameInfo, coordinates, direction)
			return true
		}
	}

	println("Invalid Input")
	return false
}

fun enPassant(gameInfo: GameInfo, coordinates: Array<Int>, direction: Int) {
	val (row1, col1, row2, col2) = coordinates
	board[row1][col1] = Pawns.EMPTY.pawn
	board[row2 + 1 * direction][col2] = Pawns.EMPTY.pawn
	board[row2][col2] = gameInfo.activePlayer.pawn
	movesHistory.add(coordinates)
}

fun attemptCaptureMove(row2: Int, col2: Int, gameInfo: GameInfo, coordinates: Array<Int>): Boolean {
	if (gameInfo.activePlayer.color == Pawns.WHITE.color) {
		if (board[row2][col2] != Pawns.BLACK.pawn) {
			println("Invalid Input")
			return false
		} else {
			movePawn(coordinates, gameInfo)
			return true
		}
	} else if (board[row2][col2] != Pawns.WHITE.pawn) {
			println("Invalid Input")
			return false
		} else {
			movePawn(coordinates, gameInfo)
			return true
		}
}

fun attemptPawnMove(coordinates: Array<Int>, gameInfo: GameInfo, startRow: Int, direction: Int): Boolean {
	val (row1, col1, row2, col2) = coordinates
	// Check for valid move
	if (gameInfo.activePlayer.color == Pawns.WHITE.color) {
		if ((row1 - row2) !in 1..2 || (row1 - row2 == 2 && row1 != startRow)) {
			println("Invalid Input")
			return false
		}
	} else {
		if ((row2 - row1) !in 1..2 || (row2 - row1 == 2 && row1 != startRow)) {
			println("Invalid Input")
			return false
		}
	}
	// Check if the positions between starting and destination are empty for a 2-square move
	if (board[row1 - 1 * direction][col1] != Pawns.EMPTY.pawn) {
		println("Invalid Input")
		return false
	}
	// Check if the destination square is empty
	if (board[row2][col2] != Pawns.EMPTY.pawn) {
		println("Invalid Input")
		return false
	}
	movePawn(coordinates, gameInfo)
	return true
}

fun movePawn(coordinates: Array<Int>, gameInfo: GameInfo) {
	val (row1, col1, row2, col2) = coordinates
	board[row1][col1] = Pawns.EMPTY.pawn
	board[row2][col2] = gameInfo.activePlayer.pawn
	movesHistory.add(coordinates)
}

fun checkWinningCondition(): Boolean {
	var countWhite = 0
	var countBlack = 0

	// pawn moved to the last opposite rank
	for (col in boardRange) {
		if (board[0][col] == Pawns.WHITE.pawn) return true
		if (board[7][col] == Pawns.BLACK.pawn) return true
	}

	// all pawns are captured
	for (row in boardRange) {
		for (col in boardRange) {
			if (board[row][col] == Pawns.WHITE.pawn) countWhite++
			if (board[row][col] == Pawns.BLACK.pawn) countBlack++
		}
	}
	return countWhite == 0 || countBlack == 0
}

fun checkStalemateCondition(gameInfo: GameInfo): Boolean {
	var remainingPawns = 0
	var cantMovePawns = 0
	var cantCapturePawns = 0
	var cantEnPassantPawns = 0


	var prevRow1 = 0
	var prevCol1 = 0
	var prevRow2 = 0
	if (movesHistory.isNotEmpty()) {
		val moves = movesHistory.last()
		prevRow1 = moves[0]
		prevCol1 = moves[1]
		prevRow2 = moves[2]
	}
	val enemyPawn = if (gameInfo.activePlayer.color == Pawns.WHITE.color) Pawns.BLACK.pawn else Pawns.WHITE.pawn
	val direction = if (gameInfo.activePlayer.color == Pawns.WHITE.color) 1 else -1
	val enPassantRow = if (gameInfo.activePlayer.color == Pawns.WHITE.color) 3 else 4
	val requiredPrevRow1 = if (gameInfo.activePlayer.color == Pawns.WHITE.color) 1 else 6
	val requiredPrevRow2 = if (gameInfo.activePlayer.color == Pawns.WHITE.color) 3 else 4

	for (row in 1..6) {
		for (col in boardRange) {
			if (board[row][col] == gameInfo.activePlayer.pawn) {
				remainingPawns++

				if (board[row - 1 * direction][col] != Pawns.EMPTY.pawn) {
					cantMovePawns++
				}

				if ((col in 1..6 && board[row - 1 * direction][col - 1] != enemyPawn && board[row - 1 * direction][col + 1] != enemyPawn)
					|| (col == 0 && board[row - 1 * direction][col + 1] != enemyPawn)
					|| (col == 7 && board[row - 1 * direction][col - 1] != enemyPawn )) {
					cantCapturePawns++
				}

				if (((row == enPassantRow &&  col in 1..6)
					&& (board[row - 1 * direction][col - 1] != Pawns.EMPTY.pawn || board[row][col - 1] != enemyPawn || (prevRow1 != requiredPrevRow1 || prevRow2 != requiredPrevRow2 || prevCol1 != col - 1))
					&& (board[row - 1 * direction][col + 1] != Pawns.EMPTY.pawn || board[row][col + 1] != enemyPawn  || (prevRow1 != requiredPrevRow1 || prevRow2 != requiredPrevRow2 || prevCol1 == col + 1)))
					|| ((row == enPassantRow &&  col == 0) && (board[row - 1 * direction][col + 1] != Pawns.EMPTY.pawn || board[row][col + 1] != enemyPawn  || (prevRow1 != requiredPrevRow1 || prevRow2 != requiredPrevRow2 || prevCol1 == col + 1)))
					|| ((row == enPassantRow &&  col == 7) && (board[row - 1 * direction][col - 1] != Pawns.EMPTY.pawn || board[row][col - 1] != enemyPawn || (prevRow1 != requiredPrevRow1 || prevRow2 != requiredPrevRow2 || prevCol1 != col - 1)))) {
						cantEnPassantPawns++
				}
			}
		}
	}

	return remainingPawns == cantMovePawns && remainingPawns == cantCapturePawns && remainingPawns == cantEnPassantPawns
}
