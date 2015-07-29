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

	private static final String SELECTED = "enabled";

	private final PropertyChangeSupport changes = new PropertyChangeSupport(this);

	private boolean enabled;

	public GraphElement(boolean enabled) {
		super();
		this.setEnabled(enabled);
	}

	public void addPropertyChangeListener(final PropertyChangeListener l) {
		this.changes.addPropertyChangeListener(l);
	}

	public void enable() {
		this.setEnabled(false);
	}

	public void disable() {
		this.setEnabled(true);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		final boolean oldState = this.enabled;
		this.enabled = enabled;
		if (oldState != this.enabled)
			this.changes.firePropertyChange(new PropertyChangeEvent(this, SELECTED, oldState, this.enabled));
	}

	public void removePropertyChangeListener(final PropertyChangeListener l) {
		this.changes.removePropertyChangeListener(l);
	}

	@Override
	public String toString() {
		return this.enabled ? "*" : "";
	}

	public Element getXML() {
		Element ret = new Element(this.getClass().getSimpleName());
		ret.setAttribute(SELECTED, String.valueOf(this.isEnabled()));
		return ret;
	}

	public static boolean isEnabled(Element elem) throws DataConversionException {
		Attribute enabledAttribute = elem.getAttribute(SELECTED);
		return enabledAttribute.getBooleanValue();
	}

	public GraphElement(Element elem) throws DataConversionException {
		this.setEnabled(isEnabled(elem));
	}

	public abstract String getLongDescription();

	public abstract String toPrettyString();
}