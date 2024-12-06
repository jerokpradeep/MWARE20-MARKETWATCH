/**
 * 
 */
package in.codifi.mw.controller.spec;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.reactive.RestResponse;

import in.codifi.mw.model.ResponseModel;
import in.codifi.mw.model.SearchScripReqModel;

/**
 * @author Vicky
 *
 */
public interface ScripsControllerSpecs {

	/**
	 * @param reqModel
	 * @return
	 */
	@Path("/search")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@APIResponse(description = "Get all the scrips based upon the scrip")
	RestResponse<ResponseModel> getScrips(SearchScripReqModel reqModel);

}
