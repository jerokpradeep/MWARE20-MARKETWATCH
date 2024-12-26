/**
 * 
 */
package in.codifi.mw.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.codifi.mw.entity.PredefinedMwEntity;

/**
 * @author Vicky
 *
 */
public interface PredefinedMwRepo extends JpaRepository<PredefinedMwEntity, Long> {

	PredefinedMwEntity findByMwNameAndMwId(String mwName, int mwId);

	PredefinedMwEntity findByMwId(int mwId);

	List<PredefinedMwEntity> findByMwName(@Param("mwName") String mwName);

	@Modifying
	@Query(value = " update TBL_PRE_DEFINED_MARKET_WATCH set MW_NAME = :mwName, UPDATED_BY = :updated_by WHERE MW_ID = :mwId ")
	int updateMWName(@Param("mwName") String mwName, @Param("updated_by") String updatedBy, @Param("mwId") long mwId);

	@Modifying
	@Query(value = " update TBL_PRE_DEFINED_MARKET_WATCH set IS_ENABLED = :isEnabled, UPDATED_BY = :updated_by WHERE MW_ID = :mwId ")
	int enableOrDisableMWName(@Param("isEnabled") int isEnabled, @Param("mwName") String mwName,
			@Param("updated_by") String updatedBy, @Param("mwId") long mwId);

}
