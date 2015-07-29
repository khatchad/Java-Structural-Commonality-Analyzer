package edu.cuny.citytech.analyzecommonality.core.analysis.graph;

import org.jdom2.DataConversionException;
import org.jdom2.Element;

import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.model.Relation;

/**
 * @author raffi
 * 
 */
public class IntentionArc<E extends IElement> extends GraphElement<E> {

	private static final String SOURCE = "source";

	private static final String TARGET = "target";

	private static final long serialVersionUID = -4758844315757084370L;

	private IntentionNode<E> fromNode;

	private IntentionNode<E> toNode;

	private Relation type;

	public IntentionArc(boolean enabled) {
		super(enabled);
	}

	public IntentionArc() {
		this(false);
	}

	public IntentionArc(final IntentionNode<E> from, final IntentionNode<E> to, final Relation type, boolean enabled) {
		super(enabled);
		this.fromNode = from;
		this.toNode = to;
		this.type = type;
	}

	public IntentionArc(final IntentionNode<E> from, final IntentionNode<E> to, final Relation type) {
		this(from, to, type, false);
	}

	public IntentionArc(Element elem) throws DataConversionException {
		super(elem);

		Element typeElem = elem.getChild(Relation.class.getSimpleName());
		this.type = Relation.valueOf(typeElem);

		Element sourceElem = elem.getChild(SOURCE).getChild(IntentionNode.class.getSimpleName());
		this.fromNode = recoverNode(sourceElem);

		Element targetElem = elem.getChild(TARGET).getChild(IntentionNode.class.getSimpleName());
		this.toNode = recoverNode(targetElem);
	}

	private static <E extends IElement> IntentionNode<E> recoverNode(Element sourceElem)
			throws DataConversionException {
		if (WildcardElement.isWildcardElement(sourceElem.getChild(IElement.class.getSimpleName())))
			return (IntentionNode<E>) (GraphElement.isEnabled(sourceElem) ? IntentionNode.ENABLED_WILDCARD
					: IntentionNode.DISABLED_WILDCARD);
		else
			return new IntentionNode<E>(sourceElem);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		return obj instanceof IntentionArc ? this.fromNode.equals(((IntentionArc) obj).fromNode)
				&& this.toNode.equals(((IntentionArc) obj).toNode) && this.type.equals(((IntentionArc) obj).type)
				: false;
	}

	public IntentionNode<E> getFromNode() {
		return this.fromNode;
	}

	public IntentionNode<E> getToNode() {
		return this.toNode;
	}

	public Relation getType() {
		return this.type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (this.fromNode == null || this.toNode == null || this.type == null)
			throw new IllegalStateException("State can not have null attributes");
		return this.fromNode.hashCode() + this.toNode.hashCode() + this.type.hashCode();
	}

	// public void setFromNode(final IntentionNode<E> fromNode) {
	// this.fromNode = fromNode;
	// }
	//
	// /**
	// * @param toNode
	// * the toNode to set
	// */
	// public void setToNode(final IntentionNode<E> toNode) {
	// this.toNode = toNode;
	// }
	// public void setType(final Relation type) {
	// this.type = type;
	// }
	public String toDotFormat() {
		final StringBuilder ret = new StringBuilder();
		ret.append(this.fromNode.hashCode());
		ret.append("->");
		ret.append(this.toNode.hashCode());
		ret.append(' ');
		ret.append("[label=");
		ret.append("\"");
		ret.append(this.type.getFullCode());
		ret.append("\"");
		if (this.isEnabled())
			ret.append(",style=bold,color=red,fontcolor=red");
		ret.append("];");
		return ret.toString();
	}

	@Override
	public String toString() {
		final StringBuilder ret = new StringBuilder();
		ret.append(super.toString());
		// ret.append('(');
		// ret.append(from.getElem().getShortName());
		// ret.append(',');
		// ret.append(to.getElem().getShortName());
		// ret.append(')');
		ret.append(this.type.getFullCode());
		return ret.toString();
	}

	@Override
	public String getLongDescription() {
		StringBuilder ret = new StringBuilder();
		ret.append(super.toString());
		ret.append(this.type.toString() + ": ");
		ret.append(this.getToNode().getLongDescription());
		return ret.toString();
	}

	@Override
	public Element getXML() {
		Element ret = super.getXML();

		Element typeXML = this.type.getXML();
		ret.addContent(typeXML);

		Element source = new Element(SOURCE);
		source.addContent(this.getFromNode().getXML());
		ret.addContent(source);

		Element target = new Element(TARGET);
		target.addContent(this.getToNode().getXML());
		ret.addContent(target);

		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionElement#
	 * getPrettyString()
	 */
	@Override
	public String toPrettyString() {
		StringBuilder ret = new StringBuilder(this.toString());
		ret.append(": ");
		ret.append(this.toNode);
		return ret.toString();
	}
}