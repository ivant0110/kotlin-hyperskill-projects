package parking

enum class ParkingSpotState(val status: String) {
	OCCUPIED("occupied"),
	FREE("free")
}
data class ParkingSpot(
	var spotNumber: Int,
	var status: String = ParkingSpotState.FREE.status,
	var registrationNumber: String = "",
	var carColor: String = ""
)
val parkingSpotList = mutableListOf<ParkingSpot>()

fun main() {
	while (true) {
		val input = readln().trim().split(" ")
		if (input[0] == "exit") break
		if (input[0] != "create" && parkingSpotList.size == 0) {
			println("Sorry, a parking lot has not been created.")
			continue
		}
		when (input[0]) {
			"create" -> createParkingLot(input[1].toInt())
			"park" -> park(input[1], input[2])
			"leave" -> leave(input[1].toInt())
			"status" -> printStatus()
			"reg_by_color" -> printRegsByColor(input[1])
			"spot_by_color" -> printSpotsByColor(input[1])
			"spot_by_reg" -> printSpotByReg(input[1])
			else -> println("Invalid input")
		}
	}
}

fun createParkingLot(spots: Int) {
	if (parkingSpotList.size > 0) parkingSpotList.clear()
	for (i in 1..spots) {
		val spot = ParkingSpot(i)
		parkingSpotList.add(spot)
	}
	println("Created a parking lot with ${parkingSpotList.size} spots.")
}

fun printStatus() {
	var carsFound = false
	for ((index, value) in parkingSpotList.withIndex()) {
		if (value.status == ParkingSpotState.OCCUPIED.status) {
			println("${index + 1} ${value.registrationNumber} ${value.carColor}")
			carsFound = true
		}
	}
	if (!carsFound) {
		println("Parking lot is empty.")
	}
}

fun park(regNumber: String, color: String) {
	val spot = findFirstEmptySpot()
	if (spot == -1) {
		println("Sorry, the parking lot is full.")
	} else {
		parkingSpotList[spot - 1].status = ParkingSpotState.OCCUPIED.status
		parkingSpotList[spot - 1].registrationNumber = regNumber
		parkingSpotList[spot - 1].carColor = color
		println("${parkingSpotList[spot - 1].carColor} car parked in spot $spot.")
	}
}

fun findFirstEmptySpot(): Int {
	for (i in 0..parkingSpotList.lastIndex) {
		if (parkingSpotList[i].status == ParkingSpotState.FREE.status) {
			return parkingSpotList[i].spotNumber
		}
	}
	return -1
}

fun leave(spot: Int) {
	if (parkingSpotList[spot - 1].status == ParkingSpotState.OCCUPIED.status) {
		parkingSpotList[spot - 1].status = ParkingSpotState.FREE.status
		parkingSpotList[spot - 1].registrationNumber = ""
		parkingSpotList[spot - 1].carColor = ""
		println("Spot $spot is ${parkingSpotList[spot - 1].status}.")
	} else {
		println("There is no car in spot $spot.")
	}
}

fun printRegsByColor(color: String) {
	val filteredByColor = parkingSpotList.filter { it.carColor.lowercase() == color.lowercase() }

	if (filteredByColor.isEmpty()) {
		println("No cars with color $color were found.")
	} else {
		val outputRegNumber = filteredByColor.map { it.registrationNumber }
		println(outputRegNumber.joinToString())
	}
}

fun printSpotsByColor(color: String) {
	val filteredByColor = parkingSpotList.filter { it.carColor.lowercase() == color.lowercase() }
	if (filteredByColor.isEmpty()) {
		println("No cars with color $color were found.")
	} else {
		val outputSpotNumber = filteredByColor.map { it.spotNumber }
		println(outputSpotNumber.joinToString())
	}
}

fun printSpotByReg(regNumber: String) {
	val filteredByRegNum = parkingSpotList.filter { it.registrationNumber.lowercase() == regNumber.lowercase() }
	if (filteredByRegNum.isEmpty()) {
		println("No cars with registration number $regNumber were found.")
	} else {
		val outputSpotNumber = filteredByRegNum.map { it.spotNumber }
		println(outputSpotNumber.joinToString())
	}
}