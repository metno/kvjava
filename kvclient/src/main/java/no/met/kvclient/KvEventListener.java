package no.met.kvclient;

import java.util.EventListener;

import no.met.kvclient.service.ObsDataList;

public interface KvEventListener extends EventListener {
	default void callListener(Object source, ObsDataList dataList) {
	}
}
