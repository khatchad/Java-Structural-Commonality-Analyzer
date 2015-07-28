package edu.cuny.citytech.analyzecommonality.ui;

import java.io.BufferedWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.util.SelectionUtil;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.cuny.citytech.analyzecommonality.core.analysis.StructuralCommonalityAnalyzer;
import edu.cuny.citytech.analyzecommonality.core.model.JavaElementSet;

@SuppressWarnings("restriction")
public class AnalyzeStructuralCommonalityHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
		List<?> list = SelectionUtil.toList(selection);
		Set<IJavaElement> elements = list.stream().filter(e -> e instanceof IJavaElement).map(e -> (IJavaElement) e)
				.collect(Collectors.toSet());
		JavaElementSet set = new JavaElementSet(elements, "some bug", "security");

		StructuralCommonalityAnalyzer analyzer = new StructuralCommonalityAnalyzer(2);
		try {
			NullProgressMonitor monitor = new NullProgressMonitor();
			analyzer.analyze(set, monitor, null);

			URI uri = ResourcesPlugin.getWorkspace().getRoot().getLocationURI();
			Path path = Paths.get(uri);
			path = path.resolve(Messages.patternFileName);

			try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE)) {
				analyzer.dumpCSV(writer);
			}
		} catch (Exception e) {
			throw new ExecutionException(null, e);
		}

		return null;
	}
}
