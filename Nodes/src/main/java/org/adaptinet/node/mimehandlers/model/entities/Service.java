package org.adaptinet.node.mimehandlers.model.entities;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Service {
	public Iterator<Destination> getDestinationIterator() { 
		return new Iterator<Destination>() {
			int cursor=0;
			int last=-1;
			public boolean hasNext() {
				if(_destination==null) return false;
				if(last<0) {
					last = _destination.length;
					while(--last>-1&&_destination[last]==null);
					last++;
				}
				return cursor!=last;
			}
			public Destination next() {
				try {
					return _destination[cursor++];
				} catch(IndexOutOfBoundsException e) {
					throw new NoSuchElementException();
				}
			}
			public void remove() {
				try {
					_destination[cursor++]=null;
				} catch(IndexOutOfBoundsException e) {
					throw new NoSuchElementException();
				}
			}
		};
	}
	public Destination getDestination(int idx) { 
		return (Destination)_destination[idx];
	}
	public Destination[] destinationArray() {
		return _destination;
	}
	public void setDestination(Destination newValue) { 
		if(_destination!=null) {
			int __OPEN_A=-1;
			for(int __I_A=0;__I_A<_destination.length;__I_A++) {
				if(_destination[__I_A]==null) {
					__OPEN_A=__I_A;
					break;
				}
			}
			if(__OPEN_A<0) {
				__OPEN_A=_destination.length;
				Destination array[] = new Destination[__OPEN_A+10];
				System.arraycopy(_destination,0,array,0,_destination.length);
				_destination = array;
			}
			_destination[__OPEN_A] = newValue;
		}
		else {
			_destination = new Destination[10];
			_destination[0] = newValue;
		}
	}	

	public void setId(String _id) {
		this._id = _id;
	}
	public String getId() {
		return _id;
	}

	private Destination [] _destination = null;
	private String _id = new String();
}
