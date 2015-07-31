package edu.cuny.citytech.analyzecommonality.ui.findbugs.popup.actions;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.util.SelectionUtil;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import edu.cuny.citytech.analyzecommonality.ui.findbugs.Plugin;

@SuppressWarnings("restriction")
public class DisplayHandleIdentifierAction implements IObjectActionDelegate {

	private Shell shell;
	private ISelection selection;

	/**
	 * Constructor for Action1.
	 */
	public DisplayHandleIdentifierAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		Object element = SelectionUtil.getSingleElement(this.selection);

		if (element != null && element instanceof IJavaElement) {
			IJavaElement jElem = (IJavaElement) element;

			Plugin.getDefault().logInfo(jElem.getHandleIdentifier());
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

}
