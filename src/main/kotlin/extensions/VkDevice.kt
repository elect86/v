package extensions

import classes.*
import kool.adr
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.MemoryUtil.*
import org.lwjgl.system.Pointer
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.VK10.*
import util.longAddress
import util.longBuffer
import vkk.VK_CHECK_RESULT
import vkk.commandBufferCount
import vkk.entities.VkBuffer
import vkk.entities.VkCommandPool
import vkk.entities.VkDeviceMemory
import vkk.entities.VkRenderPass
import vkk.stak
import vkk.vk

inline infix fun <reified T> VkDevice.allocateCommandBuffers(allocateInfo: CommandBufferAllocateInfo): T =
    stak { s ->
        val count = allocateInfo.commandBufferCount
        val pCommandBuffers = s.nmalloc(Pointer.POINTER_SIZE, count * Pointer.POINTER_SIZE)
        VK_CHECK_RESULT(nvkAllocateCommandBuffers(this, allocateInfo.run { s.native }, pCommandBuffers))
        when (T::class) {
            VkCommandBuffer::class -> VkCommandBuffer(memGetLong(pCommandBuffers), this) as T
            Array<VkCommandBuffer>::class -> Array(count) {
                VkCommandBuffer(
                    memGetAddress(pCommandBuffers + Pointer.POINTER_SIZE * it),
                    this
                )
            } as T
            ArrayList::class -> {
                val res = ArrayList<VkCommandBuffer>(count)
                for (i in 0 until count)
                    res += VkCommandBuffer(memGetAddress(pCommandBuffers + Pointer.POINTER_SIZE * i), this)
                res as T
            }
            List::class -> List(count) {
                VkCommandBuffer(
                    memGetAddress(pCommandBuffers + Pointer.POINTER_SIZE * it),
                    this
                )
            } as T
            else -> throw Exception("[VkDevice::allocateCommandBuffers] Invalid T")
        }
    }

infix fun VkDevice.allocateMemory(allocateInfo: MemoryAllocateInfo): VkDeviceMemory = stak { s ->
    VkDeviceMemory(s.longAddress {
        VK_CHECK_RESULT(nvkAllocateMemory(this, allocateInfo.run { s.native }, NULL, it))
    })
}

infix fun VkDevice.createBuffer(createInfo: BufferCreateInfo): VkBuffer = stak { s ->
    VkBuffer(s.longAddress {
        VK_CHECK_RESULT(nvkCreateBuffer(this, createInfo.run { s.native }, NULL, it))
    })
}

infix fun VkDevice.createCommandPool(createInfo: CommandPoolCreateInfo): VkCommandPool = stak { s ->
    VkCommandPool(s.longAddress {
        VK_CHECK_RESULT(
            nvkCreateCommandPool(this, createInfo.run { s.native }, NULL, it)
        )
    })
}

infix fun VkDevice.createRenderPass(createInfo: RenderPassCreateInfo): VkRenderPass = stak { s ->
    VkRenderPass(s.longAddress {
        VK_CHECK_RESULT(
            nvkCreateRenderPass(this, createInfo.run { s.native }, NULL, it)
        )
    })
}

fun VkDevice.getBufferMemoryRequirements(
    buffer: VkBuffer,
    memoryRequirements: MemoryRequirements = MemoryRequirements()
): MemoryRequirements = stak { s ->
    memoryRequirements.run {
        s.native {
            nvkGetBufferMemoryRequirements(this@getBufferMemoryRequirements, buffer.L, it)
        }
    }
}