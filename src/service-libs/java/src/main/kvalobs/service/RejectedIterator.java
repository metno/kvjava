package kvalobs.service;

import java.util.Optional;

public interface RejectedIterator {
	public void destroy();

	public Optional<RejectdecodeList> next();
}
