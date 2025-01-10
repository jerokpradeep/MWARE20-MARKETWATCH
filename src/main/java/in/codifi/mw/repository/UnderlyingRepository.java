/**
 * 
 */
package in.codifi.mw.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import in.codifi.mw.entity.primary.UnderlyingEntity;

/**
 * @author Vicky
 *
 */
public interface UnderlyingRepository extends JpaRepository<UnderlyingEntity, Long>{

}
