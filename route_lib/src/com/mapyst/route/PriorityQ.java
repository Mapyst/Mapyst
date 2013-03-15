/*
 * Copyright (C) 2013 Mapyst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mapyst.route;

import java.util.HashMap;
import java.util.Map;

/*
 Class: PQueue
 A priority queue implementation that dequeues the element with the least priority.
 Any element that is used in the priority queue must extend Prioritizable.
 This priority queue also has O(lg(n)) reprioritize (priority update) by utilizing a Map that keeps
 	track of where elements are stored in the heap

 Author:
 Brandon Kase

 Parameters:
 <E extends Prioritizable> - The type of element to be stored in the priority queue
 */
public class PriorityQ<E extends Prioritizable> {
	// FEILDS
	private E[] heap;
	private int lastIndex; //the one after the last used one AKA the next empty index
	
	private Map<E, Integer> elementToIndex;

	/*
	 * Constructor: PQueue
	 * 
	 * Parameters: initialSize - The initial size of the heap
	 */
	@SuppressWarnings("unchecked")
	public PriorityQ(int initialSize) {
		heap = (E[]) new Prioritizable[initialSize];
		lastIndex = 1;
		elementToIndex = new HashMap<E, Integer>();
	}

	/*
	 * Constructor: PQueue initial size of the heap is set to 20
	 */
	@SuppressWarnings("unchecked")
	public PriorityQ() {
		heap = (E[]) new Prioritizable[20];
		lastIndex = 1;
		elementToIndex = new HashMap<E, Integer>();
	}

	public void enqueue(E data) {
		heap[lastIndex] = data;
		elementToIndex.put(data, lastIndex); //update map
		lastIndex++;
		if (lastIndex >= heap.length)
			heap = expand(); // expands the array
		heapifyUp();
	}

	/*
	 * Function: peek Looks at the the next element to be dequeued without
	 * dequeuing it
	 *
	 * Returns: The element next in line to be dequeued
	 *
	 * Throws: NullPointerException - if the priority queue is empty.
	 */
	public E peek() {
		if (this.isEmpty())
			throw new NullPointerException("There is nothing in the queue");
		return heap[1];
	}

	/*
	 * Function: isEmpty Checks if the priority queue is empty
	 * 
	 * Returns: true if empty, false otherwise
	 */
	public boolean isEmpty() {
		return (heap[1] == null);
	}

	/*
	 * Function: dequeue Dequeues the next element with the lowest priority
	 * 
	 * Returns: The element that was dequeued
	 * 
	 * Throws: NullPointerException - if the priority queue is empty.
	 */
	public E dequeue() {
		if (this.isEmpty())
			throw new NullPointerException("There is nothing in the queue");
		E toReturn = heap[1];
		heap[1] = heap[lastIndex - 1];
		lastIndex--;
		heap[lastIndex] = null;
		elementToIndex.remove(toReturn); //update map
		// System.out.println(Arrays.toString(heap));
		heapifyDown();
		// System.out.println(Arrays.toString(heap));
		return toReturn;
	}
	
	private void swapMap(int a, int b) {
		elementToIndex.put(heap[a], b);
		elementToIndex.put(heap[b], a);
	}

	/*
	 * Function: reprioritize Re-prioritizes the priority queue. This method
	 * MUST be called to change the priority of any elements already in the
	 * priority queue. Do not change the priority by calling setPriority() on
	 * the element.
	 * 
	 * Parameters: origVal - The element whose priority you want to change
	 * newPrior - The new priority you want to be assigned to that element
	 */
	public boolean reprioritize(E origVal, int newPrior) {

		int index = find(origVal);
		if (index == -1)
			return false;
		if (heap[index] != null && ((Waypoint2D)heap[index]).getLabel().equals("5419A"))
			System.out.println("Should be: " + heap[index]);
		if (heap[index] == null)
			System.out.println("origVal: " + origVal);
		heap[index].setPriority(newPrior);

		// basically same as heapifyUp and heapifyDown but without the break
		// because we don't know if we're done
		// down
		int curr = index;
		while (curr <= lastIndex) { // while we aren't at the last
			int min = min(2 * curr, 2 * curr + 1); // get the min of the
													// children
			if (min == -1)
				break;
			if (heap[min].getPriority() < heap[curr].getPriority()) { // check if the child is less than us
				swapMap(min, curr);
				swap(min, curr);
				curr = min;
			}
			else {
				break;
			}
		}
		// up
		curr = index;
		while (curr > 1) { // while we aren't at the top of the heap
			if (heap[curr].getPriority() < heap[curr / 2].getPriority()) { // check if we are less than the parent
				swapMap(curr, curr / 2);
				swap(curr, curr / 2); // if yes swap with the parent
				curr /= 2;
			} else {
				break;
			}
		}
		
		return true;
	}	

	/*
	 * Function: toString
	 * 
	 * Returns: The string representation of the priority queue. Newlines
	 * separate each of the toString()s of the elements of the priority queue.
	 */
	public String toString() {
		String s = "";
		for (int i = 1; i < heap.length; i++) {
			s += heap[i] + ",";
			if (heap[i] != null)
				s += heap[i].toString() + "\n";
		}
		return s;
	}

	public int find(E val) {
		Integer index = elementToIndex.get(val);
		if (index == null)
			return -1;
		else
			return index;
	}
	
	public E[] getHeap() {
		return heap;
	}
	
	@SuppressWarnings("unchecked")
	private E[] expand() {
		E[] toReturn = (E[]) new Prioritizable[heap.length * 2];
        System.arraycopy(heap, 0, toReturn, 0, heap.length);
		return toReturn;
	}

	private void heapifyDown() {
		int curr = 1;
		while (curr < lastIndex - 1) { // while we aren't at one of the last two
										// spots
			int min = min(2 * curr, 2 * curr + 1); // get the min of the
													// children
			if (min == -1)
				break;
			if (heap[min].getPriority() < heap[curr].getPriority()) { // check if the child is less than us
				swapMap(min, curr);
				swap(min, curr); // if it is swap
				curr = min;
			} else
				break;
		}
	}

	// gives the min value at any two indices
	private int min(int indexA, int indexB) {
		//System.out.println("A: " + indexA + ", B: " + indexB);
		if (indexA >= lastIndex || heap[indexA] == null) // since A is always < B this means that both are null
			return -1;
		if (indexB >= lastIndex || heap[indexB] == null) // this means A is not null, but B is so return A
			return indexA;
		if (heap[indexA].getPriority() < heap[indexB].getPriority()) {
			return indexA;
		} else {
			return indexB;
		}
	}

	private void heapifyUp() {
		int curr = lastIndex - 1;
		while (curr > 1) { // while we aren't at the top of the heap
			if (heap[curr].getPriority() < heap[curr / 2].getPriority()) { // check if we are less than the parent
				swapMap(curr, curr / 2);
				swap(curr, curr / 2); // if yes swap with the parent
				curr /= 2;
			} else
				break; // otherwise break, because the heap is valid;
		}
	}

	private void swap(int indexA, int indexB) {
		E temp = heap[indexA];
		heap[indexA] = heap[indexB];
		heap[indexB] = temp;
	}
}
