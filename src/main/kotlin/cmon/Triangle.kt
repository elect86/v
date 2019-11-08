package cmon

import classes.*
import extensions.*
import glm_.vec2.Vec2i
import kool.indices
import kool.rem
import kool.reset
import main_.VKUtil.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWWindowSizeCallback
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.MemoryUtil.*
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.EXTDebugReport.VK_EXT_DEBUG_REPORT_EXTENSION_NAME
import org.lwjgl.vulkan.EXTDebugReport.vkDestroyDebugReportCallbackEXT
import org.lwjgl.vulkan.KHRSurface.*
import org.lwjgl.vulkan.KHRSwapchain.*
import org.lwjgl.vulkan.VK10.*
import uno.createSurface
import uno.glfw.GlfwWindow
import uno.glfw.glfw
import uno.glfw.windowHint.Api
import vkk.*
import vkk.entities.VkCommandPool
import vkk.entities.VkDebugReportCallback
import vkk.entities.VkRenderPass
import vkk.entities.VkSurfaceKHR
import vkk.extensionFunctions.*
import vkk.extensionFunctions.createRenderPass
import java.io.IOException
import java.nio.IntBuffer

fun main() {

    glfw.init()
    if (!glfw.vulkanSupported) throw AssertionError("GLFW failed to find the Vulkan loader")

    /* Look for instance extensions */
    val requiredExtensions = glfw.requiredInstanceExtensions
    if (requiredExtensions.isEmpty()) throw AssertionError("Failed to find list of required Vulkan extensions")

    // Create the Vulkan instance
    val instance = createInstance(requiredExtensions)
    DebugReportCallback.callback =
        { _, _, _, _, _, _, message: String, _ -> System.err.println("ERROR OCCURED: $message") }
    val debugCallbackHandle = setupDebugging(instance, VkDebugReport.ERROR_BIT_EXT or VkDebugReport.WARNING_BIT_EXT)
    val physicalDevice = instance.enumeratePhysicalDevices[0]
    val (device, queueFamilyIndex, memoryProperties) = createDeviceAndGetGraphicsQueueFamily(physicalDevice)

    // Create GLFW window
    glfw.windowHint {
        default()
        api = Api.None
        visible = false
    }
    val window = GlfwWindow(Vec2i(800, 600), "GLFW Vulkan Demo")
    window.keyCallback = { key: Int, scanCode: Int, action: Int, mods: Int ->
        if (action == GLFW_RELEASE && key == GLFW_KEY_ESCAPE)
            window.shouldClose = true
    }
    val surface = window createSurface instance

    // Create static Vulkan resources
    val (format, colorSpace) = getFormatAndColorSpace(physicalDevice, surface)
    val commandPool = createCommandPool(device, queueFamilyIndex)
    val cmdBufAllocateInfo = CommandBufferAllocateInfo(commandPool)
    val setupCommandBuffer: VkCommandBuffer = device allocateCommandBuffers cmdBufAllocateInfo
    val queue = device.getQueue(queueFamilyIndex)
    val renderPass = createRenderPass(device, format)
    val renderCommandPool = createCommandPool(device, queueFamilyIndex)
    val vertices = Triangle.createVertices(memoryProperties, device)
    val pipeline = Triangle.createPipeline(device, renderPass.L, vertices.createInfo)

    class SwapchainRecreator {
        var mustRecreate = true
        fun recreate() {
            // Begin the setup command buffer (the one we will use for swapchain/framebuffer creation)
            val cmdBufInfo = VkCommandBufferBeginInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
            var err = vkBeginCommandBuffer(setupCommandBuffer, cmdBufInfo)
            cmdBufInfo.free()
            if (err != VK_SUCCESS) {
                throw AssertionError("Failed to begin setup command buffer: " + translateVulkanResult(err))
            }
            val oldChain = if (Triangle.swapchain != null) Triangle.swapchain!!.swapchainHandle else VK_NULL_HANDLE
            // Create the swapchain (this will also add a memory barrier to initialize the framebuffer images)
            Triangle.swapchain = Triangle.createSwapChain(
                device, physicalDevice, surface.L, oldChain, setupCommandBuffer,
                Triangle.width, Triangle.height, format.i, colorSpace.i
            )
            err = vkEndCommandBuffer(setupCommandBuffer)
            if (err != VK_SUCCESS) {
                throw AssertionError("Failed to end setup command buffer: " + translateVulkanResult(err))
            }
            Triangle.submitCommandBuffer(queue, setupCommandBuffer)
            vkQueueWaitIdle(queue)

            if (Triangle.framebuffers != null) {
                for (i in Triangle.framebuffers!!.indices)
                    vkDestroyFramebuffer(device!!, Triangle.framebuffers!![i], null)
            }
            Triangle.framebuffers =
                Triangle.createFramebuffers(device, Triangle.swapchain!!, renderPass.L, Triangle.width, Triangle.height)
            // Create render command buffers
            if (Triangle.renderCommandBuffers != null) {
                vkResetCommandPool(device!!, renderCommandPool.L, VK_FLAGS_NONE)
            }
            Triangle.renderCommandBuffers = Triangle.createRenderCommandBuffers(
                device,
                renderCommandPool.L,
                Triangle.framebuffers!!,
                renderPass.L,
                Triangle.width,
                Triangle.height,
                pipeline,
                vertices.verticesBuf
            )

            mustRecreate = false
        }
    }

    val swapchainRecreator = SwapchainRecreator()

    // Handle canvas resize
    val windowSizeCallback = object : GLFWWindowSizeCallback() {
        override operator fun invoke(window: Long, width: Int, height: Int) {
            if (width <= 0 || height <= 0)
                return
            Triangle.width = width
            Triangle.height = height
            swapchainRecreator.mustRecreate = true
        }
    }
    glfwSetWindowSizeCallback(window.handle.L, windowSizeCallback)
    glfwShowWindow(window.handle.L)

    // Pre-allocate everything needed in the render loop

    val pImageIndex = memAllocInt(1)
    var currentBuffer = 0
    val pCommandBuffers = memAllocPointer(1)
    val pSwapchains = memAllocLong(1)
    val pImageAcquiredSemaphore = memAllocLong(1)
    val pRenderCompleteSemaphore = memAllocLong(1)

    // Info struct to create a semaphore
    val semaphoreCreateInfo = VkSemaphoreCreateInfo.calloc()
        .sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO)

    // Info struct to submit a command buffer which will wait on the semaphore
    val pWaitDstStageMask = memAllocInt(1)
    pWaitDstStageMask.put(0, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
    val submitInfo = VkSubmitInfo.calloc()
        .sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
        .waitSemaphoreCount(pImageAcquiredSemaphore.remaining())
        .pWaitSemaphores(pImageAcquiredSemaphore)
        .pWaitDstStageMask(pWaitDstStageMask)
        .pCommandBuffers(pCommandBuffers)
        .pSignalSemaphores(pRenderCompleteSemaphore)

    // Info struct to present the current swapchain image to the display
    val presentInfo = VkPresentInfoKHR.calloc()
        .sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
        .pWaitSemaphores(pRenderCompleteSemaphore)
        .swapchainCount(pSwapchains.remaining())
        .pSwapchains(pSwapchains)
        .pImageIndices(pImageIndex)

    // The render loop
    while (!glfwWindowShouldClose(window.handle.L)) {

        // Handle window messages. Resize events happen exactly here.
        // So it is safe to use the new swapchain images and framebuffers afterwards.
        glfwPollEvents()
        if (swapchainRecreator.mustRecreate)
            swapchainRecreator.recreate()

        // Create a semaphore to wait for the swapchain to acquire the next image
        var err = vkCreateSemaphore(device!!, semaphoreCreateInfo, null, pImageAcquiredSemaphore)
        if (err != VK_SUCCESS) {
            throw AssertionError("Failed to create image acquired semaphore: " + translateVulkanResult(err))
        }

        // Create a semaphore to wait for the render to complete, before presenting
        err = vkCreateSemaphore(device!!, semaphoreCreateInfo, null, pRenderCompleteSemaphore)
        if (err != VK_SUCCESS) {
            throw AssertionError("Failed to create render complete semaphore: " + translateVulkanResult(err))
        }

        // Get next image from the swap chain (back/front buffer).
        // This will setup the imageAquiredSemaphore to be signalled when the operation is complete
        err = vkAcquireNextImageKHR(
            device!!,
            Triangle.swapchain!!.swapchainHandle,
            Triangle.UINT64_MAX,
            pImageAcquiredSemaphore.get(0),
            VK_NULL_HANDLE,
            pImageIndex
        )
        currentBuffer = pImageIndex.get(0)
        if (err != VK_SUCCESS) {
            throw AssertionError("Failed to acquire next swapchain image: " + translateVulkanResult(err))
        }

        // Select the command buffer for the current framebuffer image/attachment
        pCommandBuffers.put(0, Triangle.renderCommandBuffers!![currentBuffer])

        // Submit to the graphics queue
        err = vkQueueSubmit(queue, submitInfo, VK_NULL_HANDLE)
        if (err != VK_SUCCESS) {
            throw AssertionError("Failed to submit render queue: " + translateVulkanResult(err))
        }

        // Present the current buffer to the swap chain
        // This will display the image
        pSwapchains.put(0, Triangle.swapchain!!.swapchainHandle)
        err = vkQueuePresentKHR(queue, presentInfo)
        if (err != VK_SUCCESS) {
            throw AssertionError("Failed to present the swapchain image: " + translateVulkanResult(err))
        }
        // Create and submit post present barrier
        vkQueueWaitIdle(queue)

        // Destroy this semaphore (we will create a new one in the next frame)
        vkDestroySemaphore(device!!, pImageAcquiredSemaphore.get(0), null)
        vkDestroySemaphore(device!!, pRenderCompleteSemaphore.get(0), null)

        MemoryStack.stackGet().reset()
    }
    presentInfo.free()
    memFree(pWaitDstStageMask)
    submitInfo.free()
    memFree(pImageAcquiredSemaphore)
    memFree(pRenderCompleteSemaphore)
    semaphoreCreateInfo.free()
    memFree(pSwapchains)
    memFree(pCommandBuffers)

    vkDestroyDebugReportCallbackEXT(instance, debugCallbackHandle.L, null)

//    windowSizeCallback.free()
//    Callbacks.glfwFreeCallbacks(window.handle.L)
    window.destroy()
    glfwTerminate()

    // We don't bother disposing of all Vulkan resources.
    // Let the OS process manager take care of it.
}

fun createInstance(requiredExtensions: ArrayList<String>): VkInstance {
    val appInfo = ApplicationInfo(apiVersion = VK_API_VERSION_1_0)
    requiredExtensions += VK_EXT_DEBUG_REPORT_EXTENSION_NAME
    val enabledLayerNames = listOf("VK_LAYER_LUNARG_standard_validation")
    val createInfo = InstanceCreateInfo(appInfo, enabledLayerNames, requiredExtensions)
    return v createInstance createInfo
}

fun setupDebugging(instance: VkInstance, flags: VkDebugReportFlagsEXT): VkDebugReportCallback {
    val dbgCreateInfo = DebugReportCallbackCreateInfo(flags)
    return instance createDebugReportCallback dbgCreateInfo
}

fun createDeviceAndGetGraphicsQueueFamily(physicalDevice: VkPhysicalDevice): Triple<VkDevice, Int, VkPhysicalDeviceMemoryProperties> {
    val queueProps = physicalDevice.queueFamilyProperties
    var graphicsQueueFamilyIndex = 0
    while (graphicsQueueFamilyIndex < queueProps.rem) {
        if (queueProps[graphicsQueueFamilyIndex].queueFlags has VkQueueFlag.GRAPHICS_BIT)
            break
        graphicsQueueFamilyIndex++
    }
    val queuePriority = 0f
    val queueCreateInfo = DeviceQueueCreateInfo(
        queueFamilyIndex = graphicsQueueFamilyIndex,
        queuePriority = queuePriority
    )

    val extensions = arrayListOf(VK_KHR_SWAPCHAIN_EXTENSION_NAME)
//    val enabledLayerNames = Triangle.layers

    val deviceCreateInfo = DeviceCreateInfo(queueCreateInfo = queueCreateInfo, enabledExtensionNames = extensions)

    val device = physicalDevice createDevice deviceCreateInfo

    val memoryProperties = physicalDevice.memoryProperties

    return Triple(device, graphicsQueueFamilyIndex, memoryProperties)
}

fun getFormatAndColorSpace(physicalDevice: VkPhysicalDevice, surface: VkSurfaceKHR): Pair<VkFormat, VkColorSpaceKHR> {
    val queueProps = physicalDevice.queueFamilyProperties

    // Iterate over each queue to learn whether it supports presenting:
    val supportsPresent = physicalDevice.getSurfaceSupportKHR(queueProps, surface)

    // Search for a graphics and a present queue in the array of queue families, try to find one that supports both
    var graphicsQueueNodeIndex = Integer.MAX_VALUE
    var presentQueueNodeIndex = Integer.MAX_VALUE
    for (i in queueProps.indices) {
        if (queueProps[i].queueFlags has VkQueueFlag.GRAPHICS_BIT) {
            if (graphicsQueueNodeIndex == Integer.MAX_VALUE)
                graphicsQueueNodeIndex = i
            if (supportsPresent[i]) {
                graphicsQueueNodeIndex = i
                presentQueueNodeIndex = i
                break
            }
        }
    }
    if (presentQueueNodeIndex == Integer.MAX_VALUE) {
        // If there's no queue that supports both present and graphics try to find a separate present queue
        for (i in queueProps.indices) {
            if (supportsPresent[i]) {
                presentQueueNodeIndex = i
                break
            }
        }
    }

    // Generate error if could not find both a graphics and a present queue
    if (graphicsQueueNodeIndex == Integer.MAX_VALUE) throw AssertionError("No graphics queue found")
    if (presentQueueNodeIndex == Integer.MAX_VALUE) throw AssertionError("No presentation queue found")
    if (graphicsQueueNodeIndex != presentQueueNodeIndex) throw AssertionError("Presentation queue != graphics queue")

    // Get list of supported formats
    val surfFormats: VkSurfaceFormatKHR.Buffer = physicalDevice.getSurfaceFormatsKHR(surface)

    val format = when {
        surfFormats.rem == 1 && surfFormats[0].format == VkFormat.UNDEFINED -> VkFormat.B8G8R8A8_UNORM
        else -> surfFormats[0].format
    }
    return format to surfFormats[0].colorSpace
}

fun createCommandPool(device: VkDevice, queueNodeIndex: Int): VkCommandPool {
    val cmdPoolInfo = CommandPoolCreateInfo(VkCommandPoolCreate.RESET_COMMAND_BUFFER_BIT.i, queueNodeIndex)
    return device createCommandPool cmdPoolInfo
}

fun createRenderPass(device: VkDevice, format: VkFormat): VkRenderPass {

    val attachment = AttachmentDescription(
        format = format,
        samples = VkSampleCount._1_BIT,
        loadOp = VkAttachmentLoadOp.CLEAR,
        storeOp = VkAttachmentStoreOp.STORE,
        stencilLoadOp = VkAttachmentLoadOp.DONT_CARE,
        stencilStoreOp = VkAttachmentStoreOp.DONT_CARE,
        initialLayout = VkImageLayout.UNDEFINED,
        finalLayout = VkImageLayout.PRESENT_SRC_KHR
    )

    val colorReference = AttachmentReference(
        attachment = 0,
        layout = VkImageLayout.COLOR_ATTACHMENT_OPTIMAL
    )

    val subpass = SubpassDescription(
        pipelineBindPoint = VkPipelineBindPoint.GRAPHICS,
        colorAttachment = colorReference
    )

    val renderPassInfo = RenderPassCreateInfo(attachment = attachment, subpass = subpass)

    return device createRenderPass renderPassInfo
}

object Triangle {

    val validation = true

    val layers = listOf("VK_LAYER_LUNARG_standard_validation")

    /**
     * This is just -1L, but it is nicer as a symbolic constant.
     */
    val UINT64_MAX = -0x1L

    /*
     * All resources that must be reallocated on window resize.
     */
    var swapchain: Swapchain? = null
    var framebuffers: LongArray? = null
    var width: Int = 0
    var height: Int = 0
    var renderCommandBuffers: Array<VkCommandBuffer>? = null

    class DeviceAndGraphicsQueueFamily {
        internal var device: VkDevice? = null
        internal var queueFamilyIndex: Int = 0
        internal var memoryProperties: VkPhysicalDeviceMemoryProperties? = null
    }


    class Swapchain {
        internal var swapchainHandle: Long = 0
        internal var images: LongArray? = null
        internal var imageViews: LongArray? = null
    }

    fun createSwapChain(
        device: VkDevice?,
        physicalDevice: VkPhysicalDevice,
        surface: Long,
        oldSwapChain: Long,
        commandBuffer: VkCommandBuffer,
        newWidth: Int,
        newHeight: Int,
        colorFormat: Int,
        colorSpace: Int
    ): Swapchain {
        var err: Int
        // Get physical device surface properties and formats
        val surfCaps = VkSurfaceCapabilitiesKHR.calloc()
        err = vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice, surface, surfCaps)
        if (err != VK_SUCCESS) {
            throw AssertionError("Failed to get physical device surface capabilities: " + translateVulkanResult(err))
        }

        val pPresentModeCount = memAllocInt(1)
        err = vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, pPresentModeCount, null)
        val presentModeCount = pPresentModeCount.get(0)
        if (err != VK_SUCCESS) {
            throw AssertionError(
                "Failed to get number of physical device surface presentation modes: " + translateVulkanResult(
                    err
                )
            )
        }

        val pPresentModes = memAllocInt(presentModeCount)
        err = vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, pPresentModeCount, pPresentModes)
        memFree(pPresentModeCount)
        if (err != VK_SUCCESS) {
            throw AssertionError(
                "Failed to get physical device surface presentation modes: " + translateVulkanResult(
                    err
                )
            )
        }

        // Try to use mailbox mode. Low latency and non-tearing
        var swapchainPresentMode = VK_PRESENT_MODE_FIFO_KHR
        for (i in 0 until presentModeCount) {
            if (pPresentModes.get(i) == VK_PRESENT_MODE_MAILBOX_KHR) {
                swapchainPresentMode = VK_PRESENT_MODE_MAILBOX_KHR
                break
            }
            if (swapchainPresentMode != VK_PRESENT_MODE_MAILBOX_KHR && pPresentModes.get(i) == VK_PRESENT_MODE_IMMEDIATE_KHR) {
                swapchainPresentMode = VK_PRESENT_MODE_IMMEDIATE_KHR
            }
        }
        memFree(pPresentModes)

        // Determine the number of images
        var desiredNumberOfSwapchainImages = surfCaps.minImageCount() + 1
        if (surfCaps.maxImageCount() > 0 && desiredNumberOfSwapchainImages > surfCaps.maxImageCount()) {
            desiredNumberOfSwapchainImages = surfCaps.maxImageCount()
        }

        val currentExtent = surfCaps.currentExtent()
        val currentWidth = currentExtent.width()
        val currentHeight = currentExtent.height()
        if (currentWidth != -1 && currentHeight != -1) {
            width = currentWidth
            height = currentHeight
        } else {
            width = newWidth
            height = newHeight
        }

        val preTransform: Int
        if (surfCaps.supportedTransforms() and VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR != 0) {
            preTransform = VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR
        } else {
            preTransform = surfCaps.currentTransform()
        }
        surfCaps.free()

        val swapchainCI = VkSwapchainCreateInfoKHR.calloc()
            .sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
            .surface(surface)
            .minImageCount(desiredNumberOfSwapchainImages)
            .imageFormat(colorFormat)
            .imageColorSpace(colorSpace)
            .imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
            .preTransform(preTransform)
            .imageArrayLayers(1)
            .imageSharingMode(VK_SHARING_MODE_EXCLUSIVE)
            .presentMode(swapchainPresentMode)
            .oldSwapchain(oldSwapChain)
            .clipped(true)
            .compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
        swapchainCI.imageExtent()
            .width(width)
            .height(height)
        val pSwapChain = memAllocLong(1)
        err = vkCreateSwapchainKHR(device!!, swapchainCI, null, pSwapChain)
        swapchainCI.free()
        val swapChain = pSwapChain.get(0)
        memFree(pSwapChain)
        if (err != VK_SUCCESS) {
            throw AssertionError("Failed to create swap chain: " + translateVulkanResult(err))
        }

        // If we just re-created an existing swapchain, we should destroy the old swapchain at this point.
        // Note: destroying the swapchain also cleans up all its associated presentable images once the platform is done with them.
        if (oldSwapChain != VK_NULL_HANDLE) {
            vkDestroySwapchainKHR(device!!, oldSwapChain, null)
        }

        val pImageCount = memAllocInt(1)
        err = vkGetSwapchainImagesKHR(device!!, swapChain, pImageCount, null)
        val imageCount = pImageCount.get(0)
        if (err != VK_SUCCESS) {
            throw AssertionError("Failed to get number of swapchain images: " + translateVulkanResult(err))
        }

        val pSwapchainImages = memAllocLong(imageCount)
        err = vkGetSwapchainImagesKHR(device!!, swapChain, pImageCount, pSwapchainImages)
        if (err != VK_SUCCESS) {
            throw AssertionError("Failed to get swapchain images: " + translateVulkanResult(err))
        }
        memFree(pImageCount)

        val images = LongArray(imageCount)
        val imageViews = LongArray(imageCount)
        val pBufferView = memAllocLong(1)
        val colorAttachmentView = VkImageViewCreateInfo.calloc()
            .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
            .format(colorFormat)
            .viewType(VK_IMAGE_VIEW_TYPE_2D)
        colorAttachmentView.subresourceRange()
            .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
            .levelCount(1)
            .layerCount(1)
        for (i in 0 until imageCount) {
            images[i] = pSwapchainImages.get(i)
            colorAttachmentView.image(images[i])
            err = vkCreateImageView(device!!, colorAttachmentView, null, pBufferView)
            imageViews[i] = pBufferView.get(0)
            if (err != VK_SUCCESS) {
                throw AssertionError("Failed to create image view: " + translateVulkanResult(err))
            }
        }
        colorAttachmentView.free()
        memFree(pBufferView)
        memFree(pSwapchainImages)

        val ret = Swapchain()
        ret.images = images
        ret.imageViews = imageViews
        ret.swapchainHandle = swapChain
        return ret
    }


    fun createFramebuffers(
        device: VkDevice?,
        swapchain: Swapchain,
        renderPass: Long,
        width: Int,
        height: Int
    ): LongArray {
        val attachments = memAllocLong(1)
        val fci = VkFramebufferCreateInfo.calloc()
            .sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
            .pAttachments(attachments)
            .height(height)
            .width(width)
            .layers(1)
            .renderPass(renderPass)
        // Create a framebuffer for each swapchain image
        val framebuffers = LongArray(swapchain.images!!.size)
        val pFramebuffer = memAllocLong(1)
        for (i in swapchain.images!!.indices) {
            attachments.put(0, swapchain.imageViews!![i])
            val err = vkCreateFramebuffer(device!!, fci, null, pFramebuffer)
            val framebuffer = pFramebuffer.get(0)
            if (err != VK_SUCCESS) {
                throw AssertionError("Failed to create framebuffer: " + translateVulkanResult(err))
            }
            framebuffers[i] = framebuffer
        }
        memFree(attachments)
        memFree(pFramebuffer)
        fci.free()
        return framebuffers
    }

    fun submitCommandBuffer(queue: VkQueue, commandBuffer: VkCommandBuffer?) {
        if (commandBuffer == null || commandBuffer!!.address() == NULL)
            return
        val submitInfo = VkSubmitInfo.calloc()
            .sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
        val pCommandBuffers = memAllocPointer(1)
            .put(commandBuffer!!)
            .flip()
        submitInfo.pCommandBuffers(pCommandBuffers)
        val err = vkQueueSubmit(queue, submitInfo, VK_NULL_HANDLE)
        memFree(pCommandBuffers)
        submitInfo.free()
        if (err != VK_SUCCESS) {
            throw AssertionError("Failed to submit command buffer: " + translateVulkanResult(err))
        }
    }

    @Throws(IOException::class)
    fun loadShader(classPath: String, device: VkDevice?, stage: Int): Long {
        val shaderCode = glslToSpirv(classPath, stage)
        val err: Int
        val moduleCreateInfo = VkShaderModuleCreateInfo.calloc()
            .sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
            .pCode(shaderCode)
        val pShaderModule = memAllocLong(1)
        err = vkCreateShaderModule(device!!, moduleCreateInfo, null, pShaderModule)
        val shaderModule = pShaderModule.get(0)
        memFree(pShaderModule)
        if (err != VK_SUCCESS) {
            throw AssertionError("Failed to create shader module: " + translateVulkanResult(err))
        }
        return shaderModule
    }

    @Throws(IOException::class)
    fun loadShader(device: VkDevice?, classPath: String, stage: Int): VkPipelineShaderStageCreateInfo {
        return VkPipelineShaderStageCreateInfo.calloc()
            .sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
            .stage(stage)
            .module(loadShader(classPath, device, stage))
            .pName(memUTF8("main"))
    }

    fun getMemoryType(
        deviceMemoryProperties: VkPhysicalDeviceMemoryProperties?,
        typeBits: Int,
        properties: Int,
        typeIndex: IntBuffer
    ): Boolean {
        var bits = typeBits
        for (i in 0..31) {
            if (bits and 1 == 1) {
                if (deviceMemoryProperties!!.memoryTypes(i).propertyFlags() and properties == properties) {
                    typeIndex.put(0, i)
                    return true
                }
            }
            bits = bits shr 1
        }
        return false
    }

    class Vertices {
        internal var verticesBuf: Long = 0
        internal var createInfo: VkPipelineVertexInputStateCreateInfo? = null
    }

    fun createVertices(deviceMemoryProperties: VkPhysicalDeviceMemoryProperties?, device: VkDevice?): Vertices {
        val vertexBuffer = memAlloc(3 * 2 * 4)
        val fb = vertexBuffer.asFloatBuffer()
        // The triangle will showup upside-down, because Vulkan does not do proper viewport transformation to
        // account for inverted Y axis between the window coordinate system and clip space/NDC
        fb.put(-0.5f).put(-0.5f)
        fb.put(0.5f).put(-0.5f)
        fb.put(0.0f).put(0.5f)

        val memAlloc = VkMemoryAllocateInfo.calloc()
            .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
        val memReqs = VkMemoryRequirements.calloc()

        var err: Int

        // Generate vertex buffer
        //  Setup
        val bufInfo = VkBufferCreateInfo.calloc()
            .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
            .size(vertexBuffer.remaining().toLong())
            .usage(VK_BUFFER_USAGE_VERTEX_BUFFER_BIT)
        val pBuffer = memAllocLong(1)
        err = vkCreateBuffer(device!!, bufInfo, null, pBuffer)
        val verticesBuf = pBuffer.get(0)
        memFree(pBuffer)
        bufInfo.free()
        if (err != VK_SUCCESS) {
            throw AssertionError("Failed to create vertex buffer: " + translateVulkanResult(err))
        }

        vkGetBufferMemoryRequirements(device!!, verticesBuf, memReqs)
        memAlloc.allocationSize(memReqs.size())
        val memoryTypeIndex = memAllocInt(1)
        getMemoryType(
            deviceMemoryProperties,
            memReqs.memoryTypeBits(),
            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT,
            memoryTypeIndex
        )
        memAlloc.memoryTypeIndex(memoryTypeIndex.get(0))
        memFree(memoryTypeIndex)
        memReqs.free()

        val pMemory = memAllocLong(1)
        err = vkAllocateMemory(device!!, memAlloc, null, pMemory)
        val verticesMem = pMemory.get(0)
        memFree(pMemory)
        if (err != VK_SUCCESS) {
            throw AssertionError("Failed to allocate vertex memory: " + translateVulkanResult(err))
        }

        val pData = memAllocPointer(1)
        err = vkMapMemory(device!!, verticesMem, 0, memAlloc.allocationSize(), 0, pData)
        memAlloc.free()
        val data = pData.get(0)
        memFree(pData)
        if (err != VK_SUCCESS) {
            throw AssertionError("Failed to map vertex memory: " + translateVulkanResult(err))
        }

        MemoryUtil.memCopy(memAddress(vertexBuffer), data, vertexBuffer.remaining().toLong())
        memFree(vertexBuffer)
        vkUnmapMemory(device!!, verticesMem)
        err = vkBindBufferMemory(device!!, verticesBuf, verticesMem, 0)
        if (err != VK_SUCCESS) {
            throw AssertionError("Failed to bind memory to vertex buffer: " + translateVulkanResult(err))
        }

        // Binding description
        val bindingDescriptor = VkVertexInputBindingDescription.calloc(1)
            .binding(0) // <- we bind our vertex buffer to point 0
            .stride(2 * 4)
            .inputRate(VK_VERTEX_INPUT_RATE_VERTEX)

        // Attribute descriptions
        // Describes memory layout and shader attribute locations
        val attributeDescriptions = VkVertexInputAttributeDescription.calloc(1)
        // Location 0 : Position
        attributeDescriptions.get(0)
            .binding(0) // <- binding point used in the VkVertexInputBindingDescription
            .location(0) // <- location in the shader's attribute layout (inside the shader source)
            .format(VK_FORMAT_R32G32_SFLOAT)
            .offset(0)

        // Assign to vertex buffer
        val vi = VkPipelineVertexInputStateCreateInfo.calloc()
            .sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
            .pVertexBindingDescriptions(bindingDescriptor)
            .pVertexAttributeDescriptions(attributeDescriptions)

        val ret = Vertices()
        ret.createInfo = vi
        ret.verticesBuf = verticesBuf
        return ret
    }

    @Throws(IOException::class)
    fun createPipeline(device: VkDevice?, renderPass: Long, vi: VkPipelineVertexInputStateCreateInfo?): Long {
        var err: Int
        // Vertex input state
        // Describes the topoloy used with this pipeline
        val inputAssemblyState = VkPipelineInputAssemblyStateCreateInfo.calloc()
            .sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
            .topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)

        // Rasterization state
        val rasterizationState = VkPipelineRasterizationStateCreateInfo.calloc()
            .sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
            .polygonMode(VK_POLYGON_MODE_FILL)
            .cullMode(VK_CULL_MODE_NONE)
            .frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE)
            .lineWidth(1.0f)

        // Color blend state
        // Describes blend modes and color masks
        val colorWriteMask = VkPipelineColorBlendAttachmentState.calloc(1)
            .colorWriteMask(0xF) // <- RGBA
        val colorBlendState = VkPipelineColorBlendStateCreateInfo.calloc()
            .sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
            .pAttachments(colorWriteMask)

        // Viewport state
        val viewportState = VkPipelineViewportStateCreateInfo.calloc()
            .sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
            .viewportCount(1) // <- one viewport
            .scissorCount(1) // <- one scissor rectangle

        // Enable dynamic states
        // Describes the dynamic states to be used with this pipeline
        // Dynamic states can be set even after the pipeline has been created
        // So there is no need to create new pipelines just for changing
        // a viewport's dimensions or a scissor box
        val pDynamicStates = memAllocInt(2)
        pDynamicStates.put(VK_DYNAMIC_STATE_VIEWPORT).put(VK_DYNAMIC_STATE_SCISSOR).flip()
        val dynamicState = VkPipelineDynamicStateCreateInfo.calloc()
            // The dynamic state properties themselves are stored in the command buffer
            .sType(VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO)
            .pDynamicStates(pDynamicStates)

        // Depth and stencil state
        // Describes depth and stenctil test and compare ops
        val depthStencilState = VkPipelineDepthStencilStateCreateInfo.calloc()
            // No depth test/write and no stencil used
            .sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO)
            .depthCompareOp(VK_COMPARE_OP_ALWAYS)
        depthStencilState.back()
            .failOp(VK_STENCIL_OP_KEEP)
            .passOp(VK_STENCIL_OP_KEEP)
            .compareOp(VK_COMPARE_OP_ALWAYS)
        depthStencilState.front(depthStencilState.back())

        // Multi sampling state
        // No multi sampling used in this example
        val multisampleState = VkPipelineMultisampleStateCreateInfo.calloc()
            .sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
            .rasterizationSamples(VK_SAMPLE_COUNT_1_BIT)

        // Load shaders
        val shaderStages = VkPipelineShaderStageCreateInfo.calloc(2)
        shaderStages.get(0).set(loadShader(device, "a/triangle.vert", VK_SHADER_STAGE_VERTEX_BIT))
        shaderStages.get(1).set(loadShader(device, "a/triangle.frag", VK_SHADER_STAGE_FRAGMENT_BIT))

        // Create the pipeline layout that is used to generate the rendering pipelines that
        // are based on this descriptor set layout
        val pPipelineLayoutCreateInfo = VkPipelineLayoutCreateInfo.calloc()
            .sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)

        val pPipelineLayout = memAllocLong(1)
        err = vkCreatePipelineLayout(device!!, pPipelineLayoutCreateInfo, null, pPipelineLayout)
        val layout = pPipelineLayout.get(0)
        memFree(pPipelineLayout)
        pPipelineLayoutCreateInfo.free()
        if (err != VK_SUCCESS) {
            throw AssertionError("Failed to create pipeline layout: " + translateVulkanResult(err))
        }

        // Assign states
        val pipelineCreateInfo = VkGraphicsPipelineCreateInfo.calloc(1)
            .sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
            .layout(layout) // <- the layout used for this pipeline (NEEDS TO BE SET! even though it is basically empty)
            .renderPass(renderPass) // <- renderpass this pipeline is attached to
            .pVertexInputState(vi)
            .pInputAssemblyState(inputAssemblyState)
            .pRasterizationState(rasterizationState)
            .pColorBlendState(colorBlendState)
            .pMultisampleState(multisampleState)
            .pViewportState(viewportState)
            .pDepthStencilState(depthStencilState)
            .pStages(shaderStages)
            .pDynamicState(dynamicState)

        // Create rendering pipeline
        val pPipelines = memAllocLong(1)
        err = vkCreateGraphicsPipelines(device!!, VK_NULL_HANDLE, pipelineCreateInfo, null, pPipelines)
        val pipeline = pPipelines.get(0)
        shaderStages.free()
        multisampleState.free()
        depthStencilState.free()
        dynamicState.free()
        memFree(pDynamicStates)
        viewportState.free()
        colorBlendState.free()
        colorWriteMask.free()
        rasterizationState.free()
        inputAssemblyState.free()
        if (err != VK_SUCCESS) {
            throw AssertionError("Failed to create pipeline: " + translateVulkanResult(err))
        }
        return pipeline
    }

    fun createRenderCommandBuffers(
        device: VkDevice?, commandPool: Long, framebuffers: LongArray, renderPass: Long, width: Int, height: Int,
        pipeline: Long, verticesBuf: Long
    ): Array<VkCommandBuffer> {
        // Create the render command buffers (one command buffer per framebuffer image)
        val cmdBufAllocateInfo = VkCommandBufferAllocateInfo.calloc()
            .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
            .commandPool(commandPool)
            .level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
            .commandBufferCount(framebuffers.size)
        val pCommandBuffer = memAllocPointer(framebuffers.size)
        var err = vkAllocateCommandBuffers(device!!, cmdBufAllocateInfo, pCommandBuffer)
        if (err != VK_SUCCESS) {
            throw AssertionError("Failed to allocate render command buffer: " + translateVulkanResult(err))
        }
        val renderCommandBuffers = arrayOfNulls<VkCommandBuffer>(framebuffers.size)
        for (i in framebuffers.indices) {
            renderCommandBuffers[i] = VkCommandBuffer(pCommandBuffer.get(i), device!!)
        }
        memFree(pCommandBuffer)
        cmdBufAllocateInfo.free()

        // Create the command buffer begin structure
        val cmdBufInfo = VkCommandBufferBeginInfo.calloc()
            .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)

        // Specify clear color (cornflower blue)
        val clearValues = VkClearValue.calloc(1)
        clearValues.color()
            .float32(0, 100 / 255.0f)
            .float32(1, 149 / 255.0f)
            .float32(2, 237 / 255.0f)
            .float32(3, 1.0f)

        // Specify everything to begin a render pass
        val renderPassBeginInfo = VkRenderPassBeginInfo.calloc()
            .sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
            .renderPass(renderPass)
            .pClearValues(clearValues)
        val renderArea = renderPassBeginInfo.renderArea()
        renderArea.offset().set(0, 0)
        renderArea.extent().set(width, height)

        for (i in renderCommandBuffers.indices) {
            // Set target frame buffer
            renderPassBeginInfo.framebuffer(framebuffers[i])

            err = vkBeginCommandBuffer(renderCommandBuffers[i], cmdBufInfo)
            if (err != VK_SUCCESS) {
                throw AssertionError("Failed to begin render command buffer: " + translateVulkanResult(err))
            }

            vkCmdBeginRenderPass(renderCommandBuffers[i], renderPassBeginInfo, VK_SUBPASS_CONTENTS_INLINE)

            // Update dynamic viewport state
            val viewport = VkViewport.calloc(1)
                .height(height.toFloat())
                .width(width.toFloat())
                .minDepth(0.0f)
                .maxDepth(1.0f)
            vkCmdSetViewport(renderCommandBuffers[i], 0, viewport)
            viewport.free()

            // Update dynamic scissor state
            val scissor = VkRect2D.calloc(1)
            scissor.extent().set(width, height)
            scissor.offset().set(0, 0)
            vkCmdSetScissor(renderCommandBuffers[i], 0, scissor)
            scissor.free()

            // Bind the rendering pipeline (including the shaders)
            vkCmdBindPipeline(renderCommandBuffers[i], VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline)

            // Bind triangle vertices
            val offsets = memAllocLong(1)
            offsets.put(0, 0L)
            val pBuffers = memAllocLong(1)
            pBuffers.put(0, verticesBuf)
            vkCmdBindVertexBuffers(renderCommandBuffers[i], 0, pBuffers, offsets)
            memFree(pBuffers)
            memFree(offsets)

            // Draw triangle
            vkCmdDraw(renderCommandBuffers[i], 3, 1, 0, 0)

            vkCmdEndRenderPass(renderCommandBuffers[i])

            err = vkEndCommandBuffer(renderCommandBuffers[i])
            if (err != VK_SUCCESS) {
                throw AssertionError("Failed to begin render command buffer: " + translateVulkanResult(err))
            }
        }
        renderPassBeginInfo.free()
        clearValues.free()
        cmdBufInfo.free()
        return renderCommandBuffers.filterNotNull().toTypedArray()
    }
}
