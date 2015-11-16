/*
  Kvalobs - Free Quality Control Software for Meteorological Observations 

  $Id: KvDataNotifyEventListener.java,v 1.1.2.2 2007/09/27 09:02:41 paule Exp $                                                       

  Copyright (C) 2007 met.no

  Contact information:
  Norwegian Meteorological Institute
  Box 43 Blindern
  0313 OSLO
  NORWAY
  email: kvalobs-dev@met.no

  This file is part of KVALOBS

  KVALOBS is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as 
  published by the Free Software Foundation; either version 2 
  of the License, or (at your option) any later version.
  
  KVALOBS is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
  
  You should have received a copy of the GNU General Public License along 
  with KVALOBS; if not, write to the Free Software Foundation Inc., 
  51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/
package kvalobs;

import kvalobs.service.DataElem;
import kvalobs.service.ObsData;
import kvalobs.service.kvDataNotifySubscriber.What;
import kvalobs.service.kvDataNotifySubscriber.WhatList;

public interface KvDataNotifyEventListener extends KvEventListener {
	@Override
	default public void callListener(Object source, kvalobs.service.ObsDataList dataList) {
		WhatList wl = new WhatList();
		for (ObsData od : dataList) {
			boolean onlyMissing = true;
			for (DataElem de : od.dataList) {
				int useinfo3 = de.useinfo.length() > 3 ? Integer.parseInt(de.useinfo.substring(3, 4)) : -1;
				if (useinfo3 != 9) {
					onlyMissing = false;
					break;
				}
			}

			if (!od.dataList.isEmpty()) {
				wl.add(new What(od.stationid, od.typeID, od.obstime, onlyMissing));
				break;
			}
			if (!od.textDataList.isEmpty())
				wl.add(new What(od.stationid, od.typeID, od.obstime, false));
		}
		if (!wl.isEmpty()) {
			kvDataNotifyEvent(new KvDataNotifyEvent(source, wl));
		}
	};

	public void kvDataNotifyEvent(KvDataNotifyEvent event);
}
