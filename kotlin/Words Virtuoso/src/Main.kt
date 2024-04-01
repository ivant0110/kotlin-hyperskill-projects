package wordsvirtuoso

import java.io.File
import kotlin.random.Random

val dictionary = mutableListOf<String>()
val cluesList = mutableListOf<String>()
val wrongCharacters = mutableSetOf<Char>()

fun main(args: Array<String>) {
    val candidateWords = initializeGame(args)
    val secretWord = pickSecretWord(candidateWords)
    playGame(secretWord)
}

fun initializeGame(args: Array<String>): List<String> {
    if (args.size != 2) {
        println("Error: Wrong number of arguments.")
        kotlin.system.exitProcess(0)
    }
    val (fileNameAllWords, fileNameCandidateWords) = args

    val fileAllWords = File(fileNameAllWords)
    val fileCandidateWords = File(fileNameCandidateWords)
    checkIfFilesExist(fileAllWords, fileCandidateWords)

    val listOfAllWords = fileAllWords.readLines().map { it.lowercase() }
    dictionary.addAll(listOfAllWords)
    val listOfCandidateWords = fileCandidateWords.readLines().map { it.lowercase() }

    val allWordsValidated = validateWords(listOfAllWords, fileNameAllWords)
    val candidateWordsValidated = validateWords(listOfCandidateWords, fileNameCandidateWords)

    val notIncludedWords = checkIfCandidateWordsAreInDictionary(listOfCandidateWords, listOfAllWords)

    if (notIncludedWords > 0) {
        println("Error: $notIncludedWords candidate words are not included in the $fileNameAllWords file.")
        kotlin.system.exitProcess(0)
    }

    if (allWordsValidated && candidateWordsValidated) {
        println("Words Virtuoso")
    }

    return listOfCandidateWords
}

fun checkIfFilesExist(fileAllWords: File, fileCandidateWords: File) {

    if (!fileAllWords.exists()) {
        println("Error: The words file $fileAllWords doesn't exist.")
        kotlin.system.exitProcess(0)
    }
    if (!fileCandidateWords.exists()) {
        println("Error: The candidate words file $fileCandidateWords doesn't exist.")
        kotlin.system.exitProcess(0)
    }
}

fun validateWords(words: List<String>, fileName: String): Boolean {

    var invalidWords = 0
    for (word in words) {
        if (!validateInput(word)) {
            invalidWords++
        }
    }
    if (invalidWords != 0) {
        println("Error: $invalidWords invalid words were found in the $fileName file.")
        kotlin.system.exitProcess(0)
    }
    return true
}

fun validateInput(input: String): Boolean {
    if (input.length != 5) {
        return false
    }
    val regex = """[a-zA-Z]{5}""".toRegex()
    if (!input.matches(regex)) {
        return false
    }
    if (input.length != input.toSet().size) {
        return false
    }
    return true
}

fun checkIfCandidateWordsAreInDictionary(candidate: List<String>, allWords: List<String>): Int {
    var wordNotIncluded = 0
    for (word in candidate) {
        if (!allWords.contains(word)) {
            wordNotIncluded++
        }
    }
    return wordNotIncluded
}

fun pickSecretWord(list: List<String>): String {
    val randomIndex = Random.nextInt(0, list.size)
    val secretWord = list[randomIndex]
    return secretWord
}

fun playGame(secretWord: String) {
    var numberOfTurns = 0
    val start = System.currentTimeMillis()

    while (true) {
        numberOfTurns++
        println("\nInput a 5-letter word:")
        val inputWord = readln().trim().lowercase()
        if (inputWord == "exit") exitGame()
        val inputWordVerified = verifyInputWord(inputWord)
        if (inputWordVerified) {
            val correctGuess = checkIfGuessIsCorrect(secretWord, inputWord)
            if (correctGuess) {
                if (numberOfTurns == 1) {
                    println()
                    for (i in inputWord) {
                        print("\u001B[48:5:10m${i.uppercase()}\u001B[0m")
                    }
                    println("\nCorrect!")
                    println("Amazing luck! The solution was found at once.")
                    kotlin.system.exitProcess(0)
                }
                println()
                cluesList.forEach { println(it) }
                for (i in inputWord) {
                    print("\u001B[48:5:10m${i.uppercase()}\u001B[0m")
                }
                println("\nCorrect!")
                val end = System.currentTimeMillis()
                val elapsedTime = end - start
                println("The solution was found after $numberOfTurns tries in ${elapsedTime / 100} seconds.")
                kotlin.system.exitProcess(0)
            }
        }
    }
}

fun exitGame() {
    println("\nThe game is over.")
    kotlin.system.exitProcess(0)
}

fun verifyInputWord(inputWord: String): Boolean {
    if (inputWord.length != 5) {
        println("The input isn't a 5-letter word.")
        return false
    }
    val regex = """[a-z]{5}""".toRegex()
    if (!inputWord.matches(regex)) {
        println("One or more letters of the input aren't valid.")
        return false
    }
    if (inputWord.length != inputWord.toSet().size) {
        println("The input has duplicate letters.")
        return false
    }
    if (inputWord !in dictionary) {
        println("The input word isn't included in my words list.")
        return false
    }
    return true
}

fun checkIfGuessIsCorrect(secret: String, input: String): Boolean {
    var correct = false
    if (secret == input) {
        correct = true
    } else {
        val sb = StringBuilder("")
        for ((index, char) in input.withIndex()) {
            if (char == secret[index]) {
                sb.append("\u001b[48:5:10m${char.uppercase()}\u001B[0m")
            } else if (secret.contains(char)) {
                sb.append("\u001b[48:5:11m${char.uppercase()}\u001B[0m")
            } else {
                wrongCharacters.add(char)
                sb.append("\u001B[48:5:7m${char.uppercase()}\u001B[0m")
            }
        }
        cluesList.add(sb.toString())
        println()
        cluesList.forEach { println(it) }
        println()
        val wrong = wrongCharacters.sorted().joinToString("")
        println("\u001B[48:5:14m${wrong.uppercase()}\u001B[0m")
        println()
    }
    return correct
}
