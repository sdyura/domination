#!/bin/sh
cd "`dirname "$0"`"
java -Ddebug=true -cp ../swingUI/lib/midletrunner.jar:../swingUI/lib/m3gbasic.jar:../swingUI/lib/SwingME.jar:../swingUI/lib/LobbyClient.jar:../swingUI/lib/Grasshopper.jar:../swingUI/dist/Domination4ME.jar org.me4se.MIDletRunner -width 320 -height 480 net.yura.domination.mobile.flashgui.DominationMain

