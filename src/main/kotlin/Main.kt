package me.qvsorrow

import kotlinx.serialization.*
import me.qvsorrow.binkode.*


fun main() {
    val bincode = Bincode {
        intEncoding = IntEncoding.Variable
        endian = ByteEndian.LittleEndian
    }
    val data = List(10) { demoSmall }
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
    val list: List<Int>,
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
)

val demoSmall = Demo(
    boolean = false,
    byte = 1,
    short = 1,
    int = 1,
    long = 1,
    float = 1f,
    double = 1.0,
    char = 'A',
    string = "Привіт 🧑‍🚒",
    enum = DemoEnum.C,
    list = listOf(42, 1337, -0),
)


fun displayBytes(bytes: ByteArray) {
    println("${bytes.size} bytes")
    for (byte in bytes) {
        print("%3s ".format(byte.toUByte().toString()))
    }
    println()
}
