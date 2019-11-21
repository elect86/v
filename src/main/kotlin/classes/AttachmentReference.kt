package classes

import kool.Ptr
import kool.toFloatBuffer
import org.lwjgl.BufferUtils
import org.lwjgl.system.*
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.VkAttachmentReference.*
import vkk.VkImageLayout
import java.nio.ByteBuffer

/**
 * Structure specifying an attachment reference.
 *
 * <h5>Valid Usage</h5>
 *
 *
 *  * If `attachment` is not [ATTACHMENT_UNUSED][VK10.VK_ATTACHMENT_UNUSED], `layout` **must** not be [IMAGE_LAYOUT_UNDEFINED][VK10.VK_IMAGE_LAYOUT_UNDEFINED] or [IMAGE_LAYOUT_PREINITIALIZED][VK10.VK_IMAGE_LAYOUT_PREINITIALIZED]
 *
 *
 * <h5>Valid Usage (Implicit)</h5>
 *
 *
 *  * `layout` **must** be a valid `VkImageLayout` value
 *
 *
 * <h5>See Also</h5>
 *
 *
 * [VkRenderPassFragmentDensityMapCreateInfoEXT], [VkSubpassDescription]
 *
 * <h3>Member documentation</h3>
 *
 *
 *  * `attachment`  either an integer value identifying an attachment at the corresponding index in [VkRenderPassCreateInfo]`::pAttachments`, or [ATTACHMENT_UNUSED][VK10.VK_ATTACHMENT_UNUSED] to signify that this attachment is not used.
 *  * `layout`  a `VkImageLayout` value specifying the layout the attachment uses during the subpass.
 *
 *
 * <h3>Layout</h3>
 *
 * <pre>`
 * struct VkAttachmentReference {
 * uint32_t attachment;
 * VkImageLayout layout;
 * }`</pre>
 */
class AttachmentReference(
    var attachment: Int,
    var layout: VkImageLayout
) {

    infix fun toPtr(ptr: Ptr) {
        nattachment(ptr, attachment)
        nlayout(ptr, layout.i)
    }

    val MemoryStack.native: Ptr
        get() = ncalloc(ALIGNOF, 1, SIZEOF).also {
            nattachment(it, attachment)
            nlayout(it, layout.i)
        }
}

fun Array<AttachmentReference>.native(stack: MemoryStack): Ptr {
    val natives = stack.ncalloc(ALIGNOF, size, SIZEOF)
    for (i in indices)
        this[i] toPtr (natives + i * SIZEOF)
    return natives
}