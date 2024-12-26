/**
 * 
 */
package in.codifi.mw.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import in.codifi.mw.repository.ContractEntityManager;
import in.codifi.mw.service.spec.ContractServiceSpecs;

/**
 * @author Vicky
 *
 */
@ApplicationScoped
public class ContractService implements ContractServiceSpecs {

	@Inject
	ContractEntityManager contractEntityManager;
	/**
	 * 
	 */
	public void loadIsinByToken() {
		contractEntityManager.loadIsinByToken();
		
	}
}
