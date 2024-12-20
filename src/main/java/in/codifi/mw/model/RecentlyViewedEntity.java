/**
 * 
 */
package in.codifi.mw.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import in.codifi.mw.entity.primary.CommonEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Vicky
 *
 */
@Getter
@Setter
@Entity(name = "TBL_RECENTLY_VIEWED")
public class RecentlyViewedEntity extends CommonEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column(name = "USER_ID")
	private String userId;

	@Column(name = "EXCH")
	private String exch;

	@Column(name = "TOKEN")
	private String token;

	@Column(name = "SORT_ORDER")
	private int sortOrder;

	@Temporal(TemporalType.DATE)
	@Column(name = "EXPIRY_DATE")
	private Date expiryDate;
}
