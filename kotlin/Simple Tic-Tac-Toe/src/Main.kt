package tictactoe

const val PLAYER_X = 'X'
const val PLAYER_O = 'O'
const val EMPTY_CELL = '_'
val range = 1..3
var grid2D: MutableList<MutableList<Char>> = mutableListOf()
val playerHistory: MutableList<Char> = mutableListOf()
var activePlayer: Char = ' '
var playGame = true
var gameState = " "

fun main() {
	setInitialScreen()
	printScreen()

	while (playGame) {
		setActivePlayer()
		val newCoordinates = getCoordinates()
		updateGrid(newCoordinates)
		printScreen()

		val scoreX: Int = checkScore(PLAYER_X)
		val scoreO: Int = checkScore(PLAYER_O)
		val countEmpty: Int = countSymbol(EMPTY_CELL)
		val countX: Int = countSymbol(PLAYER_X)
		val countO: Int = countSymbol(PLAYER_O)
		gameState = checkGameState(scoreX, scoreO, countEmpty, countX, countO)
		if (gameState != " " && gameState != "Game not finished") {
			println(gameState)
		}
	}
}

fun setInitialScreen() {
	for (i in range) {
		grid2D += mutableListOf(
			mutableListOf(EMPTY_CELL, EMPTY_CELL, EMPTY_CELL),
		)
	}
}

fun printScreen() {
	val screen = """
        ---------
        | ${grid2D[0][0]} ${grid2D[0][1]} ${grid2D[0][2]} |
        | ${grid2D[1][0]} ${grid2D[1][1]} ${grid2D[1][2]} |
        | ${grid2D[2][0]} ${grid2D[2][1]} ${grid2D[2][2]} |
        ---------
    """.trimIndent()
	println(screen)
}

fun setActivePlayer() {
	if (playerHistory.size == 0) {
		activePlayer = PLAYER_X
		playerHistory.add(activePlayer)
		return
	}
	if (playerHistory.last() == PLAYER_X) {
		activePlayer = PLAYER_O
		playerHistory.add(activePlayer)
		return
	}
	if (playerHistory.last() == PLAYER_O) {
		activePlayer = PLAYER_X
		playerHistory.add(activePlayer)
		return
	}
}

fun getCoordinates(): List<Int> {
	var num1 = 0
	var num2 = 0
	var verified = false

	while (!verified) {
		val input = readln().split(" ")
		if (input.size != 2 || input[0].toIntOrNull() == null || input[1].toIntOrNull() == null) {
			println("You should enter numbers!")
			continue
		}
		if (input[0].toInt() !in range || input[1].toInt() !in range) {
			println("Coordinates should be from 1 to 3!")
			continue
		}
		if (grid2D[input[0].toInt() - 1][input[1].toInt() - 1] != EMPTY_CELL) {
			println("This cell is occupied! Choose another one!")
			continue
		}
		verified = true
		num1 = input[0].toInt()
		num2 = input[1].toInt()
	}
	return listOf(num1, num2)
}

fun updateGrid(coordinates: List<Int>) {
	grid2D[coordinates[0] - 1][coordinates[1] - 1] = activePlayer
}

fun checkScore(player: Char): Int {
	var score = 0
	for (i in 0..2) {
		if (grid2D[i][0] == player && grid2D[i][1] == player && grid2D[i][2] == player) {
			score++
		}
		if (grid2D[0][i] == player && grid2D[1][i] == player && grid2D[2][i] == player) {
			score++
		}
	}
	if (grid2D[0][0] == player && grid2D[1][1] == player && grid2D[2][2] == player) {
		score++
	}
	if (grid2D[0][2] == player && grid2D[1][1] == player && grid2D[2][0] == player) {
		score++
	}
	return score
}

fun countSymbol(symbol: Char): Int {
	var count = 0
	for (i in 0..2) {
		for (j in 0..2) {
			if (grid2D[i][j] == symbol) {
				count++
			}
		}
	}
	return count
}

fun checkGameState(score1: Int, score2: Int, countE: Int, countX: Int, countO: Int): String {
	var state = ""
	when {
		score1 == 1 && score2 == 1 -> state = "Impossible"
		countX - 1 > countO -> state = "Impossible"
		countO - 1 > countX -> state = "Impossible"
		score1 == 0 && score2 == 0 && countE > 0 -> state = "Game not finished"
		score1 == 0 && score2 == 0 && countE == 0 -> {
			state = "Draw"
			playGame = false
		}
		score1 > score2 -> {
			state = "X wins"
			playGame = false
		}
		score2 > score1 -> {
			state = "O wins"
			playGame = false
		}
	}
	return state
}