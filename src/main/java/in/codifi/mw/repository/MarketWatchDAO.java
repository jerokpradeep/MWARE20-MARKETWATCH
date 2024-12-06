
package in.codifi.mw.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;

import in.codifi.mw.entity.MarketWatchNameDTO;
import in.codifi.mw.entity.MarketWatchScripDetailsDTO;
import in.codifi.mw.model.CacheMwDetailsModel;
import in.codifi.mw.model.MwRequestModel;
import io.quarkus.logging.Log;

/**
 * @author Vicky
 *
 */
@ApplicationScoped
public class MarketWatchDAO {
	
	@Inject
	DataSource dataSource;

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
					model.setId(rSet.getLong("id"));
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
					+ " FROM tbl_market_watch_list as A  LEFT JOIN tbl_market_watch_scrips B on  A.mw_id = B.mw_id and"
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
					model.setExchange(rSet.getString("exch"));
					model.setSegment(rSet.getString("exch_seg"));
					model.setToken(rSet.getString("token"));
					model.setSymbol(rSet.getString("symbol"));
					model.setTradingSymbol(rSet.getString("trading_symbol"));
					model.setFormattedInsName(rSet.getString("formatted_ins_name"));
					model.setExpiry(rSet.getDate("expiry_date"));
					model.setPdc(rSet.getString("pdc"));
					model.setSortOrder(rSet.getInt("sorting_order"));
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
			String query = "SELECT token,exch,(case when sorting_order is null then 0 else sorting_order end) as sorting_order,id from tbl_market_watch_list where user_id = ? and mw_id = ?";
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
	public long selectByUserId(MwRequestModel pDto, String userId) {
		long response = 0;

		PreparedStatement pStmt = null;
		Connection conn = null;
		ResultSet rSet = null;
		try {
			conn = dataSource.getConnection();
			String query = "SELECT id from tbl_market_watch_scrips  WHERE mw_id = ? and user_id = ? and token = ? and exch = ?";
			pStmt = conn.prepareStatement(query);
			int paramPos = 1;
			pStmt.setInt(paramPos++, pDto.getMwId());
			pStmt.setString(paramPos++, userId);
			pStmt.setString(paramPos++, pDto.getScripData().get(0).getToken().trim());
			pStmt.setString(paramPos++, pDto.getScripData().get(0).getExch().trim());
			rSet = pStmt.executeQuery();
			if (rSet != null) {
				while (rSet.next()) {
					response = rSet.getLong("id");
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

}
