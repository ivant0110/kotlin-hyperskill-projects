package machine

enum class Constants(val value: Int) {
	WATER_ESPRESSO(250),
	COFFEE_ESPRESSO(16),
	PRICE_ESPRESSO(4),
	WATER_LATE(350),
	MILK_LATE(75),
	COFFEE_LATE(20),
	PRICE_LATE(7),
	WATER_CAPPUCCINO(200),
	MILK_CAPPUCCINO(100),
	COFFEE_CAPPUCCINO(12),
	PRICE_CAPPUCCINO(6);
}

class CoffeeMachine() {
	private var waterTank = 400
	private var milkTank = 540
	private var coffeeBeans = 120
	private var disposableCups = 9
	private var moneyBalance = 550
	private var currentState: String = "choose.action"
	private var coffeeType: String = ""
	private var fill: Int = 0
	var action: String = ""

	fun takeInput() {
		if (currentState == "choose.action") {
			println("Write action (buy, fill, take, remaining, exit):")
			action = readln()
			if (action == "buy") {
				currentState = "choose.coffee"
			}
			if (action == "fill") {
				currentState = "fill.machine"
			}
			return
		}
		if (currentState == "choose.coffee") {
			println("\nWhat do you want to buy? 1 - espresso, 2 - latte, 3 - cappuccino, back - to main menu: ")
			coffeeType = readln()
			currentState = "choose.action"
			return
		}
		if (currentState == "fill.machine") {
			fill = readln().toInt()
			return
		}
	}

	fun printState() {
		println()
		println(
			"""
		The coffee machine has:
		$waterTank ml of water
		$milkTank ml of milk
		$coffeeBeans g of coffee beans
		$disposableCups disposable cups
		$$moneyBalance of money
	""".trimIndent()
		)
		println()
	}

	fun takeOutMoney() {
		println("I gave you $moneyBalance")
		println()
		moneyBalance = 0
	}

	fun fillCoffeeMachine() {
		println("Write how many ml of water you want to add: ")
		takeInput()
		waterTank += fill
		println("Write how many ml of milk you want to add: ")
		takeInput()
		milkTank += fill
		println("Write how many grams of coffee beans you want to add: ")
		takeInput()
		coffeeBeans += fill
		println("Write how many disposable cups you want to add: ")
		takeInput()
		disposableCups += fill
		currentState = "choose.action"
		println()
	}

	fun buyCoffee() {
		takeInput()
		when (coffeeType) {
			"1" -> { // espresso
				val haveResources = checkResources(
					Constants.WATER_ESPRESSO.value,
					0,
					Constants.COFFEE_ESPRESSO.value
				)
				if (haveResources) {
					waterTank -= Constants.WATER_ESPRESSO.value
					coffeeBeans -= Constants.COFFEE_ESPRESSO.value
					moneyBalance += Constants.PRICE_ESPRESSO.value
					disposableCups--
				}
			}
			"2" -> { // latte
				val haveResources = checkResources(
					Constants.WATER_LATE.value,
					Constants.MILK_LATE.value,
					Constants.COFFEE_ESPRESSO.value
				)
				if (haveResources) {
					waterTank -= Constants.WATER_LATE.value
					milkTank -= Constants.MILK_LATE.value
					coffeeBeans -= Constants.COFFEE_LATE.value
					moneyBalance += Constants.PRICE_LATE.value
					disposableCups--
				}
			}
			"3" -> { // cappuccino
				val haveResources = checkResources(
					Constants.WATER_CAPPUCCINO.value,
					Constants.MILK_CAPPUCCINO.value,
					Constants.COFFEE_CAPPUCCINO.value
				)
				if (haveResources) {
					waterTank -= Constants.WATER_CAPPUCCINO.value
					milkTank -= Constants.MILK_CAPPUCCINO.value
					coffeeBeans -= Constants.COFFEE_CAPPUCCINO.value
					moneyBalance += Constants.PRICE_CAPPUCCINO.value
					disposableCups--
				}
			}
			"back" -> return
		}
	}

	fun checkResources(water: Int, milk: Int, coffee: Int, cups: Int = 1): Boolean {
		if (water <= waterTank && milk <= milkTank && coffee <= coffeeBeans && cups <= disposableCups) {
			println("I have enough resources, making you a coffee!")
			println()
			return true
		} else {
			var missingIngredient: String = ""
			if (waterTank - water < 0) {
				missingIngredient = "water"
			}
			if (milkTank - milk < 0) {
				missingIngredient = "milk"
			}
			if (coffeeBeans - coffee < 0) {
				missingIngredient = "coffee beans"
			}
			if (disposableCups == 0) {
				missingIngredient = "disposable cups"
			}
			println("Sorry, not enough $missingIngredient!")
			println()
			currentState = "choose.action"
			return false
		}
	}
}

fun main() {
	val coffeeMaker = CoffeeMachine()
	do {
		coffeeMaker.takeInput()
		when (coffeeMaker.action) {
			"buy" -> coffeeMaker.buyCoffee()
			"fill" -> coffeeMaker.fillCoffeeMachine()
			"take" -> coffeeMaker.takeOutMoney()
			"remaining" -> coffeeMaker.printState()
		}
	} while (coffeeMaker.action != "exit")
}
