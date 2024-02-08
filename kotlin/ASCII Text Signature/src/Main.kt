package signature

import java.io.File
import java.io.IOException

const val BORDER = "8"
val emptyStringRoman = List(10) { "          " }
val emptyStringMedium = List(3) { "     " }
val romanFont: MutableMap<Char, List<String>> = mutableMapOf(' ' to emptyStringRoman)
val mediumFont: MutableMap<Char, List<String>> = mutableMapOf(' ' to emptyStringMedium)
val rows = Array(15) { "" }
var paddingLeft = ""
var paddingRight = ""
var fullName = ""
var status = ""

fun main() {
	processTextFiles()
	getInfoFromUSer()
	buildName()
	buildStatus()
	buildPadding()
	addPaddingAndSideBorders()
	addTopAndBottomBorder()
	printResult()
}

fun processTextFiles() {
	try {
		val path = System.getProperty("user.dir")
		val mediumFile = File("$path/src/medium.txt")
		val importMedium = mediumFile.readLines()
		val romanFile = File("$path/src/roman.txt")
		val importRoman = romanFile.readLines()

		buildFontMap(mediumFont, importMedium)
		buildFontMap(romanFont, importRoman)
	} catch (e: IOException) {
		println(e.message)
	}
}

fun buildFontMap(fontName: MutableMap<Char, List<String>>, unprocessedList: List<String>) {

	val (fontHeight, totalFonts) = unprocessedList[0].split(" ").map { it.toInt() }
	val steps = (unprocessedList.size - 1) / totalFonts
	val newList = unprocessedList.subList(1, unprocessedList.size).toMutableList()

	for (i in 0..newList.lastIndex step steps) {
		val char = newList[i].split(" ")[0].first()

		val subList = mutableListOf<String>()
		for (j in 1..fontHeight) {
			subList.add(newList[i + j])
		}
		fontName[char] = subList
	}
}

fun getInfoFromUSer() {
	println("Enter name and surname:")
	fullName = readln()
	println("Enter person's status:")
	status = readln()
}

fun buildName() {
	for (char in fullName) {
		for (i in 0..9) {
			rows[i + 1] += "${romanFont[char]?.get(i)}"
		}
	}
	for (i in 0..9) {
		rows[i + 1] = rows[i + 1].substring(0, rows[i + 1].lastIndex)
	}
}

fun buildStatus() {
	for (char in status) {
		for (i in 0..2) {
			rows[i + 11] += "${mediumFont[char]?.get(i)}"
		}
	}
	for (i in 0..2) {
		rows[i + 11] = rows[i + 11].substring(0, rows[i + 11].lastIndex)
	}
}

fun addTopAndBottomBorder() {
	repeat(rows[2].length ) { rows[0] = rows[0] + BORDER }
	rows[14] = rows[0]
}

fun buildPadding() {
	var long = rows[1].length
	var short = rows[11].length
	if (rows[1].length < rows[11].length) {
		long = rows[11].length
		short = rows[1].length
	}
	repeat((long - short) / 2) { paddingLeft += " " }

	if ((long % 2 != 0 && short % 2 != 0) || (long % 2 == 0 && short % 2 == 0)) {
		paddingRight = paddingLeft
	}
	if ((long % 2 != 0 && short % 2 == 0) || (long % 2 == 0 && short % 2 != 0)) {
		paddingRight = paddingLeft + " "
	}
}

fun addPaddingAndSideBorders() {
	if (rows[1].length < rows[11].length) {
		for (i in 0..9) {
			rows[i + 1] = "$BORDER$BORDER  $paddingLeft${rows[i + 1]}$paddingRight   $BORDER$BORDER"
		}
		for (i in 0..2) {
			rows[i + 11]  = "$BORDER$BORDER  ${rows[i + 11]}   $BORDER$BORDER"
		}
	} else {
		for (i in 0..9) {
			rows[i + 1] = "$BORDER$BORDER  ${rows[i + 1]}   $BORDER$BORDER"
		}
		for (i in 0..2) {
			rows[i + 11]  = "$BORDER$BORDER  $paddingLeft${rows[i + 11]}$paddingRight   $BORDER$BORDER"
		}
	}
}

fun printResult() {
	rows.forEach { println(it) }
}