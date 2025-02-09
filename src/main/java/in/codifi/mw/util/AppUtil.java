package in.codifi.mw.util;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import in.codifi.cache.model.ContractMasterModel;
import in.codifi.mw.config.HazelcastConfig;
import in.codifi.mw.controller.DefaultRestController;
import in.codifi.mw.model.ClinetInfoModel;
import in.codifi.mw.model.PnlLotModel;
import io.quarkus.logging.Log;

@ApplicationScoped
public class AppUtil extends DefaultRestController {

	/**
	 * 
	 * Method to get client info
	 * 
	 * @author Dinesh Kumar
	 *
	 * @return
	 */
	public ClinetInfoModel getClientInfo() {
		ClinetInfoModel model = clientInfo();
		return model;
	}

	/**
	 * 
	 * Method to get contract master
	 * 
	 * @author Dinesh Kumar
	 *
	 * @param exch
	 * @param token
	 * @return
	 */
	public ContractMasterModel getContractMaster(String exch, String token) {
		ContractMasterModel contractMasterModel = new ContractMasterModel();
		contractMasterModel = HazelcastConfig.getInstance().getContractMaster().get(exch + "_" + token);
		return contractMasterModel;
	}

	/**
	 * Method to get pnl lot
	 * 
	 * @author Dinesh Kumar
	 * @param exch
	 * @param symbol
	 * @return
	 */
	public String getPnlLotSize(String exch, String symbol) {
		String pnlLotSize = "";
		List<PnlLotModel> pnlLotModelList = new ArrayList<>();
		if (HazelcastConfig.getInstance().getPnlLot().get(AppConstants.PNL_LOT) != null) {
			pnlLotModelList = HazelcastConfig.getInstance().getPnlLot().get(AppConstants.PNL_LOT);
			for (PnlLotModel pnlLotModel : pnlLotModelList) {
				if (pnlLotModel.getExch().equalsIgnoreCase(exch) && pnlLotModel.getSymbol().equalsIgnoreCase(symbol)) {
					pnlLotSize = pnlLotModel.getLotSize();
					break;
				}
			}
		} else {
			Log.error("PNL_LOT is not in cache");
			// TODO need to load if pnl lot is not in cache
		}
		return pnlLotSize;
	}

	/**
	 * Method to get access token
	 * 
	 * @author DINESH KUMAR
	 *
	 * @return
	 */
	public String getAccessToken() {
		String token = "";
		try {
			token = getAcToken();
		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		}
		return token;
	}
}
