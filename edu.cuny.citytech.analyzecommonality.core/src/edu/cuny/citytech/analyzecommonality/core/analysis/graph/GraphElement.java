/**
 * 
 */
package edu.cuny.citytech.analyzecommonality.core.analysis.graph;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;

/**
 * @author raffi
 * 
 * @param <E>
 */
public abstract class GraphElement<E> implements Serializable {

	private static final long serialVersionUID = 1905353972018475367L;

	private static final String SELECTED = "selected";

	private final PropertyChangeSupport changes = new PropertyChangeSupport(this);

	private boolean selected;

	/**
	 * 
	 */
	public GraphElement() {
		super();
	}

	public void addPropertyChangeListener(final PropertyChangeListener l) {
		this.changes.addPropertyChangeListener(l);
	}

	public void deselect() {
		final boolean oldState = this.selected;
		this.selected = false;
		if (oldState != this.selected)
			this.changes.firePropertyChange(new PropertyChangeEvent(this, SELECTED, oldState, this.selected));
	}

	/**
	 * @param selected
	 *            the enabled to set
	 */
	public void select() {
		final boolean oldState = this.selected;
		this.selected = true;
		if (oldState != this.selected)
			this.changes.firePropertyChange(new PropertyChangeEvent(this, SELECTED, oldState, this.selected));
	}

	/**
	 * @return the enabled
	 */
	public boolean isSelected() {
		return this.selected;
	}

	public void removePropertyChangeListener(final PropertyChangeListener l) {
		this.changes.removePropertyChangeListener(l);
	}

	@Override
	public String toString() {
		return this.selected ? "*" : "";
	}

	/**
	 * @return
	 */
	public Element getXML() {
		Element ret = new Element(this.getClass().getSimpleName());
		ret.setAttribute(SELECTED, String.valueOf(this.isSelected()));
		return ret;
	}

	public static boolean isSelected(Element elem) throws DataConversionException {
		Attribute enabledAttribute = elem.getAttribute(SELECTED);
		return enabledAttribute.getBooleanValue();
	}

	public GraphElement(Element elem) throws DataConversionException {
		this.selected = isSelected(elem);
	}

	/**
	 * @return
	 */
	public abstract String getLongDescription();

	/**
	 * @return
	 */
	public abstract String toPrettyString();
}