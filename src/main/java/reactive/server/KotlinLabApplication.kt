package reactive.server

// class not needed here
class KotlinLabApplication {
    var boolVar: Boolean? = true

    fun testElvisOperator() { // = ternary operator
        val b = boolVar ?: false
        println("==> b: $b")
    }

}

fun main() {
    val a = KotlinLabApplication()
    a.testElvisOperator()

    println("hello world")
    myMethod()
}

fun myMethod() {
    println("hello 2")

}