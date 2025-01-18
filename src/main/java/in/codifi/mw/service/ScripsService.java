/**
 * 
 */
package in.codifi.mw.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.ObjectUtils;
import org.jboss.resteasy.reactive.RestResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.codifi.cache.model.ContractMasterModel;
import in.codifi.mw.cache.RedisConfig;
import in.codifi.mw.cache.RedisConstants;
import in.codifi.mw.config.ApplicationProperties;
import in.codifi.mw.entity.secondary.RecentlyViewedEntity;
import in.codifi.mw.model.ClinetInfoModel;
import in.codifi.mw.model.ErrorResponseModel;
import in.codifi.mw.model.RecentlyViewedModel;
import in.codifi.mw.model.ResponseModel;
import in.codifi.mw.model.ScripSearchResp;
import in.codifi.mw.model.SearchModel;
import in.codifi.mw.model.SearchScripReqModel;
import in.codifi.mw.repository.RecentlyViewedRepository;
import in.codifi.mw.repository.ScripSearchEntityManager;
import in.codifi.mw.service.spec.ScripsServiceSpecs;
import in.codifi.mw.util.AppConstants;
import in.codifi.mw.util.CommonUtils;
import in.codifi.mw.util.ErrorCodeConstants;
import in.codifi.mw.util.ErrorMessageConstants;
import in.codifi.mw.util.PrepareResponse;
import in.codifi.mw.util.StringUtil;
import io.quarkus.logging.Log;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

/**
 * @author Vicky
 *
 */
@ApplicationScoped
public class ScripsService implements ScripsServiceSpecs {

	private static final ConcurrentHashMap<String, List<ScripSearchResp>> getLoadedSearchData = new ConcurrentHashMap<>();

	@Inject
	ScripSearchEntityManager scripSearchRepo;

	@Inject
	PrepareResponse prepareResponse;

	@Inject
	CommonUtils commonUtils;

	@Inject
	ApplicationProperties properties;

	@Inject
	RecentlyViewedRepository recentlyViewedRepository;

	/**
	 *
	 */
	public RestResponse<ResponseModel> getScrips(SearchScripReqModel reqModel) {
		List<ScripSearchResp> responses = new ArrayList<>();
		JedisPool jedisPool = RedisConfig.getInstance().getJedisPool();
		Jedis jedis = null; // Declare Jedis here to manage it properly

		try {
			// Validate and set page size
			int tempPageSize = 50;
			String pageSize = reqModel.getPageSize();
			if (StringUtil.isNotNullOrEmptyAfterTrim(pageSize)) {
				if (!commonUtils.isPositiveWholeNumber(pageSize.trim())) {
					return prepareResponse.prepareFailedResponse(AppConstants.INVALID_PAGE_SIZE);
				}
				int parsedPageSize = Integer.parseInt(pageSize.trim());
				if (!commonUtils.isBetweenOneAndhundred(parsedPageSize)) {
					return prepareResponse.prepareMWFailedResponseString(AppConstants.PAGE_SIZE_100);
				}
				tempPageSize = parsedPageSize;
			}

			// Validate and set current page
			int currentPage = 1;
			String currentPageStr = reqModel.getCurrentPage();
			if (StringUtil.isNotNullOrEmptyAfterTrim(currentPageStr)) {
				if (!commonUtils.isPositiveWholeNumber(currentPageStr.trim())) {
					return prepareResponse.prepareFailedResponse(AppConstants.INVALID_CURRENT_PAGE);
				}
				int parsedCurrentPage = Integer.parseInt(currentPageStr.trim());
				if (!commonUtils.isBetweenOneAndfifty(parsedCurrentPage)) {
					return prepareResponse.prepareMWFailedResponseString(AppConstants.CURRENT_PAGE_50);
				}
				currentPage = parsedCurrentPage;
			}

			// Fetch data from cache if enabled
			String fetchDataFromCache = null;
			try {
				jedis = jedisPool.getResource(); // Get a connection from the pool
				fetchDataFromCache = jedis.hget(RedisConstants.FETCHDATAFROMCACHE, AppConstants.FETCH_DATA_FROM_CACHE);
			} catch (JedisException e) {
				Log.error("Error fetching data from Redis: " + e.getMessage(), e);
				return prepareResponse.prepareFailedResponse(AppConstants.FAILED_STATUS); // Return failed response if
																							// Redis fails
			}

			if (Boolean.parseBoolean(fetchDataFromCache)) {
				// Handle cache logic
				String searchText = reqModel.getSearchText().trim();
				if (searchText.length() < 2) {
					String cacheKey = String.valueOf(searchText.length());
					List<String> distinctSymbols = jedis.lrange("distinctSymbols_" + cacheKey, 0, -1);
					if (distinctSymbols != null && !distinctSymbols.isEmpty()
							&& distinctSymbols.contains(searchText.toUpperCase())) {
						responses = getSearchDetailsFromCache(reqModel);
					}
				} else {
					responses = getSearchDetailsFromCache(reqModel);
				}
			} else {
				// Handle DB logic
				String searchText = reqModel.getSearchText().trim();
				if (searchText.length() < 2) {
					String cacheKey = String.valueOf(searchText.length());
					List<String> distinctSymbols = jedis.lrange("distinctSymbols_" + cacheKey, 0, -1);
					if (distinctSymbols != null && !distinctSymbols.isEmpty()
							&& distinctSymbols.contains(searchText.toUpperCase())) {
						responses = scripSearchRepo.getScrips(reqModel);
					}
				} else {
					responses = scripSearchRepo.getScrips(reqModel);
				}
			}

			// Calculate page count
			String totalCount = scripSearchRepo.getScripsCount(reqModel);
			int pageCount = 0;
			if (StringUtil.isNotNullOrEmptyAfterTrim(totalCount)) {
				pageCount = (int) Math.ceil((double) Integer.parseInt(totalCount) / tempPageSize);
			}

			// Prepare the response
			if (responses != null && !responses.isEmpty()) {
				SearchModel finalOutput = new SearchModel();
				finalOutput.setCurrentPage(currentPage);
				finalOutput.setPageCount(pageCount);
				finalOutput.setSearchResult(responses);
				return prepareResponse.prepareSuccessResponseWithMessage(finalOutput, AppConstants.SUCCESS_STATUS,
						false);
			} else {
				// Return empty result response
				SearchModel obj = new SearchModel();
				obj.setCurrentPage(0);
				obj.setSearchResult(Collections.emptyList());
				return prepareResponse.prepareMWFailedwithEmtyResult(ErrorCodeConstants.ECMW111,
						ErrorMessageConstants.NOT_FOUND, obj);
			}

		} catch (Exception e) {
			Log.error("Error in getScrips: " + e.getMessage(), e);
			return prepareResponse.prepareFailedResponse(AppConstants.FAILED_STATUS);
		} finally {
			if (jedis != null) {
				try {
					jedis.close(); // Always return the Jedis resource to the pool
				} catch (Exception e) {
					Log.error("Error closing Jedis connection: " + e.getMessage(), e);
				}
			}
		}
	}

	private List<ScripSearchResp> getSearchDetailsFromCache(SearchScripReqModel reqModel) {
		List<ScripSearchResp> responses = new ArrayList<>();
		List<String> adjustedExchangeList = new ArrayList<>();
		String[] exchange = null;

		// Adjust exchange list based on properties.isExchfull()
		for (String exch : reqModel.getExchange()) {
			String adjustedExchange = "ALL"; // Default to the original filename

			if (properties.isExchfull()) {
				// Apply the switch statement to adjust the filename
				switch (exch.toUpperCase()) {
				case "NSEEQ":
					adjustedExchange = "NSE";
					break;
				case "NSEFO":
					adjustedExchange = "NFO";
					break;
				case "BSEEQ":
					adjustedExchange = "BSE";
					break;
				case "BSEFO":
					adjustedExchange = "BFO";
					break;
				case "NSECURR":
					adjustedExchange = "CDS";
					break;
				case "BSECURR":
					adjustedExchange = "BCD";
					break;
				case "MCXCOMM":
					adjustedExchange = "MCX";
					break;
				case "NSECOMM":
					adjustedExchange = "NCO";
					break;
				default:
					break;
				}
			} else {
				adjustedExchange = exch.toUpperCase();
			}

			adjustedExchangeList.add(adjustedExchange); // Add adjusted exchange to list
		}
		exchange = adjustedExchangeList.toArray(new String[0]);

		if (Arrays.stream(exchange).anyMatch("all"::equalsIgnoreCase)) {
			// Create cache key
			String cacheKey = reqModel.getSearchText().trim().toUpperCase() + "_" + reqModel.getPageSize() + "_"
					+ reqModel.getCurrentPage();

			JedisPool jedisPool = RedisConfig.getInstance().getJedisPool();
			Jedis jedis = null; // Declare Jedis instance to manage resource properly

			try {
				jedis = jedisPool.getResource(); // Get Jedis instance from pool

				// Fetch from Redis cache if available
				String cachedData = jedis.get(cacheKey);
				if (cachedData != null) {
					// Parse the cached data (assuming it's JSON)
					responses = new ObjectMapper().readValue(cachedData, new TypeReference<List<ScripSearchResp>>() {
					});
				} else {
					// Fetch from DB if not found in cache
					responses = scripSearchRepo.getScrips(reqModel);
					if (responses != null && !responses.isEmpty()) {
						// Update the cache with the fetched data
						String indexKey = reqModel.getSearchText().trim().toUpperCase();
						String indexData = jedis.get(indexKey);

						if (indexData != null) {
							ScripSearchResp result = new ObjectMapper().readValue(indexData, ScripSearchResp.class);
							responses.set(0, result); // Set index data to the first item
							if (responses.size() > 24) {
								responses.remove(25); // Remove extra entries if the list exceeds the limit
							}
						}

						// Store the fetched data in Redis cache
						jedis.set(cacheKey, new ObjectMapper().writeValueAsString(responses));
					}
				}
			} catch (Exception e) {
				Log.error("Error in getSearchDetailsFromCache: " + e.getMessage(), e);
			} finally {
				if (jedis != null) {
					try {
						jedis.close(); // Return Jedis resource to the pool
					} catch (Exception e) {
						Log.error("Error closing Jedis connection: " + e.getMessage(), e);
					}
				}
			}
		} else {
			// Fetch data from DB if cache is not used
			responses = scripSearchRepo.getScrips(reqModel);
		}

		return responses;
	}

	@Override
	public RestResponse<ResponseModel> getRecentlyViewed(ClinetInfoModel info) {
		ErrorResponseModel errorResponseModel = new ErrorResponseModel();
		JedisPool jedisPool = RedisConfig.getInstance().getJedisPool();
		Jedis jedis = null; // Declare Jedis instance for proper management

		try {
			String userId = info.getUserId();

			// Check if recently viewed data exists for the given user Id in the database
			List<RecentlyViewedEntity> recentlyViewedData = recentlyViewedRepository
					.findAllByUserIdOrderBySortOrderAsc(userId);

			if (recentlyViewedData == null || recentlyViewedData.isEmpty()) {
				// If no data is found for the user, return a failed response with a "no data"
				// message
				errorResponseModel.setStatus(AppConstants.FAILED_STATUS);
				errorResponseModel.setMessage(AppConstants.NO_DATA);
			}

			List<RecentlyViewedModel> recentlyViewedResonse = new ArrayList<>();

			jedis = jedisPool.getResource(); // Get Jedis instance from pool

			for (RecentlyViewedEntity result : recentlyViewedData) {
				String tempToken = result.getToken();
				String tempExch = result.getExch();
				int sortingOrder = result.getSortOrder();

				// Check the cache for the given exchange and token
				String cacheKey = tempExch + "_" + tempToken;

				String json = jedis.hget("contractMaster", cacheKey);
				if (json != null) {
					// If data is found in the cache, deserialize it into a ContractMasterModel
					ObjectMapper objectMapper = new ObjectMapper();
					ContractMasterModel contractMasterModel = objectMapper.readValue(json, ContractMasterModel.class);

					if (ObjectUtils.isNotEmpty(contractMasterModel)) {
						// Create a RecentlyViewedModel and populate it with data from the cache
						RecentlyViewedModel tempResult = new RecentlyViewedModel();
						tempResult.setAlterToken(contractMasterModel.getAlterToken());
						tempResult.setCompanyName(contractMasterModel.getCompanyName());
						tempResult.setExch(contractMasterModel.getExch());
						tempResult.setExpiry(contractMasterModel.getExpiry());
						tempResult.setFormattedInsName(contractMasterModel.getFormattedInsName());
						tempResult.setFreezQty(contractMasterModel.getFreezQty());
						tempResult.setGroupName(contractMasterModel.getGroupName());
						tempResult.setInsType(contractMasterModel.getInsType());
						tempResult.setIsin(contractMasterModel.getIsin());
						tempResult.setLotSize(contractMasterModel.getLotSize());
						tempResult.setOptionType(contractMasterModel.getOptionType());
						tempResult.setPdc(contractMasterModel.getPdc());
						tempResult.setSegment(contractMasterModel.getSegment());
						tempResult.setSortingOrder(sortingOrder);
						tempResult.setStrikePrice(contractMasterModel.getStrikePrice());
						tempResult.setSymbol(contractMasterModel.getSymbol());
						tempResult.setTickSize(contractMasterModel.getTickSize());
						tempResult.setToken(contractMasterModel.getToken());
						tempResult.setTradingSymbol(contractMasterModel.getTradingSymbol());
						tempResult.setWeekTag(contractMasterModel.getWeekTag());

						recentlyViewedResonse.add(tempResult); // Add the result to the response list
					}
				}
			}

			// If results are found, return a successful response with the recently viewed
			// data
			if (!recentlyViewedResonse.isEmpty()) {
				return prepareResponse.prepareSuccessResponseObject(recentlyViewedResonse);
			} else {
				// If no data is found after checking the cache, return an error response
				errorResponseModel.setStatus(AppConstants.FAILED_STATUS);
				errorResponseModel.setMessage(AppConstants.NO_DATA);
			}

		} catch (Exception e) {
			// Log the error and return a failed response
			Log.error("Error in getRecentlyViewed: " + e.getMessage(), e);
			errorResponseModel.setStatus(AppConstants.FAILED_STATUS);
			errorResponseModel.setMessage(AppConstants.INTERNAL_ERROR);
		} finally {
			if (jedis != null) {
				try {
					jedis.close(); // Ensure the Jedis resource is returned to the pool
				} catch (Exception e) {
					Log.error("Error closing Jedis connection: " + e.getMessage(), e);
				}
			}
		}
		return prepareResponse.prepareFailedResponse(AppConstants.FAILED_STATUS);
	}

}
