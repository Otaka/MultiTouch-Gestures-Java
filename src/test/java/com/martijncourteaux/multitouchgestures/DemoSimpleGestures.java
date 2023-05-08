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
import javax.swing.JFrame;
import java.awt.*;
import java.awt.geom.Line2D;

/**
 * @author martijn
 */
public class DemoSimpleGestures {
    private static long lastTouchPadScrollTime;
    private static double a = 0, l = 50;
    private static double x, y;

    public static void main(String[] args) {

        JFrame frame = new JFrame();
        frame.setTitle("MultiTouch Gestures Demo");
        final JComponent comp = new JComponent() {

            @Override
            protected void paintComponent(Graphics gg) {
                super.paintComponent(gg);
                Graphics2D g = (Graphics2D) gg;

                Line2D.Double line = new Line2D.Double(getWidth() * 0.5 + x, getHeight() * 0.5 + y, getWidth() * 0.5 + Math.cos(a) * l + x, getHeight() * 0.5 + Math.sin(a) * l + y);
                g.setColor(Color.red);
                g.setStroke(new BasicStroke(5.0f));
                g.draw(line);
            }

        };

        comp.addMouseWheelListener(e -> {
            if (System.currentTimeMillis() - lastTouchPadScrollTime < 5000) {
                return;
            }
            System.out.println("Scroll with mouse wheel: " + e.getWheelRotation() + " p:" + e.getPreciseWheelRotation());
        });
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(comp, BorderLayout.CENTER);
        frame.setPreferredSize(new Dimension(300, 200));

        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);

        MultiTouchGestureUtilities.addGestureListener(comp, new GestureAdapter() {

            @Override
            public void magnify(MagnifyGestureEvent e) {
                lastTouchPadScrollTime = System.currentTimeMillis();
                System.out.println("Magnify: " + e.getMagnification());
                l *= 1.0 + e.getMagnification();
                comp.repaint();
            }

            @Override
            public void rotate(RotateGestureEvent e) {
                lastTouchPadScrollTime = System.currentTimeMillis();
                System.out.println("Rotate: " + e.getRotation());
                a += e.getRotation();
                comp.repaint();
            }

            @Override
            public void scroll(ScrollGestureEvent e) {
                if (e.getSubtype() != GestureEvent.Subtype.TABLET) {
                    return;
                }
                lastTouchPadScrollTime = System.currentTimeMillis();
                System.out.println("Scroll with touch pad: " + e.getDeltaX() + ", " + e.getDeltaY()+ ", phase:"+e.getPhase());
                x += e.getDeltaX();
                y += e.getDeltaY();
                comp.repaint();
            }
        });
    }
}
