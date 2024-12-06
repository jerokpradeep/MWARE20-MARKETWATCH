package in.codifi.mw.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.resteasy.reactive.RestResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import in.codifi.cache.model.ContractMasterModel;
import in.codifi.mw.cache.HazelCacheController;
import in.codifi.mw.cache.MwCacheController;
import in.codifi.mw.config.ApplicationProperties;
import in.codifi.mw.entity.MarketWatchNameDTO;
import in.codifi.mw.entity.MarketWatchScripDetailsDTO;
import in.codifi.mw.model.Badge;
import in.codifi.mw.model.CacheMwDetailsModel;
import in.codifi.mw.model.ClinetInfoModel;
import in.codifi.mw.model.MwCommodityContarctModel;
import in.codifi.mw.model.MwIndicesModel;
import in.codifi.mw.model.MwRequestModel;
import in.codifi.mw.model.MwScripModel;
import in.codifi.mw.model.ProductLeverage;
import in.codifi.mw.model.Prompt;
import in.codifi.mw.model.ResponseModel;
import in.codifi.mw.model.SecurityInfoReqModel;
import in.codifi.mw.model.SecurityInfoRespModel;
import in.codifi.mw.model.SpotData;
import in.codifi.mw.repository.MarketWatchDAO;
import in.codifi.mw.service.spec.IMarketWatchService;
import in.codifi.mw.util.AppConstants;
import in.codifi.mw.util.CommonUtils;
import in.codifi.mw.util.ErrorCodeConstants;
import in.codifi.mw.util.PrepareResponse;
import in.codifi.mw.util.StringUtil;
import io.quarkus.logging.Log;

@SuppressWarnings("unchecked")
@Service
public class MarketWatchService implements IMarketWatchService {
	@Autowired
	PrepareResponse prepareResponse;
	@Inject
	MarketWatchDAO marketWatchDAO;
	@Inject
	ApplicationProperties properties;
	@Inject
	CommonUtils commonUtils;

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
						for (int i = 0; i < mwListSize; i++) {
							MarketWatchNameDTO newDto = new MarketWatchNameDTO();
							newDto.setUserId(pUserId);
							newDto.setMwId(i + 1);
							newDto.setMwName(AppConstants.MARKET_WATCH_LIST_NAME + (i + 1));
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
				String user = tempStrArr[0];
				String mwId = tempStrArr[1];
				String mwName = tempStrArr[2];
				JSONObject result = new JSONObject();
				List<CacheMwDetailsModel> tempJsonObject = new ArrayList<CacheMwDetailsModel>();
				tempJsonObject = (List<CacheMwDetailsModel>) mwResponse.get(tempStr);
				result.put("mwId", mwId);
				result.put("mwName", mwName);
				result.put("isEdit", true);
				result.put("isDefault", true);
				result.put("isRename", true);

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
			if (pDto != null && StringUtil.isNotNullOrEmpty(pDto.getMwName()) && pDto.getMwId() != 0) {

				if (!commonUtils.isBetweenOneAndFive(pDto.getMwId())) {
					System.out.println(pDto.getMwId() + " is not between 1 and 5.");
					return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW101,AppConstants.INVALID_MW_ID);
				}

				if (!commonUtils.isOnlyInteger(String.valueOf(pDto.getMwId()))) {
					return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW101,AppConstants.INVALID_MW_ID);
				}

				if (pDto.getMwName().length() > 40) {
					return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW102,AppConstants.MW_NAME_40);
				}

				if (!commonUtils.isAlphanumeric(pDto.getMwName()) || pDto.getMwName() == null
						|| pDto.getMwName().isEmpty() || commonUtils.isEmptyOrWhitespace(pDto.getMwName())) {
					return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW103,AppConstants.MW_NAME);
				}

				renameMwInCache(pDto.getMwName().trim(), pDto.getMwId(), userId);
				updateMwNamw(pDto.getMwName().trim(), pDto.getMwId(), userId);
				return prepareResponse
						.prepareMWSuccessResponseString("Renamed to " + pDto.getMwName().trim() + " Successfully.");

			} else {
				return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW104,AppConstants.INVALID_PARAMETER);

			}

		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());

		}
		return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW105,AppConstants.FAILED_STATUS);

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

				if (!commonUtils.isBetweenOneAndFive(pDto.getMwId())) {
					System.out.println(pDto.getMwId() + " is not between 1 and 5.");
					return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW101,AppConstants.INVALID_MW_ID);
				}

				for (int i = 0; i < pDto.getScripData().size(); i++) {
					String exch = pDto.getScripData().get(i).getExch();
					if (!commonUtils.isValidExch(exch.trim())) {
						System.out.println(exch + " this EXCH is Not Correct.");
						return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW106,AppConstants.INVALID_EXCH);
					}
				}

				for (int i = 0; i < pDto.getScripData().size(); i++) {
					String exch = pDto.getScripData().get(i).getToken();
					if (!commonUtils.checkThisIsTheNumber(exch.trim())) {
						System.out.println(exch + " this is Not Number.");
						return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW107,AppConstants.INVALID_TOKEN);
					}
				}

				for (int i = 0; i < pDto.getScripData().size(); i++) {
					int exch = pDto.getScripData().get(i).getSortingOrder();
					if (!commonUtils.isBetweenOneAndFifty(exch)) {
						System.out.println(pDto.getMwId() + " is not between 1 and 50");
						return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW108,AppConstants.INVALID_SORTING_ORDER);
					}
				}

				sortFromCache(pDto.getScripData(), userId, pDto.getMwId());
				sortScripInDataBase(pDto.getScripData(), userId, pDto.getMwId());
				return prepareResponse.prepareMWSuccessResponseString(AppConstants.SORTING_ORDER);
			} else {
				return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW104,AppConstants.INVALID_PARAMETER);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW105,AppConstants.FAILED_STATUS);

	}

	/*
	 * method to sort from Cache
	 * 
	 */
	public void sortFromCache(List<MwScripModel> dataToSort, String pUserId, int userMwId) {
		if (dataToSort != null && dataToSort.size() > 0) {
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
					if (scripDetails != null && scripDetails.size() > 0) {
						for (int i = 0; i < dataToSort.size(); i++) {
							MwScripModel tempDTO = dataToSort.get(i);
							String token = tempDTO.getToken();
							String exch = tempDTO.getExch();
							int sortOrder = tempDTO.getSortingOrder();
							for (int j = 0; j < scripDetails.size(); j++) {
								CacheMwDetailsModel tempScripDTO = scripDetails.get(j);
								String scripToken = tempScripDTO.getToken();
								String scripExch = tempScripDTO.getExchange();
								if (scripToken.equalsIgnoreCase(token) && scripExch.equalsIgnoreCase(exch)) {
									tempScripDTO.setSortOrder(sortOrder);
									scripDetails.remove(j);
									scripDetails.add(tempScripDTO);
								}
							}
						}
						result.remove("scrips");
						result.put("scrips", scripDetails);
						res.remove(indexOfRes);
						res.add(indexOfRes, result);
						MwCacheController.getMwListUserId().remove(pUserId);
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
	 */
	private void sortScripInDataBase(List<MwScripModel> scripDataToSort, String userId, int mwId) {

		if (scripDataToSort != null && scripDataToSort.size() > 0) {
			List<MarketWatchScripDetailsDTO> mwList = marketWatchDAO.findAllByUserIdAndMwId(userId, mwId);
			System.out.println("sortScripInDataBase - for user " + userId + "Count- " + mwList.size());
			List<MarketWatchScripDetailsDTO> newScripDetails = new ArrayList<>();
			for (int i = 0; i < scripDataToSort.size(); i++) {
				MwScripModel model = new MwScripModel();
				model = scripDataToSort.get(i);
				for (int j = 0; j < mwList.size(); j++) {
					MarketWatchScripDetailsDTO dbData = new MarketWatchScripDetailsDTO();
					dbData = mwList.get(j);
					if (dbData.getToken().equalsIgnoreCase(model.getToken())
							&& dbData.getEx().equalsIgnoreCase(model.getExch())) {
						dbData.setSortingOrder(model.getSortingOrder());
						newScripDetails.add(dbData);
					}
				}
			}
			if (newScripDetails != null && newScripDetails.size() > 0) {
				int res = marketWatchDAO.updateMWScrips(newScripDetails, userId, mwId);
				if (res > 0) {
					System.out.println("Updated");
				}
			}
		}
	}

	@SuppressWarnings("static-access")
	@Override
	public RestResponse<ResponseModel> addscrip(MwRequestModel parmDto, String userId) {
		try {
			/*
			 * Check the list not null or empty
			 */
			if (StringUtil.isListNotNullOrEmpty(parmDto.getScripData()) && parmDto.getMwId() > 0) {

//				long result = marketWatchDAO.checkUserId(userId);
//				if(result <= 0) {
//					List<JSONObject> resp = create(userId);
//					if (resp != null && resp.size() > 0) {
//						return prepareResponse.prepareSuccessResponseObject(resp);
//					} 
//				}

				if (!commonUtils.isBetweenOneAndFive(parmDto.getMwId())) {
					System.out.println(parmDto.getMwId() + " is not between 1 and 5.");
					return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW101,AppConstants.INVALID_MW_ID);
				}

				for (int i = 0; i < parmDto.getScripData().size(); i++) {
					String exch = parmDto.getScripData().get(i).getExch();
					if (!commonUtils.isValidExch(exch.toUpperCase().trim())) {
						System.out.println(exch + " this EXCH is Not Correct.");
						return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW106,AppConstants.INVALID_EXCH);
					}
				}

				for (int i = 0; i < parmDto.getScripData().size(); i++) {
					String exch = parmDto.getScripData().get(i).getToken();
					if (!commonUtils.checkThisIsTheNumber(exch.trim())) {
						System.out.println(exch + " this is Not Number.");
						return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW107,AppConstants.INVALID_TOKEN);
					}
				}

				int curentSortOrder = getExistingSortOrder(userId, parmDto.getMwId());
				List<MwScripModel> mwScripModels = new ArrayList<>();
				for (MwScripModel model : parmDto.getScripData()) {
					curentSortOrder = curentSortOrder + 1;
					model.setSortingOrder(curentSortOrder);
					mwScripModels.add(model);
				}
				List<CacheMwDetailsModel> scripDetails = getScripMW(mwScripModels);
				if (scripDetails != null && scripDetails.size() > 0) {
					List<CacheMwDetailsModel> newScripDetails = addNewScipsForMwIntoCache(scripDetails, userId,
							parmDto.getMwId());
					if (newScripDetails != null && newScripDetails.size() > 0) {
						insertNewScipsForMwIntoDataBase(newScripDetails, userId, parmDto.getMwId());
					}
					
					JSONObject finalOutput = new JSONObject();
			        finalOutput.put("scrip", scripDetails);

					return prepareResponse.prepareSuccessResponseObject(finalOutput);
				} else {
					return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW104,AppConstants.INVALID_PARAMETER);

				}
			} else {
				return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW104,AppConstants.INVALID_PARAMETER);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		}
		return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW105,AppConstants.FAILED_STATUS);

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
		try {
			for (int itr = 0; itr < pDto.size(); itr++) {
				MwScripModel result = new MwScripModel();
				result = pDto.get(itr);
				String exch = result.getExch().toUpperCase().trim();
				String token = result.getToken().trim();
				System.out.println(exch + "_" + token);
				if (HazelCacheController.getInstance().getContractMaster().get(exch + "_" + token) != null) {
					ContractMasterModel masterData = HazelCacheController.getInstance().getContractMaster()
							.get(exch + "_" + token);
					System.out.println(
							exch + "_" + token + "Response >>>>>>>" + masterData.getExpiry() + masterData.getWeekTag());
					CacheMwDetailsModel fResult = new CacheMwDetailsModel();
					fResult.setSymbol(masterData.getSymbol());
					fResult.setTradingSymbol(masterData.getTradingSymbol());
					fResult.setFormattedInsName(masterData.getFormattedInsName());
					fResult.setToken(masterData.getToken());
					fResult.setExchange(masterData.getExch());
					fResult.setSegment(masterData.getSegment());
					fResult.setExpiry(masterData.getExpiry() == null ? null : masterData.getExpiry());
//					fResult.setExpiry(masterData.getExpiry());
					fResult.setSortOrder(result.getSortingOrder());
					fResult.setPdc(masterData.getPdc());
//					fResult.setWeekTag(masterData.getWeekTag());
					fResult.setWeekTag(masterData.getWeekTag() == null ? "" : masterData.getWeekTag());

//					fResult.badge = Map.of("event", "", "bnpl", "", "ideas", "", "holdingqty", "");
//					fResult.screeners = List.of("topGainer", "52wk High", "Volume shocker");

					// Prepare badge and screeners
					Map<String, String> badge = Map.of("event", "true", "bnpl", "", "ideas", "", "holdingqty", "");
					List<String> screeners = List.of("topGainer", "52wk High", "Volume shocker");

					// Set badge and screeners only if they are non-empty
					if (!badge.isEmpty() && badge.values().stream().anyMatch(value -> !value.isEmpty())) {
						fResult.setBadge(badge);
					}
					if (!screeners.isEmpty() && screeners.stream().anyMatch(s -> !s.isEmpty())) {
						fResult.setScreeners(screeners);
					}
					response.add(fResult);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
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
		for (int i = 0; i < scripDetails.size(); i++) {
			CacheMwDetailsModel model = scripDetails.get(i);
			MarketWatchScripDetailsDTO resultDto = new MarketWatchScripDetailsDTO();
			String exch = model.getExchange();
			String token = model.getToken();
			if (HazelCacheController.getInstance().getContractMaster().get(exch + "_" + token) != null) {
				ContractMasterModel masterData = HazelCacheController.getInstance().getContractMaster()
						.get(exch + "_" + token);

				resultDto.setUserId(userId);
				resultDto.setMwId(mwId);
				resultDto.setEx(exch);
				resultDto.setToken(token);
				resultDto.setTradingSymbol(masterData.getTradingSymbol());
				resultDto.setEx(masterData.getExch());
				resultDto.setExSeg(masterData.getSegment());
				resultDto.setToken(masterData.getToken());
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
				marketWatchScripDetailsDTOs.add(resultDto);
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

				if (!commonUtils.isBetweenOneAndFive(pDto.getMwId())) {
					System.out.println(pDto.getMwId() + " is not between 1 and 5.");
					return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW101,AppConstants.INVALID_MW_ID);
				}

				for (int i = 0; i < pDto.getScripData().size(); i++) {
					String exch = pDto.getScripData().get(i).getExch();
					if (!commonUtils.isValidExch(exch.toUpperCase().trim())) {
						System.out.println(exch + " this EXCH is Not Correct.");
						return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW106,AppConstants.INVALID_EXCH);
					}
				}

				for (int i = 0; i < pDto.getScripData().size(); i++) {
					String token = pDto.getScripData().get(i).getToken();
					if (!commonUtils.checkThisIsTheNumber(token.trim())) {
						System.out.println(token + " this is Not Number.");
						return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW107,AppConstants.INVALID_TOKEN);
					}
				}

				long checkDeleteId = marketWatchDAO.selectByUserId(pDto, userId);

				if (checkDeleteId <= 0) {
					System.out.println(checkDeleteId);
					return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW110,AppConstants.ALREADY_DELETED_SCRIPT);
				}

				deleteFromCache(dataToDelete, useriD, mwId);
				deleteFromDB(dataToDelete, useriD, mwId);
//				return prepareResponse.prepareSuccessResponseObject(AppConstants.EMPTY_ARRAY);
				return prepareResponse.prepareMWSuccessResponseString("Deleted Successfully");
			} else {
				return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW104,AppConstants.INVALID_PARAMETER);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		}
		return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW105,AppConstants.FAILED_STATUS);

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
		if (dataToDelete != null && dataToDelete.size() > 0) {
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
					if (scripDetails != null && scripDetails.size() > 0) {
						for (int i = 0; i < dataToDelete.size(); i++) {
							MwScripModel tempDTO = dataToDelete.get(i);
							String token = tempDTO.getToken().toUpperCase().trim();
							String exch = tempDTO.getExch().toUpperCase().trim();
							for (int j = 0; j < scripDetails.size(); j++) {
								CacheMwDetailsModel tempScripDTO = scripDetails.get(j);
								String scripToken = tempScripDTO.getToken();
								String scripExch = tempScripDTO.getExchange();
								if (scripToken.equalsIgnoreCase(token) && scripExch.equalsIgnoreCase(exch)) {
									scripDetails.remove(j);
								}
							}
						}
						result.remove("scrips");
						result.put("scrips", scripDetails);
						res.remove(indexOfRes);
						res.add(indexOfRes, result);
						MwCacheController.getMwListUserId().remove(pUserId);
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
					if (dataToDelete != null && dataToDelete.size() > 0) {
						for (int i = 0; i < dataToDelete.size(); i++) {
							MwScripModel tempDTO = dataToDelete.get(i);
							String token = tempDTO.getToken().toUpperCase().trim();
							String exch = tempDTO.getExch().toUpperCase().trim();
							marketWatchDAO.deleteScripFomDataBase(pUserId, exch, token, userMwId);
						}
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
	public RestResponse<ResponseModel> getAllMwScrips(MwRequestModel pDto, String pUserId) {
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
				List<MarketWatchNameDTO> newMwList = new ArrayList<>();
				System.out.println("create mw for user - " + pUserId + "and count - " + mwList.size());
				/* If null or size is lesser than 5 create a new Market Watch */
				if (mwList == null || mwList.size() == 0) {
					System.out.println("create new mw for user - " + pUserId);
					/* Create the new Market Watch */
					int mwListSize = Integer.parseInt(properties.getMwSize());
					for (int i = 0; i < mwListSize; i++) {
						MarketWatchNameDTO newDto = new MarketWatchNameDTO();
						newDto.setUserId(pUserId);
						newDto.setMwId(i + 1);
						newDto.setMwName(AppConstants.MARKET_WATCH_LIST_NAME + (i + 1));
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
	 */
	@Override
	public RestResponse<ResponseModel> getIndices() {
		MwIndicesModel result = new MwIndicesModel();

		/*
		 * TODO Indices Set to ContractMaster data
		 */
		result.setClosingIndex("25383.75");
		result.setExchange("N");
		result.setSegmemt("C");
		result.setIndexName("Nifty");
		result.setIndexValue("25418.55");
		result.setIndiceID("999920000");

		return prepareResponse.prepareSuccessResponseObject(result);
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
		try {

			/** Validate Request **/
			if (!validateSecurityInfoParameters(model))
				return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW104,AppConstants.INVALID_PARAMETER);

			if (!commonUtils.isValidExch(model.getExch().trim())) {
				return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW106,AppConstants.INVALID_EXCH);
			}

			if (!commonUtils.checkThisIsTheNumber(model.getToken())) {
				return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW107,AppConstants.INVALID_TOKEN);
			}

			if (HazelCacheController.getInstance().getContractMaster()
					.get(model.getExch() + "_" + model.getToken()) != null) {
				ContractMasterModel masterData = HazelCacheController.getInstance().getContractMaster()
						.get(model.getExch() + "_" + model.getToken());
				SecurityInfoRespModel infoResult = new SecurityInfoRespModel();
				infoResult.setExchange(masterData.getExch());
				infoResult.setToken(masterData.getToken());
				infoResult.setTradingSymbol(masterData.getTradingSymbol());
				infoResult.setLotSize(masterData.getLotSize());
				infoResult.setTickSize(masterData.getTickSize());
				infoResult.setSymbol(masterData.getSymbol());
				infoResult.setPdc(masterData.getPdc());
				infoResult.setInsType(masterData.getInsType());
				infoResult.setExpiry(masterData.getExpiry());
				infoResult.setQtyLimit("");
				infoResult.setSliceEnable("");
				infoResult.setSurveillance("");
				infoResult.setScripIndex("");
				infoResult.setScripFnO("");

				SpotData spotDataResult = new SpotData();
				spotDataResult.setLtp(1);
				spotDataResult.setToken(masterData.getToken());
				spotDataResult.setTradingSymbol(masterData.getTradingSymbol());

				Prompt promptResult = new Prompt();
				JSONArray promptObj = new JSONArray();
				promptResult.setCategory("");
				promptResult.setDescription("");
				promptObj.add(promptResult);

				ProductLeverage productLeverageResult = new ProductLeverage();
				productLeverageResult.setDelivery("");
				productLeverageResult.setIntraday("");
				productLeverageResult.setBnpl("");

				Badge badge = new Badge();
				JSONArray badgeObj = new JSONArray();
				badge.setEvent("");
				badge.setBnpl("");
				badge.setIdeas("");
				badge.setHoldingqty(null);
				badgeObj.add(badge);

				infoResult.setSpotData(spotDataResult);
				infoResult.setPrompt(promptObj);
				infoResult.setProductLeverage(productLeverageResult);
				infoResult.setBadge(badgeObj);
				infoResult.setScreeners(List.of("topGainer", "52wk High", "Volume shocker"));
				return prepareResponse.prepareSuccessResponseObject(infoResult);

			}

		} catch (Exception e) {
			Log.error(e.getMessage());
			e.printStackTrace();
		}
		return prepareResponse.prepareMWFailedResponse(ErrorCodeConstants.ECMW105,AppConstants.FAILED_STATUS);
	}

	/**
	 * @param model
	 * @return
	 */
	private boolean validateSecurityInfoParameters(SecurityInfoReqModel model) {
		if (StringUtil.isNotNullOrEmpty(model.getExch()) && StringUtil.isNotNullOrEmpty(model.getToken())
				&& StringUtil.isNotNullOrEmpty(model.getSegment())) {
			return true;
		}
		return false;
	}

}
