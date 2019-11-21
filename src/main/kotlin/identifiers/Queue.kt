package identifiers

import classes.SubmitInfo
import kool.Ptr
import kool.adr
import org.lwjgl.system.JNI.callPI
import org.lwjgl.system.JNI.callPPJI
import vkk.VkResult
import vkk.entities.VkFence
import vkk.stak

/** Wraps a Vulkan queue handle.  */
class Queue
/**
 * Creates a `VkQueue` using the specified native handle and device.
 *
 * @param handle the native `VkQueue` handle
 * @param device the device from which the queue was retrieved
 */(
    handle: Ptr,
    /** Returns the device from which this `VkQueue` was retrieved.  */
    val device: Device
) : DispatchableHandleDevice(handle, device.capabilities) {

    // --- [ vkQueueSubmit ] ---
    fun submit(submit: SubmitInfo, fence: VkFence = VkFence.NULL): VkResult = stak { s ->
        VkResult(callPPJI(adr, 1, submit.run { s.native }, fence.L, capabilities.vkQueueSubmit)).apply { check() }
    }

    // --- [ vkQueueWaitIdle ] ---
    fun waitIdle(): VkResult =
        VkResult(callPI(adr, capabilities.vkQueueWaitIdle)).apply { check() }
}