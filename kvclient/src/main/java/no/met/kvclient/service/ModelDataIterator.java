package no.met.kvclient.service;

import java.util.Optional;

public interface ModelDataIterator {
	void destroy();

	Optional<ModelDataList> next();
}
