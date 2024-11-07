
package in.codifi.mw.entity;

import java.io.Serializable;

/**
 * @author Vicky
 *
 */
public class MarketWatchNameDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

//	@Id
//	@GeneratedValue
//	@Column(name = "ID")
	private Long id;

//	@Column(name = "MW_ID")
	private int mwId;

//	@Column(name = "USER_ID")
	private String userId;

//	@Column(name = "MW_NAME")
	private String mwName;

//	@Column(name = "POSITION")
	private Long position;

//	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//	@JoinColumn(name = "MW_ID", referencedColumnName = "MW_ID")
//	private List<MarketWatchScripDetailsDTO> mwDetailsDTO;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getMwId() {
		return mwId;
	}

	public void setMwId(int mwId) {
		this.mwId = mwId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getMwName() {
		return mwName;
	}

	public void setMwName(String mwName) {
		this.mwName = mwName;
	}

	public Long getPosition() {
		return position;
	}

	public void setPosition(Long position) {
		this.position = position;
	}
}
