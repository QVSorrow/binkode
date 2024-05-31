package me.qvsorrow

import kotlinx.serialization.Serializable
import me.qvsorrow.binkode.Bincode
import me.qvsorrow.binkode.IntEncoding
import me.qvsorrow.binkode.decodeFromByteArray
import me.qvsorrow.binkode.encodeToByteArray


fun main() {
    check(demo)
}

private inline fun<reified T> check(input: T) {

    val bincode = Bincode {
        intEncoding = IntEncoding.Variable
    }
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

enum class DemoEnum {
    A,
    B,
    C,
}

@Serializable
data class Demo(
    val boolean: Boolean,
    val byte: Byte,
    val short: Short,
    val int: Int,
    val long: Long,
    val float: Float,
    val double: Double,
    val char: Char,
    val string: String,
    val enum: DemoEnum,
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
)


fun displayBytes(bytes: ByteArray) {
    println("${bytes.size} bytes")
    for (byte in bytes) {
        print("%3s ".format(byte.toUByte().toString()))
    }
    println()
}
