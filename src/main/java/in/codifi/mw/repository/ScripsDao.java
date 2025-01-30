/**
 * 
 */
package in.codifi.mw.repository;

import javax.enterprise.context.ApplicationScoped;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;

import in.codifi.mw.config.HazelcastConfig;
import io.quarkus.logging.Log;

/**
 * @author Vicky
 *
 */
@ApplicationScoped
public class ScripsDao {

	@Inject
	DataSource dataSource;

	
	/**
	 * To get symbol name
	 * 
	 * @author Dinesh Kumar
	 *
	 * @param symbolLength
	 */
	public void loadDistintValue(int symbolLength) {
		List<String> result = null;
		PreparedStatement pStmt = null;
		Connection conn = null;
		ResultSet rSet = null;
		String tempString = "";
		try {
			conn = dataSource.getConnection();
			pStmt = conn.prepareStatement(
					"Select distinct(symbol) as symbol from tbl_global_contract_master_details where CHAR_LENGTH(symbol) = ?");
			int paramPos = 1;
			pStmt.setInt(paramPos++, symbolLength);
			rSet = pStmt.executeQuery();
			if (rSet != null) {
				result = new ArrayList<>();
				while (rSet.next()) {
					tempString = rSet.getString("symbol");
					result.add(tempString.toUpperCase());
				}
			}
			if (result != null && !result.isEmpty()) {
				HazelcastConfig.getInstance().getDistinctSymbols().put(symbolLength, result);
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
	}

}
