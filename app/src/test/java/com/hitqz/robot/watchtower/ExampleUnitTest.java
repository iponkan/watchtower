package com.hitqz.robot.watchtower;

import org.junit.Test;

import java.util.HashSet;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        String s = "abcdefffff";
        int length = lengthOfLongestSubstring(s);
        System.out.println(length);
    }

    public int lengthOfLongestSubstring(String s) {
        if (s == null || s.length() == 0) {
            return 0;
        }
        HashSet<Character> set = new HashSet<>();
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            set.add(chars[i]);
        }
        return set.size();
    }

}