package me.qvsorrow

import kotlinx.serialization.*
import me.qvsorrow.binkode.*
import me.qvsorrow.me.qvsorrow.binkode.SEALED_TAG


fun main() {
    val bincode = Bincode
    val data = demo
    checkBinary(bincode) { data }
}

private inline fun <reified T> checkBinary(bincode: BinaryFormat, inputFactory: () -> T) {

    val input = inputFactory()

    val bytes = bincode.encodeToByteArray(input)
    displayBytes(bytes)
    val output = bincode.decodeFromByteArray<T>(bytes)

    check(input == output) {
        println("Actual: $input")
        println("Expected: $output")
    }

    println("Input:  $input")
    println("Output: $output")
}

private inline fun <reified T> checkString(bincode: StringFormat, inputFactory: () -> T) {

    val input = inputFactory()

    val string = bincode.encodeToString(input)
    println(string)
    val output = bincode.decodeFromString<T>(string)

    check(input == output) {
        println("Actual: $input")
        println("Expected: $output")
    }

    println("Input:  $input")
    println("Output: $output")
}

enum class DemoEnum {
    A,
    B,
    C,
}

@Serializable
@JvmInline
value class Inline(val value: String)

@Serializable
sealed interface Sealed {
    @SerialName("$SEALED_TAG;0")
    @Serializable
    data object VariantA : Sealed

    @SerialName("$SEALED_TAG;1")
    @Serializable
    data class VariantB(val value: Int) : Sealed

    @SerialName("$SEALED_TAG;2")
    @Serializable
    data class VariantC(val value: Int) : Sealed
}

@Serializable
data class Demo(
    val boolean: Boolean,
    val byte: Byte,
    val short: Short,
    val int: Int,
    val long: Long?,
    val float: Float,
    val double: Double,
    val char: Char,
    val string: String,
    val enum: DemoEnum,
    val list: List<Int>,
    val map: Map<Int, String>,
    val sealed: Sealed,
    val inline: Inline,
)

val demo = Demo(
    boolean = false,
    byte = -42,
    short = -12345,
    int = -987654321,
    long = -1234567890987654321,
    float = 123.45678f,
    double = 12345.6789,
    char = 'A',
    string = "Привіт 🧑‍🚒",
    enum = DemoEnum.C,
    list = listOf(42, 1337, -0),
    map = mapOf(
        1 to "one",
        2 to "two",
        3 to "three",
    ),
    sealed = Sealed.VariantB(3),
    inline = Inline("inline"),
)



fun displayBytes(bytes: ByteArray) {
    println("${bytes.size} bytes")
    for (byte in bytes) {
        print("%3s ".format(byte.toUByte().toString()))
    }
    println()
}
