package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T>{
    private final Comparator<T> comparator;

    public MaxArrayDeque(Comparator<T> c){ // Constructor
        super();
        this.comparator = c;
    }

    /* if we want to compare the element in the deque,we need a comparator
       below are two different ways to call the comparator
     */
    public T max(){ // in this method we need to figure out how to pass in a comparator
        // just say return maxHelper(); ? nope we need a comparator ,so make a comparator above!
        return maxHelper(comparator);// YES! that is!
    }

    public T max(Comparator<T> c){ // in this method we have had a comparator
        return maxHelper(c);
    }

    // we need a helper
    private T maxHelper(Comparator<T> c){
        if (isEmpty()){// when the deque is empty
            return null;
        }
        T ans = get(0);
        for (int i = 1; i < size(); i ++){
            T tmp = get(i);
            if (c.compare(tmp , ans) > 0){
                ans = tmp;
            }
        }
        return ans;
    }
}
