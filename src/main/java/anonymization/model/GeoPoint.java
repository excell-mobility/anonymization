package anonymization.model;

import java.io.Serializable;

public class GeoPoint implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private double latitude;
	private double longitude;
	
	public GeoPoint(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	@Override
	public String toString() {
		return "GeoPoint{" +
				"latitude=" + latitude +
				", longitude=" + longitude +
				'}';
	}

}