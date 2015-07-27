package edu.cuny.citytech.analyzecommonality.core.analysis;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jdom2.Element;

import ca.mcgill.cs.swevo.jayfx.ConversionException;
import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.util.TimeCollector;
import edu.cuny.citytech.analyzecommonality.core.analysis.graph.ConcernGraph;
import edu.cuny.citytech.analyzecommonality.core.analysis.graph.GraphElement;
import edu.cuny.citytech.analyzecommonality.core.analysis.graph.IntentionArc;
import edu.cuny.citytech.analyzecommonality.core.analysis.graph.Pattern;
import edu.cuny.citytech.analyzecommonality.core.model.JavaElementSet;

public class StructuralCommonalityAnalyzer extends StructuralCommonalityProcessor {

	private static final String ENABLING_GRAPH_ELEMENTS_FOR_EACH_SET = "Enabling graph elements for each set ({0}) of java elements.";

	private Map<JavaElementSet, Element> javaElementSetToXMLMap = new LinkedHashMap<>();

	private Map<JavaElementSet, Set<Pattern<IntentionArc<IElement>>>> javaElementSetToPatternSetMap = new LinkedHashMap<JavaElementSet, Set<Pattern<IntentionArc<IElement>>>>();

	private static Logger logger = Logger.getLogger(StructuralCommonalityAnalyzer.class.getName());

	public StructuralCommonalityAnalyzer(int maximumAnalysisDepth) {
		super(maximumAnalysisDepth);
	}

	@Override
	protected void analyzeElementSetCollection(final Collection<? extends JavaElementSet> setCol,
			final ConcernGraph graph, final IProgressMonitor monitor, TimeCollector timeCollector)
					throws ConversionException, CoreException, IOException {

		monitor.beginTask(ENABLING_GRAPH_ELEMENTS_FOR_EACH_SET, setCol.size());

		if (timeCollector != null)
			timeCollector.start();

		logger.log(Level.INFO, ENABLING_GRAPH_ELEMENTS_FOR_EACH_SET, setCol.size());

		if (timeCollector != null)
			timeCollector.stop();

		for (final JavaElementSet set : setCol) {

			final Map<Pattern<IntentionArc<IElement>>, Set<GraphElement<IElement>>> patternToResultMap = new LinkedHashMap<Pattern<IntentionArc<IElement>>, Set<GraphElement<IElement>>>();
			final Map<Pattern<IntentionArc<IElement>>, Set<GraphElement<IElement>>> patternToEnabledElementMap = new LinkedHashMap<Pattern<IntentionArc<IElement>>, Set<GraphElement<IElement>>>();

			graph.enableElementsAccordingTo(set, monitor);
			
			String dotFormat = graph.toDotFormat();
			System.out.println(dotFormat);

			executeQueries(graph.getWorkingMemory(), patternToResultMap, patternToEnabledElementMap, monitor);

			for (final Pattern<IntentionArc<IElement>> pattern : patternToResultMap.keySet()) {
				pattern.setElements(set);
				pattern.calculateSimularityToJavaElementSetBasedOnResults(patternToResultMap.get(pattern),
						patternToEnabledElementMap.get(pattern), graph);
			}

			monitor.worked(1);
			this.javaElementSetToPatternSetMap.put(set, patternToResultMap.keySet());
		}
	}

	public void writeXMLFile(IProgressMonitor monitor) throws IOException, CoreException {
		Set<JavaElementSet> keySet = this.javaElementSetToXMLMap.keySet();
		for (JavaElementSet set : keySet)
			writeXMLFile(set, this.javaElementSetToXMLMap.get(set), monitor);
	}

	protected void writeXMLFile(final JavaElementSet set, Element javaElementSetXMLElement, IProgressMonitor monitor)
			throws IOException, CoreException {
		set.writeXML(javaElementSetXMLElement);
		ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StructuralCommonalityAnalyzer [javaElementSetToPatternSetMap=");
		builder.append(javaElementSetToPatternSetMap);
		builder.append(", getMaximumAnalysisDepth()=");
		builder.append(getMaximumAnalysisDepth());
		builder.append("]");
		return builder.toString();
	}
}