package me.qvsorrow.binkode

import java.nio.ByteOrder


data class BincodeConfiguration(

    // defaults match bincode rust crate
    // https://github.com/bincode-org/bincode/blob/trunk/docs/spec.md

    val endian: ByteEndian = ByteEndian.LittleEndian,
    val intEncoding: IntEncoding = IntEncoding.Variable,
    val byteLimit: SizeLimit = SizeLimit.Infinite,
    val trailing: Trailing = Trailing.RejectTrailing,
) {

    val isBigEndian = when (endian) {
        ByteEndian.NativeEndian -> ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN
        ByteEndian.BigEndian -> true
        ByteEndian.LittleEndian -> false
    }

    val isVariableInt = when (intEncoding) {
        IntEncoding.Variable -> true
        IntEncoding.Fixed -> false
    }
}

enum class ByteEndian {
    /**
     * Platform native byte endian.
     * Useful for direct communication with native libraries.
     */
    NativeEndian,

    /**
     * Starts from the highest byte.
     * Network byte order.
     * Default for JVM.
     */
    BigEndian,

    /**
     * Starts from the lowest byte.
     */
    LittleEndian,
}

enum class IntEncoding {
    /**
     * Encoding an unsigned integer v (of any type excepting u8/i8) works as follows:
     *
     *     If u < 251, encode it as a single byte with that value.
     *     If 251 <= u < 2**16, encode it as a literal byte 251, followed by a u16 with value u.
     *     If 2**16 <= u < 2**32, encode it as a literal byte 252, followed by a u32 with value u.
     *     If 2**32 <= u < 2**64, encode it as a literal byte 253, followed by a u64 with value u.
     *     If 2**64 <= u < 2**128, encode it as a literal byte 254, followed by a u128 with value u.
     *
     * usize is being encoded/decoded as a u64 and isize is being encoded/decoded as a i64.
     */
    Variable,
    Fixed,
}

sealed interface SizeLimit {
    data object Infinite : SizeLimit
    data class Bounded(val size: ULong) : SizeLimit
}

enum class Trailing {
    RejectTrailing,
    AllowTrailing,
}