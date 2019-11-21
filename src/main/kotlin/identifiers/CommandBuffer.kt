package identifiers

import classes.CommandBufferBeginInfo
import kool.Ptr
import kool.adr
import kool.set
import org.lwjgl.system.JNI.callPI
import org.lwjgl.system.JNI.callPPI
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.MemoryUtil.NULL
import vkk.VkResult
import vkk.stak

/** Wraps a Vulkan command buffer handle.  */
class CommandBuffer
/**
 * Creates a `VkCommandBuffer` using the specified native handle and device.
 *
 * @param handle the native `VkCommandBuffer` handle
 * @param device the device on which the command buffer was created
 */(
    handle: Ptr,
    /** Returns the device on which this `VkCommandBuffer` was created.  */
    val device: Device
) : DispatchableHandleDevice(handle, device.capabilities) {

    val isValid get() = adr != NULL
    val isInvalid get() = adr == NULL

    // --- [ vkBeginCommandBuffer ] ---

    infix fun begin(beginInfo: CommandBufferBeginInfo): VkResult =
        stak { s -> VkResult(callPPI(adr, beginInfo.run { s.native }, capabilities.vkBeginCommandBuffer)) }

    // --- [ vkEndCommandBuffer ] ---
    fun end(): VkResult = VkResult(callPI(adr, capabilities.vkEndCommandBuffer))

    // JVM
    inline fun <R> record(beginInfo: CommandBufferBeginInfo, block: () -> R): R {
        begin(beginInfo)
        return block().also { end() }
    }
}

fun Array<CommandBuffer>.native(stack: MemoryStack): Ptr {
    val pointers = stack.mallocPointer(size)
    for (i in indices)
        pointers[i] = this[i]
    return pointers.adr
}