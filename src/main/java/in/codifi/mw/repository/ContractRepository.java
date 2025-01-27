package in.codifi.mw.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import in.codifi.mw.entity.ContractEntity;

public interface ContractRepository extends JpaRepository<ContractEntity, Long> {
	 
}