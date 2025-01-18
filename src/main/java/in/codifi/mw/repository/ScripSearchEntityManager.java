/**
 * 
 */
package in.codifi.mw.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;

import in.codifi.mw.config.ApplicationProperties;
import in.codifi.mw.model.ScripSearchResp;
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
	@Inject
	DataSource dataSource;

	@Inject
	CommonUtils commonUtils;

	@Inject
	ApplicationProperties properties;

	public List<ScripSearchResp> getScrips(SearchScripReqModel reqModel) {
		List<ScripSearchResp> respone = new ArrayList<>();
		List<String> adjustedExchangeList = new ArrayList<>();
		try {
			String currentPage = StringUtil.isNullOrEmpty(reqModel.getCurrentPage().trim()) ? "1"
					: reqModel.getCurrentPage().trim();
			String pageSize = StringUtil.isNullOrEmpty(reqModel.getPageSize().trim()) ? "50"
					: reqModel.getPageSize().trim();
			int offset = (Integer.parseInt(currentPage) - 1) * Integer.parseInt(pageSize);
			String symbol = reqModel.getSearchText().trim();

			if (reqModel.getExchange() == null || reqModel.getExchange().length <= 0) {
				return respone;
			}
			String[] exch = null;
			for (String exch1 : reqModel.getExchange()) {
				String adjustedExchange = "ALL";

				if (properties.isExchfull()) {
					switch (exch1.toUpperCase().trim()) {
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
					adjustedExchange = exch1.toUpperCase().trim();
				}
				adjustedExchangeList.add(adjustedExchange);
			}
			exch = adjustedExchangeList.toArray(new String[0]);
			if (Arrays.stream(exch).anyMatch("all"::equalsIgnoreCase)) {
				exch = null;
			}

			String questionCount = "";
			String whereClause = "";
			String caseCondition = "";
			if (exch != null && exch.length > 0) {
				String ques = "";
				for (int i = 0; i < exch.length; i++) {
					ques = ques + "?,";
				}
				questionCount = ques.substring(0, ques.length() - 1);
			}

			String sqlQuery1 = "SELECT exch, exchange_segment, group_name, symbol, token, instrument_type, formatted_ins_name,week_tag,company_name,expiry_date,option_type,isin FROM tbl_global_contract_master_details ";
			String sqlQuery2 = "CASE when symbol= 'NIFTY 50' THEN 1 WHEN trading_symbol LIKE 'BANK NIFTY INDEX' THEN 2 WHEN (formatted_ins_name LIKE 'NIFTY%' OR company_name LIKE 'NIFTY%' OR symbol= 'NIFTY%' ) and exchange_segment ='nse_idx' then 3 WHEN (formatted_ins_name LIKE 'BANK NIFTY%' OR company_name LIKE 'BANK NIFTY%' OR symbol= 'BANK NIFTY%' )  then 4 ELSE 5 END ,";
			String sqlQuery3 = " sort_order_1, sort_order_2, expiry_date, symbol,formatted_ins_name limit " + pageSize
					+ " OFFSET " + offset + "";

			String[] keys = symbol.trim().split(" ");
			if (exch != null && exch.length > 0) {
				whereClause = " WHERE active_status = ? and exch IN(" + questionCount + ")";
			} else {
				whereClause = " WHERE active_status = ? ";
			}

			if (keys != null && keys.length > 0) {
				String tempWhereClause = "";

				if (keys.length == 1 && keys[0] != null && keys[0].trim().length() < 4) {
					tempWhereClause = tempWhereClause + " and (symbol like '%" + keys[0] + "%'  or company_name like '%"
							+ keys[0] + "%' )";
				} else {
					for (String tempSymbol : keys) {
						tempWhereClause = tempWhereClause + " and ( formatted_ins_name like '%" + tempSymbol
								+ "%' or symbol like '%" + tempSymbol + "%' or company_name like '%" + tempSymbol
								+ "%')";
					}
				}

				caseCondition = caseCondition + tempWhereClause;
			}

			String stringQuery = sqlQuery1 + whereClause + caseCondition + " ORDER BY  " + sqlQuery2 + sqlQuery3;

			System.out.println("<<<<Query>>>>>" + stringQuery);

			try (Connection connection = dataSource.getConnection();
					PreparedStatement stmt = connection.prepareStatement(stringQuery)) {

				int paramPosition = 1;
				stmt.setInt(paramPosition++, 1);
				if (exch != null && exch.length > 0) {
					for (String exchange : exch) {
						stmt.setString(paramPosition++, exchange);
					}
				}

				try (ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						ScripSearchResp model = new ScripSearchResp();
						String exchange = rs.getString(1);
						String segment = rs.getString(2);
						String groupName = rs.getString(3);
						String resSymbol = rs.getString(4);
						String token = rs.getString(5);
						String insType = rs.getString(6);
						String formattedInsName = rs.getString(7);
						String weekTag = rs.getString(8);
						String companyName = rs.getString(9);
						Date expiry = rs.getDate(10);
						String optionType = rs.getString(11);
						String isin = rs.getString(12);

						if (properties.isExchfull()) {
							String exchangeIifl = commonUtils.getExchangeNameIIFL(exchange);
							model.setExchange(exchangeIifl);
							String segmentIifl = commonUtils.getExchangeName(segment);
							model.setSegment(segmentIifl);
						} else {
							model.setExchange(exchange);
							model.setSegment(segment);
						}

						model.setToken(token);
						model.setFormattedInsName(formattedInsName);
						model.setWeekTag(weekTag != null ? weekTag : "");
						model.setCompanyName(companyName != null ? companyName : "");
						model.setOptionType(optionType != null ? optionType : "");
						model.setSeries(groupName != null ? groupName : "");
						model.setIsin(isin != null ? isin : "");
						model.setExpiry(expiry != null ? expiry.toString() : "");

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
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		}
		return respone;
	}

	public String getScripsCount(SearchScripReqModel reqModel) {
		List<String> adjustedExchangeList = new ArrayList<>();
		String totalCount = "0";
		try {
			String symbol = reqModel.getSearchText().trim();
			if (reqModel.getExchange() == null || reqModel.getExchange().length <= 0) {
				return totalCount;
			}
			String[] exch = null;
			for (String exch1 : reqModel.getExchange()) {
				String adjustedExchange = "ALL";

				if (properties.isExchfull()) {
					switch (exch1.toUpperCase()) {
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
					adjustedExchange = exch1.toUpperCase();
				}
				adjustedExchangeList.add(adjustedExchange);
			}
			exch = adjustedExchangeList.toArray(new String[0]);
			if (Arrays.stream(exch).anyMatch("all"::equalsIgnoreCase)) {
				exch = null;
			}

			String questionCount = "";
			String whereClause = "";
			String caseCondition = "";
			if (exch != null && exch.length > 0) {
				String ques = "";
				for (int i = 0; i < exch.length; i++) {
					ques = ques + "?,";
				}
				questionCount = ques.substring(0, ques.length() - 1);
			}

			String sqlQuery1 = "SELECT count(*) as totalCount FROM tbl_global_contract_master_details ";
			String sqlQuery2 = " sort_order_1, symbol, expiry_date, strike_price, sort_order_2, sort_order_3 ";

			String[] keys = symbol.trim().split(" ");
			if (exch != null && exch.length > 0) {
				whereClause = " WHERE active_status = ? and exch IN(" + questionCount + ")";
			} else {
				whereClause = " WHERE active_status = ? ";
			}

			if (keys != null && keys.length > 0) {
				String tempWhereClause = "";
				String tempCaseClause = "";

				if (keys.length == 1 && keys[0] != null && keys[0].trim().length() < 4) {
					tempWhereClause = tempWhereClause + " and (symbol like '" + keys[0] + "%'  or company_name like '"
							+ keys[0] + "%' )";
				} else {
					for (String tempSymbol : keys) {
						tempWhereClause = tempWhereClause + " and ( instrument_name like '%" + tempSymbol
								+ "%' or symbol like '" + tempSymbol + "%' or company_name like '%" + tempSymbol
								+ "%')";
					}
				}
				if (keys[0] != null && keys[0].trim().length() > 0) {
					tempCaseClause = " case" + tempCaseClause + " when symbol like '" + keys[0] + "%' then -1 ";
					tempCaseClause = tempCaseClause + " when instrument_name like '" + keys[0] + "%' then 0 ";
					tempCaseClause = tempCaseClause + " when company_name like '" + keys[0] + "%' then 1 else 3 end ,";
				}
				whereClause = whereClause + tempWhereClause;
				caseCondition = caseCondition + tempCaseClause;
			}

			String stringQuery = sqlQuery1 + whereClause + " ORDER BY  " + caseCondition + sqlQuery2;

			System.out.println(stringQuery);

			try (Connection connection = dataSource.getConnection();
					PreparedStatement stmt = connection.prepareStatement(stringQuery)) {

				int paramPosition = 1;
				stmt.setInt(paramPosition++, 1);
				if (exch != null && exch.length > 0) {
					for (String exchange : exch) {
						stmt.setString(paramPosition++, exchange);
					}
				}

				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						totalCount = rs.getString(1);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		}
		return totalCount;
	}
}