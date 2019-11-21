package classes

import kool.Ptr
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VkRect2D
import org.lwjgl.vulkan.VkRect2D.*
import org.lwjgl.vulkan.VkViewport

/**
 * Structure specifying a two-dimensional subregion.
 *
 * <h3>Member documentation</h3>
 *
 * <ul>
 * <li>{@code offset} &ndash; a {@link VkOffset2D} specifying the rectangle offset.</li>
 * <li>{@code extent} &ndash; a {@link VkExtent2D} specifying the rectangle extent.</li>
 * </ul>
 *
 * <h3>Layout</h3>
 *
 * <pre><code>
 * struct VkRect2D {
 *     {@link VkOffset2D VkOffset2D} offset;
 *     {@link VkExtent2D VkExtent2D} extent;
 * }</code></pre>
 */
class Rect2D(
    var offset: Offset2D = Offset2D(),
    var extent: Extent2D
) {

    fun toPtr(ptr: Ptr) {
        offset toPtr ptr
        extent.toPtr(ptr + EXTENT)
    }
}

fun Array<Rect2D>.native(stack: MemoryStack): Ptr {
    val natives = stack.ncalloc(ALIGNOF, size, SIZEOF)
    for (i in indices)
        this[i].toPtr(natives + i * SIZEOF)
    return natives
}