package minesweeper

import kotlin.random.Random

class Coordinates(val x: Int, val y: Int, val option: String)

enum class Cell(val symbol: Char) {
	UNEXPLORED('.'),
	MARKED('*'),
	FREE('/'),
	MINE('X')
}

const val ROWS = 9
const val COLUMNS = 9
val range3 = -1..1
val range9 = 0 until 9
val playGrid: MutableList<MutableList<Char>> = mutableListOf()
var hiddenGrid: MutableList<MutableList<Char>> = mutableListOf()
var hintGrid: MutableList<MutableList<Char>> = mutableListOf()
var numOfMines = 0
var playing = true
var firstExplore = true
var surroundingCells: MutableList<MutableList<Int>> = mutableListOf()
var surroundingCellsNew: MutableList<MutableList<Int>> = mutableListOf()

fun main() {
	setPlayingMineField()
	getNumberOfMines()
	setMinesOnHiddenMineField()
	setNumbersOnHintGrid()
	printScreen(playGrid)
	while (playing) {
		val coordinates = inputMineCoordinates()
		processCoordinates(coordinates)
	}
}

// Create 9 x 9 grid with unexplored ('.') cells
// This grid will be displayed to players
fun setPlayingMineField() {
	for (row in range9) {
		playGrid.add(mutableListOf())
		for (col in range9) {
			playGrid[row].add(Cell.UNEXPLORED.symbol)
		}
	}
}

// Ask player to input how many mines he wants in a grid
fun getNumberOfMines() {
	print("\nHow many mines do you want on the field? ")
	numOfMines = readln().toInt()
}

// Create another 9x9 grid and place mines in it at random order
fun setMinesOnHiddenMineField() {
	hiddenGrid = playGrid.map { it.toMutableList() }.toMutableList()

	var i = 0
	while (i < numOfMines) {
		val row = Random.nextInt(ROWS)
		val col = Random.nextInt(COLUMNS)
		if (hiddenGrid[row][col] != Cell.MINE.symbol) {
			hiddenGrid[row][col] = Cell.MINE.symbol
			i++
		}
	}
}

// Create another 9x9 grid and place hint numbers in it
fun setNumbersOnHintGrid() {
	hintGrid = playGrid.map { it.toMutableList() }.toMutableList()

	for (row in range9) {
		for (col in range9) {
			checkMinesAround(row, col)
		}
	}
}

// Look for mines in all adjacent cells, count the number of mines and put them in a hint grid
fun checkMinesAround(row: Int, col: Int) {

	var countMines = 0
	for (i in range3) {
		for (j in range3) {
			if (row + i < 0 || col + j < 0) continue
			if (row + i >= ROWS || col + j >= COLUMNS) continue
			if (i == 0 && j == 0) continue
			if (hiddenGrid[row + i][col + j] == Cell.MINE.symbol) {
				countMines++
			}
		}
		if (countMines > 0 && hiddenGrid[row][col] != Cell.MINE.symbol) {
			hintGrid[row][col] = countMines.digitToChar()
		}
	}
}

// Print grid state on a screen
fun printScreen(grid: MutableList<MutableList<Char>>) {
	println("\n │123456789│")
	println("—│—————————│")
	for (i in 0 until ROWS) {
		println("${i + 1}|${grid[i].joinToString("")}|")
	}
	println("—│—————————│")
}

// Ask player to input coordinates and action he wants to take ('mine' or 'free')
fun inputMineCoordinates(): Coordinates {
	print("Set/unset mines marks or claim a cell as free: ")
	val (x, y, command) = readln().split(" ")
	return Coordinates(x.toInt(), y.toInt(), command)
}

// process game logic
// check the coordinates and option and take appropriate action
fun processCoordinates(coords: Coordinates) {
	val col = coords.x - 1
	val row = coords.y - 1
	val option = coords.option

	// Actions for marking grid cells
	if (option == "mine") {
		val cell = playGrid[row][col]
		when (cell) {
			Cell.FREE.symbol -> {
				println("Cell already claimed free. Please try again.")
				return
			}

			Cell.UNEXPLORED.symbol -> {
				playGrid[row][col] = Cell.MARKED.symbol
			}

			Cell.MARKED.symbol -> {
				playGrid[row][col] = Cell.UNEXPLORED.symbol
			}
		}
	}

	// Actions for claiming grid cells free
	if (option == "free") {

		do {
			val hiddenCell = hiddenGrid[row][col]
			val hintCell = hintGrid[row][col]
			if (hintCell.isDigit()) {
				playGrid[row][col] = hintGrid[row][col]
				firstExplore = false
			}
			// If the cell is empty with no mines around
			if (hiddenCell != Cell.MINE.symbol && hintCell == Cell.UNEXPLORED.symbol) {
				playGrid[row][col] = Cell.FREE.symbol
				firstExplore = false
				surroundingCells.add(mutableListOf(row, col))
				checkSurroundingCells()
			}
			if (hiddenCell == Cell.MINE.symbol && !firstExplore) {
				setGameLost()
				return
			}
			// The first cell explored with the free command cannot be a mine
			// so the game puts the mines in different positions if necessary
			if (hiddenCell == Cell.MINE.symbol && firstExplore) {
				setMinesOnHiddenMineField()
				setNumbersOnHintGrid()
			}
		} while (firstExplore)
	}
	printScreen(playGrid)
	checkWinner()
}

// If the cell is empty with no mines around, all the surrounding cells are explored automatically.
// If next to the explored cell there is another empty cell with no mines around,
// all the surrounding cells should be explored as well, until no more can be explored automatically.
fun checkSurroundingCells() {
	repeat (16) {
		surroundingCells.forEach { (row, col) -> processSurroundingCells(row, col) }

		if (surroundingCells != surroundingCellsNew) {
			val uniqueList = surroundingCellsNew.distinctBy { it.joinToString() }.toMutableList()
			surroundingCells = uniqueList.map { it.toMutableList() }.toMutableList()
		}
	}
}

fun processSurroundingCells(row: Int, col: Int) {
	for (i in range3) {
		for (j in range3) {
			if (row + i < 0 || col + j < 0) continue
			if (row + i >= ROWS || col + j >= COLUMNS) continue
			if (i == 0 && j == 0) continue
			if (hintGrid[row + i][col + j].isDigit() ) {
				playGrid[row + i][col + j] = hintGrid[row + i][col + j]
			}
			if (hintGrid[row + i][col + j] == Cell.UNEXPLORED.symbol) {
				playGrid[row + i][col + j] = Cell.FREE.symbol
				surroundingCellsNew.add(mutableListOf(row + i, col + j))
			}
		}
	}
}

// finish the game and reveal the position of all hidden mines
fun setGameLost() {
	for (row in range9) {
		for (col in range9) {
			if (hiddenGrid[row][col] == Cell.MINE.symbol) {
				playGrid[row][col] = Cell.MINE.symbol
			}
		}
	}
	printScreen(playGrid)
	println("You stepped on a mine and failed!")
	playing = false
}

// check if the winning conditions are complete
fun checkWinner() {
	var markedCount = 0
	var markedMineFoundCount = 0
	var unexploredCount = 0
	var unexploredMineFoundCount = 0

	for (row in range9) {
		for (col in range9) {
			if (playGrid[row][col] == Cell.MARKED.symbol) {
				markedCount++
			}
			if (playGrid[row][col] == Cell.MARKED.symbol && hiddenGrid[row][col] == Cell.MINE.symbol) {
				markedMineFoundCount++
			}
			if (playGrid[row][col] == Cell.UNEXPLORED.symbol) {
				unexploredCount++
			}
			if (playGrid[row][col] == Cell.UNEXPLORED.symbol && hiddenGrid[row][col] == Cell.MINE.symbol) {
				unexploredMineFoundCount++
			}
		}
	}
	if (markedMineFoundCount == numOfMines && markedCount == numOfMines) {
		youWon()
	}
	if (unexploredCount == numOfMines && unexploredMineFoundCount == numOfMines) {
		youWon()
	}
}

fun youWon() {
	println("Congratulations! You found all the mines!")
	playing = false
}