package in.codifi.mw.controller.spec;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.reactive.RestResponse;
import in.codifi.mw.model.PreMwRequestModel;
import in.codifi.mw.model.PreMwScripRequestModel;
import in.codifi.mw.model.ResponseModel;

public interface PredefinedMwControllerspec {

	/**
	 * Method to create predefine marketwatch name
	 * @author Vinitha 
	 * @return
	 */
	@Path("/create")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponse(description = "To create predefined market Watch")
	RestResponse<ResponseModel> createPreMw(PreMwRequestModel pDto);
	
	/**
	 * Method to update predefine marketwatch name
	 * @author Vinitha 
	 * @return
	 */
	@Path("/update")
	@PATCH 
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponse(description = "To update predefined market Watch")
	RestResponse<ResponseModel> updatePreMw(PreMwRequestModel pDto);
	
	/**
	 * Method to delete predefine marketwatch name
	 * @author Vinitha 
	 * @param MwId
	 * @return
	 */
	@Path("/delete/{MwId}")
	@DELETE 
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponse(description = "To delete predefined market Watch")
	RestResponse<ResponseModel> deletePreMw(@PathParam("MwId") Long MwId);
	
	/**
	 * Method to enable/disable predefine marketwatch name
	 * @author Vinitha 
	 * @param MwId
	 * @return
	 */
	@Path("/enabledisable/{MwId}")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponse(description = "To enabledisable predefined market Watch")
    public RestResponse<ResponseModel> enableDisablePreMw(@PathParam("MwId") Long MwId); 
	
	/**
	 * method to get all data
	 * @author Vinitha
	 * @return
	 */
	@Path("/getAllData")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	RestResponse<ResponseModel> getAllData();
	
	/**
	 * method to Add Scrips 
	 * @author Vinitha
	 * @param MwId
	 * @param MwName
	 * @return
	 */
	@Path("/add/scrip/{MwId}/{MwName}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponse(description = "To addscrip predefined market Watch")
	RestResponse<ResponseModel> addScrip(@PathParam("MwId") Long MwId,
										 @PathParam("MwName") String MwName);
	
	/**
	 * Method to Delete Scrips
	 * @author Vinitha
	 * @param MwId
	 * @param token
	 * @return
	 */
	@Path("/delete/scrip")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponse(description = "To deletescrip predefined market Watch")
	RestResponse<ResponseModel> deleteScrip(PreMwScripRequestModel pDto);
	
	/**
	 * Method to Sort Scrips
	 * @param MwId
	 * @param MwName
	 * @param id
	 * @param sortOrder
	 * @author Vinitha
	 *@return
	 */
	@Path("/sort/scrip/{MwId}/{MwName}/{id}/{sortOrder}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	RestResponse<ResponseModel> sortMwScrips(@PathParam("MwId") Long MwId,
											 @PathParam("MwName")String MwName ,
											 @PathParam("id") Long id,
											 @PathParam("sortOrder")int sortOrder);

	/**
	 * Method to insert Scrips
	 * @author Vinitha
	 *@return
	 */
	@Path("/insert/scrip")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponse(description = "")
	RestResponse<ResponseModel> insertScrip(PreMwScripRequestModel pDto);
}
