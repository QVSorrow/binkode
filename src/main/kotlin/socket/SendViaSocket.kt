package me.qvsorrow.socket

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

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