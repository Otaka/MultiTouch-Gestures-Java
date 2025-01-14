//
//  EventDispatch.cpp
//  OSXGestures4JavaJNI
//
//  Created by Martijn Courteaux on 30/06/15.
//  Copyright © 2015 Martijn Courteaux. All rights reserved.
//

#include <stdio.h>

#import <AppKit/AppKit.h>

#include "EventDispatch.h"

JNIEnv *env;
jclass jc_EventDispatch;
jmethodID jm_dispatchMagnifyGesture;
jmethodID jm_dispatchRotateGesture;
jmethodID jm_dispatchScrollWheelEvent;

CFMachPortRef eventTap;
CFRunLoopRef runningLoop;
bool running = false;

int convertCocoaPhaseToJavaPhase(NSEventPhase phase) {
    switch (phase) {
        case NSEventPhaseNone:
            return 0;
        case NSEventPhaseBegan:
            return 1;
        case NSEventPhaseChanged:
            return 2;
        case NSEventPhaseEnded:
            return 3;
        case NSEventPhaseCancelled:
            return 4;
    }
    return -1;
}


NSEventMask eventMask =
NSEventMaskGesture |
NSEventMaskMagnify|
NSEventMaskSwipe |
NSEventMaskRotate |
NSScrollWheelMask;

CGEventRef eventTapCallback(CGEventTapProxy proxy, CGEventType type, CGEventRef eventRef, void *refcon) {
    if (!(type > 0 && type < kCGEventTapDisabledByTimeout)) {
        return eventRef;
    }
    if (type > 0x7FFFFFFF) {
        return eventRef;
    }
    
    // convert the CGEventRef to an NSEvent
    NSEvent *event = [NSEvent eventWithCGEvent:eventRef];
    
    // filter out events which do not match the mask
    if (!(eventMask & NSEventMaskFromType([event type]))) { return [event CGEvent]; }

    NSPoint m = [NSEvent mouseLocation];
    switch ([event type]){
        case NSEventTypeMagnify:{
            int phase = convertCocoaPhaseToJavaPhase([event phase]);
            env->CallStaticVoidMethod(jc_EventDispatch, jm_dispatchMagnifyGesture, (double) m.x, (double) m.y, event.magnification, phase);
            break;
        }
        case NSEventTypeRotate: {
            int phase = convertCocoaPhaseToJavaPhase([event phase]);
            env->CallStaticVoidMethod(jc_EventDispatch, jm_dispatchRotateGesture, (double) m.x, (double) m.y, event.rotation, phase);
            break;
        }
        case NSScrollWheel: {
            int phase = convertCocoaPhaseToJavaPhase([event phase]);
            int subtype = [event subtype];
            env->CallStaticVoidMethod(jc_EventDispatch, jm_dispatchScrollWheelEvent, (double) m.x, (double) m.y, event.scrollingDeltaX, event.scrollingDeltaY, phase, subtype);
            break;
        }
        default:
            break;
    }
    
    return [event CGEvent];
}

void JNICALL Java_com_martijncourteaux_multitouchgestures_EventDispatch_init(JNIEnv *env, jclass clazz) {
    ::env = env;
    jc_EventDispatch = clazz;
    jm_dispatchMagnifyGesture = env->GetStaticMethodID(jc_EventDispatch, "dispatchMagnifyGesture", "(DDDI)V");
    jm_dispatchRotateGesture = env->GetStaticMethodID(jc_EventDispatch, "dispatchRotateGesture", "(DDDI)V");
    jm_dispatchScrollWheelEvent = env->GetStaticMethodID(jc_EventDispatch, "dispatchScrollWheelEvent", "(DDDDII)V");
}

void JNICALL Java_com_martijncourteaux_multitouchgestures_EventDispatch_start(JNIEnv *env, jclass) {
    if (!running) {
        ProcessSerialNumber PSN;
        GetCurrentProcess(&PSN);
        eventTap = CGEventTapCreateForPSN(&PSN,kCGSessionEventTap, kCGEventTapOptionListenOnly,eventMask, eventTapCallback, nil);
        CFRunLoopAddSource(CFRunLoopGetCurrent(), CFMachPortCreateRunLoopSource(kCFAllocatorDefault, eventTap, 0), kCFRunLoopCommonModes);
        CGEventTapEnable(eventTap, true);
        runningLoop = CFRunLoopGetCurrent();
        CFRunLoopRun();
        running = false;
    }
}

void JNICALL Java_com_martijncourteaux_multitouchgestures_EventDispatch_stop(JNIEnv *, jclass) {
    if (running) {
        running = false;
        CFRunLoopStop(runningLoop);
        CGEventTapEnable(eventTap, false);
    }
}