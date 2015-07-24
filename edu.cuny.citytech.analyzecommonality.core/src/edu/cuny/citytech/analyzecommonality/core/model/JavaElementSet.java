package edu.cuny.citytech.analyzecommonality.core.model;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.IJavaElement;
import org.jdom2.Element;

import edu.cuny.citytech.analyzecommonality.core.analysis.util.FileUtil;
import edu.cuny.citytech.analyzecommonality.core.analysis.util.XMLUtil;

public class JavaElementSet {
	private Set<? extends IJavaElement> set;
	private String name;
	private String type;

	public JavaElementSet(Set<? extends IJavaElement> set, String name, String type) {
		this.set = set;
		this.name = name;
		this.type = type;
	}

	public Set<? extends IJavaElement> getSet() {
		return set;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getIdentifier() {
		return set.stream().map(e -> e.getHandleIdentifier()).collect(Collectors.joining(","));
	}

	private Element getSetXML() {
		Element ret = new Element("javaElements");
		
		for (IJavaElement jElem : getSet()) {
			Element xmlElem = XMLUtil.getXML(jElem);
			ret.addContent(xmlElem);
		}
		return ret;
		
	}

	public Element getXML() {
		Element ret = XMLUtil.getXML(getSet(), getIdentifier(), getName(), getType());
		ret.addContent(getSetXML());
		return ret;
	}

	public PrintWriter getXMLFileWriter() throws IOException {
		String fileName = getRelativeXMLFileName();
		final File aFile = new File(FileUtil.WORKSPACE_LOC, fileName);
		PrintWriter ret = FileUtil.getPrintWriter(aFile, false);
		return ret;
	}

	/**
	 * @param set
	 * @return
	 */
	public String getRelativeXMLFileName() {
		return getIdentifier().concat(".structcom.xml");
	}

	public File getSavedXMLFile() {
		String relativeFileName = getRelativeXMLFileName();
		File aFile = new File(FileUtil.WORKSPACE_LOC, relativeFileName);
		if (!aFile.exists())
			throw new IllegalArgumentException("No XML file found for " + this);
		return aFile;
	}
}