/**
 * 
 */
package in.codifi.mw.repository;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import io.quarkus.logging.Log;
import in.codifi.mw.config.HazelcastConfig;
import in.codifi.mw.util.AppConstants;

/**
 * @author Vicky
 *
 */
@ApplicationScoped
public class ContractEntityManager {

	@Named("mw")
	@Inject
	EntityManager entityManager;

	/**
	 * 
	 */
	public void loadIsinByToken() {
		List<Object[]> result = null;
		try {
			Query query = entityManager.createNativeQuery(
					"SELECT isin,exch,token FROM tbl_global_contract_master_details where isin is not null and isin != '' and exch in('BSE','NSE')");
			result = query.getResultList();
			for (Object[] values : result) {
				HazelcastConfig.getInstance().getIsinByToken().put(values[0].toString(),
						values[1].toString() + "_" + values[2].toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param todayDate
	 * @return
	 */
	@Transactional
	public int deleteExpiredContract(String currentDate) {
		try {

			Query query = entityManager
					.createNativeQuery("DELETE FROM tbl_global_contract_master_details a where a.expiry_date < :date");
			query.setParameter("date", currentDate);
			int deleteCount = query.executeUpdate();
			Log.info("Expired Contract ->" + deleteCount + "-" + AppConstants.RECORD_DELETED);
			return deleteCount;
//			return prepareResponse.prepareSuccessMessage(deleteCount + "-" + AppConstants.RECORD_DELETED);
		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		}
		return -1;
	}

	/**
	 * 
	 */
	public void loadIsin() {
		List<Object[]> result = null;
		try {
			Query query = entityManager.createNativeQuery(
					"SELECT exch,isin,token FROM tbl_global_contract_master_details where isin is not null and isin != '' and exch in('BSE','NSE')");
			result = query.getResultList();
			for (Object[] values : result) {
				HazelcastConfig.getInstance().getIsinKB().put(values[0].toString() + "_" + values[1].toString(),
						values[2].toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
