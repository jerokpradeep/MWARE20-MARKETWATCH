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

import in.codifi.mw.cache.RedisConfig;
import in.codifi.mw.config.HazelcastConfig;

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
				String isin = values[0].toString();
	            String exchToken = values[1].toString() + "_" + values[2].toString();
	            
	            // Storing the isin and token mapping in Redis
	            RedisConfig.getInstance().getJedis().hset("isinByToken", isin, exchToken);
//				HazelcastConfig.getInstance().getIsinByToken().put(values[0].toString(),
//						values[1].toString() + "_" + values[2].toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
