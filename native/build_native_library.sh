clang -arch arm64 -framework Cocoa -framework Carbon -I./ -I$JAVA_HOME/include -I$JAVA_HOME/include/darwin -dynamiclib -o libmtg_mac_arm64.dylib EventDispatch.mm
clang -arch x86_64 -framework Cocoa -framework Carbon -I./ -I$JAVA_HOME/include -I$JAVA_HOME/include/darwin -dynamiclib -o libmtg_mac_x86_64.dylib EventDispatch.mm
cp libmtg_mac_arm64.dylib ../src/main/resources/native/libmtg_mac_arm64.dylib
cp libmtg_mac_x86_64.dylib ../src/main/resources/native/libmtg_mac_x86_64.dylib