/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.martijncourteaux.multitouchgestures;

import com.martijncourteaux.multitouchgestures.event.GestureEvent;
import com.martijncourteaux.multitouchgestures.event.GestureEvent.Phase;

import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.Toolkit;

/**
 * @author martijn
 */
class EventDispatch {
    private static Thread gestureEventThread;

    public static native void init();

    private static native void start();

    public static native void stop();

    public static void startInSeparateThread() {
        if (gestureEventThread != null) {
            if (gestureEventThread.isAlive()) {
                return;
            }
        }

        gestureEventThread = new Thread(() -> {
            init();
            start();
        }, "Gesture Event Thread");
        gestureEventThread.start();
    }

    public static void dispatchMagnifyGesture(final double mouseX, final double mouseY, final double magnification, final int phase) {
        SwingUtilities.invokeLater(() -> {
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            MultiTouchGestureUtilities.dispatchMagnifyGesture(mouseX, d.height - mouseY, magnification, Phase.getByCode(phase));
        });
    }

    public static void dispatchRotateGesture(final double mouseX, final double mouseY, final double rotation, final int phase) {
        SwingUtilities.invokeLater(() -> {
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            MultiTouchGestureUtilities.dispatchRotateGesture(mouseX, d.height - mouseY, -Math.toRadians(rotation), Phase.getByCode(phase));
        });
    }

    public static void dispatchScrollWheelEvent(final double mouseX, final double mouseY, final double deltaX, final double deltaY, final int phase, final int subtype) {
        SwingUtilities.invokeLater(() -> {
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            MultiTouchGestureUtilities.dispatchScrollGesture(mouseX, d.height - mouseY, deltaX, deltaY, Phase.getByCode(phase), (subtype == 0 ? GestureEvent.Subtype.MOUSE : GestureEvent.Subtype.TABLET));
        });
    }
}
