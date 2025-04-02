package deque;

import java.util.Iterator;
public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private T[] arr;
    private int length;
    private int size;
    private int front;
    private int last;

    public ArrayDeque(){
        this.arr = (T[]) new Object[8];//java特性 必须要这样来创建泛型数组
        this.size = 0;
        this.length = 8;
        this.front = length - 1;
        this.last = 0;
    }
    /*
        using System.arrcopy to larger the array
     */
    private void resize(){ // 不能裸露地使用泛型数组，要上升一个抽象，因为fron不一定就是从数组最右侧往左移动，也可以RemoveFirst让其跑到最左侧
        T[] t = (T[]) new Object[size * 2];
        length *= 2;
        /*the bug needs to fix ,remove this line after fix the problem
          consider the sentence above
         */
        System.arraycopy(arr, front + 1, t, 0, size - front - 1);//if front + 1 if bigger than length - 1, will error
        System.arraycopy(arr, 0, t, size - front, front + 1);
        front = length - 1;
        last = size;
        arr = t;
    }

    @Override
    public void addFirst(T item){
        if (size == length){
            resize();
        }
        arr[front] = item;
        front --;
        size ++;
    }

    @Override
    public void addLast(T item){
        if (size == length){
            resize();
        }
        arr[last] = item;
        last ++;
        size ++;
    }
    
    @Override
    public int size(){
        return size;
    }

    @Override
    public void printDeque(){
        for (int i = front + 1; i < length; i ++){
            System.out.print(arr[i]);
        }
        for (int i = 0; i <last; i ++){
            System.out.print(arr[i]);
        }
        System.out.println();
    }

    @Override
    public T removeFirst(){
        if (isEmpty()){
            return null;
        }
        T tmp = arr[(front + 1) % length];
        arr[(front + 1) % length] = null;
        front = (front + 1) % length;
        size --;
        return tmp;
    }

    @Override
    public T removeLast(){
        if (isEmpty()){
            return null;
        }
        T tmp = arr[(last - 1 + length) % length];
        arr[(last - 1 + length) % length] = null;
        last = (last - 1 + length) % length;
       size --;
        return tmp;
    }

    @Override
    public T get(int index){
        if (index < 0 || index > length - 1){
            return null;
        }
        int pos = (front + index + 1) % length;
        return arr[pos];
    }

    public Iterator<T> iterator(){
        return new ADequeIterator();
    }

    private class ADequeIterator implements Iterator<T>{
        int pos;
        public ADequeIterator(){
            pos = 0;
        }
        @Override
        public boolean hasNext() {
            return pos < size;
        }

        @Override
        public T next() {
            T tmp = get(pos);
            pos ++;
            return tmp;
        }
    }

    @Override
    public boolean equals(Object o){
        //o is considered equal if it is a Deque
        // and if it contains the same contents (as governed by the generic T’s equals method) in the same order.
        //check whether the classes are the same
        if (o instanceof Deque){
            //check the size
            if (((Deque<?>) o).size() != this.size){
                return false;
            }

            for (int i = 0; i < this.size; i ++){
                if (((Deque<?>) o).get(i) != this.get(i)){
                    return false;
                }
            }
            return true;

        }
        return false;
    }
}
