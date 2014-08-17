## JOGL-JavaFX Integration Demo

This repository contains a port of Spasi's LWJGL-JavaFX integration to JOGL 2:

- Both of the OpenGL => JavaFX integration and JavaFX => OpenGL integration are ported.
- JOGL source is modified (GLBufferStateTracker) to allow AMD pinned memory


Status:

- RenderStream by Asynchronous PBO: ported, tested against AMD Radeon 7770 and nVIDIA Geforce GTX 860M
- RenderStream by AMD_pinned_memory: ported, tested against AMD Radeon 7770
- RenderStream by ARB_copy_buffer: ported, tested against nVIDIA Geforce GTX 860M
- RenderStream by INTEL_map_texture: ported, untested
- TextureStream by Asynchronous PBO: ported, tested against AMD Radeon 7770
- TextureStream by ARB_map_buffer_range: ported, tested against AMD Radeon 7770
- TextureStream by INTEL_map_texture: ported, untested
- Vsync: not ported, not needed.


Issues:

- Memory leak on buffer resize, same as LWJGL-JavaFX (AMD Radeon 7770)
- RenderStream by AMD_pinned_memory might fail to allocate buffer after performing too many resize operations.
