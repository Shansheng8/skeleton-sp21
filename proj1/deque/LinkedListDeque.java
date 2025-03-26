package deque;

public class LinkedListDeque<T> {
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

    public void addFirst(T item){
        Node P = new Node();
        P.item = item;
        P.pre = sentinel;
        P.next = sentinel.next;
        sentinel.next.pre = P;
        sentinel.next = P;
        size += 1;
    }

    public void addLast(T item){
        Node P = new Node();
        P.item = item;
        P.next = sentinel;
        P.pre = sentinel.pre;
        sentinel.pre.next = P;
        sentinel.pre = P;
        size += 1;
    }

    public boolean isEmpty(){
        return size == 0;
    }

    public int size(){
        return size;
    }

    public void printDeque(){
        Node tmp = sentinel.next;
        while (tmp.next != sentinel){
            System.out.print(tmp.item);
            tmp = tmp.next;
        }
        System.out.println();
    }

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

    public T get(int index){
        if (index > size - 1 || index < 0){
            return null;
        }
        Node tmp = sentinel;
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
        return  getRecursive_helper(sentinel, index);
    }
    /* To learn
    public Iterator<T> iterator(){

    }

    public boolean equals(Object o){

    }*/
}
