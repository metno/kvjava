package kvalobs;

import java.util.EventListener;

import kvalobs.service.ObsDataList;

public interface KvEventListener extends EventListener {
	default void callListener(Object source, ObsDataList dataList) {
	}
}
