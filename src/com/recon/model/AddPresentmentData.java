package com.recon.model;

public class AddPresentmentData {

	String DIFF_AMOUNT ;
	String TRAN_DATE;
	String ACCOUNTID ;
	String TXN_TYPE ;
	String RRN;
	
	@Override
	public String toString() {
		return "AddPresentmentData [DIFF_AMOUNT=" + DIFF_AMOUNT + ", TRAN_DATE=" + TRAN_DATE + ", ACCOUNTID="
				+ ACCOUNTID + ", TXN_TYPE=" + TXN_TYPE + ", RRN=" + RRN + ", AMOUNT=" + AMOUNT + ", ACCOUNT_NAME="
				+ ACCOUNT_NAME + ", DCRS_REMARKS=" + DCRS_REMARKS + "]";
	}
	String AMOUNT;
	String ACCOUNT_NAME;
	String DCRS_REMARKS ;
	
	
	public String getDIFF_AMOUNT() {
		return DIFF_AMOUNT;
	}
	public void setDIFF_AMOUNT(String dIFF_AMOUNT) {
		DIFF_AMOUNT = dIFF_AMOUNT;
	}
	public String getTRAN_DATE() {
		return TRAN_DATE;
	}
	public void setTRAN_DATE(String tRAN_DATE) {
		TRAN_DATE = tRAN_DATE;
	}
	public String getACCOUNTID() {
		return ACCOUNTID;
	}
	public void setACCOUNTID(String aCCOUNTID) {
		ACCOUNTID = aCCOUNTID;
	}
	public String getTXN_TYPE() {
		return TXN_TYPE;
	}
	public void setTXN_TYPE(String tXN_TYPE) {
		TXN_TYPE = tXN_TYPE;
	}
	public String getRRN() {
		return RRN;
	}
	public void setRRN(String rRN) {
		RRN = rRN;
	}
	public String getAMOUNT() {
		return AMOUNT;
	}
	public void setAMOUNT(String aMOUNT) {
		AMOUNT = aMOUNT;
	}
	public String getACCOUNT_NAME() {
		return ACCOUNT_NAME;
	}
	public void setACCOUNT_NAME(String aCCOUNT_NAME) {
		ACCOUNT_NAME = aCCOUNT_NAME;
	}
	public String getDCRS_REMARKS() {
		return DCRS_REMARKS;
	}
	public void setDCRS_REMARKS(String dCRS_REMARKS) {
		DCRS_REMARKS = dCRS_REMARKS;
	}
	
	
	
}
