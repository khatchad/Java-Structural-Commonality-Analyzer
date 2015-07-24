/**
 * 
 */
package edu.cuny.citytech.analyzecommonality.core.analysis.util;

import org.eclipse.jdt.core.IJavaElement;
import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * @author raffi
 *
 */
public class XMLUtil {
	private XMLUtil() {
	}

	/**
	 * @param elem
	 * @return
	 */
	public static Element getXML(IJavaElement elem) {
		String handleIdentifier = null;

		try {
			handleIdentifier = elem.getHandleIdentifier();
		} catch (NullPointerException e) {
			System.err.println("Can't retrieve element handler for: " + elem);
			System.exit(-1);
		}

		return getXML(elem, handleIdentifier, elem.getElementName(), String.valueOf(elem.getElementType()));
	}

	public static Element getXML(Object obj, String elementIdentifier, String elementName, String elementType) {
		Element ret = new Element(obj.getClass().getSimpleName());

		ret.setAttribute(new Attribute("id", elementIdentifier));
		ret.setAttribute(new Attribute("name", elementName));
		ret.setAttribute(new Attribute("type", elementType));

		return ret;
	}
}
