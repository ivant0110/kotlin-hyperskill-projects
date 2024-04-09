package calculator
import java.math.BigInteger

val variableMap = mutableMapOf<String, BigInteger>()
val regNum = """\(*[+-]?[0-9]+\)*""".toRegex()
val regLetters = """[a-zA-Z]+""".toRegex()

fun main() {
    while (true) {
        val input = readln().trim()
        if (input == "") continue
        if (input.first() == '/') {
            processCommand(input)
            continue
        }
        if (input.contains("=")) {
            processVariable(input)
            continue
        }
        val formatInputOrNull = formatInput(input)
        val formatInput = formatInputOrNull ?: continue
        val infixString = readVariables(formatInput)
        if (infixString == "") continue
        val postfixString =
            try {
                transformInfixToPostfix(infixString)
            } catch (e: Exception) {
                println("Invalid expression")
                continue
            }

        val result = evaluatePostfixExpression(postfixString)
        if (result != null) {
            println(result.toString())
        }
    }
}

// PROCESS COMMANDS
fun processCommand(input: String) {
    when (input) {
        "/exit" -> exit()
        "/help" -> help()
        else -> println("Unknown command")
    }
}

fun exit() {
    println("Bye!")
    kotlin.system.exitProcess(0)
}

fun help() {
    println("-The program calculates the sum of numbers.")
    println("-It allows addition '+' and subtraction '-'.")
    println("-It can store the results of previous calculations.")
    println("-It allows multiplication '*' and division '/'.")
    println("-It allows power operator '^'.")
    println("-The program accepts large numbers.")
}

// PROCESS VARIABLES
fun processVariable(input: String) {

    val list = input.split("=").map { it.trim() }
    if (!list[0].matches(regLetters)) {
        println("Invalid identifier")
        return
    }
    if (list.size == 1) {
        if (variableMap[list[0]] != null) {
            println(variableMap[list[0]])
        } else {
            println("Unknown variable")
        }
        return
    }
    if (list.size != 2) {
        println("Invalid assignment")
        return
    }
    if (!list[1].matches(regNum) && !list[1].matches(regLetters)) {
        println("Invalid assignment")
        return
    }
    if (list[1].matches(regNum)) {
        variableMap[list[0]] = list[1].toBigInteger()
        return
    }
    if (list[1].matches(regLetters)) {
        val valueOfExistingVariable: BigInteger
        if (variableMap[list[1]] != null) {
            valueOfExistingVariable = variableMap[list[1]]!!
            variableMap[list[0]] = valueOfExistingVariable
        } else {
            println("Unknown variable")
        }
    }
}

// FORMAT INPUT
fun formatInput(input: String): String? {
    val plusRegex = """\++""".toRegex()
    val minusRegex = """-{2,}""".toRegex()
    val operatorRegex = """[-+*^/()=]""".toRegex()
    val regexSpaces = """\s{2,}""".toRegex()
    val regexMulti = """\*{2,}""".toRegex()
    val regexDiv = """/{2,}""".toRegex()
    val regexPow = """\^{2,}""".toRegex()

    if (input.contains(regexMulti) || input.contains(regexDiv) || input.contains(regexPow)) {
        println("Invalid expression")
        return null
    }

    val replaceMultiplePlus = input.replace(plusRegex, "+")
    val replaceMultipleMinus = replaceMultiplePlus.replace(minusRegex) { match ->
        if (match.value.length % 2 == 0) "+" else "-"
    }
    val addSpaces = replaceMultipleMinus.replace(operatorRegex) { match -> " ${match.value} " }
    val removeDuplicateSpaces = addSpaces.replace(regexSpaces, " ")
    return removeDuplicateSpaces.trim()
}

// READ VARIABLES
fun readVariables(expression: String): String {
    val workingList = mutableListOf<String>()

    for (element in expression.split(" ")) {
        if (element.matches(regLetters)) {
            val value = variableMap[element]
            if (value != null) {
                workingList.add(value.toString())
            } else {
                println("Unknown variable")
                return ""
            }
        } else {
            workingList.add(element)
        }
    }
    val string = workingList.joinToString("")
    val result = formatInput(string)
    return result!!
}

// TRANSFORM EXPRESSION FROM INFIX TO POSTFIX
fun transformInfixToPostfix(expression: String): String {
    val operatorPrecedence = mapOf('(' to 0, ')' to 0, '+' to 1, '-' to 1, '*' to 2, '/' to 2, '^' to 3)
    val stringBuilder = StringBuilder()
    val operatorStack = ArrayDeque<Char>()

    fun precedenceOf(operator: Char): Int {
        return operatorPrecedence[operator] ?: throw IllegalArgumentException("Unknown operator")
    }

    val elements = expression.split(' ').filter { it.isNotBlank() }

    for (element in elements) {
        when {
            // Append operand (number) to string builder
            element.all { it.isDigit() } -> {
                stringBuilder.append(element)
                stringBuilder.append(' ')
            }
            // Add Left parenthesis to operatorStack
            element == "(" -> operatorStack.add(element.single())
            // Remove operators from stack and Append to string builder
            element == ")" -> {
                while (operatorStack.isNotEmpty() && operatorStack.last() != '(') {
                    stringBuilder.append(operatorStack.removeLast())
                    stringBuilder.append(' ')
                }
                // Remove the left parenthesis from operatorStack
                operatorStack.removeLast()
            }
            // Append operators to string builder according to their precedence rules
            // while maintaining the correct order of operations.
            element.length == 1 && element.single() in operatorPrecedence -> {
                val currentOp = element.single()
                while (operatorStack.isNotEmpty() && precedenceOf(currentOp) <= precedenceOf(operatorStack.last())) {
                    if (operatorStack.last() == '(') break
                    stringBuilder.append(operatorStack.removeLast())
                    stringBuilder.append(' ')
                }
                operatorStack.add(currentOp)
            }
            element.length > 1 -> {
                val currentOp = element.toCharArray()
                for (op in currentOp) {
                    while (operatorStack.isNotEmpty() && precedenceOf(op) <= precedenceOf(operatorStack.last())) {
                        if (operatorStack.last() == '(') break
                        stringBuilder.append(operatorStack.removeLast())
                        stringBuilder.append(' ')
                    }
                    operatorStack.add(op)
                }
            }
        }
    }
    // Append remaining operators to the output
    while (operatorStack.isNotEmpty()) {
        stringBuilder.append(operatorStack.removeLast())
        stringBuilder.append(' ')
    }
    return stringBuilder.toString().trim()
}

// CALCULATE THE RESULT FROM POSTFIX EXPRESSION
fun evaluatePostfixExpression(expression: String): BigInteger? {
    val stack = ArrayDeque<BigInteger>()
    val operators = listOf("+", "-", "*", "/", "^")

    for (element in expression.split(' ')) {
        when {
            element.all { it.isDigit() } -> stack.add(element.toBigInteger())
            element in operators -> {
                val operand2 = stack.removeLast()
                val operand1 = if (stack.isNotEmpty()) stack.removeLast() else 0.toBigInteger()

                val result = when (element) {
                    "^" -> operand1.pow(operand2.toInt())
                    "+" -> operand1 + operand2
                    "-" -> operand1 - operand2
                    "*" -> operand1 * operand2
                    "/" -> operand1 / operand2
                    else -> {
                        println("Invalid expression")
                        return null
                    }
                }
                stack.add(result)
            }
            else -> {
                println("Invalid expression")
                return null
            }
        }
    }
    return stack.single()
}
