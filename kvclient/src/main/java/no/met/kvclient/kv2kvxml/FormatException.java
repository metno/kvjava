package no.met.kvclient.kv2kvxml;

import java.util.HashMap;
import java.util.Map;

import static javax.xml.stream.XMLStreamConstants.*;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class FormatException extends Exception {

	private static final long serialVersionUID = -7792461474955805403L;

	public FormatException(String msg) {
		super(msg);
	}

	static FormatException attributeFormat(XMLStreamReader xml, String att) throws XMLStreamException {
		String msg = "Line: " + xml.getLocation().getLineNumber() + " Element '" + xml.getName().getLocalPart()
				+ "'. Expecting attribute '" + att + "'.";
		return new FormatException(msg);
	}

	static FormatException startElementFormat(XMLStreamReader xml, String... names) throws XMLStreamException {
		String nameList = null;
		for (String e : names) {
			if (nameList == null)
				nameList = e;
			else
				nameList += ", " + e;
		}

		String endStart=xml.isStartElement()?" (StartElement)":(xml.isEndElement()?" (EndElemet)":" (UNKNOWN ELEMENT)");
		String msg = "Line: " + xml.getLocation().getLineNumber() + ". Element '" + xml.getName().getLocalPart()
				+ "' " + endStart + ". Expecting start element(s) '" + nameList + "'.";
		return new FormatException(msg);
	}

	static FormatException startEndElementFormat(XMLStreamReader xml, String... names) throws XMLStreamException {
		String nameList = null;
		for (String e : names) {
			if (nameList == null)
				nameList = e;
			else
				nameList += ", " + e;
		}

		String endStart=xml.isStartElement()?" (StartElement)":(xml.isEndElement()?" (EndElemet)":" (UNKNOWN ELEMENT)");
		String msg = "Line: " + xml.getLocation().getLineNumber() + ". Element '" + xml.getName().getLocalPart()
				+ "' " + endStart +". Expecting start/end element(s) '" + nameList + "'.";
		return new FormatException(msg);
	}

	static FormatException endElementFormat(XMLStreamReader xml, String name) throws XMLStreamException {
		String endStart=xml.isStartElement()?" (StartElement)":(xml.isEndElement()?" (EndElemet)":" (UNKNOWN ELEMENT)");
		String msg = "Line: " + xml.getLocation().getLineNumber() + " Element '" + xml.getName().getLocalPart()
				+ "' " + endStart + ". Expecting end element '" + name + "'.";
		return new FormatException(msg);
	}

	static FormatException expectingFormat(XMLStreamReader xml, String expectingMsg, String gotMsg)
			throws XMLStreamException {
		String endStart=xml.isStartElement()?" (StartElement)":(xml.isEndElement()?" (EndElemet)":" (UNKNOWN ELEMENT)");
		String msg = "Line: " + xml.getLocation().getLineNumber() + " Element '" + xml.getName().getLocalPart()
				+ "' " + endStart+". Expecting: " + expectingMsg + ". Got: '" + gotMsg + "'.";
		return new FormatException(msg);
	}
}
