package com.recon.dao;


import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import com.recon.model.FileSourceBean;
import com.recon.model.ManualKnockoffBean;
import com.recon.model.NFSSettlementBean;

public interface DataCountDao {
	
	Map<String, Object> CountDateValidate(NFSSettlementBean nfsSettlementBean);
	
	boolean DataCount(NFSSettlementBean beanObj);
	
	

}
