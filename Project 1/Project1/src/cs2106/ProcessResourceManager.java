package cs2106;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.HashMap;
import java.util.Scanner;

import cs2106.PCB.STATUS_TYPE;

/**
 * 
 * @author Victor Hazali A0110741X
 * 
 */
public class ProcessResourceManager {

	enum COMMAND_TYPE {
		CREATE, INIT, DESTROY, SHOW_PROCESS, SHOW_RESOURCE, LIST_PROCESSES,
		LIST_RESOURCES, REQUEST, RELEASE, TIMEOUT, SCHEDULER, INVALID
	};

	/* Constants */
	public static final int				INIT_PRIORITY			= 0;
	public static final int				SYSTEM_PRIORITY			= 1;
	public static final int				USER_PRIORITY			= 2;

	public static final int				PID_POSITION			= 0;
	public static final int				PRIORITY_POSITION		= 1;
	public static final int				RID_POSITION			= 0;
	public static final int				QUANTITY_POSITION		= 1;

	public static final int				R1_ALLOCATED			= 1;
	public static final int				R2_ALLOCATED			= 2;
	public static final int				R3_ALLOCATED			= 3;
	public static final int				R4_ALLOCATED			= 4;

	public static final String			STATUS_READY			= "ready";
	public static final String			STATUS_BLOCKED			= "blocked";
	public static final String			STATUS_RUNNING			= "running";

	public static final String			INVALID_COMMAND			= "%1s is an invalid command.";
	public static final String			INVALID_PARAMETERS		= "%1s does not have valid parameters for command %2s";
	public static final String			INVALID_PRIORITY		= "Not able to create process with %1d priority";
	public static final String			MESSAGE_DELETE_ANCESTOR	= "Not able to delete %1s as it is an ancestor";

	/* member variables */
	private static PriorityQueue<PCB>	_readyList;
	private static HashMap<String, PCB>	_PCBList;
	private static ArrayList<RCB>		_RCBList;
	private static COMMAND_TYPE			_command;
	private static String[]				_parameters;
	private static PCB					_running;

	public static Scanner				sc						= new Scanner(
																		System.in);

	/* public methods */
	public static void main(String[] args) {
		initialise();
		scheduler();
		showToUser(_running.getPID());
		run();
	}

	/* private methods */

	/**
	 * Main logic method. The method will continuously asks for user input and
	 * execute the input.
	 */
	private static void run() {
		while (true) {
			String input;
			try {
				input = readInputFromUser();
				executeCommand(input);
				scheduler();
				showToUser(_running.getPID());

			} catch (Exception e) {
				System.out.println("Error");
				e.printStackTrace();
			}
		}

	}

	/**
	 * Reads input from user and validates the input
	 * 
	 * @return string containing user input
	 */
	private static String readInputFromUser() throws Exception {
		String input = sc.nextLine();
		validateCommand(input);
		validateParameters(input);
		return input;
	}

	/**
	 * validates the command within the input string
	 * 
	 * @param input
	 *            string from user
	 * @throws Exception
	 *             if the user enters an invalid command
	 */
	private static void validateCommand(String input) throws Exception {
		if (input.trim() == "") {
			throw new Exception(String.format(INVALID_COMMAND, input));
		}
		_command = getCommand(input);
		// if (_command == COMMAND_TYPE.INVALID) {
		// throw new Exception(String.format(INVALID_COMMAND, input));
		// }

	}

	/**
	 * validates the parameters within the input string
	 * 
	 * @param input
	 *            string from user
	 * @throws Exception
	 *             if the user did not provide the required parameters
	 */
	private static void validateParameters(String input) throws Exception {
		String parameters = removeFirstWord(input);
		if (parameters.trim() == "") {
			throw new Exception(String.format(INVALID_PARAMETERS, input));
		}
		_parameters = getParameters(parameters);
		/*
		 * Assume all inputs are correct switch(_command){ case CREATE:
		 * if(_parameters.length<2){ throw new
		 * Exception(String.format(INVALID_PARAMETERS, parameters,_command)); }
		 * if(Integer.parseInt(_parameters[PRIORITY_POSITION])==INIT_PRIORITY){
		 * throw new Exception(String.format(INVALID_PRIORITY, INIT_PRIORITY));
		 * } break; default: throw new Error("Command unrecognized"); }
		 */
	}

	/**
	 * executes the command given by the user
	 * 
	 * @param input
	 *            string from user
	 */
	private static void executeCommand(String input) throws Exception {
		switch (_command) {
			case INIT :
				initialise();
				break;
			case CREATE :
				createProcess();
				break;
			case DESTROY :
				destroyProcess();
				break;
			case REQUEST :
				requestResource();
				break;
			case RELEASE :
				releaseResource();
				break;
			case TIMEOUT :
				timeout();
				break;
			case SHOW_PROCESS :
				showProcess();
				break;
			case SHOW_RESOURCE :
				showResource();
				break;
			case LIST_PROCESSES :
				listProcesses();
				break;
			case LIST_RESOURCES :
				listResources();
				break;
			default:
				throw new Exception("Command unrecognized");
		}
	}

	/**
	 * Initializes the system. Sets all lists to new list, Creates the init
	 * process and runs it, resets all resources.
	 */
	private static void initialise() {
		initialiseLists();
		createInit();
		initialiseResources();
	}

	/**
	 * creates the init process
	 */
	private static void createInit() {
		PCB init = new PCB();
		_PCBList.put("init", init);
		_readyList.add(init);
	}

	/**
	 * Resets all lists to a brand new list. Namely the readyList, PCBList and
	 * RCBList are renewed.
	 */
	private static void initialiseLists() {
		_readyList = new PriorityQueue<PCB>();
		_PCBList = new HashMap<String, PCB>();
		_RCBList = new ArrayList<RCB>();
	}

	/**
	 * Initializes all resources to their starting stage
	 */
	private static void initialiseResources() {
		_RCBList.add(new RCB("r1", R1_ALLOCATED));
		_RCBList.add(new RCB("r2", R2_ALLOCATED));
		_RCBList.add(new RCB("r3", R3_ALLOCATED));
		_RCBList.add(new RCB("r4", R4_ALLOCATED));
	}

	/**
	 * creates a new process as specified by user. The process is then added to
	 * the readyList and PCBList
	 * 
	 * @precondition PID can not be taken. If another process with the same PID
	 *               has been created, nothing will be done.
	 */
	private static void createProcess() throws Exception {
		if (!isValidPID(_parameters[PID_POSITION])) {
			throw new Exception("PID taken");
		}
		try {
			PCB process = new PCB(_parameters[PID_POSITION], STATUS_READY,
					_readyList, _running,
					Integer.parseInt(_parameters[PRIORITY_POSITION]));
			_readyList.add(process);
			_PCBList.put(_parameters[PID_POSITION], process);
			_running.addChild(process);
		} catch (Exception e) {
			showToUser(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Checks if PID is already taken
	 * 
	 * @param string
	 *            PID entered by user
	 * @return true if it's not taken, false if it's already taken.
	 */
	private static boolean isValidPID(String string) {
		if (_PCBList.containsKey(string)) {
			return false;
		}
		return true;
	}

	/**
	 * Destroys the process specified by user. All Children of the process
	 * specified will also be destroyed, and all resources held by the processes
	 * are released.
	 * 
	 * @precondition Process specified can not be an ancestor of current running
	 *               process.
	 */
	private static void destroyProcess() throws Exception {
		PCB toDelete = getPCB(_parameters[PID_POSITION]);
		if (isAncestor(toDelete)) {
			showToUser(String
					.format(MESSAGE_DELETE_ANCESTOR, toDelete.getPID()));
		}
		else {
			for (PCB process : toDelete.getChildren()) {
				destroyProcess(process);
			}
			destroyProcess(toDelete);
			if (toDelete.equals(_running)) {
				_running = null;
			}
		}
	}

	/**
	 * Method to facilitate destroying of children processes
	 * 
	 * @param process
	 *            to be destroyed
	 */
	private static void destroyProcess(PCB process) throws Exception {
		removeFromWaitingList(process);
		_readyList.remove(process);
		_PCBList.remove(process.getPID());
		for (ResourceQuantity rq : process.getOtherResources()) {
			releaseResource(rq, process);
		}
	}

	/**
	 * Removes process from waiting list of resource it is blocked by.
	 * 
	 * @param process
	 *            process that needs to be removed from list
	 */
	private static void removeFromWaitingList(PCB process) {
		if (process.getStatusType() == STATUS_TYPE.BLOCKED) {
			try {
				process.getBlockedResource().dequeueProcess(process);
			} catch (Exception e) {
				showToUser(e.getMessage());
			}
		}
	}

	/**
	 * Checks if the process is an ancestor of the running process
	 * 
	 * @param toDelete
	 *            the process we are concerned with
	 * @return true if toDelete is an ancestor of running process, false
	 *         otherwise
	 */
	private static boolean isAncestor(PCB toDelete) {
		PCB current = _running.getParent();
		while (current != getPCB("init")) {
			if (current.equals(toDelete)) {
				return true;
			}
			current = current.getParent();
		}
		return false;
	}

	/**
	 * Requests a resource for the current running process
	 */
	private static void requestResource() {
		RCB resource = getRCB(_parameters[RID_POSITION]);
		int quantity = Integer.parseInt(_parameters[QUANTITY_POSITION]);
		if (isResourceSufficient(resource, quantity)) {
			resource.allocateResource(quantity);
			_running.acquireResource(resource, quantity);
		} else {
			try {
				resource.enqueueProcess(_running, quantity);
			} catch (Exception e) {
				showToUser(e.getMessage());
			}
			blockRunningProcess(resource);
		}
	}

	/**
	 * checks if resource needed has sufficient available units to satisfy
	 * request
	 * 
	 * @param resource
	 *            resource that is to be checked
	 * @param quantity
	 *            the amount requested
	 * @return true if resource has sufficient available units. false otherwise
	 */
	private static boolean isResourceSufficient(RCB resource, int quantity) {
		if (resource.getAvailable() >= quantity) {
			return true;
		}
		return false;
	}

	/**
	 * Blocks the current running process.
	 * 
	 * @param resource
	 *            Resource that causes the process to be blocked.
	 */
	private static void blockRunningProcess(RCB resource) {
		_running.setStatusType(STATUS_BLOCKED);
		_running.setBlockedResource(resource);
		_readyList.remove(_running);
		_running = null;
	}

	/**
	 * Releases resource from current running process
	 * 
	 * @throws Exception
	 *             if the resource and specified quantity has not been allocated
	 *             to process
	 */
	private static void releaseResource() throws Exception {
		RCB resource = getRCB(_parameters[RID_POSITION]);
		int quantity = Integer.parseInt(_parameters[QUANTITY_POSITION]);
		if (isAllocated(resource, quantity)) {
			resource.deallocateResource(quantity);
			_running.releaseResource(resource, quantity);
			serviceWaitingList(resource);
		} else {
			throw new Exception("Resource not allocated to process");
		}
	}

	/**
	 * Checks if the resource and at least the quantity is already allocated to
	 * the running process
	 * 
	 * @param resource
	 *            resource to release
	 * @param quantity
	 *            number of units of the resource to release
	 * @return true if the process has at least the amount of the resource,
	 *         false otherwise
	 */
	private static boolean isAllocated(RCB resource, int quantity) {
		for (ResourceQuantity rq : _running.getOtherResources()) {
			if (rq.getResource().equals(resource)) {
				if (rq.getQuantity() >= quantity) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Services the waiting list of a resource that has recently been released.
	 * It will attempt to allocate resources to processes on its waiting list
	 * 
	 * @param resource
	 *            the resource that has just been released
	 * @throws Exception
	 */
	private static void serviceWaitingList(RCB resource) throws Exception {
		ArrayList<ProcessQuantity> waitingList = resource.getWaitingList();
		ProcessQuantity servicable = null;
		for (ProcessQuantity pq : waitingList) {
			if (resource.getAvailable() >= pq.getQuantity()) {
				servicable = pq;
			}
		}
		if (servicable != null) {
			resource.dequeueProcess(servicable.getProcess());
			resource.allocateResource(servicable.getQuantity());
			servicable.getProcess().acquireResource(resource,
					servicable.getQuantity());
			servicable.getProcess().setStatusType(STATUS_READY);
			_readyList.add(servicable.getProcess());
		}
	}

	private static void releaseResource(ResourceQuantity rq, PCB process)
			throws Exception {
		RCB resource = rq.getResource();
		int quantity = rq.getQuantity();
		if (isAllocated(resource, quantity, process)) {
			resource.deallocateResource(quantity);
			process.releaseResource(resource, quantity);
			serviceWaitingList(resource);
		} else {
			throw new Exception("Resource not allocated to process");
		}
	}

	private static boolean isAllocated(RCB resource, int quantity, PCB process) {
		for (ResourceQuantity rq : process.getOtherResources()) {
			if (rq.getResource().equals(resource)) {
				if (rq.getQuantity() >= quantity) {
					return true;
				}
			}
		}
		return false;
	}

	private static void scheduler() {
		PCB highestPriority = _readyList.peek();
		if (_running == null
				|| _running.getPriority() < highestPriority.getPriority()) {
			_running = highestPriority;
			highestPriority.setStatusType(STATUS_RUNNING);
		}
	}

	private static void timeout() {
		PCB current = _running;
		_running = null;
		_readyList.remove(current);
		current.setStatusType(STATUS_READY);
		_readyList.add(current);
	}

	private static void showProcess() {
		PCB toShow = getPCB(_parameters[PID_POSITION]);
		showToUser(toShow.toString());
	}

	private static void showResource() {
		RCB toShow = getRCB(_parameters[RID_POSITION]);
		showToUser(toShow.toString());
	}

	private static void listProcesses() {
		showToUser(_readyList.toString());
	}

	private static void listResources() {
		// TODO Auto-generated method stub

	}

	private static COMMAND_TYPE getCommand(String input) {
		String command = getFirstWord(input);

		if (command == null) {
			throw new Error(String.format(INVALID_COMMAND, command));
		}
		if (command.equalsIgnoreCase("init")) {
			return COMMAND_TYPE.INIT;
		} else if (command.equalsIgnoreCase("cr")) {
			return COMMAND_TYPE.CREATE;
		} else if (command.equalsIgnoreCase("de")) {
			return COMMAND_TYPE.DESTROY;
		} else if (command.equalsIgnoreCase("req")) {
			return COMMAND_TYPE.REQUEST;
		} else if (command.equalsIgnoreCase("rel")) {
			return COMMAND_TYPE.RELEASE;
		} else if (command.equalsIgnoreCase("to")) {
			return COMMAND_TYPE.TIMEOUT;
		} else if (command.equalsIgnoreCase("lspro")) {
			return COMMAND_TYPE.LIST_PROCESSES;
		} else if (command.equalsIgnoreCase("lsrsc")) {
			return COMMAND_TYPE.LIST_RESOURCES;
		} else if (command.equalsIgnoreCase("showpro")) {
			return COMMAND_TYPE.SHOW_PROCESS;
		} else if (command.equalsIgnoreCase("showrsc")) {
			return COMMAND_TYPE.SHOW_RESOURCE;
		} else {
			return COMMAND_TYPE.INVALID;
		}
	}

	/**
	 * Gets a PCB from the PCB list when given a PID
	 * 
	 * @param PID
	 *            PID of the process
	 * @return PCB from PCBList
	 */
	private static PCB getPCB(String PID) {
		return _PCBList.get(PID);
	}

	/**
	 * Gets a RCB from the RCB list when given a RID
	 * 
	 * @param string
	 *            RID of the resource
	 * @return RCB from RCBList
	 */
	private static RCB getRCB(String RID) {
		for (RCB resource : _RCBList) {
			if (resource.getRID().equalsIgnoreCase(RID)) {
				return resource;
			}
		}
		return null;
	}

	private static String[] getParameters(String parameters) {
		return parameters.split("\\s+");
	}

	private static String getFirstWord(String message) {
		return message.trim().split("\\s+")[0];
	}

	private static String removeFirstWord(String message) {
		return message.replace(getFirstWord(message), "").trim();
	}

	private static void showToUser(String message) {
		System.out.println(message);

	}
}