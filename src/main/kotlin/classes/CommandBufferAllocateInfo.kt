package classes

import kool.Ptr
import org.lwjgl.BufferUtils
import org.lwjgl.system.*
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo.*
import vkk.VkCommandBufferLevel
import vkk.VkStructureType
import vkk.entities.VkCommandPool
import java.nio.ByteBuffer

/**
 * Structure specifying the allocation parameters for command buffer object.
 *
 * <h5>Valid Usage</h5>
 *
 *
 *  * `commandBufferCount` **must** be greater than 0
 *
 *
 * <h5>Valid Usage (Implicit)</h5>
 *
 *
 *  * `sType` **must** be [STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO][VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO]
 *  * `pNext` **must** be `NULL`
 *  * `commandPool` **must** be a valid `VkCommandPool` handle
 *  * `level` **must** be a valid `VkCommandBufferLevel` value
 *
 *
 * <h5>See Also</h5>
 *
 *
 * [AllocateCommandBuffers][VK10.vkAllocateCommandBuffers]
 *
 * <h3>Member documentation</h3>
 *
 *
 *  * `sType`  the type of this structure.
 *  * `pNext`  `NULL` or a pointer to an extension-specific structure.
 *  * `commandPool`  the command pool from which the command buffers are allocated.
 *  * `level`  a `VkCommandBufferLevel` value specifying the command buffer level.
 *  * `commandBufferCount`  the number of command buffers to allocate from the pool.
 *
 *
 * <h3>Layout</h3>
 *
 * <pre>`
 * struct VkCommandBufferAllocateInfo {
 * VkStructureType sType;
 * void const * pNext;
 * VkCommandPool commandPool;
 * VkCommandBufferLevel level;
 * uint32_t commandBufferCount;
 * }`</pre>
 */
class CommandBufferAllocateInfo(
    var commandPool: VkCommandPool,
    var level: VkCommandBufferLevel = VkCommandBufferLevel.PRIMARY,
    var commandBufferCount: Int = 1
) {

    val type get() = VkStructureType.COMMAND_BUFFER_ALLOCATE_INFO

    val MemoryStack.native: Ptr
        get() = ncalloc(ALIGNOF, 1, SIZEOF).also {
            nsType(it, type.i)
            ncommandPool(it, commandPool.L)
            nlevel(it, level.i)
            ncommandBufferCount(it, commandBufferCount)
        }
}