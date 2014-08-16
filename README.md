## JOGL-JavaFX Integration Demo

This repository contains a port of Spasi's LWJGL-JavaFX integration to JOGL 2:

- Only the OpenGL => JavaFX node part is ported, because I don't need the other part (JavaFX => OpenGL).
- JOGL source is modified (GLBufferStateTracker) to allow AMD pinned memory


Status:

- Stream by Asynchronous PBO: ported, tested against AMD Radeon 7770 and nVIDIA Geforce GTX 860M
- Stream by AMD_pinned_memory: ported, tested against AMD Radeon 7770
- Stream by ARB_copy_buffer: ported, tested against nVIDIA Geforce GTX 860M
- Stream by INTEL_map_texture: ported, untested
- Vsync: not ported, not needed.

