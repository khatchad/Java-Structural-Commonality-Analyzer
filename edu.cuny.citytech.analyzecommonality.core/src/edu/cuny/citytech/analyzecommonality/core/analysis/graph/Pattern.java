/**
 * 
 */
package edu.cuny.citytech.analyzecommonality.core.analysis.graph;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVPrinter;
import org.jdom2.DataConversionException;
import org.jdom2.Element;

import ca.mcgill.cs.swevo.jayfx.model.IElement;
import edu.cuny.citytech.analyzecommonality.core.model.JavaElementSet;

/**
 * @author raffi
 * 
 */
public class Pattern<E extends IntentionArc<IElement>> extends Path<E> {

	private static final long serialVersionUID = -8126850132892419370L;

	private double simularity;

	/**
	 * The elements from which this pattern was built.
	 */
	private JavaElementSet set;

	/**
	 * @param pattern
	 * @return
	 */
	private double getConcreteness() {
		final Collection<IntentionNode<IElement>> allNodes = this.getNodes();
		final Collection<IntentionNode<IElement>> wildNodes = this.getWildcardNodes();
		return (double) (allNodes.size() - wildNodes.size()) / allNodes.size();
	}

	private static double calculatePrecision(final Set<GraphElement<IElement>> searchedFor,
			final Set<GraphElement<IElement>> found) {
		final int totalElements = found.size();
		final int lookingFor = searchedFor.size();
		return (double) lookingFor / totalElements;
	}

	private static double performSimularityCalculation(final double precision, final double coverage,
			final double concreteness) {
		return precision * concreteness + coverage * (1 - concreteness);
	}

	public Pattern() {
	}

	/**
	 * @param patternElem
	 * @throws DataConversionException
	 */
	public Pattern(final Element patternElem) throws DataConversionException {
		super(patternElem);
	}

	public JavaElementSet getElements() {
		return this.set;
	}

	/**
	 * @return the simularity
	 */
	public double getSimularity() {
		return this.simularity;
	}

	/**
	 * @param advElem
	 */
	public void setElements(final JavaElementSet set) {
		this.set = set;
	}

	/**
	 * @param simularity
	 */
	protected void setSimularity(final double simularity) {
		this.simularity = simularity;
	}

	/**
	 * Calculates the similarity to the associated advice based on the given
	 * results from matching this pattern.
	 * 
	 * @param patternResults
	 *            Elements matching the pattern.
	 * @param patternEnabledResults
	 *            Elements matching the pattern that are enabled by the
	 *            associate advice.
	 * @param graph
	 *            The concern graph used to produce the results.
	 * @return The pattern simularity; also sets in the pattern.
	 */
	public double calculateSimularityToJavaElementSetBasedOnResults(final Set<GraphElement<IElement>> patternResults,
			final Set<GraphElement<IElement>> patternEnabledResults, ConcernGraph graph) {

		double precision = calculatePrecision(patternEnabledResults, patternResults);

		double coverage = patternEnabledResults.size() / graph.getEnabledElements().size();

		double concreteness = this.getConcreteness();

		double simularity = performSimularityCalculation(precision, coverage, concreteness);

		this.setSimularity(simularity);
		return simularity;
	}

	public static Stream<String> getCSVHeader() {
		Stream<String> elementHeader = JavaElementSet.getCSVHeader();
		Stream<String> patternHeader = Stream.of("Pattern", "Simularity");
		return Stream.concat(elementHeader, patternHeader);
	}
	
	public void dumpCSV(CSVPrinter printer) throws IOException {
		this.set.dumpCSV(printer);
		printer.print(super.toString());
		printer.print(this.getSimularity());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Pattern [getElements()=");
		builder.append(getElements());
		builder.append(", getSimularity()=");
		builder.append(getSimularity());
		builder.append(", pattern=");
		builder.append(super.toString());
		builder.append("]");
		return builder.toString();
	}
}