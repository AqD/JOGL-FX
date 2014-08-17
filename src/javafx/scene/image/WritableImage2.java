package javafx.scene.image;

import com.sun.javafx.tk.PlatformImage;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import javafx.beans.NamedArg;

/**
 * Created by AqD on 2014/08/17.
 */
public class WritableImage2 extends WritableImage {

    protected static Method getWritablePlatformImageMethod;
    protected static Method pixelsDirtyMethod;

    static {
        try {
            getWritablePlatformImageMethod = Image.class.getDeclaredMethod("getWritablePlatformImage");
            getWritablePlatformImageMethod.setAccessible(true);
            pixelsDirtyMethod = Image.class.getDeclaredMethod("pixelsDirty");
            pixelsDirtyMethod.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public WritableImage2(@NamedArg("width") int width, @NamedArg("height") int height) {
        super(width, height);
    }

    public WritableImage2(@NamedArg("reader") PixelReader reader, @NamedArg("width") int width, @NamedArg("height") int height) {
        super(reader, width, height);
    }

    public WritableImage2(@NamedArg("reader") PixelReader reader, @NamedArg("x") int x, @NamedArg("y") int y, @NamedArg("width") int width, @NamedArg("height") int height) {
        super(reader, x, y, width, height);
    }

    public <T extends Buffer> void setPixelsNoNotify(int x, int y, int w, int h,
                                                     PixelFormat<T> pixelformat,
                                                     T buffer, int scanlineStride) {
        PlatformImage pimg = getWritablePlatformImage2();
        pimg.setPixels(x, y, w, h, pixelformat,
                buffer, scanlineStride);
    }

    public void setPixelsNoNotify(int x, int y, int w, int h,
                                  PixelFormat<ByteBuffer> pixelformat,
                                  byte buffer[], int offset, int scanlineStride) {
        PlatformImage pimg = getWritablePlatformImage2();
        pimg.setPixels(x, y, w, h, pixelformat,
                buffer, offset, scanlineStride);
    }

    public void setPixelsNoNotify(int x, int y, int w, int h,
                                  PixelFormat<IntBuffer> pixelformat,
                                  int buffer[], int offset, int scanlineStride) {
        PlatformImage pimg = getWritablePlatformImage2();
        pimg.setPixels(x, y, w, h, pixelformat,
                buffer, offset, scanlineStride);
    }

    public void setPixelsNoNotify(int writex, int writey, int w, int h,
                                  PixelReader reader, int readx, int ready) {
        PlatformImage pimg = getWritablePlatformImage2();
        pimg.setPixels(writex, writey, w, h, reader, readx, ready);
    }

    public void pixelsDirty2() {
        try {
            pixelsDirtyMethod.invoke(this);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public PlatformImage getWritablePlatformImage2() {
        try {
            return (PlatformImage) getWritablePlatformImageMethod.invoke(this);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
