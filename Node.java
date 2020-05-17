import java.io.Serializable;

public class Node<E> implements Serializable {
	private E data;
	private Node<E> next, prev;

	public Node(E data){
		this.data = data;
		this.next = null;
		this.prev = null;
	}

	public Node<E> next(){
		return next;
	}

	public Node<E> prev(){
		return prev;
	}

	public void setNext(Node<E> newNode){
		this.next = newNode;
	}

	public void setPrev(Node<E> newNode){
		this.prev = newNode;
	}

	public E get(){
		return data;
	}

	public void set(E data){
		this.data = data;
	}
}