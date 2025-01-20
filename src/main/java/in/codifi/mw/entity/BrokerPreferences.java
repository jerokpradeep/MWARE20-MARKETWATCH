package in.codifi.mw.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tbl_broker_preference")
public class BrokerPreferences {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(name = "active_status")
	private Integer activeStatus;

	@Column(name = "broker")
	private String broker;

	@Column(name = "exchange")
	private String exchange;

	@Column(name = "modules")
	private String modules;

	@Column(name = "order_type")
	private String orderType;
}
