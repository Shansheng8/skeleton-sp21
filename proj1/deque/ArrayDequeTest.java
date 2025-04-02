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
        assertEquals(ad1, ad2);

        LinkedListDeque<Integer> lld = new LinkedListDeque<>();
        assertNotEquals(ad1, lld);
    }
}
