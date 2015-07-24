package edu.cuny.citytech.analyzecommonality.core.analysis;

import java.io.IOException;
import java.io.PrintWriter;
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
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import ca.mcgill.cs.swevo.jayfx.ConversionException;
import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.util.TimeCollector;
import edu.cuny.citytech.analyzecommonality.core.analysis.graph.ConcernGraph;
import edu.cuny.citytech.analyzecommonality.core.analysis.graph.GraphElement;
import edu.cuny.citytech.analyzecommonality.core.analysis.graph.IntentionArc;
import edu.cuny.citytech.analyzecommonality.core.analysis.graph.Pattern;
import edu.cuny.citytech.analyzecommonality.core.model.JavaElementSet;

public class StructuralCommonalityAnalyzer extends StructuralCommonalityProcessor {

	private static final String ENABLING_GRAPH_ELEMENTS_FOR_EACH_SET = "Enabling graph elements for each set of java elements.";

	private Map<JavaElementSet, Element> setToXMLMap = new LinkedHashMap<>();

	private Map<JavaElementSet, Set<Pattern<IntentionArc<IElement>>>> setToPatternSetMap = new LinkedHashMap<JavaElementSet, Set<Pattern<IntentionArc<IElement>>>>();

	private static Logger logger = Logger.getLogger(StructuralCommonalityAnalyzer.class.getName());

	public StructuralCommonalityAnalyzer(int maximumAnalysisDepth) {
		super(maximumAnalysisDepth);
	}

	@Override
	protected void analyzeElementSetCollection(final Collection<? extends JavaElementSet> setCol,
			final ConcernGraph graph, final IProgressMonitor monitor, TimeCollector timeCollector)
					throws ConversionException, CoreException, IOException {

		monitor.beginTask(ENABLING_GRAPH_ELEMENTS_FOR_EACH_SET, setCol.size());

		timeCollector.start();
		logger.log(Level.INFO, ENABLING_GRAPH_ELEMENTS_FOR_EACH_SET, setCol.size());
		timeCollector.stop();

		for (final JavaElementSet set : setCol) {

			final Map<Pattern<IntentionArc<IElement>>, Set<GraphElement<IElement>>> patternToResultMap = new LinkedHashMap<Pattern<IntentionArc<IElement>>, Set<GraphElement<IElement>>>();
			final Map<Pattern<IntentionArc<IElement>>, Set<GraphElement<IElement>>> patternToEnabledElementMap = new LinkedHashMap<Pattern<IntentionArc<IElement>>, Set<GraphElement<IElement>>>();

			graph.enableElementsAccordingTo(set, monitor);

			executeQueries(graph.getWorkingMemory(), patternToResultMap, patternToEnabledElementMap, monitor);

			for (final Pattern<IntentionArc<IElement>> pattern : patternToResultMap.keySet()) {
				pattern.setElements(set);
				pattern.calculateSimularityToAdviceBasedOnResults(patternToResultMap.get(pattern),
						patternToEnabledElementMap.get(pattern), graph);
			}

			monitor.worked(1);
			this.setToPatternSetMap.put(set, patternToResultMap.keySet());
		}
	}

	public void writeXMLFile(IProgressMonitor monitor) throws IOException, CoreException {
		Set<JavaElementSet> keySet = this.setToXMLMap.keySet();
		for (JavaElementSet set : keySet)
			writeXMLFile(set, this.setToXMLMap.get(set), monitor);
	}

	protected void writeXMLFile(final JavaElementSet set, Element adviceXMLElement, IProgressMonitor monitor)
			throws IOException, CoreException {
		DocType type = new DocType(this.getClass().getSimpleName());
		Document doc = new Document(adviceXMLElement, type);
		XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
		PrintWriter xmlOut = set.getXMLFileWriter();
		serializer.output(doc, xmlOut);
		xmlOut.close();

		ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);
	}
}