package phonebook

import java.io.File
import kotlin.math.min
import kotlin.math.sqrt

object TimeDifference {
    var linear1: Long = 0
    var linear2: Long = 0
    var bubble: Long = 0
    var jump: Long = 0
    var quick: Long = 0
    var binary: Long = 0
    var createHash: Long = 0
    var searchHash: Long = 0
}

object FilesFound {
    var linearSearch: Pair<Int, Int> = Pair(0, 0)
    var jumpSearch: Pair<Int, Int> = Pair(0, 0)
    var binarySearch: Pair<Int, Int> = Pair(0, 0)
    var hashMapSearch: Pair<Int, Int> = Pair(0, 0)
}

class Person(val number: String, val name: String)

var bubbleStopped = false
var sortedDirectoryBubble: Array<Person> = arrayOf()
var sortedDirectoryQuick: Array<Person> = arrayOf()

fun main() {
//    val directoryFile = File("./resources/small_directory.txt")
//    val findNamesFile = File("./resources/small_find.txt")
    val directoryFile = File("./resources/directory.txt")
    val findNamesFile = File("./resources/find.txt")

    val unsortedDirectory = createUnsortedDirectory(directoryFile)
    val namesToFind = findNamesFile.readLines().map { it.trim() }.toTypedArray()

    performLinearSearch(unsortedDirectory, namesToFind)
    performBubbleSortAndJumpSearch(unsortedDirectory, namesToFind)
    performQuickSortAndBinarySearch(unsortedDirectory, namesToFind)
    createHashTableAndFindNames(directoryFile, namesToFind)
}

// CREATE UNSORTED DIRECTORY
fun createUnsortedDirectory(directoryFile: File): Array<Person> {
    val listOfResults = directoryFile.readLines().map {
        val parts = it.split(" ")
        val number = parts[0].trim()
        val name = if (parts.size > 2) {
            "${parts[1].trim()} ${parts[2].trim()}"
        } else {
            parts[1].trim()
        }
        Person(number, name)
    }
    return listOfResults.toTypedArray()
}

// LINEAR SEARCH
fun performLinearSearch(unsortedDirectory: Array<Person>, namesToFind: Array<String>) {
    println("Start searching (linear search)...")
    val startTimeLinear1 = System.currentTimeMillis()
    FilesFound.linearSearch = linearSearch(unsortedDirectory, namesToFind)
    val endTimeLinear1 = System.currentTimeMillis()
    TimeDifference.linear1 = endTimeLinear1 - startTimeLinear1
    printSearchResults(TimeDifference.linear1, FilesFound.linearSearch)
}

fun linearSearch(directory: Array<Person>, find: Array<String>): Pair<Int, Int> {
    val results = mutableListOf<Person>()

    for (element in find) {
        for (entry in directory) {
            if (entry.name == element) {
                results.add(entry)
            }
        }
    }
    return Pair(results.size, find.size)
}

fun printSearchResults(timeDifference: Long, filesFound: Pair<Int, Int>) {
    val (found, total) = filesFound
    val (minutes, seconds, milliSeconds) = timeDifferenceTriple(timeDifference)
    print("Found $found / $total entries. ")
    print("Time taken: $minutes min. $seconds sec. $milliSeconds ms.\n")
}

// BUBBLE SORT AND JUMP SEARCH
fun performBubbleSortAndJumpSearch(unsortedDirectory: Array<Person>, namesToFind: Array<String>) {
    println("\nStart searching (bubble sort + jump search)...")
    val startTimeBubble = System.currentTimeMillis()

    val bubbleSortThread = Thread {
        tryBubbleSortAndJumpSearch(unsortedDirectory, namesToFind, startTimeBubble)
    }
    bubbleSortThread.start()

    Thread.sleep(timeLimit())

    if (bubbleSortThread.isAlive) {
        bubbleSortThread.interrupt()
        bubbleStopped = true
        val endTimeBubble = System.currentTimeMillis()
        TimeDifference.bubble = endTimeBubble - startTimeBubble

        val startTimeLinear2 = System.currentTimeMillis()
        val filesFound = linearSearch(unsortedDirectory, namesToFind)
        val endTimeLinear2 = System.currentTimeMillis()
        TimeDifference.linear2 = endTimeLinear2 - startTimeLinear2
        val totalTimeTaken = TimeDifference.bubble + TimeDifference.linear2
        printSearchResults(totalTimeTaken, filesFound)
    }

    printSortAndSearchTimes(TimeDifference.bubble, TimeDifference.jump + TimeDifference.linear2, bubbleStopped)
}

fun timeLimit(): Long {
    return TimeDifference.linear1 * 10
}

fun tryBubbleSortAndJumpSearch(unsortedDirectory: Array<Person>, find: Array<String>, startTimeBubble: Long) {
    try {
        sortedDirectoryBubble = bubbleSort(unsortedDirectory)
        val endTimeBubble = System.currentTimeMillis()
        TimeDifference.bubble = endTimeBubble - startTimeBubble

        val startTimeJumpSearch = System.currentTimeMillis()
        FilesFound.jumpSearch = jumpSearch(sortedDirectoryBubble, find)
        val endTimeJumpSearch = System.currentTimeMillis()
        TimeDifference.jump = endTimeJumpSearch - startTimeJumpSearch
        val totalTimeTaken = TimeDifference.bubble + TimeDifference.jump
        printSearchResults(totalTimeTaken, FilesFound.jumpSearch)
    } catch (e: InterruptedException) {
        Thread.currentThread().interrupt()
    }
}

// BUBBLE SORT
fun bubbleSort(directory: Array<Person>): Array<Person> {
    var swapped = true

    while (swapped) {
        swapped = false
        for (current in 1 until directory.size) {
            if (Thread.interrupted()) {
                throw InterruptedException("Task interrupted")
            }
            val prev = current - 1
            if (directory[prev].name > directory[current].name) {
                val temp = directory[current]
                directory[current] = directory[prev]
                directory[prev] = temp
                swapped = true
            }
        }
    }
    return directory
}

// JUMP SEARCH
fun jumpSearch(sorted: Array<Person>, find: Array<String>): Pair<Int, Int> {
    val results = mutableListOf<Person>()

    for (item in find) {
        val result = jumpSearchAsc(sorted, item)
        if (result != null) {
            results.add(result)
        }
    }
    return Pair(results.size, find.size)
}

fun jumpSearchAsc(sorted: Array<Person>, valueToFind: String): Person? {
    if (sorted.isEmpty()) return null

    var currentIndex = 0
    val lastIndex = sorted.lastIndex
    val step = sqrt(sorted.size.toDouble()).toInt()
    var currentValue = sorted[currentIndex]

    while (currentValue.name < valueToFind) {
        if (currentIndex == lastIndex) return null

        val prevIndex = currentIndex
        currentIndex = min(currentIndex + step, lastIndex)
        currentValue = sorted[currentIndex]

        while (currentValue.name > valueToFind) {
            currentIndex--
            currentValue = sorted[currentIndex]
            if (currentIndex <= prevIndex) return null
        }
    }
    return if (currentValue.name == valueToFind) currentValue else null
}

// QUICK SORT AND BINARY SEARCH
fun performQuickSortAndBinarySearch(unsortedDirectory: Array<Person>, namesToFind: Array<String>) {
    println("\nStart searching (quick sort + binary search)...")

    val startTimeQuick = System.currentTimeMillis()
    quickSort(unsortedDirectory, 0, unsortedDirectory.lastIndex)
    val endTimeQuick = System.currentTimeMillis()
    TimeDifference.quick = endTimeQuick - startTimeQuick

    val startTimeBinary = System.currentTimeMillis()
    FilesFound.binarySearch = binarySearch(sortedDirectoryQuick, namesToFind)
    val endTimeBinary = System.currentTimeMillis()
    TimeDifference.binary = endTimeBinary - startTimeBinary
    val totalTimeTaken = TimeDifference.quick + TimeDifference.binary

    printSearchResults(totalTimeTaken, FilesFound.binarySearch)
    printSortAndSearchTimes(TimeDifference.quick, TimeDifference.binary)
}

// QUICK SORT
fun quickSort(unsortedDirectory: Array<Person>, startIndex: Int, endIndex: Int) {
    sortedDirectoryQuick = unsortedDirectory
    if (startIndex < endIndex) {
        val pivotIndex = partition(sortedDirectoryQuick, startIndex, endIndex)
        quickSort(sortedDirectoryQuick, startIndex, pivotIndex - 1)
        quickSort(sortedDirectoryQuick, pivotIndex + 1, endIndex)
    }
}

fun partition(people: Array<Person>, startIndex: Int, endIndex: Int): Int {
    val pivot = people[endIndex]
    var i = startIndex

    for (j in startIndex until endIndex) {
        if (people[j].name < pivot.name) {
            val temp = people[i]
            people[i] = people[j]
            people[j] = temp
            i++
        }
    }
    val temp = people[i]
    people[i] = people[endIndex]
    people[endIndex] = temp
    return i
}

// BINARY SEARCH
fun binarySearch(sortedDirectory: Array<Person>, findList: Array<String>): Pair<Int, Int> {
    val results = mutableListOf<Person>()

    for (name in findList) {
        val result = binarySearchAsc(sortedDirectory, name)
        if (result != null) {
            results.add(result)
        }
    }
    return Pair(results.size, findList.size)
}

fun binarySearchAsc(sortedDirectory: Array<Person>, nameToFind: String): Person? {
    if (sortedDirectory.isEmpty()) return null

    var start = 0
    var end = sortedDirectory.lastIndex

    while (start <= end) {
        val middle = start + (end - start) / 2

        when {
            sortedDirectory[middle].name == nameToFind -> {
                return sortedDirectory[middle]
            }

            sortedDirectory[middle].name > nameToFind -> {
                end = middle - 1
            }

            else -> {
                start = middle + 1
            }
        }
    }
    return null
}

// PRINT SORTING AND SEARCHING TIMES
fun printSortAndSearchTimes(sortTime: Long, searchTime: Long, bubbleStopped: Boolean = false, creating: Boolean = false) {
    val (minSort, secSort, milliSort) = timeDifferenceTriple(sortTime)
    if (creating) {
        print("Creating time: $minSort min. $secSort sec. $milliSort ms.")
    } else {
        print("Sorting time: $minSort min. $secSort sec. $milliSort ms.")
    }
    if (bubbleStopped) {
        print(" - STOPPED, moved to linear search")
    }
    val searchingTime = TimeDifference.jump + TimeDifference.linear2
    val (minSearch, secSearch, milliSearch) = timeDifferenceTriple(searchTime)
    println("\nSearching time: $minSearch min. $secSearch sec. $milliSearch ms.")
}

fun timeDifferenceTriple(timeDifference: Long): Triple<Long, Long, Long> {
    val minutes = timeDifference / (1000 * 60)
    val seconds = (timeDifference / 1000) % 60
    val milliSeconds = timeDifference % 1000
    return Triple(minutes, seconds, milliSeconds)
}

// CREATE HASH TABLE  AND SEARCH FOR NAMES
fun createHashTableAndFindNames(directoryFile: File, namesToFind: Array<String>) {
    println("\nStart searching (hash table)...")

    val startTimeCreateHash = System.currentTimeMillis()
    val hashMap = createHashMap(directoryFile)
    val endTimeCreateHash = System.currentTimeMillis()
    TimeDifference.createHash = endTimeCreateHash - startTimeCreateHash

    val startTimeSearchHash = System.currentTimeMillis()
    FilesFound.hashMapSearch = searchForNamesInHashMap(hashMap, namesToFind)
    val endTimeSearchHash = System.currentTimeMillis()
    TimeDifference.searchHash = endTimeSearchHash - startTimeSearchHash

    val totalTimeTaken = TimeDifference.createHash + TimeDifference.searchHash

    printSearchResults(totalTimeTaken, FilesFound.hashMapSearch)
    printSortAndSearchTimes(TimeDifference.createHash, TimeDifference.searchHash, creating = true)
}

fun createHashMap(file: File): HashMap<String, String> {
    val hashMap = HashMap<String, String>()

    file.useLines { lines ->
        lines.forEach { line ->
            val (number, name) = line.split(" ", limit = 2)
            hashMap[name] = number
        }
    }
    return hashMap
}

fun searchForNamesInHashMap(hashMap: HashMap<String, String>, namesToFind: Array<String>): Pair<Int, Int> {
    val resultList = mutableListOf<Person>()

    for (name in namesToFind) {
        if (hashMap.containsKey(name)) {
            val number = hashMap[name]
            if (number != null) {
                resultList.add(Person(name, number))
            }
        }
    }
    return Pair(resultList.size, namesToFind.size)
}