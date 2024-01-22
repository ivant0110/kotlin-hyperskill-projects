package cinema

const val EMPTY_SEAT = 'S'
const val BOOKED_SEAT = 'B'
const val PRICE_FRONT = 10
const val PRICE_BACK = 8
var rows = 0
var seats = 0
val seatingArrangements: MutableList<MutableList<Char>> = mutableListOf(mutableListOf(' '))
var ticketPrice = 0
var purchasedTickets = 0
var income = 0

fun main() {
	getRowsAndSeats()
	setInitialScreen()
	getMenu()
}

// game initialization
fun getRowsAndSeats() {
	println("Enter the number of rows:")
	rows = readln().toInt()
	println("Enter the number of seats in each row:")
	seats = readln().toInt()
	if (rows !in 1..9 || seats !in 1..9) {
		println("Wrong input, please try again!")
		return
	}
}

fun setInitialScreen() {
	for (i in 1..seats) {
		seatingArrangements[0].add(i.digitToChar())
	}
	for (j in 1..rows) {
		seatingArrangements.add(mutableListOf(j.digitToChar()))
	}
	for (k in 1..seats) {
		for (l in 1..rows) {
			seatingArrangements[l].add(EMPTY_SEAT)
		}
	}
}

// game menu
fun getMenu() {
	var gameState = ""

	while (gameState != "0") {
		println()
		println(
			"""
			1. Show the seats
			2. Buy a ticket
			3. Statistics
			0. Exit
		""".trimIndent()
		)
		gameState = readln()

		when (gameState) {
			"1" -> printScreen()
			"2" -> buyTicket()
			"3" -> showStatistics()
		}
	}
}

// 1. Show the seats
fun printScreen() {
	println("\nCinema:")
	seatingArrangements.forEach { println(it.joinToString(" ")) }
}

// 2. Buy a ticket
fun buyTicket() {
	var seatAvailable = false
	while (!seatAvailable) {
		val coordinates = getCoordinates()
		if (coordinates[0] !in 1..rows || coordinates[1] !in 1..seats) {
			println("Wrong input!")
			continue
		}
		if (seatingArrangements[coordinates[0]][coordinates[1]] != BOOKED_SEAT) {
			calculatePrice(coordinates)
			markSeatBooked(coordinates)
			income += ticketPrice
			purchasedTickets++
			seatAvailable = true
		} else {
			println("\nThat ticket has already been purchased!")
		}
	}
}

fun getCoordinates(): List<Int> {
	println("\nEnter a row number:")
	val rowNumber = readln().toInt()
	println("Enter a seat number in that row:")
	val seatNumber = readln().toInt()
	return listOf(rowNumber, seatNumber)
}

fun calculatePrice(coordinates: List<Int>) {
	if (rows * seats <= 60) {
		ticketPrice = PRICE_FRONT
	}
	if (rows * seats > 60) {
		ticketPrice = if (coordinates[0] <= rows / 2) PRICE_FRONT else PRICE_BACK
	}
	println("Ticket price: $$ticketPrice")
}

fun markSeatBooked(coordinates: List<Int>) {
	seatingArrangements[coordinates[0]][coordinates[1]] = BOOKED_SEAT
}

// 3. Statistics
fun showStatistics() {
	val percentageSold: Double = (purchasedTickets.toDouble() / (rows.toDouble() * seats.toDouble())) * 100
	val formatPercentage = "%.2f".format(percentageSold)
	val totalIncome = calculateTotalIncome()

	println("\nNumber of purchased tickets: $purchasedTickets")
	println("Percentage: $formatPercentage%")
	println("Current income: $$income")
	println("Total income: $$totalIncome")
}

fun calculateTotalIncome(): Int {
	return if (rows * seats <= 60) {
		rows * seats * PRICE_FRONT
	} else {
		seats * (PRICE_FRONT * (rows / 2) + PRICE_BACK * (rows - (rows / 2)))
	}
}