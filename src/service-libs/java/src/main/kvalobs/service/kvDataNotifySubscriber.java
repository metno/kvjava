package kvalobs.service;

import java.time.Instant;

public interface kvDataNotifySubscriber {
	class What {
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

	class WhatList extends java.util.LinkedList<What> {
		private static final long serialVersionUID = -3828178769235593376L;

		public WhatList(WhatList other) {
			super(other);
		}

		public WhatList() {
		}
	}

	public void callback(WhatList what);
}
