package in.codifi.mw.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.lang3.ObjectUtils;
import org.jboss.resteasy.reactive.RestResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.codifi.cache.model.ContractMasterModel;
import in.codifi.mw.cache.MwCacheController;
import in.codifi.mw.cache.RedisConfig;
import in.codifi.mw.cache.RedisConstants;
import in.codifi.mw.config.ApplicationProperties;
import in.codifi.mw.entity.HoldingsDataMwEntity;
import in.codifi.mw.entity.MarketWatchNameDTO;
import in.codifi.mw.entity.MarketWatchScripDetailsDTO;
import in.codifi.mw.entity.PositionDataMwEntity;
import in.codifi.mw.entity.PredefinedMwEntity;
import in.codifi.mw.entity.PredefinedMwScripsEntity;
import in.codifi.mw.entity.secondary.RecentlyViewedEntity;
import in.codifi.mw.model.Badge;
import in.codifi.mw.model.CacheMwDetailsModel;
import in.codifi.mw.model.ClinetInfoModel;
import in.codifi.mw.model.ContractInfoDetails;
import in.codifi.mw.model.ContractInfoRespModel;
import in.codifi.mw.model.GetContractInfoReqModel;
import in.codifi.mw.model.IndexModel;
import in.codifi.mw.model.MwCommodityContarctModel;
import in.codifi.mw.model.MwRequestModel;
import in.codifi.mw.model.MwScripModel;
import in.codifi.mw.model.ProductLeverage;
import in.codifi.mw.model.Prompt;
import in.codifi.mw.model.PromptModel;
import in.codifi.mw.model.ResponseModel;
import in.codifi.mw.model.SecurityInfoReqModel;
import in.codifi.mw.model.SecurityInfoRespModel;
import in.codifi.mw.model.SpotData;
import in.codifi.mw.model.badgeModel;
import in.codifi.mw.repository.HoldingsMwRepo;
import in.codifi.mw.repository.MarketWatchDAO;
import in.codifi.mw.repository.PositionMwRepo;
import in.codifi.mw.repository.PredefinedMwRepo;
import in.codifi.mw.repository.RecentlyViewedRepository;
import in.codifi.mw.service.spec.IMarketWatchService;
import in.codifi.mw.util.AppConstants;
import in.codifi.mw.util.CommonUtils;
import in.codifi.mw.util.ErrorCodeConstants;
import in.codifi.mw.util.ErrorMessageConstants;
import in.codifi.mw.util.PrepareResponse;
import in.codifi.mw.util.StringUtil;
import io.quarkus.logging.Log;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@SuppressWarnings("unchecked")
@Service
public class MarketWatchService implements IMarketWatchService {
	private static final int MAX_SCRIPTS = 49;

//	private static final ConcurrentHashMap<String, List<IndexModel>> getIndexData = new ConcurrentHashMap<>();
	@Autowired
	PrepareResponse prepareResponse;
	@Inject
	MarketWatchDAO marketWatchDAO;
	@Inject
	ApplicationProperties properties;
	@Inject
	CommonUtils commonUtils;
	@Inject
	RecentlyViewedRepository recentlyViewedRepository;
	@Inject
	PredefinedMwRepo predefinedMwRepo;
	@Inject
	HoldingsMwRepo holdingsMwRepo;
	@Inject
	PositionMwRepo positionMwRepo;

	/**
	 * Method to Create Mw in Auto and manual
	 * 
	 */
	@Override
	public RestResponse<ResponseModel> createMW(String pUserId) {
		try {
			if (StringUtil.isNotNullOrEmpty(pUserId)) {
				if (properties.getMwFlow().equalsIgnoreCase("auto")) {
					/* Check user has how many market watch */
					List<MarketWatchNameDTO> mwList = marketWatchDAO.findAllByUserId(pUserId);
					/* If null or size is lesser than 5 create a new Market Watch */
					if (mwList == null || mwList.size() == 0) {
						/* Create the new Market Watch */
						List<MarketWatchNameDTO> newMwList = new ArrayList<MarketWatchNameDTO>();
						// TODO change hot code value
						int mwListSize = Integer.parseInt(properties.getMwSize());
						int mwId = 300;
						for (int i = 0; i < mwListSize; i++) {
							mwId = mwId + 1;
							System.out.println("mwId>>>>>" + mwId);
							MarketWatchNameDTO newDto = new MarketWatchNameDTO();
							newDto.setUserId(pUserId);
							newDto.setMwId(mwId);
							if (mwId == 301) {
								newDto.setMwName(AppConstants.RECENTLY_SEARCHED_MW_NAME);
							} else {
								newDto.setMwName(AppConstants.MARKET_WATCH_LIST_NAME + (i + 1));
							}
							newDto.setPosition(Long.valueOf(i));
							newMwList.add(newDto);
						}
						marketWatchDAO.insertMwName(newMwList);
						List<CacheMwDetailsModel> scripDetails = marketWatchDAO.getMarketWatchByUserId(pUserId);
						if (scripDetails != null && scripDetails.size() > 0) {
							List<JSONObject> tempResult = populateFields(scripDetails, pUserId);
							if (tempResult != null && tempResult.size() > 0) {
								prepareResponse.prepareSuccessResponseWithMessage(tempResult,
										AppConstants.MARKET_WATCH_CREATED);
								return prepareResponse.prepareMWSuccessResponseObject(AppConstants.SUCCESS_STATUS);
							}
						}
					} else { /* Else send the error response */
						return prepareResponse.prepareFailedResponse(AppConstants.LIMIT_REACHED_MW);
					}
				} else {
					/* Mw Manual Flow */
					List<MarketWatchNameDTO> mwList = marketWatchDAO.findAllByUserId(pUserId);
					int mwListSize = Integer.parseInt(properties.getMwSize());
					if (mwList.size() < mwListSize) {
						/* Create the new Market Watch */
						List<MarketWatchNameDTO> newMwList = new ArrayList<MarketWatchNameDTO>();

						MarketWatchNameDTO newDto = new MarketWatchNameDTO();
						newDto.setUserId(pUserId);
						newDto.setMwId(mwList.isEmpty() ? 1 : mwList.size() + 1);
						newDto.setMwName(
								AppConstants.MARKET_WATCH_LIST_NAME + (mwList.isEmpty() ? 1 : mwList.size() + 1));
						newDto.setPosition((long) mwList.size());
						newMwList.add(newDto);
						marketWatchDAO.insertMwName(newMwList);

						List<CacheMwDetailsModel> scripDetails = marketWatchDAO.getMarketWatchByUserId(pUserId);
						if (scripDetails != null && scripDetails.size() > 0) {
							List<JSONObject> tempResult = populateFields(scripDetails, pUserId);
							if (tempResult != null && tempResult.size() > 0) {
								prepareResponse.prepareSuccessResponseWithMessage(tempResult,
										AppConstants.MARKET_WATCH_CREATED);
								return prepareResponse.prepareMWSuccessResponseObject(AppConstants.SUCCESS_STATUS);
							}
						}
					} else {
						return prepareResponse.prepareFailedResponse(AppConstants.LIMIT_REACHED_MW);
					}
				}
			} else {
				return prepareResponse.prepareFailedResponse(AppConstants.INVALID_PARAMETER);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		}
		return prepareResponse.prepareFailedResponse(AppConstants.FAILED_STATUS);
	}

	/**
	 * @author Vicky
	 * @param scripDetails
	 * @param pUserId
	 * @return
	 */
	private List<JSONObject> populateFields(List<CacheMwDetailsModel> cacheMwDetailsModels, String pUserId) {

		List<JSONObject> response = new ArrayList<>();
		try {
			JSONObject tempResponse = new JSONObject();
			for (CacheMwDetailsModel tempModel : cacheMwDetailsModels) {
				String mwName = tempModel.getMwName();
				String mwId = String.valueOf(tempModel.getMwId());
				String tempMwID = pUserId + "_" + mwId + "_" + mwName;
				String scripName = tempModel.getFormattedInsName();
				if (scripName != null && !scripName.isEmpty()) {
					if (tempResponse.containsKey(tempMwID)) {
						List<CacheMwDetailsModel> tempList = new ArrayList<>();
						if (tempResponse.get(tempMwID) != null) {
							tempList = (List<CacheMwDetailsModel>) tempResponse.get(tempMwID);
						}
						tempList.add(tempModel);
						tempResponse.put(tempMwID, tempList);
					} else {
						List<CacheMwDetailsModel> tempList = new ArrayList<>();
						tempList.add(tempModel);
						tempResponse.put(tempMwID, tempList);
					}
				} else if (tempResponse.get(tempMwID) == null) {
					tempResponse.put(tempMwID, null);
				}
			}
			if (tempResponse != null) {
				response = getCacheListForScrips(tempResponse);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		}
		return response;
	}

	/**
	 * @author Vicky
	 * @param tempResponse
	 * @return
	 */
	private List<JSONObject> getCacheListForScrips(JSONObject mwResponse) {
		List<JSONObject> response = new ArrayList<JSONObject>();
		try {
			Iterator<String> itr = mwResponse.keySet().iterator();
			itr = sortedIterator(itr);
			while (itr.hasNext()) {
				String tempStr = itr.next();
				String[] tempStrArr = tempStr.split("_");
				String user = tempStrArr[0] + "_" + tempStrArr[1];
				String mwId = tempStrArr[2];
				String mwName = tempStrArr[3];
				JSONObject result = new JSONObject();
				List<CacheMwDetailsModel> tempJsonObject = new ArrayList<CacheMwDetailsModel>();
				tempJsonObject = (List<CacheMwDetailsModel>) mwResponse.get(tempStr);

				if (mwId.equalsIgnoreCase("301")) {
					result.put("mwId", mwId);
					result.put("mwName", AppConstants.RECENTLY_SEARCHED_MW_NAME);
					result.put("isEdit", false);
					result.put("isDefault", false);
					result.put("isRename", false);
				} else {
					result.put("mwId", mwId);
					result.put("mwName", mwName);
					result.put("isEdit", true);
					result.put("isDefault", false);
					result.put("isRename", true);
				}

				if (tempJsonObject != null && tempJsonObject.size() > 0) {
					result.put("scrips", tempJsonObject);
				} else {
					// Use an empty JSONArray to represent an empty array
					result.put("scrips", new JSONArray());
				}

				response = MwCacheController.getMwListUserId().get(user);
				if (response != null) {
					response = MwCacheController.getMwListUserId().get(user);
					response.add(result);
					MwCacheController.getMwListUserId().put(user, response);
				} else {
					response = new ArrayList<JSONObject>();
					response.add(result);
					MwCacheController.getMwListUserId().put(user, response);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * Sorting ITR
	 * 
	 * @param it
	 * @return
	 */
	public Iterator<String> sortedIterator(Iterator<String> it) {
		List<String> list = new ArrayList<>();
		while (it.hasNext()) {
			list.add((String) it.next());
		}
		Collections.sort(list);
		return list.iterator();
	}

	/**
	 * Method to rename MarketWatch
	 * 
	 */

	@SuppressWarnings("static-access")
	@Override
	public RestResponse<ResponseModel> renameMarketWatch(MwRequestModel pDto, String userId) {
		try {
			if (pDto != null) {
				if (pDto.getMwId() != 301) {
					if (pDto != null && pDto.getMwId() == 0) {
						return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW114,
								ErrorMessageConstants.INVALID_MWID);
					}
					if (StringUtil.isNullOrEmpty(pDto.getMwName().trim())) {
						return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW115,
								ErrorMessageConstants.INVALID_MWNAME);
					}

					if (pDto.getMwName() == null || pDto.getMwName().trim().isEmpty()) {
						return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW115,
								ErrorMessageConstants.INVALID_MWNAME);
					}

					if (!commonUtils.isBetweenOneAndFive(pDto.getMwId())) {
						return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW101,
								AppConstants.INVALID_MW_ID);
					}

					if (!commonUtils.isOnlyInteger(String.valueOf(pDto.getMwId()))) {
						return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW101,
								AppConstants.INVALID_MW_ID);
					}

					if (pDto.getMwName().length() > 40) {
						return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW102,
								AppConstants.MW_NAME_40);
					}

					if (!commonUtils.isAlphanumeric(pDto.getMwName().trim()) || pDto.getMwName() == null
							|| pDto.getMwName().isEmpty() || commonUtils.isEmptyOrWhitespace(pDto.getMwName())) {
						return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW103,
								AppConstants.MW_NAME);
					}
					if (commonUtils.isValidMWRename(pDto.getMwName())) {
						return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW114,
								ErrorMessageConstants.RENAME_INVALID);
					}
					String oldMwName = marketWatchDAO.getMWName(pDto.getMwId(), userId);
					boolean isDefault = marketWatchDAO.changeMwisDefaultStatus(pDto.getMwId(), userId,
							pDto.isDefault());
					renameMwInCache(pDto.getMwName().trim(), pDto.getMwId(), userId);
					updateMwNamw(pDto.getMwName().trim(), pDto.getMwId(), userId);
					return prepareResponse.prepareMWSuccessResponseString(
							oldMwName + " Renamed to " + pDto.getMwName().trim() + " Successfully.");
				} else {
					return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW123,
							AppConstants.RECENTLY_SEARCH_RENAME);
				}
			} else {
				return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW104,
						AppConstants.INVALID_PARAMETER);

			}

		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());

		}
		return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW105, AppConstants.FAILED_STATUS);

	}

	/** rename MW in cache **/
	private void renameMwInCache(String newWwName, int mwId, String userId) {

		List<JSONObject> res = MwCacheController.getMwListUserId().get(userId);
		String marketWatchId = String.valueOf(mwId);
		JSONObject result = null;
		if (res != null && res.size() > 0) {
			for (int itr = 0; itr < res.size(); itr++) {
				result = new JSONObject();
				result = res.get(itr);
				String mw = (String) result.get("mwId").toString();
				if (marketWatchId.equalsIgnoreCase(mw)) {
					result.remove("mwName");
					result.put("mwName", newWwName);
					res.remove(itr);
					res.add(itr, result);
					MwCacheController.getMwListUserId().remove(userId);
					MwCacheController.getMwListUserId().put(userId, res);
					break;
				}
			}
		}

	}

	/**
	 * Update the Mw name in DB
	 * 
	 * @param mwName
	 * @param mwId
	 * @param userId
	 */
	private void updateMwNamw(String mwName, int mwId, String userId) {
		ExecutorService pool = Executors.newSingleThreadExecutor();
		pool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					marketWatchDAO.updateMWName(mwName, mwId, userId);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					pool.shutdown();
				}
			}
		});

	}

	/**
	 * method to sortMw Script
	 * 
	 */
	@SuppressWarnings("static-access")
	@Override
	public RestResponse<ResponseModel> sortMwScrips(MwRequestModel pDto, String userId) {
		try {
			if (StringUtil.isNotNullOrEmpty(userId) && StringUtil.isListNotNullOrEmpty(pDto.getScripData())
					&& pDto.getMwId() > 0) {
				if (pDto.getMwId() != 301) {

					if (!commonUtils.isBetweenOneAndFive(pDto.getMwId())) {
						return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW101,
								AppConstants.INVALID_MW_ID);
					}

					for (int i = 0; i < pDto.getScripData().size(); i++) {
						String exchange = pDto.getScripData().get(i).getExchange();
						if (StringUtil.isNullOrEmpty(exchange)) {
							return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW117,
									ErrorMessageConstants.INVALID_EXCH);
						}
						if (exchange == null || exchange.trim().isEmpty()) {
							return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW117,
									ErrorMessageConstants.INVALID_EXCH);
						}
						// Check if exchange is null or invalid
						if (!commonUtils.isValidExch(exchange.toUpperCase().trim())) {
							return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW003,
									ErrorMessageConstants.INVALID_EXCHANGE);
						}
					}

					for (int i = 0; i < pDto.getScripData().size(); i++) {
						String token = pDto.getScripData().get(i).getToken();
						if (StringUtil.isNullOrEmpty(token.trim())) {
							return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW118,
									ErrorMessageConstants.TOKEN_EMTY_OR_NULL);
						}
						if (token == null || token.trim().isEmpty()) {
							return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW107,
									ErrorMessageConstants.INVALID_TOKEN);
						}
						if (!commonUtils.checkThisIsTheNumber(token.trim())) {
							return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW107,
									AppConstants.INVALID_TOKEN);
						}
					}

					for (int i = 0; i < pDto.getScripData().size(); i++) {
						int exch = pDto.getScripData().get(i).getSortingOrder();
						if (!commonUtils.isBetweenOneAndFifty(exch)) {
							return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW108,
									AppConstants.INVALID_SORTING_ORDER);
						}
					}

					sortFromCache(pDto.getScripData(), userId, pDto.getMwId());
					return sortScripInDataBase(pDto.getScripData(), userId, pDto.getMwId());
				} else {
					return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW124,
							AppConstants.RECENTLY_SEARCH_SORTING);
				}
			} else {
				return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW104,
						AppConstants.INVALID_PARAMETER);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW105, AppConstants.FAILED_STATUS);

	}

	/**
	 * 
	 * @param dataToSort
	 * @param pUserId
	 * @param userMwId
	 */
	public void sortFromCache(List<MwScripModel> dataToSort, String pUserId, int userMwId) {
		if (dataToSort != null && !dataToSort.isEmpty()) {
			List<JSONObject> res = MwCacheController.getMwListUserId().get(pUserId);
			String marketWatchId = String.valueOf(userMwId);
			JSONObject result = null;
			int indexOfRes = 0;

			if (res != null && !res.isEmpty()) {
				// Find the relevant Market Watch by mwId
				for (int itr = 0; itr < res.size(); itr++) {
					result = res.get(itr);
					String mwId = result.get("mwId").toString();
					if (marketWatchId.equalsIgnoreCase(mwId)) {
						indexOfRes = itr;
						break;
					}
				}

				if (result != null && !result.isEmpty()) {
					List<CacheMwDetailsModel> scripDetails = (List<CacheMwDetailsModel>) result.get("scrips");
					if (scripDetails != null && !scripDetails.isEmpty()) {

						// Update the sorting orders from 1 to N
						for (int i = 0; i < scripDetails.size(); i++) {
							CacheMwDetailsModel tempScripDTO = scripDetails.get(i);
							tempScripDTO.setSortOrder(i + 1); // Reindex starting from 1
						}

						result.put("scrips", scripDetails);
						res.set(indexOfRes, result);
						MwCacheController.getMwListUserId().put(pUserId, res);
					}
				}
			}
		}
	}

	/**
	 * Method to Sorting data insert to DB
	 * 
	 * @param scripDataToSort
	 * @param userId
	 * @param mwId
	 * @return
	 */

	private RestResponse<ResponseModel> sortScripInDataBase(List<MwScripModel> scripDataToSort, String userId,
			int mwId) {

		if (scripDataToSort != null && !scripDataToSort.isEmpty()) {
			List<MarketWatchScripDetailsDTO> mwList = marketWatchDAO.findAllByUserIdAndMwId(userId, mwId);

			// Validate if the size matches, if not, return an error response
			if (scripDataToSort.size() != mwList.size())
				return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW113,
						ErrorMessageConstants.MISMATCH_SORTORDER_SIZE);

			List<MarketWatchScripDetailsDTO> newScripDetails = new ArrayList<>();

			// Reindex the sorting order for the scripDataToSort from 1 to N
			for (int i = 0; i < scripDataToSort.size(); i++) {
				MwScripModel model = scripDataToSort.get(i);
				model.setSortingOrder(i + 1); // Reindex to start from 1
			}

			// Update the sorting order in the database
			for (int i = 0; i < scripDataToSort.size(); i++) {
				MwScripModel model = scripDataToSort.get(i);
				for (int j = 0; j < mwList.size(); j++) {
					MarketWatchScripDetailsDTO dbData = mwList.get(j);
					if (dbData.getToken().equalsIgnoreCase(model.getToken())
							&& dbData.getEx().equalsIgnoreCase(model.getExchange())) {
						dbData.setSortingOrder(model.getSortingOrder());
						newScripDetails.add(dbData);
					}
				}
			}

			if (!newScripDetails.isEmpty()) {
				int res = marketWatchDAO.updateMWScrips(newScripDetails, userId, mwId);
				if (res > 0) {
					System.out.println("Updated");
				}
			}
		}

		return prepareResponse.prepareMWSuccessResponseString(AppConstants.SORTING_ORDER);
	}

	/***
	 * Method to addScript in MW
	 * 
	 * @author Vicky
	 * 
	 */
	@SuppressWarnings("static-access")
	@Override
	public RestResponse<ResponseModel> addscrip(MwRequestModel parmDto, String userId) {
		try {
			// Check if Scrip Data is not null or empty
			if (StringUtil.isListNotNullOrEmpty(parmDto.getScripData())) {

				if (!commonUtils.isBetweenOneAndFive(parmDto.getMwId())) {
					return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW101,
							AppConstants.INVALID_MW_ID);
				}

				// Validate exchange and token for each scrip
				for (MwScripModel scrip : parmDto.getScripData()) {
					String exchange = scrip.getExchange();
					if (StringUtil.isNullOrEmpty(exchange.trim())) {
						return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW117,
								ErrorMessageConstants.INVALID_EXCH);
					}

					if (!commonUtils.isValidExch(exchange.toUpperCase().trim())) {
						return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW003,
								ErrorMessageConstants.INVALID_EXCHANGE);
					}

					String token = scrip.getToken();
					if (StringUtil.isNullOrEmpty(token.trim()) || !commonUtils.checkThisIsTheNumber(token.trim())) {
						return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW107,
								AppConstants.INVALID_TOKEN);
					}
				}

				Set<String> tokenSet = new HashSet<>();

				for (MwScripModel data : parmDto.getScripData()) {
					String token = data.getToken().trim();
					String exchange = data.getExchange().toUpperCase().trim();

					// Check for duplicate tokens + exchanges
					if (tokenSet.contains(token + exchange)) {
						return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW107,
								ErrorMessageConstants.SCRIP_ALDREADY);
					} else {
						tokenSet.add(token + exchange);
					}
				}
				JedisPool redispool = RedisConfig.getInstance().getJedisPool();
				// Validate token existence in the cache
				try (Jedis jedis = redispool.getResource()) {
					for (MwScripModel data : parmDto.getScripData()) {
						String token = data.getToken().trim();
						String exchange = data.getExchange().toUpperCase().trim();
						String cacheKey = (properties.isExchfull() ? commonUtils.getExchangeNameContract(exchange)
								: exchange) + "_" + token;

						if (!jedis.hexists("contractMaster", cacheKey)) {
							return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW003,
									ErrorMessageConstants.INVALID_TOKEN);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					Log.error("Error accessing Redis: " + e.getMessage());
				}

				// Assign sorting order and prepare scrip details
				int currentSortOrder = getExistingSortOrder(userId, parmDto.getMwId());
				List<MwScripModel> mwScripModels = new ArrayList<>();
				for (MwScripModel model : parmDto.getScripData()) {
					currentSortOrder++;
					model.setSortingOrder(currentSortOrder);
					mwScripModels.add(model);
				}

				// Handle the case for MW ID 301
				if (parmDto.getMwId() == 301) {
					int totalCount = marketWatchDAO.getRecentlyViewedScripsCount(parmDto.getMwId(), userId);
					if (totalCount > MAX_SCRIPTS) {
						List<MwScripModel> result = marketWatchDAO.getFirstRecords(parmDto.getMwId(), userId);
						boolean isDeleted = marketWatchDAO.deleteFirstRecords(parmDto.getMwId(), userId);
						if (isDeleted) {
							deleteFromCache(result, userId, parmDto.getMwId());
						}
					}
				}

				// Prepare Scrip Details
				List<CacheMwDetailsModel> scripDetails = getScripMW(mwScripModels);
				if (scripDetails != null && !scripDetails.isEmpty()) {
					List<CacheMwDetailsModel> newScripDetails = addNewScipsForMwIntoCache(scripDetails, userId,
							parmDto.getMwId());
					if (newScripDetails != null && !newScripDetails.isEmpty()) {
						insertNewScipsForMwIntoDataBase(newScripDetails, userId, parmDto.getMwId());
					}

					JSONObject finalOutput = new JSONObject();
					finalOutput.put("scrip", scripDetails);

					return prepareResponse.prepareSuccessResponseWithMessage(finalOutput, AppConstants.SUCCESS_STATUS,
							false);
				} else {
					return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW119,
							ErrorMessageConstants.SCRIP_NOT_AVAILABLE);
				}
			} else {
				return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW104,
						AppConstants.INVALID_PARAMETER);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.error("Error in addscrip method: " + e.getMessage());
			return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW105, AppConstants.FAILED_STATUS);
		}
	}

	/**
	 * Method to get the sorting order from the cache
	 * 
	 * @author Gowrisankar
	 * @param pUserId
	 * @param i
	 * @return
	 */
	private int getExistingSortOrder(String pUserId, int mwid) {
		int sortingOrder = 0;
		List<JSONObject> res = MwCacheController.getMwListUserId().get(pUserId);
		JSONObject result = null;
		String marketWatchId = String.valueOf(mwid);
		if (res != null && res.size() > 0) {
			for (int itr = 0; itr < res.size(); itr++) {
				result = new JSONObject();
				result = res.get(itr);
				String tempMwId = (String) result.get("mwId");
				if (tempMwId.equalsIgnoreCase(marketWatchId)) {
					List<CacheMwDetailsModel> scripDetails = (List<CacheMwDetailsModel>) result.get("scrips");
					if (scripDetails != null && scripDetails.size() > 0) {
						Optional<CacheMwDetailsModel> maxByOrder = scripDetails.stream()
								.max(Comparator.comparing(CacheMwDetailsModel::getSortOrder));
						CacheMwDetailsModel model = maxByOrder.get();
						if (model != null && model.getSortOrder() > 0) {
							sortingOrder = model.getSortOrder();
						}
					}
					break;
				}
			}
		}
		return sortingOrder;
	}

	/**
	 * Method to get the scrip from the cache for Market watch
	 * 
	 * @author Dinesh Kumar
	 * @param pDto
	 * @return
	 */
	public List<CacheMwDetailsModel> getScripMW(List<MwScripModel> pDto) {
		List<CacheMwDetailsModel> response = new ArrayList<>();
		JedisPool jedisPool = RedisConfig.getInstance().getJedisPool();

		// Use a try-with-resources block for Jedis pool to ensure proper resource
		// management.
		try (Jedis jedis = jedisPool.getResource()) {
			for (MwScripModel result : pDto) {
				String token = result.getToken().trim();
				String codifiExchange = "";

				// Determine the correct exchange format
				if (properties.isExchfull()) {
					String exchangeSegment = commonUtils
							.getExchangeSegmentNameIIFL(result.getExchange().trim().toUpperCase());
					codifiExchange = commonUtils.getExchangeName(exchangeSegment.toUpperCase().trim());
				} else {
					codifiExchange = result.getExchange().trim().toUpperCase();
				}

				// Prepare cache key
				String cacheKey = codifiExchange + "_" + token;

				try {
					// Check if the cache contains the key for contract data
					if (jedis.hexists(RedisConstants.CONTRACTMASTER, cacheKey)) {
						String json = jedis.hget(RedisConstants.CONTRACTMASTER, cacheKey);
						ObjectMapper objectMapper = new ObjectMapper();
						ContractMasterModel masterData = objectMapper.readValue(json, ContractMasterModel.class);

						if (masterData != null) {
							CacheMwDetailsModel fResult = new CacheMwDetailsModel();
							fResult.setSymbol(masterData.getSymbol());
							fResult.setTradingSymbol(masterData.getTradingSymbol());
							fResult.setFormattedInsName(masterData.getFormattedInsName());
							fResult.setToken(masterData.getToken());

							// Set exchange and segment based on full exchange flag
							if (properties.isExchfull()) {
								String exchangeIifl = commonUtils.getExchangeNameIIFL(masterData.getExch());
								fResult.setExchange(exchangeIifl);
								String segmentIifl = commonUtils.getExchangeName(masterData.getSegment());
								fResult.setSegment(segmentIifl);
							} else {
								fResult.setExchange(masterData.getExch());
								fResult.setSegment(masterData.getSegment());
							}

							// Set expiry, sortOrder, and other attributes
							fResult.setExpiry(masterData.getExpiry() == null ? "" : masterData.getExpiry().toString());
							fResult.setSortOrder(result.getSortingOrder());
							fResult.setPdc(masterData.getPdc());
							fResult.setWeekTag(masterData.getWeekTag() == null ? "" : masterData.getWeekTag());

							// Prepare badge and screeners (set empty screeners for now)
							badgeModel badge = new badgeModel();
							badge.setBnpl("");
							badge.setEvent(true);
							badge.setHoldingqty("0");
							badge.setIdeas("");
							List<String> screeners = List.of("topGainer", "52wk High", "Volume shocker");

							// Set badge and screeners in the response model
							fResult.setBadge(badge);
							fResult.setScreeners(new JSONArray()); // Screeners are empty for now

							// Add the populated model to the response list
							response.add(fResult);
						}
					}
				} catch (Exception e) {
					// Handle exception properly (e.g., log it)
					Log.error("Error processing scrip with token: " + result.getToken(), e);
				}
			}
		} catch (Exception e) {
			// Handle exception in case the Jedis resource cannot be fetched
			Log.error("Error fetching Jedis resource from pool", e);
		}

		return response;
	}

	/**
	 * Method to add the New Scrips in Market Watch New
	 * 
	 * @author Gowrisankar
	 * @param newScripDetails
	 * @param pUserId
	 * @param userMwId
	 */
	public List<CacheMwDetailsModel> addNewScipsForMwIntoCache(List<CacheMwDetailsModel> newScripDetails,
			String pUserId, int userMwId) {
		List<CacheMwDetailsModel> responseModel = new ArrayList<>();
		responseModel.addAll(newScripDetails);
		List<JSONObject> res = MwCacheController.getMwListUserId().get(pUserId);
		String marketWatchId = String.valueOf(userMwId);
		JSONObject result = null;
		int indexOfRes = 0;
		if (res != null && res.size() > 0) {
			for (int itr = 0; itr < res.size(); itr++) {
				result = new JSONObject();
				result = res.get(itr);
				String mwId = (String) result.get("mwId").toString();
				if (marketWatchId.equalsIgnoreCase(mwId)) {
					indexOfRes = itr;
					break;
				}
			}
			if (result != null && !result.isEmpty()) {
				List<CacheMwDetailsModel> scripDetails = (List<CacheMwDetailsModel>) result.get("scrips");
				List<CacheMwDetailsModel> latestScripDetails = new ArrayList<>();
				if (scripDetails != null && scripDetails.size() > 0) {
					latestScripDetails.addAll(scripDetails);
					for (int i = 0; i < newScripDetails.size(); i++) {
						CacheMwDetailsModel tempNewScrip = newScripDetails.get(i);
						String tempNewToken = tempNewScrip.getToken();
						String tempNewExch = tempNewScrip.getExchange();
						int alreadyAdded = 0;
						for (int j = 0; j < scripDetails.size(); j++) {
							CacheMwDetailsModel scrip = scripDetails.get(j);
							String token = scrip.getToken();
							String exch = scrip.getExchange();
							if (tempNewToken.equalsIgnoreCase(token) && tempNewExch.equalsIgnoreCase(exch)) {
								alreadyAdded = 1;
								break;
							}
						}
						if (alreadyAdded == 0) {
							latestScripDetails.add(tempNewScrip);
						} else {
							// If already exist remove it from list to avoid duplicate insert on DB
							responseModel.remove(i);
						}
					}
				} else {
					latestScripDetails.addAll(newScripDetails);
				}
				result.remove("scrips");
				result.put("scrips", latestScripDetails);
				res.remove(indexOfRes);
				res.add(indexOfRes, result);
				MwCacheController.getMwListUserId().remove(pUserId);
				MwCacheController.getMwListUserId().put(pUserId, res);
			}
		}
		return responseModel;
	}

	/**
	 * Method to insert into data base in thread
	 * 
	 * @author Dinesh Kumar
	 * @param parmDto
	 */
	private void insertNewScipsForMwIntoDataBase(List<CacheMwDetailsModel> scripDetails, String userId, int mwId) {
		ExecutorService pool = Executors.newSingleThreadExecutor();
		pool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					List<MarketWatchScripDetailsDTO> marketWatchNameDto = prepareMarketWatchEntity(scripDetails, userId,
							mwId);

					/*
					 * Insert the scrip details into the data base
					 */
					if (marketWatchNameDto != null && marketWatchNameDto.size() > 0) {
						marketWatchDAO.insertMwData(marketWatchNameDto);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					pool.shutdown();
				}
			}
		});
	}

	private List<MarketWatchScripDetailsDTO> prepareMarketWatchEntity(List<CacheMwDetailsModel> scripDetails,
			String userId, int mwId) {
		List<MarketWatchScripDetailsDTO> marketWatchScripDetailsDTOs = new ArrayList<>();

// Loop through all scrip details
		for (CacheMwDetailsModel model : scripDetails) {
			MarketWatchScripDetailsDTO resultDto = new MarketWatchScripDetailsDTO();
			String token = model.getToken();

// Determine the exchange format
			String codifiExchange;
			try {
				if (properties.isExchfull()) {
					String exchangeSegment = commonUtils
							.getExchangeSegmentNameIIFL(model.getExchange().trim().toUpperCase());
					codifiExchange = commonUtils.getExchangeName(exchangeSegment.toUpperCase().trim());
				} else {
					codifiExchange = model.getExchange().trim().toUpperCase();
				}
			} catch (Exception e) {
				Log.error(
						"Error determining exchange format for token: " + token + ", exchange: " + model.getExchange(),
						e);
				continue; // Skip this scrip if exchange parsing fails
			}

// Prepare cache key to fetch data from Redis
			String cacheKey = codifiExchange + "_" + token;

// Use Jedis in a try-with-resources block to ensure it is properly closed
			try (Jedis jedis = RedisConfig.getInstance().getJedisPool().getResource()) {
// Check if the contract data exists in Redis
				if (jedis.hexists(RedisConstants.CONTRACTMASTER, cacheKey)) {
					String json = jedis.hget(RedisConstants.CONTRACTMASTER, cacheKey);
					ObjectMapper objectMapper = new ObjectMapper();
					ContractMasterModel masterData = objectMapper.readValue(json, ContractMasterModel.class);

					if (masterData != null) {
// Map contract master data to DTO
						resultDto.setUserId(userId);
						resultDto.setMwId(mwId);
						resultDto.setEx(codifiExchange);
						resultDto.setToken(masterData.getToken());
						resultDto.setTradingSymbol(masterData.getTradingSymbol());
						resultDto.setEx(masterData.getExch());
						resultDto.setExSeg(masterData.getSegment());
						resultDto.setSymbol(masterData.getSymbol());
						resultDto.setGroupName(masterData.getGroupName());
						resultDto.setInstrumentType(masterData.getInsType());
						resultDto.setOptionType(masterData.getOptionType());
						resultDto.setStrikePrice(masterData.getStrikePrice());
						resultDto.setExpDt(masterData.getExpiry());
						resultDto.setLotSize(masterData.getLotSize());
						resultDto.setTickSize(masterData.getTickSize());
						resultDto.setFormattedName(masterData.getFormattedInsName());
						resultDto.setPdc(masterData.getPdc());
						resultDto.setAlterToken(masterData.getAlterToken());
						resultDto.setWeekTag(masterData.getWeekTag());
						resultDto.setSortingOrder(model.getSortOrder());

// Add the DTO to the result list
						marketWatchScripDetailsDTOs.add(resultDto);
					}
				} else {
					Log.info("Contract data not found in Redis for token: " + token + " with exchange: "
							+ codifiExchange);
				}
			} catch (Exception e) {
// Log and print any errors that occur during the Redis operation
				Log.error(
						"Error preparing MarketWatch entity for token: " + token + " with exchange: " + codifiExchange,
						e);
			}
		}

		return marketWatchScripDetailsDTOs;
	}

	/**
	 * Method to delete the scrips from the cache and market watch
	 * 
	 * @param pDto
	 * @return
	 */
	@SuppressWarnings("static-access")
	@Override
	public RestResponse<ResponseModel> deletescrip(MwRequestModel pDto, String userId) {
		try {
			int mwId = pDto.getMwId();
			String useriD = userId;
			List<MwScripModel> dataToDelete = pDto.getScripData();
			if (StringUtil.isNotNullOrEmpty(useriD) && StringUtil.isListNotNullOrEmpty(dataToDelete) && mwId > 0) {

				if (mwId != 301) {
					if (!commonUtils.isBetweenOneAndFive(pDto.getMwId())) {
						return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW101,
								AppConstants.INVALID_MW_ID);
					}

					for (int i = 0; i < pDto.getScripData().size(); i++) {
						String exchange = pDto.getScripData().get(i).getExchange();
						if (StringUtil.isNullOrEmpty(exchange)) {
							return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW117,
									ErrorMessageConstants.INVALID_EXCH);
						}
						if (exchange == null || exchange.trim().isEmpty()) {
							return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW117,
									ErrorMessageConstants.INVALID_EXCH);
						}
						// Check if exchange is null or invalid
						if (!commonUtils.isValidExch(exchange.toUpperCase().trim())) {
							return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW003,
									ErrorMessageConstants.INVALID_EXCHANGE);
						}
					}

					for (int i = 0; i < pDto.getScripData().size(); i++) {
						String token = pDto.getScripData().get(i).getToken();
						if (StringUtil.isNullOrEmpty(token)) {
							return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW118,
									ErrorMessageConstants.TOKEN_EMTY_OR_NULL);
						}
						if (token == null || token.trim().isEmpty()) {
							return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW117,
									ErrorMessageConstants.INVALID_TOKEN);
						}
						if (!commonUtils.checkThisIsTheNumber(token.trim())) {
							return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW107,
									AppConstants.INVALID_TOKEN);
						}
					}

					String checkDeleteId = marketWatchDAO.selectByUserId(pDto, userId);

					if (StringUtil.isNullOrEmpty(checkDeleteId)) {
						System.out.println(checkDeleteId);
						return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW110,
								AppConstants.ALREADY_DELETED_SCRIPT);
					}

					deleteFromCache(dataToDelete, useriD, mwId);
					deleteFromDB(dataToDelete, useriD, mwId);
					return prepareResponse.prepareMWSuccessResponseString(checkDeleteId + " Deleted Successfully");
				} else {
					return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW122,
							AppConstants.RECENTLY_SEARCH_DELETED);
				}
			} else {
				return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW104,
						AppConstants.INVALID_PARAMETER);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		}
		return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW105, AppConstants.FAILED_STATUS);

	}

	/**
	 * Method to delete the scrips from the cache
	 * 
	 * @author Gowrisankar
	 * @param newScripDetails
	 * @param pUserId
	 * @param userMwId
	 */

	public void deleteFromCache(List<MwScripModel> dataToDelete, String pUserId, int userMwId) {
		if (dataToDelete != null && !dataToDelete.isEmpty()) {
			List<JSONObject> res = MwCacheController.getMwListUserId().get(pUserId);
			String marketWatchId = String.valueOf(userMwId);
			JSONObject result = null;
			int indexOfRes = -1;

			if (res != null && !res.isEmpty()) {
				for (int itr = 0; itr < res.size(); itr++) {
					result = res.get(itr);
					String mwId = result.get("mwId").toString();
					if (marketWatchId.equalsIgnoreCase(mwId)) {
						indexOfRes = itr;
						break;
					}
				}

				if (result != null && !result.isEmpty()) {
					List<CacheMwDetailsModel> scripDetails = (List<CacheMwDetailsModel>) result.get("scrips");
					if (scripDetails != null && !scripDetails.isEmpty()) {
						// Step 1: Remove the specified scrip(s)
						for (MwScripModel tempDTO : dataToDelete) {
							String token = tempDTO.getToken().toUpperCase().trim();
							String exch = tempDTO.getExchange().toUpperCase().trim();
							scripDetails.removeIf(tempScripDTO -> tempScripDTO.getToken().equalsIgnoreCase(token)
									&& tempScripDTO.getExchange().equalsIgnoreCase(exch));
						}

						// Step 2: Recalculate and update the sorting order
						for (int i = 0; i < scripDetails.size(); i++) {
							scripDetails.get(i).setSortOrder(i + 1); // 1, 2, 3, 4, etc.
						}

						result.put("scrips", scripDetails);
						res.set(indexOfRes, result);
						MwCacheController.getMwListUserId().put(pUserId, res);
					}
				}
			}
		}
	}

	/**
	 * Method to delete the scrips from the cache
	 * 
	 * @author Gowrisankar
	 * @param newScripDetails
	 * @param pUserId
	 * @param userMwId
	 */

	public void deleteFromDB(List<MwScripModel> dataToDelete, String pUserId, int userMwId) {
		ExecutorService pool = Executors.newSingleThreadExecutor();
		pool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					if (dataToDelete != null && !dataToDelete.isEmpty()) {
						for (MwScripModel tempDTO : dataToDelete) {
							String token = tempDTO.getToken().toUpperCase().trim();
							String exch = tempDTO.getExchange().toUpperCase().trim();
							marketWatchDAO.deleteScripFomDataBase(pUserId, exch, token, userMwId);
						}
					}

					// Step 1: Retrieve remaining scrips after deletion
					List<MarketWatchScripDetailsDTO> mwList = marketWatchDAO.findAllByUserIdAndMwId(pUserId, userMwId);

					// Step 2: Recalculate the sorting order
					for (int i = 0; i < mwList.size(); i++) {
						mwList.get(i).setSortingOrder(i + 1); // Update sort order to 1, 2, 3, 4, ...
					}

					// Step 3: Update the database with new sort order
					if (!mwList.isEmpty()) {
						marketWatchDAO.updateMWScrips(mwList, pUserId, userMwId);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					pool.shutdown();
				}
			}
		});
	}

	@Override
	public RestResponse<ResponseModel> getAllMwScrips(String pUserId) {
		try {
			/*
			 * Check the user has the scrips in cache or not
			 */
			List<JSONObject> result = MwCacheController.getMwListUserId().get(pUserId);
			if (result != null && result.size() > 0) {
				/*
				 * if cache is there return from then return from cache
				 */
				System.out.println("getAllMwScrips - result from cache-" + pUserId);
				return prepareResponse.prepareSuccessResponseObject(result);
			} else {
				/*
				 * take the scrip details from the Data base for the user
				 */
//				List<IMwTblResponse> scripDetails = mwNameRepo.getUserScripDetails(pUserId);
				System.out.println("getAllMwScrips - getting result from DB-" + pUserId);
				List<CacheMwDetailsModel> scripDetails = marketWatchDAO.getMarketWatchByUserId(pUserId);
				if (scripDetails != null && scripDetails.size() > 0) {
					/*
					 * Populate the filed for Marketwatch as per the requirement
					 */
					System.out.println("getAllMwScrips - result from DB-" + pUserId);
					List<JSONObject> tempResult = populateFields(scripDetails, pUserId);
					if (tempResult != null && tempResult.size() > 0) {
						return prepareResponse.prepareSuccessResponseObject(tempResult);

					}
				} else {
					System.out.println("getAllMwScrips - Failed to get data from DB and create new-" + pUserId);
					/**
					 * Create New market watch if does not exist
					 */
					List<JSONObject> resp = create(pUserId);
					if (resp != null && resp.size() > 0) {
						return prepareResponse.prepareSuccessResponseObject(resp);
					} else {
						return prepareResponse.prepareFailedResponse(AppConstants.NO_MW);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		}
		return prepareResponse.prepareFailedResponse(AppConstants.FAILED_STATUS);
	}

	/**
	 * @param pUserId
	 * @return
	 */
	private List<JSONObject> create(String pUserId) {
		System.out.println("Reached to create mw for user - " + pUserId);
		List<JSONObject> response = new ArrayList<>();
		try {
			if (StringUtil.isNotNullOrEmpty(pUserId)) {
				/* Check user has how many market watch */
				List<MarketWatchNameDTO> mwList = marketWatchDAO.findAllByUserId(pUserId);
				System.out.println("create mw for user - " + pUserId + "and count - " + mwList.size());
				/* If null or size is lesser than 5 create a new Market Watch */
				if (mwList == null || mwList.size() == 0) {
					List<MarketWatchNameDTO> newMwList = new ArrayList<>();
					System.out.println("create new mw for user - " + pUserId);
					/* Create the new Market Watch */
					int mwListSize = Integer.parseInt(properties.getMwSize());
					int mwId = 300;
					for (int i = 0; i < mwListSize; i++) {
						mwId = mwId + 1;
						System.out.println("<<<<mwId>>>>>" + mwId);
						MarketWatchNameDTO newDto = new MarketWatchNameDTO();
						newDto.setUserId(pUserId);
						newDto.setMwId(mwId);
						if (mwId == 301) {
							newDto.setMwName(AppConstants.RECENTLY_SEARCHED_MW_NAME);
						} else {
							newDto.setMwName(AppConstants.MARKET_WATCH_LIST_NAME + (i + 1));
						}
						newDto.setPosition(Long.valueOf(i));
						newMwList.add(newDto);
					}
					marketWatchDAO.insertMwName(newMwList);
//					List<IMwTblResponse> scripDetails = mwNameRepo.getUserScripDetails(pUserId);
					List<CacheMwDetailsModel> scripDetails = marketWatchDAO.getMarketWatchByUserId(pUserId);
					if (scripDetails != null && scripDetails.size() > 0) {
						response = populateFields(scripDetails, pUserId);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
		return response;
	}

	/**
	 * Method to getIndices
	 * 
	 * @author Vicky
	 * 
	 */
	@Override
	public JSONObject getIndices() {
		JSONObject json = new JSONObject();
		List<IndexModel> response = new ArrayList<>();

		if (MwCacheController.getGetIndexData().get(AppConstants.GET_INDEX) != null) {
			response = MwCacheController.getGetIndexData().get(AppConstants.GET_INDEX);
			System.out.println("Cache Data");
			json.put("status", "Ok");
			json.put("message", "Success");
			json.put("Indices", response);
		} else {
			List<IndexModel> result = marketWatchDAO.getIndicesList();
			MwCacheController.getGetIndexData().put(AppConstants.GET_INDEX, result);
			if (result.size() > 0) {
				json.put("status", "Ok");
				json.put("message", "Success");
				json.put("Indices", result);
			}
		}

		return json;
	}

	/**
	 * Method to getCommodity Contarct
	 * 
	 */
	@Override
	public RestResponse<ResponseModel> getcommodityContarct(MwCommodityContarctModel pDto) {
		MwCommodityContarctModel response = new MwCommodityContarctModel();

		/*
		 * TODO CommodityContarct Set to ContractMaster data
		 */
		response.setPriceRange("55440.00 - 60060.00");
		response.setPriceUnit("CANDY");
		response.setQtyUnit("1 CANDY");
		response.setDeliveryUnit("CANDY");
		response.setTickSize("1000.0");
		response.setLotSize("48.0");
		response.setMaxOrderValue("0");
		response.setContractStartDate("Apr 17 2024 12:00AM");
		response.setTenderStartDate("Sep 24 2024 12:00AM");
		response.setTenderEndDate("Sep 30 2024 11:59PM");
		response.setDelievryStartDate("Sep 24 2024 11:59PM");
		response.setDelievryEndDate("Oct 3 2024 11:59PM");
		response.setLastTradingDate("Sep 30 2024 4:59PM");

		return prepareResponse.prepareSuccessResponseObject(response);
	}

	/**
	 * method to get info details
	 * 
	 */
	@SuppressWarnings("static-access")
	@Override
	public RestResponse<ResponseModel> getSecurityInfo(SecurityInfoReqModel model, ClinetInfoModel info) {
		try (Jedis jedis = RedisConfig.getInstance().getJedisPool().getResource()) {

			// ** Validate Request **
			if (StringUtil.isNullOrEmpty(model.getToken())) {
				return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW121,
						ErrorMessageConstants.TOKEN_EMPTY);
			}
			if (StringUtil.isNullOrEmpty(model.getExchange())) {
				return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW003,
						ErrorMessageConstants.INVALID_EXCH);
			}
			if (!validateSecurityInfoParameters(model)) {
				return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW104,
						AppConstants.INVALID_PARAMETER);
			}

			if (StringUtil.isNotNullOrEmpty(model.getSegment())
					&& !commonUtils.isValidExchSegment(model.getSegment().trim())) {
				return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW003,
						ErrorMessageConstants.INVALID_EXCHANGE_SEGMENT);
			}
			if (!commonUtils.checkThisIsTheNumber(model.getToken().trim())) {
				return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW107, AppConstants.INVALID_TOKEN);
			}

			// Determine the exchange format
			String codifiExchange = getCodifiExchange(model);

			// Fetch data from Redis cache
			String cacheKey = codifiExchange + "_" + model.getToken().trim();
			if (jedis.hexists(RedisConstants.CONTRACTMASTER, cacheKey)) {
				String json = jedis.hget(RedisConstants.CONTRACTMASTER, cacheKey);
				ObjectMapper objectMapper = new ObjectMapper();
				ContractMasterModel masterData = objectMapper.readValue(json, ContractMasterModel.class);

				if (masterData != null) {
					SecurityInfoRespModel infoResult = buildSecurityInfoResp(masterData, codifiExchange);
					return prepareResponse.prepareSuccessResponseWithMessage(infoResult, AppConstants.SUCCESS_STATUS,
							false);
				}
			}
			return prepareResponse.prepareMWFailedwithEmtyResult(ErrorCodeConstants.ECMW111,
					ErrorMessageConstants.NOT_FOUND, new JSONObject());

		} catch (Exception e) {
			Log.error(e.getMessage());
			e.printStackTrace();
		}

		return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW105, AppConstants.FAILED_STATUS);
	}

	// Helper Method to get the exchange format
	private String getCodifiExchange(SecurityInfoReqModel model) {
		String codifiExchange = "";
		if (properties.isExchfull()) {
			String exchangeSegment = commonUtils.getExchangeSegmentNameIIFL(model.getExchange().trim().toUpperCase());
			codifiExchange = commonUtils.getExchangeName(exchangeSegment.toUpperCase().trim());
		} else {
			codifiExchange = model.getExchange().trim().toUpperCase();
		}
		return codifiExchange;
	}

	// Helper Method to build the SecurityInfoRespModel
	private SecurityInfoRespModel buildSecurityInfoResp(ContractMasterModel masterData, String codifiExchange) {
		SecurityInfoRespModel infoResult = new SecurityInfoRespModel();

		String exchangeIifl = commonUtils.getExchangeNameIIFL(masterData.getExch());
		infoResult.setIsin(StringUtil.isNullOrEmpty(codifiExchange) ? "" : masterData.getIsin());
		infoResult.setExchange(properties.isExchfull() ? exchangeIifl : masterData.getExch());
		infoResult.setToken(masterData.getToken());
		infoResult.setTradingSymbol(masterData.getTradingSymbol());
		infoResult.setLotSize(masterData.getLotSize());
		infoResult.setTickSize(masterData.getTickSize());
		infoResult.setSymbol(masterData.getSymbol());
		infoResult.setPdc(masterData.getPdc());
		infoResult.setInsType(masterData.getInsType());
		infoResult.setExpiry(masterData.getExpiry() == null ? "" : masterData.getExpiry());
		infoResult.setQtyLimit("");
		infoResult.setSliceEnable("");
		infoResult.setSurveillance("");
		infoResult.setScripIndex(false);
		infoResult.setFnOAvailable("1".equals(masterData.getOptionType()));
		infoResult.setCompanyName(masterData.getCompanyName());

		SpotData spotDataResult = new SpotData();
		spotDataResult.setLtp(1);
		spotDataResult.setToken(masterData.getToken());
		spotDataResult.setTradingSymbol(masterData.getTradingSymbol());
		spotDataResult.setNsebseToken(masterData.getAlterToken());

//		Prompt promptResult = new Prompt();
//		JSONArray promptObj = new JSONArray();
//		promptObj.add(promptResult);

		ProductLeverage productLeverageResult = new ProductLeverage();
		productLeverageResult.setDelivery("");
		productLeverageResult.setIntraday("");
		productLeverageResult.setBnpl("");

		Badge badge = new Badge();
		badge.setEvent(false);
		badge.setBnpl("");
		badge.setIdeas("");
		badge.setHoldingqty(0);

		infoResult.setSpotData(spotDataResult);
//		infoResult.setPrompt(promptObj);
		infoResult.setProductLeverage(productLeverageResult);
		infoResult.setBadge(badge);
		infoResult.setScreeners(new JSONArray());

		return infoResult;
	}

	/**
	 * @param model
	 * @return
	 */
	private boolean validateSecurityInfoParameters(SecurityInfoReqModel model) {
		if (StringUtil.isNotNullOrEmpty(model.getExchange()) && StringUtil.isNotNullOrEmpty(model.getToken())) {
			return true;
		}
		return false;
	}

	/**
	 * Method to save the scrips into the recently viewed Database
	 * 
	 * @author Gowrisankar
	 * @return
	 */
	private boolean saveRecentlyViewedScrips(String pExchange, String pToken, String pUserId, Date expiryDate) {
		boolean isSaved = false;

		try (Jedis jedis = RedisConfig.getInstance().getJedisPool().getResource()) {
			String cacheKey = pExchange + "_" + pToken;
			String json = jedis.hget("contractMaster", cacheKey);
			ContractMasterModel contractMasterModel = null;

			if (json != null) {
				ObjectMapper objectMapper = new ObjectMapper();
				contractMasterModel = objectMapper.readValue(json, ContractMasterModel.class);
			}

			if (ObjectUtils.isNotEmpty(contractMasterModel)) {
				// Fetch recently viewed data from the database
				List<RecentlyViewedEntity> recentlyViewedData = recentlyViewedRepository
						.findAllByUserIdOrderBySortOrderAsc(pUserId);

				List<RecentlyViewedEntity> savingList = new ArrayList<>();
				if (recentlyViewedData != null && !recentlyViewedData.isEmpty()) {
					RecentlyViewedEntity tempRecentlyViewed = new RecentlyViewedEntity();
					tempRecentlyViewed.setUserId(pUserId);
					tempRecentlyViewed.setExch(pExchange);
					tempRecentlyViewed.setToken(pToken);
					tempRecentlyViewed.setSortOrder(0);
					if (expiryDate != null) {
						tempRecentlyViewed.setExpiryDate(expiryDate);
					}

					// Add the new entry to the saving list
					savingList.add(tempRecentlyViewed);

					// Check the size of the recently viewed list
					int dataSize = recentlyViewedData.size();
					if (dataSize >= 25) {
						recentlyViewedData.remove(dataSize - 1); // Remove the oldest entry if size exceeds 25
						dataSize = dataSize - 1;
					}

					int count = 1;
					for (int itr = 0; itr < dataSize; itr++) {
						RecentlyViewedEntity result = recentlyViewedData.get(itr);
						String tempExch = result.getExch();
						String tempToken = result.getToken();

						if (!tempExch.equalsIgnoreCase(pExchange) || !tempToken.equalsIgnoreCase(pToken)) {
							// Update the list with existing entries
							RecentlyViewedEntity recentlyViewed = new RecentlyViewedEntity();
							recentlyViewed.setUserId(pUserId);
							recentlyViewed.setExch(tempExch);
							recentlyViewed.setToken(tempToken);
							recentlyViewed.setSortOrder(count++);
							recentlyViewed.setCreatedOn(result.getCreatedOn());
							recentlyViewed.setUpdatedOn(result.getUpdatedOn());
							if (expiryDate != null) {
								recentlyViewed.setExpiryDate(expiryDate);
							}
							savingList.add(recentlyViewed);
						}
					}

					// Save the updated recently viewed list to the database
					if (!savingList.isEmpty()) {
						recentlyViewedRepository.deleteAllByUserId(pUserId); // Delete old data
						recentlyViewedRepository.saveAll(savingList); // Save new data
						isSaved = true;
					}
				} else {
					// If no recently viewed data exists, add a new entry
					RecentlyViewedEntity tempRecentlyViewed = new RecentlyViewedEntity();
					tempRecentlyViewed.setUserId(pUserId);
					tempRecentlyViewed.setExch(pExchange);
					tempRecentlyViewed.setToken(pToken);
					tempRecentlyViewed.setSortOrder(0);
					if (expiryDate != null) {
						tempRecentlyViewed.setExpiryDate(expiryDate);
					}
					recentlyViewedRepository.save(tempRecentlyViewed);
					recentlyViewedRepository.flush();
					isSaved = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return isSaved;
	}

	/**
	 * 
	 */
	@Override
	public RestResponse<ResponseModel> getContractInfo(GetContractInfoReqModel model, ClinetInfoModel info) {
		try (Jedis jedis = RedisConfig.getInstance().getJedisPool().getResource()) {
			ContractInfoRespModel response = new ContractInfoRespModel();
			List<ContractInfoDetails> detailsList = new ArrayList<>();

			if (model != null && StringUtil.isNotNullOrEmpty(model.getToken())
					&& StringUtil.isNotNullOrEmpty(model.getExch())) {
				String token = model.getToken();
				String exch = model.getExch().toUpperCase();
				String cacheKey = exch + "_" + token;

				// Fetching data from Redis
				String json = jedis.hget("contractMaster", cacheKey);

				if (json != null) {
					ObjectMapper objectMapper = new ObjectMapper();
					ContractMasterModel contractMasterModel = objectMapper.readValue(json, ContractMasterModel.class);

					if (ObjectUtils.isNotEmpty(contractMasterModel)) {
						ContractInfoDetails details = prepareContractInfoResp(contractMasterModel);
						detailsList.add(details);

						/** To add alter token details **/
						if (contractMasterModel != null
								&& (exch.equalsIgnoreCase("NSE") || exch.equalsIgnoreCase("BSE"))
								&& StringUtil.isNotNullOrEmpty(contractMasterModel.getAlterToken())) {

							String altExch = exch.equalsIgnoreCase("BSE") ? "NSE" : "BSE";
							String altCacheKey = altExch + "_" + contractMasterModel.getAlterToken();
							String altJson = jedis.hget("contractMaster", altCacheKey);

							if (altJson != null) {
								ContractMasterModel alterContractMasterModel = objectMapper.readValue(altJson,
										ContractMasterModel.class);
								ContractInfoDetails altDetails = prepareContractInfoResp(alterContractMasterModel);
								detailsList.add(altDetails);
							}
						}

						// Set the response details
						response.setFreezeQty(contractMasterModel.getFreezQty());
						response.setIsin(contractMasterModel.getIsin());
						response.setScrips(detailsList);

						// Save the recently viewed scrips for the user
						String userId = info.getUserId();
						if (StringUtil.isNotNullOrEmpty(userId)) {
							saveRecentlyViewedScrips(exch, model.getToken(), userId, contractMasterModel.getExpiry());
						}

						return prepareResponse.prepareSuccessResponseObject(response);
					} else {
						return prepareResponse.prepareFailedResponse(AppConstants.TOKEN_NOT_EXISTS);
					}
				} else {
					return prepareResponse.prepareFailedResponse(AppConstants.TOKEN_NOT_EXISTS);
				}
			} else {
				return prepareResponse.prepareFailedResponse(AppConstants.INVALID_PARAMETER);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		}

		return prepareResponse.prepareFailedResponse(AppConstants.FAILED_STATUS);
	}

	/**
	 * 
	 * @param model
	 * @return
	 */
	private ContractInfoDetails prepareContractInfoResp(ContractMasterModel model) {
		ContractInfoDetails details = new ContractInfoDetails();

		// To add prompt message
		if (model != null && (model.getExch().equalsIgnoreCase("NSE") || model.getExch().equalsIgnoreCase("BSE"))) {
			JedisPool jedisPool = RedisConfig.getInstance().getJedisPool();

			// Using try-with-resources to manage Jedis resource
			try (Jedis jedis = jedisPool.getResource()) {
				String promptKey = model.getIsin() + "_" + model.getExch();
				String cachedPromptData = jedis.get(promptKey); // Use jedis directly to get the cached data

				if (cachedPromptData != null) {
					// Deserialize the prompt data
					List<PromptModel> prompt = new ObjectMapper().readValue(cachedPromptData,
							new TypeReference<List<PromptModel>>() {
							});
					if (prompt != null && !prompt.isEmpty()) {
						details.setPrompt(prompt);
					}
				}
			} catch (Exception e) {
				e.printStackTrace(); // Handle exceptions related to Redis or JSON deserialization
			}
		}

		// Set contract details in the response
		details.setExchange(model.getExch());
		details.setLotSize(model.getLotSize());
		details.setTickSize(model.getTickSize());
		details.setToken(model.getToken());
		details.setTradingSymbol(model.getTradingSymbol());
		details.setSymbol(model.getSymbol());
		details.setFormattedInsName(model.getFormattedInsName());
		details.setPdc(model.getPdc());
		details.setInsType(model.getInsType());
		details.setExpiry(model.getExpiry());

		return details;
	}

	/***
	 * method to Get all Mw List Predefined and Holdings , Positions Added
	 * 
	 * @author Vicky
	 * 
	 */
	@Override
	public RestResponse<ResponseModel> getAllMwScripsMob(String pUserId, boolean predefined) {
		try {
			/*
			 * Check the user has the scrips in cache or not
			 */
			List<JSONObject> result = MwCacheController.getMwListUserId().get(pUserId);
			if (result != null && result.size() > 0) {
				/*
				 * if cache is there return from then return from cache
				 */
				System.out.println("getAllMwScripsMob - result from cache-" + pUserId);
				if (predefined == true) {
					List<JSONObject> predefinedMW = preparePredefinedMw(predefined, pUserId);
					List<JSONObject> combinedList = Stream.concat(predefinedMW.stream(), result.stream())
							.collect(Collectors.toList());

					List<JSONObject> holdingsMW = preparHoldingMw(pUserId);
					List<JSONObject> holdingsResult = Stream.concat(combinedList.stream(), holdingsMW.stream())
							.collect(Collectors.toList());

//					List<JSONObject> postionMW = preparPostionMw(pUserId);
//					List<JSONObject> postionsResult = Stream.concat(holdingsResult.stream(), postionMW.stream())
//							.collect(Collectors.toList());

					return prepareResponse.prepareSuccessResponseObject(holdingsResult);

				}
				return prepareResponse.prepareSuccessResponseObject(result);
			} else {
				/*
				 * take the scrip details from the Data base for the user
				 */
//				List<IMwTblResponse> scripDetails = mwNameRepo.getUserScripDetails(pUserId);
				System.out.println("getAllMwScripsMob - getting result from DB-" + pUserId);
				List<CacheMwDetailsModel> scripDetails = marketWatchDAO.getMarketWatchByUserId(pUserId);

				if (scripDetails != null && scripDetails.size() > 0) {
					/*
					 * Populate the filed for Marketwatch as per the requirement
					 */
					System.out.println("getAllMwScripsMob - result from DB-" + pUserId);
					List<JSONObject> tempResult = populateFields(scripDetails, pUserId);
					if (tempResult != null && !tempResult.isEmpty()) {
						if (predefined == true) {
							List<JSONObject> predefinedMW = preparePredefinedMw(predefined, pUserId);
							List<JSONObject> combinedList = Stream.concat(predefinedMW.stream(), tempResult.stream())
									.collect(Collectors.toList());

							List<JSONObject> holdingsMW = preparHoldingMw(pUserId);
							List<JSONObject> holdingsResult = Stream.concat(combinedList.stream(), holdingsMW.stream())
									.collect(Collectors.toList());

//							List<JSONObject> postionMW = preparPostionMw(pUserId);
//							List<JSONObject> postionsResult = Stream.concat(holdingsResult.stream(), postionMW.stream())
//									.collect(Collectors.toList());

							return prepareResponse.prepareSuccessResponseObject(holdingsResult);

						}
						return prepareResponse.prepareSuccessResponseObject(tempResult);
					}
				} else {

					/**
					 * Create New market watch if does not exist
					 */
					System.out.println("getAllMwScripsMob - Failed to get data from DB and create new-" + pUserId);
					List<JSONObject> resp = create(pUserId);
					if (predefined == true) {
						List<JSONObject> predefinedMW = preparePredefinedMw(predefined, pUserId);
						List<JSONObject> combinedList = Stream.concat(predefinedMW.stream(), resp.stream())
								.collect(Collectors.toList());

						List<JSONObject> holdingsMW = preparHoldingMw(pUserId);
						List<JSONObject> holdingsResult = Stream.concat(combinedList.stream(), holdingsMW.stream())
								.collect(Collectors.toList());

//						List<JSONObject> postionMW = preparPostionMw(pUserId);
//						List<JSONObject> postionsResult = Stream.concat(holdingsResult.stream(), postionMW.stream())
//								.collect(Collectors.toList());

						return prepareResponse.prepareSuccessResponseObject(holdingsResult);

					}
					if (resp != null && !resp.isEmpty()) {
						return prepareResponse.prepareSuccessResponseObject(resp);
					} else {
						return prepareResponse.prepareFailedResponse(AppConstants.NO_MW);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		}
		return prepareResponse.prepareFailedResponse(AppConstants.FAILED_STATUS);
	}

	/***
	 * 
	 * @param predefined
	 * @param userId
	 * @return
	 */
	private List<JSONObject> preparePredefinedMw(boolean predefined, String userId) {
		List<JSONObject> predefinedMW = new ArrayList<>();
		try {
			List<PredefinedMwEntity> predefinedMwEntities = new ArrayList<>();
			List<PredefinedMwEntity> userPredefinedMwEntities = new ArrayList<>();

			/** Get predefined mw list from cache or DB **/
			if (MwCacheController.getMasterPredefinedMwList().get(AppConstants.PREDEFINED_MW) != null) {
				predefinedMwEntities = MwCacheController.getMasterPredefinedMwList().get(AppConstants.PREDEFINED_MW);
			} else {
				predefinedMwEntities = predefinedMwRepo.findAll();
			}

			userPredefinedMwEntities.addAll(predefinedMwEntities);

			if (StringUtil.isListNotNullOrEmpty(userPredefinedMwEntities)) {
				List<PredefinedMwEntity> sortedLists = userPredefinedMwEntities.stream()
						.sorted(Comparator.comparing(PredefinedMwEntity::getPosition)).collect(Collectors.toList());
				predefinedMW = preparePredefinedMWList(sortedLists);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		}
		return predefinedMW;
	}

	/***
	 * 
	 * @param predefinedMw
	 * @return
	 */
	private List<JSONObject> preparePredefinedMWList(List<PredefinedMwEntity> predefinedMw) {
		List<JSONObject> predefinedMW = new ArrayList<>();
		for (PredefinedMwEntity preMW : predefinedMw) {
			List<JSONObject> predefinedMWScrips = new ArrayList<>();
			JSONObject jObj1 = new JSONObject();
			jObj1.put("mwId", preMW.getMwId());
			jObj1.put("mwName", preMW.getMwName());
			// jObj1.put("position", preMW.getPosition()); // Uncomment if needed
			jObj1.put("isRename", false);
			jObj1.put("isDefault", false);
			jObj1.put("isEdit", false);

			for (PredefinedMwScripsEntity scrips : preMW.getScrips()) {
				ContractMasterModel masterData = null;
				String cacheKey = scrips.getExchange() + "_" + scrips.getToken();
				JedisPool jedisPool = RedisConfig.getInstance().getJedisPool();

				// Using try-with-resources to manage Jedis resource
				try (Jedis jedis = jedisPool.getResource()) {
					if (jedis.hexists(RedisConstants.CONTRACTMASTER, cacheKey)) {
						String json = jedis.hget(RedisConstants.CONTRACTMASTER, cacheKey);
						ObjectMapper objectMapper = new ObjectMapper();
						masterData = objectMapper.readValue(json, ContractMasterModel.class);
					}
				} catch (Exception e) {
					e.printStackTrace(); // Handle Redis connection or JSON deserialization exceptions
				}

				if (masterData != null) {
					JSONObject obj = new JSONObject();
					if (properties.isExchfull()) {
						String exchangeIifl = commonUtils.getExchangeNameIIFL(masterData.getExch());
						obj.put("exchange", exchangeIifl);
						String segmentIifl = commonUtils.getExchangeName(masterData.getSegment());
						obj.put("segment", segmentIifl);
					} else {
						obj.put("exchange", masterData.getExch());
						obj.put("segment", masterData.getSegment());
					}

					obj.put("token", masterData.getToken());
					obj.put("tradingSymbol", masterData.getTradingSymbol());
					obj.put("formattedInsName", masterData.getFormattedInsName());
					obj.put("sortOrder", scrips.getSortOrder());
					obj.put("pdc", masterData.getPdc());
					obj.put("symbol", masterData.getSymbol());

					if (masterData.getExpiry() != null) {
						Date expiry = masterData.getExpiry();
						String expDate = new SimpleDateFormat("YYYY-MM-dd").format(expiry);
						obj.put("expiry", expDate);
					} else {
						obj.put("expiry", "");
					}

					obj.put("weekTag", masterData.getWeekTag() == null ? "" : masterData.getWeekTag());
					obj.put("screeners", new JSONArray()); // Add the empty screeners array
					predefinedMWScrips.add(obj);
				}
			}

			jObj1.put("scrips", predefinedMWScrips);
			predefinedMW.add(jObj1);
		}
		return predefinedMW;
	}

	/***
	 * 
	 * @param predefined
	 * @param userId
	 * @return
	 */

	private List<JSONObject> preparHoldingMw(String userId) {
		List<JSONObject> predefinedMW = new ArrayList<>();
		try {
			List<HoldingsDataMwEntity> predefinedMwEntities = new ArrayList<>();
			List<HoldingsDataMwEntity> userPredefinedMwEntities = new ArrayList<>();

			/** Get predefined mw list from cache or DB **/
			if (MwCacheController.getMasterHoldingsMwList().get(AppConstants.HOLDINGS_MW) != null) {
				predefinedMwEntities = MwCacheController.getMasterHoldingsMwList().get(AppConstants.HOLDINGS_MW);
			} else {
				predefinedMwEntities = holdingsMwRepo.findByUserId(userId);
			}

			userPredefinedMwEntities.addAll(predefinedMwEntities);

			predefinedMW = prepareHoldingMWList(userPredefinedMwEntities);

		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		}
		return predefinedMW;
	}

	/***
	 * 
	 * @param predefinedMw
	 * @return
	 */
	private List<JSONObject> prepareHoldingMWList(List<HoldingsDataMwEntity> predefinedMw) {
		List<JSONObject> predefinedMW = new ArrayList<>();
		List<JSONObject> predefinedMWScrips = new ArrayList<>();
		JSONObject jObj1 = new JSONObject();
		jObj1.put("mwId", AppConstants.HOLDING_MW_ID);
		jObj1.put("mwName", AppConstants.HOLDING_MW_NAME);
		// jObj1.put("position", 2); // Uncomment if needed
		jObj1.put("isRename", false);
		jObj1.put("isDefault", false);
		jObj1.put("isEdit", false);

		JedisPool jedisPool = RedisConfig.getInstance().getJedisPool();

		// Using try-with-resources to manage Jedis resource
		try (Jedis jedis = jedisPool.getResource()) {
			for (HoldingsDataMwEntity scrips : predefinedMw) {
				// Check if the ISIN exists in Redis
				if (jedis.hexists(RedisConstants.ISINBYTOKEN, scrips.getIsin())) {
					String exchToken = jedis.hget(RedisConstants.ISINBYTOKEN, scrips.getIsin());
					String[] result = exchToken.split("_");
					String exchange = result[0];
					String token = result[1];
					System.out.println(exchToken);

					// Fetch contract master data from Redis
					if (jedis.hexists(RedisConstants.CONTRACTMASTER, exchToken)) {
						String json = jedis.hget(RedisConstants.CONTRACTMASTER, exchToken);

						try {
							// Deserialize the JSON string into a ContractMasterModel object
							ObjectMapper objectMapper = new ObjectMapper();
							ContractMasterModel masterData = objectMapper.readValue(json, ContractMasterModel.class);

							JSONObject obj = new JSONObject();
							if (properties.isExchfull()) {
								String exchangeIifl = commonUtils.getExchangeNameIIFL(masterData.getExch());
								obj.put("exchange", exchangeIifl);
								String segmentIifl = commonUtils.getExchangeName(masterData.getSegment());
								obj.put("segment", segmentIifl);
							} else {
								obj.put("exchange", masterData.getExch());
								obj.put("segment", masterData.getSegment());
							}

							obj.put("token", masterData.getToken());
							obj.put("tradingSymbol", masterData.getTradingSymbol());
							obj.put("formattedInsName", masterData.getFormattedInsName());
							obj.put("sortOrder", "");
							obj.put("pdc", masterData.getPdc());
							obj.put("symbol", masterData.getSymbol());
							if (masterData.getExpiry() != null) {
								Date expiry = masterData.getExpiry();
								String expDate = new SimpleDateFormat("YYYY-MM-dd").format(expiry);
								obj.put("expiry", expDate);
							}
							obj.put("weekTag", masterData.getWeekTag() == null ? "" : masterData.getWeekTag());
							obj.put("qty", scrips.getQty());

							predefinedMWScrips.add(obj);
						} catch (Exception e) {
							e.printStackTrace(); // Handle JSON deserialization exception
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace(); // Handle Redis connection or operation issues
		}

		jObj1.put("scrips", predefinedMWScrips);
		predefinedMW.add(jObj1);
		return predefinedMW;
	}

	/***
	 * 
	 * @param predefined
	 * @param userId
	 * @return
	 */

	private List<JSONObject> preparPostionMw(String userId) {
		List<JSONObject> predefinedMW = new ArrayList<>();
		try {
			List<PositionDataMwEntity> predefinedPostionsMwEntities = new ArrayList<>();
			List<PositionDataMwEntity> userPredefinedPostionMwEntities = new ArrayList<>();

			/** Get predefined mw list from cache or DB **/
			if (MwCacheController.getMasterPostionMwList().get(AppConstants.POSITION_MW) != null) {
				predefinedPostionsMwEntities = MwCacheController.getMasterPostionMwList().get(AppConstants.POSITION_MW);
			} else {
				predefinedPostionsMwEntities = positionMwRepo.findByUserId(userId);
			}

			userPredefinedPostionMwEntities.addAll(predefinedPostionsMwEntities);

			predefinedMW = preparePostionMWList(userPredefinedPostionMwEntities);

		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		}
		return predefinedMW;
	}

	/***
	 * 
	 * @param predefinedMw
	 * @return
	 */
	private List<JSONObject> preparePostionMWList(List<PositionDataMwEntity> predefinedMw) {
		List<JSONObject> predefinedMW = new ArrayList<>();
		List<JSONObject> predefinedMWScrips = new ArrayList<>();
		JSONObject jObj1 = new JSONObject();
		jObj1.put("mwId", AppConstants.POSITION_MW_ID);
		jObj1.put("mwName", AppConstants.POSITION_MW_NAME);
		// jObj1.put("position", 2); // Uncomment if needed
		jObj1.put("isRename", false);
		jObj1.put("isDefault", false);
		jObj1.put("isEdit", false);

		for (PositionDataMwEntity scrips : predefinedMw) {

			String codifiExchange = "";
			if (properties.isExchfull()) {
				String exchangeSegment = commonUtils
						.getExchangeSegmentNameIIFL(scrips.getExchange().trim().toUpperCase());
				codifiExchange = commonUtils.getExchangeName(exchangeSegment.toUpperCase().trim());
			} else {
				codifiExchange = scrips.getExchange().trim().toUpperCase();
			}

			System.out.println("Postion >>>>>>>>" + codifiExchange + "_" + scrips.getToken());

			JedisPool jedisPool = RedisConfig.getInstance().getJedisPool();

			// Using Jedis in try-with-resources to manage resource properly
			try (Jedis jedis = jedisPool.getResource()) {
				if (jedis.hexists(RedisConstants.CONTRACTMASTER, codifiExchange + "_" + scrips.getToken())) {
					String json = jedis.hget(RedisConstants.CONTRACTMASTER, codifiExchange + "_" + scrips.getToken());

					try {
						// Deserialize the JSON string into a ContractMasterModel object
						ObjectMapper objectMapper = new ObjectMapper();
						ContractMasterModel masterData = objectMapper.readValue(json, ContractMasterModel.class);

						JSONObject obj = new JSONObject();

						if (properties.isExchfull()) {
							String exchangeIifl = commonUtils.getExchangeNameIIFL(masterData.getExch());
							obj.put("exchange", exchangeIifl);
							String segmentIifl = commonUtils.getExchangeName(masterData.getSegment());
							obj.put("segment", segmentIifl);
						} else {
							obj.put("exchange", masterData.getExch());
							obj.put("segment", masterData.getSegment());
						}

						obj.put("token", masterData.getToken());
						obj.put("tradingSymbol", masterData.getTradingSymbol());
						obj.put("formattedInsName", masterData.getFormattedInsName());
						obj.put("sortOrder", "");
						obj.put("pdc", masterData.getPdc());
						obj.put("symbol", masterData.getSymbol());
						if (masterData.getExpiry() != null) {
							Date expiry = masterData.getExpiry();
							String expDate = new SimpleDateFormat("YYYY-MM-dd").format(expiry);
							obj.put("expiry", expDate);
						}
						obj.put("weekTag", masterData.getWeekTag() == null ? "" : masterData.getWeekTag());

						predefinedMWScrips.add(obj);
					} catch (Exception e) {
						e.printStackTrace(); // Handle JSON deserialization exception
					}
				}

			} catch (Exception e) {
				e.printStackTrace(); // Handle Redis connection or operation issues
			}
		}

		jObj1.put("scrips", predefinedMWScrips);
		predefinedMW.add(jObj1);
		return predefinedMW;
	}

}
