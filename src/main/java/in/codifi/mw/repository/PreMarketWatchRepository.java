package in.codifi.mw.repository;

import in.codifi.mw.entity.*;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PreMarketWatchRepository extends JpaRepository <PredefinedMwEntity, Long> {
	
	/**
	 * Method to find By MwId
	 * @author Vinitha 
	 * @return
	 */
	PredefinedMwEntity findByMwId(Long mwId);
	
	/**
	 * Method to delete By MwId
	 * @author Vinitha 
	 * @return
	 */
	void deleteByMwId(Long mwId);

	/**
	 * Method to find By isEnabled
	 * @author Vinitha 
	 * @return
	 */	
	List<PredefinedMwEntity> findByIsEnabled(int i);

	/**
	 * Method to find record exist or not
	 * @author Vinitha 
	 * @return
	 */	
	boolean existsByMwId(Long mwId);

	/**
	 * Method to find record by Mwname and MwId
	 * @author Vinitha 
	 * @return
	 */	
	PredefinedMwEntity findByMwIdAndMwName(Long mwId , String mwName);

	
}
