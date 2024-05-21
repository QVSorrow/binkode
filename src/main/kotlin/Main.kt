package me.qvsorrow

import kotlinx.serialization.Serializable
import me.qvsorrow.binkode.Bincode
import me.qvsorrow.binkode.IntEncoding
import me.qvsorrow.binkode.encodeToByteArray
import okio.Buffer
import okio.sink
import okio.source
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel


fun main() {
    val data = data()
    val bytes = Bincode {
        intEncoding = IntEncoding.Variable
    }.encodeToByteArray(data)
    displayBytes(bytes)
    sendViaSocket(bytes)
}

fun sendViaSocket(bytes: ByteArray) {
    SocketChannel.open(InetSocketAddress(42042)).use { channel ->
        channel.write(ByteBuffer.wrap(bytes))
        channel.shutdownOutput()

        val byteBuffer = ByteBuffer.allocate(1024)
        channel.read(byteBuffer)
        byteBuffer.flip()

        val outputBytes = ByteArray(byteBuffer.remaining())
        byteBuffer.get(outputBytes)
        val string = outputBytes.toString(Charsets.UTF_8)

        println("Output: $string")
    }
//    Socket("127.0.0.1", 42042).use { socket ->
//        socket.channel
////        val source = socket.source()
//        val sink = socket.sink()
//        val buffer = Buffer()
//        buffer.write(bytes)
//        sink.write(buffer, bytes.size.toLong())
//        sink.flush()
//
////        buffer.clear()
////        source.read(buffer, 8)
////        val size = buffer.readLong()
////        buffer.clear()
//
////        source.read(buffer, size)
////        val string = buffer.readByteArray().toString(Charsets.UTF_8)
//        val string = socket.getInputStream().readAllBytes().toString(Charsets.UTF_8)
//        println("Output: $string")
//    }
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
//    val float: Float,
//    val double: Double,
//    val char: Char,
//    val string: String,
//    val enum: DemoEnum,
)

fun data() = Demo(
    boolean = false,
    byte = -42,
    short = -12345,
    int = -987654321,
    long = -1234567890987654321,
//    float = 123.45678f,
//    double = 12345.6789,
//    char = 'A',
//    string = "Привіт 🧑‍🚒",
//    enum = DemoEnum.C,
)

fun displayBytes(bytes: ByteArray) {
    println("${bytes.size} bytes")
    for (byte in bytes) {
        print("%3s ".format(byte.toUByte().toString()))
    }
    println()
}

fun displayAsRustVec(bytes: ByteArray) {
    print("vec![")
    for (byte in bytes) {
        print("%s, ".format(byte.toUByte().toString()))
    }
    println("]")
}
