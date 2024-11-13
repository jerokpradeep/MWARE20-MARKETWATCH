
package in.codifi.mw.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Vicky
 *
 */
@SuppressWarnings("serial")
@Getter
@Setter
public class MwIndicesModel implements Serializable{

	public String closingIndex;
	public String exchange;
	public String segmemt;
	public String indexName;
	public String indexValue;
	public String indiceID;
}
