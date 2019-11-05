package classes

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
}