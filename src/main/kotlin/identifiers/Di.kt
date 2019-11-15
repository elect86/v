package identifiers

import kool.Ptr
import org.lwjgl.system.Pointer

/** Base class for Vulkan dispatchable handles.  */
abstract class DispatchableHandleInstance(
    handle: Ptr,
    /** the [CapabilitiesInstance] instance associated with this dispatchable handle.  */
    val capabilities: CapabilitiesInstance
) : Pointer.Default(handle)