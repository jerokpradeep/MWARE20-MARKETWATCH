/**
 * 
 */
package in.codifi.mw.entity.secondary;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

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
public class RecentlyViewedEntity implements Serializable{
	/**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;
	@Id
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column(name = "CREATED_ON", insertable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@CreationTimestamp
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "IST")
	private Date createdOn;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column(name = "UPDATED_ON")
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedOn;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column(name = "CREATED_BY")
	private String createdBy;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column(name = "UPDATED_BY")
	private String updatedBy;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column(name = "ACTIVE_STATUS")
	private int activeStatus = 1;

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
