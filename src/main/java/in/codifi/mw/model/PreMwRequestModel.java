package in.codifi.mw.model;

import java.util.Date;
import java.util.List;

import in.codifi.mw.entity.PredefinedMwScripsEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class PreMwRequestModel {
	private Long id;
	private Long mwId;
	private String mwName;
	private Long position;
	private Long isEditable;
	private Long isEnabled ;
	private String tag;
	private List<PredefinedMwScripsEntity> scrips;
	private int activeStatus;
	private Date UpdatedOn;
		
}
