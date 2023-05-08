
MultiTouch Gestures for Java
============================

This project aims to provide an easy way to enable MultiTouch touchpad gestures for Java in Swing. This project
was originally started as an alternative for the AppleJavaExtensions `com.apple.eawt` package.

Read this is you came here because `com.apple.eawt` is not working
------------------------------------------------------------------

You landed on this page, probably because you are unable to make `com.apple.eawt` compile
on newer versions of Java (JDK 7 and higher). However, after creating this project, I found that there is
a workaround. You should have a look at this question on StackOverflow:
[Using internal sun classes with javac](https://stackoverflow.com/questions/4065401/using-internal-sun-classes-with-javac).
All the classes in the `com.apple.eawt` package are not included in the `$JAVA_HOME/lib/ct.sym` file, which makes
the compilation fail with an error like: 

    com.apple.eawt can not find package
    package com.apple.eawt does not exist

Adding `-XDignore.symbol.file` to your compiler flags solves it.

However, this project still has a purpose, I believe, since you can have smooth two finger scrolling without any hassle.

The Apple gesture features don't report scroll events, which are way smoother than the ones you get using
a classic `MouseWheelListener`.


Supported Platforms
-------------------
Since my personal need for now is only OS X, this is supported. However, if anyone needs support
for other platforms as well, feel free to fork and create a corresponding native source file for
your platform.


Building
--------

Currently, there are two architectures that are supported by OSX: x86_64 and arm64. This library supports both.

I checked that you can easily build x64 library on Mac M1(Arm64), but I am not sure about the reverse, because do not have x64 Mac.

At first, you should build native library with the following command:

```
export JAVA_HOME = {YOUR_JDK_FOLDER_LOCATION}
cd {PROJECT_ROOT}/native
sh build_native_library.sh
```

After building native libraries you can execute maven build as always: 
```
mvn clean install
```

### Java
This project is a Maven project that you can open in any Java IDE. So just open it in IntelliJ IDEA or NetBeans
and you are basically good to go. You can compile this manually as well.

### Native Mac OS X
This is simple Objective-C file that resides in the `native/` directory. There is also a shell script `native/build_native_library.sh` that can compile the file and put result binaries in appropriate place.

Testing
-------
There is a test file provided in:

    src/test/java/com/martijncourteaux/multitouchgestures/DemoSimpleGestures.java

You can have a look at that file for a sample.

Usage
-----
The usage is pretty similar to Apple's AppleJavaExtensions package from Java 6. You build your
Swing frame, just as regular. Then you can do this:

    MultiTouchGestureUtilities.addGestureListener(comp, listener);

Where `listener` is a `MultiTouchGestureListener` and `comp` is the JComponent you want to add
the listener to. **When you dispose/remove a JFrame or component with a listener, you *MUST*
remove the `MultiTouchGestureListener` using one of following techniques:**

    MultiTouchGestureUtilities.removeGestureListener(comp, listener);
    MultiTouchGestureUtilities.removeAllGestureListeners(comp);

The `MultiTouchGestureListener` interface has these methods:

    public void magnify(MagnifyGestureEvent e);
    public void rotate(RotateGestureEvent e);
    public void scroll(ScrollGestureEvent e);

Where each event type has these parameters:

 - `mouseX`: x-coordinate of the mouse in the component space.
 - `mouseX`: y-coordinate of the mouse in the component space.
 - `absMouseX`: x-coordinate of the mouse on the screen
 - `absMouseY`: y-coordinate of the mouse on the screen
 - `phase`: a `GesturePhase` enum indicating what phase the gesture is in.

`scroll` event has additional parameter:
 - `subtype` - enum indicating what kind device is used for scrolling (`MOUSE` or `TABLET`)

A `GesturePhase` is an enum containing these values:

 - `MOMENTUM`: Indicates this event is caused by momentum (like OS X).
 - `BEGIN`: The gesture just began.
 - `CHANGED`: The gesture updated (e.g.: rotating, zooming)
 - `END`: The gesture ended.
 - `CANCELLED`: The gesture was cancelled due to some popup or other thing.
 - `OTHER`: Any other reason for an event. Most of the time this will be errornous.

**Remark**: On OS X, the `MOMENTUM` events come after the `END` event.

Remark for OS X
---------------
The built-in Java scrolling listeners are not as smooth as the one provided in this project.
So I highly recommend checking it out. This gives you the native OS X scrolling experience.



