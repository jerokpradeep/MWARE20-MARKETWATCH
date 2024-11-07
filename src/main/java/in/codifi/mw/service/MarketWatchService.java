package in.codifi.mw.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.resteasy.reactive.RestResponse;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import in.codifi.mw.cache.MwCacheController;
import in.codifi.mw.config.ApplicationProperties;
import in.codifi.mw.entity.MarketWatchNameDTO;
import in.codifi.mw.model.CacheMwDetailsModel;
import in.codifi.mw.model.MwRequestModel;
import in.codifi.mw.model.ResponseModel;
import in.codifi.mw.repository.MarketWatchDAO;
import in.codifi.mw.service.spec.IMarketWatchService;
import in.codifi.mw.util.AppConstants;
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
				if (tempJsonObject != null && tempJsonObject.size() > 0) {
					result.put("scrips", tempJsonObject);
				} else {
					result.put("scrips", null);
				}

				response = MwCacheController.getMwListByUserId().get(user);
				if (response != null) {
					response = MwCacheController.getMwListByUserId().get(user);
					response.add(result);
					MwCacheController.getMwListByUserId().put(user, response);
				} else {
					response = new ArrayList<JSONObject>();
					response.add(result);
					MwCacheController.getMwListByUserId().put(user, response);
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

	@Override
	public RestResponse<ResponseModel> renameMarketWatch(MwRequestModel pDto) {
		try {
			if (pDto != null && StringUtil.isNotNullOrEmpty(pDto.getMwName())
					&& StringUtil.isNotNullOrEmpty(pDto.getUserId()) && pDto.getMwId() != 0) {

				renameMwInCache(pDto.getMwName(), pDto.getMwId(), pDto.getUserId());
				updateMwNamw(pDto.getMwName(), pDto.getMwId(), pDto.getUserId());
//				return prepareResponse.prepareSuccessResponseObject(AppConstants.EMPTY_ARRAY);
				return prepareResponse.prepareMWSuccessResponseObject(AppConstants.SUCCESS_STATUS);

			} else {
				return prepareResponse.prepareFailedResponse(AppConstants.INVALID_PARAMETER);
			}

		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());

		}
		return prepareResponse.prepareFailedResponse(AppConstants.FAILED_STATUS);
	}
	
	/** rename MW in cache **/
	private void renameMwInCache(String newWwName, int mwId, String userId) {

		List<JSONObject> res = MwCacheController.getMwListByUserId().get(userId);
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
					MwCacheController.getMwListByUserId().remove(userId);
					MwCacheController.getMwListByUserId().put(userId, res);
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

}
