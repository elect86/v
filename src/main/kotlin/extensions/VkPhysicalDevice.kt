package extensions

import classes.DeviceCreateInfo
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkDevice
import org.lwjgl.vulkan.VkPhysicalDevice
import vkk.VK_CHECK_RESULT
import vkk.pointerBuffer
import vkk.stak

infix fun VkPhysicalDevice.createDevice(createInfo: DeviceCreateInfo): VkDevice = stak { s ->
    val vkDeviceCreateInfo = createInfo.run { s.native }
    VkDevice(s.pointerBuffer {
        VK_CHECK_RESULT(VK10.vkCreateDevice(this, vkDeviceCreateInfo, null, it))
    }, this, vkDeviceCreateInfo)
}