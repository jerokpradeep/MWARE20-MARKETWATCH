package in.codifi.mw.controller.spec;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.reactive.RestResponse;
import org.json.simple.JSONObject;

import in.codifi.mw.model.MwCommodityContarctModel;
import in.codifi.mw.model.MwIndicesModel;
import in.codifi.mw.model.MwRequestModel;
import in.codifi.mw.model.ResponseModel;
import in.codifi.mw.model.SecurityInfoReqModel;

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

	@Path("/sortMwScrips")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponse(description = "To add symbol in market Watch")
	public RestResponse<ResponseModel> sortMwScrips(MwRequestModel pDto);

	/**
	 * Method to add the scrip into cache and data base
	 * 
	 * @param pDto
	 * @return
	 */
	@Path("/addscrip")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponse(description = "To add symbol in market Watch")
	public RestResponse<ResponseModel> addscrip(MwRequestModel pDto);

	/**
	 * Method to delete the scrips from the cache and market watch
	 * 
	 * @author Gowrisankar
	 * @param pDto
	 * @return
	 */
	@Path("/deletescrip")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponse(description = "To delete symbol in market Watch")
	public RestResponse<ResponseModel> deletescrip(MwRequestModel pDto);

	/**
	 * Method to get the scrip details for the given User Id *
	 * 
	 * @author Gowrisankar
	 * @param pDto
	 * @return
	 */
	@Path("/getAllMwScrips")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponse(description = "Get Scrip details for given user id .")
	RestResponse<ResponseModel> getAllMwScrips();

	/**
	 * Method to geIndices
	 * 
	 */
	@Path("/getIndices")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	JSONObject getIndices(MwIndicesModel pDto);

	/**
	 * Method to geIndices
	 * 
	 */
	@Path("/getcommodityContarct")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	RestResponse<ResponseModel> getcommodityContarct(MwCommodityContarctModel pDto);

	/**
	 * 
	 * @param model
	 * @return
	 */
	@Path("/security/info")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	RestResponse<ResponseModel> getSecurityInfo(SecurityInfoReqModel model);
}
