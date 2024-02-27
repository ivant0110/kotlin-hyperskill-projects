package search

import java.io.File

val MENU = """
    === Menu ===
    1. Find a person
    2. Print all people
    0. Exit""".trimIndent()

val invertedIndex: MutableMap<String, MutableList<Int>> = mutableMapOf()

fun main(args: Array<String>) {
	val argument = if (args.isNotEmpty()) args[0] else ""
	val filename = if (argument == "--data" && args.size > 1) args[1] else ""
	val people = File(filename).readLines().toMutableList()

	buildInvertedIndex(people)

	while (true) {
		println("\n$MENU")
		val action = readln()

		when (action) {
			"0" -> break
			"1" -> {
				println("Select a matching strategy: ALL, ANY, NONE")
				val strategy = readln()
				val strategyRegex = ("ALL|ANY|NONE").toRegex()
				if (!strategy.matches(strategyRegex)) {
					println("Invalid input")
					continue
				}
				findPerson(people, strategy)
			}
			"2" -> printAllPeople(people)
			else -> println("\nIncorrect option! Try again.")
		}
	}
	println("\nBye!")
}

fun buildInvertedIndex(people: List<String>) {
	val allWords = people.joinToString(" ").split("\\s+".toRegex())

	for (word in allWords) {
		val wordFoundInLineIndex: MutableList<Int> = mutableListOf()

		for ((index, line) in people.withIndex()) {
			if (line.lowercase().contains(word.lowercase())) {
				wordFoundInLineIndex.add(index)
			}
		}
		invertedIndex[word.lowercase()] = wordFoundInLineIndex
	}
}

fun findPerson(people: List<String>, strategy: String) {
	println("\nEnter a name or email to search all suitable people.")
	val searchWordsList = readln().split(" ")

	when (strategy) {
		"ALL" -> {
			val listOfPersonsFound = findAll(searchWordsList, people)
			printResult(listOfPersonsFound)
		}
		"ANY" -> {
			val listOfPersonsFound = findAny(searchWordsList, people )
			printResult(listOfPersonsFound)
		}
		"NONE" -> {
			val listOfPersonsFound = findAny(searchWordsList, people )
			val result = people.subtract(listOfPersonsFound.toSet()).toMutableList()
			printResult(result)
		}
	}
}

fun findAll(searchWordsList: List<String>, people: List<String>): MutableList<String> {
	val listOfPersonsFound: MutableList<String> =  mutableListOf()
	val listsOfIndicesFound: MutableList<MutableList<Int>> =  mutableListOf()

	if (searchWordsList.isNotEmpty()) {
		for (searchWord in searchWordsList) {
			if (invertedIndex.containsKey(searchWord.lowercase())) {
				val listOfIndices = invertedIndex[searchWord.lowercase()]
				listsOfIndicesFound.add(listOfIndices!!)
			}
		}
	}

	if (listsOfIndicesFound.isNotEmpty()) {
		var commonElements = listsOfIndicesFound[0]

		for (i in 1 until listsOfIndicesFound.size) {
			commonElements = commonElements.intersect(listsOfIndicesFound[i].toSet()).toMutableList()
		}

		for (index in commonElements) {
			listOfPersonsFound.add(people[index])
		}
	}
	return listOfPersonsFound
}

fun findAny(searchWordsList: List<String>, people: List<String>): MutableList<String> {
	val listOfPersonsFound: MutableList<String> =  mutableListOf()
	for (searchWord in searchWordsList) {
		if (invertedIndex.containsKey(searchWord.lowercase())) {
			val listOfIndices = invertedIndex[searchWord.lowercase()]
			for (index in listOfIndices!!) {
				listOfPersonsFound.add(people[index])
			}
		}
	}
	return listOfPersonsFound
}

fun printResult(listOfPersonsFound: MutableList<String>) {
	if (listOfPersonsFound.size == 1) {
		println("${listOfPersonsFound.size} person found:")
	} else if (listOfPersonsFound.size > 1) {
		println("${listOfPersonsFound.size} persons found:")
	} else {
		println("No matching people found.")
	}
	listOfPersonsFound.forEach { println(it) }
}

fun printAllPeople(people: MutableList<String>) {
	println("\n=== List of people ===")
	people.forEach { println(it) }
}
