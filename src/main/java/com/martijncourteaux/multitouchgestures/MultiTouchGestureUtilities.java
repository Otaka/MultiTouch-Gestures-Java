/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.martijncourteaux.multitouchgestures;

import com.martijncourteaux.multitouchgestures.event.GestureEvent;
import com.martijncourteaux.multitouchgestures.event.MagnifyGestureEvent;
import com.martijncourteaux.multitouchgestures.event.RotateGestureEvent;
import com.martijncourteaux.multitouchgestures.event.ScrollGestureEvent;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author martijn
 */
@SuppressWarnings("unused")
public class MultiTouchGestureUtilities {
    private static boolean loaded;
    private final static HashMap<JComponent, MultiTouchClient> clients = new HashMap<>();
    private static int listenerCount = 0;

    public static boolean isSupported() {
        return isMacOS();
    }

    private static boolean isMacOS() {
        String os = System.getProperty("os.name").toLowerCase().replaceAll("\\s+", "");
        return os.contains("macos");
    }

    private static boolean isArchArm() {
        String arch = System.getProperty("os.arch").toLowerCase().replaceAll("\\s+", "");
        return arch.contains("aarch");
    }

    private static void loadNativeLibrary() {
        if (loaded) {
            return;
        }
        if (!isSupported()) {
            throw new IllegalStateException("Only macOS is supported.");
        }

        String libName = isArchArm() ? "libmtg_mac_arm64.dylib" : "libmtg_mac_x86_64.dylib";
        File path = extractNative(libName);
        System.load(path.getAbsolutePath());

        loaded = true;
        Runtime.getRuntime().addShutdownHook(new Thread(EventDispatch::stop));
    }

    private static File extractNative(String nativeLibraryName) {
        byte[] libraryContent = readResourceToBuffer("/native/" + nativeLibraryName);
        String tempDir = System.getProperty("java.io.tmpdir");
        File nativeLibraryFile = new File(tempDir, nativeLibraryName);
        if (!nativeLibraryFile.exists()) {
            writeToFile(nativeLibraryFile, libraryContent);
            nativeLibraryFile.deleteOnExit();
            return nativeLibraryFile;
        } else {
            try {
                File tempFile = File.createTempFile(nativeLibraryName, "", new File(tempDir));
                writeToFile(tempFile, libraryContent);
                tempFile.deleteOnExit();
                return tempFile;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void writeToFile(File outputFile, byte[] content) {
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(content);
        } catch (IOException e) {
            throw new RuntimeException("Cannot write to file " + outputFile.getAbsolutePath(), e);
        }
    }

    private static byte[] readResourceToBuffer(String fileName) {
        try (InputStream is = MultiTouchGestureUtilities.class.getResourceAsStream(fileName)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            copyStreams(is, baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Cannot load resource " + fileName, e);
        }
    }

    private static void copyStreams(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buff = new byte[1024 * 8];
        int bytesRead;
        while ((bytesRead = inputStream.read(buff)) != -1) {
            outputStream.write(buff, 0, bytesRead);
        }
    }

    public static int getListenerCount() {
        return listenerCount;
    }

    public static void addGestureListener(JComponent component, GestureListener listener) {
        if (!isSupported()) {
            throw new IllegalStateException("Only macOS is supported.");
        }

        if (!loaded) {
            loadNativeLibrary();
        }

        if (listenerCount == 0) {
            EventDispatch.startInSeparateThread();
        }
        MultiTouchClient client = clients.get(component);
        if (client == null) {
            client = new MultiTouchClient(component);
            client.attachListeners();
            clients.put(component, client);
        }
        List<GestureListener> list = client.getListeners();

        list.add(listener);
        listenerCount++;
    }

    public static boolean removeGestureListener(JComponent component, GestureListener listener) {
        MultiTouchClient client = clients.get(component);
        if (client == null) {
            return false;
        }
        List<GestureListener> list = client.getListeners();
        if (list == null) {
            return false;
        }

        if (list.remove(listener)) {
            if (list.isEmpty()) {
                client.detachListeners();
                clients.remove(component);
            }
            listenerCount--;
            if (listenerCount == 0) {
                EventDispatch.stop();
            }
            return true;
        }
        return false;
    }

    public static int removeAllGestureListeners(JComponent component) {
        MultiTouchClient client = clients.get(component);
        if (client == null) {
            return 0;
        }
        client.detachListeners();
        clients.remove(component);
        List<GestureListener> list = client.getListeners();
        if (list == null) {
            return 0;
        }
        int c = list.size();
        list.clear();
        return c;
    }

    protected static void dispatchMagnifyGesture(double mouseX, double mouseY, double magnification, GestureEvent.Phase phase) {
        if (listenerCount == 0) {
            return;
        }

        int mXi = (int) Math.round(mouseX);
        int mYi = (int) Math.round(mouseY);

        for (Map.Entry<JComponent, MultiTouchClient> e : clients.entrySet()) {
            JComponent c = e.getKey();
            if (!c.isValid() || !c.isVisible()) {
                continue;
            }
            Rectangle r = new Rectangle(c.getLocationOnScreen(), c.getSize());
            if (r.contains(mXi, mYi)) {
                MultiTouchClient client = e.getValue();
                if (client.isInside()) {
                    List<GestureListener> list = client.getListeners();

                    Point relP = new Point(mXi, mYi);
                    SwingUtilities.convertPointFromScreen(relP, c);
                    {
                        MagnifyGestureEvent me = new MagnifyGestureEvent(c, relP.getX(), relP.getY(), mouseX, mouseY, phase, magnification);
                        for (GestureListener l : list) {
                            l.magnify(me);
                        }
                    }

                    return;
                }
            }
        }
    }

    protected static void dispatchRotateGesture(double mouseX, double mouseY, double rotation, GestureEvent.Phase phase) {
        if (listenerCount == 0) {
            return;
        }

        int mXi = (int) Math.round(mouseX);
        int mYi = (int) Math.round(mouseY);

        for (HashMap.Entry<JComponent, MultiTouchClient> e : clients.entrySet()) {
            JComponent c = e.getKey();
            Rectangle r = new Rectangle(c.getLocationOnScreen(), c.getSize());
            if (r.contains(mXi, mYi)) {
                MultiTouchClient client = e.getValue();
                if (client.isInside()) {
                    List<GestureListener> list = client.getListeners();

                    Point relP = new Point(mXi, mYi);
                    SwingUtilities.convertPointFromScreen(relP, c);
                    {
                        RotateGestureEvent re = new RotateGestureEvent(c, relP.getX(), relP.getY(), mouseX, mouseY, phase, rotation);
                        for (GestureListener l : list) {
                            l.rotate(re);
                        }
                    }

                    return;
                }
            }
        }
    }

    protected static void dispatchScrollGesture(double mouseX, double mouseY, double dX, double dY, GestureEvent.Phase phase, GestureEvent.Subtype subtype) {
        if (listenerCount == 0) {
            return;
        }

        int mXi = (int) Math.round(mouseX);
        int mYi = (int) Math.round(mouseY);

        for (HashMap.Entry<JComponent, MultiTouchClient> e : clients.entrySet()) {
            JComponent c = e.getKey();
            Rectangle r = new Rectangle(c.getLocationOnScreen(), c.getSize());
            if (r.contains(mXi, mYi)) {
                MultiTouchClient client = e.getValue();
                if (client.isInside()) {
                    List<GestureListener> list = client.getListeners();
                    Point relP = new Point(mXi, mYi);
                    SwingUtilities.convertPointFromScreen(relP, c);
                    {
                        ScrollGestureEvent se = new ScrollGestureEvent(c, relP.getX(), relP.getY(), mouseX, mouseY, phase, dX, dY, subtype);
                        for (GestureListener l : list) {
                            l.scroll(se);
                        }
                    }

                    return;
                }
            }
        }
    }
}
