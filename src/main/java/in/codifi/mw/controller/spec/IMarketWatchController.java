package in.codifi.mw.controller.spec;


import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.reactive.RestResponse;

import in.codifi.mw.model.MwRequestModel;
import in.codifi.mw.model.ResponseModel;

public interface IMarketWatchController {

	/**
	 * @author Vicky
	 * @return
	 */
	@Path("/test")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	String test();

	/**
	 * Method to create the new marketWatch
	 * 
	 * @author Dinesh Kumar
	 * @param requestContext
	 * @return
	 */
	@Path("/createMW")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponse(description = "To create market Watch")
	RestResponse<ResponseModel> createMW(MwRequestModel pDto);

	/**
	 * Method to change market watch name
	 * 
	 * @param pDto
	 * @return
	 */
	@Path("/renameMW")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponse(description = "To rename market Watch")
	RestResponse<ResponseModel> renameMarketWatch(MwRequestModel pDto);

}
