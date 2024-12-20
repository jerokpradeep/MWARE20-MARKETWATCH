/**
 * 
 */
package in.codifi.mw.controller;

import javax.inject.Inject;
import javax.ws.rs.Path;

import org.jboss.resteasy.reactive.RestResponse;

import in.codifi.mw.controller.spec.ScripsControllerSpecs;
import in.codifi.mw.model.ClinetInfoModel;
import in.codifi.mw.model.ResponseModel;
import in.codifi.mw.model.SearchScripReqModel;
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

	@Override
	public RestResponse<ResponseModel> getScrips(SearchScripReqModel reqModel) {

		if (StringUtil.isNullOrEmpty(reqModel.getSearchText())) {
			return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW120,
					ErrorMessageConstants.INVALID_SEARCHTEXT);
		}
		if (reqModel.getExchange() == null || reqModel.getExchange().length == 0) {
			return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW003,
					ErrorMessageConstants.INVALID_EXCH);
		}
		if (!commonUtils.checkExchangeIsValid(reqModel.getExchange())) {
			return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW003,
					ErrorMessageConstants.INVALID_EXCHANGE);
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
