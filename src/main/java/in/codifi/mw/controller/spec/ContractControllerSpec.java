/**
 * 
 */
package in.codifi.mw.controller.spec;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.reactive.RestResponse;

import in.codifi.mw.model.LoaderReqModel;
import in.codifi.mw.model.ResponseModel;

/**
 * @author Vicky
 *
 */
public interface ContractControllerSpec {

	/**
	 * Method to get ASM/GSM file from server
	 * 
	 * @author Dinesh Kumar
	 *
	 * @return
	 */
	@Path("/reload/asmgsm")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	RestResponse<ResponseModel> reloadAsmGsmFile(LoaderReqModel model);

	/**
	 * Method to get reload - contract master file from server
	 * 
	 * @author Nesan
	 *
	 * @return
	 */
	@Path("/reload/contractmaster/v1")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	RestResponse<ResponseModel> reloadContractMasterFileV1(LoaderReqModel model);

	/**
	 * @return
	 */
	@Path("/load/scripdata")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	RestResponse<ResponseModel> manualInsertScriptFile();

}
