package lwjglfx;

import com.sun.javafx.sg.prism.NGCanvas;
import com.sun.prism.Graphics;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.JoglWrapper;

import javax.media.opengl.GL4bc;
import javax.media.opengl.GLContext;
import java.nio.FloatBuffer;

import static javax.media.opengl.GL4bc.*;
import static org.lwjgl.opengl.JoglWrapper.gl;
import static org.lwjgl.opengl.JoglWrapper.glGetInteger;

/**
 * Created by AqD on 2014/08/18.
 */
public class MyNGCanvas extends NGCanvas {
    protected static GLContext glContext;

    private static final float VIEW_ROT_X = 10.0f;
    private static final float VIEW_ROT_Y = 25.0f;
    private static final float VIEW_ROT_Z = 0.0f;

    protected boolean initialized;
    protected int gear1;
    protected int gear2;
    protected int gear3;
    protected float angle = 45;

    protected Runnable dirtyCallback;

    public MyNGCanvas(Runnable dirtyCallback) {
        this.dirtyCallback = dirtyCallback;
    }
    protected void renderContent(Graphics g) {
        super.renderContent(g);
        if (glContext == null) {
            glContext = DirectGears.factory.createExternalGLContext();
            assert (glContext != null);
            System.out.println(glContext.getGL());
            System.out.println(glContext.getGL().getClass());
            JoglWrapper.gl = (GL4bc) glContext.getGL();
        }
        int program = glGetInteger(GL_CURRENT_PROGRAM);
        gl.glUseProgram(0);
        gl.glPushAttrib(GL_ALL_ATTRIB_BITS);
        gl.glPushMatrix();
        try {
            gl.glClearColor(0.1f, 0f, 0.2f, 1f);
            gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            gl.glLoadIdentity();
            if (!initialized) {
                init();
                // initialized = true;
                // display lists are NOT preserved, so init() has to be called everytime!!
            }
            display();
        } finally {
            gl.glPopMatrix();
            gl.glPopAttrib();
            gl.glUseProgram(program);
        }
        this.geometryChanged();
        this.dirtyCallback.run();
    }

    protected void init() {
        FloatBuffer pos = BufferUtils.createFloatBuffer(4).put(new float[]{5.0f, 5.0f, 10.0f, 0.0f});
        FloatBuffer red = BufferUtils.createFloatBuffer(4).put(new float[]{0.8f, 0.1f, 0.0f, 1.0f});
        FloatBuffer green = BufferUtils.createFloatBuffer(4).put(new float[]{0.0f, 0.8f, 0.2f, 1.0f});
        FloatBuffer blue = BufferUtils.createFloatBuffer(4).put(new float[]{0.2f, 0.2f, 1.0f, 1.0f});

        pos.flip();
        red.flip();
        green.flip();
        blue.flip();

        gl.glLightfv(GL_LIGHT0, GL_POSITION, pos);
        gl.glEnable(GL_CULL_FACE);
        gl.glEnable(GL_LIGHTING);
        gl.glEnable(GL_LIGHT0);
        gl.glEnable(GL_DEPTH_TEST);

        // make the gears
        gear1 = gl.glGenLists(1);
        gl.glNewList(gear1, GL_COMPILE);
        gl.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, red);
        Gears.gear(1.0f, 4.0f, 1.0f, 20, 0.7f);
        gl.glEndList();

        gear2 = gl.glGenLists(1);
        gl.glNewList(gear2, GL_COMPILE);
        gl.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, green);
        Gears.gear(0.5f, 2.0f, 2.0f, 10, 0.7f);
        gl.glEndList();

        gear3 = gl.glGenLists(1);
        gl.glNewList(gear3, GL_COMPILE);
        gl.glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, blue);
        Gears.gear(1.3f, 2.0f, 0.5f, 10, 0.7f);
        gl.glEndList();

        gl.glEnable(GL_NORMALIZE);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustum(-1.0f, 1.0f, -1.0f, 1.0f, 5.0f, 60.0f);

        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glTranslatef(1.0f, -0.5f, -40.0f);
    }

    protected void display() {
        gl.glPushMatrix();
        gl.glRotatef(VIEW_ROT_X, 1.0f, 0.0f, 0.0f);
        gl.glRotatef(VIEW_ROT_Y, 0.0f, 1.0f, 0.0f);
        gl.glRotatef(VIEW_ROT_Z, 0.0f, 0.0f, 1.0f);
        gl.glEnable(GL_LIGHTING);
        int i = 0;
        {
            gl.glPushMatrix();
            gl.glTranslatef(-3.0f, -2.0f, i);
            gl.glRotatef(angle, 0.0f, 0.0f, 1.0f);
            gl.glCallList(gear1);
            gl.glPopMatrix();

            gl.glPushMatrix();
            gl.glTranslatef(3.1f, -2.0f, i);
            gl.glRotatef(-2.0f * angle - 9.0f, 0.0f, 0.0f, 1.0f);
            gl.glCallList(gear2);
            gl.glPopMatrix();

            gl.glPushMatrix();
            gl.glTranslatef(-3.1f, 4.2f, i);
            gl.glRotatef(-2.0f * angle - 25.0f, 0.0f, 0.0f, 1.0f);
            gl.glCallList(gear3);
            gl.glPopMatrix();
        }
        angle += 1f;
    }
}
