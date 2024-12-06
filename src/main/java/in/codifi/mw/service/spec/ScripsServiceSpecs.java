/**
 * 
 */
package in.codifi.mw.service.spec;

import org.jboss.resteasy.reactive.RestResponse;

import in.codifi.mw.model.ResponseModel;
import in.codifi.mw.model.SearchScripReqModel;

/**
 * @author Vicky
 *
 */
public interface ScripsServiceSpecs {

	/**
	 * @param reqModel
	 * @return
	 */
	RestResponse<ResponseModel> getScrips(SearchScripReqModel reqModel);

}
