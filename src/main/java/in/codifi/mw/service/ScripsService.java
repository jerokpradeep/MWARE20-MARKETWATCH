/**
 * 
 */
package in.codifi.mw.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.resteasy.reactive.RestResponse;
import org.json.simple.JSONObject;

import in.codifi.mw.config.HazelcastConfig;
import in.codifi.mw.model.ResponseModel;
import in.codifi.mw.model.ScripSearchResp;
import in.codifi.mw.model.SearchScripReqModel;
import in.codifi.mw.repository.ScripSearchEntityManager;
import in.codifi.mw.service.spec.ScripsServiceSpecs;
import in.codifi.mw.util.AppConstants;
import in.codifi.mw.util.CommonUtils;
import in.codifi.mw.util.PrepareResponse;
import in.codifi.mw.util.StringUtil;
import io.quarkus.logging.Log;

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

	/**
	 *
	 */
	@SuppressWarnings("unchecked")
	@Override
	public RestResponse<ResponseModel> getScrips(SearchScripReqModel reqModel) {
		List<ScripSearchResp> responses = new ArrayList<>();
		try {
			int tempPageSize = 0;
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

				if (reqModel.getSearchText().trim().length() < 3) {
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
				if (reqModel.getSearchText().trim().length() < 3) {
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
			int currentPage = 0;
			if (StringUtil.isNotNullOrEmptyAfterTrim(totalCount)) {
				currentPage = Integer.parseInt(totalCount) / tempPageSize;
			}

			if (responses != null && responses.size() > 0) {
				JSONObject finalOutput = new JSONObject();
				finalOutput.put("pageSize", totalCount);
				finalOutput.put("currentPage", currentPage);
				finalOutput.put("searchResult", responses);
				return prepareResponse.prepareSuccessResponseWithMessage(finalOutput, AppConstants.SUCCESS_STATUS,
						false);
			} else {
				return prepareResponse.prepareFailedResponse(AppConstants.NOT_FOUND);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		}
		return prepareResponse.prepareFailedResponse(AppConstants.FAILED_STATUS);
	}

	private List<ScripSearchResp> getSearchDetailsFromCache(SearchScripReqModel reqModel) {

		List<ScripSearchResp> responses = new ArrayList<>();
		/*
		 * Check the cache is not and storing is enabled or not
		 */
		String[] exchange = reqModel.getExchange();
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
					getLoadedSearchData.put(reqModel.getSearchText().trim().toUpperCase() + "_" + reqModel.getPageSize()
							+ "_" + reqModel.getCurrentPage(), responses);
				}
			}
		} else {
			responses = scripSearchRepo.getScrips(reqModel);
		}
		return responses;
	}

}
