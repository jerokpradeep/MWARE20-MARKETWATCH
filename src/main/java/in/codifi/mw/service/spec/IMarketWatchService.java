package in.codifi.mw.service.spec;

import org.jboss.resteasy.reactive.RestResponse;

import in.codifi.mw.model.MWResponseModel;
import in.codifi.mw.model.MwRequestModel;
import in.codifi.mw.model.ResponseModel;

public interface IMarketWatchService {

	/**
	 * @author Vicky
	 * @param userId
	 * @return
	 */
	RestResponse<ResponseModel> createMW(String userId);

	/**
	 * @param pDto
	 * @return
	 */
	RestResponse<ResponseModel> renameMarketWatch(MwRequestModel pDto);

}
