package in.codifi.mw.controller;

import javax.inject.Inject;
import javax.ws.rs.Path;
import org.jboss.resteasy.reactive.RestResponse;
import in.codifi.mw.model.PreMwRequestModel;
import in.codifi.mw.model.PreMwScripRequestModel;
import in.codifi.mw.controller.spec.PredefinedMwControllerspec;
import in.codifi.mw.util.PrepareResponse;
import in.codifi.mw.model.ResponseModel;
import in.codifi.mw.service.spec.PreMarketWatchServicespec;
import in.codifi.mw.util.AppUtil;


@Path("/preMarketWatch")
public class PredifinedMwController implements PredefinedMwControllerspec {
	
	@Inject
	PreMarketWatchServicespec PreMarketWatchService;
	
	@Inject
	PrepareResponse prepareResponse;

	@Inject
	AppUtil utils;


	/**
	 * Method to create predefine marketwatch name
	 * @author Vinitha 
	 * @return
	 */
	public RestResponse<ResponseModel> createPreMw(PreMwRequestModel pDto) {
		return PreMarketWatchService.createPreMw(pDto);
	}

	/**
	 * Method to update predefine marketwatch name
	 * @author Vinitha 
	 * @return
	 */
	public RestResponse<ResponseModel> updatePreMw(PreMwRequestModel pDto) {
		return PreMarketWatchService.updatePreMw(pDto);
	}


	/**
	 * Method to delete predefine marketwatch name
	 * @author Vinitha 
	 * @param MwId
	 * @return
	 */	public RestResponse<ResponseModel> deletePreMw(Long MwId) {
		return PreMarketWatchService.deletePreMw(MwId);
	}


	 /**
		 * Method to enable/disable predefine marketwatch name
		 * @author Vinitha 
		 * @param MwId
		 * @return
		 */
	public RestResponse<ResponseModel> enableDisablePreMw(Long MwId) {
		return PreMarketWatchService.enableDisablePreMw(MwId);
	}

	/**
	 * method to get all data
	 * @author Vinitha
	 * @return
	 */
	public RestResponse<ResponseModel> getAllData() {
		return PreMarketWatchService.getAllData() ;
	}

	/**
	 * method to Add Scrips 
	 * @author Vinitha
	 * @param MwId
	 * @param MwName
	 * @return
	 */
	public RestResponse<ResponseModel> addScrip(Long MwId , String MwName) {
		return PreMarketWatchService.addScrip(MwId,MwName);
	}

	/**
	 * Method to Delete Scrips
	 * @author Vinitha
	 * @param MwId
	 * @param token
	 * @return
	 */
	public RestResponse<ResponseModel> deleteScrip(PreMwScripRequestModel pDto) {
		
		return PreMarketWatchService.deleteScrip(pDto.getMwId(),pDto.getToken());
	}


	/**
	 * Method to Sort Scrips
	 * @param MwId
	 * @param MwName
	 * @param id
	 * @param sortOrder
	 * @author Vinitha
	 *@return
	 */

	public RestResponse<ResponseModel> sortMwScrips(Long MwId, String MwName,Long id,int sortOrder ) {
		
		return PreMarketWatchService.sortScrip(MwId,MwName,id,sortOrder);
	}

	/**
	 * Method to insert Scrips
	 * @author Vinitha
	 *@return
	 */
	public RestResponse<ResponseModel> insertScrip(PreMwScripRequestModel pDto) {
		return PreMarketWatchService.insertScrip(pDto);
	}

	
}
