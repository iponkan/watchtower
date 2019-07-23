package com.hitqz.robot.watchtower;

import org.junit.Test;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        char[] chars = new char[]{'h', 'e', 'l', 'l', 'o'};
        reverseString(chars);
        for (int i = 0; i < chars.length; i++) {
            System.out.println(chars[i]);
        }
    }

    public void reverseString(char[] s) {
        int length = s.length;
        int halfLength = length / 2;
        for (int i = 0; i < halfLength; i++) {
            int bottom = length - 1 - i;
            char a = s[i];
            s[i] = s[bottom];
            s[bottom] = a;
        }
    }

}