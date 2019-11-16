package identifiers

import classes.DeviceCreateInfo
import classes.QueueFamilyProperties
import glm_.BYTES
import kool.Adr
import kool.Ptr
import kool.adr
import kool.rem
import org.lwjgl.system.JNI.callPPPPI
import org.lwjgl.system.JNI.callPPPV
import org.lwjgl.system.MemoryUtil.NULL
import org.lwjgl.system.MemoryUtil.memGetInt
import org.lwjgl.vulkan.*
import vkk.VkResult
import vkk.stak

/** Wraps a Vulkan physical device handle.  */
class PhysicalDevice
/**
 * Creates a `VkPhysicalDevice` using the specified native handle and Vulkan instance.
 *
 * @param handle   the native `VkDevice` handle
 * @param instance the Vulkan instance from which the physical device was enumerated
 */(
    handle: Adr,
    /** Returns the Vulkan instance from which this physical device was enumerated.  */
    val instance: Instance
) : Dispatchable(handle, instance.capabilities) {

    // --- [ vkCreateDevice ] ---
    fun nCreateDevice(pCreateInfo: Ptr, pDevice: Ptr): VkResult =
        VkResult(callPPPPI(adr, pCreateInfo, NULL, pDevice, capabilities.vkCreateDevice))

    infix fun createDevice(createInfo: DeviceCreateInfo): VkDevice = stak { s ->
        VkDevice(
            s.pointerAddress { nCreateDevice(createInfo.run { s.native }, it).apply { check() } },
            this,
            createInfo
        )
    }

    // --- [ vkGetPhysicalDeviceQueueFamilyProperties ] ---
    inline fun nGetQueueFamilyProperties(pQueueFamilyPropertyCount: Ptr, pQueueFamilyProperties: Ptr) =
        callPPPV(
            adr,
            pQueueFamilyPropertyCount,
            pQueueFamilyProperties,
            capabilities.vkGetPhysicalDeviceQueueFamilyProperties
        )

    val queueFamilyProperties: MutableList<QueueFamilyProperties>
        get() = stak { s ->
            val pCount = s.nmalloc(Int.BYTES, Int.BYTES)
            nGetQueueFamilyProperties(pCount, NULL)
            val count = memGetInt(pCount)
            val pQueueFamilyProperties = VkQueueFamilyProperties.callocStack(count, s)
            nGetQueueFamilyProperties(pCount, pQueueFamilyProperties.adr)
            MutableList(pQueueFamilyProperties.rem) { i -> QueueFamilyProperties.from(pQueueFamilyProperties[i].adr) }
        }
}