package chucknorris

const val ZERO = "00"
const val ONE = "0"
const val SECOND_BLOCK = "0"
var option = ""
var encodedOutput = ""
var inputMessage = ""
var binary = ""
var sequence = 1

var toBinary = ""
var blocksOfSeven = mutableListOf<String>()
var decodedOutput = ""

fun main() {
    while (option != "exit") {
        getUserInput()
        when (option) {
            "encode" -> encode()
            "decode" -> decode()
            "exit" -> break
            else -> {
                println("There is no '$option' operation")
                println()
            }
        }
    }
    println("Bye!")
}

fun getUserInput() {
    println("Please input operation (encode/decode/exit): ")
    option = readln()
}

fun encode() {
    getInputFromUser()
    encodeToBinary()
    encodeToUnary()
    println("Encoded string: ")
    println(encodedOutput)
    println()
}

fun getInputFromUser() {
    println("Input string: ")
    inputMessage = readln().trim()
}

fun encodeToBinary() {
    for (char in inputMessage) {
        val bin = Integer.toBinaryString(char.code).toInt()
        binary += String.format("%07d", bin)
    }
}

fun encodeToUnary() {
    for ((i, bin) in binary.withIndex()) {
        encode(bin, i)
    }
}

fun encode(binNum: Char, index: Int) {
    val firstBlock = if (binNum == '1') ONE else ZERO

    if (index == 0) {
        encodedOutput += firstBlock + " "
    }
    if (index > 0 && binNum != binary[index - 1]) {
        encodedOutput += firstBlock + " "
    }
    if (index + 1 >= binary.length) {
        encodedOutput += SECOND_BLOCK.repeat(sequence)
        return
    }
    if (binary[index + 1] == binNum) {
        sequence++
    }
    if (binary[index + 1] != binNum) {
        encodedOutput += SECOND_BLOCK.repeat(sequence) + " "
        sequence = 1
    }
}

fun decode() {
    getEncryptedMessage()
    val validMessage: Boolean = validateMessage()

    if (validMessage) {
        splitIntoBlocksByTwo()
        getBlocksOfSevenSymbols()
        transformBinaryToCharacters()
        println("Decoded string: ")
        println(decodedOutput)
        println()
    } else {
        println("Encoded string is not valid.")
        println()
    }
}

fun getEncryptedMessage() {
    println("Input encoded string: ")
    inputMessage = readln().trim()
}

fun validateMessage(): Boolean {

    // Check if the encoded message includes characters other than 0 or spaces
    val allowedCharacters = setOf('0', ' ')
    if (inputMessage.toSet() != allowedCharacters) {
        return false
    }

    // Check if the first block of each sequence is 0 or 00
    val splitMessage = inputMessage.split(" ")
    for ((index, block) in splitMessage.withIndex()) {
        if(index % 2 == 0) {
            if (block != "0" && block != "00") {
                return false
            }
        }
    }

    // Check if the number of blocks is odd
    if (inputMessage.split(" ").size % 2 != 0) {
        return false
    }

    // Check if	the length of the decoded binary string is not a multiple of 7
    var binary = ""
    for ((index, block) in splitMessage.withIndex()) {
        if(index % 2 == 1) {
            binary += block
        }
    }
    return binary.length % 7 == 0
}

fun splitIntoBlocksByTwo() {
    val blocksByTwo = inputMessage.split(" ").chunked(2) { it.joinToString(" ") }
    for (block in blocksByTwo) {
        if (block[0] == block[1]) {
            toBinary += "0".repeat(block.length - 3)
        }
        if (block[0] != block[1]) {
            toBinary += "1".repeat(block.length - 2)
        }
    }
}

fun getBlocksOfSevenSymbols() {
    blocksOfSeven = toBinary.chunked(7).toMutableList()

}

fun transformBinaryToCharacters() {
    for (binary in blocksOfSeven) {
        decodedOutput += binary.toInt(2).toChar()
    }
}
