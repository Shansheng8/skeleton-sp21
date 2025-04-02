package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T> , Iterable<T>{
    private int size;
    private final Node sentinel;

    private class Node{
        private Node pre;
        private Node next;
        private T item;
    }

    public LinkedListDeque(){ //Constructor
        sentinel = new Node();//Don't forget to initialize sentinel
        this.sentinel.pre = sentinel;
        this.sentinel.next = sentinel;
        this.sentinel.item = null;
        this.size = 0;
    }

    @Override
    public void addFirst(T item){
        Node P = new Node();
        P.item = item;
        P.pre = sentinel;
        P.next = sentinel.next;
        sentinel.next.pre = P;
        sentinel.next = P;
        size += 1;
    }

    @Override
    public void addLast(T item){
        Node P = new Node();
        P.item = item;
        P.next = sentinel;
        P.pre = sentinel.pre;
        sentinel.pre.next = P;
        sentinel.pre = P;
        size += 1;
    }

    @Override
    public int size(){
        return size;
    }

    @Override
    public void printDeque(){
        Node tmp = sentinel.next;
        while (tmp.next != sentinel){
            System.out.print(tmp.item);
            tmp = tmp.next;
        }
        System.out.println();
    }

    @Override
    public T removeFirst(){
        if (size == 0){
            return null;
        }
        T stuff = sentinel.next.item;
        sentinel.next = sentinel.next.next;
        sentinel.next.pre = sentinel;
        size -= 1;
        return stuff;
    }

    @Override
    public T removeLast(){
        if (size == 0){
            return null;
        }
        T stuff = sentinel.pre.item;
        sentinel.pre = sentinel.pre.pre;
        sentinel.pre.next = sentinel;
        size -= 1;
        return stuff;
    }

    @Override
    public T get(int index){
        if (index > size - 1 || index < 0){
            return null;
        }
        Node tmp = sentinel.next;
        int t = 0;
        while (t != index){
            tmp = tmp.next;
            t += 1;
        }
        return tmp.item;
    }

    /*we need a helper here,to break the barrier
      the LinkedListDeque itself doesn't support the recursive behavior but Node does
     */
    private T getRecursive_helper(Node cur,int index){
        if (index == 0){
            return cur.item;
        }
        return getRecursive_helper(cur.next, index - 1);
    }

    public T getRecursive(int index){
        if (index > size - 1 || index  < 0){
            return null;
        }
        return  getRecursive_helper(sentinel.next, index);
    }

    public Iterator<T> iterator(){
       return new LLDequeIterator();
    }
    /*
     this is also a helper, return an iterator for the method above
     this makes the LLDeque iterable
     */
    private class LLDequeIterator implements Iterator<T>{
        int pos;
        public LLDequeIterator(){
            pos = 0;
        }

        @Override
        public boolean hasNext(){
            return pos < size;
        }

        @Override
        public T next(){
            T tmp = get(pos);
            pos ++;
            return tmp;
        }
    }

    @Override
    public boolean equals(Object o) {
        //o is considered equal if it is a Deque
        // and if it contains the same contents (as governed by the generic T’s equals method) in the same order.
        //check whether the classes are the same
        if (!(o instanceof Deque) || ((Deque<?>) o).size() != this.size()) {
            return false;
        }
        if (o == this) {
            return true;
        }
        for (int i = 0; i < this.size(); i++) {
            Object item = ((Deque<?>) o).get(i);
            if (!(this.get(i).equals(item))) {
                return false;
            }
        }
        return true;
    }
}
