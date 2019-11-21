package classes

import kool.Ptr
import kool.toBuffer
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil.memPutAddress
import org.lwjgl.vulkan.VkSpecializationInfo.*

/**
 * Structure specifying specialization info.
 *
 * <h5>Description</h5>
 *
 * <p>{@code pMapEntries} points to a structure of type {@link VkSpecializationMapEntry}.</p>
 *
 * <h5>Valid Usage</h5>
 *
 * <ul>
 * <li>The {@code offset} member of each element of {@code pMapEntries} <b>must</b> be less than {@code dataSize}</li>
 * <li>The {@code size} member of each element of {@code pMapEntries} <b>must</b> be less than or equal to {@code dataSize} minus {@code offset}</li>
 * </ul>
 *
 * <h5>Valid Usage (Implicit)</h5>
 *
 * <ul>
 * <li>If {@code mapEntryCount} is not 0, {@code pMapEntries} <b>must</b> be a valid pointer to an array of {@code mapEntryCount} valid {@link VkSpecializationMapEntry} structures</li>
 * <li>If {@code dataSize} is not 0, {@code pData} <b>must</b> be a valid pointer to an array of {@code dataSize} bytes</li>
 * </ul>
 *
 * <h5>See Also</h5>
 *
 * <p>{@link VkPipelineShaderStageCreateInfo}, {@link VkSpecializationMapEntry}</p>
 *
 * <h3>Member documentation</h3>
 *
 * <ul>
 * <li>{@code mapEntryCount} &ndash; the number of entries in the {@code pMapEntries} array.</li>
 * <li>{@code pMapEntries} &ndash; a pointer to an array of {@link VkSpecializationMapEntry} which maps constant IDs to offsets in {@code pData}.</li>
 * <li>{@code dataSize} &ndash; the byte size of the {@code pData} buffer.</li>
 * <li>{@code pData} &ndash; contains the actual constant values to specialize with.</li>
 * </ul>
 *
 * <h3>Layout</h3>
 *
 * <pre><code>
 * struct VkSpecializationInfo {
 *     uint32_t mapEntryCount;
 *     {@link VkSpecializationMapEntry VkSpecializationMapEntry} const * pMapEntries;
 *     size_t dataSize;
 *     void const * pData;
 * }</code></pre>
 */
class SpecializationInfo(
    var mapEntries: Array<SpecializationMapEntry>? = null,
    var data: ByteArray? = null
) {

    fun toPtr(ptr: Ptr, stack: MemoryStack) {
        mapEntries?.let {
            memPutAddress(ptr + PMAPENTRIES, it.native(stack))
            nmapEntryCount(ptr, it.size)
        }
        data?.toBuffer(stack)?.let { npData(ptr, it) }
    }
}