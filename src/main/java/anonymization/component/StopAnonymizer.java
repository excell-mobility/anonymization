package anonymization.component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.springframework.stereotype.Component;

import anonymization.model.TrackingDate;
import anonymization.model.GeoPoint;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;

@Component
public class StopAnonymizer {
	
	private int blur_factor_start;
	private int blur_factor_end;
	private int blur_factor_left;
	private int blur_factor_right;
	private double disguise_distance;
	private long maxWaitingValue;
	private int minimumDataQuality;
	private DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public StopAnonymizer(int blur_factor_start, int blur_factor_end,
			int blur_factor_left, int blur_factor_right,
			double disguise_distance, long maxWaitingValue,
			int minimumDataQuality) {
		this.blur_factor_start = blur_factor_start;
		this.blur_factor_end = blur_factor_end;
		this.blur_factor_left = blur_factor_left;
		this.blur_factor_right = blur_factor_right;
		this.disguise_distance = disguise_distance;
		this.maxWaitingValue = maxWaitingValue;
		this.minimumDataQuality = minimumDataQuality;
	}
	
	public StopAnonymizer() {
		this.blur_factor_start = 2;
		this.blur_factor_end = 2;
		this.blur_factor_left = 1;
		this.blur_factor_right = 1;
		this.disguise_distance = 75;
		this.maxWaitingValue = 300000;
		this.minimumDataQuality = 6;
	}
	
	public List<TrackingDate> anonymizeTracksJson(JSONObject jsonObject) throws JSONException, ParseException, NoSuchAuthorityCodeException, TransformException, FactoryException {
		
		List<TrackingDate> trackingData = Lists.newLinkedList();
		
		if(!jsonObject.has("points")) {
			throw new JSONException("The json input data is invalid!");
		}
		
		JSONArray jsonArray = jsonObject.getJSONArray("points");
		for(int index = 0; index < jsonArray.length(); index++) {
			JSONObject trackJSON = jsonArray.getJSONObject(index);
			double latitude = trackJSON.has("latitude") ? trackJSON.getDouble("latitude") : 0.0;
			double longitude = trackJSON.has("longitude") ? trackJSON.getDouble("longitude") : 0.0;
			long timestamp = trackJSON.has("timestamp") ? format.parse(trackJSON.getString("timestamp")).getTime() : 0l;
			trackingData.add(new TrackingDate(new GeoPoint(latitude, longitude), timestamp));
		}

		return anonymizeTracks(trackingData);
		
	}
	
	public List<TrackingDate> anonymizeTracks(List<TrackingDate> trackingData) throws NoSuchAuthorityCodeException, TransformException, FactoryException {
		
		System.out.println("Original list size: " + trackingData.size());
		
		double approximate_Distance = calculateDistance(trackingData);
		System.out.println("Distance before: " + approximate_Distance);
		
		List<TrackingDate> sanitizedList = null;
		// check how much points are removed from the track
		// sanitize start and stop first, because of highest privacy risks
		if(minimumDataQuality * disguise_distance < approximate_Distance) {
			sanitizedList = sanitizeStartEndTracks(trackingData, blur_factor_start, 
					blur_factor_end, disguise_distance);
			System.out.println("Sanitized list size after removing start and end points: " + sanitizedList.size());
		}
		
		if(sanitizedList == null) {
			System.out.println("Could not sanitize start and end of the track");
		}
		double calculateDistance = calculateDistance(sanitizedList);
		System.out.println("Distance after sanitization: " + calculateDistance);
		
		// analyze stops and cut data points with a high security risk
		List<Integer> positionsOfStops = calculatePositionsOfStops(sanitizedList, maxWaitingValue);
		System.out.println("The data contains " + positionsOfStops.size() + " stops");
		System.out.println("Positions of the stops: " + positionsOfStops);
		
		System.out.println("The stops are: ");
		for(Integer position: positionsOfStops) {
			System.out.println(sanitizedList.get(position));
		}
		
		// check quality constraint, there should exist enough points in the data set
		List<TrackingDate> sanitizedListWithoutStop = Lists.newLinkedList(sanitizedList);
		int removedNumber = 0;
		for(Integer position: positionsOfStops) {
			if(positionsOfStops.size() > 0 && 
					minimumDataQuality * positionsOfStops.size() * disguise_distance < calculateDistance) {
				sanitizedListWithoutStop = sanitizeStops(sanitizedListWithoutStop, blur_factor_left, 
						blur_factor_right, disguise_distance, position - removedNumber);
				// switching of indices is necessary
				removedNumber = sanitizedList.size() - sanitizedListWithoutStop.size();
			}
		}
		
		if(sanitizedListWithoutStop.size() == sanitizedList.size()) {
			System.out.println("Could not sanitize the stops of the track");
		}
		calculateDistance = calculateDistance(sanitizedListWithoutStop);
		System.out.println("Sanitized list size after removing the stops: " + sanitizedListWithoutStop.size());
		System.out.println("Distance after sanitization of stops: " + calculateDistance);
		
		return sanitizedListWithoutStop;

	}
	
	private static List<TrackingDate> sanitizeStops(
			List<TrackingDate> sanitizedList, int blur_factor_left, int blur_factor_right, double disguise_distance, Integer position)
				throws NoSuchAuthorityCodeException, TransformException, FactoryException {
		
		List<TrackingDate> startList = null;
		List<TrackingDate> endList = null;
		
		if(position - blur_factor_left > 0) {
			startList = sanitizedList.subList(position - blur_factor_left, position + 1);
		} else {
			return null;
		}
		
		if(position + blur_factor_right < sanitizedList.size()) {
			endList = sanitizedList.subList(position, position + blur_factor_right + 1);
		} else {
			return null;
		}
		
		List<TrackingDate> sanitizedListWithoutStop = Lists.newLinkedList();
		
		double startDistance = calculateDistance(startList);
		double endDistance = calculateDistance(endList);
		
		if (startDistance >= disguise_distance && endDistance >= disguise_distance) {
			// sanitization conditions valid, remove the points at the near the position
			List<TrackingDate> saveList = Lists.newLinkedList(sanitizedList);
			List<TrackingDate> subList = saveList.subList(0, position - blur_factor_left + 1);
			sanitizedListWithoutStop.addAll(subList);
			List<TrackingDate> subList2 = saveList.subList(position + blur_factor_right, saveList.size());
			sanitizedListWithoutStop.addAll(subList2);
		} else if(startDistance >= disguise_distance && endDistance < disguise_distance) {
			// only increase right blur factor because quality constraint for the left part is already fulfilled
			return sanitizeStops(sanitizedList, blur_factor_left, blur_factor_right + 1, 
					disguise_distance, position);
		} else if(startDistance < disguise_distance && endDistance >= disguise_distance) {
			// only increase left blur factor because quality constraint for the right part is already fulfilled
			return sanitizeStops(sanitizedList, blur_factor_left + 1, blur_factor_right, 
					disguise_distance, position);
		} else {
			// recursively remove more points from the track to reach the disguise distance
			return sanitizeStops(sanitizedList, blur_factor_left + 1, blur_factor_right + 1, 
					disguise_distance, position);
		}
		
		return sanitizedListWithoutStop;
	}

	private static List<Integer> calculatePositionsOfStops(List<TrackingDate> sanitizedList,
			long maxWaitingValue) {

		List<Integer> numberOfStops = Lists.newLinkedList();
		
		for(int index = 0; index < sanitizedList.size() - 1; index++) {
			if(sanitizedList.get(index).getTimestamp() + maxWaitingValue 
					<= sanitizedList.get(index + 1).getTimestamp()) {
				numberOfStops.add(index);
			}
		}
		
		return numberOfStops;
	}

	private static List<TrackingDate> sanitizeStartEndTracks(List<TrackingDate> trackingDates, int blur_factor_start, 
			int blur_factor_end, double disguise_distance) throws NoSuchAuthorityCodeException, TransformException, FactoryException {

		// check parameter blur factor that makes sense
		if((2 * blur_factor_start + 2 * blur_factor_end) >= trackingDates.size() 
				&& blur_factor_start > 0
				&& blur_factor_end > 0) {
			return null;
		}
		
		List<TrackingDate> startlist = trackingDates.subList(0, blur_factor_start);
		List<TrackingDate> endlist = trackingDates.subList(trackingDates.size() - blur_factor_end, trackingDates.size());
		List<TrackingDate> sanitizedList = Lists.newLinkedList();
		
		double startDistance = calculateDistance(startlist);
		double endDistance = calculateDistance(endlist);
		
		if (startDistance >= disguise_distance && endDistance >= disguise_distance) {
			// sanitization conditions valid, remove the points at the beginning and the end
			sanitizedList = trackingDates.subList(blur_factor_start - 1, trackingDates.size() - blur_factor_end + 1);
		} else if(startDistance >= disguise_distance && endDistance < disguise_distance) {
			// recursively remove more points from the track at the end to reach the disguise distance
			return sanitizeStartEndTracks(trackingDates, blur_factor_start, blur_factor_end + 1, disguise_distance);
		} else if(startDistance < disguise_distance && endDistance >= disguise_distance) {
			// recursively remove more points from the track at the beginning to reach the disguise distance
			return sanitizeStartEndTracks(trackingDates, blur_factor_start + 1, blur_factor_end, disguise_distance);
		} else {
			// recursively remove more points from the track to reach the disguise distance
			return sanitizeStartEndTracks(trackingDates, blur_factor_start + 1, blur_factor_end + 1, disguise_distance);
		}
		
		return sanitizedList;
	}
	
	private static double calculateDistance(List<TrackingDate> trackingDates) throws NoSuchAuthorityCodeException, TransformException, FactoryException {
		
		double distance = 0.0;
		
		if(trackingDates.size() <= 1) {
			return distance;
		}
		
		for(int index = 0; index < trackingDates.size() - 1; index++) {
			
			distance += getDistance(trackingDates.get(index).getPoint().getLatitude(),
					trackingDates.get(index).getPoint().getLongitude(),
					trackingDates.get(index + 1).getPoint().getLatitude(),
					trackingDates.get(index + 1).getPoint().getLongitude());
		}
		
		return distance;
	}
	
	private static double getDistance(double first_lat, double first_lon, 
			double second_lat, double second_lon) throws TransformException, NoSuchAuthorityCodeException, FactoryException {
		
		CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");
	    GeodeticCalculator gc = new GeodeticCalculator(crs);
	    gc.setStartingPosition( JTS.toDirectPosition( new Coordinate(first_lon, first_lat), crs ) );
	    gc.setDestinationPosition( JTS.toDirectPosition( new Coordinate(second_lon, second_lat), crs ) );
	    
	    double distance = gc.getOrthodromicDistance();
	    return distance;
	    
	}

}