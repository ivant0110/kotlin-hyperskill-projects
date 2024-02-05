package encryptdecrypt

import java.io.File

var mode = ""
var key = 0
var out = ""
var data = ""
var result = ""
var alg = ""

fun main(args: Array<String>) {

	try {
		getInputData(args)
		when (mode) {
			"enc" -> {
				result = encryptMessage(data, key, alg)
			}

			"dec" -> {
				result = decryptMessage(data, key, alg)
			}

			else -> println("Unknown operation")
		}
		output()

	} catch (e: Exception) {
		println(e.message)
	}
}

fun getInputData(args: Array<String>) {
	val argsMap = mutableMapOf<String, String>()

	for (i in 0..args.size step 2) {
		if (i + 1 < args.size) {
			argsMap[args[i]] = args[i + 1]
		}
	}

	mode = if (argsMap["-mode"] != null) argsMap["-mode"].toString() else "enc"
	key = if (argsMap["-key"] != null) argsMap["-key"]!!.toInt() else 0
	out = if (argsMap["-out"] != null) argsMap["-out"].toString() else "standard"
	alg = if (argsMap["-alg"] != null) argsMap["-alg"].toString() else "shift"

	if (argsMap["-data"] == null && argsMap["-in"] == null) {
		data = ""
	}
	if ((argsMap["-data"] == null && argsMap["-in"] != null)) {
		val file = File(argsMap["-in"].toString())
		data = file.readText()
	}
	if ((argsMap["-data"] != null && argsMap["-in"] == null)) {
		data = argsMap["-data"].toString()
	}
	if ((argsMap["-data"] != null && argsMap["-in"] != null)) {
		data = argsMap["-data"].toString()
	}
}

fun encryptMessage(message: String, number: Int, algorithm: String): String {
	var encrypted = ""

	if (algorithm == "unicode") {
		for (char in message) {
			encrypted += char + number
		}
	} else {
		val regexLowercase = Regex("[a-z]")
		val regexUppercase = Regex("[A-Z]")
		for (char in message) {
			if (char.toString().matches(regexLowercase)) {
				if ((char.code + number) > 122) {
					encrypted += (96 + ((char.code + number) % 122)).toChar()
				} else {
					encrypted += (char + number)
				}
			} else if (char.toString().matches(regexUppercase)) {
				if ((char.code + number) > 90) {
					encrypted += (64 + ((char.code + number) % 90)).toChar()
				} else {
					encrypted += (char + number)
				}
			} else {
				encrypted += char
			}
		}
	}
	return encrypted
}

fun decryptMessage(message: String, number: Int, algorithm: String): String {
	var encrypted = ""
	if (algorithm == "unicode") {

		for (char in message) {
			encrypted += char - number
		}

	} else {
		val regexLowercase = Regex("[a-z]")
		val regexUppercase = Regex("[A-Z]")
		for (char in message) {
			if (char.toString().matches(regexLowercase)) {
				if ((char.code - number) < 97) {
					encrypted += (123 - (number - (char.code - 97))).toChar()
				} else {
					encrypted += (char - number)
				}
			} else if (char.toString().matches(regexUppercase)) {
				if ((char.code - number) < 65) {
					encrypted += (91 - (number - (char.code - 65))).toChar()
				} else {
					encrypted += (char - number)
				}
			} else {
				encrypted += char
			}
		}
	}
	return encrypted
}

fun output() {
	if (out == "standard") println(result)
	else {
		val file = File(out)
		file.writeText(result)
	}
}
