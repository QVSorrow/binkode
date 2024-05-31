package me.qvsorrow.binkode

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import me.qvsorrow.me.qvsorrow.binkode.*
import me.qvsorrow.me.qvsorrow.binkode.BYTE
import me.qvsorrow.me.qvsorrow.binkode.INT
import me.qvsorrow.me.qvsorrow.binkode.LONG
import me.qvsorrow.me.qvsorrow.binkode.SHORT
import okio.Buffer
import java.lang.Double.doubleToLongBits
import java.lang.Float.floatToIntBits

@OptIn(ExperimentalSerializationApi::class)
class BincodeEncoder(
    private val configuration: BincodeConfiguration,
    override val serializersModule: SerializersModule,
    val buffer: Buffer = Buffer(),
) : AbstractEncoder() {

    private val writer = OkioBufferWriter(buffer)

    private val intEncoder = run {
        val endian = if (configuration.isBigEndian) BigEndianIntEncoder(writer) else LittleEndianIntEncoder(writer)
        if (configuration.isVariableInt) VariableIntEncoder(endian) else FixedIntEncoder(endian)
    }
    private val uintEncoder = UIntBincodeEncoder(serializersModule, intEncoder.unsignedEncoder())

    override fun encodeBoolean(value: Boolean) {
        intEncoder.encodeByte(if (value) 1 else 0)
    }

    override fun encodeByte(value: Byte) {
        intEncoder.encodeByte(value)
    }

    override fun encodeShort(value: Short) {
        intEncoder.encodeShort(value)
    }

    override fun encodeInt(value: Int) {
        intEncoder.encodeInt(value)
    }

    override fun encodeLong(value: Long) {
        intEncoder.encodeLong(value)
    }

    override fun encodeFloat(value: Float) {
        intEncoder.encodeInt(floatToIntBits(value))
    }

    override fun encodeDouble(value: Double) {
        intEncoder.encodeLong(doubleToLongBits(value))
    }

    override fun encodeChar(value: Char) {
        writer.writeUtf8CodePoint(value.code)
    }

    override fun encodeString(value: String) {
        if (value.startsWith(SEALED_TAG)) {
            val index = value.drop(SEALED_TAG.length + 1).toInt()
            uintEncoder.encodeInt(index)
        } else {
            val bytes = value.toByteArray(Charsets.UTF_8)
            intEncoder.encodeLong(bytes.size.toLong())
            writer.writeBytes(bytes)
        }
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        intEncoder.encodeInt(index)
    }

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        intEncoder.encodeLong(collectionSize.toLong())
        return super.beginCollection(descriptor, collectionSize)
    }

    override fun encodeInline(descriptor: SerialDescriptor): Encoder {
        return when {
            descriptor.isUnsignedNumber -> uintEncoder
            else -> super.encodeInline(descriptor)
        }
    }

    override fun encodeNotNullMark() {
        intEncoder.encodeByte(1)
    }

    override fun encodeNull() {
        intEncoder.encodeByte(0)
    }
}

@ExperimentalSerializationApi
private class UIntBincodeEncoder(
    override val serializersModule: SerializersModule,
    private val intEncoder: IntEncoder,
) : AbstractEncoder() {

    override fun encodeByte(value: Byte) {
        intEncoder.encodeByte(value)
    }

    override fun encodeShort(value: Short) {
        intEncoder.encodeShort(value)
    }

    override fun encodeInt(value: Int) {
        intEncoder.encodeInt(value)
    }

    override fun encodeLong(value: Long) {
        intEncoder.encodeLong(value)
    }

}


interface Writer {
    fun writeByte(value: Byte)
    fun writeBytes(value: ByteArray)

    fun writeShortLe(value: Short)
    fun writeShortBe(value: Short)
    fun writeIntLe(value: Int)
    fun writeIntBe(value: Int)
    fun writeLongLe(value: Long)
    fun writeLongBe(value: Long)
    fun writeFloatLe(value: Float)
    fun writeFloatBe(value: Float)
    fun writeDoubleLe(value: Double)
    fun writeDoubleBe(value: Double)
    fun writeUtf8CodePoint(value: Int)
    fun writeUtf8(value: String)
}

@JvmInline
internal value class OkioBufferWriter(private val buffer: Buffer) : Writer {

    override fun writeByte(value: Byte) {
        buffer.writeByte(value.toInt())
    }

    override fun writeBytes(value: ByteArray) {
        buffer.write(value)
    }

    override fun writeShortLe(value: Short) {
        buffer.writeShortLe(value.toInt())
    }

    override fun writeShortBe(value: Short) {
        buffer.writeShort(value.toInt())
    }

    override fun writeIntLe(value: Int) {
        buffer.writeIntLe(value)
    }

    override fun writeIntBe(value: Int) {
        buffer.writeInt(value)
    }

    override fun writeLongLe(value: Long) {
        buffer.writeLongLe(value)
    }

    override fun writeLongBe(value: Long) {
        buffer.writeLong(value)
    }

    override fun writeFloatLe(value: Float) {
        writeIntLe(floatToIntBits(value))
    }

    override fun writeFloatBe(value: Float) {
        writeIntBe(floatToIntBits(value))
    }

    override fun writeDoubleLe(value: Double) {
        writeLongLe(doubleToLongBits(value))
    }

    override fun writeDoubleBe(value: Double) {
        writeLongBe(doubleToLongBits(value))
    }

    override fun writeUtf8CodePoint(value: Int) {
        buffer.writeUtf8CodePoint(value)
    }

    override fun writeUtf8(value: String) {
        buffer.writeUtf8(value)
    }
}


interface IntEncoder {
    fun encodeByte(value: Byte)
    fun encodeShort(value: Short)
    fun encodeInt(value: Int)
    fun encodeLong(value: Long)

    fun unsignedEncoder(): IntEncoder
}


internal class BigEndianIntEncoder(
    private val writer: Writer,
) : IntEncoder {
    override fun encodeByte(value: Byte) {
        writer.writeByte(value)
    }

    override fun encodeShort(value: Short) {
        writer.writeShortBe(value)
    }

    override fun encodeInt(value: Int) {
        writer.writeIntBe(value)
    }

    override fun encodeLong(value: Long) {
        writer.writeLongBe(value)
    }

    override fun unsignedEncoder(): IntEncoder = this
}

internal class LittleEndianIntEncoder(
    private val writer: Writer,
) : IntEncoder {
    override fun encodeByte(value: Byte) {
        writer.writeByte(value)
    }

    override fun encodeShort(value: Short) {
        writer.writeShortLe(value)
    }

    override fun encodeInt(value: Int) {
        writer.writeIntLe(value)
    }

    override fun encodeLong(value: Long) {
        writer.writeLongLe(value)
    }

    override fun unsignedEncoder(): IntEncoder = this
}

internal class FixedIntEncoder(private val delegate: IntEncoder) : IntEncoder by delegate

internal class VariableIntEncoder(private val delegate: IntEncoder) : IntEncoder by delegate {

    private val denseEncoder = DenseUIntEncoder(delegate)
    // TODO: try pass denseEncoder into VariableUIntEncoder
    private val unsigned = VariableUIntEncoder(delegate)

    override fun encodeShort(value: Short) {
        denseEncoder.encodeUShort(zigzag(value))
    }

    override fun encodeInt(value: Int) {
        denseEncoder.encodeUInt(zigzag(value))
    }

    override fun encodeLong(value: Long) {
        denseEncoder.encodeULong(zigzag(value))
    }

    override fun unsignedEncoder(): IntEncoder = unsigned
}


internal class VariableUIntEncoder(private val delegate: IntEncoder) : IntEncoder by delegate {
    private val denseEncoder = DenseUIntEncoder(delegate)

    override fun encodeShort(value: Short) {
        denseEncoder.encodeUShort(value.toUShort())
    }

    override fun encodeInt(value: Int) {
        denseEncoder.encodeUInt(value.toUInt())
    }

    override fun encodeLong(value: Long) {
        denseEncoder.encodeULong(value.toULong())
    }

    override fun unsignedEncoder(): IntEncoder = this
}


/**
 * [Bincode VarIntEncoding](https://github.com/bincode-org/bincode/blob/trunk/docs/spec.md#varintencoding)
 */
internal class DenseUIntEncoder(private val delegate: IntEncoder) {

    fun encodeUShort(value: UShort) {
        when {
            value < BYTE -> delegate.encodeByte(value.toByte())
            value < SHORT -> {
                delegate.encodeByte(251u.toByte())
                delegate.encodeShort(value.toShort())
            }

            else -> error("unreachable")
        }
    }

    fun encodeUInt(value: UInt) {
        when {
            value < BYTE -> delegate.encodeByte(value.toByte())
            value < SHORT -> {
                delegate.encodeByte(251u.toByte())
                delegate.encodeShort(value.toShort())
            }

            value < INT -> {
                delegate.encodeByte(252u.toByte())
                delegate.encodeInt(value.toInt())
            }

            else -> error("unreachable")
        }
    }

    fun encodeULong(value: ULong) {
        when {
            value < BYTE -> delegate.encodeByte(value.toByte())
            value < SHORT -> {
                delegate.encodeByte(251u.toByte())
                delegate.encodeShort(value.toShort())
            }

            value < INT -> {
                delegate.encodeByte(252u.toByte())
                delegate.encodeInt(value.toInt())
            }

            value < LONG -> {
                delegate.encodeByte(253u.toByte())
                delegate.encodeLong(value.toLong())
            }

            else -> error("unreachable")
        }
    }
}

