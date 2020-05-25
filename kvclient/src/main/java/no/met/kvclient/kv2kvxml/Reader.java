package no.met.kvclient.kv2kvxml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.EndElement;

import no.met.kvclient.service.ObsData;
import no.met.kvclient.service.ObsDataList;
import no.met.kvutil.DateTimeUtil;

/*
<?xml version="1.0" encoding="UTF-8"?>
<KvalobsData created="2016-12-22 14:32:20">
  <station val="29350">
    <typeid val="502">
      <obstime val="2015-11-17 12:00:00">
        <kvdata paramid="104">
          <original>338.799988</original>
          <controlinfo>0101000000000110</controlinfo>
          <useinfo>7100000400000000</useinfo>
        </kvdata>
        <kvtextdata paramid="1022">
          <original>20151117120000</original>
        </kvtextdata>
      </obstime>
      <obstime val="2015-11-17 13:00:00">
       	<kvdata paramid="104">
          <original>338.799988</original>
          <controlinfo>0101000000000110</controlinfo>
          <useinfo>7100000400000000</useinfo>
        </kvdata>
        <kvtextdata paramid="1022">
          <original>20151117120000</original>
        </kvtextdata>
      </obstime>
    </typeid>
  </station>
</KvalobsData>
*/

/*
<?xml version="1.0" encoding="UTF-8"?>
<KvalobsData>
  <station val="46610">
    <typeid val="330">
      <obstime val="2016-09-20 13:00:00">
        <tbtime val="2016-09-20 10:57:10.746935">
          <kvdata paramid="1">
            <original>8</original>
            <controlinfo>0110000000000010</controlinfo>
            <useinfo>7100000300000000</useinfo>
          </kvdata>
          <kvdata paramid="104">
            <original>329.049988</original>
            <controlinfo>0100000000000110</controlinfo>
            <useinfo>7100000300000000</useinfo>
          </kvdata>
          <kvdata paramid="173">
            <original>1020.90002</original>
            <controlinfo>0100000000000000</controlinfo>
            <useinfo>7100000300000000</useinfo>
          </kvdata>
          <kvdata paramid="177">
            <original>0.360000014</original>
            <controlinfo>0110000000000010</controlinfo>
            <useinfo>7100000300000000</useinfo>
          </kvdata>
          <kvdata paramid="211">
            <original>99999</original>
            <corrected>19.6000004</corrected>
            <controlinfo>0610604000000000</controlinfo>
            <useinfo>7133700300000002</useinfo>
            <cfailed>QC1-1-211,QC1-4-211</cfailed>
          </kvdata>
          <kvdata paramid="213">
            <original>99999</original>
            <corrected>-32766</corrected>
            <controlinfo>0610102000000000</controlinfo>
            <useinfo>7138100300000001</useinfo>
            <cfailed>QC1-1-213</cfailed>
          </kvdata>
          <kvdata paramid="215">
            <original>99999</original>
            <corrected>-32766</corrected>
            <controlinfo>0610102000000000</controlinfo>
            <useinfo>7138100300000001</useinfo>
            <cfailed>QC1-1-215</cfailed>
          </kvdata>
          <kvdata paramid="262">
            <original>69.9179993</original>
            <controlinfo>0100200000000010</controlinfo>
            <useinfo>7100000300000001</useinfo>
            <cfailed>QC1-4-262</cfailed>
          </kvdata>
          <kvtextdata paramid="1022">
            <original>20160920130000</original>
          </kvtextdata>
        </tbtime>
        <tbtime val="2016-09-20 10:57:11.615849">
          <kvdata paramid="178">
            <original>-32767</original>
            <corrected>1020.59998</corrected>
            <controlinfo>0000601000000000</controlinfo>
            <useinfo>7894700000000001</useinfo>
            <cfailed>QC1-4-178</cfailed>
          </kvdata>
        </tbtime>
      </obstime>
    </typeid>
  </station>
</KvalobsData>
*/

class Reader {
	Instant startTime = Instant.now();
	long currentStationId;
	long currentTypeId;
	int currentSensor;
	int currentLevel;
	Instant currentObstime;
	Instant currentTbtime;
	Instant created;
	ObsData currentObsData;
	static XMLInputFactory factory = XMLInputFactory.newFactory();
	XMLStreamReader xml;
	ObsDataList obsData;

	Reader(String xmlsrc) throws XMLStreamException {
		xml = factory.createXMLStreamReader(new StringReader(xmlsrc));
		obsData = new ObsDataList();
	}

	Reader(Path file) throws XMLStreamException, IOException, SecurityException {
		BufferedReader reader = Files.newBufferedReader(file);
		xml = factory.createXMLStreamReader(reader);
		obsData = new ObsDataList();
	}

	Instant getTbTime() {
		return currentTbtime != null ? currentTbtime : startTime;
	}

	String getElementName() throws XMLStreamException, FormatException {
		if (!xml.isStartElement() && !xml.isEndElement())
			throw FormatException.expectingFormat(xml, "Current element must be a start or end element",
					XMLHelper.getStreamEventType(xml.getEventType()));
		return xml.getName().getLocalPart().toLowerCase();
	}

	String getElement(String first, String... validNames) throws XMLStreamException, FormatException {
		if (first == null) {
			xml.nextTag();
			return xml.getName().getLocalPart().toLowerCase();
		}

		xml.nextTag();
		String name = xml.getName().getLocalPart().toLowerCase();

		if (name.compareToIgnoreCase(first) == 0)
			return name;

		for (String elementName : validNames) {
			if (name.compareToIgnoreCase(elementName) == 0)
				return name;
		}
		throw FormatException.startEndElementFormat(xml, validNames);
	}

	String getStartElement(String first, String... validNames) throws XMLStreamException, FormatException {
		try {
			String element = getElement(first, validNames);
			if (!xml.isStartElement())
				throw FormatException.startElementFormat(xml, validNames);
			return element;
		} catch (FormatException ex) {
			throw FormatException.startElementFormat(xml, validNames);
		}
	}

	// boolean isStartElement(String name) throws XMLStreamException {
	// if (xml.isStartElement() && (name == null || isElement(name)))
	// return true;
	// else
	// return false;
	// }

	boolean isStartElement(String name, String... names) throws XMLStreamException {
		if (!xml.isStartElement())
			return false;

		if (name == null || isElement(name))
			return true;

		for (String n : names)
			if (isElement(n))
				return true;

		return false;
	}

	boolean isEndElement(String name) throws XMLStreamException {
		if (xml.isEndElement() && (name == null || isElement(name)))
			return true;
		else
			return false;
	}

	boolean isElement(String name) throws XMLStreamException {
		String elementName = xml.getName().getLocalPart();
		return name.compareToIgnoreCase(elementName) == 0;
	}

	String getElementValue() throws XMLStreamException, FormatException {
		String name = getElementName();
		String val = xml.getElementText();

		if (isEndElement(name))
			return val.trim();
		else
			throw FormatException.endElementFormat(xml, name);
	}

	String getAttribute(String att) throws XMLStreamException, FormatException {
		String val = xml.getAttributeValue(null, att);
		if (val == null)
			throw FormatException.attributeFormat(xml, att);
		return val.trim();
	}

	long getAttributeAsLong(String name) throws XMLStreamException, FormatException {
		String val = getAttribute(name);
		try {
			return Long.parseLong(val);
		} catch (NumberFormatException ex) {
			throw FormatException.expectingFormat(xml, "number", val);
		}
	}

	int getAttributeAsInt(String name) throws XMLStreamException, FormatException {
		String val = getAttribute(name);
		try {
			return Integer.parseInt(val);
		} catch (NumberFormatException ex) {
			throw FormatException.expectingFormat(xml, "number", val);
		}
	}

	Instant getAttributeAsInstant(String name) throws XMLStreamException, FormatException {
		String val = getAttribute(name);
		try {
			return DateTimeUtil.parseToInstant(val);
		} catch (DateTimeParseException ex) {
			throw FormatException.expectingFormat(xml, "isotime", val + ". " + ex.getMessage());
		}
	}

	Instant parseInstant(String isotime) throws XMLStreamException, FormatException {
		try {
			return DateTimeUtil.parse(isotime).toInstant();
		} catch (DateTimeParseException ex) {
			throw FormatException.expectingFormat(xml, "isotime", isotime + ". " + ex.getMessage());
		}
	}

	void decodeKvData() throws XMLStreamException, FormatException {
		// System.err.println("decodeKvData: start");
		int paramid = getAttributeAsInt("paramid");
		String element = getElement("original", "corrected", "controlinfo", "useinfo", "cfailed", "kvdata");
		double original = Double.MAX_VALUE;
		double corrected = Double.MAX_VALUE;
		String controlinfo = null;
		String useinfo = null;
		String cfailed = null;
		Instant tbtime = null;

		while (isStartElement(null)) {
			switch (element) {
			case "original":
				original = Double.parseDouble(getElementValue());
				break;
			case "corrected":
				corrected = Double.parseDouble(getElementValue());
				break;
			case "controlinfo":
				controlinfo = getElementValue();
				break;
			case "useinfo":
				useinfo = getElementValue();
				break;
			case "cfailed":
				cfailed = getElementValue();
				break;
			default:
			}
			element = getElement("tbtime", "original", "corrected", "controlinfo", "useinfo", "cfailed", "kvdata");
		}

		// System.err.println("decodeKvData: "+getElementName() + "
		// "+(isEndElement(null)?"endElement":"startElement"));
		if (isEndElement("kvdata")) {
			if (corrected == Double.MAX_VALUE)
				corrected = original;

			if (original != Double.MAX_VALUE)
				currentObsData.addData(paramid, currentSensor, currentLevel, getTbTime(), original, corrected,
						controlinfo, useinfo, cfailed);
		}
		getElement(null);
		// System.err.println("decodeKvData: start, element:
		// "+getElementName()+"
		// "+(isEndElement(null)?"endElement":"startElement"));
	}

	void decodeKvTextData() throws XMLStreamException, FormatException {
		// System.err.println("decodeKvTextData: start");
		int paramid = getAttributeAsInt("paramid");
		String element = getElement("original", "tbtime", "kvtextdata");
		String original = null;
		Instant tbtime = null;

		while (isStartElement(null)) {
			switch (element) {
			case "original":
				original = getElementValue();
				break;
			case "tbtime":
				tbtime = parseInstant(getElementValue());
				break;
			default:
			}
			element = getElement("tbtime", "original", "kvtextdata");
		}

		// System.err.println("decodeKvTextData: "+getElementName() + "
		// "+(isEndElement(null)?"endElement":"startElement"));
		if (isEndElement("kvtextdata")) {
			if (tbtime == null)
				tbtime = startTime;
			if (original != null)
				currentObsData.addTextData(paramid, original, getTbTime());
		}
		getElement(null);
		// System.err.println("decodeKvTextData: start, element:
		// "+getElementName()+"
		// "+(isEndElement(null)?"endElement":"startElement"));
	}

	void decodeSensor() throws XMLStreamException, FormatException {
		currentSensor = getAttributeAsInt("val");
		String element = getElement("sensor", "level", "kvdata");

		while (isStartElement(null)) {
			switch (element) {
			case "level":
				decodeLevel();
				break;
			case "kvdata":
				decodeKvData();
				break;
			default:
			}
		}
		// reset the currentSensor to the default.
		currentSensor = 0;
		getElement(null);
	}

	void decodeLevel() throws XMLStreamException, FormatException {
		currentLevel = getAttributeAsInt("val");
		String element = getElement("level", "kvdata");

		while (isStartElement(null)) {
			switch (element) {
			case "kvdata":
				decodeKvData();
				break;
			default:
			}
		}
		// reset the currentLevel to the default.
		currentLevel = 0;
		getElement(null);
	}

	void decodeData(String tag) throws XMLStreamException, FormatException {
		String element;
		if (tag == null)
			element = getElement("kvtextdata", "kvdata", "sensor", "level", "obstime", "tbtime");
		else
			element = tag;

		if (isEndElement(null))
			return;

		while (isStartElement(null)) {
			element = getElementName();
			switch (element) {
			case "kvdata":
				decodeKvData();
				break;
			case "kvtextdata":
				decodeKvTextData();
				break;
			case "sensor":
				decodeSensor();
				break;
			case "level":
				decodeLevel();
				break;
			default:
				return;
			}
			// System.out.println("decodeData: "+getElementName()+" endElement:
			// "+(isEndElement(null)?"true":"false"));
		}
	}

	void decodeTbTime(boolean hasStartTagTbtime) throws XMLStreamException, FormatException {
		if (currentStationId == Long.MAX_VALUE || currentTypeId == Long.MAX_VALUE || currentObstime == null) {
			String sid = currentStationId == Long.MAX_VALUE ? "null" : Long.toString(currentStationId);
			String tid = currentTypeId == Long.MAX_VALUE ? "null" : Long.toString(currentTypeId);
			String obstime = currentObstime == null ? "null" : currentObstime.toString();
			throw new FormatException("Internal:  stationid, typeid  and/or obstime is not set, stationid(" + sid
					+ "), typeid(" + tid + "), obstime(" + obstime + ").");
		}

		if (!hasStartTagTbtime)
			getElement("obstime", "tbtime");

		if (isStartElement("tbtime")) {
			while( isStartElement("tbtime")) {
				currentTbtime = getAttributeAsInstant("val");
				decodeData(null);
			
				if( isEndElement("tbtime"))
					getElement(null);
				else
					throw FormatException.endElementFormat(xml, "tbtime");
			}
		}
	}

	void decodeObstime() throws XMLStreamException, FormatException {

		getElement("obstime", "typeid");

		while (isStartElement("obstime") ) {
			// System.out.println("obstime: Start");
			currentObstime = getAttributeAsInstant("val");
			currentTbtime = null;
			currentObsData = new ObsData(currentStationId, currentTypeId, currentObstime);
			getElement(null);
			if( isStartElement("tbtime") ) 
				decodeTbTime(true);
			else
				decodeData(getElementName());
			
			if (isEndElement("obstime")) {
				if ( !currentObsData.isEmpty()) 
					obsData.add(currentObsData);
			} else {
				throw FormatException.endElementFormat(xml, "obstime");
			}
			getElement("obstime", "typeid");
		}
		getElement(null);
	}

	void decodeTypeid() throws XMLStreamException, FormatException {
		getElement("typeid", "station");
		while (isStartElement("typeid")) {
			// System.out.println("typeid: Start");
			currentTypeId = getAttributeAsLong("val");
			currentObstime = null;
			currentTbtime = null;
			currentObsData = null;
			currentSensor = 0;
			currentLevel = 0;
			decodeObstime();
			// System.out.println("decodeTypeid: "+getElementName()+"
			// endElement: "+(isEndElement(null)?"true":"false"));
		}
		currentTypeId = Long.MAX_VALUE;
		getElement(null);
		// System.out.println("@@@@@decodeTypeid: "+getElementName()+"
		// endElement: "+(isEndElement(null)?"true":"false"));
	}

	void decodeStation() throws XMLStreamException, FormatException {
		getElement("station", "kvalobsdata");
		while (isStartElement("station")) {
			// System.out.println("station: Start");
			currentStationId = getAttributeAsLong("val");
			currentTypeId = Long.MAX_VALUE;
			currentObstime = null;
			currentTbtime = null;
			currentObsData = null;
			currentSensor = 0;
			currentLevel = 0;
			decodeTypeid();
		}
	}

	ObsDataList getObsData() throws XMLStreamException, FormatException {
		currentStationId = Long.MAX_VALUE;
		currentTypeId = Long.MAX_VALUE;
		currentObstime = null;
		currentTbtime = null;
		created=null;
		currentObsData = null;
		currentSensor = 0;
		currentLevel = 0;

		try {
			getStartElement("kvalobsdata");
			try {
				obsData.setCreated(getAttributeAsInstant("created"));
			} catch ( Exception ex) {
				obsData.setCreated(null);
			}

			// System.out.println("KvalobsData: Start");
			decodeStation();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new FormatException("Not in 'KvalobData' xml-format. " + ex.getMessage());
		}

		return obsData;
	}
}
