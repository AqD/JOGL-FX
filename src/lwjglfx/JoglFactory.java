/**
 * Public Domain, use as you wish
 *
 * Created by AqD on 2014/08/17.
 */
package lwjglfx;

import jogamp.opengl.GLDrawableFactoryImpl;
import org.lwjgl.opengl.JoglWrapper;

import javax.media.opengl.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provide GL config and initialization
 * <p>
 * Customize configuration and startup sequence here
 * </p>
 */
public final class JoglFactory {

    public static final Logger logger = Logger.getLogger("lwjglfx");

    static
    {
        Handler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
    }

    /**
     * Get default MSAA samples
     *
     * @return
     */
    public static int getDefaultSamples() {
        return 4;
    }

    /**
     * Get default OpenGL capabilities
     *
     * @return
     */
    public static GLCapabilities getDefaultCapabilities() {
        GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL4bc));
        // OPTIONAL CAPS
        caps.setHardwareAccelerated(true);
        caps.setDepthBits(24);
        // REQUIRED CAPS
        caps.setOnscreen(false);
        caps.setPBuffer(true);
        return caps;
    }

    /**
     * Create PBuffer and assign global GLContext/GL
     *
     * @return
     */
    public static synchronized GLOffscreenAutoDrawable createPBuffer(int width, int height) {
        GLCapabilities caps = getDefaultCapabilities();
        GLProfile profile = caps.getGLProfile();
        GLDrawableFactoryImpl factory = GLDrawableFactoryImpl.getFactoryImpl(profile);
        GLOffscreenAutoDrawable pbuffer = factory.createOffscreenAutoDrawable(
                null /* default platform device */,
                caps,
                null,
                width, height);
        pbuffer.setRealized(true);
        GLContext context = pbuffer.createContext(JoglWrapper.context);
        assert (context != null);
        context.makeCurrent();
        GL4bc gl = (GL4bc) context.getGL();
        assert (gl != null);
        JoglWrapper.context = context;
        JoglWrapper.gl = gl;
        return pbuffer;
    }
}
