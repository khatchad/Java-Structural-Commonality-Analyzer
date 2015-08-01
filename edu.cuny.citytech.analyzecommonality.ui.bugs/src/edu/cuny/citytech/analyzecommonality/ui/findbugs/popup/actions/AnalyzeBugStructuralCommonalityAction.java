package edu.cuny.citytech.analyzecommonality.ui.findbugs.popup.actions;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.google.common.collect.Sets;

import de.tobject.findbugs.FindBugsJob;
import de.tobject.findbugs.FindbugsPlugin;
import de.tobject.findbugs.actions.FindBugsAction;
import de.tobject.findbugs.builder.ResourceUtils;
import de.tobject.findbugs.builder.WorkItem;
import edu.cuny.citytech.analyzecommonality.core.analysis.StructuralCommonalityAnalyzer;
import edu.cuny.citytech.analyzecommonality.core.model.JavaElementSet;
import edu.cuny.citytech.analyzecommonality.ui.findbugs.Plugin;
import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.MethodAnnotation;

public class AnalyzeBugStructuralCommonalityAction extends FindBugsAction {

	private static final Plugin plugin = Plugin.getDefault();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.tobject.findbugs.actions.FindBugsAction#run(org.eclipse.jface.action.
	 * IAction)
	 */
	@Override
	public void run(IAction action) {
		if (selection == null || selection.isEmpty()) {
			return;
		}

		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sSelection = (IStructuredSelection) selection;

			// find bugs.
			super.run(action);

			// analyze commonality.
			Job job = new Job("Analyzing structural commonality from bugs.") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					// wait for findbugs to finish.
					Job[] jobs = Job.getJobManager().find(FindbugsPlugin.class);
					Arrays.asList(jobs).stream().filter(j -> j instanceof FindBugsJob).forEach(j -> {
						try {
							j.join();
						} catch (InterruptedException e) {
							plugin.logWarning("Error waiting for FindBugs job: " + j.getName() + " to finish.", e);
							e.printStackTrace();
						}
					});

					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;

					// analyze the bug collection.
					// get the projects.
					Map<IProject, List<WorkItem>> resourcesPerProject = ResourceUtils
							.getResourcesPerProject(sSelection);

					// TODO: Should do this in parallel but the CSV files need
					// to be different for each project (or does it).
					resourcesPerProject.keySet().forEach(project -> {
						// get the bug report.
						BugCollection bugCollection = null;
						try {
							bugCollection = FindbugsPlugin.getBugCollection(project, monitor);
						} catch (CoreException e) {
							plugin.logWarning("Failed to get bug collection for: " + project.getName(), e);
							e.printStackTrace();
						}

						if (bugCollection != null) {
							Spliterator<BugInstance> spliterator = bugCollection.spliterator();
							Stream<BugInstance> stream = StreamSupport.stream(spliterator, false);

							// TODO: In parallel?
							// group bugs by their categories.
							Map<String, List<BugInstance>> categoryToBugInstanceMap = stream
									.collect(Collectors.groupingBy(b -> b.getBugPattern().getCategory()));

							Map<String, Set<IJavaElement>> categoryToRelatedJavaElementsMap = new LinkedHashMap<>();

							categoryToBugInstanceMap.keySet().forEach(category -> {
								categoryToBugInstanceMap.get(category).forEach(bug -> {
									Set<IJavaElement> relatedJavaElements = getRelatedJavaElements(bug, project,
											monitor);
									categoryToRelatedJavaElementsMap.merge(category, relatedJavaElements, Sets::union);
								});
							});

							// at this point, we have a mapping from categories
							// to related IJavaElements.
							// now we can create JavaElementSets.
							Collection<JavaElementSet> javaElementSetCollection = new LinkedHashSet<>();

							categoryToBugInstanceMap.keySet().forEach(category -> {
								Set<IJavaElement> set = categoryToRelatedJavaElementsMap.get(category);
								javaElementSetCollection.add(new JavaElementSet(set, null, category));
							});

							// feed them to the analyzer.
							StructuralCommonalityAnalyzer analyzer = new StructuralCommonalityAnalyzer(1);
							try {
								analyzer.analyze(javaElementSetCollection, monitor, null);
							} catch (Exception e) {
								plugin.logError("Couldn't analyze java elements.", e);
								throw new RuntimeException(e);
							}

							Path path = Paths.get("patterns.csv");
							plugin.logInfo("Storing results in: " + path.toAbsolutePath());
							
							try (Writer writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE)) {
								writer.toString();
								analyzer.dumpCSV(writer);
							} catch (IOException e) {
								plugin.logWarning("Failed to write CSV file.", e);
								throw new RuntimeException(e);
							}
						}
					});

					return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
				}
			};

			job.setPriority(Job.LONG);
			job.schedule();
		}
	}

	private static Set<IJavaElement> getRelatedJavaElements(BugInstance instance, IProject project,
			IProgressMonitor monitor) {
		Set<IJavaElement> ret = new LinkedHashSet<>();
		IJavaProject javaProject = JavaCore.create(project);

		if (javaProject != null && javaProject.exists()) {
			try {
				if (!javaProject.isOpen())
					javaProject.open(monitor);
				if (!javaProject.isConsistent())
					javaProject.makeConsistent(monitor);
			} catch (JavaModelException e) {
				plugin.logError("Error reading project: " + project.getName() + ".", e);
				return ret;
			}

			// TODO: Analyze more than the primary method?
			MethodAnnotation methodAnnotation = instance.getPrimaryMethod();

			if (methodAnnotation != null) {
				IType type;
				try {
					type = javaProject.findType(methodAnnotation.getClassName());
				} catch (JavaModelException e) {
					plugin.logWarning("Can't get type.", e);
					e.printStackTrace();
					return Collections.emptySet();
				}

				String methodName = methodAnnotation.getMethodName();
				String methodSignature = methodAnnotation.getMethodSignature();
				String[] parameterTypes = Signature.getParameterTypes(methodSignature);
				IMethod method = type.getMethod(methodName, parameterTypes);

				if (method.exists()) {
					plugin.logInfo("Found method: " + methodAnnotation.getMethodName());
					ret.add(method);
				}
				else
					plugin.logError("Method: " + methodAnnotation.getMethodName() + " does not exist.");
			} else
				plugin.logWarning(
						"Could not find related Java elements for bug instance: " + instance.getInstanceKey());
		}

		return ret;
	}
}
