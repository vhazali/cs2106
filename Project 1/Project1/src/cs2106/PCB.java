package cs2106;

import java.util.ArrayList;
import java.util.Queue;

/**
 * 
 * @author Victor Hazali A0110741X
 * 
 */
public class PCB implements Comparable<PCB> {

	/* Constants */
	public static final int	INIT_PRIORITY	= 0;
	public static final int	SYSTEM_PRIORITY	= 1;
	public static final int	USER_PRIORITY	= 2;

	enum STATUS_TYPE {
		READY, RUNNING, BLOCKED, INVALID
	};

	/* member variables */

	private String						_PID;
	private ArrayList<ResourceQuantity>	_otherResources;
	private STATUS_TYPE					_statusType;
	private Queue<PCB>					_readyList;
	private RCB							_blockedResource;
	private PCB							_parent;
	private ArrayList<PCB>				_children;
	private int							_priority;

	/* Constructors */
	public PCB() {
		_PID = "init";
		_otherResources = new ArrayList<ResourceQuantity>();
		_statusType = STATUS_TYPE.RUNNING;
		_readyList = null;
		_blockedResource = null;
		_parent = null;
		_children = new ArrayList<PCB>();
		_priority = INIT_PRIORITY;
	}

	public PCB(String PID, String status, Queue<PCB> readyList, PCB parent,
			int priority) throws Exception {
		_PID = PID;
		_otherResources = new ArrayList<ResourceQuantity>();
		_statusType = getStatusType(status);
		if (_statusType == STATUS_TYPE.INVALID) {
			throw new IllegalArgumentException("Status is invalid!");
		} else if (_statusType != STATUS_TYPE.READY
				&& _statusType != STATUS_TYPE.RUNNING) {
			throw new IllegalArgumentException(
					"Wrong constructor called. Call blocked constructor instead");
		}
		_readyList = readyList;
		_parent = parent;
		_children= new ArrayList<PCB>();
		_priority = priority;
	}

	public PCB(String PID, String status, RCB blockedResource, PCB parent,
			int priority) throws Exception {
		_PID = PID;
		_statusType = getStatusType(status);
		if (_statusType == STATUS_TYPE.INVALID) {
			throw new IllegalArgumentException("Status is invalid!");
		} else if (_statusType != STATUS_TYPE.BLOCKED) {
			throw new IllegalArgumentException(
					"Wrong constructor called, call ready constructor instead");
		}
		_blockedResource = blockedResource;
		_parent = parent;
		_priority = priority;

	}

	/* Getters and Setters */

	public String getPID() {
		return _PID;
	}

	public void setPID(String PID) {
		_PID = PID;
	}

	public ArrayList<ResourceQuantity> getOtherResources() {
		return _otherResources;
	}

	public void setOtherResources(ArrayList<ResourceQuantity> otherResources) {
		_otherResources = otherResources;
	}

	public STATUS_TYPE getStatusType() {
		return _statusType;
	}

	public void setStatusType(String type) {
		_statusType = getStatusType(type);
		if (_statusType == STATUS_TYPE.BLOCKED) {
			_readyList = null;
		}
		else {
			_blockedResource = null;
		}
	}

	public Queue<PCB> getReadyList() {
		return _readyList;
	}

	public void setReadyList(Queue<PCB> readyList) {
		_readyList = readyList;
	}

	public RCB getBlockedResource() {
		return _blockedResource;
	}

	public void setBlockedResource(RCB blockedResource) {
		_blockedResource = blockedResource;
	}

	public PCB getParent() {
		return _parent;
	}

	public void setParent(PCB parent) {
		_parent = parent;
	}

	public ArrayList<PCB> getChildren() {
		return _children;
	}

	public void setChildren(ArrayList<PCB> children) {
		_children = children;
	}

	public int getPriority() {
		return _priority;
	}

	public void setPriority(int priority) throws Exception {
		if (!_PID.equalsIgnoreCase("init") && priority == INIT_PRIORITY) {
			throw new IllegalArgumentException("only init can have priority "
					+ INIT_PRIORITY);
		}
		_priority = priority;
	}

	/* public methods */
	public void acquireResource(RCB resource, int quantity) {
		ResourceQuantity obtained = alreadyObtained(resource);
		if (obtained != null) {
			int obtainedQuantity = obtained.getQuantity();
			obtained.setQuantity(obtainedQuantity + quantity);
		}
		else {
			obtained = new ResourceQuantity(resource, quantity);
			_otherResources.add(obtained);
		}
	}

	public void releaseResource(RCB resource, int quantity) throws Exception {
		ResourceQuantity obtained = alreadyObtained(resource);
		if (obtained == null) {
			throw new IllegalArgumentException("Process does not have "
					+ resource.getRID());
		} else {
			if (obtained.getQuantity() < quantity) {
				throw new IllegalArgumentException("Process does not have "
						+ quantity + " of " + resource.getRID());
			} else if (obtained.getQuantity() == quantity) {
				_otherResources.remove(obtained);
			} else {
				int oldQuantity = obtained.getQuantity();
				obtained.setQuantity(oldQuantity - quantity);
			}
		}
	}

	public void addChild(PCB process) {
		_children.add(process);
	}

	public String toString() {
		String resources = "";
		for (ResourceQuantity resourceqty : _otherResources) {
			resources.concat(resourceqty.toString());
		}
		String statusType;
		switch (_statusType) {
			case READY :
				statusType = "Ready";
				break;
			case RUNNING :
				statusType = "Running";
				break;
			case BLOCKED :
				statusType = "Blocked";
				break;
			case INVALID :
				statusType = "Invalid";
				break;
			default:
				statusType = "unkown";
		}
		String statusList;
		if (_readyList == null) {
			statusList = "Ready List is null";
		} else {
			statusList = "Pointing to ready list";
		}

		return "PID: " + _PID
				+ "\nResources: " + resources
				+ "\nStatus Type: " + statusType
				+ "\nReady list: " + statusList
				+ "\nBlocked Resource: " + _blockedResource
				+ "\nParent: " + _parent
				+ "\nChildren: " + _children
				+ "\nPriority: " + _priority;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o == this) {
			return true;
		}
		if (!(o instanceof PCB)) {
			return false;
		}
		PCB other = (PCB) o;
		if (other.getPID().equalsIgnoreCase(_PID)) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return _PID.hashCode();
	}

	/* private methods */
	private STATUS_TYPE getStatusType(String type) {
		if (type.equalsIgnoreCase("ready")) {
			return STATUS_TYPE.READY;
		}
		else if (type.equalsIgnoreCase("running")) {
			return STATUS_TYPE.RUNNING;
		}
		else if (type.equalsIgnoreCase("blocked")) {
			return STATUS_TYPE.BLOCKED;
		}
		else {
			return STATUS_TYPE.INVALID;
		}
	}

	/**
	 * Checks if the process has already acquired the resource.
	 * 
	 * @param resource
	 *            to check for
	 * @return null if the resource has yet to be acquired or a ResourceQuantity
	 *         pair if it has already been acquired
	 */
	private ResourceQuantity alreadyObtained(RCB resource) {
		for (ResourceQuantity obtained : _otherResources) {
			if (obtained.getResource().equals(resource)) {
				return obtained;
			}
		}
		return null;
	}

	@Override
	public int compareTo(PCB o) {
		if(_priority>o.getPriority()){
			return -1;
		}
		if(_priority<o.getPriority()){
			return 1;
		}
		return 0;
	}


}
