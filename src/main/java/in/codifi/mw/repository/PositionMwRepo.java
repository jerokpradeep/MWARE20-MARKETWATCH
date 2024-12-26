/**
 * 
 */
package in.codifi.mw.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import in.codifi.mw.entity.PositionDataMwEntity;

/**
 * @author Vicky
 *
 */
public interface PositionMwRepo extends JpaRepository<PositionDataMwEntity, Long>{

	List<PositionDataMwEntity> findByUserId(String userId);

}
