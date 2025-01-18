package in.codifi.mw.controller;

import javax.inject.Inject;
import javax.ws.rs.Path;

import org.jboss.resteasy.reactive.RestResponse;
import org.json.simple.JSONObject;

import in.codifi.mw.controller.spec.IMarketWatchController;
import in.codifi.mw.model.ClinetInfoModel;
import in.codifi.mw.model.GetContractInfoReqModel;
import in.codifi.mw.model.MwCommodityContarctModel;
import in.codifi.mw.model.MwIndicesModel;
import in.codifi.mw.model.MwRequestModel;
import in.codifi.mw.model.ResponseModel;
import in.codifi.mw.model.SecurityInfoReqModel;
import in.codifi.mw.repository.MarketWatchDAO;
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
	ValidateRequestService validFateRequestService;
	@Inject
	IMarketWatchService iMarketWatchService;
	@Inject
	PrepareResponse prepareResponse;
	@Inject
	AppUtil appUtil;
	@Inject
	MarketWatchDAO dao;

	@Override
	public String test() {
//		dao.loadContract();
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
		return iMarketWatchService.createMW(info.getUserId());
//		return iMarketWatchService.createMW("Test109");

	}

	/**
	 * @author Vicky
	 * @param pDto
	 * @return
	 */
	@Override
	public RestResponse<ResponseModel> renameMarketWatch(MwRequestModel pDto) {

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

		return iMarketWatchService.renameMarketWatch(pDto, info.getUcc());

	}

	@Override
	public RestResponse<ResponseModel> sortMwScrips(MwRequestModel pDto) {

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
		return iMarketWatchService.sortMwScrips(pDto, info.getUcc());

//		boolean isValid = validateRequestService.isValidUser(pDto);
//		if (isValid) {
//			return iMarketWatchService.sortMwScrips(pDto);
//		} else {
//			return prepareResponse.prepareFailedResponse(AppConstants.INVALID_PARAMETER);
//		}

//		return iMarketWatchService.sortMwScrips(pDto);
	}

	/**
	 * Method to add the scrip into cache and data base
	 * 
	 * @author Gowrisankar
	 */
	@Override
	public RestResponse<ResponseModel> addscrip(MwRequestModel pDto) {

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
		return iMarketWatchService.addscrip(pDto, info.getUcc());

//		boolean isValid = validateRequestService.isValidUser(pDto);
//		if (isValid) {
//			return iMarketWatchService.addscrip(pDto);
//		} else {
//			return prepareResponse.prepareFailedResponse(AppConstants.INVALID_PARAMETER);
//		}

//		return iMarketWatchService.addscrip(pDto);
	}

	/**
	 * Method to delete the scrips from the cache and market watch
	 * 
	 * @author Gowrisankar
	 */
	@Override
	public RestResponse<ResponseModel> deletescrip(MwRequestModel pDto) {

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
		return iMarketWatchService.deletescrip(pDto, info.getUcc());

//		boolean isValid = validateRequestService.isValidUser(pDto);
//		if (isValid) {
//			return iMarketWatchService.deletescrip(pDto);
//		} else {
//			return prepareResponse.prepareFailedResponse(AppConstants.INVALID_PARAMETER);
//		}
//		return iMarketWatchService.deletescrip(pDto);
	}

	/**
	 * Method to provide the user scrips details from cache or data base
	 * 
	 * @author Gowrisankar
	 */
	@Override
	public RestResponse<ResponseModel> getAllMwScrips() {

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
		return iMarketWatchService.getAllMwScrips(info.getUcc());

//		System.out.println("getAllMwScrips - controller");
//		boolean isValid = validateRequestService.isValidUser(pDto);
//		if (isValid) {
//			return iMarketWatchService.getAllMwScrips(pDto.getUserId());
//		} else {
//			return prepareResponse.prepareFailedResponse(AppConstants.INVALID_PARAMETER);
//		}
//		return iMarketWatchService.getAllMwScrips(pDto.getUserId());
	}

	/**
	 * Method to geIndices
	 * 
	 */
	@Override
	public JSONObject getIndices(MwIndicesModel pDto) {

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

	/**
	 * method to getSecurityInfo Details
	 * 
	 * @author Vicky
	 * 
	 */
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

	/***
	 * 
	 */
	@Override
	public RestResponse<ResponseModel> getContractInfo(GetContractInfoReqModel reqModel) {
		if (reqModel == null)
			return prepareResponse.prepareFailedResponse(AppConstants.INVALID_PARAMETER);

		ClinetInfoModel info = appUtil.getClientInfo();
		if (info == null || StringUtil.isNullOrEmpty(info.getUserId())) {
			Log.error("Client info is null");
			return prepareResponse.prepareFailedResponse(AppConstants.FAILED_STATUS);
		}
		return iMarketWatchService.getContractInfo(reqModel, info);
	}

	/**
	 * 
	 * @param pDto
	 * @return
	 */
	@Override
	public RestResponse<ResponseModel> getAllMwScripsMob(MwRequestModel pDto) {
		System.out.println("getAllMwScripsMob - controller");
//		boolean isValid = validateRequestService.isValidUser(pDto);
//		if (isValid) {
//			return codifiMwService.getAllMwScripsMob(pDto.getUserId(), pDto.isPredefined());
//		} else {
//			return prepareResponse.prepareFailedResponse(AppConstants.INVALID_PARAMETER);
//		}

		ClinetInfoModel info = appUtil.getClientInfo();
		if (info == null || StringUtil.isNullOrEmpty(info.getUserId())) {
			Log.error("Client info is null");
			return prepareResponse.prepareFailedResponse(AppConstants.FAILED_STATUS);
		}
		return iMarketWatchService.getAllMwScripsMob(info.getUserId(), true);
	}
}
