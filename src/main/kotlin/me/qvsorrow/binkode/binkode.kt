package me.qvsorrow.binkode

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import me.qvsorrow.me.qvsorrow.binkode.toBuffered
import me.qvsorrow.me.qvsorrow.binkode.withLimit
import okio.*
import java.io.ByteArrayInputStream


sealed class Bincode(
    val configuration: BincodeConfiguration,
    override val serializersModule: SerializersModule
) : BinaryFormat {

    companion object Default : Bincode(BincodeConfiguration(), EmptySerializersModule())

    override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
        Buffer().use { sink ->
            encodeToSink(sink, serializer, value)
            return sink.readByteArray()
        }
    }

    override fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
        Buffer().use { source ->
            source.write(bytes)
            return decodeFromSource(source, deserializer)
        }
    }

    fun <T> encodeToSink(sink: Sink, serializer: SerializationStrategy<T>, value: T) {
        val encoder = BincodeEncoder(configuration, serializersModule, sink.toBuffered())
        encoder.encodeSerializableValue(serializer, value)
    }

    fun <T> decodeFromSource(source: Source, deserializer: DeserializationStrategy<T>): T {
        val bufferedSource = source
            .withLimit(configuration.byteLimit)
            .toBuffered()

        val decoder = BincodeDecoder(configuration, serializersModule, bufferedSource)
        val value = decoder.decodeSerializableValue(deserializer)
        when (configuration.trailing) {
            Trailing.RejectTrailing -> require(bufferedSource.exhausted()) { "Bincode configuration Trailing.RejectTrailing requires all bytes are read" }
            Trailing.AllowTrailing -> Unit
        }
        return value
    }
}

inline fun <reified T> Bincode.encodeToSink(sink: Sink, value: T) {
    return encodeToSink(sink, serializer<T>(), value)
}

inline fun <reified T> Bincode.decodeFromSource(source: Source): T {
    return decodeFromSource(source, serializer<T>())
}


fun Bincode(from: Bincode = Bincode.Default, builderAction: BincodeBuilder.() -> Unit): Bincode {
    val builder = BincodeBuilder(from)
    builder.builderAction()
    val configuration = builder.build()
    return BincodeImpl(configuration, builder.serializersModule)
}

internal class BincodeImpl(
    configuration: BincodeConfiguration,
    module: SerializersModule,
) : Bincode(configuration, module)

class BincodeBuilder(from: Bincode) {

    var serializersModule: SerializersModule = from.serializersModule
    var endian: ByteEndian = from.configuration.endian
    var intEncoding: IntEncoding = from.configuration.intEncoding
    var byteLimit: SizeLimit = from.configuration.byteLimit
    var trailing: Trailing = from.configuration.trailing

    fun build(): BincodeConfiguration {
        return BincodeConfiguration(
            endian = endian,
            intEncoding = intEncoding,
            byteLimit = byteLimit,
            trailing = trailing,
        )
    }
}

