package bot
import java.util.Scanner
val scanner = Scanner(System.`in`)

fun main() {
	greet("Rambo", "2024") 
	remindName()
	guessAge()
	count()
	test()
	end()
}

fun greet(assistantName: String, birthYear: String) {
	println("Hello! My name is ${assistantName}.")
	println("I was created in ${birthYear}.")
	println("Please, remind me your name.")
}

fun remindName() {
	val name = scanner.nextLine()
	println("What a great name you have, ${name}!")
}

fun guessAge() {
	println("Let me guess your age.")
	println("Enter remainders of dividing your age by 3, 5 and 7.")
	val rem3 = scanner.nextInt()
	val rem5 = scanner.nextInt()
	val rem7 = scanner.nextInt()
	val age = (rem3 * 70 + rem5 * 21 + rem7 * 15) % 105
	println("Your age is ${age}; that's a good time to start programming!")
}

fun count() {
	println("Now I will prove to you that I can count to any number you want.")
	val num = scanner.nextInt()
	for (i in 0..num) {
		print(i)
		println("!")
	}
}

fun test() {
	var answer: Boolean= false
	val question = """Why do we use methods?
        1. To repeat a statement multiple times.
        2. To decompose a program into several small subroutines.
        3. To determine the execution time of a program.
        4. To interrupt the execution of a program."""
	println("Let's test your programming knowledge.")
	println(question)

	while (!answer) {
		val num = scanner.nextInt()
		if (num != 2) {
			println("Please, try again.")
		} else {
			answer = true
		}
	}
}

fun end() {
	println("Congratulations, have a nice day!")
}
