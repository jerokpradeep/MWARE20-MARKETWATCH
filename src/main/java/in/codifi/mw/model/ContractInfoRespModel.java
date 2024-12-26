/**
 * 
 */
package in.codifi.mw.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Vicky
 *
 */
@Getter
@Setter
public class ContractInfoRespModel {

	private String isin;
	private String freezeQty;
	private List<ContractInfoDetails> scrips;
}
