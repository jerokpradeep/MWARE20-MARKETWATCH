package in.codifi.mw.controller;

import javax.inject.Inject;
import javax.ws.rs.Path;

import org.jboss.resteasy.reactive.RestResponse;

import in.codifi.mw.controller.spec.IMarketWatchController;
import in.codifi.mw.model.ClinetInfoModel;
import in.codifi.mw.model.MwCommodityContarctModel;
import in.codifi.mw.model.MwIndicesModel;
import in.codifi.mw.model.MwRequestModel;
import in.codifi.mw.model.ResponseModel;
import in.codifi.mw.model.SecurityInfoReqModel;
import in.codifi.mw.service.ValidateRequestService;
import in.codifi.mw.service.spec.IMarketWatchService;
import in.codifi.mw.util.AppConstants;
import in.codifi.mw.util.AppUtil;
import in.codifi.mw.util.PrepareResponse;
import in.codifi.mw.util.StringUtil;
import io.quarkus.logging.Log;

@Path("/marketWatch")
public class MarketWatchController implements IMarketWatchController {

	@Inject
	ValidateRequestService validateRequestService;
	@Inject
	IMarketWatchService iMarketWatchService;
	@Inject
	PrepareResponse prepareResponse;
	@Inject
	AppUtil appUtil;

	@Override
	public String test() {
		return "MarketWatch Module is working";
	}

	/**
	 * 
	 * method to create Market Watch
	 * 
	 * @author Vicky
	 * @param pDto
	 * @return
	 */
	@Override
	public RestResponse<ResponseModel> createMW(MwRequestModel pDto) {

		/*
		 * Check the client info, get the user with the client info
		 */
		ClinetInfoModel info = appUtil.getClientInfo();
		if (info == null || StringUtil.isNullOrEmpty(info.getUserId())) {
			Log.error("Client info is null");
			return prepareResponse.prepareFailedResponse(AppConstants.INVALID_PARAMETER);
		} else if (StringUtil.isNullOrEmpty(info.getUcc())) {
			Log.error("Client info is null");
			return prepareResponse.prepareFailedResponse(AppConstants.INVALID_PARAMETER);
		}
		return iMarketWatchService.createMW(info.getUcc());
//		return iMarketWatchService.createMW("Test109");

	}

	/**
	 * @author Vicky
	 * @param pDto
	 * @return
	 */
	@Override
	public RestResponse<ResponseModel> renameMarketWatch(MwRequestModel pDto) {

		boolean isValid = validateRequestService.isValidUser(pDto);
		if (isValid) {
			return iMarketWatchService.renameMarketWatch(pDto);
		} else {
			return prepareResponse.prepareFailedResponse(AppConstants.INVALID_PARAMETER);
		}
//		return iMarketWatchService.renameMarketWatch(pDto);
	}

	@Override
	public RestResponse<ResponseModel> sortMwScrips(MwRequestModel pDto) {

		boolean isValid = validateRequestService.isValidUser(pDto);
		if (isValid) {
			return iMarketWatchService.sortMwScrips(pDto);
		} else {
			return prepareResponse.prepareFailedResponse(AppConstants.INVALID_PARAMETER);
		}

//		return iMarketWatchService.sortMwScrips(pDto);
	}

	/**
	 * Method to add the scrip into cache and data base
	 * 
	 * @author Gowrisankar
	 */
	@Override
	public RestResponse<ResponseModel> addscrip(MwRequestModel pDto) {

		boolean isValid = validateRequestService.isValidUser(pDto);
		if (isValid) {
			return iMarketWatchService.addscrip(pDto);
		} else {
			return prepareResponse.prepareFailedResponse(AppConstants.INVALID_PARAMETER);
		}

//		return iMarketWatchService.addscrip(pDto);
	}

	/**
	 * Method to delete the scrips from the cache and market watch
	 * 
	 * @author Gowrisankar
	 */
	@Override
	public RestResponse<ResponseModel> deletescrip(MwRequestModel pDto) {
		boolean isValid = validateRequestService.isValidUser(pDto);
		if (isValid) {
			return iMarketWatchService.deletescrip(pDto);
		} else {
			return prepareResponse.prepareFailedResponse(AppConstants.INVALID_PARAMETER);
		}
//		return iMarketWatchService.deletescrip(pDto);
	}

	/**
	 * Method to provide the user scrips details from cache or data base
	 * 
	 * @author Gowrisankar
	 */
	@Override
	public RestResponse<ResponseModel> getAllMwScrips(MwRequestModel pDto) {

		System.out.println("getAllMwScrips - controller");
		boolean isValid = validateRequestService.isValidUser(pDto);
		if (isValid) {
			return iMarketWatchService.getAllMwScrips(pDto.getUserId());
		} else {
			return prepareResponse.prepareFailedResponse(AppConstants.INVALID_PARAMETER);
		}
//		return iMarketWatchService.getAllMwScrips(pDto.getUserId());
	}

	/**
	 * Method to geIndices
	 * 
	 */
	@Override
	public RestResponse<ResponseModel> getIndices(MwIndicesModel pDto) {

//		boolean isValid = validateRequestService.isValidUser(pDto);
//		if (isValid) {
//			return iMarketWatchService.getAllMwScrips(pDto.getUserId());
//		} else {
//			return prepareResponse.prepareFailedResponse(AppConstants.INVALID_PARAMETER);
//		}
		return iMarketWatchService.getIndices();
	}

	/**
	 * Method to get Commodity Contarct
	 *
	 */
	@Override
	public RestResponse<ResponseModel> getcommodityContarct(MwCommodityContarctModel pDto) {

//		boolean isValid = validateRequestService.isValidUser(pDto);
//		if (isValid) {
//			return iMarketWatchService.getAllMwScrips(pDto.getUserId());
//		} else {
//			return prepareResponse.prepareFailedResponse(AppConstants.INVALID_PARAMETER);
//		}
		return iMarketWatchService.getcommodityContarct(pDto);
	}
	
	@Override
	public RestResponse<ResponseModel> getSecurityInfo(SecurityInfoReqModel model) {

		if (model == null)
			return prepareResponse.prepareFailedResponse(AppConstants.INVALID_PARAMETER);

		ClinetInfoModel info = appUtil.getClientInfo();
		if (info == null || StringUtil.isNullOrEmpty(info.getUserId())) {
			Log.error("Client info is null");
			return prepareResponse.prepareFailedResponse(AppConstants.FAILED_STATUS);
		} 
		return iMarketWatchService.getSecurityInfo(model, info);
	}
}
