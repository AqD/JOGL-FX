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

import lwjglfx.JoglFactory;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.util.stream.StreamUtil.RenderStreamFactory;

import static javax.media.opengl.GL4bc.*;
import static org.lwjgl.opengl.JoglWrapper.*;

/**
 * Default StreamPBOReader implementation: Asynchronous ReadPixels to PBOs
 */
final class RenderStreamPBOCopy extends RenderStreamPBO {

    public static final RenderStreamFactory FACTORY = new RenderStreamFactory("ARB_copy_buffer") {
        public boolean isSupported(final ContextCapabilities caps) {
            return RenderStreamPBODefault.FACTORY.isSupported(caps)
                    && caps.GL_ARB_copy_buffer
                    && caps.GL_NV_gpu_program5 // Nvidia only
                    && (caps.OpenGL40 || caps.GL_ARB_tessellation_shader) // Fermi+
                    ;
        }

        public RenderStream create(final StreamHandler handler, final int samples, final int transfersToBuffer) {
            // UPDATE 2014/08/17: READ_PIXELS on nVIDIA 860M is faster and less problemic here
            return new RenderStreamPBOCopy(handler, samples, transfersToBuffer, ReadbackType.READ_PIXELS);
        }
    };

    private int devicePBO;

    RenderStreamPBOCopy(final StreamHandler handler, final int samples, final int transfersToBuffer, final ReadbackType readbackType) {
        super(handler, samples, transfersToBuffer, readbackType);
        JoglFactory.logger.finest(String.format("%s created: msaa %d, %s", this.getClass().getSimpleName(), samples, readbackType));
    }

    protected void resizeBuffers(final int height, final int stride) {
        super.resizeBuffers(height, stride);

        devicePBO = glGenBuffers();

        gl.glBindBuffer(GL_PIXEL_PACK_BUFFER, devicePBO);
        gl.glBufferData(GL_PIXEL_PACK_BUFFER, height * stride, null, GL_STREAM_COPY); // Should allocate device memory
        gl.glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
    }

    protected void readBack(final int index) {
        gl.glBindBuffer(GL_PIXEL_PACK_BUFFER, devicePBO);

        super.readBack(index);

        gl.glBindBuffer(GL_COPY_WRITE_BUFFER, pbos[index]);

        gl.glCopyBufferSubData(GL_PIXEL_PACK_BUFFER, GL_COPY_WRITE_BUFFER, 0, 0, height * stride);

        gl.glBindBuffer(GL_COPY_WRITE_BUFFER, 0);
        gl.glBindBuffer(GL_COPY_READ_BUFFER, 0);
    }

    protected void pinBuffer(final int index) {
        gl.glBindBuffer(GL_PIXEL_PACK_BUFFER, pbos[index]);

        // We don't need to manually synchronized here, MapBuffer will block until ReadPixels above has finished.
        // The buffer will be unmapped in waitForProcessingToComplete
        // pinnedBuffers[index] = gl.glMapBuffer(GL_PIXEL_PACK_BUFFER, GL_READ_ONLY, height * stride, pinnedBuffers[index]); AQD
        pinnedBuffers[index] = gl.glMapBuffer(GL_PIXEL_PACK_BUFFER, GL_READ_ONLY);

        gl.glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
    }

    protected void copyFrames(final int src, final int trg) {
        gl.glBindBuffer(GL_PIXEL_PACK_BUFFER, pbos[src]);
        gl.glBindBuffer(GL_COPY_WRITE_BUFFER, pbos[trg]);

        gl.glCopyBufferSubData(GL_PIXEL_PACK_BUFFER, GL_COPY_WRITE_BUFFER, 0, 0, height * stride);

        gl.glBindBuffer(GL_COPY_WRITE_BUFFER, 0);
        gl.glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
    }

    protected void postProcess(final int index) {
        gl.glUnmapBuffer(GL_PIXEL_PACK_BUFFER);
    }

    protected void destroyObjects() {
        glDeleteBuffers(devicePBO);
        super.destroyObjects();
    }
}