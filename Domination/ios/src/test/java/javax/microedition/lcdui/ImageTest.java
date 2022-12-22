package javax.microedition.lcdui;

import org.junit.Test;

public class ImageTest {

    @Test
    public void test1() throws Exception {

        // TODO BUG BUG BUG BUG: if we create a UIGraphicsBeginImageContext from the TCP Thread
        // then we discard it and force a GC, then when we quit the TCP Thread we will crash
        // repo steps: open lobby, open a random game, close game, close lobby, force GC, TCP thread ends.
        System.out.println("############# STARTING TEST 1");

        try {
            // this is TEST 1, this breaks on current master
            final Image[] ref = new Image[1];
            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        ref[0] = Image.createImage("/marble.jpg");
                        ref[0].getRGB(new int[20], 0, 10, 0, 0, 10, 2);
                    }
                    catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            };
            t.start();
            t.join();
            ref[0] = null;
            Class.forName("org.moe.GCUtil").getMethod("gc").invoke(null);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        System.out.println("TEST 1 DONE!");

    }

    @Test
    public void test2() throws Exception {
        System.out.println("############# STARTING TEST 2");

        try {
            // this is TEST 2, this fails when we switch to new methods
            final Image[] ref = new Image[1];
            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        ref[0] = Image.createImage("/marble.jpg");
                        ref[0].getRGB(new int[20], 0, 10, 0, 0, 10, 2);

                        Class.forName("org.moe.GCUtil").getMethod("gc").invoke(null);
                    }
                    catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            };
            t.start();
            t.join();

            ref[0] = null;
            Class.forName("org.moe.GCUtil").getMethod("gc").invoke(null);

        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        System.out.println("TEST 2 DONE!");
    }
}