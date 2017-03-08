package org.amg.node.mimehandlers.model.entities;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Source {

	public String getClassName() {
		if (_className != null) {
			return new String(_className);
		} else {
			return null;
		}
	}

	public void setClassName(String newValue) {
		_className = newValue != null ? newValue.toCharArray() : null;
	}

	public void setPackageName(String newValue) {
		_packageName = newValue != null ? newValue.toCharArray() : null;
	}

	public String getPackageName() {
		if (_packageName != null) {
			return new String(_packageName);
		} else {
			return null;
		}
	}

	public Iterator<ControllerMethod> getControllerMethodIterator() {
		return new Iterator<ControllerMethod>() {
			int cursor = 0;
			int last = -1;

			public boolean hasNext() {
				if (_controllerMethod == null)
					return false;
				if (last < 0) {
					last = _controllerMethod.length;
					while (--last > -1 && _controllerMethod[last] == null)
						;
					last++;
				}
				return cursor != last;
			}

			public ControllerMethod next() {
				try {
					return _controllerMethod[cursor++];
				} catch (IndexOutOfBoundsException e) {
					throw new NoSuchElementException();
				}
			}

			public void remove() {
				try {
					_controllerMethod[cursor++] = null;
				} catch (IndexOutOfBoundsException e) {
					throw new NoSuchElementException();
				}
			}
		};
	}

	public ControllerMethod getControllerMethod(int idx) {
		return (ControllerMethod) _controllerMethod[idx];
	}

	public ControllerMethod[] controllerMethodArray() {
		return _controllerMethod;
	}

	public void setMethod(ControllerMethod newValue) {
		if (_controllerMethod != null) {
			int __OPEN_A = -1;
			for (int __I_A = 0; __I_A < _controllerMethod.length; __I_A++) {
				if (_controllerMethod[__I_A] == null) {
					__OPEN_A = __I_A;
					break;
				}
			}
			if (__OPEN_A < 0) {
				__OPEN_A = _controllerMethod.length;
				ControllerMethod array[] = new ControllerMethod[__OPEN_A + 10];
				System.arraycopy(_controllerMethod, 0, array, 0, _controllerMethod.length);
				_controllerMethod = array;
			}
			_controllerMethod[__OPEN_A] = newValue;
		} else {
			_controllerMethod = new ControllerMethod[10];
			_controllerMethod[0] = newValue;
		}
	}

	private char[] _className;
	private char[] _packageName;
	private ControllerMethod[] _controllerMethod;
}
