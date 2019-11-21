package identifiers

import classes.*
import glm_.BYTES
import kool.Ptr
import kool.adr
import org.lwjgl.system.APIUtil.apiLog
import org.lwjgl.system.Checks
import org.lwjgl.system.FunctionProvider
import org.lwjgl.system.JNI.*
import org.lwjgl.system.MemoryUtil.*
import org.lwjgl.vulkan.VkFramebufferCreateInfo
import org.lwjgl.vulkan.VkImageViewCreateInfo
import org.lwjgl.vulkan.VkPhysicalDeviceProperties
import util.*
import vkk.VK_CHECK_RESULT
import vkk.VkMemoryMapFlags
import vkk.VkResult
import vkk.entities.*
import vkk.stak

/** Wraps a Vulkan device dispatchable handle. */
class Device(
    handle: Ptr,
    val physicalDevice: PhysicalDevice, ci: DeviceCreateInfo, apiVersion: Int = 0
) : DispatchableHandleDevice(handle, getDeviceCapabilities(handle, physicalDevice, ci, apiVersion)) {

    // --- [ vkAcquireNextImageKHR ] ---
    fun acquireNextImageKHR(swapchain: VkSwapchainKHR, timeout: Long, semaphore: VkSemaphore, fence: VkFence = VkFence(NULL)): Int =
        stak {
            val p = it.nmalloc(Int.BYTES, Int.SIZE_BYTES)
            VK_CHECK_RESULT(
                callPJJJJPI(adr, swapchain.L, timeout, semaphore.L, fence.L, p, capabilities.vkAcquireNextImageKHR)
            )
            memGetInt(p)
        }

    // --- [ vkAllocateCommandBuffers ] ---
    infix fun allocateCommandBuffer(allocateInfo: CommandBufferAllocateInfo): CommandBuffer = stak { s ->
        CommandBuffer(s.pointerAddress {
            callPPPI(adr, allocateInfo.run { s.native }, it, capabilities.vkAllocateCommandBuffers)
        }, this)
    }

    // --- [ vkAllocateMemory ] ---
    infix fun allocateMemory(allocateInfo: MemoryAllocateInfo): VkDeviceMemory = stak { s ->
        VkDeviceMemory(s.longAddress {
            callPPPPI(adr, allocateInfo.run { s.native }, NULL, it, capabilities.vkAllocateMemory)
        })
    }

    // --- [ vkBindBufferMemory ] ---
    fun bindBufferMemory(buffer: VkBuffer, memory: VkDeviceMemory, memoryOffset: VkDeviceSize = VkDeviceSize(0)): VkResult =
        VkResult(callPJJJI(adr, buffer.L, memory.L, memoryOffset.L, capabilities.vkBindBufferMemory)).apply { check() }

    // --- [ vkCreateBuffer ] ---
    infix fun createBuffer(createInfo: BufferCreateInfo): VkBuffer = stak { s ->
        VkBuffer(s.longAddress { callPPPPI(adr, createInfo.run { s.native }, NULL, it, capabilities.vkCreateBuffer) })
    }

    // --- [ vkCreateCommandPool ] ---
    infix fun createCommandPool(createInfo: CommandPoolCreateInfo): VkCommandPool = stak { s ->
        VkCommandPool(s.longAddress { callPPPPI(adr, createInfo.run { s.native }, NULL, it, capabilities.vkCreateCommandPool) })
    }

    // --- [ vkCreateFramebuffer ] ---
    inline fun nCreateFramebuffer(createInfo: Ptr, framebuffer: Ptr): VkResult =
        VkResult(callPPPPI(adr, createInfo, NULL, framebuffer, capabilities.vkCreateFramebuffer))

    infix fun createFramebuffer(createInfo: FramebufferCreateInfo): VkFramebuffer = stak { s ->
        VkFramebuffer(s.longAddress { nCreateFramebuffer(createInfo.run { s.native }, it).check() })
    }

    // JVM
    fun createFramebufferArray(createInfo: FramebufferCreateInfo, imageViews: VkImageView_Array): VkFramebuffer_Array = stak { s ->
        val pCreateInfo = createInfo.run { s.native }
        VkFramebufferCreateInfo.nattachmentCount(pCreateInfo, 1)
        val pAttachment = s.nmallocLong()
        memPutAddress(pCreateInfo + VkFramebufferCreateInfo.PATTACHMENTS, pAttachment)
        val pFramebuffer = s.nmallocLong()
        VkFramebuffer_Array(imageViews.size) { i ->
            memPutLong(pAttachment, imageViews[i].L)
            nCreateFramebuffer(pCreateInfo, pFramebuffer).check()
            VkFramebuffer(memGetLong(pFramebuffer))
        }
    }

    // --- [ vkCreateGraphicsPipelines ] ---
    infix fun createGraphicsPipeline(createInfo: GraphicsPipelineCreateInfo): VkPipeline =
        createGraphicsPipeline(VkPipelineCache.NULL, createInfo)

    fun createGraphicsPipeline(pipelineCache: VkPipelineCache, createInfo: GraphicsPipelineCreateInfo): VkPipeline =
        VkPipeline(stak { s ->
            s.longAddress {
                callPJPPPI(adr, pipelineCache.L, 1, createInfo.run { s.native }, NULL, it, capabilities.vkCreateGraphicsPipelines)
            }
        })

    // --- [ vkCreateImageView ] ---
    inline fun nCreateImageView(createInfo: Ptr, imageView: Ptr): VkResult =
        VkResult(callPPPPI(adr, createInfo, NULL, imageView, capabilities.vkCreateImageView))

    infix fun createImageView(createInfo: ImageViewCreateInfo): VkImageView = stak { s ->
        VkImageView(s.longAddress { nCreateImageView(createInfo.run { s.native }, it).check() })
    }

    // JVM
    fun createImageViewArray(createInfo: ImageViewCreateInfo, images: VkImage_Array): VkImageView_Array = stak { s ->
        val pCreateInfo = createInfo.run { s.native }
        val pImageView = s.nmallocLong()
        VkImageView_Array(images.size) { i ->
            VkImageViewCreateInfo.nimage(pCreateInfo, images[i].L)
            nCreateImageView(pCreateInfo, pImageView)
            VkImageView(memGetLong(pImageView))
        }
    }

    // --- [ vkCreatePipelineLayout ] ---
    infix fun createPipelineLayout(createInfo: PipelineLayoutCreateInfo): VkPipelineLayout = stak { s ->
        VkPipelineLayout(s.longAddress { callPPPPI(adr, createInfo.run { s.native }, NULL, it, capabilities.vkCreatePipelineLayout) })
    }

    // --- [ vkCreateRenderPass ] ---
    infix fun createRenderPass(createInfo: RenderPassCreateInfo): VkRenderPass = stak { s ->
        VkRenderPass(s.longAddress { callPPPPI(adr, createInfo.run { s.native }, NULL, it, capabilities.vkCreateRenderPass) })
    }

    // --- [ vkCreateSemaphore ] ---
    infix fun createSemaphore(createInfo: SemaphoreCreateInfo): VkSemaphore = stak { s ->
        VkSemaphore(s.longAddress { callPPPPI(adr, createInfo.run { s.native }, NULL, it, capabilities.vkCreateSemaphore) })
    }

    // --- [ vkCreateShaderModule ] ---

    infix fun createShaderModule(createInfo: ShaderModuleCreateInfo): VkShaderModule = stak { s ->
        VkShaderModule(s.longAddress { callPPPPI(adr, createInfo.run { s.native }, NULL, it, capabilities.vkCreateShaderModule) })
    }

    // --- [ vkCreateSwapchainKHR ] ---
    infix fun createSwapchainKHR(createInfo: SwapchainCreateInfoKHR): VkSwapchainKHR = stak { s ->
        VkSwapchainKHR(s.longAddress {
            VK_CHECK_RESULT(callPPPPI(adr, createInfo.run { s.native }, NULL, it, capabilities.vkCreateSwapchainKHR))
        })
    }

    // --- [ vkDestroyFramebuffer ] ---
    infix fun destroy(framebuffer: VkFramebuffer) =
        callPJPV(adr, framebuffer.L, NULL, capabilities.vkDestroyFramebuffer)

    // --- [ vkDestroySwapchainKHR ] ---
    infix fun destroy(swapchain: VkSwapchainKHR) =
        callPJPV(adr, swapchain.L, NULL, capabilities.vkDestroySwapchainKHR)

    // --- [ vkGetBufferMemoryRequirements ] ---

    infix fun getBufferMemoryRequirements(buffer: VkBuffer): MemoryRequirements = stak { s ->
        MemoryRequirements.fromNative(s) { callPJPV(adr, buffer.L, it, capabilities.vkGetBufferMemoryRequirements) }
    }

    // --- [ vkGetDeviceQueue ] ---
    fun getQueue(queueFamilyIndex: Int, queueIndex: Int = 0): Queue =
        Queue(stak.pointerAddress { callPPV(adr, queueFamilyIndex, queueIndex, it, capabilities.vkGetDeviceQueue) }, this)


    // --- [ vkGetSwapchainImagesKHR ] ---
    inline fun nGetSwapchainImagesKHR(swapchain: VkSwapchainKHR, pSwapchainImageCount: Ptr, pSwapchainImages: Ptr = NULL): VkResult =
        VkResult(callPJPPI(adr, swapchain.L, pSwapchainImageCount, pSwapchainImages, capabilities.vkGetSwapchainImagesKHR))

    infix fun getSwapchainImagesKHR(swapchain: VkSwapchainKHR): VkImage_Array = stak { s ->
        var pSwapchainImages: Ptr = NULL
        val pSwapchainImageCount = s.nmallocInt()
        var swapchainImageCount = 0
        var result: VkResult
        do {
            result = nGetSwapchainImagesKHR(swapchain, pSwapchainImageCount)
            swapchainImageCount = memGetInt(pSwapchainImageCount)
            if (result == VkResult.SUCCESS && swapchainImageCount != 0) {
                pSwapchainImages = s.nmallocLong(swapchainImageCount)
                result = nGetSwapchainImagesKHR(swapchain, pSwapchainImageCount, pSwapchainImages)
            }
        } while (result == VkResult.INCOMPLETE)
        VkImage_Array(swapchainImageCount) { VkImage(memGetLong(pSwapchainImages + Long.BYTES * it)) }
    }

    // --- [ vkMapMemory ] ---
    fun mapMemory(memory: VkDeviceMemory, offset: VkDeviceSize, size: VkDeviceSize, flags: VkMemoryMapFlags = 0): Ptr =
        stak.pointerAddress { callPJJJPI(adr, memory.L, offset.L, size.L, flags, it, capabilities.vkMapMemory) }

    // --- [ vkUnmapMemory ] ---
    infix fun unmapMemory(memory: VkDeviceMemory) = callPJV(adr, memory.L, capabilities.vkUnmapMemory)

    // JVM custom
    inline fun <R> mappedMemory(
        memory: VkDeviceMemory, offset: VkDeviceSize, size: VkDeviceSize, flags: VkMemoryMapFlags = 0, block: (Ptr) -> R
    ): R =
        block(mapMemory(memory, offset, size, flags)).also {
            unmapMemory(memory)
        }
}

private fun getDeviceCapabilities(handle: Ptr, physicalDevice: PhysicalDevice, ci: DeviceCreateInfo, apiVersion_: Int): CapabilitiesDevice {
    var apiVersion = apiVersion_
    val GetDeviceProcAddr = stak { s ->
        if (apiVersion == 0) {
            val GetPhysicalDeviceProperties = callPPP(
                physicalDevice.instance.adr,
                s.nAscii("vkGetPhysicalDeviceProperties"),
                VK.globalCommands!!.vkGetInstanceProcAddr
            )

            val props = VkPhysicalDeviceProperties.callocStack(s)
            callPPV(physicalDevice.adr, props.adr, GetPhysicalDeviceProperties)
            apiVersion = props.apiVersion()
            if (apiVersion == 0)  // vkGetPhysicalDeviceProperties failed?
                apiVersion = physicalDevice.instance.capabilities.apiVersion
        }

        callPPP(physicalDevice.instance.adr, s.nAscii("vkGetDeviceProcAddr"), VK.globalCommands!!.vkGetInstanceProcAddr)
    }

    return CapabilitiesDevice(FunctionProvider { functionName ->
        callPPP(handle, functionName.adr, GetDeviceProcAddr).also {
            if (it == NULL && Checks.DEBUG_FUNCTIONS)
                apiLog("Failed to locate address for VK device function " + memASCII(functionName))
        }
    }, physicalDevice.capabilities, VK.getEnabledExtensionSet(apiVersion, ci.enabledExtensionNames))
}