package svcs

import java.io.File
import java.security.SecureRandom
import java.security.MessageDigest
import java.math.BigInteger

const val DIRECTORY = "vcs"
const val COMMITS = "commits"
const val CONFIG_FILE = "config.txt"
const val FILES_INDEX = "index.txt"
const val LOG_FILE = "log.txt"

val workingDir: String = System.getProperty("user.dir")
val separator: String = File.separator
val directoryPath = File("$workingDir$separator$DIRECTORY")
val commitsFolder = File("$directoryPath$separator$COMMITS")
val configFilePath = File(directoryPath, CONFIG_FILE)
val indexFilePath = File(directoryPath, FILES_INDEX)
val logFilePath = File(directoryPath, LOG_FILE)
var argument = ""
val logMessages = mutableListOf<String>()
val commands = mapOf(
	"config" to "Get and set a username.",
	"add" to "Add a file to the index.",
	"log" to "Show commit logs.",
	"commit" to "Save changes.",
	"checkout" to "Restore a file."
)

fun main(args: Array<String>) {
	if (args.isNotEmpty()) argument = args[0]
	if (!directoryPath.exists()) directoryPath.mkdir()

	when (argument) {
		"" -> getHelp()
		"--help" -> getHelp()
		"config" -> configUsername(args)
		"add" -> addFileToIndex(args)
		"commit" -> saveFileChanges(args)
		"log" -> {
			readCommitHistory()
			showCommitHistory()
		}
		"checkout" -> restoreFiles(args)
		else -> println("'$argument' is not a SVCS command.")
	}
}

fun getHelp() {
	println("These are SVCS commands:")
	for ((k, v) in commands.entries) {
		println("${k.padEnd(11)} $v")
	}
}

fun configUsername(args: Array<String>) {
	if (args.size > 1) {
		val username = args[1]

		if (!configFilePath.exists()) {
			configFilePath.createNewFile()
		}
		configFilePath.writeText(username)
		val usernameFromFile = readUsernameFromFile()
		println("The username is ${usernameFromFile}.")
	} else if (args.size == 1 && configFilePath.exists()) {
		val usernameFromFile = readUsernameFromFile()
		println("The username is ${usernameFromFile}.")
	} else if (args.size == 1 && !configFilePath.exists()) {
		println("Please, tell me who you are.")
	}
}

fun readUsernameFromFile(): String {
	return configFilePath.readText()
}

fun addFileToIndex(args: Array<String>) {
	if (!indexFilePath.exists()) {
		indexFilePath.createNewFile()
	}
	val readTrackedFiles = indexFilePath.readLines()

	if (args.size > 1) {
		val fileName = args[1]
		val fileNamePath = File(fileName)
		if (fileNamePath.exists()) {
			if (!readTrackedFiles.contains(fileName)) {
				indexFilePath.appendText("$fileName\n")
			}
			println("The file '$fileName' is tracked.")
		} else {
			println("Can't find '$fileName'.")
		}
	} else if (args.size == 1 && readTrackedFiles.isNotEmpty()) {

		println("Tracked files:")
		for (fileName in readTrackedFiles) {
			println(fileName)
		}
	} else if (args.size == 1 && readTrackedFiles.isEmpty()) {
		println("Add a file to the index.")
	}
}

fun checkIfFilesHaveChanged(): Boolean {
	var filesHaveChanged = false
	if (logFilePath.exists()) {
		val filesToCheck = indexFilePath.readLines()
		val fileHashes = mutableListOf<String>()
		filesToCheck.forEach { it ->
			val source = File(workingDir, it)
			val sha256 = source.hash("SHA-256")
			val hashed = sha256.joinToString("") { "%02x".format(it) }
			fileHashes.add(hashed)
		}

		val hashesOfLastCommit = mutableListOf<String>()
		readCommitHistory()
		val readID = logMessages.last().split("\n").first().split(" ")[1]
		val commitFolder = File("$commitsFolder$separator$readID")
		filesToCheck.forEach { it ->
			val source = File(commitFolder, it)
			val sha256 = source.hash("SHA-256")
			val hashed = sha256.joinToString("") { "%02x".format(it) }
			hashesOfLastCommit.add(hashed)
		}

		for ((index, file) in fileHashes.withIndex()) {
			if (file != hashesOfLastCommit[index]) {
				filesHaveChanged = true
			}
		}
	} else {
		filesHaveChanged = true
	}
	return filesHaveChanged
}

fun File.hash(algorithm: String): ByteArray {
	val digest = MessageDigest.getInstance(algorithm)
	this.inputStream().use { inputStream ->
		val bytes = inputStream.readBytes()
		digest.update(bytes)
	}
	return digest.digest()
}

fun saveFileChanges(args: Array<String>) {
	val filesHaveChanged = checkIfFilesHaveChanged()

	if (args.size > 1 && !filesHaveChanged) {
		println("Nothing to commit.")
	} else if (args.size > 1 && filesHaveChanged) {
		val commitId = generateUniqueId()
		saveFileVersion(commitId)
		saveChangesToLogFile(args, commitId)
		println("Changes are committed.")
	} else if (args.size == 1) {
		println("Message was not passed.")
	}
}

fun generateUniqueId(): String {
	val secureRandom = SecureRandom()
	val uniqueId = BigInteger(128, secureRandom).toString(32)
	return uniqueId
}

fun saveFileVersion(id: String) {
	if (!commitsFolder.exists()) commitsFolder.mkdir()

	val newDir = File("$commitsFolder$separator$id")
	newDir.mkdir()
	val filesToCopy = indexFilePath.readLines()

	filesToCopy.forEach {
		val source = File(workingDir, it)
		val destination = File(newDir, it)
		source.copyTo(destination)
	}
}

fun saveChangesToLogFile(args: Array<String>, id: String) {
	if (!logFilePath.exists()) logFilePath.createNewFile()
	val usernameFromFile = readUsernameFromFile()
	val commitMessage = args[1]
	val logMessage = "commit $id\nAuthor: $usernameFromFile\n$commitMessage\n\n"
	logFilePath.appendText(logMessage)
}

fun readCommitHistory() {
	if (logFilePath.exists()) {
		var currentMessage = StringBuilder()
		logFilePath.useLines { lines ->
			lines.forEach { line ->
				if (line.isBlank()) {
					logMessages.add(currentMessage.toString().trim())
					currentMessage = StringBuilder()
				} else {
					currentMessage.appendLine(line)
				}
			}
		}
	}
}

fun showCommitHistory() {
	if (logMessages.isNotEmpty()) {
		for (i in logMessages.lastIndex downTo 0) {
			if (i == 0) {
				println(logMessages[i])
			} else {
				println(logMessages[i] + "\n")
			}
		}
	} else {
		println("No commits yet.")
	}
}

fun restoreFiles(args: Array<String>) {
	if (args.size > 1) {
		val id = args[1]
		readCommitHistory()
		val commitExists: Boolean = checkIfCommitExist(id)

		if (commitExists) {
			restoreFiles(id)
		} else {
			println("Commit does not exist.")
		}
	} else if (args.size == 1) {
		println("Commit id was not passed.")
	}
}

fun checkIfCommitExist(id: String): Boolean {
	var exists = false

	if (logMessages.isNotEmpty()) {
		val idList = mutableListOf<String>()
		for (message in logMessages) {
			idList.add(message.split("\n").first().split(" ")[1])
		}
		exists = idList.contains(id)
	}
	return exists
}

fun restoreFiles(commitId: String) {
	val sourceDir = File("$commitsFolder$separator$commitId")
	val filesToCopy = indexFilePath.readLines()

	filesToCopy.forEach {
		val source = File(sourceDir, it)
		val destination = File(workingDir, it)
		source.copyTo(destination, overwrite = true)
	}
	println("Switched to commit $commitId.")
}