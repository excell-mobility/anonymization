package anonymization.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.text.ParseException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import exceptions.InternalAnonymizationErrorException;
import anonymization.component.StopAnonymizer;
import anonymization.model.TrackingDate;

@CrossOrigin(origins = "*")
@RestController
@Api(value="/v1/anonymization")
public class AnonymizationController {
	
	@Autowired
	StopAnonymizer anonymizer;
	
	@RequestMapping(value = "/v1/anonymization", method = RequestMethod.POST)
    @ApiOperation(
    		value = "Anonymize tracking data", 
    		response=TrackingDate.class, 
    		produces = "application/json")
    @ResponseBody
    public List<TrackingDate> schedulingcare(
    		@ApiParam(name="jsonObjectInput", value="JSON object with tracking data points")
    		@RequestBody String jsonObjectInput) throws InternalAnonymizationErrorException, JSONException, ParseException, NoSuchAuthorityCodeException, TransformException, FactoryException {
    		JSONObject jsonObject = new JSONObject(jsonObjectInput);
    		return anonymizer.anonymizeTracksJson(jsonObject);
    }
	
    @ExceptionHandler(value = InternalAnonymizationErrorException.class)
    public BodyBuilder anonymizationError() {
    	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
