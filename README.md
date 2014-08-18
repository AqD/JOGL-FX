## JOGL-JavaFX Integration Demo

This repository contains a port of Spasi's LWJGL-JavaFX integration to JOGL 2:

- Both of the OpenGL => JavaFX integration and JavaFX => OpenGL integration are ported.
- JOGL source is modified (GLBufferStateTracker) to allow AMD pinned memory


Status:

- RenderStream by Asynchronous PBO: ported, tested
  * AMD Radeon 7770 (v14.4)
  * Intel HD Graphics 4600 (v15.33)
  * nVIDIA Geforce GTX 860M (v340.52)
- RenderStream by AMD_pinned_memory: ported, tested
  * AMD Radeon 7770 (v14.4)
- RenderStream by ARB_copy_buffer: ported, tested
  * Intel HD Graphics 4600 (v15.33)
  * nVIDIA Geforce GTX 860M (v340.52)
- RenderStream by INTEL_map_texture: ported, failed

- TextureStream by Asynchronous PBO: ported, tested
  * AMD Radeon 7770 (v14.4)
  * Intel HD Graphics 4600 (v15.33)
  * nVIDIA Geforce GTX 860M (v340.52)
- TextureStream by ARB_map_buffer_range: ported, tested
  * AMD Radeon 7770 (v14.4)
  * Intel HD Graphics 4600 (v15.33)
  * nVIDIA Geforce GTX 860M (v340.52)
- TextureStream by INTEL_map_texture: ported, failed

- Vsync: not ported, not needed.


Issues:

- Memory leak on buffer resize, same as LWJGL-JavaFX (AMD Radeon 7770)
- RenderStream by AMD_pinned_memory might fail to allocate buffer after performing too many resize operations.
- INTEL_map_texture no longer works with RGBA8, source here => https://software.intel.com/en-us/forums/topic/385153

Notes:

- RenderStream by Async-PBO and AMD pinned memory achieves double performace when the ES2 engine is used on Windows.


## JOGL on JavaFX-ES2

JOGL can be used directly inside the rendering procedure of JavaFX nodes, if the Prism engine is OpenGL ES2.

On Windows the JNI part is shipped but java classes are not. You have to:
  1. Make sure you have jre8\bin\prism-es2.dll (JDK 8u11 ships with them)
  2. Download Linux version of JDK, use its ext\jfxrt.jar
  3. Compile classes from javafx-src.zip\com\sun\prism\es2\WinGL*.java
  4. Copy the compiled WinGL*.class into ext\jfxrt.jar
  5. Run java with -Xshare:dump to re-generate class cache
  6. Start this demo with "-Dprism.order=es2"

See the demo in src\lwjglfx\DirectGears.java

