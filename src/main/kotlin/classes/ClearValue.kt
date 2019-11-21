package classes

import glm_.BYTES
import glm_.asRawIntBits
import glm_.bitsAsFloat
import glm_.vec4.Vec4
import glm_.vec4.Vec4i
import glm_.vec4.Vec4ui
import kool.Ptr
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.MemoryUtil.memPutInt
import org.lwjgl.vulkan.VkAttachmentReference
import org.lwjgl.vulkan.VkClearValue
import org.lwjgl.vulkan.VkClearValue.ALIGNOF
import org.lwjgl.vulkan.VkClearValue.SIZEOF

/**
 * Structure specifying a clear value.
 *
 * <h5>Description</h5>
 *
 * <p>This union is used where part of the API requires either color or depth/stencil clear values, depending on the attachment, and defines the initial clear values in the {@link VkRenderPassBeginInfo} structure.</p>
 *
 * <h5>See Also</h5>
 *
 * <p>{@link VkClearAttachment}, {@link VkClearColorValue}, {@link VkClearDepthStencilValue}, {@link VkRenderPassBeginInfo}</p>
 *
 * <h3>Member documentation</h3>
 *
 * <ul>
 * <li>{@code color} &ndash; specifies the color image clear values to use when clearing a color image or attachment.</li>
 * <li>{@code depthStencil} &ndash; specifies the depth and stencil clear values to use when clearing a depth/stencil image or attachment.</li>
 * </ul>
 *
 * <h3>Layout</h3>
 *
 * <pre><code>
 * union VkClearValue {
 *     {@link VkClearColorValue VkClearColorValue} color;
 *     {@link VkClearDepthStencilValue VkClearDepthStencilValue} depthStencil;
 * }</code></pre>
 */
class ClearValue(
    val union: IntArray = IntArray(4)
) {
    constructor(r: Float, g: Float, b: Float, a: Float) :
            this(intArrayOf(r.asRawIntBits, g.asRawIntBits, b.asRawIntBits, a.asRawIntBits))

    var colorVec4: Vec4
        get() = Vec4 { union[it].bitsAsFloat }
        set(value) {
            for (i in 0..3)
                union[i] = value[i].asRawIntBits
        }

    var colorVec4i: Vec4i
        get() = Vec4i(union)
        set(value) {
            value to union
        }

    var colorVec4ui: Vec4ui
        get() = Vec4ui(union)
        set(value) {
            value to union
        }

    var depth: Float
        get() = union[0].bitsAsFloat
        set(value) {
            union[0] = value.asRawIntBits
        }

    var stencil: Int
        get() = union[0]
        set(value) {
            union[0] = value
        }

    infix fun toPtr(ptr: Ptr) {
        memPutInt(ptr, union[0])
        memPutInt(ptr + Int.BYTES, union[1])
        memPutInt(ptr + Int.BYTES * 2, union[2])
        memPutInt(ptr + Int.BYTES * 3, union[3])
    }
}

fun Array<ClearValue>.native(stack: MemoryStack): Ptr {
    val natives = stack.ncalloc(ALIGNOF, size, SIZEOF)
    for (i in indices)
        this[i] toPtr (natives + i * SIZEOF)
    return natives
}