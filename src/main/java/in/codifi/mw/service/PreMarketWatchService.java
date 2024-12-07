package in.codifi.mw.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jboss.resteasy.reactive.RestResponse;

import in.codifi.mw.entity.PredefinedMwEntity;
import in.codifi.mw.entity.PredefinedMwScripsEntity;
import in.codifi.mw.model.PreMwRequestModel;
import in.codifi.mw.model.PreMwScripRequestModel;
import in.codifi.mw.model.ResponseModel;
import in.codifi.mw.repository.PreMarketWatchRepository;
import in.codifi.mw.repository.PredefinedMwScripRepository;
import in.codifi.mw.service.spec.PreMarketWatchServicespec;
import in.codifi.mw.util.ErrorMessageConstants;
import in.codifi.mw.util.ErrorCodeConstants;
import in.codifi.mw.util.PrepareResponse;
import in.codifi.mw.util.StringUtil;
import in.codifi.mw.cache.HazelCacheController;
import in.codifi.mw.cache.MwCacheController;
import in.codifi.cache.model.ContractMasterModel;

@ApplicationScoped
public class PreMarketWatchService implements PreMarketWatchServicespec {


	@Inject
	PreMarketWatchRepository predefinedMwRepository;

	@Inject 
	PredefinedMwScripRepository predefinedMwScripRepository;
	
	@Inject
	PrepareResponse prepareResponse;

	
	/**
	 * Method to create predefine marketwatch name
	 * @author Vinitha
	 * @return
	 */
	public RestResponse<ResponseModel> createPreMw(PreMwRequestModel pDto) {
		try {
			if (pDto == null || pDto.getMwId() == null || StringUtil.isNullOrEmpty(pDto.getMwName())|| pDto.getPosition() == null || pDto.getPosition() <= 0 || pDto.getIsEnabled() == null || pDto.getIsEditable() == null || pDto.getTag() == null)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_REQUEST ,ErrorCodeConstants.ECMW003);

			if(pDto.getMwId() > 15)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_ID ,ErrorCodeConstants.ECMW111);
			
			PredefinedMwEntity data = predefinedMwRepository.findByMwId(pDto.getMwId());
			if (data != null) 
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.EXISTING_MWID , ErrorCodeConstants.ECMW003);
			
			PredefinedMwEntity data2 = predefinedMwRepository.findByMwName(pDto.getMwName());
			if (data2 != null) 
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.EXISTING_MWNAME , ErrorCodeConstants.ECMW003);
			
			if( pDto.getMwName().length()>40)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_NAME , ErrorCodeConstants.ECMW114);			

			if(!validateMwName(pDto.getMwName()))
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_NAME1 , ErrorCodeConstants.ECMW115);			
			
			if(pDto.getPosition()> 15)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_POSITION ,ErrorCodeConstants.ECMW112);			
			
			if((!isValidBinaryInput(pDto.getIsEditable())) || (!isValidBinaryInput(pDto.getIsEnabled())))
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_BINARY,ErrorCodeConstants.ECMW116);
			
			if((!isValidBinaryInput1(pDto.getActiveStatus())))
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_BINARY ,ErrorCodeConstants.ECMW116);
			
			if( pDto.getTag().length()>10)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_TAGNAME , ErrorCodeConstants.ECMW119);
			
			if(!validateMwName(pDto.getTag()))
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_NAME2 , ErrorCodeConstants.ECMW122);			
			

			
			PredefinedMwEntity marketWatch = new PredefinedMwEntity();

			marketWatch.setMwId(pDto.getMwId());
			marketWatch.setMwName(pDto.getMwName());
			marketWatch.setPosition(pDto.getPosition());
			marketWatch.setTag(pDto.getTag());       
			marketWatch.setIsEnabled(pDto.getIsEnabled());
			marketWatch.setIsEditable(pDto.getIsEditable());
			marketWatch.setActiveStatus(pDto.getActiveStatus());

			PredefinedMwEntity savedMarketWatch = predefinedMwRepository.save(marketWatch);

			if (savedMarketWatch == null)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.FAILED_CREATION , ErrorCodeConstants.ECMW006);

			return prepareResponse.prepareMWSuccessResponseObject2(ErrorMessageConstants.MARKET_WATCH_CREATED , ErrorCodeConstants.ECMW019);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.FAILED_CREATION , ErrorCodeConstants.ECMW006);
	}

	/**
	 * Method to update predefine marketwatch name
	 * @author Vinitha
	 * @return
	 */
	public RestResponse<ResponseModel> updatePreMw(PreMwRequestModel pDto) {
		try {
			if (pDto == null || pDto.getMwId() == null || pDto.getMwId() <= 0) 
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_REQUEST , ErrorCodeConstants.ECMW003);
			
			if(pDto.getMwId() > 15 )
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_ID ,ErrorCodeConstants.ECMW111);			

			PredefinedMwEntity marketWatch = predefinedMwRepository.findByMwId(pDto.getMwId());
			if (marketWatch == null) 
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.NO_MW , ErrorCodeConstants.ECMW003);
			
			if(marketWatch.getIsEditable()==0 && pDto.getIsEditable() == null) 
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.SCRIP_ISNOT_EDITABLE , ErrorCodeConstants.ECMW018);
			
			if(marketWatch.getIsEditable()==0 && pDto.getIsEditable() != null && !pDto.getIsEditable().equals(marketWatch.getIsEditable())) 
				marketWatch.setIsEditable(pDto.getIsEditable());
			
			if( pDto.getMwName() != null && pDto.getMwName().length()>40)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_NAME , ErrorCodeConstants.ECMW114);			

			if(( pDto.getMwName() != null) && (!validateMwName(pDto.getMwName())))
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_NAME1 , ErrorCodeConstants.ECMW115);			

			if(( pDto.getPosition() != null) && pDto.getPosition()> 15)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_POSITION ,ErrorCodeConstants.ECMW112);
			
			if(( pDto.getPosition() != null) && pDto.getPosition() <0 )
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_POSITION ,ErrorCodeConstants.ECMW112);			
			
			if(( pDto.getIsEditable() != null) && (!isValidBinaryInput(pDto.getIsEditable())) )
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_BINARY,ErrorCodeConstants.ECMW116);
			
			if(( pDto.getIsEnabled() != null) && (!isValidBinaryInput(pDto.getIsEnabled())) )
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_BINARY,ErrorCodeConstants.ECMW116);		
			
			if( pDto.getActiveStatus()>0  && (!isValidBinaryInput1(pDto.getActiveStatus())))
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_BINARY ,ErrorCodeConstants.ECMW116);
			
			if( ( pDto.getTag() != null) && pDto.getTag().length()>10)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_TAGNAME , ErrorCodeConstants.ECMW119);			
	
			if (pDto.getMwName() != null && !pDto.getMwName().equals(marketWatch.getMwName())) 
				marketWatch.setMwName(pDto.getMwName());
				
			else if (pDto.getTag() != null && !pDto.getTag().equals(marketWatch.getTag())) 
				marketWatch.setTag(pDto.getTag());			

			else if (pDto.getPosition() != null) {				
				if (!pDto.getPosition().equals(marketWatch.getPosition())) 
					marketWatch.setPosition(pDto.getPosition());				
			}

			else if (pDto.getIsEnabled() != null && !pDto.getIsEnabled().equals(marketWatch.getIsEnabled())) 
				marketWatch.setIsEnabled(pDto.getIsEnabled());	
			
														
			PredefinedMwEntity updatedMarketWatch = predefinedMwRepository.save(marketWatch);
			
			if (updatedMarketWatch == null) 
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.NOT_ABLE_TO_MW_UPDATE ,ErrorCodeConstants.ECMW007);
			
			return prepareResponse.prepareMWSuccessResponseObject2(ErrorMessageConstants.SUCCESS_UPDATE ,ErrorCodeConstants.ECMW020 );
			} catch (Exception e) {
			e.printStackTrace();
		}
		return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.NOT_ABLE_TO_MW_UPDATE ,ErrorCodeConstants.ECMW007);
	}

	/**
	 * Method to delete predefine marketwatch name
	 * @author Vinitha
	 * @return
	 */
	public RestResponse<ResponseModel> deletePreMw(Long MwId) {
		try {
			if (MwId == null || MwId <= 0) 
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_REQUEST , ErrorCodeConstants.ECMW003);
			
			if( MwId > 15 )
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_ID ,ErrorCodeConstants.ECMW111);			
												
			PredefinedMwEntity entity = predefinedMwRepository.findByMwId(MwId);
			if (entity != null) 			
				predefinedMwRepository.deleteByMwId(MwId);
			else
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.NO_MW , ErrorCodeConstants.ECMW003);			

			PredefinedMwEntity deletedEntity = predefinedMwRepository.findByMwId(MwId);
			if (deletedEntity == null) {
				return prepareResponse.prepareMWSuccessResponseObject2(ErrorMessageConstants.SUCCESS_DELETE ,ErrorCodeConstants.ECMW021);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.FAILED_STATUS , ErrorCodeConstants.ECMW102);
	}

	/**
	 * Method to enable/disable predefine marketwatch name
	 * @author Vinitha
	 * @return
	 */
	public RestResponse<ResponseModel> enableDisablePreMw(Long mwId) {
		try {
			if (mwId == null || mwId <= 0) 
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_REQUEST , ErrorCodeConstants.ECMW003);
			
			if( mwId > 15 )
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_ID ,ErrorCodeConstants.ECMW111);			
	
			PredefinedMwEntity entity = predefinedMwRepository.findByMwId(mwId);
			
			if (entity == null) 
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.NO_MW , ErrorCodeConstants.ECMW003);

			PredefinedMwEntity record = predefinedMwRepository.findByMwId(mwId);
			if (record != null) {
				if (record.getIsEnabled() == 1) {
					record.setIsEnabled(0);
				} else {
					record.setIsEnabled(1);
				}
				predefinedMwRepository.save(record);
			}
			return prepareResponse.prepareMWSuccessResponseObject2(ErrorMessageConstants.SUCCESS_STATUS , ErrorCodeConstants.ECMW001);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.FAILED_STATUS , ErrorCodeConstants.ECMW102);
	}

	/**
	 * method to get all data
	 * @author Vinitha
	 * @return
	 */
	public RestResponse<ResponseModel> getAllData() {
		 try {
	            List<PredefinedMwEntity> entities = predefinedMwRepository.findAll();
	            
	            if (entities == null)
	                return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.NULL_DATA_STATUS ,ErrorCodeConstants.ECMW009 );
	            
	            if (entities.isEmpty())
	                return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.NO_DATA_FOUND_STATUS , ErrorCodeConstants.ECMW017 );
	            
	            if (entities != null) {	              
	                entities.forEach(entity -> {
	                    List<PredefinedMwScripsEntity> enrichedScrips = entity.getScrips().stream()
	                            .map(this::enrichWithCacheData)
	                            .collect(Collectors.toList());
	                    entity.setScrips(enrichedScrips);
	                });
	            }
			
			return prepareResponse.prepareSuccessResponseObject(entities , ErrorCodeConstants.ECMW001);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.FAILED_STATUS , ErrorCodeConstants.ECMW102);
	}
	

	/**
	 * Method to add Scrip details which is not added in predefined mw scriptable
	 * @author Vinitha
	 * @return
	 */
	private PredefinedMwScripsEntity enrichWithCacheData(PredefinedMwScripsEntity scrip) {
	    String cacheKey = scrip.getExchange() + "_" + scrip.getToken();
	    ContractMasterModel masterData = HazelCacheController.getInstance()
	            .getContractMaster()
	            .get(cacheKey);
	    
	    if (masterData != null) {
	        scrip.setSymbol(masterData.getSymbol());
	        scrip.setTradingSymbol(masterData.getTradingSymbol());
	        scrip.setFormattedInsName(masterData.getFormattedInsName());
	        scrip.setSegment(masterData.getSegment());
	        scrip.setPdc(masterData.getPdc());
	        scrip.setExpiry(masterData.getExpiry());
	        scrip.setWeekTag(masterData.getWeekTag());
	    }
	    return scrip;
	}


	/**
	 * method to Add Scrip 
	 * @author Vinitha
	 * @return
	 */
	public RestResponse<ResponseModel> addScrip(Long MwId, String MwName) {
	        try {
	        	if(MwId == null || MwName == null)
	        		return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_REQUEST ,ErrorCodeConstants.ECMW003);
	        	
	        	if(MwId > 15)
					return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_ID ,ErrorCodeConstants.ECMW111);					
	        	
	        	if( MwName.length() > 40 )
					return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_NAME , ErrorCodeConstants.ECMW114);
	        	
	        	if(!validateMwName(MwName))
					return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_NAME1 , ErrorCodeConstants.ECMW115);									
			
	        	PredefinedMwEntity data = predefinedMwRepository.findByMwIdAndMwName(MwId , MwName);
	        	
	        	if(data == null)
	        		return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.NO_MW , ErrorCodeConstants.ECMW005);

	        	if (data.getIsEditable() == 0)
					return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.SCRIP_ISNOT_EDITABLE , ErrorCodeConstants.ECMW018);
	        	        	
	        	 long maxSortOrder = calculateMaxSortOrder();
	               			
	        	 List<PredefinedMwScripsEntity> scripDetails = getScripMW(data, maxSortOrder);
	        	 if(scripDetails == null || scripDetails.isEmpty()) 
	 				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.NO_MW , ErrorCodeConstants.ECMW010);
	
	        	 data.getScrips().addAll(scripDetails);
	        	 PredefinedMwEntity newData = predefinedMwRepository.saveAndFlush(data);
	        	 if (newData != null)	
	        		 MwCacheController.getPredefinedMwList().clear();
	 			return prepareResponse.prepareMWSuccessResponseObject2(ErrorMessageConstants.SUCCESS_TO_ADD , ErrorCodeConstants.ECMW022);	        	 
	        } catch (Exception e) {
	            e.printStackTrace();
	            return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.FAILED_TO_ADD , ErrorCodeConstants.ECMW011);
	        }
			
	    }

	/**
	 * method to calculate MaxSortOrder 
	 * @author Vinitha
	 * @return
	 */	
	private long calculateMaxSortOrder() {
		    List<PredefinedMwScripsEntity> allScrips = predefinedMwScripRepository.findAll();
		    long maxSortOrder = 0;
		    for (PredefinedMwScripsEntity scrip : allScrips) {
		        if (scrip.getSortOrder() > maxSortOrder) {
		            maxSortOrder = scrip.getSortOrder();
		        }
		    }
		    return maxSortOrder;
	 }
	
	/**
	 * method to get scrips from cache 
	 * @author Vinitha
	 * @return
	 */	
	private List<PredefinedMwScripsEntity> getScripMW(PredefinedMwEntity data, long maxSortOrder) {
		List<PredefinedMwScripsEntity> response = new ArrayList<>();
			try {
				for (int i = 0; i < data.getScrips().size(); i++) {
					maxSortOrder = maxSortOrder + 1;
					
					PredefinedMwScripsEntity scrip = new PredefinedMwScripsEntity();
					scrip=data.getScrips().get(i);
					String exch = scrip.getExchange();
					String token = scrip.getToken();				
					
					if (HazelCacheController.getInstance().getContractMaster()
							.get(exch + "_" + token) != null) {
						
						ContractMasterModel masterData = HazelCacheController.getInstance().getContractMaster()
								.get(exch + "_" + token);
						
						scrip.setMwId(data.getMwId());	
						scrip.setToken(masterData.getToken());
						scrip.setExchange(masterData.getExch());				
						scrip.setSortOrder(maxSortOrder);				
						response.add(scrip);
					}
				}
			} catch (Exception e) {
	            e.printStackTrace();
	        }
			return response;
		}

	/**
	 * Method to delete Scrip
	 * @author Vinitha
	 * @return
	 */
	public RestResponse<ResponseModel> deleteScrip(Long MwId, String token) {	
		try {
			if (MwId == null || MwId <= 0 || token == null) 
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_REQUEST , ErrorCodeConstants.ECMW003);			
	
			if(MwId > 15)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_ID ,ErrorCodeConstants.ECMW111);	
			
			PredefinedMwEntity entity = predefinedMwRepository.findByMwId(MwId);
			
			if (entity == null) 
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_REQUEST , ErrorCodeConstants.ECMW003);
			
			PredefinedMwScripsEntity entity1 = predefinedMwScripRepository.findByToken(token);
			
			if (entity1 == null) 
        		return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.NO_MW , ErrorCodeConstants.ECMW005);
			else
				predefinedMwScripRepository.deleteByToken(token);
															
			PredefinedMwScripsEntity deletedEntity = predefinedMwScripRepository.findByToken(token);
			if (deletedEntity == null) 
				return prepareResponse.prepareMWSuccessResponseObject2(ErrorMessageConstants.SUCCESS_DEL_SCRIP , ErrorCodeConstants.ECMW024);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.FAILED_DEL_SCRIP , ErrorCodeConstants.ECMW012);
	}
	
	/**
	 * Method to Sort Scrips
	 * @author Vinitha	
	 *@return
	 */
	public RestResponse<ResponseModel> sortScrip(Long MwId, String MwName,Long id,int sortOrder) {
		try {
			if (MwId == null || MwId <= 0 || MwName ==null || id == null || sortOrder <=0 || id <=0) 
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_REQUEST , ErrorCodeConstants.ECMW003);	
			
			if(MwId > 15)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_ID ,ErrorCodeConstants.ECMW111);	
			
			if(MwName.length() > 40 )
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_NAME , ErrorCodeConstants.ECMW114);
			
			if(!validateMwName(MwName))
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_NAME1 , ErrorCodeConstants.ECMW115);									
		
			if(sortOrder >30 )
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_ORDER , ErrorCodeConstants.ECMW113);
						
			PredefinedMwEntity entity = predefinedMwRepository.findByMwIdAndMwName(MwId , MwName);
			
			if (entity == null) 
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.NO_MW , ErrorCodeConstants.ECMW003);
			
//			PredefinedMwScripsEntity data2 = predefinedMwScripRepository.findByIdAndSortOrder(id , sortOrder);
//			if(data2 != null)
//				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.EXISTING_SORTORDER , ErrorCodeConstants.ECMW125);
							
			boolean scripFound = false;
			for (PredefinedMwScripsEntity scripsEntity : entity.getScrips()) {
	            if (scripsEntity.getId().equals(id)) 
	            {	         
	                scripsEntity.setSortOrder(sortOrder);
	                scripFound = true;
	                break;  
	            }
	        }
			if (!scripFound) 
	            return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_SCRIP_ID , ErrorCodeConstants.ECMW016);
			
			 predefinedMwRepository.save(entity);
			 return prepareResponse.prepareMWSuccessResponseObject2(ErrorMessageConstants.SCRIPS_SORTED , ErrorCodeConstants.ECMW023);	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.SCRIP_NOT_SORTED , ErrorCodeConstants.ECMW013);
	}
	

	/**
	 * Method to insert Scrips
	 * @author Vinitha
	 *@return
	 */
	public RestResponse<ResponseModel> insertScrip(PreMwScripRequestModel pDto) {
		try {
			if (pDto == null)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_REQUEST , ErrorCodeConstants.ECMW003);

			if (pDto.getMwId() <=0 || pDto.getToken().trim().isEmpty() ||pDto.getToken().trim() == null || pDto.getExchange()==null || pDto.getExchange().trim().isEmpty()) 
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_REQUEST , ErrorCodeConstants.ECMW003);
			
			if(pDto.getMwId() > 15 )
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_ID ,ErrorCodeConstants.ECMW111);			

			if(pDto.getSortOrder() > 30 )
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_ORDER ,ErrorCodeConstants.ECMW113);	
		
			final List<String> VALID_OPTIONS = Arrays.asList("BCD","bcd","BFO","bfo","BSE","bse","CDS","cds","MCX","NCO","nco","NFO","nfo","NSE","nse");
					
			if( pDto.getExchange().length()>10 || (!VALID_OPTIONS.contains(pDto.getExchange())))
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_EXCHANGE , ErrorCodeConstants.ECMW118);
			
			if( pDto.getToken().length()>10)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.INVALID_TOKEN , ErrorCodeConstants.ECMW117);
			
			PredefinedMwEntity data1 = predefinedMwRepository.findByMwId(pDto.getMwId());
			if(data1 == null)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.NOT_EXISTING_MWNAME , ErrorCodeConstants.ECMW124);
									
			PredefinedMwScripsEntity data = predefinedMwScripRepository.findByExchangeAndToken(pDto.getExchange(),pDto.getToken());
			if(data != null)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.EXISTING_SCRIP , ErrorCodeConstants.ECMW123);
			
			PredefinedMwScripsEntity data2 = predefinedMwScripRepository.findByMwIdAndSortOrder(pDto.getMwId() , pDto.getSortOrder());
			if(data2 != null)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.EXISTING_SORTORDER , ErrorCodeConstants.ECMW125);
							
			PredefinedMwScripsEntity marketWatch = new PredefinedMwScripsEntity();

			marketWatch.setMwId(pDto.getMwId());
			marketWatch.setToken(pDto.getToken());
			marketWatch.setExchange(pDto.getExchange());
			marketWatch.setActiveStatus(pDto.getActiveStatus());
			marketWatch.setSortOrder(pDto.getSortOrder());

			PredefinedMwScripsEntity savedMarketWatch = predefinedMwScripRepository.save(marketWatch);

			if (savedMarketWatch == null)
				return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.NO_DATA_FOUND_STATUS , ErrorCodeConstants.ECMW017);

			return prepareResponse.prepareMWSuccessResponseObject2(ErrorMessageConstants.SUCCESS_TO_ADD ,ErrorCodeConstants.ECMW022);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prepareResponse.prepareFailedResponseObj(ErrorMessageConstants.FAILED_INSERT_DATA,ErrorCodeConstants.ECMW014);
	}
	
	
	public boolean validateMwName(String mwName) {		
		String allowedPattern = "^[a-zA-Z0-9&_]+$";
        if(Pattern.matches(allowedPattern, mwName))
        	return true;
        return false;    
	}
	
	public boolean isValidBinaryInput(Long value) {
	    return value == 0 || value == 1;
	}
	public boolean isValidBinaryInput1(int value) {
	    return value == 0 || value == 1;
	}
}
