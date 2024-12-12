package in.codifi.mw.service.spec;

import org.jboss.resteasy.reactive.RestResponse;
import org.json.simple.JSONObject;

import in.codifi.mw.model.ClinetInfoModel;
import in.codifi.mw.model.MwCommodityContarctModel;
import in.codifi.mw.model.MwRequestModel;
import in.codifi.mw.model.ResponseModel;
import in.codifi.mw.model.SecurityInfoReqModel;

public interface IMarketWatchService {

	/**
	 * @author Vicky
	 * @param userId
	 * @return
	 */
	RestResponse<ResponseModel> createMW(String userId);

	/**
	 * @param pDto
	 * @param userId
	 * @return
	 */
	RestResponse<ResponseModel> renameMarketWatch(MwRequestModel pDto, String userId);

	/**
	 * @param pDto
	 * @param userId
	 * @return
	 */
	RestResponse<ResponseModel> sortMwScrips(MwRequestModel pDto, String userId);

	/**
	 * @param pDto
	 * @param userId
	 * @return
	 */
	RestResponse<ResponseModel> addscrip(MwRequestModel pDto, String userId);

	/**
	 * @param pDto
	 * @param userId
	 * @return
	 */
	RestResponse<ResponseModel> deletescrip(MwRequestModel pDto, String userId);

	/**
	 * @param pDto
	 * @param userId
	 * @return
	 */
	RestResponse<ResponseModel> getAllMwScrips(String userId);

	/**
	 * @return
	 */
	JSONObject getIndices();

	/**
	 * @param pDto
	 * @return
	 */
	RestResponse<ResponseModel> getcommodityContarct(MwCommodityContarctModel pDto);

	/**
	 * @param model
	 * @param info
	 * @return
	 */
	RestResponse<ResponseModel> getSecurityInfo(SecurityInfoReqModel model, ClinetInfoModel info);

}
