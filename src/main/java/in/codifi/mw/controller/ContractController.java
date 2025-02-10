/**
 * 
 */
package in.codifi.mw.controller;

import javax.inject.Inject;
import javax.ws.rs.Path;

import org.jboss.resteasy.reactive.RestResponse;

import in.codifi.mw.controller.spec.ContractControllerSpec;
import in.codifi.mw.model.LoaderReqModel;
import in.codifi.mw.model.ResponseModel;
import in.codifi.mw.service.spec.ContractServiceSpecs;

/**
 * @author Vicky
 *
 */
@Path("/contract")
public class ContractController implements ContractControllerSpec {

	@Inject
	ContractServiceSpecs service;
	
	/**
	 * Method to get ASM/GSM file from server
	 * 
	 * @author Dinesh Kumar
	 *
	 * @return
	 */
	@Override
	public RestResponse<ResponseModel> reloadAsmGsmFile(LoaderReqModel model) {
		return service.reloadAsmGsmFile(model.getDaysOffset());
	}

	/**
	 * Method to reload contract master file from server
	 * 
	 * @author Dinesh Kumar
	 * 
	 * @return
	 */
	@Override
	public RestResponse<ResponseModel> reloadContractMasterFileV1(LoaderReqModel model) {
		return service.reloadContractMasterFileV1(model.getDaysOffset());
	}
	
	
	@Override
	public RestResponse<ResponseModel> manualInsertScriptFile() {
		return service.manualInsertScriptFile();
	}
}
