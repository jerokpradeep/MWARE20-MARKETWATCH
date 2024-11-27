package in.codifi.mw.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import in.codifi.mw.entity.AccessLogEntity;

public interface AccessLogRepo extends JpaRepository<AccessLogEntity, Long> {

}
