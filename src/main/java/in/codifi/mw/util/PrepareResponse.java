
package in.codifi.mw.util;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.resteasy.reactive.RestResponse;
import org.json.simple.JSONArray;

import in.codifi.mw.model.MWResponseModel;
import in.codifi.mw.model.ResponseModel;
import javax.ws.rs.core.Response.Status;

@ApplicationScoped
public class PrepareResponse {

	/**
	 * Common method for Response
	 *
	 * @param errorMessage
	 * @return
	 */
	public RestResponse<ResponseModel> prepareFailedResponse(String errorMessage) {

		ResponseModel responseObject = new ResponseModel();
		responseObject.setResult(AppConstants.EMPTY_ARRAY);
		responseObject.setStatus(AppConstants.STAT_NOT_OK);
		responseObject.setMessage(errorMessage);
		return RestResponse.ResponseBuilder.create(Status.OK, responseObject).build();
	}

	@SuppressWarnings("unchecked")
	private List<Object> getResult(Object resultData) {
		List<Object> result = new ArrayList<>();
		if (resultData instanceof JSONArray || resultData instanceof List) {
			result = (List<Object>) resultData;
		} else {
			result.add(resultData);
		}
		return result;
	}

	/**
	 * Common method to Success Response
	 *
	 * @param resultData
	 * @return
	 */
	public RestResponse<ResponseModel> prepareSuccessResponseObject(Object resultData) {
		ResponseModel responseObject = new ResponseModel();
		responseObject.setResult(getResult(resultData));
		responseObject.setStatus(AppConstants.STATUS_OK);
		responseObject.setMessage(AppConstants.SUCCESS_STATUS);
		return RestResponse.ResponseBuilder.create(Status.OK, responseObject).build();
	}

	/**
	 * Common method to Success Response
	 *
	 * @param resultData
	 * @return
	 */
	public RestResponse<ResponseModel> prepareSuccessResponseWithMessage(Object resultData, String message) {
		ResponseModel responseObject = new ResponseModel();
		responseObject.setResult(getResult(resultData));
		responseObject.setStatus(message);
		return RestResponse.ResponseBuilder.create(Status.OK, responseObject).build();
	}

	public RestResponse<ResponseModel> prepareResponse(Object resultData) {
		return RestResponse.ResponseBuilder.create(Status.OK, (ResponseModel) resultData).build();
	}

	/**
	 * Common method to Success Response
	 *
	 * @param resultData
	 * @return
	 */
	
	public RestResponse<ResponseModel> prepareSuccessMessage(String message) {
		ResponseModel responseObject = new ResponseModel();
//		responseObject.setResult(AppConstants.EMPTY_ARRAY);
		responseObject.setStatus(AppConstants.STATUS_OK);
		responseObject.setMessage(message);
		return RestResponse.ResponseBuilder.create(Status.OK, responseObject).build();
	}
	
	
	public RestResponse<ResponseModel> prepareMWSuccessResponseObject(Object resultData) {
		ResponseModel responseObject = new ResponseModel();
		responseObject.setStatus(AppConstants.STATUS_OK);
		responseObject.setMessage(AppConstants.SUCCESS_STATUS);
		return RestResponse.ResponseBuilder.create(Status.OK, responseObject).build();
	}
	
	public RestResponse<ResponseModel> prepareMWSuccessResponseString(String resultData) {
		ResponseModel responseObject = new ResponseModel();
		responseObject.setStatus(AppConstants.STATUS_OK);
		responseObject.setMessage(resultData);
		return RestResponse.ResponseBuilder.create(Status.OK, responseObject).build();
	}

	public RestResponse<ResponseModel> prepareFailedResponseObj(String message, String status) {
	    // Create the inner result object
	    ResponseModel result = new ResponseModel();
	    result.setStatus(status);
	    result.setMessage(message);

	    // Create the main response object
	    ResponseModel responseObject = new ResponseModel();
	    responseObject.setStatus(" NOT Ok");
	    responseObject.setMessage("Failed");
	    responseObject.setResult(result);

	    return RestResponse.ResponseBuilder
	        .create(Status.OK, responseObject)
	        .build();
	}
	public RestResponse<ResponseModel> prepareMWSuccessResponseObject2(String message, String status) {
	    // Create the inner result object
	    ResponseModel result = new ResponseModel();
	    result.setStatus(status);
	    result.setMessage(message);

	    // Create the main response object
	    ResponseModel responseObject = new ResponseModel();
	    responseObject.setStatus("Ok");
	    responseObject.setMessage("Success");
	    responseObject.setResult(result);

	    return RestResponse.ResponseBuilder
	        .create(Status.OK, responseObject)
	        .build();
	}
	public RestResponse<ResponseModel> prepareSuccessResponseObject(Object resultData , String status) {
		ResponseModel responseObject = new ResponseModel();
		responseObject.setResult(getResult(resultData));
		responseObject.setStatus("");
		responseObject.setMessage(ErrorMessageConstants.SUCCESS_STATUS);
		responseObject.setStatus(status);
		return RestResponse.ResponseBuilder.create(Status.OK, responseObject).build();
	}

	
	public RestResponse<ResponseModel> prepareMWFailedResponseString(String resultData) {
		ResponseModel responseObject = new ResponseModel();
		responseObject.setStatus(AppConstants.STATUS_OK);
		responseObject.setMessage(resultData);
		return RestResponse.ResponseBuilder.create(Status.OK, responseObject).build();
	}
	
	public RestResponse<ResponseModel> prepareMWFailedResponse(String code,String message) {
		ResponseModel responseObject = new ResponseModel();
		responseObject.setStatus(code);
		responseObject.setMessage(message);
		return RestResponse.ResponseBuilder.create(Status.OK, responseObject).build();
	}
	
	public RestResponse<ResponseModel> prepareSuccessResponseWithMessage(Object resultData, String message,
			boolean withArray) {
		ResponseModel responseObject = new ResponseModel();
		responseObject.setStatus(AppConstants.STATUS_OK);
		responseObject.setMessage(message);
		if (withArray) {
			responseObject.setResult(getResult(resultData));
		} else {
			responseObject.setResult(resultData);
		}
		return RestResponse.ResponseBuilder.create(Status.OK, responseObject).build();
	}

}
