package edu.cuny.citytech.analyzecommonality.ui;

import org.eclipse.osgi.util.NLS;

/**
 * @author <a href="mailto:rkhatchadourian@citytech.cuny.edu">Raffi
 *         Khatchadourian</a>
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "edu.cuny.citytech.analyzecommonality.ui.messages"; //$NON-NLS-1$

	public static String patternFileName;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages() {
	}
}
