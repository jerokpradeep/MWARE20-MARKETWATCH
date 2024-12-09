package in.codifi.mw.model;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PreMwScripRequestModel {

	private Long id;
	private Long mwId;
	private String token;
	private String exchange;	
	private Long sortOrder;
	private int activeStatus = 1;
	
}
