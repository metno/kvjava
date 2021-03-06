/*
  Kvalobs - Free Quality Control Software for Meteorological Observations 

  $Id: KvDataNotifyEvent.java,v 1.1.2.3 2007/09/27 09:02:41 paule Exp $                                                       

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
package no.met.kvclient;

//import no.met.kvclient.service.kvDataNotifySubscriber.WhatList;

import java.time.Instant;

//import no.met.kvclient.KvDataNotifyEventListener.What;
import no.met.kvclient.service.SubscribeId;

public class KvDataNotifyEvent extends KvEvent {
	private static final long serialVersionUID = 1771127947051664937L;

	static public class What {
		public long stationID;
		public long typeID_;
		public Instant obsTime;
		public boolean missing;

		public What(long stationID, long typeID_, Instant obsTime, boolean missing) {
			this.stationID = stationID;
			this.typeID_ = typeID_;
			this.obsTime = obsTime;
			this.missing = missing;
		}

		public What(What other) {
			this.stationID = other.stationID;
			this.typeID_ = other.typeID_;
			this.obsTime = other.obsTime;
			this.missing = other.missing;
		}
	}

	static public class WhatList extends java.util.LinkedList<What> {
		private static final long serialVersionUID = -3828178769235593376L;

		public WhatList(WhatList other) {
			super(other);
		}

		public WhatList() {
		}
	}

	
	private WhatList what;

	public KvDataNotifyEvent(Object source, SubscribeId id, WhatList what) {
		super(source, id, "KvDataNotifyEvent");
		this.what = what;
	}

	public WhatList getWhatList() {
		return what;
	}
}
