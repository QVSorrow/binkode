package me.qvsorrow.binkode

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import okio.Buffer
import okio.Sink
import okio.Source
import okio.use


public sealed class Bincode(
    public val configuration: BincodeConfiguration,
    override val serializersModule: SerializersModule
) : BinaryFormat {

    public companion object Default : Bincode(BincodeConfiguration(), EmptySerializersModule())

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

    public fun <T> encodeToSink(sink: Sink, serializer: SerializationStrategy<T>, value: T) {
        val encoder = BincodeEncoder(configuration, serializersModule, sink.toBuffered())
        encoder.encodeSerializableValue(serializer, value)
    }

    public fun <T> decodeFromSource(source: Source, deserializer: DeserializationStrategy<T>): T {
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

public inline fun <reified T> Bincode.encodeToSink(sink: Sink, value: T) {
    return encodeToSink(sink, serializer<T>(), value)
}

public inline fun <reified T> Bincode.decodeFromSource(source: Source): T {
    return decodeFromSource(source, serializer<T>())
}


public fun Bincode(from: Bincode = Bincode.Default, builderAction: BincodeBuilder.() -> Unit): Bincode {
    val builder = BincodeBuilder(from)
    builder.builderAction()
    val configuration = builder.build()
    return BincodeImpl(configuration, builder.serializersModule)
}

internal class BincodeImpl(
    configuration: BincodeConfiguration,
    module: SerializersModule,
) : Bincode(configuration, module)

public class BincodeBuilder(from: Bincode) {

    public var serializersModule: SerializersModule = from.serializersModule
    public var endian: ByteEndian = from.configuration.endian
    public var intEncoding: IntEncoding = from.configuration.intEncoding
    public var byteLimit: SizeLimit = from.configuration.byteLimit
    public var trailing: Trailing = from.configuration.trailing

    public fun build(): BincodeConfiguration {
        return BincodeConfiguration(
            endian = endian,
            intEncoding = intEncoding,
            byteLimit = byteLimit,
            trailing = trailing,
        )
    }
}

