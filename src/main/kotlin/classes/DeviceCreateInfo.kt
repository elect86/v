package classes

import kool.Ptr
import kool.adr
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.MemoryUtil.*
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.VkDeviceCreateInfo.*
import util.PointerBuffer
import util.native
import vkk.VkDeviceCreateFlags
import vkk.VkStructureType

/**
 * Structure specifying parameters of a newly created device.
 *
 * <h5>Valid Usage</h5>
 *
 *
 *  * The `queueFamilyIndex` member of each element of `pQueueCreateInfos` **must** be unique within `pQueueCreateInfos`, except that two members can share the same `queueFamilyIndex` if one is a protected-capable queue and one is not a protected-capable queue.
 *  * If the `pNext` chain includes a [VkPhysicalDeviceFeatures2] structure, then `pEnabledFeatures` **must** be `NULL`
 *  * `ppEnabledExtensionNames` **must** not contain [VK_AMD_negative_viewport_height][AMDNegativeViewportHeight]
 *
 *
 * <h5>Valid Usage (Implicit)</h5>
 *
 *
 *  * `sType` **must** be [STRUCTURE_TYPE_DEVICE_CREATE_INFO][VK10.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO]
 *  * Each `pNext` member of any structure (including this one) in the `pNext` chain **must** be either `NULL` or a pointer to a valid instance of [VkDeviceGroupDeviceCreateInfo], [VkDeviceMemoryOverallocationCreateInfoAMD], [VkPhysicalDevice16BitStorageFeatures], [VkPhysicalDevice8BitStorageFeaturesKHR], [VkPhysicalDeviceASTCDecodeFeaturesEXT], [VkPhysicalDeviceBlendOperationAdvancedFeaturesEXT], [VkPhysicalDeviceBufferDeviceAddressFeaturesEXT], [VkPhysicalDeviceCoherentMemoryFeaturesAMD], [VkPhysicalDeviceComputeShaderDerivativesFeaturesNV], [VkPhysicalDeviceConditionalRenderingFeaturesEXT], [VkPhysicalDeviceCooperativeMatrixFeaturesNV], [VkPhysicalDeviceCornerSampledImageFeaturesNV], [VkPhysicalDeviceCoverageReductionModeFeaturesNV], [VkPhysicalDeviceDedicatedAllocationImageAliasingFeaturesNV], [VkPhysicalDeviceDepthClipEnableFeaturesEXT], [VkPhysicalDeviceDescriptorIndexingFeaturesEXT], [VkPhysicalDeviceExclusiveScissorFeaturesNV], [VkPhysicalDeviceFeatures2], [VkPhysicalDeviceFragmentDensityMapFeaturesEXT], [VkPhysicalDeviceFragmentShaderBarycentricFeaturesNV], [VkPhysicalDeviceFragmentShaderInterlockFeaturesEXT], [VkPhysicalDeviceHostQueryResetFeaturesEXT], [VkPhysicalDeviceImagelessFramebufferFeaturesKHR], [VkPhysicalDeviceIndexTypeUint8FeaturesEXT], [VkPhysicalDeviceInlineUniformBlockFeaturesEXT], [VkPhysicalDeviceLineRasterizationFeaturesEXT], [VkPhysicalDeviceMemoryPriorityFeaturesEXT], [VkPhysicalDeviceMeshShaderFeaturesNV], [VkPhysicalDeviceMultiviewFeatures], [VkPhysicalDevicePipelineExecutablePropertiesFeaturesKHR], [VkPhysicalDeviceProtectedMemoryFeatures], [VkPhysicalDeviceRepresentativeFragmentTestFeaturesNV], [VkPhysicalDeviceSamplerYcbcrConversionFeatures], [VkPhysicalDeviceScalarBlockLayoutFeaturesEXT], [VkPhysicalDeviceShaderAtomicInt64FeaturesKHR], [VkPhysicalDeviceShaderDemoteToHelperInvocationFeaturesEXT], [VkPhysicalDeviceShaderDrawParametersFeatures], [VkPhysicalDeviceShaderFloat16Int8FeaturesKHR], [VkPhysicalDeviceShaderImageFootprintFeaturesNV], [VkPhysicalDeviceShaderIntegerFunctions2FeaturesINTEL], [VkPhysicalDeviceShaderSMBuiltinsFeaturesNV], [VkPhysicalDeviceShadingRateImageFeaturesNV], [VkPhysicalDeviceSubgroupSizeControlFeaturesEXT], [VkPhysicalDeviceTexelBufferAlignmentFeaturesEXT], [VkPhysicalDeviceTextureCompressionASTCHDRFeaturesEXT], [VkPhysicalDeviceTransformFeedbackFeaturesEXT], [VkPhysicalDeviceUniformBufferStandardLayoutFeaturesKHR], [VkPhysicalDeviceVariablePointersFeatures], [VkPhysicalDeviceVertexAttributeDivisorFeaturesEXT], [VkPhysicalDeviceVulkanMemoryModelFeaturesKHR], or [VkPhysicalDeviceYcbcrImageArraysFeaturesEXT]
 *  * Each `sType` member in the `pNext` chain **must** be unique
 *  * `flags` **must** be 0
 *  * `pQueueCreateInfos` **must** be a valid pointer to an array of `queueCreateInfoCount` valid [VkDeviceQueueCreateInfo] structures
 *  * If `enabledLayerCount` is not 0, `ppEnabledLayerNames` **must** be a valid pointer to an array of `enabledLayerCount` null-terminated UTF-8 strings
 *  * If `enabledExtensionCount` is not 0, `ppEnabledExtensionNames` **must** be a valid pointer to an array of `enabledExtensionCount` null-terminated UTF-8 strings
 *  * If `pEnabledFeatures` is not `NULL`, `pEnabledFeatures` **must** be a valid pointer to a valid [VkPhysicalDeviceFeatures] structure
 *  * `queueCreateInfoCount` **must** be greater than 0
 *
 *
 * <h5>See Also</h5>
 *
 *
 * [VkDeviceQueueCreateInfo], [VkPhysicalDeviceFeatures], [CreateDevice][VK10.vkCreateDevice]
 *
 * <h3>Member documentation</h3>
 *
 *
 *  * `sType`  the type of this structure.
 *  * `pNext`  `NULL` or a pointer to an extension-specific structure.
 *  * `flags`  reserved for future use.
 *  * `queueCreateInfoCount`  the unsigned integer size of the `pQueueCreateInfos` array. Refer to the [Queue Creation](https://www.khronos.org/registry/vulkan/specs/1.1-extensions/html/vkspec.html#devsandqueues-queue-creation) section below for further details.
 *  * `pQueueCreateInfos`  a pointer to an array of [VkDeviceQueueCreateInfo] structures describing the queues that are requested to be created along with the logical device. Refer to the [Queue Creation](https://www.khronos.org/registry/vulkan/specs/1.1-extensions/html/vkspec.html#devsandqueues-queue-creation) section below for further details.
 *  * `enabledLayerCount`  deprecated and ignored.
 *  * `ppEnabledLayerNames`  deprecated and ignored. See [Device Layer Deprecation](https://www.khronos.org/registry/vulkan/specs/1.1-extensions/html/vkspec.html#extendingvulkan-layers-devicelayerdeprecation).
 *  * `enabledExtensionCount`  the number of device extensions to enable.
 *  * `ppEnabledExtensionNames`  a pointer to an array of `enabledExtensionCount` null-terminated UTF-8 strings containing the names of extensions to enable for the created device. See the [Extensions](https://www.khronos.org/registry/vulkan/specs/1.1-extensions/html/vkspec.html#extendingvulkan-extensions) section for further details.
 *  * `pEnabledFeatures`  `NULL` or a pointer to a [VkPhysicalDeviceFeatures] structure that contains boolean indicators of all the features to be enabled. Refer to the [Features](https://www.khronos.org/registry/vulkan/specs/1.1-extensions/html/vkspec.html#features) section for further details.
 *
 *
 * <h3>Layout</h3>
 *
 * <pre>`
 * struct VkDeviceCreateInfo {
 * VkStructureType sType;
 * void const * pNext;
 * VkDeviceCreateFlags flags;
 * uint32_t queueCreateInfoCount;
 * [VkDeviceQueueCreateInfo] const * pQueueCreateInfos;
 * uint32_t enabledLayerCount;
 * char const * const * ppEnabledLayerNames;
 * uint32_t enabledExtensionCount;
 * char const * const * ppEnabledExtensionNames;
 * [VkPhysicalDeviceFeatures] const * pEnabledFeatures;
 * }`</pre>
 */
class DeviceCreateInfo(
    var flags: VkDeviceCreateFlags = 0,
    var queueCreateInfos: Array<DeviceQueueCreateInfo>,
    var enabledExtensionNames: Collection<String>,
    var enabledFeatures: PhysicalDeviceFeatures? = null,
    var next: Ptr = NULL
) {

    val type get() = VkStructureType.DEVICE_CREATE_INFO

    constructor(
        flags: VkDeviceCreateFlags = 0,
        queueCreateInfo: DeviceQueueCreateInfo,
        enabledExtensionNames: Collection<String>,
        enabledFeatures: PhysicalDeviceFeatures? = null,
        next: Ptr = NULL
    ) : this(flags, arrayOf(queueCreateInfo), enabledExtensionNames, enabledFeatures, next)

    val MemoryStack.native: VkDeviceCreateInfo
        get() = callocStack(this)
            .sType(type.i)
            .flags(flags)
            .pNext(next)
            .pQueueCreateInfos(queueCreateInfos.native(this))
            .ppEnabledExtensionNames(PointerBuffer(enabledExtensionNames)).apply {
                memPutAddress(adr + PENABLEDFEATURES, enabledFeatures?.run { native } ?: NULL)
            }
}