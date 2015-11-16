package no.met.kvclient.service;

import java.util.Optional;

public interface DataIterator {
	void destroy();

	Optional<ObsDataList> next();
}
