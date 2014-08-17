/**
 * Public Domain, use as you wish
 *
 * Created by AqD on 2014/08/17.
 */
package org.lwjgl.opengl;

import java.nio.IntBuffer;
import javax.media.opengl.*;
import org.lwjgl.BufferUtils;

/**
 * Provide LWJGL-only functions to JOGL calls
 *
 * @author AqD
 */
public final class JoglWrapper {

    public static GLContext context;
    public static GL4bc gl;

    public static int glGetInteger(int pname) {
        IntBuffer intBuffer = BufferUtils.createIntBuffer(32);
        gl.glGetIntegerv(pname, intBuffer);
        return intBuffer.get(0);
    }

    public static int glGenBuffers() {
        IntBuffer intBuffer = BufferUtils.createIntBuffer(32);
        gl.glGenBuffers(1, intBuffer);
        return intBuffer.get(0);
    }

    public static int glGenFramebuffers() {
        IntBuffer intBuffer = BufferUtils.createIntBuffer(32);
        gl.glGenFramebuffers(1, intBuffer);
        return intBuffer.get(0);
    }

    public static int glGenRenderbuffers() {
        IntBuffer intBuffer = BufferUtils.createIntBuffer(32);
        gl.glGenRenderbuffers(1, intBuffer);
        return intBuffer.get(0);
    }

    public static int glGenTextures() {
        IntBuffer intBuffer = BufferUtils.createIntBuffer(32);
        gl.glGenTextures(1, intBuffer);
        return intBuffer.get(0);
    }

    public static void glDeleteBuffers(int buffer) {
        IntBuffer intBuffer = BufferUtils.createIntBuffer(32);
        intBuffer.put(0, buffer);
        gl.glDeleteTextures(1, intBuffer);
    }

    public static void glDeleteFramebuffers(int buffer) {
        IntBuffer intBuffer = BufferUtils.createIntBuffer(32);
        intBuffer.put(0, buffer);
        gl.glDeleteFramebuffers(1, intBuffer);
    }

    public static void glDeleteRenderbuffers(int buffer) {
        IntBuffer intBuffer = BufferUtils.createIntBuffer(32);
        intBuffer.put(0, buffer);
        gl.glDeleteRenderbuffers(1, intBuffer);
    }

    public static void glDeleteTextures(int texture) {
        IntBuffer intBuffer = BufferUtils.createIntBuffer(32);
        intBuffer.put(0, texture);
        gl.glDeleteTextures(1, intBuffer);
    }
}
