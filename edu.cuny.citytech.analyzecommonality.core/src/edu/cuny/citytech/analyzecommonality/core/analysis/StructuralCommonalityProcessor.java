package edu.cuny.citytech.analyzecommonality.core.analysis;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;

import ca.mcgill.cs.swevo.jayfx.ConversionException;
import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.model.Relation;
import ca.mcgill.cs.swevo.jayfx.util.TimeCollector;
import edu.cuny.citytech.analyzecommonality.core.analysis.graph.ConcernGraph;
import edu.cuny.citytech.analyzecommonality.core.analysis.graph.GraphElement;
import edu.cuny.citytech.analyzecommonality.core.analysis.graph.IntentionArc;
import edu.cuny.citytech.analyzecommonality.core.analysis.graph.IntentionNode;
import edu.cuny.citytech.analyzecommonality.core.analysis.graph.Path;
import edu.cuny.citytech.analyzecommonality.core.analysis.graph.Pattern;
import edu.cuny.citytech.analyzecommonality.core.model.JavaElementSet;
import edu.cuny.citytech.analyzecommonality.core.util.Util;

public abstract class StructuralCommonalityProcessor {

	private static Logger logger = Logger.getLogger(StructuralCommonalityProcessor.class.getName());

	private static final String SIMULARITY = "simularity";

	public static final int DEFAULT_MAXIMUM_ANALYSIS_DEPTH = 2;

	/**
	 * @param relation
	 * @param string
	 * @param session
	 * @param patternToResultMap
	 * @param patternToEnabledElementMap
	 * @param lMonitor
	 */
	@SuppressWarnings("unchecked")
	private static void executeArcQuery(final String queryString, final Relation relation, final KieSession session,
			final Map<Pattern<IntentionArc<IElement>>, Set<GraphElement<IElement>>> patternToResultMap,
			final Map<Pattern<IntentionArc<IElement>>, Set<GraphElement<IElement>>> patternToEnabledElementMap,
			final IProgressMonitor lMonitor) {

		final QueryResults suggestedArcs = session.getQueryResults(queryString, new Object[] { relation });

		lMonitor.beginTask("Executing query: " + queryString.replace("X", relation.toString()) + ".",
				suggestedArcs.size());
		for (final Iterator<QueryResultsRow> it = suggestedArcs.iterator(); it.hasNext();) {
			final QueryResultsRow result = it.next();
			final IntentionArc suggestedArc = (IntentionArc) result.get("$suggestedArc");

			final IntentionArc enabledArc = (IntentionArc) result.get("$enabledArc");

			final Path enabledPath = (Path) result.get("$enabledPath");

			final IntentionNode commonNode = (IntentionNode) result.get("$commonNode");
			final Pattern pattern = enabledPath.extractPattern(commonNode, enabledArc);

			if (!patternToResultMap.containsKey(pattern))
				patternToResultMap.put(pattern, new LinkedHashSet<GraphElement<IElement>>());
			patternToResultMap.get(pattern).add(suggestedArc);

			if (!patternToEnabledElementMap.containsKey(pattern))
				patternToEnabledElementMap.put(pattern, new LinkedHashSet<GraphElement<IElement>>());
			patternToEnabledElementMap.get(pattern).add(enabledArc);

			lMonitor.worked(1);
		}
		lMonitor.done();
	}

	/**
	 * @param lMonitor
	 * @param session
	 * @param patternToResultMap
	 * @param patternToEnabledElementMap
	 */
	@SuppressWarnings("unchecked")
	private static void executeNodeQuery(final IProgressMonitor lMonitor, final KieSession session,
			final Map<Pattern<IntentionArc<IElement>>, Set<GraphElement<IElement>>> patternToResultMap,
			final Map<Pattern<IntentionArc<IElement>>, Set<GraphElement<IElement>>> patternToEnabledElementMap,
			final String queryString) {

		System.out.println("Session " + session);
		final QueryResults suggestedNodes = session.getQueryResults(queryString);
		System.out.println("Executing node query: " + queryString + "." + suggestedNodes.size());
		System.out.println("Results: " + suggestedNodes);
		System.out.println("Result size: " + suggestedNodes.size());
		for (final Iterator<QueryResultsRow> it = suggestedNodes.iterator(); it.hasNext();) {
			final QueryResultsRow result = it.next();
            IntentionNode<IElement> enabledNode = (IntentionNode<IElement>) result.get("$enabledNode");
            System.out.println(enabledNode);
            System.out.println("Enabled? " + enabledNode.isEnabled());
//			final IntentionNode suggestedNode = (IntentionNode) result.get("$suggestedNode");
//
//			final IntentionNode enabledNode = (IntentionNode) result.get("$enabledNode");
//
//			final Path enabledPath = (Path) result.get("$enabledPath");
//
//			final IntentionNode commonNode = (IntentionNode) result.get("$commonNode");
//			final Pattern pattern = enabledPath.extractPattern(commonNode, enabledNode);
//
//			if (!patternToResultMap.containsKey(pattern))
//				patternToResultMap.put(pattern, new LinkedHashSet<GraphElement<IElement>>());
//			patternToResultMap.get(pattern).add(suggestedNode);
//
//			if (!patternToEnabledElementMap.containsKey(pattern))
//				patternToEnabledElementMap.put(pattern, new LinkedHashSet<GraphElement<IElement>>());
//			patternToEnabledElementMap.get(pattern).add(enabledNode);

			lMonitor.worked(1);
		}
		lMonitor.done();
	}

	/**
	 * @param adviceXMLElement
	 * @param pattern
	 * @param simularity
	 */
	private static Element getPatternXMLElement(final Pattern pattern, double simularity) {
		Element patternXMLElement = pattern.getXML();
		patternXMLElement.setAttribute(SIMULARITY, String.valueOf(simularity));
		return patternXMLElement;
	}

	private int maximumAnalysisDepth = DEFAULT_MAXIMUM_ANALYSIS_DEPTH;

	public StructuralCommonalityProcessor() {
	}

	/**
	 * @param maximumAnalysisDepth
	 */
	public StructuralCommonalityProcessor(int maximumAnalysisDepth) {
		this.maximumAnalysisDepth = maximumAnalysisDepth;
	}

	public void analyze(JavaElementSet set, final IProgressMonitor monitor, TimeCollector timeCollector)
			throws Exception {
		analyze(Collections.singleton(set), monitor, timeCollector);
	}

	public void analyze(final Collection<? extends JavaElementSet> setCol, final IProgressMonitor monitor,
			TimeCollector timeCollector) throws Exception {

		Collection<IJavaElement> flattened = setCol.stream().flatMap(s -> s.getSet().stream())
				.collect(Collectors.toList());
		final Collection<IProject> projectsToAnalyze = Util.getProjects(flattened);

		if (timeCollector != null)
			timeCollector.start();

		logger.log(Level.INFO, "Building graph from projects.", projectsToAnalyze);

		if (timeCollector != null)
			timeCollector.stop();

		final ConcernGraph graph = createConcernGraph(projectsToAnalyze, monitor, timeCollector);

		if (timeCollector != null)
			timeCollector.start();

		logger.info("Analyzing.");

		if (timeCollector != null)
			timeCollector.stop();

		analyzeElementSetCollection(setCol, graph, monitor, timeCollector);
	}

	protected abstract void analyzeElementSetCollection(final Collection<? extends JavaElementSet> setCol,
			final ConcernGraph graph, final IProgressMonitor monitor, TimeCollector timeCollector)
					throws ConversionException, CoreException, IOException, JDOMException;

	protected ConcernGraph createConcernGraph(final Collection<IProject> projectsToAnalyze,
			final IProgressMonitor monitor, TimeCollector timeCollector) throws Exception {
		final ConcernGraph graph = new ConcernGraph(projectsToAnalyze, this.maximumAnalysisDepth, monitor,
				timeCollector);
		return graph;
	}

	/**
	 * @param session
	 * @param patternToResultMap
	 * @param patternToEnabledElementMap
	 * @param monitor
	 */
	protected void executeQueries(final KieSession session,
			final Map<Pattern<IntentionArc<IElement>>, Set<GraphElement<IElement>>> patternToResultMap,
			final Map<Pattern<IntentionArc<IElement>>, Set<GraphElement<IElement>>> patternToEnabledElementMap,
			final IProgressMonitor monitor) {

		executeNodeQuery(new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK), session,
				patternToResultMap, patternToEnabledElementMap, "forward suggested execution nodes");

//		executeNodeQuery(new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK), session,
//				patternToResultMap, patternToEnabledElementMap, "backward suggested execution nodes");
//
//		executeArcQuery("forward suggested X arcs", Relation.CALLS, session, patternToResultMap,
//				patternToEnabledElementMap,
//				new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
//
//		executeArcQuery("backward suggested X arcs", Relation.CALLS, session, patternToResultMap,
//				patternToEnabledElementMap,
//				new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
//
//		executeArcQuery("forward suggested X arcs", Relation.GETS, session, patternToResultMap,
//				patternToEnabledElementMap,
//				new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
//
//		executeArcQuery("backward suggested X arcs", Relation.GETS, session, patternToResultMap,
//				patternToEnabledElementMap,
//				new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
//
//		executeArcQuery("forward suggested X arcs", Relation.SETS, session, patternToResultMap,
//				patternToEnabledElementMap,
//				new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
//
//		executeArcQuery("backward suggested X arcs", Relation.SETS, session, patternToResultMap,
//				patternToEnabledElementMap,
//				new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
	}

	public int getMaximumAnalysisDepth() {
		return this.maximumAnalysisDepth;
	}

	/**
	 * @param patternToEnabledElementMap
	 * @param pattern
	 * @return
	 */
	private Element getXML(final Set<GraphElement<IElement>> set, String elementName) {
		Element ret = new Element(elementName);
		for (GraphElement<IElement> enabledElement : set) {
			if (enabledElement instanceof IntentionArc)
				ret.addContent(((IntentionArc) enabledElement).getXML());
			else
				ret.addContent(enabledElement.getXML());
		}
		return ret;
	}

	public void setMaximumAnalysisDepth(short maximumAnalysisDepth) {
		this.maximumAnalysisDepth = maximumAnalysisDepth;
	}
}
