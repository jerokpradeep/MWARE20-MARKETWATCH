/**
 * 
 */
package in.codifi.mw.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import in.codifi.mw.entity.ContractEntity;

/**
 * @author Vicky
 *
 */
public interface ContractRepository  extends JpaRepository<ContractEntity, Long> {
	List<ContractEntity> findAllByInsType(@Param("insType") String insType);
}
