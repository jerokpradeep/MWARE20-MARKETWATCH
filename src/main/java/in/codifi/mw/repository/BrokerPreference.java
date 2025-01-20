package in.codifi.mw.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.codifi.mw.entity.BrokerPreferences;

public interface BrokerPreference extends JpaRepository <BrokerPreferences, Integer>{
	
	@Query("SELECT bp.exchange FROM BrokerPreferences bp WHERE bp.broker = :broker AND bp.activeStatus = 1")
	List<String> findExchangesByBroker(@Param("broker") String broker);


}
