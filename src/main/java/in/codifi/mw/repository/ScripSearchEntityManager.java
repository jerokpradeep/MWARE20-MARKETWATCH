/**
 * 
 */
package in.codifi.mw.repository;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.persistence.Query;
import javax.sql.DataSource;

import in.codifi.mw.model.ScripSearchResp;
import in.codifi.mw.model.SearchResultModel;
import in.codifi.mw.model.SearchScripReqModel;
import in.codifi.mw.util.CommonUtils;
import in.codifi.mw.util.StringUtil;
import io.quarkus.logging.Log;

/**
 * @author Vicky
 *
 */
@ApplicationScoped
public class ScripSearchEntityManager {
	@Named("mw")
	@Inject
	EntityManager entityManager;

	@Inject
	CommonUtils commonUtils;

//	@Named("mw")
//	@Inject
//	DataSource dataSource;

	@SuppressWarnings("unchecked")
	@Transactional
	public List<ScripSearchResp> getScrips(SearchScripReqModel reqModel) {
		List<ScripSearchResp> respone = new ArrayList<>();
		try {

			String currentPage = StringUtil.isNullOrEmpty(reqModel.getCurrentPage().trim()) ? "1"
					: reqModel.getCurrentPage().trim();
			String pageSize = StringUtil.isNullOrEmpty(reqModel.getPageSize().trim()) ? "50"
					: reqModel.getPageSize().trim();
			int offset = (Integer.parseInt(currentPage) - 1) * Integer.parseInt(pageSize);
			String symbol = reqModel.getSearchText().trim();
			String[] exch = reqModel.getExchange();
			if (exch == null || exch.length <= 0) {
				return respone;
			}
			if (Arrays.stream(exch).anyMatch("all"::equalsIgnoreCase)) {
				exch = null;
			}
			String stringQuery = "";
			String questionCount = "";
			String whereClause = "";
			String caseCondition = "";
			if (exch != null && exch.length > 0) {
				List<String> exchList = new ArrayList<String>(Arrays.asList(exch));
				exch = exchList.toArray(new String[0]);
				String ques = "";
				for (int i = 0; i < exch.length; i++) {
					ques = ques + "?,";
				}
				questionCount = ques.substring(0, ques.length() - 1);
			}

			String sqlQuery1 = "SELECT exch, exchange_segment, group_name, symbol, token, instrument_type, formatted_ins_name,week_tag,company_name,expiry_date,option_type,isin FROM tbl_global_contract_master_details ";
			String sqlQuery2 = " sort_order_1, symbol, expiry_date, strike_price, sort_order_2, sort_order_3 limit "
					+ pageSize + " OFFSET " + offset + "";

			/**
			 * To Add no of question mark in where condition base of exchange. If exchange
			 * is all no need to put in where condition
			 **/
			String[] keys = symbol.trim().split(" ");
			if (exch != null && exch.length > 0) {
				whereClause = " WHERE active_status = ? and exch IN(" + questionCount + ")";
			} else {
				whereClause = " WHERE active_status = ? ";
			}

			if (keys != null && keys.length > 0) {
				String tempWhereClause = "";
				String tempCaseClause = "";

				/**
				 * To add where class based on search key If search key is like 'NIFTY BANK',
				 * check in instrument_name column else search key is like 'NIFTY', check in
				 * symbol column
				 **/

				if (keys.length == 1 && keys[0] != null && keys[0].trim().length() < 4) {
					tempWhereClause = tempWhereClause + " and (symbol like '" + keys[0] + "%'  or company_name like '"
							+ keys[0] + "%' )";
				} else {
					for (String tempSymbol : keys) {
						/** Changed to get company name in search **/
						tempWhereClause = tempWhereClause + " and ( instrument_name like '%" + tempSymbol
								+ "%' or symbol like '" + tempSymbol + "%' or company_name like '%" + tempSymbol
								+ "%')";
					}
				}
				if (keys[0] != null && keys[0].trim().length() > 0) {
					/** Changed to get company name in search **/
					tempCaseClause = " case" + tempCaseClause + " when symbol like '" + keys[0] + "%' then -1 ";
					tempCaseClause = tempCaseClause + " when instrument_name like '" + keys[0] + "%' then 0 ";
					tempCaseClause = tempCaseClause + " when company_name like '" + keys[0] + "%' then 1 else 3 end ,";
				}
				whereClause = whereClause + tempWhereClause;
				caseCondition = caseCondition + tempCaseClause;
			}

			stringQuery = sqlQuery1 + whereClause + " ORDER BY  " + caseCondition + sqlQuery2;

			System.out.println(stringQuery);

			Query query = entityManager.createNativeQuery(stringQuery);
			/** set param position **/
			int paramPosition = 1;
			query.setParameter(paramPosition++, 1);
			if (exch != null && exch.length > 0) {
				for (int i = 0; i < exch.length; i++) {
					query.setParameter(paramPosition++, exch[i]);
				}
			}

			List<Object[]> result = query.getResultList();
			for (Object[] object : result) {
				ScripSearchResp model = new ScripSearchResp();
				String exchange = String.valueOf(object[0]);
				String segment = String.valueOf(object[1]);
				String groupName = String.valueOf(object[2]);
				String resSymbol = String.valueOf(object[3]);
				String token = String.valueOf(object[4]);
				String insType = String.valueOf(object[5]);
				String formattedInsName = String.valueOf(object[6]);
				String weekTag = String.valueOf(object[7]);
				String companyName = String.valueOf(object[8]);
				if (object[9] != null) {
					Date expiry = (Date) object[9];
					model.setExpiry(expiry);
				}
				String optiontype = String.valueOf(object[10]);
				String isin = String.valueOf(object[11]);

				String exchangeIifl = commonUtils.getExchangeNameIIFL(exchange);
				model.setExchange(exchangeIifl);
				String segmentIifl = commonUtils.getExchangeName(segment);
				model.setSegment(segmentIifl);
				model.setToken(token);
				model.setFormattedInsName(formattedInsName);
				model.setWeekTag(weekTag);
				model.setCompanyName(companyName);
				model.setOptionType(optiontype);
				model.setIsFnOAvailable(exchange.equalsIgnoreCase("NFO") ? "true" : "false");

				model.setSeries(groupName);
				model.setIsin(isin);

				if (exchange.equalsIgnoreCase("NSE")) {
					model.setSymbol(resSymbol + "-" + groupName);
				} else {
					model.setSymbol(resSymbol);
				}
				if ((exchange.equalsIgnoreCase("NSE") || exchange.equalsIgnoreCase("BSE")
						|| exchange.equalsIgnoreCase("MCX")) && StringUtil.isNotNullOrEmpty(insType)
						&& insType.equalsIgnoreCase("INDEX")) {
					model.setSegment("INDEX");
					model.setSymbol(resSymbol);
				}
				respone.add(model);
			}

		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		}
		return respone;
	}

	public String getScripsCount(SearchScripReqModel reqModel) {
		List<ScripSearchResp> respone = new ArrayList<>();
		String totalCount = "0";
		try {

			String symbol = reqModel.getSearchText().trim();
			String[] exch = reqModel.getExchange();
			if (exch == null || exch.length <= 0) {
				return totalCount;
			}
			if (Arrays.stream(exch).anyMatch("all"::equalsIgnoreCase)) {
				exch = null;
			}
			String stringQuery = "";
			String questionCount = "";
			String whereClause = "";
			String caseCondition = "";
			if (exch != null && exch.length > 0) {
				List<String> exchList = new ArrayList<String>(Arrays.asList(exch));
				exch = exchList.toArray(new String[0]);
				String ques = "";
				for (int i = 0; i < exch.length; i++) {
					ques = ques + "?,";
				}
				questionCount = ques.substring(0, ques.length() - 1);
			}

			String sqlQuery1 = "SELECT count(*) as totalCount  FROM tbl_global_contract_master_details ";
			String sqlQuery2 = " sort_order_1, symbol, expiry_date, strike_price, sort_order_2, sort_order_3 ";

			/**
			 * To Add no of question mark in where condition base of exchange. If exchange
			 * is all no need to put in where condition
			 **/
			String[] keys = symbol.trim().split(" ");
			if (exch != null && exch.length > 0) {
				whereClause = " WHERE active_status = ? and exch IN(" + questionCount + ")";
			} else {
				whereClause = " WHERE active_status = ? ";
			}

			if (keys != null && keys.length > 0) {
				String tempWhereClause = "";
				String tempCaseClause = "";

				/**
				 * To add where class based on search key If search key is like 'NIFTY BANK',
				 * check in instrument_name column else search key is like 'NIFTY', check in
				 * symbol column
				 **/

				if (keys.length == 1 && keys[0] != null && keys[0].trim().length() < 4) {
					tempWhereClause = tempWhereClause + " and (symbol like '" + keys[0] + "%'  or company_name like '"
							+ keys[0] + "%' )";
				} else {
					for (String tempSymbol : keys) {
						/** Changed to get company name in search **/
						tempWhereClause = tempWhereClause + " and ( instrument_name like '%" + tempSymbol
								+ "%' or symbol like '" + tempSymbol + "%' or company_name like '%" + tempSymbol
								+ "%')";
					}
				}
				if (keys[0] != null && keys[0].trim().length() > 0) {
					/** Changed to get company name in search **/
					tempCaseClause = " case" + tempCaseClause + " when symbol like '" + keys[0] + "%' then -1 ";
					tempCaseClause = tempCaseClause + " when instrument_name like '" + keys[0] + "%' then 0 ";
					tempCaseClause = tempCaseClause + " when company_name like '" + keys[0] + "%' then 1 else 3 end ,";
				}
				whereClause = whereClause + tempWhereClause;
				caseCondition = caseCondition + tempCaseClause;
			}

			stringQuery = sqlQuery1 + whereClause + " ORDER BY  " + caseCondition + sqlQuery2;

			System.out.println(stringQuery);

			Query query = entityManager.createNativeQuery(stringQuery);
			/** set param position **/
			int paramPosition = 1;
			query.setParameter(paramPosition++, 1);
			if (exch != null && exch.length > 0) {
				for (int i = 0; i < exch.length; i++) {
					query.setParameter(paramPosition++, exch[i]);
				}
			}

//			List<Object[]> result = query.getResultList();
//			for (Object[] object : result) {
//			    // Check the type of object[0]
//			    if (object[0] instanceof BigInteger) {
//			        // Convert BigInteger to String
//			        totalCount = ((BigInteger) object[0]).toString();
//			    } else {
//			        // If it's not BigInteger, just use String.valueOf() (or other handling logic)
//			        totalCount = String.valueOf(object[0]);
//			    }
//			}

			List<?> result = query.getResultList();
			for (Object item : result) {
				if (item instanceof BigInteger) {
					totalCount = ((BigInteger) item).toString();
				} else if (item instanceof Object[]) {
					Object[] objectArray = (Object[]) item;
					totalCount = objectArray[0] instanceof BigInteger ? ((BigInteger) objectArray[0]).toString()
							: String.valueOf(objectArray[0]);
				} else {
					totalCount = String.valueOf(item);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		}
		return totalCount;
	}

}
