package edu.cuny.citytech.analyzecommonality.ui.findbugs;

import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class Plugin extends AbstractUIPlugin {

	private static Plugin plugin;
	private static String SYMBOLIC_NAME;

	public Plugin() {
		super();
		plugin = this;
		SYMBOLIC_NAME = getBundle().getSymbolicName();
	}

	public static Plugin getDefault() {
		return plugin;
	}

	public void logWarning(String message, Throwable e) {
		log(Status.WARNING, message, e);
	}

	public void log(int severity, String message, Throwable e) {
		Status status;

		if (e != null)
			status = new Status(severity, SYMBOLIC_NAME, message, e);
		else
			status = new Status(severity, SYMBOLIC_NAME, message);

		getLog().log(status);
	}

	public void logInfo(String message) {
		log(Status.INFO, message);
	}

	public void logWarning(String message) {
		log(Status.WARNING, message);
	}

	public void log(int severity, String message) {
		log(severity, message, null);
	}
}
