package in.codifi.mw.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import in.codifi.mw.entity.PredefinedMwScripsEntity;

public interface PredefinedMwScripRepository extends JpaRepository <PredefinedMwScripsEntity, Long> {

	/**
	 * Method to delete Scrip by Token
	 * @author Vinitha
	 *@return
	 */
	void deleteByToken(String token);


	/**
	 * Method to find Scrip by Token
	 * @author Vinitha
	 *@return
	 */
	PredefinedMwScripsEntity findByToken(String token);


	List<PredefinedMwScripsEntity> findByMwId(Long mwId);


	PredefinedMwScripsEntity findByExchangeAndToken(String exchange, String token);


	PredefinedMwScripsEntity findByMwIdAndSortOrder(Long mwId , int sortOrder);


	PredefinedMwScripsEntity findByIdAndSortOrder(Long id, int sortOrder);
	

}
