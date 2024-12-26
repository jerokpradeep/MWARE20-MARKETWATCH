/**
 * 
 */
package in.codifi.mw.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import in.codifi.mw.entity.HoldingsDataMwEntity;
import in.codifi.mw.entity.PredefinedMwEntity;

/**
 * @author Vicky
 *
 */
public interface HoldingsMwRepo extends JpaRepository<HoldingsDataMwEntity, Long>{

	List<HoldingsDataMwEntity> findByUserId(String userId);
}
