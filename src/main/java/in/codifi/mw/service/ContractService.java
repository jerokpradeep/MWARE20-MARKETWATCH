/**
 * 
 */
package in.codifi.mw.service;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.resteasy.reactive.RestResponse;

import in.codifi.cache.model.ContractMasterModel;
import in.codifi.cache.model.UnderlyingModel;
import in.codifi.mw.config.HazelcastConfig;
import in.codifi.mw.entity.ContractEntity;
import in.codifi.mw.entity.primary.UnderlyingEntity;
import in.codifi.mw.model.GenericResponse;
import in.codifi.mw.model.ResponseModel;
import in.codifi.mw.repository.ContractEntityManager;
import in.codifi.mw.repository.ContractRepository;
import in.codifi.mw.repository.UnderlyingRepository;
import in.codifi.mw.service.spec.ContractServiceSpecs;
import in.codifi.mw.util.AppConstants;
import in.codifi.mw.util.PrepareResponse;
import in.codifi.mw.util.StringUtil;

/**
 * @author Vicky
 *
 */
@ApplicationScoped
public class ContractService implements ContractServiceSpecs {

	@Inject
	ContractEntityManager contractEntityManager;
	@Inject
	UnderlyingRepository underlyingRepository;
	@Inject
	PrepareResponse prepareResponse;
	@Inject
	ContractRepository contractRepository;

	/**
	 * 
	 */
	public void loadIsinByToken() {
		contractEntityManager.loadIsinByToken();

	}

	/**
	 * 
	 */
	public RestResponse<ResponseModel> loadUnderlyingScrips() {
		try {
			List<UnderlyingEntity> contractList = new ArrayList<>();
			contractList = underlyingRepository.findAll();
			if (contractList.size() > 0)
				HazelcastConfig.getInstance().getContractMaster().clear();
			for (UnderlyingEntity contractEntity : contractList) {
				UnderlyingModel result = new UnderlyingModel();

				result.setExch(contractEntity.getExchange());
				result.setIsin(contractEntity.getIsin());
				result.setLotSize(contractEntity.getLotSize());
				result.setSymbol(contractEntity.getSymbol());
				result.setToken(contractEntity.getToken());
				result.setType(contractEntity.getType());
				result.setUnderlying(contractEntity.getUnderlying());

				String key = contractEntity.getSymbol();
				HazelcastConfig.getInstance().getUnderlyingScript().put(key, result);
			}
			System.out.println("Underlying Scrips Loaded SucessFully");
			System.out.println("Full Size " + HazelcastConfig.getInstance().getUnderlyingScript().size());

		} catch (Exception e) {
			e.printStackTrace();
			return prepareResponse.prepareFailedResponse(AppConstants.UNDERLYING_LOAD_FAILED);
		}
		return prepareResponse.prepareSuccessMessage(AppConstants.UNDERLYING_LOAD_SUCESS);

	}
	
	@Override
	public RestResponse<ResponseModel> loadContractMaster() {
		try {
			List<ContractEntity> contractList = new ArrayList<>();
			contractList = contractRepository.findAll();
			if (contractList.size() > 0)
				HazelcastConfig.getInstance().getContractMaster().clear();
			for (ContractEntity contractEntity : contractList) {
				ContractMasterModel result = new ContractMasterModel();

				result.setExch(contractEntity.getExch());
				result.setSegment(contractEntity.getSegment());
				result.setSymbol(contractEntity.getSymbol());
				result.setIsin(contractEntity.getIsin());
				result.setFormattedInsName(contractEntity.getFormattedInsName());
				result.setToken(contractEntity.getToken());
				result.setTradingSymbol(contractEntity.getTradingSymbol());
				result.setGroupName(contractEntity.getGroupName());
				result.setInsType(contractEntity.getInsType());
				result.setOptionType(contractEntity.getOptionType());
				result.setStrikePrice(contractEntity.getStrikePrice());
				result.setExpiry(contractEntity.getExpiryDate());
				result.setLotSize(contractEntity.getLotSize());
				result.setTickSize(contractEntity.getTickSize());
				result.setPdc(contractEntity.getPdc());
				result.setWeekTag(contractEntity.getWeekTag());
				result.setFreezQty(contractEntity.getFreezeQty());
				result.setAlterToken(contractEntity.getAlterToken());
				result.setCompanyName(contractEntity.getCompanyName());
				String key = contractEntity.getExch() + "_" + contractEntity.getToken();
				HazelcastConfig.getInstance().getContractMaster().put(key, result);

				String token = contractEntity.getToken();
				String exch = contractEntity.getExch().toUpperCase();
				String tradingSymbol = contractEntity.getTradingSymbol();
				if (StringUtil.isNotNullOrEmpty(tradingSymbol)) {
					HazelcastConfig.getInstance().getTradingSymbolTokenMapKB().put(tradingSymbol.toUpperCase(),
							token + "_" + exch);
				}
			}
			System.out.println("Loaded SucessFully");
			System.out.println("Full Size " + HazelcastConfig.getInstance().getContractMaster().size());

			System.out.println("Trading symbol token map TR Loaded SucessFully");
			System.out.println("Trading symbol token map TR Full Size "
					+ HazelcastConfig.getInstance().getTradingSymbolTokenMapKB().size());
		} catch (Exception e) {
			e.printStackTrace();
			return prepareResponse.prepareFailedResponse(AppConstants.CONTRACT_LOAD_FAILED);
		}
		return prepareResponse.prepareSuccessMessage(AppConstants.CONTRACT_LOAD_SUCESS);
	}
}
