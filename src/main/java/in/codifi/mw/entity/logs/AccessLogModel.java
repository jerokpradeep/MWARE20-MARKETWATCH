package in.codifi.mw.entity.logs;

import java.sql.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccessLogModel {

	private long id;
	private String uri;
	private String ucc;
	private String user_id;
	private String module;
	private String method;
	private String source;
	private String req_body;
	private String res_body;
	private String req_id;
	private String device_ip;
	private String user_agent;
	private String content_type;
	private String resBody;
	private String domain;
	private int in_time;
	private int out_time;
	private int lag_time;
	private int elapsed_time;
	private String session;
	private String vendor;	
	private int batch_id; 
	private String TableName;
	private java.sql.Timestamp created_on;
	private String created_by;
	private Date updated_on;
	private String updated_by;
}
