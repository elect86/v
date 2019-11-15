package identifiers

import kool.Ptr
import org.lwjgl.system.*
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VK11
import java.util.*

lateinit var vkCaps: CapabilitiesInstance

/** Defines the capabilities of a Vulkan {@code VkInstance}. */
class CapabilitiesInstance(
    provider: FunctionProvider,
    val apiVersion: Int, ext: Set<String>, deviceExt: Set<String>
) {

    val caps = HashMap<String, Ptr>(79)

    val Vulkan10 = checkCapsInstanceVK10(provider, caps, ext)
    val Vulkan11 = checkCapsInstanceVK11(provider, caps, ext)

    // VK10
    val vkDestroyInstance = VK.get(caps, "vkDestroyInstance")
    val vkEnumeratePhysicalDevices = VK.get(caps, "vkEnumeratePhysicalDevices")
    val vkGetPhysicalDeviceFeatures = VK.get(caps, "vkGetPhysicalDeviceFeatures")
    val vkGetPhysicalDeviceFormatProperties = VK.get(caps, "vkGetPhysicalDeviceFormatProperties")
    val vkGetPhysicalDeviceImageFormatProperties = VK.get(caps, "vkGetPhysicalDeviceImageFormatProperties")
    val vkGetPhysicalDeviceProperties = VK.get(caps, "vkGetPhysicalDeviceProperties")
    val vkGetPhysicalDeviceQueueFamilyProperties = VK.get(caps, "vkGetPhysicalDeviceQueueFamilyProperties")
    val vkGetPhysicalDeviceMemoryProperties = VK.get(caps, "vkGetPhysicalDeviceMemoryProperties")
    val vkCreateDevice = VK.get(caps, "vkCreateDevice")
    val vkEnumerateDeviceExtensionProperties = VK.get(caps, "vkEnumerateDeviceExtensionProperties")
    val vkEnumerateDeviceLayerProperties = VK.get(caps, "vkEnumerateDeviceLayerProperties")
    val vkGetPhysicalDeviceSparseImageFormatProperties = VK.get(caps, "vkGetPhysicalDeviceSparseImageFormatProperties")


    // VK11
    val vkEnumeratePhysicalDeviceGroups = VK.get(caps, "vkEnumeratePhysicalDeviceGroups")
    val vkGetPhysicalDeviceFeatures2 = VK.get(caps, "vkGetPhysicalDeviceFeatures2")
    val vkGetPhysicalDeviceProperties2 = VK.get(caps, "vkGetPhysicalDeviceProperties2")
    val vkGetPhysicalDeviceFormatProperties2 = VK.get(caps, "vkGetPhysicalDeviceFormatProperties2")
    val vkGetPhysicalDeviceImageFormatProperties2 = VK.get(caps, "vkGetPhysicalDeviceImageFormatProperties2")
    val vkGetPhysicalDeviceQueueFamilyProperties2 = VK.get(caps, "vkGetPhysicalDeviceQueueFamilyProperties2")
    val vkGetPhysicalDeviceMemoryProperties2 = VK.get(caps, "vkGetPhysicalDeviceMemoryProperties2")
    val vkGetPhysicalDeviceSparseImageFormatProperties2 =
        VK.get(caps, "vkGetPhysicalDeviceSparseImageFormatProperties2")
    val vkGetPhysicalDeviceExternalBufferProperties = VK.get(caps, "vkGetPhysicalDeviceExternalBufferProperties")
    val vkGetPhysicalDeviceExternalFenceProperties = VK.get(caps, "vkGetPhysicalDeviceExternalFenceProperties")
    val vkGetPhysicalDeviceExternalSemaphoreProperties = VK.get(caps, "vkGetPhysicalDeviceExternalSemaphoreProperties")


    // EXT_acquire_xlib_display
    val vkAcquireXlibDisplayEXT = VK.get(caps, "vkAcquireXlibDisplayEXT")
    val vkGetRandROutputDisplayEXT = VK.get(caps, "vkGetRandROutputDisplayEXT")

    // EXT_calibrated_timestamps
    val vkGetPhysicalDeviceCalibrateableTimeDomainsEXT = VK.get(caps, "vkGetPhysicalDeviceCalibrateableTimeDomainsEXT")

    // EXT_debug_report
    val vkCreateDebugReportCallbackEXT = VK.get(caps, "vkCreateDebugReportCallbackEXT")
    val vkDestroyDebugReportCallbackEXT = VK.get(caps, "vkDestroyDebugReportCallbackEXT")
    val vkDebugReportMessageEXT = VK.get(caps, "vkDebugReportMessageEXT")

    // EXT_debug_utils
    val vkCreateDebugUtilsMessengerEXT = VK.get(caps, "vkCreateDebugUtilsMessengerEXT")
    val vkDestroyDebugUtilsMessengerEXT = VK.get(caps, "vkDestroyDebugUtilsMessengerEXT")
    val vkSubmitDebugUtilsMessageEXT = VK.get(caps, "vkSubmitDebugUtilsMessageEXT")

    // EXT_direct_mode_display
    val vkReleaseDisplayEXT = VK.get(caps, "vkReleaseDisplayEXT")


    // EXT_display_surface_counter
    val vkGetPhysicalDeviceSurfaceCapabilities2EXT = VK.get(caps, "vkGetPhysicalDeviceSurfaceCapabilities2EXT")

    // EXT_full_screen_exclusive
    val vkGetPhysicalDeviceSurfacePresentModes2EXT = VK.get(caps, "vkGetPhysicalDeviceSurfacePresentModes2EXT")


    // EXT_headless_surface
    val vkCreateHeadlessSurfaceEXT = VK.get(caps, "vkCreateHeadlessSurfaceEXT")

    // EXT_metal_surface
    val vkCreateMetalSurfaceEXT = VK.get(caps, "vkCreateMetalSurfaceEXT")

    // EXT_sample_locations
    val vkGetPhysicalDeviceMultisamplePropertiesEXT = VK.get(caps, "vkGetPhysicalDeviceMultisamplePropertiesEXT")

    // KHR_device_group
    val vkGetPhysicalDevicePresentRectanglesKHR = VK.get(caps, "vkGetPhysicalDevicePresentRectanglesKHR")

    // KHR_device_group_creation
//    public final long
//    vkEnumeratePhysicalDeviceGroupsKHR
//
//    // KHR_display
//    public final long
//    vkGetPhysicalDeviceDisplayPropertiesKHR,
//    vkGetPhysicalDeviceDisplayPlanePropertiesKHR,
//    vkGetDisplayPlaneSupportedDisplaysKHR,
//    vkGetDisplayModePropertiesKHR,
//    vkCreateDisplayModeKHR,
//    vkGetDisplayPlaneCapabilitiesKHR,
//    vkCreateDisplayPlaneSurfaceKHR
//
//    // KHR_external_fence_capabilities
//    public final long
//    vkGetPhysicalDeviceExternalFencePropertiesKHR
//
//    // KHR_external_memory_capabilities
//    public final long
//    vkGetPhysicalDeviceExternalBufferPropertiesKHR
//
//    // KHR_external_semaphore_capabilities
//    public final long
//    vkGetPhysicalDeviceExternalSemaphorePropertiesKHR
//
//    // KHR_get_display_properties2
//    public final long
//    vkGetPhysicalDeviceDisplayProperties2KHR,
//    vkGetPhysicalDeviceDisplayPlaneProperties2KHR,
//    vkGetDisplayModeProperties2KHR,
//    vkGetDisplayPlaneCapabilities2KHR
//
//    // KHR_get_physical_device_properties2
//    public final long
//    vkGetPhysicalDeviceFeatures2KHR,
//    vkGetPhysicalDeviceProperties2KHR,
//    vkGetPhysicalDeviceFormatProperties2KHR,
//    vkGetPhysicalDeviceImageFormatProperties2KHR,
//    vkGetPhysicalDeviceQueueFamilyProperties2KHR,
//    vkGetPhysicalDeviceMemoryProperties2KHR,
//    vkGetPhysicalDeviceSparseImageFormatProperties2KHR
//
//    // KHR_get_surface_capabilities2
//    public final long
//    vkGetPhysicalDeviceSurfaceCapabilities2KHR,
//    vkGetPhysicalDeviceSurfaceFormats2KHR
//
//    // KHR_surface
//    public final long
//    vkDestroySurfaceKHR,
//    vkGetPhysicalDeviceSurfaceSupportKHR,
//    vkGetPhysicalDeviceSurfaceCapabilitiesKHR,
//    vkGetPhysicalDeviceSurfaceFormatsKHR,
//    vkGetPhysicalDeviceSurfacePresentModesKHR
//
//    // KHR_wayland_surface
//    public final long
//    vkCreateWaylandSurfaceKHR,
//    vkGetPhysicalDeviceWaylandPresentationSupportKHR
//
//    // KHR_win32_surface
//    public final long
//    vkCreateWin32SurfaceKHR,
//    vkGetPhysicalDeviceWin32PresentationSupportKHR
//
//    // KHR_xlib_surface
//    public final long
//    vkCreateXlibSurfaceKHR,
//    vkGetPhysicalDeviceXlibPresentationSupportKHR
//
//    // MVK_macos_surface
//    public final long
//    vkCreateMacOSSurfaceMVK
//
//    // NV_cooperative_matrix
//    public final long
//    vkGetPhysicalDeviceCooperativeMatrixPropertiesNV
//
//    // NV_coverage_reduction_mode
//    public final long
//    vkGetPhysicalDeviceSupportedFramebufferMixedSamplesCombinationsNV
//
//    // NV_external_memory_capabilities
//    public final long
//    vkGetPhysicalDeviceExternalImageFormatPropertiesNV
//
//    // NVX_device_generated_commands
//    public final long
//    vkGetPhysicalDeviceGeneratedCommandsPropertiesNVX
//
//    /** The Vulkan API version number. */
//    public final int apiVersion
//
//    /** When true, {@link VK10} is supported. */
//    public final boolean Vulkan10
//    /** When true, {@link VK11} is supported. */
//    public final boolean Vulkan11
//    /** When true, {@link EXTAcquireXlibDisplay} is supported. */
//    public final boolean VK_EXT_acquire_xlib_display
//    /** When true, {@link EXTDebugReport} is supported. */
//    public final boolean VK_EXT_debug_report
//    /** When true, {@link EXTDebugUtils} is supported. */
//    public final boolean VK_EXT_debug_utils
//    /** When true, {@link EXTDirectModeDisplay} is supported. */
//    public final boolean VK_EXT_direct_mode_display
//    /** When true, {@link EXTDisplaySurfaceCounter} is supported. */
//    public final boolean VK_EXT_display_surface_counter
//    /** When true, {@link EXTHeadlessSurface} is supported. */
//    public final boolean VK_EXT_headless_surface
//    /** When true, {@link EXTMetalSurface} is supported. */
//    public final boolean VK_EXT_metal_surface
//    /** When true, {@link EXTSwapchainColorspace} is supported. */
//    public final boolean VK_EXT_swapchain_colorspace
//    /** When true, {@link EXTValidationFeatures} is supported. */
//    public final boolean VK_EXT_validation_features
//    /** When true, {@link EXTValidationFlags} is supported. */
//    public final boolean VK_EXT_validation_flags
//    /** When true, {@link KHRDeviceGroupCreation} is supported. */
//    public final boolean VK_KHR_device_group_creation
//    /** When true, {@link KHRDisplay} is supported. */
//    public final boolean VK_KHR_display
//    /** When true, {@link KHRExternalFenceCapabilities} is supported. */
//    public final boolean VK_KHR_external_fence_capabilities
//    /** When true, {@link KHRExternalMemoryCapabilities} is supported. */
//    public final boolean VK_KHR_external_memory_capabilities
//    /** When true, {@link KHRExternalSemaphoreCapabilities} is supported. */
//    public final boolean VK_KHR_external_semaphore_capabilities
//    /** When true, {@link KHRGetDisplayProperties2} is supported. */
//    public final boolean VK_KHR_get_display_properties2
//    /** When true, {@link KHRGetPhysicalDeviceProperties2} is supported. */
//    public final boolean VK_KHR_get_physical_device_properties2
//    /** When true, {@link KHRGetSurfaceCapabilities2} is supported. */
//    public final boolean VK_KHR_get_surface_capabilities2
//    /** When true, {@link KHRSurface} is supported. */
//    public final boolean VK_KHR_surface
//    /** When true, {@link KHRSurfaceProtectedCapabilities} is supported. */
//    public final boolean VK_KHR_surface_protected_capabilities
//    /** When true, {@link KHRWaylandSurface} is supported. */
//    public final boolean VK_KHR_wayland_surface
//    /** When true, {@link KHRWin32Surface} is supported. */
//    public final boolean VK_KHR_win32_surface
//    /** When true, {@link KHRXlibSurface} is supported. */
//    public final boolean VK_KHR_xlib_surface
//    /** When true, {@link MVKMacosSurface} is supported. */
//    public final boolean VK_MVK_macos_surface
//    /** When true, {@link NVExternalMemoryCapabilities} is supported. */
//    public final boolean VK_NV_external_memory_capabilities
}

fun checkCapsInstanceVK10(provider: FunctionProvider, caps: MutableMap<String, Ptr>, ext: Set<String>): Boolean =
    "Vulkan10" in ext && VK.checkExtension(
        "Vulkan10",
        VK.isSupported(provider, "vkDestroyInstance", caps)
                && VK.isSupported(provider, "vkEnumeratePhysicalDevices", caps)
                && VK.isSupported(provider, "vkGetPhysicalDeviceFeatures", caps)
                && VK.isSupported(provider, "vkGetPhysicalDeviceFormatProperties", caps)
                && VK.isSupported(provider, "vkGetPhysicalDeviceImageFormatProperties", caps)
                && VK.isSupported(provider, "vkGetPhysicalDeviceProperties", caps)
                && VK.isSupported(provider, "vkGetPhysicalDeviceQueueFamilyProperties", caps)
                && VK.isSupported(provider, "vkGetPhysicalDeviceMemoryProperties", caps)
                && VK.isSupported(provider, "vkCreateDevice", caps)
                && VK.isSupported(provider, "vkEnumerateDeviceExtensionProperties", caps)
                && VK.isSupported(provider, "vkEnumerateDeviceLayerProperties", caps)
                && VK.isSupported(provider, "vkGetPhysicalDeviceSparseImageFormatProperties", caps)
    )

fun checkCapsInstanceVK11(provider: FunctionProvider, caps: MutableMap<String, Ptr>, ext: Set<String>): Boolean =
    "Vulkan11" in ext && VK.checkExtension(
        "Vulkan11",
        VK.isSupported(provider, "vkEnumeratePhysicalDeviceGroups", caps)
                && VK.isSupported(provider, "vkGetPhysicalDeviceFeatures2", caps)
                && VK.isSupported(provider, "vkGetPhysicalDeviceProperties2", caps)
                && VK.isSupported(provider, "vkGetPhysicalDeviceFormatProperties2", caps)
                && VK.isSupported(provider, "vkGetPhysicalDeviceImageFormatProperties2", caps)
                && VK.isSupported(provider, "vkGetPhysicalDeviceQueueFamilyProperties2", caps)
                && VK.isSupported(provider, "vkGetPhysicalDeviceMemoryProperties2", caps)
                && VK.isSupported(provider, "vkGetPhysicalDeviceSparseImageFormatProperties2", caps)
                && VK.isSupported(provider, "vkGetPhysicalDeviceExternalBufferProperties", caps)
                && VK.isSupported(provider, "vkGetPhysicalDeviceExternalFenceProperties", caps)
                && VK.isSupported(provider, "vkGetPhysicalDeviceExternalSemaphoreProperties", caps)
    )