package util

import glm_.i
import kool.PointerBuffer
import kool.Ptr
import kool.set
import org.lwjgl.PointerBuffer
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil.*
import java.nio.ByteBuffer

@JvmName("PointerBufferSafe")
fun MemoryStack.PointerBuffer(strings: Collection<String>?): PointerBuffer? = strings?.let { PointerBuffer(it) }

fun MemoryStack.PointerBuffer(strings: Collection<String>): PointerBuffer =
    PointerBuffer_(strings.size) { i ->
        val length = memLengthUTF8(strings.elementAt(i), true)
        nmalloc(1, length).also {
            encodeUTF8(strings.elementAt(i), true, it)
        }
    }

inline fun MemoryStack.PointerBuffer_(size: Int, init: (Int) -> Ptr) =
    PointerBuffer(size).apply { for (i in 0 until size) this[i] = init(i) }

fun MemoryStack.nUtf8(text: CharSequence, nullTerminated: Boolean = true): Ptr {
    val length = memLengthUTF8(text, nullTerminated)
    return nmalloc(1, length).also {
        encodeUTF8(text, nullTerminated, it)
    }
}

internal fun encodeUTF8(text: CharSequence, nullTerminated: Boolean, target: Long): Int {
    var i = 0
    val len = text.length
    var p = 0

    var c = text[i]

    // ASCII fast path
    while (i < len && c.i < 0x80) {
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
                if (!c.isHighSurrogate()) {
                    UNSAFE.putByte(target + p++, (0xE0 or (cp shr 12)).toByte())
                } else {
                    cp = Character.toCodePoint(c, text[i++])

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

val UNSAFE: sun.misc.Unsafe = run {
    val fields = sun.misc.Unsafe::class.java.declaredFields

    /*
        Different runtimes use different names for the Unsafe singleton,
        so we cannot use .getDeclaredField and we scan instead. For example:

        Oracle: theUnsafe
        PERC : m_unsafe_instance
        Android: THE_ONE
        */
    lateinit var unsafe: sun.misc.Unsafe
    for (field in fields) {
        if (field.type != sun.misc.Unsafe::class.java)
            continue

        val modifiers = field.modifiers
        if (!(java.lang.reflect.Modifier.isStatic(modifiers) && java.lang.reflect.Modifier.isFinal(modifiers)))
            continue

        try {
            field.isAccessible = true
            unsafe = field.get(null) as sun.misc.Unsafe
        } catch (ignored: Exception) {
        }

        break
    }
    unsafe
//    throw UnsupportedOperationException("LWJGL requires sun.misc.Unsafe to be available.")
}