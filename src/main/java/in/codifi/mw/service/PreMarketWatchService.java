package in.codifi.mw.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.resteasy.reactive.RestResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.codifi.cache.model.ContractMasterModel;
import in.codifi.mw.cache.HazelCacheController;
import in.codifi.mw.cache.MwCacheController;
import in.codifi.mw.cache.RedisConfig;
import in.codifi.mw.entity.PredefinedMwEntity;
import in.codifi.mw.entity.PredefinedMwScripsEntity;
import in.codifi.mw.model.PreMwRequestModel;
import in.codifi.mw.model.PreMwScripRequestModel;
import in.codifi.mw.model.ResponseModel;
import in.codifi.mw.repository.PreMarketWatchRepository;
import in.codifi.mw.repository.PredefinedMwScripRepository;
import in.codifi.mw.service.spec.PreMarketWatchServicespec;
import in.codifi.mw.util.ErrorCodeConstants;
import in.codifi.mw.util.ErrorMessageConstants;
import in.codifi.mw.util.PrepareResponse;
import in.codifi.mw.util.StringUtil;
import in.codifi.optionbees.model.OHLCModel;

@ApplicationScoped
public class PreMarketWatchService implements PreMarketWatchServicespec {

	@Inject
	PreMarketWatchRepository predefinedMwRepository;

	@Inject
	PredefinedMwScripRepository predefinedMwScripRepository;

	@Inject
	PrepareResponse prepareResponse;

	/**
	 * Method to create predefine marketwatch name
	 * 
	 * @author Vinitha
	 * @return
	 */
	public RestResponse<ResponseModel> createPreMw(PreMwRequestModel pDto) {
		try {
			if (pDto == null || pDto.getMwId() == null || StringUtil.isNullOrEmpty(pDto.getMwName())
					|| pDto.getPosition() == null || pDto.getPosition() <= 0)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_REQUEST,
						ErrorCodeConstants.ECMW003);

			if (pDto.getMwName().length() > 40)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_NAME,
						ErrorCodeConstants.ECMW003);

			if (pDto.getMwId() > 15 || pDto.getPosition() > 15)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_ID,
						ErrorCodeConstants.ECMW003);

			if ((!isValidBinaryInput(pDto.getIsEditable())) || (!isValidBinaryInput(pDto.getIsEnabled())))
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_BINARY,
						ErrorCodeConstants.ECMW003);

			if ((!isValidBinaryInput1(pDto.getActiveStatus())))
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_BINARY,
						ErrorCodeConstants.ECMW003);

			PredefinedMwEntity marketWatch = new PredefinedMwEntity();

			marketWatch.setMwId(pDto.getMwId());
			marketWatch.setMwName(pDto.getMwName());
			marketWatch.setPosition(pDto.getPosition());
			marketWatch.setTag(pDto.getTag());
//	        marketWatch.setCreatedBy(pDto.getCreatedBy());
//	        marketWatch.setUpdatedBy(pDto.getUpdatedBy());	        
			marketWatch.setUpdatedOn(pDto.getUpdatedOn());
			marketWatch.setIsEnabled(pDto.getIsEnabled());
			marketWatch.setIsEditable(pDto.getIsEditable());
			marketWatch.setActiveStatus(pDto.getActiveStatus());

			PredefinedMwEntity savedMarketWatch = predefinedMwRepository.save(marketWatch);

			if (savedMarketWatch == null)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.FAILED_CREATION,
						ErrorCodeConstants.ECMW006);

			return prepareResponse.prepareMWSuccessResponseObject2(ErrorMessageConstants.MARKET_WATCH_CREATED,
					ErrorCodeConstants.ECMW019);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.FAILED_CREATION,
				ErrorCodeConstants.ECMW006);
	}

	/**
	 * Method to update predefine marketwatch name
	 * 
	 * @author Vinitha
	 * @return
	 */
	public RestResponse<ResponseModel> updatePreMw(PreMwRequestModel pDto) {
		try {
			if (pDto == null || pDto.getMwId() == null || pDto.getMwId() <= 0)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_REQUEST,
						ErrorCodeConstants.ECMW003);

			if (pDto.getMwId() > 15)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_ID,
						ErrorCodeConstants.ECMW003);

			PredefinedMwEntity marketWatch = predefinedMwRepository.findByMwId(pDto.getMwId());
			if (marketWatch == null)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.NO_MW,
						ErrorCodeConstants.ECMW003);

			if (pDto.getMwName().length() > 40)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_NAME,
						ErrorCodeConstants.ECMW003);

			if (pDto.getTag().length() > 10)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_NAME,
						ErrorCodeConstants.ECMW003);

			if (pDto.getMwId() > 15 || pDto.getPosition() > 15)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_ID,
						ErrorCodeConstants.ECMW003);

			if ((!isValidBinaryInput(pDto.getIsEditable())) || (!isValidBinaryInput(pDto.getIsEnabled())))
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_BINARY,
						ErrorCodeConstants.ECMW003);

			if ((!isValidBinaryInput1(pDto.getActiveStatus())))
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_BINARY,
						ErrorCodeConstants.ECMW003);

			if (pDto.getMwName() != null && !pDto.getMwName().equals(marketWatch.getMwName()))
				marketWatch.setMwName(pDto.getMwName());

			else if (pDto.getTag() != null && !pDto.getTag().equals(marketWatch.getTag()))
				marketWatch.setTag(pDto.getTag());

			else if (pDto.getPosition() != null) {
				if (pDto.getPosition() <= 0)
					return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_REQUEST,
							ErrorCodeConstants.ECMW003);

				if (!pDto.getPosition().equals(marketWatch.getPosition()))
					marketWatch.setPosition(pDto.getPosition());
			}

			else if (pDto.getIsEnabled() != null && !pDto.getIsEnabled().equals(marketWatch.getIsEnabled()))
				marketWatch.setIsEnabled(pDto.getIsEnabled());

			else if (pDto.getIsEditable() != null && !pDto.getIsEditable().equals(marketWatch.getIsEditable()))
				marketWatch.setIsEditable(pDto.getIsEditable());

			PredefinedMwEntity updatedMarketWatch = predefinedMwRepository.save(marketWatch);

			if (updatedMarketWatch == null)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.NOT_ABLE_TO_MW_UPDATE,
						ErrorCodeConstants.ECMW007);

			return prepareResponse.prepareMWSuccessResponseObject2(ErrorMessageConstants.SUCCESS_UPDATE,
					ErrorCodeConstants.ECMW020);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.NOT_ABLE_TO_MW_UPDATE,
				ErrorCodeConstants.ECMW007);
	}

	/**
	 * Method to delete predefine marketwatch name
	 * 
	 * @author Vinitha
	 * @return
	 */
	public RestResponse<ResponseModel> deletePreMw(Long MwId) {
		try {
			if (MwId == null || MwId <= 0)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_REQUEST,
						ErrorCodeConstants.ECMW003);

			if (MwId > 15)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_ID,
						ErrorCodeConstants.ECMW003);

			PredefinedMwEntity entity = predefinedMwRepository.findByMwId(MwId);
			if (entity != null)
				predefinedMwRepository.deleteByMwId(MwId);
			else
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.NO_MW,
						ErrorCodeConstants.ECMW003);

			PredefinedMwEntity deletedEntity = predefinedMwRepository.findByMwId(MwId);
			if (deletedEntity == null) {
				return prepareResponse.prepareMWSuccessResponseObject2(ErrorMessageConstants.SUCCESS_DELETE,
						ErrorCodeConstants.ECMW021);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.FAILED_STATUS,
				ErrorCodeConstants.ECMW102);
	}

	/**
	 * Method to enable/disable predefine marketwatch name
	 * 
	 * @author Vinitha
	 * @return
	 */
	public RestResponse<ResponseModel> enableDisablePreMw(Long mwId) {
		try {
			if (mwId == null || mwId <= 0)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_REQUEST,
						ErrorCodeConstants.ECMW003);

			if (mwId > 15)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_ID,
						ErrorCodeConstants.ECMW003);

			PredefinedMwEntity entity = predefinedMwRepository.findByMwId(mwId);

			if (entity == null)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.NO_MW,
						ErrorCodeConstants.ECMW003);

			PredefinedMwEntity record = predefinedMwRepository.findByMwId(mwId);
			if (record != null) {
				if (record.getIsEnabled() == 1) {
					record.setIsEnabled(0);
				} else {
					record.setIsEnabled(1);
				}
				predefinedMwRepository.save(record);
			}
			return prepareResponse.prepareMWSuccessResponseObject2(ErrorMessageConstants.SUCCESS_STATUS,
					ErrorCodeConstants.ECMW001);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.FAILED_STATUS,
				ErrorCodeConstants.ECMW102);
	}

	/**
	 * method to get all data
	 * 
	 * @author Vinitha
	 * @return
	 */
	public RestResponse<ResponseModel> getAllData() {
		try {
			List<PredefinedMwEntity> entities = predefinedMwRepository.findAll();

			if (entities == null)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.NULL_DATA_STATUS,
						ErrorCodeConstants.ECMW009);

			if (entities.isEmpty())
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.NO_DATA_FOUND_STATUS,
						ErrorCodeConstants.ECMW017);

			if (entities != null) {
				entities.forEach(entity -> {
					List<PredefinedMwScripsEntity> enrichedScrips = entity.getScrips().stream()
							.map(this::enrichWithCacheData).collect(Collectors.toList());
					entity.setScrips(enrichedScrips);
				});
			}

			return prepareResponse.prepareSuccessResponseObject(entities, ErrorCodeConstants.ECMW001);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.FAILED_STATUS,
				ErrorCodeConstants.ECMW102);
	}

	/**
	 * method to Add Scrip
	 * 
	 * @author Vinitha
	 * @return
	 */
	public RestResponse<ResponseModel> addScrip(Long MwId, String MwName) {
		try {
			if (MwId == null || MwName == null)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_REQUEST,
						ErrorCodeConstants.ECMW003);

			if (MwName.length() > 40)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_NAME,
						ErrorCodeConstants.ECMW003);

			if (MwId > 15)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_ID,
						ErrorCodeConstants.ECMW003);

			PredefinedMwEntity data = predefinedMwRepository.findByMwIdAndMwName(MwId, MwName);

			if (data == null)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.NO_MW,
						ErrorCodeConstants.ECMW005);

			if (data.getIsEditable() == 0)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.SCRIP_ISNOT_EDITABLE,
						ErrorCodeConstants.ECMW018);

			long maxSortOrder = calculateMaxSortOrder();

			List<PredefinedMwScripsEntity> scripDetails = getScripMW(data, maxSortOrder);
			if (scripDetails == null || scripDetails.isEmpty())
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.NO_MW,
						ErrorCodeConstants.ECMW010);

			data.getScrips().addAll(scripDetails);
			PredefinedMwEntity newData = predefinedMwRepository.saveAndFlush(data);
			if (newData != null)
				MwCacheController.getPredefinedMwList().clear();
			return prepareResponse.prepareMWSuccessResponseObject2(ErrorMessageConstants.SUCCESS_TO_ADD,
					ErrorCodeConstants.ECMW022);
		} catch (Exception e) {
			e.printStackTrace();
			return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.FAILED_TO_ADD,
					ErrorCodeConstants.ECMW011);
		}

	}

	/**
	 * method to calculate MaxSortOrder
	 * 
	 * @author Vinitha
	 * @return
	 */
	private long calculateMaxSortOrder() {
		List<PredefinedMwScripsEntity> allScrips = predefinedMwScripRepository.findAll();
		long maxSortOrder = 0;
		for (PredefinedMwScripsEntity scrip : allScrips) {
			if (scrip.getSortOrder() > maxSortOrder) {
				maxSortOrder = scrip.getSortOrder();
			}
		}
		return maxSortOrder;
	}

	/**
	 * method to get scrips from cache
	 * 
	 * @author Vinitha
	 * @return
	 */
	private List<PredefinedMwScripsEntity> getScripMW(PredefinedMwEntity data, long maxSortOrder) {
		List<PredefinedMwScripsEntity> response = new ArrayList<>();
		try {
			for (int i = 0; i < data.getScrips().size(); i++) {
				maxSortOrder = maxSortOrder + 1;

				PredefinedMwScripsEntity scrip = new PredefinedMwScripsEntity();
				scrip = data.getScrips().get(i);
				String exch = scrip.getExchange();
				String token = scrip.getToken();
				if (RedisConfig.getInstance().getJedis().hexists("contractMaster", exch + "_" + token)) {
					String Json = RedisConfig.getInstance().getJedis().hget("wsData", exch + "_" + token);
					ObjectMapper objectMapper = new ObjectMapper();
					// Deserialize the JSON string into an OHLCModel object
					ContractMasterModel model = objectMapper.readValue(Json, ContractMasterModel.class);
					scrip.setMwId(data.getMwId());
					scrip.setToken(model.getToken());
					scrip.setExchange(model.getExch());
					scrip.setSortOrder(maxSortOrder);
					response.add(scrip);
				}
			}
		} catch (

		Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * Method to add Scrip details which is not added in predefined mw scriptable
	 * 
	 * @author Vinitha
	 * @return
	 */
	private PredefinedMwScripsEntity enrichWithCacheData(PredefinedMwScripsEntity scrip) {
		
		String cacheKey = scrip.getExchange() + "_" + scrip.getToken();
		
		String Json = RedisConfig.getInstance().getJedis().hget("wsData",cacheKey);
		ObjectMapper objectMapper = new ObjectMapper();
		// Deserialize the JSON string into an OHLCModel object
		ContractMasterModel model = objectMapper.readValue(Json, ContractMasterModel.class);
//		ContractMasterModel masterData = HazelCacheController.getInstance().getContractMaster().get(cacheKey);

		if (model != null) {
			scrip.setSymbol(model.getSymbol());
			scrip.setTradingSymbol(model.getTradingSymbol());
			scrip.setFormattedInsName(model.getFormattedInsName());
			scrip.setSegment(model.getSegment());
			scrip.setPdc(model.getPdc());
			scrip.setExpiry(model.getExpiry());
			scrip.setWeekTag(model.getWeekTag());
		}
		return scrip;
	}

	/**
	 * Method to delete Scrip
	 * 
	 * @author Vinitha
	 * @return
	 */
	public RestResponse<ResponseModel> deleteScrip(Long MwId, String token) {
		try {
			if (MwId == null || MwId <= 0 || token == null)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_REQUEST,
						ErrorCodeConstants.ECMW003);

			if (MwId > 15)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_ID,
						ErrorCodeConstants.ECMW003);

			PredefinedMwEntity entity = predefinedMwRepository.findByMwId(MwId);

			if (entity == null)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_REQUEST,
						ErrorCodeConstants.ECMW003);

			predefinedMwScripRepository.deleteByToken(token);

			PredefinedMwScripsEntity entity1 = predefinedMwScripRepository.findByToken(token);

			if (entity1 == null)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.NO_MW,
						ErrorCodeConstants.ECMW005);

			PredefinedMwScripsEntity deletedEntity = predefinedMwScripRepository.findByToken(token);
			if (deletedEntity == null)
				return prepareResponse.prepareMWSuccessResponseObject2(ErrorMessageConstants.SUCCESS_DEL_SCRIP,
						ErrorCodeConstants.ECMW024);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.FAILED_DEL_SCRIP,
				ErrorCodeConstants.ECMW012);
	}

	/**
	 * Method to Sort Scrips
	 * 
	 * @author Vinitha
	 * @return
	 */
	public RestResponse<ResponseModel> sortScrip(Long MwId, String MwName, Long id, int sortOrder) {
		try {
			if (MwId == null || MwId <= 0 || MwName == null || id == null)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_REQUEST,
						ErrorCodeConstants.ECMW003);

			if (MwId > 15)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_ID,
						ErrorCodeConstants.ECMW003);

			if (MwName.length() > 40)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_NAME,
						ErrorCodeConstants.ECMW003);

			if (sortOrder > 60)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_ORDER,
						ErrorCodeConstants.ECMW003);

			PredefinedMwEntity entity = predefinedMwRepository.findByMwIdAndMwName(MwId, MwName);

			if (entity == null)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.NO_MW,
						ErrorCodeConstants.ECMW003);
			if (MwId > 15)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_ID,
						ErrorCodeConstants.ECMW003);

			boolean scripFound = false;
			for (PredefinedMwScripsEntity scripsEntity : entity.getScrips()) {
				if (scripsEntity.getId().equals(id)) {
					scripsEntity.setSortOrder(sortOrder);
					scripFound = true;
					break;
				}
			}
			if (!scripFound)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_SCRIP_ID,
						ErrorCodeConstants.ECMW016);

			predefinedMwRepository.save(entity);
			return prepareResponse.prepareMWSuccessResponseObject2(ErrorMessageConstants.SCRIPS_SORTED,
					ErrorCodeConstants.ECMW023);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.SCRIP_NOT_SORTED,
				ErrorCodeConstants.ECMW013);
	}

	/**
	 * Method to insert Scrips
	 * 
	 * @author Vinitha
	 * @return
	 */
	public RestResponse<ResponseModel> insertScrip(PreMwScripRequestModel pDto) {
		try {
			if (pDto == null)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_REQUEST,
						ErrorCodeConstants.ECMW003);

			if (pDto.getMwId() <= 0 || pDto.getToken().trim().isEmpty() || pDto.getToken().trim() == null
					|| pDto.getExchange() == null || pDto.getExchange().trim().isEmpty())
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_REQUEST,
						ErrorCodeConstants.ECMW003);

			if (pDto.getMwId() > 15)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_ID,
						ErrorCodeConstants.ECMW003);

			if (pDto.getSortOrder() > 60)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_ORDER,
						ErrorCodeConstants.ECMW003);

//			List<PredefinedMwScripsEntity> data = predefinedMwScripRepository.findByMwId(pDto.getMwId());
//			for(PredefinedMwScripsEntity dt:data)			
//				if(pDto.getSortOrder() == dt.getSortOrder())
//					return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_ORDER ,ErrorCodeConstants.ECMW003);	
//			
			final List<String> VALID_OPTIONS = Arrays.asList("BCD", "bcd", "BFO", "bfo", "BSE", "bse", "CDS", "cds",
					"MCX", "NCO", "nco", "NFO", "nfo", "NSE", "nse");

			if (pDto.getExchange().length() > 10 || (!VALID_OPTIONS.contains(pDto.getExchange())))
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_EXCHANGE,
						ErrorCodeConstants.ECMW003);

			if (pDto.getToken().length() > 10)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_TOKEN,
						ErrorCodeConstants.ECMW003);

			PredefinedMwScripsEntity marketWatch = new PredefinedMwScripsEntity();

			marketWatch.setMwId(pDto.getMwId());
			marketWatch.setToken(pDto.getToken());
			marketWatch.setExchange(pDto.getExchange());
			marketWatch.setActiveStatus(pDto.getActiveStatus());
			marketWatch.setSortOrder(pDto.getSortOrder());

			PredefinedMwScripsEntity savedMarketWatch = predefinedMwScripRepository.save(marketWatch);

			if (savedMarketWatch == null)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.NO_DATA_FOUND_STATUS,
						ErrorCodeConstants.ECMW017);

			return prepareResponse.prepareMWSuccessResponseObject2(ErrorMessageConstants.SUCCESS_TO_ADD,
					ErrorCodeConstants.ECMW022);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.FAILED_INSERT_DATA,
				ErrorCodeConstants.ECMW014);
	}

	public boolean isValidBinaryInput(Long value) {
		return value == 0 || value == 1;
	}

	public boolean isValidBinaryInput1(int value) {
		return value == 0 || value == 1;
	}
}
