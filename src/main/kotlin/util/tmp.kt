package util

import glm_.BYTES
import glm_.L
import glm_.b
import glm_.i
import glm_.vec2.Vec2
import identifiers.Instance
import identifiers.PhysicalDevice
import kool.*
import kool.lib.indices
import org.lwjgl.PointerBuffer
import org.lwjgl.glfw.GLFWVulkan
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil.*
import org.lwjgl.system.Pointer
import uno.glfw.GlfwWindow
import vkk.*
import vkk.entities.VkDeviceSize
import vkk.entities.VkImageView_Array
import vkk.entities.VkSemaphore_Array
import vkk.entities.VkSurfaceKHR
import java.nio.ByteBuffer
import java.nio.LongBuffer

@JvmName("PointerBufferSafe")
fun MemoryStack.PointerBuffer(strings: Collection<String>?): PointerBuffer? = strings?.let { PointerBuffer(it) }

fun MemoryStack.PointerBuffer(strings: Collection<String>): PointerBuffer =
    PointerBuffer_(strings.size) { i ->
        val string = strings.elementAt(i)
        val length = memLengthUTF8(string, true)
        nmalloc(1, length).also { encodeUTF8(string, true, it) }
    }

inline fun MemoryStack.PointerBuffer_(size: Int, init: (Int) -> Ptr) =
    PointerBuffer(size).apply {
        for (i in 0 until size)
            this[i] = init(i)
    }

@JvmName("PointerAddressSafe")
fun MemoryStack.PointerAddress(strings: Collection<String>?): Ptr = strings?.let { PointerAddress(it) } ?: NULL

fun MemoryStack.PointerAddress(strings: Collection<String>): Ptr =
    PointerAddress(strings.size) { i ->
        val string = strings.elementAt(i)
        val length = memLengthUTF8(string, true)
        nmalloc(1, length).also { encodeUTF8(string, true, it) }
    }

inline fun MemoryStack.PointerAddress(size: Int, init: (Int) -> Ptr): Ptr {
    val bytes = size * Pointer.POINTER_SIZE
    val address = nmalloc(Pointer.POINTER_SIZE, bytes)
    memSet(address, 0, bytes.L)
    for (i in 0 until size)
        memPutAddress(address + i * Pointer.POINTER_SIZE, init(i))
    return address
}

@JvmName("nUtf8Safe")
fun MemoryStack.nUtf8(text: CharSequence?, nullTerminated: Boolean = true): Ptr =
    text?.let { nUtf8(it, nullTerminated) } ?: NULL

fun MemoryStack.nUtf8(text: CharSequence, nullTerminated: Boolean = true): Ptr {
    val length = memLengthUTF8(text, nullTerminated)
    return nmalloc(1, length).also {
        encodeUTF8(text, nullTerminated, it)
    }
}

@JvmName("nAsciiSafe")
fun MemoryStack.nAscii(text: CharSequence?, nullTerminated: Boolean = true): Ptr =
    text?.let { nUtf8(it, nullTerminated) } ?: NULL

fun MemoryStack.nAscii(text: CharSequence, nullTerminated: Boolean = true): Ptr {
    val length = memLengthASCII(text, nullTerminated)
    return nmalloc(1, length).also {
        encodeASCII(text, nullTerminated, it)
    }
}

internal fun encodeUTF8(text: CharSequence, nullTerminated: Boolean, target: Ptr): Int {
    var i = 0
    val len = text.length
    var p = 0

    var c = text[i]

    // ASCII fast path
    while (i < len && c.i < 0x80) {
        UNSAFE.putByte(target + p++, c.b)
        if (++i < len)
            c = text[i]
        else break
    }

    // Slow path
    while (i < len) {
        c = text[i++]
        if (c.toInt() < 0x80)
            UNSAFE.putByte(target + p++, c.b)
        else {
            var cp = c.i
            if (c.i < 0x800) {
                UNSAFE.putByte(target + p++, (0xC0 or (cp shr 6)).b)
            } else {
                if (!c.isHighSurrogate())
                    UNSAFE.putByte(target + p++, (0xE0 or (cp shr 12)).b)
                else {
                    cp = Character.toCodePoint(c, text[i++])

                    UNSAFE.putByte(target + p++, (0xF0 or (cp shr 18)).b)
                    UNSAFE.putByte(target + p++, (0x80 or (cp shr 12 and 0x3F)).b)
                }
                UNSAFE.putByte(target + p++, (0x80 or (cp shr 6 and 0x3F)).b)
            }
            UNSAFE.putByte(target + p++, (0x80 or (cp and 0x3F)).b)
        }
    }

    if (nullTerminated)
        UNSAFE.putByte(target + p++, 0.b) // TODO: did we have a bug here?

    return p
}

internal fun encodeASCII(text: CharSequence, nullTerminated: Boolean, target: Ptr): Int {
    var len = text.length
    for (p in 0 until len)
        UNSAFE.putByte(target + p, text[p].b)
    if (nullTerminated)
        UNSAFE.putByte(target + len++, 0.b)
    return len
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

fun <R> MemoryStack.longAddress(block: (Ptr) -> R): Long {
    val p = nmalloc(8, Long.BYTES)
    block(p)
    return memGetLong(p)
}

fun <R> MemoryStack.longBuffer(block: (LongBuffer) -> R): Long {
    val p = callocLong(1)
    block(p)
    return p[0]
}

fun <R> MemoryStack.pointerAddress(block: (Ptr) -> R): Long = longAddress(block)


infix fun GlfwWindow.createSurface(instance: Instance): VkSurfaceKHR = stak {
    VkSurfaceKHR(stak.longAddress {
        VK_CHECK_RESULT(GLFWVulkan.nglfwCreateWindowSurface(instance.adr, handle.L, NULL, it))
    })
}

class PhysicalDevice_Buffer(val buffer: PointerBuffer, val instance: Instance) {
    val rem: Int
        get() = buffer.rem
    val adr: Adr
        get() = buffer.adr

    operator fun get(index: Int) = PhysicalDevice(buffer[index], instance)
    operator fun set(index: Int, physicalDevice: PhysicalDevice) {
        buffer.put(index, physicalDevice.adr)
    }
}

// TODO others
inline fun MemoryStack.nmallocInt(num: Int = 1) = nmalloc(Int.BYTES, num * Int.BYTES)
inline fun MemoryStack.nmallocLong(num: Int = 1) = nmalloc(Long.BYTES, num * Long.BYTES)

inline fun MemoryStack.nmallocPointer() = nmalloc(Pointer.POINTER_SIZE, Pointer.POINTER_SIZE)
inline fun MemoryStack.allocInt(num: Int = 1): Ptr {
    val bytes = num * Int.BYTES
    return nmalloc(Int.BYTES, bytes).also {
        memSet(address, 0, bytes.L)
    }
}

fun bufferOf(vararg vecs: Vec2): ByteBuffer {
    val buffer = Buffer(vecs.size * Vec2.size)
    for (i in vecs.indices)
        vecs[i].to(buffer, i * Vec2.size)
    return buffer
}

fun VkDeviceSize(buffer: ByteBuffer): VkDeviceSize = VkDeviceSize(buffer.rem)

inline class VkResult_Array(val array: IntArray) {

    operator fun get(index: Int) = VkResult(array[index])
    operator fun set(index: Int, result: VkResult) = array.set(index, result.i)

    val size get() = array.size
    val indices get() = array.indices

    inline fun forEach(action: (VkResult) -> Unit) {
        for (element in array) action(VkResult(element))
    }
}

fun VkResult_Array(size: Int, block: (Int) -> VkResult) = VkResult_Array(IntArray(size) { block(it).i })
fun VkResult_Array(size: Int) = VkResult_Array(IntArray(size))
fun VkResult_Array(elements: Collection<VkResult>) = VkResult_Array(IntArray(elements.size) { elements.elementAt(it).i })
fun VkResult_Array() = VkResult_Array(IntArray(0))

inline class VkPresentModeKHR_Array(val array: IntArray) {

    operator fun get(index: Int) = VkPresentModeKHR(array[index])
    operator fun set(index: Int, result: VkPresentModeKHR) = array.set(index, result.i)

    val size get() = array.size
    val indices get() = array.indices

    inline fun forEach(action: (VkPresentModeKHR) -> Unit) {
        for (element in array) action(VkPresentModeKHR(element))
    }
}

fun VkPresentModeKHR_Array(size: Int, block: (Int) -> VkPresentModeKHR) = VkPresentModeKHR_Array(IntArray(size) { block(it).i })
fun VkPresentModeKHR_Array(size: Int) = VkPresentModeKHR_Array(IntArray(size))
fun VkPresentModeKHR_Array(elements: Collection<VkPresentModeKHR>) = VkPresentModeKHR_Array(IntArray(elements.size) { elements.elementAt(it).i })
fun VkPresentModeKHR_Array() = VkPresentModeKHR_Array(IntArray(0))

fun VkSemaphore_Array.native(stack: MemoryStack): Ptr {
    val p = stack.nmallocInt(size)
    for(i in indices)
        memPutLong(p + Long.BYTES * i, this[i].L)
    return p
}

fun VkImageView_Array.native(stack: MemoryStack): Ptr {
    val p = stack.nmallocInt(size)
    for(i in indices)
        memPutLong(p + Long.BYTES * i, this[i].L)
    return p
}

val VkPresentModeKHR_Buffer.indices: IntRange
    get() = buffer.indices
