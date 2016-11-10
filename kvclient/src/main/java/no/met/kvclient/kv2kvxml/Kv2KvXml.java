package no.met.kvclient.kv2kvxml;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import javax.xml.stream.XMLStreamException;

import no.met.kvclient.service.ObsDataList;

public class Kv2KvXml {

	static public ObsDataList decodeFromString(String xml) throws FormatException {
		try {
			Reader r = new Reader(xml);
			return r.getObsData();
		} catch (XMLStreamException ex) {
			ex.printStackTrace();
			throw new FormatException("Could not parse xml. " + ex.getMessage());
		}
	}

	static public ObsDataList decodeFromPath(Path file) throws FormatException, IOException {
		try {
			Reader r = new Reader(file);
			return r.getObsData();
		} catch (XMLStreamException ex) {
			ex.printStackTrace();
			throw new FormatException("Could not parse xml. " + ex.getMessage());
		}
	}

	static public ObsDataList decodeFromFile(String file) throws FormatException, IOException, InvalidPathException {
		Path path = FileSystems.getDefault().getPath(file);
		return decodeFromPath(path);
	}
	// static public ObsDataList encode(String xml){
	// return null;
	// }

}
