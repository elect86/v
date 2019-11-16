package identifiers

import kool.Ptr
import org.lwjgl.system.FunctionProvider
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.EXTAcquireXlibDisplay.VK_EXT_ACQUIRE_XLIB_DISPLAY_EXTENSION_NAME
import java.util.*

lateinit var vkCaps: CapabilitiesInstance

/** Defines the capabilities of a Vulkan `VkInstance`.  */
class CapabilitiesInstance internal constructor(
    provider: FunctionProvider,
    /** The Vulkan API version number.  */
    val apiVersion: Int,
    ext: Set<String>,
    deviceExt: Set<String>
) {

    val caps = HashMap<String, Ptr>(79)

    // VK10
//    val vkDestroyInstance: Long
//    val vkEnumeratePhysicalDevices: Long
//    val vkGetPhysicalDeviceFeatures: Long
//    val vkGetPhysicalDeviceFormatProperties: Long
//    val vkGetPhysicalDeviceImageFormatProperties: Long
//    val vkGetPhysicalDeviceProperties: Long
//    val vkGetPhysicalDeviceQueueFamilyProperties: Long
//    val vkGetPhysicalDeviceMemoryProperties: Long
//    val vkCreateDevice: Long
//    val vkEnumerateDeviceExtensionProperties: Long
//    val vkEnumerateDeviceLayerProperties: Long
//    val vkGetPhysicalDeviceSparseImageFormatProperties: Long
//    // VK11
//    val vkEnumeratePhysicalDeviceGroups: Long
//    val vkGetPhysicalDeviceFeatures2: Long
//    val vkGetPhysicalDeviceProperties2: Long
//    val vkGetPhysicalDeviceFormatProperties2: Long
//    val vkGetPhysicalDeviceImageFormatProperties2: Long
//    val vkGetPhysicalDeviceQueueFamilyProperties2: Long
//    val vkGetPhysicalDeviceMemoryProperties2: Long
//    val vkGetPhysicalDeviceSparseImageFormatProperties2: Long
//    val vkGetPhysicalDeviceExternalBufferProperties: Long
//    val vkGetPhysicalDeviceExternalFenceProperties: Long
//    val vkGetPhysicalDeviceExternalSemaphoreProperties: Long
//    // EXT_acquire_xlib_display
//    val vkAcquireXlibDisplayEXT: Long
//    val vkGetRandROutputDisplayEXT: Long
//    // EXT_calibrated_timestamps
//    val vkGetPhysicalDeviceCalibrateableTimeDomainsEXT: Long
//    // EXT_debug_report
//    val vkCreateDebugReportCallbackEXT: Long
//    val vkDestroyDebugReportCallbackEXT: Long
//    val vkDebugReportMessageEXT: Long
//    // EXT_debug_utils
//    val vkCreateDebugUtilsMessengerEXT: Long
//    val vkDestroyDebugUtilsMessengerEXT: Long
//    val vkSubmitDebugUtilsMessageEXT: Long
//    // EXT_direct_mode_display
//    val vkReleaseDisplayEXT: Long
//    // EXT_display_surface_counter
//    val vkGetPhysicalDeviceSurfaceCapabilities2EXT: Long
//    // EXT_full_screen_exclusive
//    val vkGetPhysicalDeviceSurfacePresentModes2EXT: Long
//    // EXT_headless_surface
//    val vkCreateHeadlessSurfaceEXT: Long
//    // EXT_metal_surface
//    val vkCreateMetalSurfaceEXT: Long
//    // EXT_sample_locations
//    val vkGetPhysicalDeviceMultisamplePropertiesEXT: Long
//    // KHR_device_group
//    val vkGetPhysicalDevicePresentRectanglesKHR: Long
//    // KHR_device_group_creation
//    val vkEnumeratePhysicalDeviceGroupsKHR: Long
//    // KHR_display
//    val vkGetPhysicalDeviceDisplayPropertiesKHR: Long
//    val vkGetPhysicalDeviceDisplayPlanePropertiesKHR: Long
//    val vkGetDisplayPlaneSupportedDisplaysKHR: Long
//    val vkGetDisplayModePropertiesKHR: Long
//    val vkCreateDisplayModeKHR: Long
//    val vkGetDisplayPlaneCapabilitiesKHR: Long
//    val vkCreateDisplayPlaneSurfaceKHR: Long
//    // KHR_external_fence_capabilities
//    val vkGetPhysicalDeviceExternalFencePropertiesKHR: Long
//    // KHR_external_memory_capabilities
//    val vkGetPhysicalDeviceExternalBufferPropertiesKHR: Long
//    // KHR_external_semaphore_capabilities
//    val vkGetPhysicalDeviceExternalSemaphorePropertiesKHR: Long
//    // KHR_get_display_properties2
//    val vkGetPhysicalDeviceDisplayProperties2KHR: Long
//    val vkGetPhysicalDeviceDisplayPlaneProperties2KHR: Long
//    val vkGetDisplayModeProperties2KHR: Long
//    val vkGetDisplayPlaneCapabilities2KHR: Long
//    // KHR_get_physical_device_properties2
//    val vkGetPhysicalDeviceFeatures2KHR: Long
//    val vkGetPhysicalDeviceProperties2KHR: Long
//    val vkGetPhysicalDeviceFormatProperties2KHR: Long
//    val vkGetPhysicalDeviceImageFormatProperties2KHR: Long
//    val vkGetPhysicalDeviceQueueFamilyProperties2KHR: Long
//    val vkGetPhysicalDeviceMemoryProperties2KHR: Long
//    val vkGetPhysicalDeviceSparseImageFormatProperties2KHR: Long
//    // KHR_get_surface_capabilities2
//    val vkGetPhysicalDeviceSurfaceCapabilities2KHR: Long
//    val vkGetPhysicalDeviceSurfaceFormats2KHR: Long
//    // KHR_surface
//    val vkDestroySurfaceKHR: Long
//    val vkGetPhysicalDeviceSurfaceSupportKHR: Long
//    val vkGetPhysicalDeviceSurfaceCapabilitiesKHR: Long
//    val vkGetPhysicalDeviceSurfaceFormatsKHR: Long
//    val vkGetPhysicalDeviceSurfacePresentModesKHR: Long
//    // KHR_wayland_surface
//    val vkCreateWaylandSurfaceKHR: Long
//    val vkGetPhysicalDeviceWaylandPresentationSupportKHR: Long
//    // KHR_win32_surface
//    val vkCreateWin32SurfaceKHR: Long
//    val vkGetPhysicalDeviceWin32PresentationSupportKHR: Long
//    // KHR_xlib_surface
//    val vkCreateXlibSurfaceKHR: Long
//    val vkGetPhysicalDeviceXlibPresentationSupportKHR: Long
//    // MVK_macos_surface
//    val vkCreateMacOSSurfaceMVK: Long
//    // NV_cooperative_matrix
//    val vkGetPhysicalDeviceCooperativeMatrixPropertiesNV: Long
//    // NV_coverage_reduction_mode
//    val vkGetPhysicalDeviceSupportedFramebufferMixedSamplesCombinationsNV: Long
//    // NV_external_memory_capabilities
//    val vkGetPhysicalDeviceExternalImageFormatPropertiesNV: Long
//    // NVX_device_generated_commands
//    val vkGetPhysicalDeviceGeneratedCommandsPropertiesNVX: Long
    /** When true, [VK10] is supported.  */
    val Vulkan10: Boolean = checkCapsInstanceVK10(provider, caps, ext)
    /** When true, [VK11] is supported.  */
    val Vulkan11: Boolean = checkCapsInstanceVK11(provider, caps, ext)
    /** When true, [EXTAcquireXlibDisplay] is supported.  */
    val VK_EXT_acquire_xlib_display: Boolean = VK_EXT_ACQUIRE_XLIB_DISPLAY_EXTENSION_NAME in ext && VK.checkExtension(
        VK_EXT_ACQUIRE_XLIB_DISPLAY_EXTENSION_NAME,
        provider.isSupported("vkAcquireXlibDisplayEXT", caps)
                && provider.isSupported("vkGetRandROutputDisplayEXT", caps)
    )
//    /** When true, [EXTDebugReport] is supported.  */
//    val VK_EXT_debug_report: Boolean
//    /** When true, [EXTDebugUtils] is supported.  */
//    val VK_EXT_debug_utils: Boolean
//    /** When true, [EXTDirectModeDisplay] is supported.  */
//    val VK_EXT_direct_mode_display: Boolean
//    /** When true, [EXTDisplaySurfaceCounter] is supported.  */
//    val VK_EXT_display_surface_counter: Boolean
//    /** When true, [EXTHeadlessSurface] is supported.  */
//    val VK_EXT_headless_surface: Boolean
//    /** When true, [EXTMetalSurface] is supported.  */
//    val VK_EXT_metal_surface: Boolean
//    /** When true, [EXTSwapchainColorspace] is supported.  */
//    val VK_EXT_swapchain_colorspace: Boolean
//    /** When true, [EXTValidationFeatures] is supported.  */
//    val VK_EXT_validation_features: Boolean
//    /** When true, [EXTValidationFlags] is supported.  */
//    val VK_EXT_validation_flags: Boolean
//    /** When true, [KHRDeviceGroupCreation] is supported.  */
//    val VK_KHR_device_group_creation: Boolean
//    /** When true, [KHRDisplay] is supported.  */
//    val VK_KHR_display: Boolean
//    /** When true, [KHRExternalFenceCapabilities] is supported.  */
//    val VK_KHR_external_fence_capabilities: Boolean
//    /** When true, [KHRExternalMemoryCapabilities] is supported.  */
//    val VK_KHR_external_memory_capabilities: Boolean
//    /** When true, [KHRExternalSemaphoreCapabilities] is supported.  */
//    val VK_KHR_external_semaphore_capabilities: Boolean
//    /** When true, [KHRGetDisplayProperties2] is supported.  */
//    val VK_KHR_get_display_properties2: Boolean
//    /** When true, [KHRGetPhysicalDeviceProperties2] is supported.  */
//    val VK_KHR_get_physical_device_properties2: Boolean
//    /** When true, [KHRGetSurfaceCapabilities2] is supported.  */
//    val VK_KHR_get_surface_capabilities2: Boolean
//    /** When true, [KHRSurface] is supported.  */
//    val VK_KHR_surface: Boolean
//    /** When true, [KHRSurfaceProtectedCapabilities] is supported.  */
//    val VK_KHR_surface_protected_capabilities: Boolean
//    /** When true, [KHRWaylandSurface] is supported.  */
//    val VK_KHR_wayland_surface: Boolean
//    /** When true, [KHRWin32Surface] is supported.  */
//    val VK_KHR_win32_surface: Boolean
//    /** When true, [KHRXlibSurface] is supported.  */
//    val VK_KHR_xlib_surface: Boolean
//    /** When true, [MVKMacosSurface] is supported.  */
//    val VK_MVK_macos_surface: Boolean
//    /** When true, [NVExternalMemoryCapabilities] is supported.  */
//    val VK_NV_external_memory_capabilities: Boolean

    init {
        ext.contains("VK_EXT_calibrated_timestamps") && VK.checkExtension(
            "VK_EXT_calibrated_timestamps",
            provider.isSupported("vkGetPhysicalDeviceCalibrateableTimeDomainsEXT", caps)
        )
//        VK_EXT_debug_report = EXTDebugReport.checkCapsInstance(provider, caps, ext)
//        VK_EXT_debug_utils = EXTDebugUtils.checkCapsInstance(provider, caps, ext)
//        VK_EXT_direct_mode_display = EXTDirectModeDisplay.checkCapsInstance(provider, caps, ext)
//        VK_EXT_display_surface_counter = EXTDisplaySurfaceCounter.checkCapsInstance(provider, caps, ext)
//        EXTFullScreenExclusive.checkCapsInstance(provider, caps, deviceExt)
//        VK_EXT_headless_surface = EXTHeadlessSurface.checkCapsInstance(provider, caps, ext)
//        VK_EXT_metal_surface = EXTMetalSurface.checkCapsInstance(provider, caps, ext)
//        EXTSampleLocations.checkCapsInstance(provider, caps, deviceExt)
//        VK_EXT_swapchain_colorspace = ext.contains("VK_EXT_swapchain_colorspace")
//        VK_EXT_validation_features = ext.contains("VK_EXT_validation_features")
//        VK_EXT_validation_flags = ext.contains("VK_EXT_validation_flags")
//        KHRDeviceGroup.checkCapsInstance(provider, caps, deviceExt)
//        VK_KHR_device_group_creation = KHRDeviceGroupCreation.checkCapsInstance(provider, caps, ext)
//        VK_KHR_display = KHRDisplay.checkCapsInstance(provider, caps, ext)
//        VK_KHR_external_fence_capabilities = KHRExternalFenceCapabilities.checkCapsInstance(provider, caps, ext)
//        VK_KHR_external_memory_capabilities = KHRExternalMemoryCapabilities.checkCapsInstance(provider, caps, ext)
//        VK_KHR_external_semaphore_capabilities = KHRExternalSemaphoreCapabilities.checkCapsInstance(provider, caps, ext)
//        VK_KHR_get_display_properties2 = KHRGetDisplayProperties2.checkCapsInstance(provider, caps, ext)
//        VK_KHR_get_physical_device_properties2 = KHRGetPhysicalDeviceProperties2.checkCapsInstance(provider, caps, ext)
//        VK_KHR_get_surface_capabilities2 = KHRGetSurfaceCapabilities2.checkCapsInstance(provider, caps, ext)
//        VK_KHR_surface = KHRSurface.checkCapsInstance(provider, caps, ext)
//        VK_KHR_surface_protected_capabilities = ext.contains("VK_KHR_surface_protected_capabilities")
//        KHRSwapchain.checkCapsInstance(provider, caps, deviceExt)
//        VK_KHR_wayland_surface = KHRWaylandSurface.checkCapsInstance(provider, caps, ext)
//        VK_KHR_win32_surface = KHRWin32Surface.checkCapsInstance(provider, caps, ext)
//        VK_KHR_xlib_surface = KHRXlibSurface.checkCapsInstance(provider, caps, ext)
//        VK_MVK_macos_surface = MVKMacosSurface.checkCapsInstance(provider, caps, ext)
//        NVCooperativeMatrix.checkCapsInstance(provider, caps, deviceExt)
//        NVCoverageReductionMode.checkCapsInstance(provider, caps, deviceExt)
//        VK_NV_external_memory_capabilities = NVExternalMemoryCapabilities.checkCapsInstance(provider, caps, ext)
//        NVXDeviceGeneratedCommands.checkCapsInstance(provider, caps, deviceExt)
//        vkDestroyInstance = org.lwjgl.vulkan.VK.get(caps, "vkDestroyInstance")
//        vkEnumeratePhysicalDevices = org.lwjgl.vulkan.VK.get(caps, "vkEnumeratePhysicalDevices")
//        vkGetPhysicalDeviceFeatures = org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceFeatures")
//        vkGetPhysicalDeviceFormatProperties = org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceFormatProperties")
//        vkGetPhysicalDeviceImageFormatProperties =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceImageFormatProperties")
//        vkGetPhysicalDeviceProperties = org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceProperties")
//        vkGetPhysicalDeviceQueueFamilyProperties =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceQueueFamilyProperties")
//        vkGetPhysicalDeviceMemoryProperties = org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceMemoryProperties")
//        vkCreateDevice = org.lwjgl.vulkan.VK.get(caps, "vkCreateDevice")
//        vkEnumerateDeviceExtensionProperties = org.lwjgl.vulkan.VK.get(caps, "vkEnumerateDeviceExtensionProperties")
//        vkEnumerateDeviceLayerProperties = org.lwjgl.vulkan.VK.get(caps, "vkEnumerateDeviceLayerProperties")
//        vkGetPhysicalDeviceSparseImageFormatProperties =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceSparseImageFormatProperties")
//        vkEnumeratePhysicalDeviceGroups = org.lwjgl.vulkan.VK.get(caps, "vkEnumeratePhysicalDeviceGroups")
//        vkGetPhysicalDeviceFeatures2 = org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceFeatures2")
//        vkGetPhysicalDeviceProperties2 = org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceProperties2")
//        vkGetPhysicalDeviceFormatProperties2 = org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceFormatProperties2")
//        vkGetPhysicalDeviceImageFormatProperties2 =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceImageFormatProperties2")
//        vkGetPhysicalDeviceQueueFamilyProperties2 =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceQueueFamilyProperties2")
//        vkGetPhysicalDeviceMemoryProperties2 = org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceMemoryProperties2")
//        vkGetPhysicalDeviceSparseImageFormatProperties2 =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceSparseImageFormatProperties2")
//        vkGetPhysicalDeviceExternalBufferProperties =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceExternalBufferProperties")
//        vkGetPhysicalDeviceExternalFenceProperties =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceExternalFenceProperties")
//        vkGetPhysicalDeviceExternalSemaphoreProperties =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceExternalSemaphoreProperties")
//        vkAcquireXlibDisplayEXT = org.lwjgl.vulkan.VK.get(caps, "vkAcquireXlibDisplayEXT")
//        vkGetRandROutputDisplayEXT = org.lwjgl.vulkan.VK.get(caps, "vkGetRandROutputDisplayEXT")
//        vkGetPhysicalDeviceCalibrateableTimeDomainsEXT =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceCalibrateableTimeDomainsEXT")
//        vkCreateDebugReportCallbackEXT = org.lwjgl.vulkan.VK.get(caps, "vkCreateDebugReportCallbackEXT")
//        vkDestroyDebugReportCallbackEXT = org.lwjgl.vulkan.VK.get(caps, "vkDestroyDebugReportCallbackEXT")
//        vkDebugReportMessageEXT = org.lwjgl.vulkan.VK.get(caps, "vkDebugReportMessageEXT")
//        vkCreateDebugUtilsMessengerEXT = org.lwjgl.vulkan.VK.get(caps, "vkCreateDebugUtilsMessengerEXT")
//        vkDestroyDebugUtilsMessengerEXT = org.lwjgl.vulkan.VK.get(caps, "vkDestroyDebugUtilsMessengerEXT")
//        vkSubmitDebugUtilsMessageEXT = org.lwjgl.vulkan.VK.get(caps, "vkSubmitDebugUtilsMessageEXT")
//        vkReleaseDisplayEXT = org.lwjgl.vulkan.VK.get(caps, "vkReleaseDisplayEXT")
//        vkGetPhysicalDeviceSurfaceCapabilities2EXT =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceSurfaceCapabilities2EXT")
//        vkGetPhysicalDeviceSurfacePresentModes2EXT =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceSurfacePresentModes2EXT")
//        vkCreateHeadlessSurfaceEXT = org.lwjgl.vulkan.VK.get(caps, "vkCreateHeadlessSurfaceEXT")
//        vkCreateMetalSurfaceEXT = org.lwjgl.vulkan.VK.get(caps, "vkCreateMetalSurfaceEXT")
//        vkGetPhysicalDeviceMultisamplePropertiesEXT =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceMultisamplePropertiesEXT")
//        vkGetPhysicalDevicePresentRectanglesKHR =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDevicePresentRectanglesKHR")
//        vkEnumeratePhysicalDeviceGroupsKHR = org.lwjgl.vulkan.VK.get(caps, "vkEnumeratePhysicalDeviceGroupsKHR")
//        vkGetPhysicalDeviceDisplayPropertiesKHR =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceDisplayPropertiesKHR")
//        vkGetPhysicalDeviceDisplayPlanePropertiesKHR =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceDisplayPlanePropertiesKHR")
//        vkGetDisplayPlaneSupportedDisplaysKHR = org.lwjgl.vulkan.VK.get(caps, "vkGetDisplayPlaneSupportedDisplaysKHR")
//        vkGetDisplayModePropertiesKHR = org.lwjgl.vulkan.VK.get(caps, "vkGetDisplayModePropertiesKHR")
//        vkCreateDisplayModeKHR = org.lwjgl.vulkan.VK.get(caps, "vkCreateDisplayModeKHR")
//        vkGetDisplayPlaneCapabilitiesKHR = org.lwjgl.vulkan.VK.get(caps, "vkGetDisplayPlaneCapabilitiesKHR")
//        vkCreateDisplayPlaneSurfaceKHR = org.lwjgl.vulkan.VK.get(caps, "vkCreateDisplayPlaneSurfaceKHR")
//        vkGetPhysicalDeviceExternalFencePropertiesKHR =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceExternalFencePropertiesKHR")
//        vkGetPhysicalDeviceExternalBufferPropertiesKHR =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceExternalBufferPropertiesKHR")
//        vkGetPhysicalDeviceExternalSemaphorePropertiesKHR =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceExternalSemaphorePropertiesKHR")
//        vkGetPhysicalDeviceDisplayProperties2KHR =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceDisplayProperties2KHR")
//        vkGetPhysicalDeviceDisplayPlaneProperties2KHR =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceDisplayPlaneProperties2KHR")
//        vkGetDisplayModeProperties2KHR = org.lwjgl.vulkan.VK.get(caps, "vkGetDisplayModeProperties2KHR")
//        vkGetDisplayPlaneCapabilities2KHR = org.lwjgl.vulkan.VK.get(caps, "vkGetDisplayPlaneCapabilities2KHR")
//        vkGetPhysicalDeviceFeatures2KHR = org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceFeatures2KHR")
//        vkGetPhysicalDeviceProperties2KHR = org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceProperties2KHR")
//        vkGetPhysicalDeviceFormatProperties2KHR =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceFormatProperties2KHR")
//        vkGetPhysicalDeviceImageFormatProperties2KHR =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceImageFormatProperties2KHR")
//        vkGetPhysicalDeviceQueueFamilyProperties2KHR =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceQueueFamilyProperties2KHR")
//        vkGetPhysicalDeviceMemoryProperties2KHR =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceMemoryProperties2KHR")
//        vkGetPhysicalDeviceSparseImageFormatProperties2KHR =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceSparseImageFormatProperties2KHR")
//        vkGetPhysicalDeviceSurfaceCapabilities2KHR =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceSurfaceCapabilities2KHR")
//        vkGetPhysicalDeviceSurfaceFormats2KHR = org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceSurfaceFormats2KHR")
//        vkDestroySurfaceKHR = org.lwjgl.vulkan.VK.get(caps, "vkDestroySurfaceKHR")
//        vkGetPhysicalDeviceSurfaceSupportKHR = org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceSurfaceSupportKHR")
//        vkGetPhysicalDeviceSurfaceCapabilitiesKHR =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceSurfaceCapabilitiesKHR")
//        vkGetPhysicalDeviceSurfaceFormatsKHR = org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceSurfaceFormatsKHR")
//        vkGetPhysicalDeviceSurfacePresentModesKHR =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceSurfacePresentModesKHR")
//        vkCreateWaylandSurfaceKHR = org.lwjgl.vulkan.VK.get(caps, "vkCreateWaylandSurfaceKHR")
//        vkGetPhysicalDeviceWaylandPresentationSupportKHR =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceWaylandPresentationSupportKHR")
//        vkCreateWin32SurfaceKHR = org.lwjgl.vulkan.VK.get(caps, "vkCreateWin32SurfaceKHR")
//        vkGetPhysicalDeviceWin32PresentationSupportKHR =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceWin32PresentationSupportKHR")
//        vkCreateXlibSurfaceKHR = org.lwjgl.vulkan.VK.get(caps, "vkCreateXlibSurfaceKHR")
//        vkGetPhysicalDeviceXlibPresentationSupportKHR =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceXlibPresentationSupportKHR")
//        vkCreateMacOSSurfaceMVK = org.lwjgl.vulkan.VK.get(caps, "vkCreateMacOSSurfaceMVK")
//        vkGetPhysicalDeviceCooperativeMatrixPropertiesNV =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceCooperativeMatrixPropertiesNV")
//        vkGetPhysicalDeviceSupportedFramebufferMixedSamplesCombinationsNV =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceSupportedFramebufferMixedSamplesCombinationsNV")
//        vkGetPhysicalDeviceExternalImageFormatPropertiesNV =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceExternalImageFormatPropertiesNV")
//        vkGetPhysicalDeviceGeneratedCommandsPropertiesNVX =
//            org.lwjgl.vulkan.VK.get(caps, "vkGetPhysicalDeviceGeneratedCommandsPropertiesNVX")
    }
}


fun checkCapsInstanceVK10(provider: FunctionProvider, caps: MutableMap<String, Ptr>, ext: Set<String>): Boolean =
    "Vulkan10" in ext && VK.checkExtension(
        "Vulkan10",
        provider.isSupported("vkDestroyInstance", caps)
                && provider.isSupported("vkEnumeratePhysicalDevices", caps)
                && provider.isSupported("vkGetPhysicalDeviceFeatures", caps)
                && provider.isSupported("vkGetPhysicalDeviceFormatProperties", caps)
                && provider.isSupported("vkGetPhysicalDeviceImageFormatProperties", caps)
                && provider.isSupported("vkGetPhysicalDeviceProperties", caps)
                && provider.isSupported("vkGetPhysicalDeviceQueueFamilyProperties", caps)
                && provider.isSupported("vkGetPhysicalDeviceMemoryProperties", caps)
                && provider.isSupported("vkCreateDevice", caps)
                && provider.isSupported("vkEnumerateDeviceExtensionProperties", caps)
                && provider.isSupported("vkEnumerateDeviceLayerProperties", caps)
                && provider.isSupported("vkGetPhysicalDeviceSparseImageFormatProperties", caps)
    )

fun checkCapsInstanceVK11(provider: FunctionProvider, caps: MutableMap<String, Ptr>, ext: Set<String>): Boolean =
    "Vulkan11" in ext && VK.checkExtension(
        "Vulkan11",
        provider.isSupported("vkEnumeratePhysicalDeviceGroups", caps)
                && provider.isSupported("vkGetPhysicalDeviceFeatures2", caps)
                && provider.isSupported("vkGetPhysicalDeviceProperties2", caps)
                && provider.isSupported("vkGetPhysicalDeviceFormatProperties2", caps)
                && provider.isSupported("vkGetPhysicalDeviceImageFormatProperties2", caps)
                && provider.isSupported("vkGetPhysicalDeviceQueueFamilyProperties2", caps)
                && provider.isSupported("vkGetPhysicalDeviceMemoryProperties2", caps)
                && provider.isSupported("vkGetPhysicalDeviceSparseImageFormatProperties2", caps)
                && provider.isSupported("vkGetPhysicalDeviceExternalBufferProperties", caps)
                && provider.isSupported("vkGetPhysicalDeviceExternalFenceProperties", caps)
                && provider.isSupported("vkGetPhysicalDeviceExternalSemaphoreProperties", caps)
    )