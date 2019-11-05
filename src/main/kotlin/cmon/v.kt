package cmon

import classes.InstanceCreateInfo
import org.lwjgl.PointerBuffer
import org.lwjgl.system.NativeType
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkAllocationCallbacks
import org.lwjgl.vulkan.VkInstance
import org.lwjgl.vulkan.VkInstanceCreateInfo
import vkk.stak

object v {

    fun createInstance(createInfo: InstanceCreateInfo): VkInstance = stak {
        val nCreateInfo =
        VK10.vkCreateInstance()
    }
}