/**
 * 
 */
package in.codifi.mw.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import in.codifi.mw.entity.primary.PromptEntity;

/**
 * @author Vicky
 *
 */
public interface PromptRepository extends JpaRepository<PromptEntity, Long>{

}
