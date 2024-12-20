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

import org.jboss.resteasy.reactive.RestResponse;

import in.codifi.cache.model.ContractMasterModel;
import in.codifi.mw.config.ApplicationProperties;
import in.codifi.mw.config.HazelcastConfig;
import in.codifi.mw.model.ClinetInfoModel;
import in.codifi.mw.model.ErrorResponseModel;
import in.codifi.mw.model.RecentlyViewedEntity;
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
import org.apache.commons.lang3.ObjectUtils;

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
	@SuppressWarnings("unchecked")
	@Override
	public RestResponse<ResponseModel> getScrips(SearchScripReqModel reqModel) {
		List<ScripSearchResp> responses = new ArrayList<>();
		try {
			int tempPageSize = (StringUtil.isNullOrEmpty(reqModel.getPageSize())
					|| reqModel.getPageSize().equalsIgnoreCase("0")) ? 50 : Integer.parseInt(reqModel.getPageSize());
			int currentPage = StringUtil.isNullOrEmpty(reqModel.getCurrentPage())
					|| reqModel.getCurrentPage().equalsIgnoreCase("0") ? 1
							: Integer.parseInt(reqModel.getCurrentPage());
			if (StringUtil.isNotNullOrEmptyAfterTrim(reqModel.getPageSize())) {
				if (!commonUtils.isPositiveWholeNumber(reqModel.getPageSize().trim())) {
					return prepareResponse.prepareFailedResponse(AppConstants.INVALID_PAGE_SIZE);
				} else {
					if (!commonUtils.isBetweenOneAndhundred(Integer.parseInt(reqModel.getPageSize().trim()))) {
						return prepareResponse.prepareMWFailedResponseString(AppConstants.PAGE_SIZE_100);
					}
					tempPageSize = Integer.parseInt(reqModel.getPageSize().trim());
				}
			}

			if (StringUtil.isNotNullOrEmptyAfterTrim(reqModel.getCurrentPage())) {

				if (!commonUtils.isPositiveWholeNumber(reqModel.getCurrentPage().trim())) {
					return prepareResponse.prepareFailedResponse(AppConstants.INVALID_CURRENT_PAGE);
				} else {
					if (!commonUtils.isBetweenOneAndfifty(Integer.parseInt(reqModel.getCurrentPage().trim()))) {
						return prepareResponse.prepareMWFailedResponseString(AppConstants.CURRENT_PAGE_50);
					}
				}

			}

			/* To check where to fetch data */
			if (HazelcastConfig.getInstance().getFetchDataFromCache().get(AppConstants.FETCH_DATA_FROM_CACHE) != null
					&& HazelcastConfig.getInstance().getFetchDataFromCache().get(AppConstants.FETCH_DATA_FROM_CACHE)) {

				if (reqModel.getSearchText().trim().length() < 2) {
					if (HazelcastConfig.getInstance().getDistinctSymbols()
							.get(reqModel.getSearchText().trim().length()) != null
							&& HazelcastConfig.getInstance().getDistinctSymbols()
									.get(reqModel.getSearchText().trim().length()).size() > 0
							&& HazelcastConfig.getInstance().getDistinctSymbols()
									.get(reqModel.getSearchText().trim().length())
									.contains(reqModel.getSearchText().trim().toUpperCase())) {
						responses = getSearchDetailsFromCache(reqModel);
					}
				} else {
					responses = getSearchDetailsFromCache(reqModel);
				}
			} else {
				if (reqModel.getSearchText().trim().length() < 2) {
					if (HazelcastConfig.getInstance().getDistinctSymbols()
							.get(reqModel.getSearchText().trim().length()) != null
							&& HazelcastConfig.getInstance().getDistinctSymbols()
									.get(reqModel.getSearchText().trim().length()).size() > 0
							&& HazelcastConfig.getInstance().getDistinctSymbols()
									.get(reqModel.getSearchText().trim().length())
									.contains(reqModel.getSearchText().trim().toUpperCase())) {
						responses = scripSearchRepo.getScrips(reqModel);
					}
				} else {
					responses = scripSearchRepo.getScrips(reqModel);
				}
			}

			String totalCount = scripSearchRepo.getScripsCount(reqModel);
			int pageCount = 0;
			if (StringUtil.isNotNullOrEmptyAfterTrim(totalCount)) {
//				pageCount = Integer.parseInt(totalCount) / tempPageSize;
				pageCount = (int) Math.ceil((double) Integer.parseInt(totalCount) / tempPageSize);
			}

			if (responses != null && responses.size() > 0) {
				SearchModel finalOutput = new SearchModel();
				finalOutput.setCurrentPage(currentPage);
				finalOutput.setPageCount(Math.round(pageCount));
				finalOutput.setSearchResult(responses);
				return prepareResponse.prepareSuccessResponseWithMessage(finalOutput, AppConstants.SUCCESS_STATUS,
						false);
			} else {
				// Create the main JSONObject
				SearchModel obj = new SearchModel();
				// Add values in the specific order you need
				obj.setCurrentPage(0);
				obj.setCurrentPage(0);
				obj.setSearchResult(Collections.emptyList());
				return prepareResponse.prepareMWFailedwithEmtyResult(ErrorCodeConstants.ECMW111,
						ErrorMessageConstants.NOT_FOUND, obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		}
		return prepareResponse.prepareFailedResponse(AppConstants.FAILED_STATUS);
	}

	private List<ScripSearchResp> getSearchDetailsFromCache(SearchScripReqModel reqModel) {

		List<ScripSearchResp> responses = new ArrayList<>();
		List<String> adjustedExchangeList = new ArrayList<>();
		/*
		 * Check the cache is not and storing is enabled or not
		 */
		String[] exchange = null;
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
					// Keep filename as is if no match is found
					break;
				}
			}else {
				adjustedExchange = exch.toUpperCase();
			}
			
			// Add the adjusted filename to the ArrayList
			adjustedExchangeList.add(adjustedExchange);
		}
		// Convert the ArrayList to an array
		exchange = adjustedExchangeList.toArray(new String[0]);
		/*
		 * Check Exchange array contains ALL
		 */
		if (Arrays.stream(exchange).anyMatch("all"::equalsIgnoreCase)) {
			if (getLoadedSearchData.get(reqModel.getSearchText().trim().toUpperCase() + "_" + reqModel.getPageSize()
					+ "_" + reqModel.getCurrentPage()) != null) {
				responses = getLoadedSearchData.get(reqModel.getSearchText().trim().toUpperCase() + "_"
						+ reqModel.getPageSize() + "_" + reqModel.getCurrentPage());
			} else {
				responses = scripSearchRepo.getScrips(reqModel);
				if (responses != null && responses.size() > 0) {
					if (HazelcastConfig.getInstance().getIndexDetails()
							.get(reqModel.getSearchText().trim().toUpperCase()) != null) {
						ScripSearchResp result = HazelcastConfig.getInstance().getIndexDetails()
								.get(reqModel.getSearchText().trim().toUpperCase());
						responses.set(0, result);
						if (responses.size() > 24) {
							responses.remove(25);
						}
					}
					String pageSize = StringUtil.isNullOrEmpty(reqModel.getPageSize()) ? "50" : reqModel.getPageSize();
					String currentPage = StringUtil.isNullOrEmpty(reqModel.getCurrentPage()) ? "1"
							: reqModel.getCurrentPage();
					getLoadedSearchData.put(
							reqModel.getSearchText().trim().toUpperCase() + "_" + pageSize + "_" + currentPage,
							responses);
				}
			}
		} else {
			responses = scripSearchRepo.getScrips(reqModel);
		}
		return responses;
	}

	@Override
	public RestResponse<ResponseModel> getRecentlyViewed(ClinetInfoModel info) {
		ErrorResponseModel errorResponseModel = new ErrorResponseModel();
		try {
			String userId = info.getUserId();
			/*
			 * Check the data's are present for the given user Id into the Database
			 */
			List<RecentlyViewedEntity> recentlyViewedData = recentlyViewedRepository
					.findAllByUserIdOrderBySortOrderAsc(userId);
			List<RecentlyViewedModel> recentlyViewedResonse = new ArrayList<>();
			if (recentlyViewedData != null && recentlyViewedData.size() > 0) {
				for (RecentlyViewedEntity result : recentlyViewedData) {
					String tempToken = result.getToken();
					String tempExch = result.getExch();
					int sortingOrder = result.getSortOrder();
					/*
					 * Check the hazecast cache for the given exchange and token
					 */
					ContractMasterModel contractMasterModel = HazelcastConfig.getInstance().getContractMaster()
							.get(tempExch + "_" + tempToken);
					if (ObjectUtils.isNotEmpty(contractMasterModel)) {
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
						recentlyViewedResonse.add(tempResult);
					}
				}
				if (recentlyViewedResonse != null && recentlyViewedResonse.size() > 0) {
					return prepareResponse.prepareSuccessResponseObject(recentlyViewedResonse);
				} else {
					errorResponseModel.setStatus(AppConstants.FAILED_STATUS);
					errorResponseModel.setMessage(AppConstants.NO_DATA);
				}
			} else {
				errorResponseModel.setStatus(AppConstants.FAILED_STATUS);
				errorResponseModel.setMessage(AppConstants.NO_DATA);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		}
		return prepareResponse.prepareFailedResponse(AppConstants.FAILED_STATUS);
	}

}
