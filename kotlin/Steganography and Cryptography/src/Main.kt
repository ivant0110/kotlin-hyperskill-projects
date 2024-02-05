package cryptography

import java.awt.Color
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File
import java.io.IOException

fun main() {
	while (true) {
		println("Task (hide, show, exit): ")
		when (val task = readln()) {
			"exit" -> {
				println("Bye!")
				break
			}
			"hide" -> hideMessage()
			"show" -> showMessage()
			else -> println("Wrong task: $task")
		}
	}
}

fun hideMessage() {
	try {
		val (inputFile, outputFile) = getFileInfoFromUser()
		val messageInBits = getMessageAndEncrypt()

		val imageBuffer: BufferedImage = ImageIO.read(inputFile)

		if (messageInBits.length > imageBuffer.width * imageBuffer.height) {
			println("The input image is not large enough to hold this message.")
			return
		}

		hideMessageInBufferedImage(imageBuffer, messageInBits)

		val output = File("$outputFile")
		ImageIO.write(imageBuffer, "png", output)
		println("Message saved in $outputFile image.")

	} catch (e: IOException) {
		println(e.message)
	}
}

fun getFileInfoFromUser(): Array<File> {
	println("Input image file:")
	val inputFile = File(readln())
	println("Output image file:")
	val outputFile = File(readln())
	return arrayOf(inputFile, outputFile)
}

fun getMessageAndEncrypt(): String {
	println("Message to hide:")
	val message = readln()
	val password = readPassword()

	val encryptedBytesArr = encryptOrDecryptMessage(message, password)

	val encryptedBytes = encryptedBytesArr + byteArrayOf(0, 0, 3)
	val encryptedBits = bytesToBits(encryptedBytes)

	return encryptedBits
}

fun bytesToBits(bytes: ByteArray): String {
	return bytes.map { byte ->
		Integer.toBinaryString(byte.toInt() and 0xFF).padStart(8, '0')
	}.joinToString("")
}


fun hideMessageInBufferedImage(image: BufferedImage, messageBits: String) {
	var index = 0

	for (y in 0 until image.height) {
		for (x in 0 until image.width) {
			val color = Color(image.getRGB(x, y))

			if (index < messageBits.length) {
				val blue = color.blue and 0xFE or messageBits[index].toString().toInt()
				val newColorRGB = Color(color.red, color.green, blue % 256)
				image.setRGB(x, y, newColorRGB.rgb)
			}
			index++
		}
	}
}

fun showMessage() {
	try {
		println("Input image file:")
		val fileWithHiddenMessage = File(readln())

		val hiddenMessageImageBuffer: BufferedImage = ImageIO.read(fileWithHiddenMessage)
		val width = hiddenMessageImageBuffer.width
		val height = hiddenMessageImageBuffer.height
		var messageBits = ""

		outerLoop@ for (y in 0 until height) {
			for (x in 0 until width) {
				val color = Color(hiddenMessageImageBuffer.getRGB(x, y))

				val colorBlueString = color.blue.toString(2)
				val bit = colorBlueString.substring(colorBlueString.lastIndex)
				messageBits += bit
				if (messageBits.contains("000000000000000000000011")) break@outerLoop
			}
		}

		val messageBytes = messageBits.chunked(8).toMutableList()

		repeat(3) {
			messageBytes.removeAt(messageBytes.lastIndex)
		}

		val message = decryptMessage(messageBytes)
		println("Message:")
		println(message)
	} catch (e: IOException) {
		println(e.message)
	}
}

fun decryptMessage(message: MutableList<String>): String {
	val decPassword = readPassword()
	val messageBytes = message.map { (it.toInt(2).toChar()) }.joinToString("")

	val decryptedBytes = encryptOrDecryptMessage(messageBytes, decPassword)

	return decryptedBytes.map { it.toInt().toChar() }.joinToString("")
}

fun readPassword(): String {
	println("Password:")
	return readln()
}

fun encryptOrDecryptMessage(message: String, password: String): ByteArray {
	val messageBytes = message.encodeToByteArray()
	val passwordBytes = password.encodeToByteArray()

	val resultBytes = ByteArray(messageBytes.size)

	for ((index, byte) in messageBytes.withIndex()) {
		val passByte = passwordBytes[index % passwordBytes.size]
		val result = byte.toInt() xor passByte.toInt()
		resultBytes[index] = result.toByte()
	}
	return resultBytes
}