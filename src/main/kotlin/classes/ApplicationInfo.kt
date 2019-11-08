package classes

import kool.Ptr
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil.memPutAddress
import org.lwjgl.vulkan.VkApplicationInfo.*
import util.nUtf8
import vkk.VkStructureType

class ApplicationInfo(
    var applicationName: String? = null,
    var applicationVersion: Int = 0,
    var engineName: String? = null,
    var engineVersion: Int = 0,
    var apiVersion: Int = 0
) {

    val type get() = VkStructureType.APPLICATION_INFO

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

    val MemoryStack.native: Ptr
        get() = ncalloc(ALIGNOF, 1, SIZEOF).also {
            nsType(it, type.i)
            memPutAddress(it + PAPPLICATIONNAME, nUtf8(applicationName))
            napplicationVersion(it, applicationVersion)
            memPutAddress(it + PENGINENAME, nUtf8(engineName))
            nengineVersion(it, engineVersion)
            napiVersion(it, apiVersion)
        }
}