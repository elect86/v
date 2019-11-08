package cmon

import classes.InstanceCreateInfo
import kool.adr
import org.lwjgl.system.MemoryUtil.NULL
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkInstance
import vkk.VK_CHECK_RESULT
import vkk.stak

object v {

    infix fun createInstance(createInfo: InstanceCreateInfo): VkInstance = stak { s ->
        val instanceCreateInfo = createInfo.run { s.native }
        val p = s.callocPointer(1)
        VK_CHECK_RESULT(VK10.nvkCreateInstance(instanceCreateInfo.adr, NULL, p.adr))
        VkInstance(p[0], instanceCreateInfo)
    }
}