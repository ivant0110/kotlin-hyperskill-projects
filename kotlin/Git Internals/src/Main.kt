package gitinternals

import java.io.File
import java.io.ByteArrayInputStream
import java.util.zip.InflaterInputStream
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

val separator: String = File.separator
val workingDirSys: String = System.getProperty("user.dir")

fun main() {
    val inputData = getInputFromUser()
    val command = inputData[2]
    when (command) {
        "commit-tree" -> listTree(inputData)
        "log" -> listLogs(inputData)
        "list-branches" -> {
            val path = inputData[0]
            listBranches(path)
        }
        "cat-file" -> displayGitObjectFileData(inputData)
        else -> println("Unknown command")
    }
}

// GET INPUT FROM USER
fun getInputFromUser(): List<String> {
    println("Enter .git directory location:")
    val dirPath = readln().trim()
    println("Enter command:")
    val command = readln().trim()
    var hash = ""
    var branch = ""
    when (command) {
        "cat-file" -> {
            println("Enter git object hash:")
            hash = readln().trim()
        }
        "log" -> {
            println("Enter branch name:")
            branch = readln().trim()
        }
        "commit-tree" -> {
            println("Enter commit-hash:")
            hash = readln().trim()
        }
    }
    return listOf(dirPath, hash, command, branch)
}

// PRINT FULL FILE TREE
fun listTree(inputData: List<String>) {
    val treeHash = getTreeHash(inputData)

    if (treeHash == "") return
    val path = inputData[0]
    val treePath = buildGitObjectPath(listOf(path, treeHash))
    val compressedData = readGitObject(treePath)

    var treeList2D = prepare2DTreeList(compressedData)
    var hasSubTree: Boolean
    var filePath = ""
    val fileList = mutableListOf<String>()
    do {
        hasSubTree = false
        for (item in treeList2D) {
            if (item[0] == "100644") {
                fileList.add("$filePath${item[2]}")
            } else if (item[0] == "40000") {
                hasSubTree = true
                val hash = item[1]
                filePath = "${item[2]}/"
                val subTreePath = buildGitObjectPath(listOf(path, hash))
                val compressedTreeData = readGitObject(subTreePath)
                treeList2D = prepare2DTreeList(compressedTreeData)
            }
        }
    } while (hasSubTree)
    fileList.forEach { println(it) }
}

fun getTreeHash(inputData: List<String>): String {
    val inputPath = buildGitObjectPath(inputData)
    val compressedData = readGitObject(inputPath)
    val decompressedData = decompressGitObject(compressedData)
    if (decompressedData.isBlank()) return ""

    val treeHash = decompressedData
        .split("\n")
        .first { it.startsWith("tree") }
        .split(" ")
        .lastOrNull()
    if (treeHash == null) return ""
    return treeHash
}

// PRINT GIT LOGS
fun listLogs(inputData: List<String>) {
    val branchPath = buildBranchPath(inputData)
    val branchFile = File(branchPath)
    if (!branchFile.exists()) return

    val directory = inputData[0]
    var commitHash = branchFile.readText().trim()
    var parent: Int

    do {
        val data = getLogData(listOf(directory, commitHash))
        printLog(data, commitHash)

        parent = data.count { it.contains("parent") }
        if (parent == 1) {
            commitHash = data[2][1]
        }
        if (parent == 2) {
            commitHash = data[2][1]
            val mergedHash = data[3][1]
            val mergedData = getLogData(listOf(directory, mergedHash))
            printLog(mergedData, mergedHash, true)
        }
    } while (parent != 0)
}

fun getLogData(inputList: List<String>): List<List<String>> {
    val commitPath = buildGitObjectPath(inputList)
    val compressedData = readGitObject(commitPath)
    val decompressedData = decompressGitObject(compressedData)
    val data = decompressedData
        .split("\n")
        .filter { it.isNotBlank() }
        .map { it.split(" ") }
    return data
}

fun printLog(data: List<List<String>>, hash: String, merged: Boolean = false) {

    if (merged) println("Commit: $hash (merged)") else println("Commit: $hash")
    data.map {
        if (it.contains("committer")) {
            val email = it[2].replace("[<>]".toRegex(), "")
            val timeStamp = formatTimestamp(it)
            println("${it[1]} $email commit timestamp: $timeStamp")
        }
    }
    val startIndex = data.indexOfFirst { it.contains("committer") }
    val messageLists = data.subList(startIndex + 1, data.size)
    messageLists.forEach { println(it.joinToString(" ")) }
    println()
}

fun buildBranchPath(inputData: List<String>): String {
    val (path, _, _, branch) = inputData
    val folders = mutableListOf<String>()
    val workingDir = findWorkingDir()
    folders.addAll(workingDir)
    folders.addAll(path.split("""[\\/]""".toRegex()))
    folders.add("refs")
    folders.add("heads")
    folders.add(branch)
    return folders.joinToString(separator)
}

// LIST BRANCHES
fun listBranches(path: String) {
    val (localBranchesPath, headPath) = buildLocalBranchesPath(path)
    val directory = File(localBranchesPath)
    val headFile = File(headPath)
    var head = ""
    if (!directory.exists() || !directory.isDirectory) return
    val files = directory.listFiles() ?: arrayOf<File>()

    if (headFile.exists()) {
        val headInfo = headFile.readLines().first()
        head = headInfo.split(' ')[1].split("""[\\/]""".toRegex()).last()
    }

    val listOfFiles = mutableListOf<String>()

    for (file in files) {
        val fileName = file.name as String
        listOfFiles.add(fileName)
    }
    val sortedListOfFiles = listOfFiles.sorted().toMutableList()

    val finalList = mutableListOf<String>()
    for (file in sortedListOfFiles) {
        if (file == head) {
            finalList.add("* $file")
        } else {
            finalList.add("  $file")
        }
    }

    finalList.forEach { println(it) }
}

fun buildLocalBranchesPath(path: String): Pair<String, String> {
    val folders = mutableListOf<String>()
    val workingDir = findWorkingDir()
    folders.addAll(workingDir)
    folders.addAll(path.split("""[\\/]""".toRegex()))
    folders.add("HEAD")
    val headPath = folders.joinToString(separator)
    folders.removeLast()
    folders.add("refs")
    folders.add("heads")
    val localBranchesPath = folders.joinToString(separator)
    return Pair(localBranchesPath, headPath)
}

// PRINT GIT OBJECT CONTENT
fun displayGitObjectFileData(input: List<String>) {
    val inputPath = buildGitObjectPath(input)
    val compressedData = readGitObject(inputPath)
    val decompressedData = decompressGitObject(compressedData)
    if (decompressedData.isBlank()) return
    val objectType = findObjectType(decompressedData)
    when (objectType) {
        "BLOB" -> formatBlob(decompressedData)
        "COMMIT" -> formatCommit(decompressedData)
        "TREE" -> formatTree(compressedData)
    }
}

fun buildGitObjectPath(input: List<String>): String {
    val (directory, hash) = input
    val folders = mutableListOf<String>()
    val workingDir = findWorkingDir()
    folders.addAll(workingDir)
    folders.addAll(directory.split("""[\\/]""".toRegex()))
    folders.add("objects")
    folders.add(hash.substring(0, 2))
    folders.add(hash.substring(2))
    val result = folders.joinToString(separator)
    return result
}

fun readGitObject(filePath: String): ByteArray {
    val file = File(filePath)
    return if (file.exists() && file.isFile()) {
        file.readBytes()
    } else {
        ByteArray(0)
    }
}

fun decompressGitObject(compressed: ByteArray): String {
    if (compressed.isNotEmpty()) {
        val byteArrayInputStream = ByteArrayInputStream(compressed)
        val inflaterInputStream = InflaterInputStream(byteArrayInputStream)
        val decompressBytes = inflaterInputStream.use { it.readBytes() }

        // Convert the decompressed byte array to a string
        val stringBuilder = StringBuilder()
        var index = 0
        while (index < decompressBytes.size) {
            // when we encounter a null terminated string
            // (null byte - byte with the value 0) we append a newline character
            if (decompressBytes[index].toInt() == 0) {
                stringBuilder.append('\n')
            } else {
                stringBuilder.append(decompressBytes[index].toInt().toChar())
            }
            index++
        }
        return stringBuilder.toString()
    } else {
        return ""
    }
}

fun findObjectType(decompressed: String): String {
    val firstLine = decompressed.split("\n").firstOrNull() ?: ""
    val objectType = firstLine.split(" ").first().uppercase()
    println("*$objectType*")
    return objectType
}

fun formatBlob(decompressed: String) {
    val content = decompressed.split("\n")
    for ((index, line) in content.withIndex()) {
        if (index > 0 && line.isNotBlank()) {
            println(line)
        }
    }
}

fun formatCommit(decompressed: String) {
    val content = decompressed
        .split("\n")
        .filter { it.isNotBlank() }
        .map { it.split(" ") }

    // print tree
    content.map {
        if (it.contains("tree")) {
            println("${it.first()}: ${it.last()}")
        }
    }

    // print parents
    when (content.count { it.contains("parent") }) {
        1 -> {
            println("parents: ${content[2][1]}")
        }
        2 -> {
            println("parents: ${content[2][1]} | ${content[3][1]}")
        }
    }

    // print author
    content.map {
        if (it.contains("author")) {
            val email = it[2].replace("[<>]".toRegex(), "")
            val timeStamp = formatTimestamp(it)
            println("${it[0]}: ${it[1]} $email original timestamp: $timeStamp")
        }
    }

    // print commiter
    content.map {
        if (it.contains("committer")) {
            val email = it[2].replace("[<>]".toRegex(), "")
            val timeStamp = formatTimestamp(it)
            println("${it[0]}: ${it[1]} $email commit timestamp: $timeStamp")
        }
    }

    // print message
    val startIndex = content.indexOfFirst { it.contains("committer") }
    val messageLists = content.subList(startIndex + 1, content.size)
    println("commit message:")
    messageLists.forEach { println(it.joinToString(" ")) }
}

fun formatTimestamp(list: List<String>): String {
    val zone = ZoneOffset.of(list[4])
    val timeStamp = Instant.ofEpochSecond(list[3].toLong()).atZone(zone)
    val formattedTimeStamp = timeStamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxx"))
    return formattedTimeStamp
}

fun formatTree(compressed: ByteArray) {
    val treeList2D = prepare2DTreeList(compressed)
    treeList2D.forEach { it.joinToString(" ").also(::println) }
}

fun prepare2DTreeList(compressed: ByteArray): MutableList<List<String>> {
    val byteArrayInputStream = ByteArrayInputStream(compressed)
    val inflaterInputStream = InflaterInputStream(byteArrayInputStream)
    val decompressed = inflaterInputStream.use { it.readAllBytes() }

    val listSplit = decompressed
        .map { it.toUShort() }.joinToString(" ")
        .split(" 0 ")
        .map { it.split(" ") }

    val list1D = listSplit
        .map {
            it.map { it.toUShort().toInt().toChar() }
                .joinToString("")
        }

    val list2D = mutableListOf<List<String>>()
    for (index in 1 until list1D.lastIndex) {
        val binarySHA = list1D[index + 1].substring(0, 20)
        val hexCode = convertBinaryToHex(binarySHA)
        if (index == 1) {
            val (metadataNumber, filename) = list1D[index].split(" ")
            list2D.add(listOf(metadataNumber, hexCode, filename))
        }
        if (index > 1 && index < list1D.lastIndex) {
            val (metadataNumber, filename) = list1D[index].substring(20).split(" ")
            list2D.add(listOf(metadataNumber, hexCode, filename))
        }
    }
    return list2D
}

fun convertBinaryToHex(binary: String): String {
    val hexCode = binary
        .map { it.code.toByte() }
        .joinToString("") { "%02x".format(it) }
    return hexCode
}

fun findWorkingDir(): List<String> {
    val list = mutableListOf<String>()
    list.addAll(workingDirSys.split("""[\\/]""".toRegex()))
//    list.add("Git Internals") // required for tests to work locally
//    list.add("task") // required for tests to work locally
    return list
}