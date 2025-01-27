/**
 * 
 */
package in.codifi.mw.service.spec;

import org.jboss.resteasy.reactive.RestResponse;

import in.codifi.mw.model.ResponseModel;

/**
 * @author Vicky
 *
 */
public interface ContractServiceSpecs {

	RestResponse<ResponseModel> loadContractMaster();

}
