package com.recon.auto;

import java.util.ArrayList;
import java.util.List;

public class Switch_POS {

	
	public List<String> readPOSSwitch()
	{
		List<String> DataElements = new ArrayList<String>();
		
		String record_len = "Entire Record Length|6";
		DataElements.add(record_len);
		String data_len = "Data Len|6";
		DataElements.add(data_len);
		String data_record = "Data Record|2";
		DataElements.add(data_record);
		String date_time = "Date Time|19";
		DataElements.add(date_time);
		String record_type = "Record Type|2";
		DataElements.add(record_type);
		String logical_nw = "Logical Network|4";
		DataElements.add(logical_nw);
		String card_fiid = "Card FIID|4";
		DataElements.add(card_fiid);
		String card_Number = "Card Number|19";
		DataElements.add(card_Number);
		String member_number = "Member Number|3";
		DataElements.add(member_number);
		String network = "Network|4";
		DataElements.add(network);
		String Fiid_institution = "FIID Of Institution|4";
		DataElements.add(Fiid_institution);
		String retailer_grp = "Retailer Group|4";
		DataElements.add(retailer_grp);
		String retailer_reg_grp = "Retailer Region Group|4";
		DataElements.add(retailer_reg_grp);
		String retailer_id = "Retailer Id|19";
		DataElements.add(retailer_id);
		String terminal_id = "Terminal Id|16";
		DataElements.add(terminal_id);
		String Shift_number = "Shift Number|3";
		DataElements.add(Shift_number);
		String tran_occ_time = "Tran Occurred Time|146|8";
		DataElements.add(tran_occ_time);
		String info_code ="Information Code|170|1";
		DataElements.add(info_code);
		String uder_data = "User Data|196|1";
		DataElements.add(uder_data);
		String msg_type = "Msg Type|4";
		DataElements.add(msg_type);
		String status_Code = "Status Code|2";
		DataElements.add(status_Code);
		String Auth_orig = "Auth Originator|1";
		DataElements.add(Auth_orig);
		String Auth_resp = "Auth Responder|1";
		DataElements.add(Auth_resp);
		String issuer_code = "Issuer Code|2";
		DataElements.add(issuer_code);
		String Auth_seq = "Auth Sequence|296|12";
		DataElements.add(Auth_seq);
		String term_location= "Term Location|25";
		DataElements.add(term_location);
		String term_Owner_name = "Terminal Owner Name|22";
		DataElements.add(term_Owner_name);
		String terminal_City = "Terminal City|13";
		DataElements.add(terminal_City);
		String term_state = "Term State|3";
		DataElements.add(term_state);
		String term_Country = "Term Country|2";
		DataElements.add(term_Country);
		String acq_inst_id = "Acq Inst Id|385|11";
		DataElements.add(acq_inst_id);
		String rec_inst_id = "rec Inst Id|11";
		DataElements.add(rec_inst_id);
		String term_type = "Term Type|2";
		DataElements.add(term_type);
		String sic_code = "Sic Code|427|4";
		DataElements.add(sic_code);
		String auth_tran_code = "Auth Tran Code|439|5";
		DataElements.add(auth_tran_code);
		String tran_category = "Transaction Category|1";
		DataElements.add(tran_category);
		String cart_type = "Cart type|2";
		DataElements.add(cart_type);
		String acc_number="Account Number |19";
		DataElements.add(acc_number);
		String resp_code = "Response Code|3";
		DataElements.add(resp_code);
		String amount1 = "Amount1 |19";
		DataElements.add(amount1);
		String amount2 = "Amount2|19";
		DataElements.add(amount2);
		String track2 = "Track2|512|40";
		DataElements.add(track2);
		String pre_aut_seq_no = "Pre Auth Seq No|567|12";
		DataElements.add(pre_aut_seq_no);
		String invoice_num = "Invoice Number|10";
		DataElements.add(invoice_num);
		String org_invoive_numb = "Original Invoice number|10";
		DataElements.add(org_invoive_numb);
		String Auth_indicator = "Auth Indicator|615|1";
		DataElements.add(Auth_indicator);
		String approval_code = "Approval Code|622|8";
		DataElements.add(approval_code);
		String app_Code_len = "Approval Code Length|1";
		DataElements.add(app_Code_len);
		String capture_Number = "Capture Number|664|1";
		DataElements.add(capture_Number);
		String reversal_Reason = "Reversal Reason|665|2";
		DataElements.add(reversal_Reason);
		String chbk_indict = "Chargeback Indict|2";
		DataElements.add(chbk_indict);
		String occ_of_chbk = "Occurrence Of Chargeback|1";
		DataElements.add(occ_of_chbk);
		String Auth_Code = "Auth Code|671|6";
		DataElements.add(Auth_Code);
		String Currence_code = "Currency Code|679|3";
		DataElements.add(Currence_code);
		
		return DataElements;
	}
	
	public List<String> readATMSwitch()
	{
		List<String> DataElements = new ArrayList<String>();
		
		String record_len = "Entire Record Length|6";
		DataElements.add(record_len);
		String data_len = "Data Len|6";
		DataElements.add(data_len);
		/*String data_record = "Data Record|2";
		DataElements.add(data_record);*/
		String data_time = "Date Time|14|19";
		DataElements.add(data_time);
		String record_type = "Record Type|2";
		DataElements.add(record_type);
		String auth_ppd = "Auth PPD|4";
		DataElements.add(auth_ppd);
		String Term_LN = "Term LN|4";
		DataElements.add(Term_LN);
		String Term_fiid = "Term fiid|4";
		DataElements.add(Term_fiid);
		String Term_id = "Term ID|16";
		DataElements.add(Term_id);
		String Card_LN = "Card LN|4";
		DataElements.add(Card_LN);
		String Card_FIID = "Card FIID|4";
		DataElements.add(Card_FIID);
		String Card_number = "Card Number|19";
		DataElements.add(Card_number);
		String Branch_id = "Branch Id|4";
		DataElements.add(Branch_id);
		String Code_indicating_envelope = "Code indicating envelope|103|2";
		DataElements.add(Code_indicating_envelope);
		String message_type = "Message Type|4";
		DataElements.add(message_type);
		String Auth_originator = "Auth originator|111|1";
		DataElements.add(Auth_originator);
		String Auth_responder = "Auth responder|1";
		DataElements.add(Auth_responder);
		String Tran_begin_date = "Transaction begin date|170|6";
		DataElements.add(Tran_begin_date);
		String time = "Time|8";
		DataElements.add(time);
		String Auth_seq_number = "Auth seq number|202|12";
		DataElements.add(Auth_seq_number);
		String Type_of_Terminal = "Type of Terminal|2";
		DataElements.add(Type_of_Terminal);
		String Acqu_Inst_Id = "Acqu Inst Id|221|11";
		DataElements.add(Acqu_Inst_Id);
		String Receiving_Inst_Id = "Receiving Inst Id|11";
		DataElements.add(Receiving_Inst_Id);
		String Transaction_type = "Type of Transaction|2";
		DataElements.add(Transaction_type);
		/*String from_acc_type = "From Account Type|2";
		DataElements.add(from_acc_type);
		String to_acc_type = "To Account Type|2";
		DataElements.add(to_acc_type);*/
		String from_acc_numb = "From Account Number|249|19";
		DataElements.add(from_acc_numb);
		String to_acc_numb = "To Account Number|269|19";
		DataElements.add(to_acc_numb);
		String Amount_1 = "Amount 1|289|19";
		DataElements.add(Amount_1);
		String Amount_2 = "Amount 2|19";
		DataElements.add(Amount_2);
		String avail_Bal = "Available Balance|19";
		DataElements.add(avail_Bal);
		String resp_code = "Response Code|358|2";
		DataElements.add(resp_code);
		String term_name_loc = "Terminal Name And Location|25";
		DataElements.add(term_name_loc);
		String term_Owner_name = "Term Owner Name|22";
		DataElements.add(term_Owner_name);
		String term_City = "Term City|13";
		DataElements.add(term_City);
		String state = "State|3";
		DataElements.add(state);
		String term_Country = "Term Country|2";
		DataElements.add(term_Country);
		String orig_seq_no = "Original Sequence Number|12";
		DataElements.add(orig_seq_no);
		String Date_of_tran_origin = "Date of Transaction Origin|4";
		DataElements.add(Date_of_tran_origin);
		String time_of_tran_origin = "Time of Transaction Origin|8";
		DataElements.add(time_of_tran_origin);
		String currency_code = "Currency Code|453|3";
		DataElements.add(currency_code);
		String RRN = "RRN|538|12";
		DataElements.add(RRN);
		
		return DataElements;
	}


public List<String> readDHANASwitch()
{
	List<String> DataElements = new ArrayList<String>();
	
	String acquirerId = "AcquirerID|1|3";
	DataElements.add(acquirerId);
	String IssuerId = "IssuerID|4|6";
	DataElements.add(IssuerId);
	/*String data_record = "Data Record|2";
	DataElements.add(data_record);*/
	String TranType = "Tran Type|7|8";
	DataElements.add(TranType);
	String from_Acc_Type = "From Account Type|9|10";
	DataElements.add(from_Acc_Type);
	String to_Acc_Type = "To Account Type|11|12";
	DataElements.add(to_Acc_Type);
	String RRN = "RRN|13|24";
	DataElements.add(RRN);
	String Response_Code = "Response Code|25|26";
	DataElements.add(Response_Code);
	String pan = "pan|27|45";
	DataElements.add(pan);
	String Approval_No = "Aprroval No|46|51";
	DataElements.add(Approval_No);
	String Trace = "Trace|52|63";
	DataElements.add(Trace);
	String Calender_year = "Calender Year|64|64";
	DataElements.add(Calender_year);
	String Tran_Date = "Tran Date|65|70";
	DataElements.add(Tran_Date);
	String Tran_Time = "Tran Time|71|76";
	DataElements.add(Tran_Time);
	String mcc = "MCC|77|80";
	DataElements.add(mcc);
	String Card_Acceptor_ID = "Card Acceptor ID|81|95";
	DataElements.add(Card_Acceptor_ID);
	String Card_Acceptor_Term_ID = "Card Acceptor Term ID|96|103";
	DataElements.add(Card_Acceptor_Term_ID);
	String Card_Acceptor_Term_Loc = "Card Acceptor Term Loc|104|143";
	DataElements.add(Card_Acceptor_Term_Loc);
	String Acquirer_Id = "Acquirer_Id|144|154";
	DataElements.add(Acquirer_Id);
	String Account_Num = "Account Num|155|173";
	DataElements.add(Account_Num);
	String Tran_Currency = "Tran Currency|174|176";
	DataElements.add(Tran_Currency);
	String Issuer_Currency = "Issuer Currency|177|179";
	DataElements.add(Issuer_Currency);
	String tran_amount = "tran amount|180|194";
	DataElements.add(tran_amount);
	String actual_Tran_amt = "Actual Tran Amount|195|209";
	DataElements.add(actual_Tran_amt);
	/*String from_acc_type = "From Account Type|2";
	DataElements.add(from_acc_type);
	String to_acc_type = "To Account Type|2";
	DataElements.add(to_acc_type);*/
	String bll_currency = "Billing Currency|210|212";
	DataElements.add(bll_currency);
	String ch_amount = "Ch Amount|213|227";
	DataElements.add(ch_amount);
	String Settlement_date = "Settlement Date|228|233";
	DataElements.add(Settlement_date);
	String Respcode = "RespCode|234|236";
	DataElements.add(Respcode);
	String RevCode = "RevCode|237|239";
	DataElements.add(RevCode);	
	return DataElements;
}

public List<String> readMGBSwitchAcq()
{
	List<String> DataElements = new ArrayList<String>();
	
	String institute_code = "Insti_code|11|17";
	DataElements.add(institute_code);
	String card_num = "Card_Num|29|46";
	DataElements.add(card_num);
	String acquirer_inst_code = "Acquirer_inst_code|57|64";
	DataElements.add(acquirer_inst_code);
	String acquirer_bank = "Acquirer_Bank|79|85";
	DataElements.add(acquirer_bank);
	String acc_numb = "Account_Numb|92|103";
	DataElements.add(acc_numb);
	String msgtype = "Msgtype|112|116";
	DataElements.add(msgtype);
	String rrn = "RRN|124|136";
	DataElements.add(rrn);
	String trace = "Trace|145|151";
	DataElements.add(trace);
	String card_accep_id = "Card_Accept_Id|162|172";
	DataElements.add(card_accep_id);
	String card_accep_name="Card_Accept_Name|183|209";
	DataElements.add(card_accep_name);
	String tran_amount = "Tran_Amount|213|227";
	DataElements.add(tran_amount);
	String local_date = "Local_Date|228|238";
	DataElements.add(local_date);
	String local_Time = "Local_Time|239|247";
	DataElements.add(local_Time);
	String authCode = "AuthCode|251|261";
	DataElements.add(authCode);
	String code_Action = "Code_Action|267|272";
	DataElements.add(code_Action);
	String pr_code = "pr_Code|277|285";
	DataElements.add(pr_code);
	String t_code = "TCode|286";
	DataElements.add(t_code);
	return DataElements;
}
public List<String> readMGBSwitch()
{
	List<String> DataElements = new ArrayList<String>();
	
	String institute_code = "Insti_code|11|17";
	DataElements.add(institute_code);
	String card_num = "Card_Num|29|46";
	DataElements.add(card_num);
	String acquirer_inst_code = "Acquirer_inst_code|57|64";
	DataElements.add(acquirer_inst_code);
	String acquirer_bank = "Acquirer_Bank|79|85";
	DataElements.add(acquirer_bank);
	String acc_numb = "Account_Numb|92|103";
	DataElements.add(acc_numb);
	String msgtype = "Msgtype|112|116";
	DataElements.add(msgtype);
	String rrn = "RRN|124|136";
	DataElements.add(rrn);
	String trace = "Trace|145|151";
	DataElements.add(trace);
	String card_accep_id = "Card_Accept_Id|156|174";
	DataElements.add(card_accep_id);
	String card_accep_name="Card_Accept_Name|175|200";
	DataElements.add(card_accep_name);
	String tran_amount = "Tran_Amount|201|219";
	DataElements.add(tran_amount);
	String local_date = "Local_Date|219|230";
	DataElements.add(local_date);
	String local_Time = "Local_Time|230|240";
	DataElements.add(local_Time);
	String authCode = "AuthCode|241|251";
	DataElements.add(authCode);
	String code_Action = "Code_Action|256|266";
	DataElements.add(code_Action);
	String pr_code = "pr_Code|267|283";
	DataElements.add(pr_code);
	String t_code = "TCode|284";
	DataElements.add(t_code);
	return DataElements;
}
public List<String> readNtbNupay()
{
List<String> DataElements = new ArrayList<String>();
	//String TXNFUNCTION_CODE =  
	String RESPONSE_CODE = "RESPONSE_CODE|6|7";
	DataElements.add(RESPONSE_CODE);
	String APPROVAL_CODE = "APPROVAL_CODE|12|12";
	DataElements.add(APPROVAL_CODE);
	String CARD_NUMBER = "CARD_NUMBER|18|33";
	DataElements.add(CARD_NUMBER);
	String REFERENCE_NUMBER = "REFERENCE_NUMBER|40|51";
	DataElements.add(REFERENCE_NUMBER);
	
	String TRANSACTION_DATE = "TRANSACTION_DATE|53|58";
	DataElements.add(TRANSACTION_DATE);
	String TRANSACTION_TIME = "TRANSACTION_TIME|59|64";
	DataElements.add(TRANSACTION_TIME);
	String AMOUNT = "AMOUNT|146|160";
	DataElements.add(AMOUNT);
	String TRANSACTION_CODE = "TRANSACTION_CODE|208|208";
	DataElements.add(TRANSACTION_CODE);
	
	return DataElements;
}






public List<String> readMGBPosCBS()
{
List<String> DataElements = new ArrayList<String>();
	
	String branch = "Branch|9|15";
	DataElements.add(branch);
	String term = "term|17|22";
	DataElements.add(term);
	String user = "User|23|30";
	DataElements.add(user);
	String txn_code = "txn_code|34|41";
	DataElements.add(txn_code);
	String post_date = "post_date|42|54";
	DataElements.add(post_date);
	String trace = "trace|55|65";
	DataElements.add(trace);
	String amount = "amount|66|83"; // with .00
	DataElements.add(amount);
	String action = "action|84|85";
	DataElements.add(action);
	String balance = "balance|88|106";
	DataElements.add(balance);
	String narration = "narration|112";
	DataElements.add(narration);
	
	return DataElements;
	
}

public List<String> readMGBAtmCBS()
{
List<String> DataElements = new ArrayList<String>();
	
	String branch = "Branch|9|15";
	DataElements.add(branch);
	String term = "term|17|22";
	DataElements.add(term);
	String user = "User|23|30";
	DataElements.add(user);
	String txn_code = "txn_code|34|41";
	DataElements.add(txn_code);
	String post_date = "post_date|42|54";
	DataElements.add(post_date);
	String trace = "trace|55|65";
	DataElements.add(trace);
	String amount = "amount|66|83"; // with .00
	DataElements.add(amount);
	String action = "action|84|85";
	DataElements.add(action);
	String balance = "balance|88|106";
	DataElements.add(balance);
	String card_number = "card_number|114|131";
	DataElements.add(card_number);
	String rrn = "rrn|131|146";
	DataElements.add(rrn);
	String atmid = "atmid|147";
	DataElements.add(atmid);
	
	return DataElements;
	
}

public List<String> readMGBFailedCBS()
{
List<String> DataElements = new ArrayList<String>();
	
	String stan = "stan|0|7";
	DataElements.add(stan);
	String rrno = "rrno|7|20";
	DataElements.add(rrno);
	String station_id = "station_id|21|32";
	DataElements.add(station_id);
	String txn_time = "txn_time|33|44";
	DataElements.add(txn_time);
	String term_date = "term_date|44|54";
	DataElements.add(term_date);
	String tran_c = "tran_c|55|61";
	DataElements.add(tran_c);
	String bancs_jou = "bancs_jou|62|71"; // with .00
	DataElements.add(bancs_jou);
	String cust_no = "cust_no|72|89";
	DataElements.add(cust_no);
	String card_no = "card_no|89|107";
	DataElements.add(card_no);
	String amount = "amount|107|121";
	DataElements.add(amount);
	String corr_jou = "corr_jou|122";
	DataElements.add(corr_jou);
	
	return DataElements;
	
}

public List<String> readMGBSuccessCBS()
{
List<String> DataElements = new ArrayList<String>();
	
	String stan = "stan|0|7";
	DataElements.add(stan);
	String rrno = "rrno|7|20";
	DataElements.add(rrno);
	String station_id = "station_id|20|32";
	DataElements.add(station_id);
	String txn_time = "txn_time|33|44";
	DataElements.add(txn_time);
	String term_date = "term_date|44|54";
	DataElements.add(term_date);
	String tran_c = "tran_c|55|61";
	DataElements.add(tran_c);
	String bancs_jou = "bancs_jou|62|71"; // with .00
	DataElements.add(bancs_jou);
	String cust_no = "cust_no|72|89";
	DataElements.add(cust_no);
	String card_no = "card_no|89|107";
	DataElements.add(card_no);
	String amount = "amount|107";
	DataElements.add(amount);
	
	return DataElements;
	
}
}