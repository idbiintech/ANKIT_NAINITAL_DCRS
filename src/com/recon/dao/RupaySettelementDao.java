package com.recon.dao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.recon.model.RupaySettlementBean;
import com.recon.model.RupayUploadBean;

public interface RupaySettelementDao {

String uploadRupaySettlementData(List<RupaySettlementBean> list,RupaySettlementBean beanObj);	
public HashMap<String, Object> validatePrevFileUpload(RupaySettlementBean beanObj);
public HashMap<String, Object> updateFileSettlement(RupaySettlementBean beanObj,int count);
public HashMap<String, List<RupaySettlementBean>>  getTTUMData(String settlementDate);
boolean settlementRollback(RupayUploadBean beanObj);
boolean settlementFilesRollback(RupayUploadBean beanObj);
String uploadPresentmentData(RupayUploadBean beanObj, MultipartFile file) throws IOException, Exception, SQLException;
public boolean validatePresentment(String filedate);
public boolean processPresentment(String filedate);
Map<String,Object> bbps_report(String type,String rrnNo,String task) throws Exception;

}
