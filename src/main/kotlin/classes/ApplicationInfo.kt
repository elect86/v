package classes

import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VkApplicationInfo
import org.lwjgl.vulkan.VkApplicationInfo.callocStack
import vkk.VkStructureType

class ApplicationInfo(
    var applicationName: String? = null,
    var applicationVersion: Int = 0,
    var engineName: String? = null,
    var engineVersion: Int = 0,
    var apiVersion: Int = 0
) {

    val type: VkStructureType
        get() = VkStructureType.APPLICATION_INFO

    /**
     * Copies the specified struct data to this struct.
     *
     * @param src the source struct
     *
     * @return this struct
     */
    operator fun invoke(src: ApplicationInfo): ApplicationInfo {
        applicationName = src.applicationName
        applicationVersion = src.applicationVersion
        engineName = src.engineName
        engineVersion = src.engineVersion
        apiVersion = src.apiVersion
        return this
    }

    val MemoryStack.native: VkApplicationInfo
        get() = callocStack(this)
            .sType(type.i)
            .pApplicationName(UTF8Safe(applicationName))
            .applicationVersion(applicationVersion)
            .pEngineName(UTF8Safe(engineName))
            .engineVersion(engineVersion)
            .apiVersion(apiVersion)
}