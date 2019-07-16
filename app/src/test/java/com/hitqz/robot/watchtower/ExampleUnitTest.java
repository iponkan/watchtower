package com.hitqz.robot.watchtower;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        int[] ints = twoSum(new int[]{2, 7, 11, 15}, 9);
        System.out.println(" " + ints[0] + " " + ints[1]);
    }

    public int[] twoSum(int[] nums, int target) {
        HashMap<Integer, Integer> integers = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            integers.put(nums[i], i);
        }
        for (int i = 0; i < nums.length - 1; i++) {
            int j = target - nums[i];
            if (integers.containsKey(j) && integers.get(j) != i) {
                return new int[]{i, integers.get(j)};
            }
        }

        throw new IllegalArgumentException("No two sum solution");
    }

    public int[] twoSum2(int[] nums, int target) {
        int[] indexs = new int[2];
        HashMap<Integer, Integer> integers = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            integers.put(i, nums[i]);
        }


        here:

        for (int i = 0; i < nums.length - 1; i++) {
            for (Map.Entry<Integer, Integer> entry : integers.entrySet()) {
                if (entry.getValue() == target - nums[i] && entry.getKey() != i) {
                    indexs[0] = i;
                    indexs[1] = entry.getKey();
                    break here;
                }
            }
        }

        return indexs;
    }

    public int[] twoSum3(int[] nums, int target) {
        int[] indexs = new int[2];

        here:

        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                if (nums[j] == target - nums[i]) {
                    indexs[0] = i;
                    indexs[1] = j;
                    break here;
                }
            }
        }

        return indexs;
    }
}