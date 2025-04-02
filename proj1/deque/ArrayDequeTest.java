package deque;

import edu.princeton.cs.introcs.StdRandom;
import org.junit.Test;

import static org.junit.Assert.*;


public class ArrayDequeTest {
    @Test
    public void testRandom() {
        deque.ArrayDeque<Integer> student = new deque.ArrayDeque<>();
        ArrayDeque<Integer> solution = new ArrayDeque<>();
        String msg = "";
        for (int i = 0; i < StdRandom.uniform(0, 1000000); i++) {
            double choice = StdRandom.uniform();
            Integer randVal = StdRandom.uniform(0, 100);
            if (choice < 0.33) {
                student.addLast(randVal);
                solution.addLast(randVal);
                msg += "addLast(" + randVal + ")\n";
            } else if (choice < 0.67) {
                student.addFirst(randVal);
                solution.addFirst(randVal);
                msg += "addFirst(" + randVal + ")\n";
            } else {
                int size = student.size();
                msg += "size()\n";
                if (size > 0) {
                    if (randVal < 50) {
                        msg += "removeFirst()\n";
                        assertEquals(msg, solution.removeFirst(), student.removeFirst());
                    } else {
                        msg += "removeLast()\n";
                        assertEquals(msg, solution.removeLast(), student.removeLast());
                    }
                }
            }
        }
    }

    @Test
    public void testReSize() {
        deque.ArrayDeque<Integer> student = new deque.ArrayDeque<>();
        ArrayDeque<Integer> solution = new ArrayDeque<>();
        student.addFirst(1);
        solution.addFirst(1);
        student.addFirst(2);
        solution.addFirst(2);
        assertEquals(solution.removeFirst(), student.removeFirst());
        assertEquals(solution.removeFirst(), student.removeFirst());
    }

    @Test
    public void testReSize2() {
        deque.ArrayDeque<Integer> student = new deque.ArrayDeque<>();
        ArrayDeque<Integer> solution = new ArrayDeque<>();
        student.addLast(0);
        student.addLast(1);
        solution.addLast(0);
        solution.addLast(1);
        assertEquals(solution.removeFirst(), student.removeFirst());
        solution.addLast(3);
        solution.addLast(4);
        solution.addLast(5);
        solution.addLast(6);
        solution.addLast(7);
        solution.addLast(8);
        student.addLast(3);
        student.addLast(4);
        student.addLast(5);
        student.addLast(6);
        student.addLast(7);
        student.addLast(8);
        assertEquals(solution.removeFirst(), student.removeFirst());
    }

    @Test
    public void getTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        ad.addLast(1);
        ad.addLast(2);
        ad.addLast(3);
        ad.addLast(4);
        ad.addLast(5);
        ad.addLast(6);
        ad.addLast(7);
        ad.addLast(8);
        int p = 1;
        for (int t : ad){
            assertEquals(true, t == p);
            p ++;
        }
        ad.addLast(9);
        assertEquals(true, ad.get(0) == 1);
       assertEquals(true, ad.get(8) == 9);

        ad.removeFirst();
        assertEquals(true, ad.get(0) == 2);

        ad.addFirst(10);
        assertEquals(true, ad.get(0) == 10);
    }

    @Test
    public void removeTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        ad.addLast(1);
        ad.addLast(2);
        ad.addLast(3);
        ad.addLast(4);
        ad.addLast(5);
        ad.addLast(6);
        ad.addLast(7);
        ad.addLast(8);
        ad.removeFirst();
        ad.removeFirst();
        assertEquals(true, ad.get(0) == 3);
        ad.removeLast();
        ad.removeLast();
        ad.removeLast();
        ad.removeLast();
        assertEquals(true, ad.get(1) == 4);
        ad.removeLast();
        ad.removeLast();
        assertEquals(true, ad.removeLast() == null);
    }
}
