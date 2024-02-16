package connectfour

enum class GameState {
	PLAYING, WON, DRAW, OVER
}

data class Player(val name: String, val disk: Char)
data class GameInfo(
	var rows: Int = 0,
	var columns: Int = 0,
	var activePlayerIndex: Int = 0,
	var gameState: GameState = GameState.PLAYING,
	var numberOfGames: Int = 1,
	var multipleGames: Boolean = false
)

private const val DEFAULT_BOARD = "6 x 7"
val playerNames = mutableListOf("", "")
var board: MutableList<MutableList<Char>> = mutableListOf()

val scores = mutableMapOf<String, Int>()
var gameInfo = GameInfo()
val emptyPlayer = Player("", ' ')

fun main() {
	getPlayerNames()
	val playerOne = Player(playerNames[0], 'o')
	val playerTwo = Player(playerNames[1], '*')
	getBoardInfo()
	getNumberOfGames()
	printInfo(playerOne.name, playerTwo.name)
	createBoard()
	if (gameInfo.multipleGames) {
		scores[playerOne.name] = 0
		scores[playerTwo.name] = 0
		for (i in 1..gameInfo.numberOfGames) {
			if(gameInfo.gameState == GameState.OVER) break
			if (i % 2 == 0) gameInfo.activePlayerIndex = 1 else gameInfo.activePlayerIndex = 0
			println("Game #$i")
			playGame(playerOne, playerTwo)
			if (gameInfo.gameState != GameState.OVER) {
				printScore(playerOne, playerTwo)
				board.clear()
				createBoard()
				gameInfo.gameState = GameState.PLAYING
			}
		}
		gameInfo.gameState = GameState.OVER
	} else {
		playGame(playerOne, playerTwo)
		gameInfo.gameState = GameState.OVER
	}
	if (gameInfo.gameState == GameState.OVER) println("Game over!")
}

fun getPlayerNames() {
	println("Connect Four")
	println("First player's name:")
	playerNames[0] = readln()
	println("Second player's name:")
	playerNames[1] = readln()
}

fun getBoardInfo() {
	while (true) {
		println("Set the board dimensions (Rows x Columns)")
		println("Press Enter for default (6 x 7)")
		var inputBoard = readln()
		if (inputBoard == "") inputBoard = DEFAULT_BOARD

		val regexInput = "\\s*[0-9]+\\s*[xX]\\s*[0-9]+\\s*".toRegex()
		val inputValid = regexInput.matches(inputBoard)
		if (!inputValid) {
			println("Invalid input")
			continue
		}

		val (rows, cols) = inputBoard.split(Regex("[xX]")).map { it.trim() }

		val regexRange = "[5-9]".toRegex()
		val rowValid = regexRange.matches(rows)
		if (!rowValid) {
			println("Board rows should be from 5 to 9")
			continue
		}
		val columnValid = regexRange.matches(cols)
		if (!columnValid) {
			println("Board columns should be from 5 to 9")
			continue
		}
		gameInfo.rows = rows.toInt()
		gameInfo.columns = cols.toInt()
		break
	}
}

fun getNumberOfGames() {
	while (true) {
		println("Do you want to play single or multiple games?")
		println("For a single game, input 1 or press Enter")
		println("Input a number of games:")
		val numberOfGames = readln().trim()
		if (numberOfGames == "") break
		if ((numberOfGames.all { it.isDigit() } && numberOfGames.toInt() == 0) || !numberOfGames.all { it.isDigit() }) {
			println("Invalid input")
			continue
		}
		if (numberOfGames.all { it.isDigit() } && numberOfGames.toInt() > 1) {
			gameInfo.numberOfGames = numberOfGames.toInt()
			gameInfo.multipleGames = true
		}
		break
	}
}

fun printInfo(playerOne: String, playerTwo: String) {
	println("$playerOne VS $playerTwo")
	println("${gameInfo.rows} X ${gameInfo.columns} board")
	if (gameInfo.multipleGames) {
		println("Total ${gameInfo.numberOfGames} games")
	} else {
		println("Single game")
	}
}

fun printScore(playerOne: Player, playerTwo: Player) {
	println("Score")
	println("${playerOne.name}: ${scores[playerOne.name]} ${playerTwo.name}: ${scores[playerTwo.name]}")
}

fun createBoard() {
	for (row in 0 until gameInfo.rows) {
		board.add(mutableListOf())
		for (col in 0 until gameInfo.columns) {
			board[row].add(emptyPlayer.disk)
		}
	}
}

fun printBoard() {
	var title = ""
	for (col in 1..gameInfo.columns) {
		title += " $col"
	}

	var rowOutput = ""
	for (row in 0 until gameInfo.rows) {
		for (col in 0 until gameInfo.columns) {
			rowOutput += "║${board[row][col]}"
		}
		rowOutput += "║\n"
	}
	rowOutput = rowOutput.substring(0, rowOutput.length - 1)

	var footer = "╚═"
	for (col in 2..gameInfo.columns) {
		footer += "╩═"
	}
	footer += "╝"

	println(title)
	println(rowOutput)
	println(footer)
}

fun playGame(playerOne: Player, playerTwo: Player) {
	printBoard()
	while (gameInfo.gameState == GameState.PLAYING) {
		println("${playerNames[gameInfo.activePlayerIndex]}'s turn:")
		val inputDiskColumn = readln()
		if (inputDiskColumn == "end") {
			gameInfo.gameState = GameState.OVER
			break
		}
		if (!inputDiskColumn.all { it.isDigit() }) {
			println("Incorrect column number")
			continue
		}
		if (inputDiskColumn.toInt() !in 1..gameInfo.columns) {
			println("The column number is out of range (1 - ${gameInfo.columns})")
			continue
		}
		val diskColumn = inputDiskColumn.toInt()
		var disk = emptyPlayer.disk
		if (gameInfo.activePlayerIndex == 0) {
			disk = playerOne.disk
		} else if (gameInfo.activePlayerIndex == 1) {
			disk = playerTwo.disk
		}
		val diskAdded = addDiskToBoard(diskColumn, disk)
		if (!diskAdded) {
			println("Column $diskColumn is full")
			continue
		}
		printBoard()
		gameInfo.gameState = checkWinningCondition(disk)
		if (gameInfo.gameState == GameState.WON) {
			println("Player ${playerNames[gameInfo.activePlayerIndex]} won")
			scores[playerNames[gameInfo.activePlayerIndex]] = scores[playerNames[gameInfo.activePlayerIndex]]!! + 2
			break
		}
		if (gameInfo.gameState == GameState.DRAW) {
			println("It is a draw")
			scores[playerOne.name] = scores[playerOne.name]!! + 1
			scores[playerTwo.name] = scores[playerTwo.name]!! + 1
			break
		}

		gameInfo.activePlayerIndex = when (gameInfo.activePlayerIndex) {
			0 -> 1
			else -> 0
		}
	}
}

fun addDiskToBoard(diskCol: Int, disk: Char): Boolean {
	for (i in board.lastIndex downTo 0) {
		if (board[i][diskCol - 1] == emptyPlayer.disk) {
			board[i][diskCol - 1] = disk
			return true
		}
	}
	return false
}

fun checkWinningCondition(disk: Char): GameState {
	// check horizontal
	for (r in 0 until gameInfo.rows) {
		for (c in 0..(gameInfo.columns - 4)) {
			if (board[r][c] == disk && board[r][c + 1] == disk && board[r][c + 2] == disk && board[r][c + 3] == disk) {
				return GameState.WON
			}
		}
	}
	// check vertical
	for (r in 0..(gameInfo.rows - 4)) {
		for (c in 0 until gameInfo.columns) {
			if (board[r][c] == disk && board[r + 1][c] == disk && board[r + 2][c] == disk && board[r + 3][c] == disk) {
				return GameState.WON
			}
		}
	}
	// check main diagonal
	for (r in 0..gameInfo.rows - 4) {
		for (c in 0..(gameInfo.columns - 4)) {
			if (board[r][c] == disk && board[r + 1][c + 1] == disk && board[r + 2][c + 2] == disk && board[r + 3][c + 3] == disk) {
				return GameState.WON
			}
		}
	}
	// check anti diagonal
	for (r in 0..gameInfo.rows - 4) {
		for (c in (gameInfo.columns - 1) downTo 3) {
			if (board[r][c] == disk && board[r + 1][c - 1] == disk && board[r + 2][c - 2] == disk && board[r + 3][c - 3] == disk) {
				return GameState.WON
			}
		}
	}
	// check for draw
	var countEmpty = 0
	for (r in 0 until gameInfo.rows) {
		for (c in 0 until gameInfo.columns) {
			if (board[r][c] == emptyPlayer.disk) {
				countEmpty++
			}
		}
	}
	if (countEmpty == 0) return GameState.DRAW

	return GameState.PLAYING
}
