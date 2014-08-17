package lwjglfx;/*
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

import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritableImage2;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import org.lwjgl.util.stream.StreamHandler;
import org.lwjgl.util.stream.StreamUtil;
import org.lwjgl.util.stream.StreamUtil.RenderStreamFactory;
import org.lwjgl.util.stream.StreamUtil.TextureStreamFactory;

import static java.lang.Math.round;
import static javafx.beans.binding.Bindings.createStringBinding;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.observableList;
import static javax.media.opengl.GL4bc.*;
import static org.lwjgl.opengl.JoglWrapper.gl;

/**
 * The JavaFX application GUI controller.
 */
public class GUIController implements Initializable {

    @FXML
    private AnchorPane gearsRoot;
    @FXML
    private ImageView gearsView;

    @FXML
    private Label fpsLabel;
    @FXML
    private Label javaInfoLabel;
    @FXML
    private Label systemInfoLabel;
    @FXML
    private Label glInfoLabel;

    @FXML
    private CheckBox vsync;

    @FXML
    private ChoiceBox<RenderStreamFactory> renderChoice;
    @FXML
    private ChoiceBox<TextureStreamFactory> textureChoice;
    @FXML
    private ChoiceBox<BufferingChoice> bufferingChoice;

    @FXML
    private Slider msaaSamples;

    @FXML
    private WebView webView;

    private WritableImage renderImage;
    private WritableImage webImage;

    private Gears gears;

    public GUIController() {
    }

    public void initialize(final URL url, final ResourceBundle resourceBundle) {
        gearsView.fitWidthProperty().bind(gearsRoot.widthProperty());
        gearsView.fitHeightProperty().bind(gearsRoot.heightProperty());

        final StringBuilder info = new StringBuilder(128);
        info
                .append(System.getProperty("java.vm.name"))
                .append(' ')
                .append(System.getProperty("java.version"))
                .append(' ')
                .append(System.getProperty("java.vm.version"));

        javaInfoLabel.setText(info.toString());

        info.setLength(0);
        info
                .append(System.getProperty("os.name"))
                .append(" - JavaFX ")
                .append(System.getProperty("javafx.runtime.version"));

        systemInfoLabel.setText(info.toString());

        bufferingChoice.setItems(observableArrayList(BufferingChoice.values()));
    }

    private StreamHandler getReadHandler() {
        return new StreamHandler() {
            protected long lastUpdateTimeMillis;

            public int getWidth() {
                return (int) gearsView.getFitWidth();
            }

            public int getHeight() {
                return (int) gearsView.getFitHeight();
            }

            public void process(final int width, final int height, final ByteBuffer data, final int stride, final Semaphore signal) {
                assert (data != null);
                assert (signal != null);
                data.rewind();
                assert (data.remaining() == stride * height);
                long currentUpdateTimeMillis = System.currentTimeMillis();
                if (currentUpdateTimeMillis - lastUpdateTimeMillis < 20) {
                    signal.release();
                    return;
                }
                lastUpdateTimeMillis = currentUpdateTimeMillis;
                /*
                // This method runs in the background rendering thread
                final boolean setNewImage;
                if (renderImage == null || (int) renderImage.getWidth() != width || (int) renderImage.getHeight() != height) {
                    renderImage = new WritableImage2(width, height);
                    setNewImage = true;
                } else {
                    setNewImage = false;
                }
                // Upload the image to JavaFX
                renderImage.setPixelsNoNotify(0, 0, width, height, PixelFormat.getByteBgraPreInstance(), data, stride);
                 */
                Platform.runLater(() -> {
                    try {
                        // If we're quitting, discard update
                        if (!gearsView.isVisible())
                            return;
                        /*
                        // Detect resize and recreate the image
                        if (setNewImage) {
                            gearsView.setImage(renderImage);
                        }
                        renderImage.pixelsDirty2();
                         */
                        if (renderImage == null || (int) renderImage.getWidth() != width || (int) renderImage.getHeight() != height) {
                            renderImage = new WritableImage(width, height);
                            gearsView.setImage(renderImage);
                        }
                        renderImage.getPixelWriter().setPixels(0, 0, width, height, PixelFormat.getByteBgraPreInstance(), data, stride);
                    } finally {
                        // Notify the render thread that we're done processing
                        signal.release();
                    }
                });
            }
        };
    }

    private StreamHandler getWriteHandler() {
        return new StreamHandler() {
            public int getWidth() {
                return (int) webView.getWidth();
            }

            public int getHeight() {
                return (int) webView.getHeight();
            }

            public void process(final int width, final int height, final ByteBuffer buffer, final int stride, final Semaphore signal) {
                // This method runs in the background rendering thread
                Platform.runLater(() -> {
                    if (webImage == null || webImage.getWidth() != width || webImage.getHeight() != height)
                        webImage = new WritableImage2(width, height);

                    webView.snapshot(snapshotResult -> {
                        snapshotResult.getImage().getPixelReader().getPixels(0, 0, width, height, PixelFormat.getByteBgraPreInstance(), buffer, stride);

                        signal.release();
                        return null;
                    }, new SnapshotParameters(), webImage);
                });
            }
        };
    }

    // This method will run in the background rendering thread
    void runGears(final CountDownLatch runningLatch) {
        try {
            gears = new Gears(getReadHandler(), getWriteHandler());
        } catch (Throwable t) {
            t.printStackTrace();
            return;
        }

        final List<RenderStreamFactory> renderStreamFactories = StreamUtil.getRenderStreamImplementations();
        final List<TextureStreamFactory> textureStreamFactories = StreamUtil.getTextureStreamImplementations();

        final String vendor = gl.glGetString(GL_VENDOR);
        final String version = gl.glGetString(GL_VERSION);
        final String renderer = gl.glGetString(GL_RENDERER);

        Platform.runLater(() -> {
            // Listen for FPS changes and update the fps label
            final ReadOnlyIntegerProperty fps = gears.fpsProperty();

            fpsLabel.textProperty().bind(createStringBinding(() -> "FPS: " + fps.get(), fps));
            glInfoLabel.setText(vendor + "; " + version + "; " + renderer);

            renderChoice.setItems(observableList(renderStreamFactories));
            for (int i = 0; i < renderStreamFactories.size(); i++) {
                if (renderStreamFactories.get(i) == gears.getRenderStreamFactory()) {
                    renderChoice.getSelectionModel().select(i);
                    break;
                }
            }
            renderChoice.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> gears.setRenderStreamFactory(newValue));

            textureChoice.setItems(observableList(textureStreamFactories));
            for (int i = 0; i < textureStreamFactories.size(); i++) {
                if (textureStreamFactories.get(i) == gears.getTextureStreamFactory()) {
                    textureChoice.getSelectionModel().select(i);
                    break;
                }
            }
            textureChoice.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> gears.setTextureStreamFactory(newValue));

            bufferingChoice.getSelectionModel().select(gears.getTransfersToBuffer() - 1);
            bufferingChoice.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> gears.setTransfersToBuffer(newValue.getTransfersToBuffer()));

            vsync.selectedProperty().addListener((observableValue, oldValue, newValue) -> gears.setVsync(newValue));

            final int maxSamples = gears.getMaxSamples();
            if (maxSamples == 1)
                msaaSamples.setDisable(true);
            else {
                msaaSamples.setMax(maxSamples);
                msaaSamples.setValue(Math.min(maxSamples, JoglFactory.getDefaultSamples()));
                msaaSamples.valueProperty().addListener(new ChangeListener<Number>() {

                    public boolean isPoT(final int value) {
                        return value != 0 && (value & (value - 1)) == 0;
                    }

                    public int nextPoT(final int value) {
                        int v = value - 1;

                        v |= (v >>> 1);
                        v |= (v >>> 2);
                        v |= (v >>> 4);
                        v |= (v >>> 8);
                        v |= (v >>> 16);

                        return v + 1;
                    }

                    public void changed(final ObservableValue<? extends Number> observableValue, final Number oldValue, final Number newValue) {
                        final float value = newValue.floatValue();
                        final int samples = round(value);

                        if (isPoT(samples))
                            gears.setSamples(samples);
                        else {
                            // Snap to powers of two
                            final int nextPoT = nextPoT(samples);
                            final int prevPoT = nextPoT >> 1;

                            msaaSamples.setValue(
                                    value - prevPoT < nextPoT - value
                                            ? prevPoT
                                            : nextPoT
                            );
                        }
                    }
                });
            }

            // Listen for changes to the WebView contents.
            final ChangeListener<Number> numberListener = (observableValue, oldValue, newValue) -> gears.updateSnapshot();

            webView.widthProperty().addListener(numberListener);
            webView.heightProperty().addListener(numberListener);

            final WebEngine engine = webView.getEngine();

            engine.getLoadWorker().progressProperty().addListener(numberListener);
            engine.setOnStatusChanged(e -> gears.updateSnapshot());

            webView.setEventDispatcher(new EventDispatcher() {
                private final EventDispatcher parent = webView.getEventDispatcher();

                public Event dispatchEvent(final Event e, final EventDispatchChain dispatchChain) {
                    // Mouse over events within the page will be triggered by the StatusChanged handler above.
                    if (e.getEventType() != MouseEvent.MOUSE_MOVED && gears != null)
                        gears.updateSnapshot();

                    return parent.dispatchEvent(e, dispatchChain);
                }
            });

            // Force an update every 4 frames for carets.
            final Timeline timeline = new Timeline();
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.setAutoReverse(true);
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(4 * (1000 / 60)), e -> {
                if (webView.isFocused())
                    gears.updateSnapshot();
            }));
            timeline.play();

            // Do one last update on focus lost
            webView.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue)
                    gears.updateSnapshot();
            });

            webView.getEngine().load("http://www.opengl.org/");
        });

        gears.execute(runningLatch);
    }

    private enum BufferingChoice {
        SINGLE(1, "No buffering"),
        DOUBLE(2, "Double buffering"),
        TRIPLE(3, "Triple buffering");

        private final int transfersToBuffer;
        private final String description;

        private BufferingChoice(final int transfersToBuffer, final String description) {
            this.transfersToBuffer = transfersToBuffer;
            this.description = transfersToBuffer + "x - " + description;
        }

        public int getTransfersToBuffer() {
            return transfersToBuffer;
        }

        public String getDescription() {
            return description;
        }

        public String toString() {
            return description;
        }
    }
}