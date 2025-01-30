/**
 * 
 */
package in.codifi.mw.service.spec;

import org.jboss.resteasy.reactive.RestResponse;

import in.codifi.mw.model.GenericResponse;
import in.codifi.mw.model.ResponseModel;

/**
 * @author Vicky
 *
 */
public interface ContractServiceSpecs {

	/**
	 * @return
	 */
	RestResponse<ResponseModel> deleteExpiredContract();

	/**
	 * @return
	 */
	RestResponse<ResponseModel> loadContractMaster();

	/**
	 * @param daysOffset
	 * @return
	 */
	RestResponse<ResponseModel> reloadAsmGsmFile(int daysOffset);
	
	/**
	 * Method to load PNL Lot
	 * @author Dinesh Kumar
	 * @return
	 */
	RestResponse<ResponseModel> loadPnlLotSize();

	/**
	 * @param daysOffset
	 * @return
	 */
	RestResponse<ResponseModel> reloadContractMasterFileV1(int daysOffset);
}
