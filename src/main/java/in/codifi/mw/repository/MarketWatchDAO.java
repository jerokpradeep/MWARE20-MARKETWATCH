
package in.codifi.mw.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.json.simple.JSONObject;

import in.codifi.cache.model.ContractMasterModel;
import in.codifi.mw.cache.HazelCacheController;
import in.codifi.mw.config.ApplicationProperties;
import in.codifi.mw.entity.MarketWatchNameDTO;
import in.codifi.mw.entity.MarketWatchScripDetailsDTO;
import in.codifi.mw.model.CacheMwDetailsModel;
import in.codifi.mw.model.IndexModel;
import in.codifi.mw.model.MwRequestModel;
import in.codifi.mw.model.MwScripModel;
import in.codifi.mw.model.badgeModel;
import in.codifi.mw.util.CommonUtils;
import in.codifi.mw.util.StringUtil;
import io.quarkus.logging.Log;

/**
 * @author Vicky
 *
 */
@ApplicationScoped
public class MarketWatchDAO {

	@Inject
	DataSource dataSource;

	@Named("mw")
	@Inject
	DataSource entityManager;

	@Inject
	CommonUtils commonUtils;

	@Inject
	ApplicationProperties properties;

	/**
	 * Method to find mw name by user id
	 * 
	 * @author Vicky
	 * @param pUserId
	 * @return
	 */
	public List<MarketWatchNameDTO> findAllByUserId(String userId) {
		List<MarketWatchNameDTO> response = new ArrayList<>();
		PreparedStatement pStmt = null;
		Connection conn = null;
		ResultSet rSet = null;
		try {
			conn = dataSource.getConnection();
			String query = "SELECT id, mw_name,user_id,(case when mw_id is null then 0 else mw_id end) as mw_id  FROM tbl_market_watch_list where user_id = ?";
			pStmt = conn.prepareStatement(query);
			int paramPos = 1;
			pStmt.setString(paramPos++, userId);
			rSet = pStmt.executeQuery();
			if (rSet != null) {
				while (rSet.next()) {
					MarketWatchNameDTO model = new MarketWatchNameDTO();
					model.setMwName(rSet.getString("mw_name"));
					model.setUserId(rSet.getString("user_id"));
					model.setMwId(rSet.getInt("mw_id"));
					response.add(model);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		} finally {
			try {
				if (rSet != null)
					rSet.close();
				if (pStmt != null)
					pStmt.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return response;
	}

	/**
	 * Method to get MarketWatch By UserId
	 * 
	 * @author Vicky
	 * @param newMwList
	 */
	public boolean insertMwName(List<MarketWatchNameDTO> mwNameDto) {
		PreparedStatement pStmt = null;
		Connection conn = null;
		int count = 1;
		try {
			conn = dataSource.getConnection();
			pStmt = conn.prepareStatement(
					"INSERT INTO tbl_market_watch_list (user_id,mw_id,mw_name,position) VALUES (?,?,?,?)");
			for (MarketWatchNameDTO dto : mwNameDto) {
				int paramPos = 1;
				pStmt.setString(paramPos++, dto.getUserId());
				pStmt.setInt(paramPos++, dto.getMwId());
				pStmt.setString(paramPos++, dto.getMwName());
				pStmt.setLong(paramPos++, dto.getPosition());
				count++;
				pStmt.addBatch();
				if (count == 1000) {
					pStmt.executeBatch();
					count = 1;
				}
			}
			if (count > 1) {
				pStmt.executeBatch();
			}
			Log.info("MW Name inserted");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		} finally {
			try {
				if (pStmt != null)
					pStmt.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				e.printStackTrace();
				Log.error(e.getMessage());
			}
		}

		return false;
	}

	/**
	 * @author Vicky
	 * @param pUserId
	 * @return
	 */
	public List<CacheMwDetailsModel> getMarketWatchByUserId(String userId) {
		List<CacheMwDetailsModel> response = new ArrayList<>();

		PreparedStatement pStmt = null;
		Connection conn = null;
		ResultSet rSet = null;
		try {
			conn = dataSource.getConnection();
			String query = "SELECT A.mw_name, A.user_id,(case when A.mw_id is null then 0 else A.mw_id end) as mw_id,"
					+ " B.exch, B.exch_seg, B.token, B.symbol, B.trading_symbol, B.formatted_ins_name, B.expiry_date, B.pdc,"
					+ " (case when B.sorting_order is null then 0 else B.sorting_order end) as sorting_order"
					+ "  , B.week_tag FROM tbl_market_watch_list as A  LEFT JOIN tbl_market_watch_scrips B on  A.mw_id = B.mw_id and"
					+ " A.user_id = B.user_id where A.user_id = ? order by A.user_id, A.mw_id , B.sorting_order";
			pStmt = conn.prepareStatement(query);
			int paramPos = 1;
			pStmt.setString(paramPos++, userId);
			rSet = pStmt.executeQuery();
			if (rSet != null) {
				while (rSet.next()) {
					CacheMwDetailsModel model = new CacheMwDetailsModel();
					model.setMwName(rSet.getString("mw_name"));
					model.setUserId(rSet.getString("user_id"));
					model.setMwId(rSet.getInt("mw_id"));
					model.setExchange(commonUtils.getExchangeNameIIFL(rSet.getString("exch")));
					model.setSegment(commonUtils.getExchangeName(rSet.getString("exch_seg")));
					model.setToken(rSet.getString("token"));
					model.setSymbol(rSet.getString("symbol"));
					model.setTradingSymbol(rSet.getString("trading_symbol"));
					model.setFormattedInsName(rSet.getString("formatted_ins_name"));
					model.setExpiry(rSet.getDate("expiry_date") != null ? rSet.getDate("expiry_date").toString() : "");
					model.setPdc(rSet.getString("pdc"));
					model.setSortOrder(rSet.getInt("sorting_order"));
					model.setWeekTag(
							StringUtil.isNullOrEmpty(rSet.getString("week_tag")) ? "" : rSet.getString("week_tag"));
					badgeModel badge = new badgeModel();
					badge.setBnpl("");
					badge.setEvent(false);
					badge.setHoldingqty("0");
					badge.setIdeas("");
					model.setBadge(badge); // Correct if badgeModel is a class
					model.setScreeners(new ArrayList<String>());
					response.add(model);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		} finally {
			try {
				if (rSet != null)
					rSet.close();
				if (pStmt != null)
					pStmt.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return response;
	}

//	public List<CacheMwDetailsModel> getMarketWatchByUserId(String userId) {
//		List<CacheMwDetailsModel> response = new ArrayList<>();
//
//		PreparedStatement pStmt = null;
//		Connection conn = null;
//		ResultSet rSet = null;
//		try {
//			conn = dataSource.getConnection();
//			String query = "SELECT A.mw_name, A.user_id,(case when A.mw_id is null then 0 else A.mw_id end) as mw_id,"
//					+ " B.exch, B.token, B.expiry_date,"
//					+ " (case when B.sorting_order is null then 0 else B.sorting_order end) as sorting_order"
//					+ " FROM tbl_market_watch_list as A  LEFT JOIN tbl_market_watch_scrips B on  A.mw_id = B.mw_id and"
//					+ " A.user_id = B.user_id where A.user_id = ? order by A.user_id, A.mw_id , B.sorting_order";
//			pStmt = conn.prepareStatement(query);
//			int paramPos = 1;
//			pStmt.setString(paramPos++, userId);
//			rSet = pStmt.executeQuery();
//			if (rSet != null) {
//				while (rSet.next()) {
//					CacheMwDetailsModel model = new CacheMwDetailsModel();
//					model.setMwName(rSet.getString("mw_name"));
//					model.setUserId(rSet.getString("user_id"));
//					model.setMwId(rSet.getInt("mw_id"));
//					model.setExchange(rSet.getString("exch"));
//					model.setToken(rSet.getString("token"));
//					model.setExpiry(rSet.getDate("expiry_date"));
//					model.setSortOrder(rSet.getInt("sorting_order"));
//					response.add(model);
//				}
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			Log.error(e.getMessage());
//		} finally {
//			try {
//				if (rSet != null)
//					rSet.close();
//				if (pStmt != null)
//					pStmt.close();
//				if (conn != null)
//					conn.close();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		return response;
//	}

	/**
	 * @param mwName
	 * @param mwId
	 * @param userId
	 */
	public int updateMWName(String mwName, int mwId, String userId) {
		int isSuccessfull = 0;
		PreparedStatement pStmt = null;
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			pStmt = conn
					.prepareStatement("UPDATE tbl_market_watch_list SET mw_name = ? WHERE mw_id = ? AND user_id = ?");
			int paramPos = 1;
			pStmt.setString(paramPos++, mwName);
			pStmt.setInt(paramPos++, mwId);
			pStmt.setString(paramPos++, userId);
			pStmt.execute();
		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		} finally {
			try {

				if (pStmt != null)
					pStmt.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return isSuccessfull;
	}

	public String getMWName(int mwId, String userId) {
		String mwName = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			// Establishing the connection
			conn = dataSource.getConnection();

			// Corrected SQL query with proper FROM clause
			String query = "SELECT mw_name FROM tbl_market_watch_list WHERE mw_id = ? AND user_id = ?";
			pStmt = conn.prepareStatement(query);

			// Setting the parameters
			int paramPos = 1;
			pStmt.setInt(paramPos++, mwId);
			pStmt.setString(paramPos++, userId);

			// Execute query and get results
			rs = pStmt.executeQuery();

			// If there's a result, assign the mw_name to mwName
			if (rs.next()) {
				mwName = rs.getString("mw_name");
			}
		} catch (Exception e) {
			e.printStackTrace(); // Log the exception (optional, if using a logger)
			Log.error(e.getMessage());
		} finally {
			try {
				// Ensure resources are closed
				if (rs != null) {
					rs.close();
				}
				if (pStmt != null) {
					pStmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mwName; // Return the mw_name or null if not found
	}

	public boolean changeMwisDefaultStatus(int mwId, String userId, boolean status) {
		PreparedStatement pStmt = null;
		Connection conn = null;
		try {
			// Establishing the connection
			conn = dataSource.getConnection();

			// SQL query to update is_default based on the mw_id and user_id
			String query = "UPDATE tbl_market_watch_list SET is_default = ? WHERE mw_id = ? AND user_id = ?";
			pStmt = conn.prepareStatement(query);

			// Setting the parameters
			int paramPos = 1;
			pStmt.setInt(paramPos++, status ? 1 : 0); // Set 1 for true, 0 for false
			pStmt.setInt(paramPos++, mwId);
			pStmt.setString(paramPos++, userId);

			// Execute the update query and check how many rows were affected
			int rowsAffected = pStmt.executeUpdate(); // Execute update

			// Return true if the update was successful (1 or more rows affected), false
			// otherwise
			return rowsAffected > 0;

		} catch (Exception e) {
			e.printStackTrace(); // Log the exception (optional, if using a logger)
			Log.error(e.getMessage());
		} finally {
			try {
				// Ensure resources are closed
				if (pStmt != null) {
					pStmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false; // Return false if update fails
	}

	/**
	 * @param userId
	 * @param mwId
	 * @return
	 */
	public List<MarketWatchScripDetailsDTO> findAllByUserIdAndMwId(String userId, int mwId) {
		List<MarketWatchScripDetailsDTO> response = new ArrayList<>();

		PreparedStatement pStmt = null;
		Connection conn = null;
		ResultSet rSet = null;
		try {
			conn = dataSource.getConnection();
			String query = "SELECT token,exch,(case when sorting_order is null then 0 else sorting_order end) as sorting_order,id from tbl_market_watch_scrips where user_id = ? and mw_id = ?";
			pStmt = conn.prepareStatement(query);
			int paramPos = 1;
			pStmt.setString(paramPos++, userId);
			pStmt.setInt(paramPos++, mwId);
			rSet = pStmt.executeQuery();
			if (rSet != null) {
				while (rSet.next()) {
					MarketWatchScripDetailsDTO model = new MarketWatchScripDetailsDTO();
					model.setToken(rSet.getString("token"));
					model.setEx(rSet.getString("exch"));
					model.setSortingOrder(rSet.getInt("sorting_order"));
					model.setId(rSet.getLong("id"));
					response.add(model);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		} finally {
			try {
				if (rSet != null)
					rSet.close();
				if (pStmt != null)
					pStmt.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return response;
	}

	/**
	 * @param newScripDetails
	 * @param userId
	 * @param mwId
	 * @return
	 */
	public int updateMWScrips(List<MarketWatchScripDetailsDTO> scripsDto, String userId, int mwId) {
		int isSuccessfull = 0;
		PreparedStatement pStmt = null;
		Connection conn = null;
		int count = 1;
		try {
			conn = dataSource.getConnection();
			pStmt = conn.prepareStatement(
					"UPDATE tbl_market_watch_list SET sorting_order = ? where mw_id = ? and user_id = ? and id = ?");
			for (MarketWatchScripDetailsDTO dto : scripsDto) {
				int paramPos = 1;
				pStmt.setInt(paramPos++, dto.getSortingOrder());
				pStmt.setInt(paramPos++, dto.getMwId());
				pStmt.setString(paramPos++, dto.getUserId());
				pStmt.setLong(paramPos++, dto.getId());
				count++;
				pStmt.addBatch();
				if (count == 1000) {
					pStmt.executeBatch();
					count = 1;
				}
			}
			if (count > 1) {
				pStmt.executeBatch();
			}
			isSuccessfull = 1;
		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		} finally {
			try {
				pStmt.close();
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return isSuccessfull;
	}

	/**
	 * @param marketWatchNameDto
	 */
	public boolean insertMwData(List<MarketWatchScripDetailsDTO> mwScripsDto) {
		PreparedStatement pStmt = null;
		Connection conn = null;
		int count = 1;
		try {
			conn = dataSource.getConnection();
			pStmt = conn.prepareStatement("INSERT INTO tbl_market_watch_scrips (user_id,mw_id,token,alter_token,exch,"
					+ "exch_seg,trading_symbol,formatted_ins_name,group_name,instrument_type,expiry_date,lot_size,"
					+ "option_type,pdc,sorting_order,strike_price,symbol,tick_size,week_tag) "
					+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			for (MarketWatchScripDetailsDTO dto : mwScripsDto) {
				int paramPos = 1;
				pStmt.setString(paramPos++, dto.getUserId());
				pStmt.setInt(paramPos++, dto.getMwId());
				pStmt.setString(paramPos++, dto.getToken());
				pStmt.setString(paramPos++, dto.getAlterToken());
				pStmt.setString(paramPos++, dto.getEx());
				pStmt.setString(paramPos++, dto.getExSeg());
				pStmt.setString(paramPos++, dto.getTradingSymbol());
				pStmt.setString(paramPos++, dto.getFormattedName());
				pStmt.setString(paramPos++, dto.getGroupName());
				pStmt.setString(paramPos++, dto.getInstrumentType());
				if (dto.getExpDt() != null) {
					java.sql.Date sqldate = (java.sql.Date) dto.getExpDt();
					pStmt.setDate(paramPos++, sqldate);
				} else {
					pStmt.setDate(paramPos++, null);
				}
				pStmt.setString(paramPos++, dto.getLotSize());
				pStmt.setString(paramPos++, dto.getOptionType());
				pStmt.setString(paramPos++, dto.getPdc());
				pStmt.setInt(paramPos++, dto.getSortingOrder());
				pStmt.setString(paramPos++, dto.getStrikePrice());
				pStmt.setString(paramPos++, dto.getSymbol());
				pStmt.setString(paramPos++, dto.getTickSize());
				pStmt.setString(paramPos++, dto.getWeekTag());
				count++;
				pStmt.addBatch();
				if (count == 1000) {
					pStmt.executeBatch();
					count = 1;
				}
			}
			if (count > 1) {
				pStmt.executeBatch();
			}
			Log.info("MW scrips inserted");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		} finally {
			try {

				if (pStmt != null)
					pStmt.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				e.printStackTrace();
				Log.error(e.getMessage());
			}
		}

		return false;
	}

	/**
	 * @param pUserId
	 * @param exch
	 * @param token
	 * @param userMwId
	 */
	public long deleteScripFomDataBase(String pUserId, String exch, String token, int mwId) {
		long resp = 0;
		PreparedStatement pStmt = null;
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			pStmt = conn.prepareStatement(
					"DELETE FROM tbl_market_watch_scrips  WHERE mw_id = ? and user_id = ? and token = ? and exch = ? ");
			int paramPos = 1;
			pStmt.setInt(paramPos++, mwId);
			pStmt.setString(paramPos++, pUserId);
			pStmt.setString(paramPos++, token);
			pStmt.setString(paramPos++, exch);
			pStmt.execute();
			resp = 1;
			return resp;
		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		} finally {
			try {
				if (pStmt != null)
					pStmt.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return resp;
	}

	/**
	 * @param pDto
	 * @param userId
	 * @return
	 */
	public String selectByUserId(MwRequestModel pDto, String userId) {
		String response = "";

		PreparedStatement pStmt = null;
		Connection conn = null;
		ResultSet rSet = null;
		try {
			conn = dataSource.getConnection();
			String query = "SELECT id,symbol from tbl_market_watch_scrips  WHERE mw_id = ? and user_id = ? and token = ? and exch = ?";
			pStmt = conn.prepareStatement(query);
			int paramPos = 1;
			pStmt.setInt(paramPos++, pDto.getMwId());
			pStmt.setString(paramPos++, userId);
			pStmt.setString(paramPos++, pDto.getScripData().get(0).getToken().trim());
			pStmt.setString(paramPos++,
					commonUtils.getExchangeNameContract(pDto.getScripData().get(0).getExchange().trim()));
			rSet = pStmt.executeQuery();
			if (rSet != null) {
				while (rSet.next()) {
					response = rSet.getString("symbol");
					System.out.println(response);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		} finally {
			try {
				if (rSet != null)
					rSet.close();
				if (pStmt != null)
					pStmt.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return response;
	}

	/**
	 * @param userId
	 * @return
	 */
	public long checkUserId(String userId) {
		long response = 0;

		PreparedStatement pStmt = null;
		Connection conn = null;
		ResultSet rSet = null;
		try {
			conn = dataSource.getConnection();
			String query = "SELECT id from tbl_market_watch_list  WHERE user_id = ?";
			pStmt = conn.prepareStatement(query);
			int paramPos = 1;
			pStmt.setString(paramPos++, userId);
			rSet = pStmt.executeQuery();
			if (rSet != null) {
				while (rSet.next()) {
					response = rSet.getLong("id");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		} finally {
			try {
				if (rSet != null)
					rSet.close();
				if (pStmt != null)
					pStmt.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return response;
	}

	/***
	 * Method to get IndicesList
	 * 
	 * @author Vicky
	 * 
	 * @return
	 */
	public List<IndexModel> getIndicesList() {
		List<IndexModel> response = new ArrayList<>();

		PreparedStatement pStmt = null;
		Connection conn = null;
		ResultSet rSet = null;
		try {
			conn = entityManager.getConnection();
			String query = "SELECT pdc,exch,exchange_segment,symbol,token FROM tbl_global_contract_master_details where instrument_type ='INDEX' ";
			pStmt = conn.prepareStatement(query);
			rSet = pStmt.executeQuery();
			if (rSet != null) {
				while (rSet.next()) {
					IndexModel model = new IndexModel();
					model.setClosingIndex(rSet.getString("pdc"));
					if (properties.isExchfull()) {
						String exchangeIifl = commonUtils.getExchangeNameIIFL(rSet.getString("exch"));
						model.setExchange(exchangeIifl);
						String segmentIifl = commonUtils.getExchangeName(rSet.getString("exchange_segment"));
						model.setSegment(segmentIifl);
					} else {
						model.setExchange(rSet.getString("exch"));
						model.setSegment(rSet.getString("exchange_segment"));
					}

					model.setIndexName(rSet.getString("symbol"));
					model.setIndexValue("");
					model.setIndiceID(rSet.getString("token"));
					response.add(model);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		} finally {
			try {
				if (rSet != null)
					rSet.close();
				if (pStmt != null)
					pStmt.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return response;
	}

	/***
	 * Get Contract Data
	 */
	public void loadContract() {

		PreparedStatement pStmt = null;
		Connection conn = null;
		ResultSet rSet = null;
		try {
			// Get database connection
			conn = entityManager.getConnection();

			// SQL query to fetch data from the table
			String query = "SELECT * FROM tbl_global_contract_master_details where exch='NSE'";

			// Prepare the SQL statement
			pStmt = conn.prepareStatement(query);

			// Execute the query and get the result set
			rSet = pStmt.executeQuery();

			// Check if the result set has any data
			if (rSet != null) {
				while (rSet.next()) {
					// Create a new model object for each row
					ContractMasterModel model = new ContractMasterModel();
					// Set the fields of the model using values from the ResultSet
					model.setAlterToken(rSet.getString("alter_token"));
					model.setCompanyName(rSet.getString("company_name"));
					model.setExch(rSet.getString("exch"));
					model.setExpiry(rSet.getDate("expiry_date"));
					model.setFormattedInsName(rSet.getString("formatted_ins_name"));
					model.setFreezQty(rSet.getString("freeze_qty"));
					model.setGroupName(rSet.getString("group_name"));
					model.setInstrumentName(rSet.getString("instrument_name"));
					model.setInsType(rSet.getString("instrument_type"));
					model.setIsin(rSet.getString("isin"));
					model.setLotSize(rSet.getString("lot_size"));
					model.setOptionType(rSet.getString("option_type"));
					model.setPdc(rSet.getString("pdc"));
					model.setSegment(rSet.getString("exchange_segment"));
					model.setStrikePrice(rSet.getString("strike_price"));
					model.setSymbol(rSet.getString("symbol"));
					model.setTickSize(rSet.getString("tick_size"));
					model.setToken(rSet.getString("token"));
					model.setTradingSymbol(rSet.getString("trading_symbol"));
					model.setWeekTag(rSet.getString("week_tag"));
					System.out.println(rSet.getString("exch").toUpperCase() + "_" + rSet.getString("token"));
					// Save the model in HazelCache
					HazelCacheController.getInstance().getContractMaster()
							.put(rSet.getString("exch").toUpperCase() + "_" + rSet.getString("token"), model);
				}
			}

		} catch (Exception e) {
			// Log the exception
			e.printStackTrace();
			Log.error(e.getMessage());
		} finally {
			// Clean up resources
			try {
				if (rSet != null)
					rSet.close();
				if (pStmt != null)
					pStmt.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @author Vicky
	 * 
	 *         Get Recently Viewed Script Count
	 * 
	 * @param mwId
	 * @param userId
	 * @return
	 */
	public int getRecentlyViewedScripsCount(int mwId, String userId) {
		int response = 0;

		PreparedStatement pStmt = null;
		Connection conn = null;
		ResultSet rSet = null;
		try {
			conn = dataSource.getConnection();
			String query = "SELECT count(*)as totalcount from tbl_market_watch_scrips  WHERE user_id = ? and mw_id = ?";
			pStmt = conn.prepareStatement(query);
			int paramPos = 1;
			pStmt.setString(paramPos++, userId);
			pStmt.setInt(paramPos++, mwId);
			rSet = pStmt.executeQuery();
			if (rSet != null) {
				while (rSet.next()) {
					response = rSet.getInt("totalcount");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		} finally {
			try {
				if (rSet != null)
					rSet.close();
				if (pStmt != null)
					pStmt.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return response;
	}

	/**
	 * @param mwId
	 * @param userId
	 * @return
	 */
	public boolean deleteFirstRecords(int mwId, String userId) {
		boolean response = false;
		PreparedStatement pStmt = null;
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			pStmt = conn.prepareStatement(
					"DELETE FROM market_watch.tbl_market_watch_scrips  where user_id = ? and mw_id = ? ORDER BY id ASC LIMIT 1");
			int paramPos = 1;
			pStmt.setString(paramPos++, userId);
			pStmt.setInt(paramPos++, mwId);
			pStmt.execute();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		} finally {
			try {

				if (pStmt != null)
					pStmt.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return response;
	}

	/**
	 * @param mwId
	 * @param userId
	 * @return
	 */
	public List<MwScripModel> getFirstRecords(int mwId, String userId) {
		List<MwScripModel> response = new ArrayList<>();
		PreparedStatement pStmt = null;
		Connection conn = null;
		ResultSet rSet = null;
		try {
			conn = dataSource.getConnection();
			String query = "SELECT exch, token FROM market_watch.tbl_market_watch_scrips  where user_id = ? and mw_id = ? ORDER BY id ASC LIMIT 1 ";
			pStmt = conn.prepareStatement(query);
			int paramPos = 1;
			pStmt.setString(paramPos++, userId);
			pStmt.setInt(paramPos++, mwId);
			rSet = pStmt.executeQuery();
			if (rSet != null) {
				while (rSet.next()) {
					MwScripModel model = new MwScripModel();
					if (properties.isExchfull()) {
						String exchangeIifl = commonUtils.getExchangeNameIIFL(rSet.getString("exch"));
						model.setExchange(exchangeIifl);
						model.setToken(rSet.getString("token"));
					} else {
						model.setExchange(rSet.getString("exch"));
						model.setToken(rSet.getString("token"));
					}

					response.add(model);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		} finally {
			try {
				if (rSet != null)
					rSet.close();
				if (pStmt != null)
					pStmt.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return response;
	}

	/**
	 * @param mwId
	 * @param userId
	 * @return
	 */
//	@SuppressWarnings("unchecked")
//	public JSONObject getFirstRecords(int mwId, String userId) {
//		JSONObject response = null;
//		Connection conn = null;
//		PreparedStatement pStmt = null;
//		ResultSet rSet = null;
//		try {
//			conn = dataSource.getConnection();
//			pStmt = conn.prepareStatement(
//					" SELECT exch, token FROM market_watch.tbl_market_watch_scrips  where user_id = ? and mw_id = ? ORDER BY id ASC LIMIT 1 ");
//			int paramPos = 1;
//			pStmt.setString(paramPos++, userId);
//			pStmt.setInt(paramPos++, mwId);
//			rSet = pStmt.executeQuery();
//			if (rSet != null) {
//				response = new JSONObject();
//				while (rSet.next()) {
//					if (properties.isExchfull()) {
//						String exchangeIifl = commonUtils.getExchangeNameIIFL(rSet.getString("exch"));
//						response.put("exchange", exchangeIifl);
//						response.put("token", rSet.getString("token"));
//					} else {
//						response.put("exchange", rSet.getString("exch"));
//						response.put("token", rSet.getString("token"));
//					}
//
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				if (pStmt != null)
//					pStmt.close();
//				if (conn != null)
//					conn.close();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		return response;
//	}

}
