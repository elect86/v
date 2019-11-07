package cmon

import classes.InstanceCreateInfo
import kool.PointerBuffer
import kool.adr
import org.lwjgl.PointerBuffer
import org.lwjgl.system.MemoryUtil.NULL
import org.lwjgl.system.NativeType
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkAllocationCallbacks
import org.lwjgl.vulkan.VkInstance
import org.lwjgl.vulkan.VkInstanceCreateInfo
import vkk.VK_CHECK_RESULT
import vkk.pointerBuffer
import vkk.stak

object v {

    infix fun createInstance(createInfo: InstanceCreateInfo): VkInstance = stak { s ->
        val vkInstanceCreateInfo = createInfo.run { s.native }
        val p = s.callocPointer(1)
        VK_CHECK_RESULT(VK10.nvkCreateInstance(vkInstanceCreateInfo.adr, NULL, p.adr))
        VkInstance(p[0], vkInstanceCreateInfo)
    }
}