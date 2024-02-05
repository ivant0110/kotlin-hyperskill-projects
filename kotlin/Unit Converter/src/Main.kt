package converter

enum class Unit(val symbols: List<String>, val conversionRate: Double, val type: String) {
	METER(listOf("m", "meter", "meters"), 1.0, "length"),
	KILOMETER(listOf("km", "kilometer", "kilometers"), 1000.0, "length"),
	CENTIMETER(listOf("cm", "centimeter", "centimeters"), 0.01, "length"),
	MILLIMETER(listOf("mm", "millimeter", "millimeters"), 0.001, "length"),
	MILE(listOf("mi", "mile", "miles"), 1609.35, "length"),
	YARD(listOf("yd", "yard", "yards"), 0.9144, "length"),
	FOOT(listOf("ft", "foot", "feet"), 0.3048, "length"),
	INCH(listOf("in", "inch", "inches"), 0.0254, "length"),
	GRAM(listOf("g", "gram", "grams"), 1.0, "weight"),
	KILOGRAM(listOf("kg", "kilogram", "kilograms"), 1000.0, "weight"),
	MILLIGRAM(listOf("mg", "milligram", "milligrams"), 0.001, "weight"),
	POUND(listOf("lb", "pound", "pounds"), 453.592, "weight"),
	OUNCE(listOf("oz", "ounce", "ounces"), 28.3495, "weight"),
	CELSIUS(listOf("celsius", "degree Celsius", "degrees Celsius", "c", "dc"), 0.0, "temperature"),
	FAHRENHEIT(listOf("fahrenheit", "degree Fahrenheit", "degrees Fahrenheit", "f", "df"), 0.0, "temperature"),
	KELVIN(listOf("k", "kelvin", "kelvins"), 0.0, "temperature");

	val singular: String
		get() = symbols[1]

	val plural: String
		get() = symbols[2]
}

var convertUnit = true
var inputValue: Double = 0.0
var inputUnit = ""
var resultUnit = ""

fun main() {
	while (convertUnit) {
		val inputParsed = inputData()
		if(!inputParsed) continue
		if (!convertUnit) break
		val initialUnit = Unit.values().find { inputUnit in it.symbols }
		val finalUnit = Unit.values().find { resultUnit in it.symbols }

		if (initialUnit != null && finalUnit != null && initialUnit.type == finalUnit.type) {
			if (initialUnit.type != "temperature") {
				if (inputValue < 0) {
					println("${initialUnit.type} shouldn't be negative")
					continue
				}
				val intermediateValue = convertToIntermediateValue(initialUnit)
				convertToTargetUnit(intermediateValue, initialUnit, finalUnit)
			} else {
				convertTemperature(initialUnit, finalUnit)
			}
		} else {
			val unitOne = if (initialUnit == null) "???" else initialUnit.plural
			val unitTwo = if (finalUnit == null) "???" else finalUnit.plural
			println("Conversion from $unitOne to $unitTwo is impossible")
			println()
		}
	}
}

fun inputData(): Boolean {
	print("Enter what you want to convert (or exit): ")
	try {
		val input = readln().trim()
		if (input.lowercase() == "exit") {
			convertUnit = false
		} else {
			val inputList = input.split(" ")

			inputValue = inputList[0].toDouble()

			if (inputList.size == 4) {
				inputUnit = inputList[1].lowercase()
				resultUnit = inputList[3].lowercase()
			}
			if (inputList.size == 5) {
				if (inputList[1].lowercase() == "degree" || inputList[1].lowercase() == "degrees") {
					inputUnit = "${inputList[1].lowercase()} ${inputList[2].lowercase().capitalize()}"
					resultUnit = inputList[4].lowercase()
				} else {
					inputUnit = inputList[1].lowercase()
					resultUnit = "${inputList[3].lowercase()} ${inputList[4].lowercase().capitalize()}"
				}
			}
			if (inputList.size >= 6) {
				inputUnit = "${inputList[1].lowercase()} ${inputList[2].lowercase().capitalize()}"
				resultUnit = "${inputList[4].lowercase()} ${inputList[5].lowercase().capitalize()}"
			}
		}
		return true
	} catch (e: Exception) {
		println("Parse error")
		println()
		return false
	}
}

fun convertToIntermediateValue(initUnit: Unit): Double {
	val intermediateValue = inputValue * initUnit.conversionRate
	return intermediateValue
}

fun convertToTargetUnit(intermediate: Double, initUnit: Unit, targetUnit: Unit) {
	val convertedDistance = intermediate / targetUnit.conversionRate
	println("$inputValue ${if (inputValue == 1.0) initUnit.singular else initUnit.plural} is $convertedDistance ${if (convertedDistance == 1.0) targetUnit.singular else targetUnit.plural}")
	println()
}

fun convertTemperature(initUnit: Unit, targetUnit: Unit) {
	var result = 0.0

	if (initUnit.name == "CELSIUS" && targetUnit.name == "FAHRENHEIT") {
		result = inputValue * (9.0 / 5.0) + 32
	}
	if (initUnit.name == "FAHRENHEIT" && targetUnit.name == "CELSIUS") {
		result = (inputValue - 32) * (5.0 / 9.0)
	}
	if (initUnit.name == "KELVIN" && targetUnit.name == "CELSIUS") {
		result = inputValue - 273.15
	}
	if (initUnit.name == "CELSIUS" && targetUnit.name == "KELVIN") {
		result = inputValue + 273.15
	}
	if (initUnit.name == "FAHRENHEIT" && targetUnit.name == "KELVIN") {
		result = (inputValue + 459.67) * (5.0 / 9.0)
	}
	if (initUnit.name == "KELVIN" && targetUnit.name == "FAHRENHEIT") {
		result = inputValue * (9.0 / 5.0) - 459.67
	}
	if (initUnit.name ==  targetUnit.name ) {
		result = inputValue
	}

	println("$inputValue ${if (inputValue == 1.0) initUnit.singular else initUnit.plural} is $result ${if (result == 1.0) targetUnit.singular else targetUnit.plural}")
	println()
}