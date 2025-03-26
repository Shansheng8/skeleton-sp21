package deque;

public class ArrayDeque<T> {
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

    public void addFirst(T item){
        if (size == length){
            resize();
        }
        arr[front] = item;
        front --;
        size ++;
    }

    public void addLast(T item){
        if (size == length){
            resize();
        }
        arr[last] = item;
        last ++;
        size ++;
    }

    public boolean isEmpty(){
        return size == 0;
    }

    public int size(){
        return size;
    }

    public void printDeque(){
        for (int i = front + 1; i < length; i ++){
            System.out.print(arr[i]);
        }
        for (int i = 0; i <last; i ++){
            System.out.print(arr[i]);
        }
        System.out.println();
    }

    public T removeFirst(){
        if (isEmpty()){
            return null;
        }
        T tmp = arr[(front + 1) % length];
        arr[(front + 1) % length] = null;
        front = (front + 1) % length;
        return tmp;
    }

    public T removeLast(){
        if (isEmpty()){
            return null;
        }
        T tmp = arr[(last - 1 + length) % length];
        arr[(last - 1 + length) % length] = null;
        last = (last - 1 + length) % length;
        return tmp;
    }

    public T get(int index){
        if (index < 0 || index > length - 1){
            return null;
        }
        int pos = (front + index + 1) % length;
        return arr[pos];
    }

    /* To learn , same as the LinkedListDeque
    public Iterator<T> iterator(){

    }

    public boolean equals(Object o){

    }*/
}
