package no.met.kvclient;

import java.util.EventListener;

import no.met.kvclient.service.ObsDataList;
import no.met.kvclient.service.SubscribeId;

public interface KvEventListener extends EventListener {
	default void callListener(Object source, SubscribeId id, ObsDataList dataList) {
	}
}
