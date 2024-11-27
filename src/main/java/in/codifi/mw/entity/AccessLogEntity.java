package in.codifi.mw.entity;

import java.sql.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tbl_access_log")
public class AccessLogEntity {
	
		@Id
		@GeneratedValue
		@Column(name = "ID")
		private long id;
		
		@Column(name = "URI")
		private String uri;
		
		@Column(name = "UCC")
		private String ucc;
		
		@Column(name = "USER_ID")
		private String user_id;
		
		@Column(name = "MODULE")
		private String module;
		
		@Column(name = "METHOD")
		private String method;
		
		@Column(name = "SOURCE")
		private String source;
		
		@Column(name = "REQ_BODY")
		private long req_body;
		
		@Column(name = "RES_BODY")
		private long res_body;
		
		@Column(name = "REQ_ID")
		private String req_id;
		
		@Column(name = "DEVICE_IP")
		private String device_ip;
		
		@Column(name = "USER_AGENT")
		private Long user_agent;
		
		@Column(name = "CONTENT_TYPE")
		private String content_type;
		
		@Column(name = "DOMAIN")
		private Long domain;
		
		@Column(name = "IN_TIME")
		private Date in_time;
		
		@Column(name = "OUT_TIME")
		private Date out_time;
		
		@Column(name = "LAG_TIME")
		private String lag_time;

		@Column(name = "ELAPSED_TIME")
		private String elapsed_time;
		
		@Column(name = "SESSION")
		private int session;
				
		@Column(name = "VENDOR")
		private String vendor;	
		
		@Column(name = "BATCH_ID")
		private int batch_id; 
		
		@Column(name = "CREATED_ON")
		@CreationTimestamp
		private java.sql.Timestamp created_on;
		
		@Column(name = "CREATED_BY")
		private String created_by;
		
		@Column(name = "UPDATED_ON")
		@UpdateTimestamp
		private Date updated_on;
		
		@Column(name = "UPDATED_BY")
		private String updated_by;
		
		@Column(name = "ACTIVE_STATUS")
		private int active_status;
			

}
