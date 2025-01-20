/**
 * 
 */
package in.codifi.mw.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.Path;

import org.jboss.resteasy.reactive.RestResponse;

import in.codifi.mw.controller.spec.ScripsControllerSpecs;
import in.codifi.mw.model.ClinetInfoModel;
import in.codifi.mw.model.ResponseModel;
import in.codifi.mw.model.SearchScripReqModel;
import in.codifi.mw.repository.BrokerPreference;
import in.codifi.mw.service.spec.ScripsServiceSpecs;
import in.codifi.mw.util.AppConstants;
import in.codifi.mw.util.AppUtil;
import in.codifi.mw.util.CommonUtils;
import in.codifi.mw.util.ErrorCodeConstants;
import in.codifi.mw.util.ErrorMessageConstants;
import in.codifi.mw.util.PrepareResponse;
import in.codifi.mw.util.StringUtil;
import io.quarkus.logging.Log;

/**
 * @author Vicky
 *
 */
@Path("/scrip")
public class ScripsController implements ScripsControllerSpecs {

	@Inject
	ScripsServiceSpecs scripsService;

	@Inject
	PrepareResponse prepareResponse;
	@Inject
	CommonUtils commonUtils;
	@Inject
	AppUtil appUtil;
	@Inject
	BrokerPreference brokerprerepo;

//	@Override
//	public RestResponse<ResponseModel> getScrips(SearchScripReqModel reqModel) {
//		
//		/** to get userid using clientinfo **/
//		
//		ClinetInfoModel info = appUtil.getClientInfo();
//		if (info == null || StringUtil.isNullOrEmpty(info.getUserId())) {
//			Log.error("Client info is null");
//			return prepareResponse.prepareFailedResponse(AppConstants.FAILED_STATUS);
//		} 
//		
//		/** find the userid **/
//		
//		String userId = info.getUserId().toLowerCase();
//		String broker = userId.split("_")[0];
//		System.out.println("userid"+broker );
//		
//		/** check the repository for allowed exchanges **/
//		
//	    List<String> allowedExchanges = brokerprerepo.findExchangesByBroker(broker);
//	    
//	    System.out.println("Allowed Exchanges: " + allowedExchanges);
//
//
//	    if (reqModel.getExchange() == null || reqModel.getExchange().length == 0) {
//			return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW003,
//					ErrorMessageConstants.INVALID_EXCH);
//		}
//	    
//	    List<String> requestedExchanges = Arrays.asList(reqModel.getExchange());
//	    
//	    System.out.println("Requested Exchanges: " + requestedExchanges);
//
//
//	    /** Filter only valid exchanges **/
//	    List<String> normalizedAllowedExchanges = allowedExchanges.stream()
//	            .map(String::trim)
//	            .map(String::toLowerCase)
//	            .collect(Collectors.toList());
//	    System.out.println("Normalized Allowed Exchanges: " + normalizedAllowedExchanges);
//	    
//	    List<String> normalizedRequestedExchanges = requestedExchanges.stream()
//	            .map(String::trim)
//	            .map(String::toLowerCase)
//	            .collect(Collectors.toList());
//	    System.out.println("Normalized Requested Exchanges: " + normalizedRequestedExchanges);
//	    
//	    // Filter only valid exchanges
//	    List<String> validExchanges = normalizedRequestedExchanges.stream()
//	            .filter(normalizedAllowedExchanges::contains)
//	            .collect(Collectors.toList());
//	    
//	    System.out.println("Valid Exchanges: " + validExchanges);
//	    
//	    if (validExchanges.isEmpty()) {
//	        return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW003,
//	                ErrorMessageConstants.INVALID_EXCHANGE);
//	    }
//
//	    /** Update the request model with only valid exchanges **/
//	    
//	    reqModel.setExchange(validExchanges.toArray(new String[0]));
//		System.out.println("allowed exch" + allowedExchanges + "req exch" + requestedExchanges );
//		if (StringUtil.isNullOrEmpty(reqModel.getSearchText())) {
//			return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW120,
//					ErrorMessageConstants.INVALID_SEARCHTEXT);
//		}
////		if (reqModel.getExchange() == null || reqModel.getExchange().length == 0) {
////			return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW003,
////					ErrorMessageConstants.INVALID_EXCH);
////		}
////		if (!commonUtils.checkExchangeIsValid(reqModel.getExchange())) {
////			return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW003,
////					ErrorMessageConstants.INVALID_EXCHANGE);
////		}
//		if (reqModel.getSearchText().trim().length() >= 2) {
//			return scripsService.getScrips(reqModel);
//		}
//
//		return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW112,
//				ErrorMessageConstants.ERROR_MIN_CHAR);
//	}
	
	@Override
	public RestResponse<ResponseModel> getScrips(SearchScripReqModel reqModel) {
	    ClinetInfoModel info = appUtil.getClientInfo();
	    if (info == null || StringUtil.isNullOrEmpty(info.getUserId())) {
	        Log.error("Client info is null");
	        return prepareResponse.prepareFailedResponse(AppConstants.FAILED_STATUS);
	    }

	    String userId = info.getUserId().toLowerCase();
	    String broker = userId.split("_")[0];
	    System.out.println("userid: " + broker);

	    List<String> allowedExchanges = brokerprerepo.findExchangesByBroker(broker);
	    System.out.println("Allowed Exchanges: " + allowedExchanges);

	    if (reqModel.getExchange() == null || reqModel.getExchange().length == 0) {
	        return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW003,
	                ErrorMessageConstants.INVALID_EXCH);
	    }

	    List<String> requestedExchanges = Arrays.asList(reqModel.getExchange());
	    System.out.println("Requested Exchanges: " + requestedExchanges);

	    // First, properly split and clean the allowed exchanges
	    Set<String> normalizedAllowedExchanges = allowedExchanges.stream()
	            .flatMap(s -> Arrays.stream(s.split(","))) // Split by comma
	            .map(String::trim)
	            .map(String::toLowerCase)
	            .collect(Collectors.toSet());
	    
	    System.out.println("Normalized Allowed Exchanges: " + normalizedAllowedExchanges);

	    // Then normalize and filter requested exchanges
	    List<String> validExchanges = requestedExchanges.stream()
	            .map(String::trim)
	            .map(String::toLowerCase)
	            .filter(normalizedAllowedExchanges::contains)
	            .collect(Collectors.toList());

	    System.out.println("Valid Exchanges: " + validExchanges);

	    if (validExchanges.isEmpty()) {
	        return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW003,
	                ErrorMessageConstants.INVALID_EXCHANGE);
	    }

	    // Update request model with validated exchanges
	    reqModel.setExchange(validExchanges.toArray(new String[0]));

	    if (StringUtil.isNullOrEmpty(reqModel.getSearchText())) {
	        return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW120,
	                ErrorMessageConstants.INVALID_SEARCHTEXT);
	    }

	    if (reqModel.getSearchText().trim().length() >= 2) {
	        return scripsService.getScrips(reqModel);
	    }

	    return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW112,
	            ErrorMessageConstants.ERROR_MIN_CHAR);
	}
	
	@Override
	public RestResponse<ResponseModel> getRecentlyViewed() {
		ClinetInfoModel info = appUtil.getClientInfo();
		if (info == null || StringUtil.isNullOrEmpty(info.getUserId())) {
			Log.error("Client info is null");
			return prepareResponse.prepareFailedResponse(AppConstants.FAILED_STATUS);
		} else if (StringUtil.isNullOrEmpty(info.getUcc())) {
			return prepareResponse.prepareFailedResponse(AppConstants.GUEST_USER_ERROR);
		}
		return scripsService.getRecentlyViewed(info);
	}
}
