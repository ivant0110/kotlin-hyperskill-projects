package converter

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

val range = 2..36
var sourceBase = 0
var targetBase = 0
var wholeNumberToConvert = ""
var fractionalNumberToConvert = ""
var wholeNumberInDecimal = ""
var fractionalNumberInDecimal = ""
var resultWhole = ""
var resultFractional = ""
var finalResult = ""

fun main() {
	while (true) {
		println("Enter two numbers in format: {source base} {target base} (To quit type /exit) ")
		val input = readln()
		if (input == "/exit") break
		val inputVerified = verifyInput(input)
		if (inputVerified) convert()
	}
}

fun verifyInput(input: String): Boolean {
	if (input.split(" ").size != 2) {
		println("Wrong input\n")
		return false
	}
	val (source, target) = input.split(" ")

	if (!source.all { it.isDigit() } || !target.all { it.isDigit() }) {
		println("Wrong input\n")
		return false
	}
	sourceBase = source.toInt()
	targetBase = target.toInt()

	if (sourceBase !in range || targetBase !in range) {
		println("We cant convert this base\n")
		return false
	}
	return true
}

fun convert() {
	while (true) {
		print("Enter number in base $sourceBase to convert to base $targetBase (To go back type /back) ")
		val inputAction = readln()
		if (inputAction == "/back") {
			println()
			return
		}
		if (inputAction.split(" ").size != 1) {
			println("wrong input\n")
			return
		}

		if (inputAction.contains(".")) {
			val (whole, fractional) = inputAction.split(".")
			wholeNumberToConvert = whole
			fractionalNumberToConvert = fractional
			convertWholeNumber()
			convertFractionalNumber()
		} else {
			wholeNumberToConvert = inputAction
			convertWholeNumber()
		}

		println("Conversion result: $finalResult\n")
	}
}

fun convertWholeNumber() {
	if (sourceBase == 10 && targetBase == 10) {
		resultWhole = wholeNumberToConvert
	}

	if (sourceBase != 10 && targetBase == 10) {
		convertWholeToDecimal()
		resultWhole = wholeNumberInDecimal
	}

	if (sourceBase == 10 && targetBase != 10) {
		wholeNumberInDecimal = wholeNumberToConvert
		convertWholeFromDecimal()
	}

	if (sourceBase != 10 && targetBase != 10) {
		convertWholeToDecimal()
		convertWholeFromDecimal()
	}

	finalResult = resultWhole
}

fun convertFractionalNumber() {
	convertFractionalToDecimal()
	convertFractionalFromDecimal()
	finalResult = "$resultWhole.$resultFractional"
}

fun convertWholeToDecimal() {
	val numToDecimal = wholeNumberToConvert

	var sum = BigInteger("0")
	for (i in numToDecimal.indices) {
		val power = numToDecimal.length - 1 - i
		val tempChar = numToDecimal[i].uppercase()[0]
		var tempNum: String
		if (!tempChar.isDigit()) {
			val num = tempChar.code - 55
			tempNum = num.toString()
		} else {
			tempNum = tempChar.toString()
		}
		sum += tempNum.toBigInteger() * sourceBase.toBigInteger().pow(power)
	}
	wholeNumberInDecimal = sum.toString()
}

fun convertFractionalToDecimal() {
	val numToDecimal = fractionalNumberToConvert
	var sum = BigDecimal("0")

	for (i in numToDecimal.indices) {
		val tempChar = numToDecimal[i].uppercase()[0]
		var tempNum: String
		if (!tempChar.isDigit()) {
			val num = tempChar.code - 55
			tempNum = num.toString()
		} else {
			tempNum = tempChar.toString()
		}
		sum += tempNum.toBigDecimal().setScale(15, RoundingMode.CEILING) / sourceBase.toBigDecimal()
			.pow(i + 1)
	}
	fractionalNumberInDecimal = sum.toString()
}

fun convertWholeFromDecimal() {
	var bigNumber = wholeNumberInDecimal.toBigInteger()
	var reminder = ""
	val zero = BigInteger.ZERO
	val target = targetBase.toBigInteger()
	do {
		val temp: Int = (bigNumber % target).toInt()
		if (temp > 9) {
			val num = (temp + 55).toChar()
			reminder += num
		} else {
			reminder += temp
		}
		bigNumber /= target
	} while (bigNumber > zero)
	resultWhole = reminder.reversed()
}

fun convertFractionalFromDecimal() {
	var bigDecimal = fractionalNumberInDecimal.toBigDecimal()
	var reminder = ""
	val target = targetBase.toBigDecimal()
	repeat(fractionalNumberInDecimal.length) {
		val temp: Int = (bigDecimal * target).toInt()
		if (temp > 9) {
			val num = (temp + 55).toChar()
			reminder += num
		} else {
			reminder += temp
		}
		bigDecimal = bigDecimal * target - temp.toBigDecimal()
	}
	resultFractional = reminder.substring(0, 5)
}
