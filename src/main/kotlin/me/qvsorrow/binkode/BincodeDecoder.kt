package me.qvsorrow.binkode

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule
import me.qvsorrow.me.qvsorrow.binkode.*
import me.qvsorrow.me.qvsorrow.binkode.BYTE
import me.qvsorrow.me.qvsorrow.binkode.INT_MARKER
import me.qvsorrow.me.qvsorrow.binkode.LONG_MARKER
import me.qvsorrow.me.qvsorrow.binkode.SHORT_MARKER
import me.qvsorrow.me.qvsorrow.binkode.reverseZigZag
import okio.Buffer

@OptIn(ExperimentalSerializationApi::class)
class BincodeDecoder(
    private val configuration: BincodeConfiguration,
    override val serializersModule: SerializersModule,
    buffer: Buffer,
) : AbstractDecoder() {

    private val reader = OkioBufferReader(buffer)

    private val intDecoder = run {
        val endian = if (configuration.isBigEndian) BigEndianIntDecoder(reader) else LittleEndianIntDecoder(reader)
        if (configuration.isVariableInt) VariableIntDecoder(endian) else FixedIntDecoder(endian)
    }

    private val uintDecoder = UIntBincodeDecoder(serializersModule, intDecoder)
    private var elementIndex = 0

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (elementIndex == descriptor.elementsCount) return CompositeDecoder.DECODE_DONE
        return elementIndex++
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        elementIndex = 0
        return this
    }

    override fun decodeBoolean(): Boolean {
        return when (intDecoder.decodeByte()) {
            BYTE_TRUE -> true
            BYTE_FALSE -> false
            else -> error("unreachable")
        }
    }

    override fun decodeByte(): Byte {
        return intDecoder.decodeByte()
    }

    override fun decodeShort(): Short {
        return intDecoder.decodeShort()
    }

    override fun decodeInt(): Int {
        return intDecoder.decodeInt()
    }

    override fun decodeLong(): Long {
        return intDecoder.decodeLong()
    }

    override fun decodeFloat(): Float {
        return Float.fromBits(intDecoder.decodeInt())
    }

    override fun decodeDouble(): Double {
        return Double.fromBits(intDecoder.decodeLong())
    }

    override fun decodeChar(): Char {
        return Char(reader.readUtf8CodePoint())
    }

    override fun decodeString(): String {
        val size = intDecoder.decodeLong()
        val bytes = reader.readBytes(size)
        return String(bytes, Charsets.UTF_8)
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        return intDecoder.decodeInt()
    }

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
        return intDecoder.decodeLong().toInt()
    }

    override fun decodeInline(descriptor: SerialDescriptor): Decoder {
        return when {
            descriptor.isUnsignedNumber -> uintDecoder
            else -> super.decodeInline(descriptor)
        }
    }
}

private const val BYTE_TRUE = 1.toByte()
private const val BYTE_FALSE = 0.toByte()

@ExperimentalSerializationApi
private class UIntBincodeDecoder(
    override val serializersModule: SerializersModule,
    private val intDecoder: IntDecoder,
) : AbstractDecoder() {

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        TODO("Not yet implemented")
    }

    override fun decodeByte(): Byte {
        return intDecoder.decodeByte()
    }

    override fun decodeShort(): Short {
        return intDecoder.decodeShort()
    }

    override fun decodeInt(): Int {
        return intDecoder.decodeInt()
    }

    override fun decodeLong(): Long {
        return intDecoder.decodeLong()
    }
}


interface Reader {
    fun readByte(): Byte
    fun readBytes(length: Long): ByteArray
    fun readShortLe(): Short
    fun readShortBe(): Short
    fun readIntLe(): Int
    fun readIntBe(): Int
    fun readLongLe(): Long
    fun readLongBe(): Long
    fun readFloatLe(): Float
    fun readFloatBe(): Float
    fun readDoubleLe(): Double
    fun readDoubleBe(): Double
    fun readUtf8CodePoint(): Int
    fun readUtf8(length: Long): String
}

@JvmInline
value class OkioBufferReader(private val buffer: Buffer) : Reader {

    override fun readByte(): Byte {
        return buffer.readByte()
    }

    override fun readBytes(length: Long): ByteArray {
        return buffer.readByteArray(length)
    }

    override fun readShortLe(): Short {
        return buffer.readShortLe()
    }

    override fun readShortBe(): Short {
        return buffer.readShort()
    }

    override fun readIntLe(): Int {
        return buffer.readIntLe()
    }

    override fun readIntBe(): Int {
        return buffer.readInt()
    }

    override fun readLongLe(): Long {
        return buffer.readLongLe()
    }

    override fun readLongBe(): Long {
        return buffer.readLong()
    }

    override fun readFloatLe(): Float {
        return Float.fromBits(buffer.readIntLe())
    }

    override fun readFloatBe(): Float {
        return Float.fromBits(buffer.readInt())
    }

    override fun readDoubleLe(): Double {
        return Double.fromBits(buffer.readLongLe())
    }

    override fun readDoubleBe(): Double {
        return Double.fromBits(buffer.readLong())
    }

    override fun readUtf8CodePoint(): Int {
        return buffer.readUtf8CodePoint()
    }

    override fun readUtf8(length: Long): String {
        return buffer.readUtf8(length)
    }

}

interface IntDecoder {
    fun decodeByte(): Byte
    fun decodeShort(): Short
    fun decodeInt(): Int
    fun decodeLong(): Long

    fun unsignedDecoder(): IntDecoder
}

internal class BigEndianIntDecoder(
    private val reader: Reader,
) : IntDecoder {
    override fun decodeByte(): Byte {
        return reader.readByte()
    }

    override fun decodeShort(): Short {
        return reader.readShortBe()
    }

    override fun decodeInt(): Int {
        return reader.readIntBe()
    }

    override fun decodeLong(): Long {
        return reader.readLongBe()
    }

    override fun unsignedDecoder(): IntDecoder = this
}

internal class LittleEndianIntDecoder(
    private val reader: Reader,
) : IntDecoder {
    override fun decodeByte(): Byte {
        return reader.readByte()
    }

    override fun decodeShort(): Short {
        return reader.readShortLe()
    }

    override fun decodeInt(): Int {
        return reader.readIntLe()
    }

    override fun decodeLong(): Long {
        return reader.readLongLe()
    }

    override fun unsignedDecoder(): IntDecoder = this
}

internal class FixedIntDecoder(private val delegate: IntDecoder) : IntDecoder by delegate

internal class VariableIntDecoder(private val delegate: IntDecoder) : IntDecoder by delegate {
    private val denseDecoder = DenseUIntDecoder(delegate)

    // TODO: try pass denseEncoder into VariableUIntDecoder
    private val unsigned = VariableUIntDecoder(delegate)

    override fun decodeShort(): Short {
        return reverseZigZag(denseDecoder.decodeUShort())
    }

    override fun decodeInt(): Int {
        return reverseZigZag(denseDecoder.decodeUInt())
    }

    override fun decodeLong(): Long {
        return reverseZigZag(denseDecoder.decodeULong())
    }

    override fun unsignedDecoder(): IntDecoder = unsigned
}

internal class VariableUIntDecoder(private val delegate: IntDecoder) : IntDecoder by delegate {

    private val denseDecoder = DenseUIntDecoder(delegate)

    override fun decodeShort(): Short {
        return denseDecoder.decodeUShort().toShort()
    }

    override fun decodeInt(): Int {
        return denseDecoder.decodeUInt().toInt()
    }

    override fun decodeLong(): Long {
        return denseDecoder.decodeULong().toLong()
    }


    override fun unsignedDecoder(): IntDecoder = this
}


/**
 * [Bincode VarIntEncoding](https://github.com/bincode-org/bincode/blob/trunk/docs/spec.md#varintencoding)
 */
internal class DenseUIntDecoder(private val delegate: IntDecoder) {

    fun decodeUShort(): UShort {
        val byte = delegate.decodeByte().toUByte()
        return when {
            byte < SHORT_MARKER -> byte.toUShort()
            byte == SHORT_MARKER -> delegate.decodeShort().toUShort()
            else -> error("unreachable")
        }
    }

    fun decodeUInt(): UInt {
        val byte = delegate.decodeByte().toUByte()
        return when {
            byte < SHORT_MARKER -> byte.toUInt()
            byte == SHORT_MARKER -> delegate.decodeShort().toUInt()
            byte == INT_MARKER -> delegate.decodeInt().toUInt()
            else -> error("unreachable")
        }
    }

    fun decodeULong(): ULong {
        val byte = delegate.decodeByte().toUByte()
        return when {
            byte < SHORT_MARKER -> byte.toULong()
            byte == SHORT_MARKER -> delegate.decodeShort().toULong()
            byte == INT_MARKER -> delegate.decodeInt().toULong()
            byte == LONG_MARKER -> delegate.decodeLong().toULong()
            else -> error("unreachable")
        }
    }

}

