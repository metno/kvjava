package no.met.kvclient.kv2kvxml;

import java.util.HashMap;
import java.util.Map;

import static javax.xml.stream.XMLStreamConstants.*;


public class XMLHelper {
	private static final Map<Integer, String> EventType;
	
	static {
		EventType = new HashMap<>();
		EventType.put(START_ELEMENT,"START_ELEMENT"); 	
	    EventType.put(ATTRIBUTE,"ATTRIBUTE");
		EventType.put(NAMESPACE,"NAMESPACE");
	    EventType.put(END_ELEMENT,"END_ELEMENT");
	    EventType.put(CHARACTERS,"CHARACTERS");
	    EventType.put(CDATA,"CDATA");
	    EventType.put(COMMENT,"COMMENT");
	    EventType.put(SPACE,"SPACE");
	    EventType.put(START_DOCUMENT,"START_DOCUMENT");
	    EventType.put(END_DOCUMENT,"END_DOCUMENT");
	    EventType.put(PROCESSING_INSTRUCTION, "PROCESSING_INSTRUCTION");
	    EventType.put(ENTITY_REFERENCE,"ENTITY_REFERENCE");
	    EventType.put(DTD, "DTD"); 
	}
	
	static String getStreamEventType(int eventType){
		String r=EventType.get(eventType);
		if(r==null)
			return "UNKNOWN EVENT ("+eventType+")";
		else
			return r;
	}
}
