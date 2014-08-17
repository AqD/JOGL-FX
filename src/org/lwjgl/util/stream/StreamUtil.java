/*
 * Copyright (c) 2002-2012 LWJGL Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'LWJGL' nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.lwjgl.util.stream;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.opengl.ContextCapabilities;
import sun.misc.Unsafe;

import static javax.media.opengl.GL4bc.*;
import static org.lwjgl.opengl.JoglWrapper.*;

/**
 * @author Spasi
 */
public final class StreamUtil {

    private static final int TEX_ROW_ALIGNMENT = 16 * 4; // 16 pixels

    private StreamUtil() {
    }

    static int createRenderTexture(final int width, final int height) {
        return createRenderTexture(width, height, GL_NEAREST);
    }

    static int createRenderTexture(final int width, final int height, final int filter) {
        final int texID = glGenTextures();

        gl.glBindTexture(GL_TEXTURE_2D, texID);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter);
        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, null);
        gl.glBindTexture(GL_TEXTURE_2D, 0);

        return texID;
    }

    static int createRenderBuffer(final int width, final int height, final int internalformat) {
        return createRenderBuffer(width, height, 1, internalformat);
    }

    static int createRenderBuffer(final int width, final int height, final int samples, final int internalformat) {
        final int bufferID = glGenRenderbuffers();

        gl.glBindRenderbuffer(GL_RENDERBUFFER, bufferID);
        if (samples <= 1)
            gl.glRenderbufferStorage(GL_RENDERBUFFER, internalformat, width, height);
        else
            gl.glRenderbufferStorageMultisample(GL_RENDERBUFFER, samples, internalformat, width, height);
        gl.glBindRenderbuffer(GL_RENDERBUFFER, 0);

        return bufferID;
    }

    static void waitOnFence(final long[] fences, final int index) {
        gl.glWaitSync(fences[index], 0, GL_TIMEOUT_IGNORED);
        gl.glDeleteSync(fences[index]);
        fences[index] = 0;
    }

    static boolean isAMD(final ContextCapabilities caps) {
        return caps.GL_ATI_fragment_shader || caps.GL_ATI_texture_compression_3dc || caps.GL_AMD_debug_output;
    }

    static boolean isNVIDIA(final ContextCapabilities caps) {
        return caps.GL_NV_vertex_program || caps.GL_NV_register_combiners || caps.GL_NV_gpu_program4;
    }

    static int getStride(final int width) {
        // Force a packed format on AMD. Their drivers show unstable
        // performance if we mess with (UN)PACK_ROW_LENGTH.
        return isAMD(ContextCapabilities.get()) ?
                width * 4 :
                getStride(width, TEX_ROW_ALIGNMENT);
    }

    /**
     * Aligns the row stride. This is beneficial for all the memcpy's we're doing (and also required for INTEL_map_texture).
     *
     * @param width    the row width in pixels
     * @param aligment the row aligment in bytes. Must be a power-of-two value.
     * @return the aligned row stride
     */
    static int getStride(final int width, final int aligment) {
        int stride = width * 4;

        if ((stride & (aligment - 1)) != 0)
            stride += aligment - (stride & (aligment - 1));

        return stride;
    }

    private static void checkCapabilities(final ContextCapabilities caps) {
        if (!caps.OpenGL15)
            throw new UnsupportedOperationException("Support for OpenGL 1.5 or higher is required.");

        if (!(caps.OpenGL20 || caps.GL_ARB_texture_non_power_of_two))
            throw new UnsupportedOperationException("Support for npot textures is required.");

        if (!(caps.OpenGL30 || caps.GL_ARB_framebuffer_object || caps.GL_EXT_framebuffer_object))
            throw new UnsupportedOperationException("Framebuffer object support is required.");
    }

    public static RenderStreamFactory getRenderStreamImplementation() {
        final List<RenderStreamFactory> list = getRenderStreamImplementations();

        if (list.isEmpty())
            throw new UnsupportedOperationException("A supported TextureStream implementation could not be found.");

        return list.get(0);
    }

    public static List<RenderStreamFactory> getRenderStreamImplementations() {
        final ContextCapabilities caps = ContextCapabilities.get();

        checkCapabilities(caps);

        final List<RenderStreamFactory> list = new ArrayList<>();

        addIfSupported(caps, list, RenderStreamPBOAMD.FACTORY);
        addIfSupported(caps, list, RenderStreamPBOCopy.FACTORY);
        addIfSupported(caps, list, RenderStreamINTEL.FACTORY);
        addIfSupported(caps, list, RenderStreamPBODefault.FACTORY);

        return list;
    }

    public static TextureStreamFactory getTextureStreamImplementation() {
        final List<TextureStreamFactory> list = getTextureStreamImplementations();

        if (list.isEmpty())
            throw new UnsupportedOperationException("A supported TextureStream implementation could not be found.");

        return list.get(0);
    }

    public static List<TextureStreamFactory> getTextureStreamImplementations() {
        final ContextCapabilities caps = ContextCapabilities.get();

        checkCapabilities(caps);

        final List<TextureStreamFactory> list = new ArrayList<>();

        addIfSupported(caps, list, TextureStreamINTEL.FACTORY);
        addIfSupported(caps, list, TextureStreamPBODefault.FACTORY);
        addIfSupported(caps, list, TextureStreamPBORange.FACTORY);   // NOT faster than PBODefault

        return list;
    }

    private static <T extends StreamFactory<?>> void addIfSupported(final ContextCapabilities caps, final List<T> list, final T factory) {
        if (factory.isSupported(caps))
            list.add(factory);
    }

    public abstract static class StreamFactory<S> {

        private final String description;

        protected StreamFactory(final String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public abstract boolean isSupported(ContextCapabilities caps);

        public String toString() {
            return description;
        }
    }

    public abstract static class RenderStreamFactory extends StreamFactory<RenderStream> {

        protected RenderStreamFactory(final String description) {
            super(description);
        }

        public abstract RenderStream create(StreamHandler handler, int samples, int transfersToBuffer);
    }

    public abstract static class TextureStreamFactory extends StreamFactory<TextureStream> {

        protected TextureStreamFactory(final String description) {
            super(description);
        }

        public abstract TextureStream create(StreamHandler handler, int transfersToBuffer);
    }

    static int checkSamples(final int samples, final ContextCapabilities caps) {
        if (samples <= 1)
            return samples;

        if (!(caps.OpenGL30 || (caps.GL_EXT_framebuffer_multisample && caps.GL_EXT_framebuffer_blit)))
            throw new UnsupportedOperationException("Multisampled rendering on framebuffer objects is not supported.");

        return Math.min(samples, glGetInteger(GL_MAX_SAMPLES));
    }

    static final class PageSizeProvider {

        static final int PAGE_SIZE;

        static {
            int pageSize = 4096; // Assume 4kb if Unsafe is not available

            try {
                pageSize = getUnsafeInstance().pageSize();
            } catch (Exception e) {
                // ignore
            }

            PAGE_SIZE = pageSize;
        }

        private static Unsafe getUnsafeInstance() {
            final Field[] fields = Unsafe.class.getDeclaredFields();

			/*
            Different runtimes use different names for the Unsafe singleton,
			so we cannot use .getDeclaredField and we scan instead. For example:

			Oracle: theUnsafe
			PERC : m_unsafe_instance
			Android: THE_ONE
			*/
            for (Field field : fields) {
                if (!field.getType().equals(Unsafe.class))
                    continue;

                final int modifiers = field.getModifiers();
                if (!(Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)))
                    continue;

                field.setAccessible(true);
                try {
                    return (Unsafe) field.get(null);
                } catch (IllegalAccessException e) {
                    // ignore
                }
                break;
            }

            throw new UnsupportedOperationException();
        }
    }
}