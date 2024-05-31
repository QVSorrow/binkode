package me.qvsorrow.me.qvsorrow.binkode

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SealedClassSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor

/**
 * [Zigzag algorithm](https://docs.rs/bincode/2.0.0-rc/bincode/config/struct.Configuration.html#method.with_variable_int_encoding)
 */

internal fun zigzag(value: Short): UShort = when {
    value == 0.toShort() -> 0u
    value < 0 -> ((-value).toUInt() * 2u - 1u).toUShort()
    else -> (value.toUShort() * 2u.toUShort()).toUShort()
}

internal fun zigzag(value: Int): UInt = when {
    value == 0 -> 0u
    value < 0 -> (-value).toUInt() * 2u - 1u
    else -> value.toUInt() * 2u
}

internal fun zigzag(value: Long): ULong = when {
    value == 0L -> 0u
    value < 0 -> (-value).toULong() * 2u - 1u
    else -> value.toULong() * 2u
}

internal fun reverseZigZag(value: UShort): Short = when {
    value == 0.toUShort() -> 0
    value % 2u == 1u -> (-((value.toUInt() + 1u) / 2u).toShort()).toShort()
    else -> (value.toUInt() / 2u).toShort()
}

internal fun reverseZigZag(value: UInt): Int = when {
    value == 0u -> 0
    value % 2u == 1u -> -((value + 1u) / 2u).toInt()
    else -> (value / 2u).toInt()
}

internal fun reverseZigZag(value: ULong): Long = when {
    value == 0uL -> 0
    value % 2uL == 1uL -> -((value + 1uL) / 2uL).toLong()
    else -> (value / 2uL).toLong()
}

internal val SerialDescriptor.isUnsignedNumber: Boolean
    get() = this.isInline && this in unsignedNumberDescriptors

@OptIn(InternalSerializationApi::class)
internal val SerializationStrategy<*>.isSealed: Boolean
    get() = this is SealedClassSerializer<*>

internal val unsignedNumberDescriptors = setOf(
    UInt.serializer().descriptor,
    ULong.serializer().descriptor,
    UByte.serializer().descriptor,
    UShort.serializer().descriptor
)

internal const val BYTE = 251u
internal const val SHORT = 65536uL
internal const val INT = 4294967296uL
internal const val LONG = 18446744073709551615uL

internal val SHORT_MARKER = 251u.toUByte()
internal val INT_MARKER = 252u.toUByte()
internal val LONG_MARKER = 253u.toUByte()


internal const val SEALED_TAG = "me.qvsorrow.bincode.sealed.tag"
