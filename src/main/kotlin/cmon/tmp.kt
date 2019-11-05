package cmon

import kool.PointerBuffer
import kool.Ptr
import kool.set
import org.lwjgl.PointerBuffer
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil.*

fun MemoryStack.PointerBuffer(strings: ArrayList<String>): PointerBuffer {
    PointerBuffer_(strings.size) {
        val length = memLengthUTF8(strings[it], true)
        nmalloc(1, length).also {
            encodeUTF8(strings[it], true, it)
        }
    }
}

inline fun MemoryStack.PointerBuffer_(size: Int, init: (Int) -> Ptr)=
    PointerBuffer(size).apply { for (i in 0 until size) this[i] = init(i) }

internal fun encodeUTF8(text: CharSequence, nullTerminated: Boolean, target: Long): Int {
    var i = 0
    val len = text.length
    var p = 0

    var c: Char

    // ASCII fast path
    while (i < len && (c = text[i]).toInt() < 0x80) {
        UNSAFE.putByte(target + p++, c.toByte())
        i++
    }

    // Slow path
    while (i < len) {
        c = text[i++]
        if (c.toInt() < 0x80) {
            UNSAFE.putByte(target + p++, c.toByte())
        } else {
            var cp = c.toInt()
            if (c.toInt() < 0x800) {
                UNSAFE.putByte(target + p++, (0xC0 or (cp shr 6)).toByte())
            } else {
                if (!isHighSurrogate(c)) {
                    UNSAFE.putByte(target + p++, (0xE0 or (cp shr 12)).toByte())
                } else {
                    cp = toCodePoint(c, text[i++])

                    UNSAFE.putByte(target + p++, (0xF0 or (cp shr 18)).toByte())
                    UNSAFE.putByte(target + p++, (0x80 or (cp shr 12 and 0x3F)).toByte())
                }
                UNSAFE.putByte(target + p++, (0x80 or (cp shr 6 and 0x3F)).toByte())
            }
            UNSAFE.putByte(target + p++, (0x80 or (cp and 0x3F)).toByte())
        }
    }

    if (nullTerminated) {
        UNSAFE.putByte(target + p++, 0.toByte()) // TODO: did we have a bug here?
    }

    return p
}