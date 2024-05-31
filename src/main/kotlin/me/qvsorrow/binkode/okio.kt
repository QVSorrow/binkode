package me.qvsorrow.me.qvsorrow.binkode

import me.qvsorrow.binkode.SizeLimit
import okio.*

internal class ByteLimitSource(
    private val delegate: Source,
    private val limit: ULong
) : Source by delegate {

    private var total = 0uL

    override fun read(sink: Buffer, byteCount: Long): Long {
        val count = delegate.read(sink, byteCount)
        if (count >= 0L) {
            total += count.toULong()
            require(total <= limit) { "Limit reached (Bincode configuration SizeLimit.Bounded($limit))" }
        }
        return count
    }
}

internal fun Source.withLimit(limit: ULong): Source {
    return ByteLimitSource(this, limit)
}

internal fun Source.withLimit(sizeLimit: SizeLimit): Source {
    return if (sizeLimit is SizeLimit.Bounded) {
        withLimit(sizeLimit.size)
    } else {
        this
    }
}

internal fun Sink.toBuffered(): BufferedSink {
    return if (this is BufferedSink) this else buffer()
}

internal fun Source.toBuffered(): BufferedSource {
    return if (this is BufferedSource) this else buffer()
}
