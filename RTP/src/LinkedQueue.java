import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

public class LinkedQueue<T> implements Queue<T> {
	
	/**
	 * The size of queue.
	 */
    private int total;
    
    /**
     * Head and tail node.
     */
    private Node first, last;

    
    /**
     * Node structure.
     */
    private class Node {
        private T ele;
        private Node next;
    }

    /**
     * Constructor
     */
    public LinkedQueue() {
    	first = null;
    	last = null;
    }

    /**
     * Add element into the end of queue.
     * @param ele
     * @return
     */
    synchronized public LinkedQueue<T> enqueue(T ele)
    {
        Node current = last;
        last = new Node();
        last.ele = ele;
        if (total++ == 0) first = last;
        else current.next = last;

        return this;
    }

    /**
     * Get and remove the first node in the queue
     * @return
     */
    synchronized public T dequeue()
    {
        if (total == 0) throw new java.util.NoSuchElementException();
        T ele = first.ele;
        first = first.next;
        if (--total == 0){
        	first = null;
        	last = null;
        }
        return ele;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        Node tmp = first;
        while (tmp != null) {
            sb.append(tmp.ele).append(", ");
            tmp = tmp.next;
        }
        return sb.toString();
    }
    
    public ArrayList<T> returnArrayList(){
    	ArrayList<T> myList = new ArrayList<T>();
    	Node myNode = this.first;
    	
    	for(int i = 0; i < this.total; i++){
    		myList.add(myNode.ele);
    		myNode = myNode.next;
    	}
    	return myList;
    }

	@Override
	public boolean addAll(Collection<? extends T> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean contains(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return (first == null);
	}

	@Override
	public Iterator<T> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean remove(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean add(T arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public T element() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean offer(T arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public T peek() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T poll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T remove() {
		// TODO Auto-generated method stub
		return null;
	}

}