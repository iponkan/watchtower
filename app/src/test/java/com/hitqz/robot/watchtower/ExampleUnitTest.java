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
        ListNode l1 = new ListNode(5);
//        l1.next = new ListNode(1);
//        l1.next.next = new ListNode(1);
//        l1.next.next.next = new ListNode(1);
//        l1.next.next.next.next = new ListNode(1);
//        l1.next.next.next.next.next = new ListNode(1);
//        l1.next.next.next.next.next.next = new ListNode(1);
//        l1.next.next.next.next.next.next.next = new ListNode(1);
//        l1.next.next.next.next.next.next.next.next = new ListNode(1);
//        l1.next.next.next.next.next.next.next.next.next = new ListNode(1);
//        l1.next.next.next.next.next.next.next.next.next.next = new ListNode(1);

        ListNode l2 = new ListNode(5);
//        l2.next = new ListNode(1);
//        l2.next.next = new ListNode(1);
//        l2.next.next.next = new ListNode(1);
//        l2.next.next.next.next = new ListNode(1);
//        l2.next.next.next.next.next = new ListNode(1);
//        l2.next.next.next.next.next.next = new ListNode(1);
//        l2.next.next.next.next.next.next.next = new ListNode(1);
//        l2.next.next.next.next.next.next.next.next = new ListNode(1);
//        l2.next.next.next.next.next.next.next.next.next = new ListNode(1);
//        l2.next.next.next.next.next.next.next.next.next.next = new ListNode(1);

        ListNode listNode = addTwoNumbers(l1, l2);

        StringBuilder stringBuilder = new StringBuilder();
        System.out.println(printListNode(listNode, stringBuilder));
    }

    private String printListNode(ListNode listNode, StringBuilder stringBuilder) {

        stringBuilder.append(" ");
        stringBuilder.append(listNode.val);

        if (listNode.next != null) {
            printListNode(listNode.next, stringBuilder);
        }
        return stringBuilder.toString();

    }

    public class ListNode {
        int val;
        ListNode next;

        ListNode(int x) {
            val = x;
        }
    }

    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {

        boolean jin = false;
        int val = l1.val + l2.val;
        if (val >= 10) {
            val = val % 10;
            jin = true;
        }

        ListNode listNode = new ListNode(val);

        if (l1.next != null || l2.next != null || jin) {
            listNode.next = add(l1.next, l2.next, jin);
            return listNode;
        }
        return listNode;
    }

    private ListNode add(ListNode l1, ListNode l2, boolean jin) {
        int val = (l1 == null ? 0 : l1.val) + (l2 == null ? 0 : l2.val) + (jin ? 1 : 0);
        if (val >= 10) {
            val = val % 10;
            jin = true;
        } else {
            jin = false;
        }

        ListNode next = new ListNode(val);

        ListNode l1Next = null;
        if (l1 != null) {
            l1Next = l1.next;
        }

        ListNode l2Next = null;
        if (l2 != null) {
            l2Next = l2.next;
        }


        if (l1Next != null || l2Next != null || jin) {
            next.next = add(l1Next, l2Next, jin);
            return next;
        } else {
            return next;
        }
    }
}