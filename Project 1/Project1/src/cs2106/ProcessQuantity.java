package cs2106;

/**
 * 
 * @author Victor Hazali A0110741X
 * 
 */
public class ProcessQuantity {

	/* member variables */
	private PCB	_process;
	private int	_quantity;

	/* Constructors */
	public ProcessQuantity(PCB process, int quantity) {
		_process = process;
		_quantity = quantity;
	}

	/* getters and setters */
	public PCB getProcess() {
		return _process;
	}

	public void setProcess(PCB process) {
		_process = process;
	}

	public int getQuantity() {
		return _quantity;
	}

	public void setQuantity(int quantity) {
		_quantity = quantity;
	}

	/* public methods */
	public String toString() {
		return "PID: " + _process.getPID() + " quantity: " + _quantity;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o == this) {
			return true;
		}
		if (!(o instanceof ProcessQuantity)) {
			return false;
		}
		ProcessQuantity other = (ProcessQuantity) o;
		if (other.getProcess().equals(_process)
				&& other.getQuantity() == _quantity) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return _process.hashCode() + _quantity;
	}
}
