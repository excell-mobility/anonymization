package anonymization.model;

public class TrackingDate {
	
	private GeoPoint point;
	private long timestamp;
	
	
	public TrackingDate(GeoPoint point, long timestamp) {
		this.point = point;
		this.timestamp = timestamp;
	}

	public GeoPoint getPoint() {
		return point;
	}

	public void setPoint(GeoPoint point) {
		this.point = point;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "TrackingDate [point=" + point + ", timestamp=" + timestamp
				+ "]";
	}

}