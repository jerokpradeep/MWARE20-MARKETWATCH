/**
 * 
 */
package in.codifi.mw.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import in.codifi.mw.entity.secondary.RecentlyViewedEntity;


/**
 * @author Vicky
 *
 */
public interface RecentlyViewedRepository extends JpaRepository<RecentlyViewedEntity, Long> {

	List<RecentlyViewedEntity> findAllByUserIdOrderBySortOrderAsc(String userId);

	void deleteAllByUserId(String pUserId);
}
