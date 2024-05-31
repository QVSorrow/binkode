package me.qvsorrow.binkode

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import okio.Buffer
import java.io.ByteArrayInputStream


sealed class Bincode(
    val configuration: BincodeConfiguration,
    override val serializersModule: SerializersModule
) : BinaryFormat {

    companion object Default : Bincode(BincodeConfiguration(), EmptySerializersModule())

    override fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
        val buffer = Buffer().run {
            when (val limit = configuration.byteLimit) {
                is SizeLimit.Bounded -> readFrom(ByteArrayInputStream(bytes), minOf(limit.size.toLong(), bytes.size.toLong()))
                SizeLimit.Infinite -> readFrom(ByteArrayInputStream(bytes), bytes.size.toLong())
            }

        }
        val decoder = BincodeDecoder(configuration, serializersModule, buffer)
        val value = decoder.decodeSerializableValue(deserializer)
        when (configuration.trailing) {
            Trailing.RejectTrailing -> require(buffer.exhausted()) { "Bincode configuration Trailing.RejectTrailing requires all bytes are read" }
            Trailing.AllowTrailing -> Unit
        }
        return value
    }

    override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
        val encoder = BincodeEncoder(configuration, serializersModule)
        encoder.encodeSerializableValue(serializer, value)
        return encoder.buffer.readByteArray()
    }
}

fun Bincode(from: Bincode = Bincode.Default, builderAction: BincodeBuilder.() -> Unit): Bincode {
    val builder = BincodeBuilder(from)
    builder.builderAction()
    val configuration = builder.build()
    return BincodeImpl(configuration, builder.serializersModule)
}

inline fun <reified T> Bincode.encodeToByteArray(value: T) = encodeToByteArray(serializer<T>(), value)
inline fun <reified T> Bincode.decodeFromByteArray(bytes: ByteArray) = decodeFromByteArray(serializer<T>(), bytes)

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

