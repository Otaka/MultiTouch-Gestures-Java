/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.martijncourteaux.osxgestures4java.event;

import javax.swing.JComponent;

/**
 *
 * @author martijn
 */
public class MagnifyGestureEvent extends GestureEvent
{
    private final double magnification;

    public MagnifyGestureEvent(JComponent source, double mouseX, double mouseY, double absMouseX, double absMouseY, double magnification)
    {
        super(source, mouseX, mouseY, absMouseX, absMouseY);
        this.magnification = magnification;
    }

    public double getMagnification()
    {
        return magnification;
    }
}