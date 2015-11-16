package kvalobs.service;

import java.util.Optional;

public interface DataIterator {
	void destroy();

	Optional<ObsDataList> next();
}
