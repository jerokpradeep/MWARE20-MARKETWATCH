/**
 * 
 */
package in.codifi.mw.entity.primary;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Entity;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Vicky
 *
 */
@Getter
@Setter
@Entity(name = "TBL_UNDERLYING_SCRIPS")
public class UnderlyingEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = "ID")
	private int id;

	@Column(name = "EXCHANGE")
	private String exchange;

	@Column(name = "ISIN")
	private String isin;

	@Column(name = "LOT_SIZE")
	private String lotSize;

	@Column(name = "SYMBOL")
	private String symbol;

	@Column(name = "TOKEN")
	private String token;

	@Column(name = "TYPE")
	private String type;

	@Column(name = "UNDERLYING")
	private String underlying;

}
