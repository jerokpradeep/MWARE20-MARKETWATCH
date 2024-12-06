/**
 * 
 */
package in.codifi.mw.controller;

import javax.inject.Inject;
import javax.ws.rs.Path;

import org.jboss.resteasy.reactive.RestResponse;

import in.codifi.mw.controller.spec.ScripsControllerSpecs;
import in.codifi.mw.model.ResponseModel;
import in.codifi.mw.model.SearchScripReqModel;
import in.codifi.mw.service.spec.ScripsServiceSpecs;
import in.codifi.mw.util.AppConstants;
import in.codifi.mw.util.PrepareResponse;
import in.codifi.mw.util.StringUtil;

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
	
	@Override
	public RestResponse<ResponseModel> getScrips(SearchScripReqModel reqModel) {

		if (StringUtil.isNullOrEmpty(reqModel.getSearchText())) {
			return prepareResponse.prepareMWFailedResponseString(AppConstants.INVALID_SEARCH_TEXT);
		}
		
		if (reqModel != null && reqModel.getSearchText().trim().length() >= 2) {
			return scripsService.getScrips(reqModel);
		}
		return prepareResponse.prepareFailedResponse(AppConstants.ERROR_MIN_CHAR);
	}
}
