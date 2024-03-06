package bullscows
import kotlin.random.Random

const val SECRET_CODE_SYMBOLS = "0123456789abcdefghijklmnopqrstuvwxyz"
const val STAR = '*'
const val MAX_LENGTH = 36

fun main() {
    val digits = inputSecretCodeLength()
	if (digits == -1) return
	val symbols = inputNumberOfPossibleSymbols(digits)
	if (symbols == -1) return
	val secretCode = generateSecretCode(digits, symbols)

    println("Okay, let's start a game!")
    var turn = 0
    while (true) {
        turn++
        println("Turn $turn:")
        val guess = readln()
        var bullsCount = 0
        var cowsCount = 0

        for ((index, digit) in guess.withIndex()) {
            if (secretCode[index] == guess[index]) {
                bullsCount++
                continue
            }
            if (secretCode.contains(digit)) {
                cowsCount++
            }
        }

        var bulls = ""
        var cows = ""

        if (bullsCount == 1) bulls = "$bullsCount bull"
        if (bullsCount > 1) bulls = "$bullsCount bulls"
        if (cowsCount == 1) cows = "$cowsCount cow"
        if (cowsCount > 1) cows = "$cowsCount cows"

        val grader = if (cowsCount == 0 && bullsCount == 0) {
            "None"
        } else if (cowsCount == 0) {
            bulls
        } else if (bullsCount == 0) {
            cows
        } else {
            "$bulls and $cows"
        }

        println("Grade: $grader")
        if(bullsCount == secretCode.length) {
            println("Congratulations! You guessed the secret code.")
            break
        }
    }
}

fun inputSecretCodeLength(): Int {
    val digits: Int
		println("Input the length of the secret code:")
		val input = readln()
		val number = input.toIntOrNull()
	    if (number == null) {
			println("Error: $input isn't a valid number.")
		    return -1
		} else {
		    digits = number
		}
		if (digits == 0) {
			println("Error: minimum length of the secret code is 1.")
			return -1
		}
		if (digits > MAX_LENGTH) {
			println("Error: maximum length of the secret code is 36.")
			return -1
		}
    return digits
}

fun inputNumberOfPossibleSymbols(digits: Int): Int {
	println("Input the number of possible symbols in the code:")
	val symbols: Int = readln().toInt()
		if (symbols < digits) {
			println("Error: it's not possible to generate a code with a length of $digits with $symbols unique symbols.")
			return -1
		}
		if (symbols > MAX_LENGTH) {
			println("Error: maximum number of possible symbols in the code is 36 (0-9, a-z).")
			return -1
		}
	return symbols
}

fun generateSecretCode(digits: Int, symbols: Int): String {
	var secretCode = ""
	while (secretCode.length != digits) {
		val randomNum = Random.nextInt(symbols)
		val randomChar = SECRET_CODE_SYMBOLS[randomNum]
		if (!secretCode.contains(randomChar)) {
			secretCode += randomChar
		}
		if (secretCode.length == digits) break
	}
	val charactersUsed: String = if (symbols<=10) {
		"${SECRET_CODE_SYMBOLS[0]}-${SECRET_CODE_SYMBOLS[symbols-1]}"
	} else {
		"${SECRET_CODE_SYMBOLS[0]}-${SECRET_CODE_SYMBOLS[9]}, ${SECRET_CODE_SYMBOLS[10]}-${SECRET_CODE_SYMBOLS[symbols-1]}"
	}
	var hiddenCode = ""
	repeat(digits) {
		hiddenCode += STAR
	}
	println("The secret is prepared: $hiddenCode ($charactersUsed).")
	return secretCode
}