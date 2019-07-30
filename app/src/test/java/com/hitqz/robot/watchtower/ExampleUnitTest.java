package com.hitqz.robot.watchtower;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        String s = "vqblqcb";
        int length = lengthOfLongestSubstring(s);
        System.out.println(length);
    }

    public int lengthOfLongestSubstring(String s) {
        if (s == null || s.length() == 0) {
            return 0;
        }
        int max = 0;
        Stack<Character> stack = new Stack<>();
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            Character character = chars[i];
            if (stack.size() > 0 && character.equals(stack.peek())) {
                max = Math.max(max, stack.size());
                stack.clear();
                stack.add(character);
            } else if (stack.contains(character)) {
                max = Math.max(max, stack.size());
                int index = stack.search(character);
                if (index > 0) {
                    List<Character> list = new ArrayList<>(stack.subList(index - 1, stack.size()));
                    stack.removeAll(list);
                }
                stack.add(character);
            } else {
                stack.add(character);
            }
        }
        return Math.max(max, stack.size());
    }

    private void test() {
        List<String> list = new ArrayList<>();
        list.add("A");
        list.add("B");

//        for (String s : list) {
//            if (s.equals("B")) {
//                list.remove(s);
//            }
//        }

        if (list.contains("B")) {
            list.removeAll(new ArrayList<String>(Arrays.asList(new String[]{"B"})));
        }
    }

}