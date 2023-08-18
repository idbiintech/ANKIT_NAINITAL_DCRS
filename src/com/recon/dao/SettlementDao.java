package com.recon.dao;

import com.recon.model.ManualFileBean;

public interface SettlementDao {
	
	public void manualReconToSettlement(ManualFileBean manualFileBeanObj)throws Exception;

}
