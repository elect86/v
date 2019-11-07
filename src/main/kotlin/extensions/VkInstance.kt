package extensions

import classes.DebugReportCallbackCreateInfo
import kool.adr
import org.lwjgl.system.MemoryUtil.NULL
import org.lwjgl.vulkan.EXTDebugReport.nvkCreateDebugReportCallbackEXT
import org.lwjgl.vulkan.VkInstance
import util.longAddress
import vkk.VK_CHECK_RESULT
import vkk.entities.VkDebugReportCallback
import vkk.stak

infix fun VkInstance.createDebugReportCallback(createInfo: DebugReportCallbackCreateInfo): VkDebugReportCallback =
    stak { s ->
        VkDebugReportCallback(s.longAddress {
            VK_CHECK_RESULT(nvkCreateDebugReportCallbackEXT(this, createInfo.run { s.native }.adr, NULL, it))
        })
    }