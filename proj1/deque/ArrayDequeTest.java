package deque;

import org.junit.Test;
import static org.junit.Assert.*;
public class ArrayDequeTest {
    @Test
    public void equalsTest() {
        ArrayDeque<Integer> ad1 = new ArrayDeque<>();
        ArrayDeque<Integer> ad2 = new ArrayDeque<>();
        ad1.addLast(1);
        ad1.addLast(2);
        ad2.addLast(1);
        ad2.addLast(2);
        assertEquals(ad1, ad2);

        ad2.removeFirst();
        assertNotEquals(ad1, ad2);

        ad2.addLast(1);
        assertNotEquals(ad1, ad2);

        LinkedListDeque<Integer> lld = new LinkedListDeque<>();
        lld.addLast(1);
        lld.addLast(2);
        assertEquals(ad1, lld);
    }

    @Test
    public void iteratorTest(){
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        int p = 1;
        for (int t : ad){
            assertEquals(true, t == p);
            p ++;
        }
    }
}
