package in.codifi.mw.service.spec;

import org.jboss.resteasy.reactive.RestResponse;
import in.codifi.mw.model.PreMwRequestModel;
import in.codifi.mw.model.PreMwScripRequestModel;
import in.codifi.mw.model.ResponseModel;

public interface PreMarketWatchServicespec {
	

	/**
	 * Method to create predefine marketwatch name
	 * @author Vinitha 
	 * @return
	 */

	RestResponse<ResponseModel> createPreMw(PreMwRequestModel pDto);

	/**
	 * Method to update predefine marketwatch name
	 * @author Vinitha 
	 * @return
	 */
	RestResponse<ResponseModel> updatePreMw(PreMwRequestModel pDto);

	/**
	 * Method to delete predefine marketwatch name
	 * @author Vinitha 
	 * @return
	 */
	RestResponse<ResponseModel> deletePreMw(Long MwId);
	
	
	/**
	 * Method to enable/disable predefine marketwatch name
	 * @author Vinitha 
	 * @return
	 */
	RestResponse<ResponseModel> enableDisablePreMw(Long mwId);


	/**
	 * method to get all data
	 * @author Vinitha
	 * @param responseContext 
	 * @param requestContext 
	 * @return
	 */
	RestResponse<ResponseModel> getAllData();

	/**
	 * method to Add Scrip 
	 * @author Vinitha
	 * @return
	 */
	RestResponse<ResponseModel> addScrip(Long MwId, String MwName);

	/**
	 * Method to delete Scrip
	 * @author Vinitha
	 * @return
	 */
	RestResponse<ResponseModel> deleteScrip(Long MwId, String token);

	/**
	 * Method to Sort Scrips
	 * @author Vinitha
	 *@return
	 */
	RestResponse<ResponseModel> sortScrip(Long MwId, String MwName ,Long id,int sortOrder);

	/**
	 * Method to insert Scrips
	 * @author Vinitha
	 *@return
	 */
	RestResponse<ResponseModel> insertScrip(PreMwScripRequestModel pDto);
	
}
