package com.recon.model;

public class RecordCount {
	// COUnT, FILE_NAME , FiLEDATE , NeTWORK
	private String taskName;
	private String file_name;
	private String count;
	private String date;
	private String batchNo;
	private String cycle;
	private String pstd_date;
	private String tmode;
	private String filedate;
	private String network;

	public String getFile_name() {
		return file_name;
	}

	public void setFile_name(String file_name) {
		this.file_name = file_name;
	}

	public String getFiledate() {
		return filedate;
	}

	public void setFiledate(String filedate) {
		this.filedate = filedate;
	}

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	public String getPstd_date() {
		return pstd_date;
	}

	public void setPstd_date(String pstd_date) {
		this.pstd_date = pstd_date;
	}

	public String getTmode() {
		return tmode;
	}

	public void setTmode(String tmode) {
		this.tmode = tmode;
	}

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	public String getCycle() {
		return cycle;
	}

	public void setCycle(String cycle) {
		this.cycle = cycle;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getCount() {
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}
}
