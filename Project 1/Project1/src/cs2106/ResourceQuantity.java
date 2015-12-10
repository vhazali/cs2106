package cs2106;

/**
 * 
 * @author Victor Hazali A0110741X
 * 
 */
public class ResourceQuantity {

	/* member variables */
	private RCB	_resource;
	private int	_quantity;

	/* Constructors */
	public ResourceQuantity(RCB resource, int quantity) {
		_resource = resource;
		_quantity = quantity;
	}

	/* Getters and Setters */
	public RCB getResource() {
		return _resource;
	}

	public void setResource(RCB resource) {
		_resource = resource;
	}

	public int getQuantity() {
		return _quantity;
	}

	public void setQuantity(int quantity) {
		_quantity = quantity;
	}

	/* public methods */
	public String toString() {
		return "RID: " + _resource.getRID() + " quantity: " + _quantity;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o == this) {
			return true;
		}
		if (!(o instanceof ResourceQuantity)) {
			return false;
		}
		ResourceQuantity other = (ResourceQuantity) o;
		if (other.getResource().equals(_resource)
				&& other.getQuantity() == _quantity) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return _resource.hashCode() + _quantity;
	}
}
