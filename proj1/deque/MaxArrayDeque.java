package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private final Comparator<T> comparator;

    public MaxArrayDeque(Comparator<T> c){
        super();
        this.comparator = c;
    }

    private T maxHelper(Comparator<T> c){
        if (isEmpty()){
            return null;
        }
        T ans = get(0);
        for (int i = 1; i < size(); i ++){
            T tmp = get(i);
            if (c.compare(ans, tmp) > 0){
                ans = tmp;
            }
        }
        return ans;
    }

    public T max(){
        return maxHelper(this.comparator);
    }

    public T max(Comparator<T> c){
        return maxHelper(c);
    }
}
