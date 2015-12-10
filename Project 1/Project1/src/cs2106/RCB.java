package cs2106;

import java.util.ArrayList;

/**
 * 
 * @author Victor Hazali A0110741X
 * 
 */
public class RCB {

	/* member variables */

	private String						_RID;
	private int							_allocated;
	private int							_available;
	private ArrayList<ProcessQuantity>	_waitingList;

	/* Constructors */
	public RCB(String RID, int allocated) {
		_RID = RID;
		_allocated = allocated;
		_available = allocated;
		_waitingList = new ArrayList<ProcessQuantity>();
	}

	/* Getters and Setters */

	public String getRID() {
		return _RID;
	}

	public void setRID(String RID) {
		_RID = RID;
	}

	public int getAllocated() {
		return _allocated;
	}

	public void setAllocated(int allocated) {
		_allocated = allocated;
	}

	public int getAvailable() {
		return _available;
	}

	public void setAvailable(int available) {
		_available = available;
	}

	public ArrayList<ProcessQuantity> getWaitingList() {
		return _waitingList;
	}

	public void setWaitingList(ArrayList<ProcessQuantity> waitingList) {
		_waitingList = waitingList;
	}

	/* Public Methods */
	public void enqueueProcess(PCB process, int quantity) throws Exception {
		ProcessQuantity waiting = alreadyWaiting(process);
		if (waiting == null) {
			_waitingList.add(new ProcessQuantity(process, quantity));
		} else {
			if (waiting.getQuantity() + quantity > _allocated) {
				throw new IllegalArgumentException(
						"request exceeded total available resource!");
			}
			int oldQuantity = waiting.getQuantity();
			waiting.setQuantity(oldQuantity + quantity);
		}
	}

	public void dequeueProcess(PCB process) throws Exception {
		ProcessQuantity waiting = alreadyWaiting(process);
		if (waiting == null) {
			throw new IllegalArgumentException("Process not queueing");
		}
		_waitingList.remove(waiting);
	}

	public void allocateResource(int quantity) {
		if (quantity < 0) {
			throw new IllegalArgumentException(
					"can not allocate negative values");
		}
		if (quantity > _allocated) {
			throw new IllegalArgumentException("can not allocate more than max");
		}
		_available = _available - quantity;
	}

	public void deallocateResource(int quantity) {
		if (quantity + _available > _allocated) {
			throw new IllegalArgumentException(
					"can not have more units than allocated");
		}
		if (quantity < 0) {
			throw new IllegalArgumentException(
					"can not de allocate negative value");
		}
		_available = _available + quantity;
	}

	public String toString() {
		return "RID: " + _RID
				+ "\nAllocated: " + _allocated
				+ "\nAvailable: " + _available
				+ "\nWaiting list: " + _waitingList;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o == this) {
			return true;
		}
		if (!(o instanceof RCB)) {
			return false;
		}
		RCB other = (RCB) o;
		if (other.getRID().equalsIgnoreCase(_RID)) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return _RID.hashCode();
	}

	/* Private Methods */
	private ProcessQuantity alreadyWaiting(PCB process) {
		for (ProcessQuantity waiting : _waitingList) {
			if (waiting.getProcess().equals(process)) {
				return waiting;
			}
		}
		return null;
	}
}
