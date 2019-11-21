package cmon

import classes.*
import glm_.L
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import identifiers.*
import kool.adr
import kool.free
import kool.rem
import main_.VKUtil.glslToSpirv
import main_.VKUtil.translateVulkanResult
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWWindowSizeCallbackI
import org.lwjgl.system.MemoryUtil.*
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.EXTDebugReport.VK_EXT_DEBUG_REPORT_EXTENSION_NAME
import org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME
import org.lwjgl.vulkan.VK10.*
import uno.glfw.GlfwWindow
import uno.glfw.glfw
import uno.glfw.windowHint.Api
import util.VkDeviceSize
import util.bufferOf
import util.createSurface
import vkk.*
import vkk.entities.*
import java.io.IOException

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
    val setupCommandBuffer = device allocateCommandBuffer cmdBufAllocateInfo
    val queue = device.getQueue(queueFamilyIndex)
    val renderPass = createRenderPass(device, format)
    val renderCommandPool = createCommandPool(device, queueFamilyIndex)
    val (vertexBuffer, pipelineInfo) = createVertices(device, memoryProperties)
    val pipeline = createPipeline(device, renderPass, pipelineInfo)

    val swapchainRecreator = object {

        var dirty = true

        fun run() {
            // Begin the setup command buffer (the one we will use for swapchain/framebuffer creation)
            val cmdBufInfo = CommandBufferBeginInfo()
            setupCommandBuffer.record(cmdBufInfo) {
                val oldChain = Triangle.swapchain
                // Create the swapchain (this will also add a memory barrier to initialize the framebuffer images)
                createSwapChain(
                    device, physicalDevice, surface, oldChain, setupCommandBuffer,
                    Triangle.size, format, colorSpace
                )
            }
            submitCommanBuffer(queue, setupCommandBuffer)
            queue.waitIdle()

            Triangle.framebuffers?.forEach { device destroy it }
//            Triangle.framebuffers = createFramebuffers(device, Triangle.swapchain!!, renderPass.L, Triangle.width, Triangle.height)
//            // Create render command buffers
//            if (Triangle.renderCommandBuffers != null) {
//                vkResetCommandPool(device!!, renderCommandPool.L, VK_FLAGS_NONE)
//            }
//            Triangle.renderCommandBuffers = Triangle.createRenderCommandBuffers(
//                device,
//                renderCommandPool.L,
//                Triangle.framebuffers!!,
//                renderPass.L,
//                Triangle.width,
//                Triangle.height,
//                pipeline,
//                vertices.verticesBuf
//            )

            dirty = false
        }
    }

    // Handle canvas resize
    val windowSizeCallback = GLFWWindowSizeCallbackI { _, width, height ->
        if (width > 0 && height > 0) {
            Triangle.size.put(width, height)
            swapchainRecreator.dirty = true
        }
    }
    glfwSetWindowSizeCallback(window.handle.L, windowSizeCallback)
    glfwShowWindow(window.handle.L)

    // Pre-allocate everything needed in the render loop

    var imageIndex = 0
    var currentBuffer = 0
    var commandBuffer: CommandBuffer? = null
    var swapchain = VkSwapchainKHR.NULL
    var imageAcquiredSemaphore = VkSemaphore.NULL
    var renderCompleteSemaphore = VkSemaphore.NULL

    // Info struct to create a semaphore
    val semaphoreCreateInfo = SemaphoreCreateInfo()

    // Info struct to submit a command buffer which will wait on the semaphore
    val pWaitDstStageMask = memAllocInt(1)
    pWaitDstStageMask.put(0, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
    val submitInfo = SubmitInfo(
        waitSemaphore = imageAcquiredSemaphore,
        waitDstStageMask = VkPipelineStage.COLOR_ATTACHMENT_OUTPUT_BIT.i,
        commandBuffer = commandBuffer,
        signalSemaphore = renderCompleteSemaphore
    )

    // Info struct to present the current swapchain image to the display
    val presentInfo = PresentInfoKHR(renderCompleteSemaphore, swapchain, imageIndex)

    // The render loop
    while (!window.shouldClose) {
        // Handle window messages. Resize events happen exactly here.
        // So it is safe to use the new swapchain images and framebuffers afterwards.
        glfwPollEvents()
        if (swapchainRecreator.dirty)
            swapchainRecreator.run()

        // Create a semaphore to wait for the swapchain to acquire the next image
        imageAcquiredSemaphore = device createSemaphore semaphoreCreateInfo

        // Create a semaphore to wait for the render to complete, before presenting
        renderCompleteSemaphore = device createSemaphore semaphoreCreateInfo

        // Get next image from the swap chain (back/front buffer).
        // This will setup the imageAquiredSemaphore to be signalled when the operation is complete
//        imageIndex = device.acquireNextImageKHR(Triangle.swapchain!!.handle, Triangle.UINT64_MAX, imageAcquiredSemaphore)
//        currentBuffer = imageIndex

        // Select the command buffer for the current framebuffer image/attachment
//        commandBuffer = Triangle.renderCommandBuffers!![currentBuffer]

//        // Submit to the graphics queue
//        err = vkQueueSubmit(queue, submitInfo, VK_NULL_HANDLE)
//        if (err != VK_SUCCESS) {
//            throw AssertionError("Failed to submit render queue: " + translateVulkanResult(err))
//        }
//
//        // Present the current buffer to the swap chain
//        // This will display the image
//        pSwapchains.put(0, Triangle.swapchain!!.swapchainHandle)
//        err = vkQueuePresentKHR(queue, presentInfo)
//        if (err != VK_SUCCESS) {
//            throw AssertionError("Failed to present the swapchain image: " + translateVulkanResult(err))
//        }
//        // Create and submit post present barrier
//        vkQueueWaitIdle(queue)
//
//        // Destroy this semaphore (we will create a new one in the next frame)
//        vkDestroySemaphore(device, pImageAcquiredSemaphore.get(0), null)
//        vkDestroySemaphore(device, pRenderCompleteSemaphore.get(0), null)
//
//        MemoryStack.stackGet().reset()
    }
//    presentInfo.free()
//    memFree(pWaitDstStageMask)
//    submitInfo.free()
//    memFree(pImageAcquiredSemaphore)
//    memFree(pRenderCompleteSemaphore)
//    semaphoreCreateInfo.free()
//    memFree(pSwapchains)
//    memFree(pCommandBuffers)
//
////    vkDestroyDebugReportCallbackEXT(instance, debugCallbackHandle.L, null)
//
////    windowSizeCallback.free()
////    Callbacks.glfwFreeCallbacks(window.handle.L)
//    window.destroy()
//    glfwTerminate()

    // We don't bother disposing of all Vulkan resources.
    // Let the OS process manager take care of it.
}

fun createInstance(requiredExtensions: ArrayList<String>): Instance {
    val appInfo = ApplicationInfo(apiVersion = VK_API_VERSION_1_0)
    requiredExtensions += VK_EXT_DEBUG_REPORT_EXTENSION_NAME
    val enabledLayerNames = listOf("VK_LAYER_LUNARG_standard_validation")
    val createInfo = InstanceCreateInfo(appInfo, enabledLayerNames, requiredExtensions)
    return Instance(createInfo)
}

fun setupDebugging(instance: Instance, flags: VkDebugReportFlagsEXT): VkDebugReportCallback {
    val dbgCreateInfo = DebugReportCallbackCreateInfo(flags)
    return instance createDebugReportCallback dbgCreateInfo
}

fun createDeviceAndGetGraphicsQueueFamily(physicalDevice: PhysicalDevice): Triple<Device, Int, PhysicalDeviceMemoryProperties> {
    val queueProps = physicalDevice.queueFamilyProperties
    var graphicsQueueFamilyIndex = 0
    while (graphicsQueueFamilyIndex < queueProps.size) {
        if (queueProps[graphicsQueueFamilyIndex].queueFlags has VkQueueFlag.GRAPHICS_BIT)
            break
        graphicsQueueFamilyIndex++
    }
    val queuePriority = 0f
    val queueCreateInfo = DeviceQueueCreateInfo(graphicsQueueFamilyIndex, queuePriority = queuePriority)

    val extensions = arrayListOf(VK_KHR_SWAPCHAIN_EXTENSION_NAME)
//    val enabledLayerNames = Triangle.layers

    val deviceCreateInfo = DeviceCreateInfo(queueCreateInfo = queueCreateInfo, enabledExtensionNames = extensions)

    val device = physicalDevice createDevice deviceCreateInfo

    val memoryProperties = physicalDevice.memoryProperties

    return Triple(device, graphicsQueueFamilyIndex, memoryProperties)
}

fun getFormatAndColorSpace(physicalDevice: PhysicalDevice, surface: VkSurfaceKHR): Pair<VkFormat, VkColorSpaceKHR> {
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
    val surfFormats = physicalDevice getSurfaceFormatsKHR surface

    val format = when {
        surfFormats.size == 1 && surfFormats[0].format == VkFormat.UNDEFINED -> VkFormat.B8G8R8A8_UNORM
        else -> surfFormats[0].format
    }
    return format to surfFormats[0].colorSpace
}

fun createCommandPool(device: Device, queueNodeIndex: Int): VkCommandPool {
    val cmdPoolInfo = CommandPoolCreateInfo(VkCommandPoolCreate.RESET_COMMAND_BUFFER_BIT.i, queueNodeIndex)
    return device createCommandPool cmdPoolInfo
}

fun createRenderPass(device: Device, format: VkFormat): VkRenderPass {

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

fun createVertices(device: Device, deviceMemoryProperties: PhysicalDeviceMemoryProperties)
        : Pair<VkBuffer, PipelineVertexInputStateCreateInfo> {

    // The triangle will showup upside-down, because Vulkan does not do proper viewport transformation to
    // account for inverted Y axis between the window coordinate system and clip space/NDC
    val vertexBuffer = bufferOf(
        Vec2(-0.5f, -0.5f),
        Vec2(+0.5f, -0.5f),
        Vec2(+0.0f, +0.5f)
    )

    // Generate vertex buffer
    //  Setup
    val bufInfo = BufferCreateInfo(
        size = VkDeviceSize(vertexBuffer),
        usageFlags = VkBufferUsage.VERTEX_BUFFER_BIT.i
    )
    val verticesBuf = device createBuffer bufInfo

    val memReqs = device getBufferMemoryRequirements verticesBuf
    val (memoryTypeIndex, _) = getMemoryType(
        deviceMemoryProperties,
        memReqs.memoryTypeBits,
        VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT
    )
    val memAlloc = MemoryAllocateInfo(allocationSize = memReqs.size, memoryTypeIndex = memoryTypeIndex)

    val verticesMem = device allocateMemory memAlloc

    device.mappedMemory(verticesMem, VkDeviceSize(0), memAlloc.allocationSize) { data ->
        memCopy(vertexBuffer.adr, data, vertexBuffer.rem.L)
    }
    vertexBuffer.free()
    device.bindBufferMemory(verticesBuf, verticesMem)

    // Binding description
    val bindingDescriptor = VertexInputBindingDescription(
        binding = 0, // <- we bind our vertex buffer to point 0
        stride = 2 * 4,
        inputRate = VkVertexInputRate.VERTEX
    )

    // Attribute descriptions
    // Describes memory layout and shader attribute locations
    val attributeDescriptions = VertexInputAttributeDescription(
        // Location 0 : Position
        binding = 0, // <- binding point used in the VkVertexInputBindingDescription
        location = 0, // <- location in the shader's attribute layout (inside the shader source)
        format = VkFormat.R32G32_SFLOAT
    )

    // Assign to vertex buffer
    return verticesBuf to PipelineVertexInputStateCreateInfo(bindingDescriptor, attributeDescriptions)
}

fun getMemoryType(
    deviceMemoryProperties: PhysicalDeviceMemoryProperties,
    typeBits: Int,
    properties: Int
): Pair<Int, Boolean> {
    var bits = typeBits
    for (i in 0..31) {
        if (bits and 1 == 1)
            if (deviceMemoryProperties.memoryTypes[i].propertyFlags and properties == properties)
                return i to true
        bits = bits shr 1
    }
    return 0 to false
}

fun createPipeline(device: Device, renderPass: VkRenderPass, vi: PipelineVertexInputStateCreateInfo): VkPipeline {

    // Vertex input state
    // Describes the topoloy used with this pipeline
    val inputAssemblyState = PipelineInputAssemblyStateCreateInfo(VkPrimitiveTopology.TRIANGLE_LIST)

    // Rasterization state
    val rasterizationState = PipelineRasterizationStateCreateInfo(
        cullMode = VkCullMode.NONE.i,
        frontFace = VkFrontFace.COUNTER_CLOCKWISE
    )

    // Color blend state
    // Describes blend modes and color masks
    val colorWriteMask = PipelineColorBlendAttachmentState(colorWriteMask = 0xF) // <- RGBA
    val colorBlendState = PipelineColorBlendStateCreateInfo(attachment = colorWriteMask)

    // Viewport state
    val viewportState = PipelineViewportStateCreateInfo(
        viewport = Viewport(0f, 0f, 0f, 0f, 0f, 0f),    // <- one viewport
        scissor = Rect2D(extent = Extent2D(0, 0))
    )                             // <- one scissor rectangle

    // Enable dynamic states
    // Describes the dynamic states to be used with this pipeline
    // Dynamic states can be set even after the pipeline has been created
    // So there is no need to create new pipelines just for changing
    // a viewport's dimensions or a scissor box
    // The dynamic state properties themselves are stored in the command buffer
    val dynamicState = PipelineDynamicStateCreateInfo(VkDynamicState.VIEWPORT, VkDynamicState.SCISSOR)

    // Depth and stencil state
    // Describes depth and stenctil test and compare ops
    val depthStencilState = PipelineDepthStencilStateCreateInfo(
        // No depth test/write and no stencil used
        depthCompareOp = VkCompareOp.ALWAYS,
        front = StencilOpState(compareOp = VkCompareOp.ALWAYS)
    )

    // Multi sampling state
    // No multi sampling used in this example
    val multisampleState = PipelineMultisampleStateCreateInfo()

    // Load shaders
    val shaderStages = arrayOf(
        loadShader(device, "triangle.vert", VkShaderStage.VERTEX_BIT),
        loadShader(device, "triangle.frag", VkShaderStage.FRAGMENT_BIT)
    )

    // Create the pipeline layout that is used to generate the rendering pipelines that
    // are based on this descriptor set layout
    val pipelineLayoutCreateInfo = PipelineLayoutCreateInfo()

    val layout = device createPipelineLayout pipelineLayoutCreateInfo

    // Assign states
    val pipelineCreateInfo = GraphicsPipelineCreateInfo(
        layout = layout, // <- the layout used for this pipeline (NEEDS TO BE SET! even though it is basically empty)
        renderPass = renderPass, // <- renderpass this pipeline is attached to
        vertexInputState = vi,
        inputAssemblyState = inputAssemblyState,
        rasterizationState = rasterizationState,
        colorBlendState = colorBlendState,
        multisampleState = multisampleState,
        viewportState = viewportState,
        depthStencilState = depthStencilState,
        stages = shaderStages,
        dynamicState = dynamicState
    )

    // Create rendering pipeline
    return device createGraphicsPipeline pipelineCreateInfo
}

private fun loadShader(device: Device, classPath: String, stage: VkShaderStage): PipelineShaderStageCreateInfo =
    PipelineShaderStageCreateInfo(
        stage = stage,
        module = loadModule(classPath, device, stage),
        name = "main"
    )

private fun loadModule(classPath: String, device: Device, stage: VkShaderStage): VkShaderModule {
    val shaderCode = glslToSpirv(classPath, stage.i)
    val moduleCreateInfo = ShaderModuleCreateInfo(code = shaderCode, codeSize = shaderCode.rem.L)
    return device createShaderModule moduleCreateInfo
}

fun createSwapChain(
    device: Device, physicalDevice: PhysicalDevice, surface: VkSurfaceKHR, oldSwapChain: VkSwapchainKHR,
    commandBuffer: CommandBuffer, newSize: Vec2i, format: VkFormat, colorSpace: VkColorSpaceKHR
) {
    // Get physical device surface properties and formats
    val surfCaps = physicalDevice getSurfaceCapabilitiesKHR surface

    val presentModes = physicalDevice getSurfacePresentModesKHR surface

    // Try to use mailbox mode. Low latency and non-tearing
    var swapchainPresentMode = VkPresentModeKHR.FIFO
    for (i in presentModes.indices) {
        if (presentModes[i] == VkPresentModeKHR.MAILBOX) {
            swapchainPresentMode = VkPresentModeKHR.MAILBOX
            break
        }
        if (swapchainPresentMode != VkPresentModeKHR.MAILBOX && presentModes[i] == VkPresentModeKHR.IMMEDIATE)
            swapchainPresentMode = VkPresentModeKHR.IMMEDIATE
    }

    // Determine the number of images
    var desiredNumberOfSwapchainImages = surfCaps.minImageCount + 1
    if (surfCaps.maxImageCount in 1 until desiredNumberOfSwapchainImages)
        desiredNumberOfSwapchainImages = surfCaps.maxImageCount

    val currentExtent = surfCaps.currentExtent
    val currentSize = currentExtent.size
    Triangle.size put when {
        currentSize allNotEqual -1 -> currentSize
        else -> newSize
    }

    val preTransform = when {
        surfCaps.supportedTransforms has VkSurfaceTransformKHR.IDENTITY_BIT -> VkSurfaceTransformKHR.IDENTITY_BIT
        else -> surfCaps.currentTransform
    }

    val swapchainCI = SwapchainCreateInfoKHR(
        surface = surface,
        minImageCount = desiredNumberOfSwapchainImages,
        imageFormat = format,
        imageColorSpace = colorSpace,
        imageUsage = VkImageUsage.COLOR_ATTACHMENT_BIT.i,
        preTransform = preTransform,
        imageArrayLayers = 1,
        imageSharingMode = VkSharingMode.EXCLUSIVE,
        presentMode = swapchainPresentMode,
        oldSwapchain = oldSwapChain,
        clipped = true,
        compositeAlpha = VkCompositeAlphaKHR.OPAQUE_BIT,
        imageExtent = Extent2D(Triangle.size)
    )
    Triangle.swapchain = device createSwapchainKHR swapchainCI

    // If we just re-created an existing swapchain, we should destroy the old swapchain at this point.
    // Note: destroying the swapchain also cleans up all its associated presentable images once the platform is done with them.
    if (oldSwapChain.isValid)
        device destroy oldSwapChain

    Triangle.images = device getSwapchainImagesKHR Triangle.swapchain

    val colorAttachmentView = ImageViewCreateInfo(
        format = format,
        viewType = VkImageViewType._2D,
        subresourceRange = ImageSubresourceRange(
            aspectMask = VkImageAspect.COLOR_BIT.i,
            levelCount = 1,
            layerCount = 1)
        )
    Triangle.imageViews = device.createImageViewArray(colorAttachmentView, Triangle.images)
}

fun submitCommanBuffer(queue: Queue, commandBuffer: CommandBuffer) {
    if (commandBuffer.isInvalid) return
    val submitInfo = SubmitInfo(commandBuffer = commandBuffer)
    queue.submit(submitInfo)
}

fun createFramebuffers(
    device: Device,
    swapchain: Triangle.Swapchain,
    renderPass: VkRenderPass,
    size: Vec2i
): VkFramebuffer_Array {
    val fci = FramebufferCreateInfo(
        dimension = Vec3i(size, 1),
        renderPass = renderPass
    )
    // Create a framebuffer for each swapchain image
    return device.createFramebufferArray(fci, swapchain.imageViews)
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
    var swapchain = VkSwapchainKHR.NULL
    var images = VkImage_Array()
    var imageViews = VkImageView_Array()
    var framebuffers: VkFramebuffer_Array? = null
    var size = Vec2i()
    var renderCommandBuffers: Array<CommandBuffer>? = null

    class DeviceAndGraphicsQueueFamily {
        internal var device: VkDevice? = null
        internal var queueFamilyIndex: Int = 0
        internal var memoryProperties: VkPhysicalDeviceMemoryProperties? = null
    }


    class Swapchain {
        var handle = VkSwapchainKHR.NULL
        var images = VkImage_Array()
        var imageViews = VkImageView_Array()
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


    class Vertices {
        internal var verticesBuf: Long = 0
        internal var createInfo: VkPipelineVertexInputStateCreateInfo? = null
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
