package net.yura.domination.engine;

import junit.framework.TestCase;

/**
 * WARNING!! this is not technically a UI test, but its grouped with other UI tests as
 * its very strange and does not work on a lot of setups (e.g. running in java 1.8)
 *
 *
 * WARNING!! this class only work in new version of java, in java 1.8 this test will fail
 * @author yura
 */
public class JavaCompatUtilTest extends TestCase {

    public void testEmptyString() {
        String empty = "";
        assertEquals(0, JavaCompatUtil.graphemeCount(empty));
        assertEquals("", JavaCompatUtil.subGrapheme(empty, 0, 0));
    }

    public void testSingleLetter() {
        String s = "A";
        assertEquals(1, JavaCompatUtil.graphemeCount(s));
        assertEquals("A", JavaCompatUtil.subGrapheme(s, 0, 1));
        assertEquals("", JavaCompatUtil.subGrapheme(s, 0, 0));
    }

    public void testMultipleLetters() {
        String s = "Hello";
        assertEquals(5, JavaCompatUtil.graphemeCount(s));
        assertEquals("He", JavaCompatUtil.subGrapheme(s, 0, 2));
        assertEquals("llo", JavaCompatUtil.subGrapheme(s, 2, 5));
        assertEquals("Hello", JavaCompatUtil.subGrapheme(s, 0, 5));
    }

    public void testSpacesAndPunctuation() {
        String s = "Hi, there!";
        assertEquals(10, JavaCompatUtil.graphemeCount(s));
        assertEquals("Hi,", JavaCompatUtil.subGrapheme(s, 0, 3));
        assertEquals(" ", JavaCompatUtil.subGrapheme(s, 3, 4));
        assertEquals("there!", JavaCompatUtil.subGrapheme(s, 4, 10));
    }

    public void testSimpleEmoji() {
        String s = "\uD83D\uDE0A\uD83D\uDE02"; // ??
        assertEquals(2, JavaCompatUtil.graphemeCount(s));
        assertEquals("\uD83D\uDE0A", JavaCompatUtil.subGrapheme(s, 0, 1)); // ?
        assertEquals("\uD83D\uDE02", JavaCompatUtil.subGrapheme(s, 1, 2)); // ?
        assertEquals("\uD83D\uDE0A\uD83D\uDE02", JavaCompatUtil.subGrapheme(s, 0, 2)); // ??
    }

    public void testMixedTextAndEmoji() {
        String s = "A\uD83D\uDC4B" + "B\ud83c\uddfa\ud83c\udde6" + "\u0061\u0300\u0301\u0300\u0301\u0300\u0301\u0300\u0301"; // A?B??à???????
        assertEquals(5, JavaCompatUtil.graphemeCount(s));
        assertEquals("A", JavaCompatUtil.subGrapheme(s, 0, 1));
        assertEquals("\uD83D\uDC4BB", JavaCompatUtil.subGrapheme(s, 1, 3)); // ?B
        assertEquals("\ud83c\uddfa\ud83c\udde6\u0061\u0300\u0301\u0300\u0301\u0300\u0301\u0300\u0301", JavaCompatUtil.subGrapheme(s, 3, 5)); // ??à???????
        assertEquals("A\uD83D\uDC4BB\ud83c\uddfa\ud83c\udde6\u0061\u0300\u0301\u0300\u0301\u0300\u0301\u0300\u0301", JavaCompatUtil.subGrapheme(s, 0, 5)); // A?B??à???????
    }

    public void testEdgeCases() {
        String s = "X\uD83D\uDE0AY"; // X?Y
        assertEquals(3, JavaCompatUtil.graphemeCount(s));
        assertEquals("", JavaCompatUtil.subGrapheme(s, 0, 0));
        assertEquals("Y", JavaCompatUtil.subGrapheme(s, 2, 3));
        assertEquals("X\uD83D\uDE0AY", JavaCompatUtil.subGrapheme(s, 0, 3));
    }

    public void testComplexEmoji() {
        String s = "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66\uD83C\uDDEB\uD83C\uDDF7"; // ?????????
        assertEquals(2, JavaCompatUtil.graphemeCount(s));
        assertEquals("\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66", JavaCompatUtil.subGrapheme(s, 0, 1)); // ???????
        assertEquals("\uD83C\uDDEB\uD83C\uDDF7", JavaCompatUtil.subGrapheme(s, 1, 2)); // ??
        assertEquals("\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66\uD83C\uDDEB\uD83C\uDDF7", JavaCompatUtil.subGrapheme(s, 0, 2)); // ?????????
    }

    public void testLongComplexEmojiSequence() {
        String s = "\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC8B\u200D\uD83D\uDC68" +
                   "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC66" +
                   "\uD83D\uDC69\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC67"; 
        // ????????????????????
        assertEquals(3, JavaCompatUtil.graphemeCount(s));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC8B\u200D\uD83D\uDC68",
                     JavaCompatUtil.subGrapheme(s, 0, 1)); // ????????
        assertEquals("\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC66",
                     JavaCompatUtil.subGrapheme(s, 1, 2)); // ?????
        assertEquals("\uD83D\uDC69\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC67",
                     JavaCompatUtil.subGrapheme(s, 2, 3)); // ???????
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC8B\u200D\uD83D\uDC68" +
                     "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC66",
                     JavaCompatUtil.subGrapheme(s, 0, 2)); // ?????????????
    }
}
