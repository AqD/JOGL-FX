package lwjglfx;

import com.sun.javafx.sg.prism.NGCanvas;
import com.sun.javafx.sg.prism.NGNode;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;

import java.util.Random;
import java.util.concurrent.Semaphore;

/**
 * Created by AqD on 2014/08/18.
 */
public class MyCanvas extends Canvas {
    public MyCanvas() {
        Random random = new Random();
        Thread worker = new Thread(() -> {
            Semaphore semaphore = new Semaphore(1);
            while (true) {
                try {
                    Thread.sleep(20); // TODO: how to vsync??
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> {
                    this.setTranslateX(random.nextDouble() * 0.1);
                    semaphore.release();
                });
            }
        }, "MyCanvas");
        worker.setDaemon(true);
        worker.start();
    }

    @Override
    protected NGNode impl_createPeer() {
        return new MyNGCanvas(() -> {
        });
    }

    public void impl_updatePeer() {
        super.impl_updatePeer();
        NGCanvas peer = impl_getPeer();
    }
}