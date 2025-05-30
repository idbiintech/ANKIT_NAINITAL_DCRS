package com.recon.dao.impl;

import static com.recon.util.GeneralUtil.CHECK_IP;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.StoredProcedure;
import org.springframework.stereotype.Component;









import com.recon.dao.LoginDao;
import com.recon.model.LoginBean;
import com.recon.model.ProcessDtlBean;
import com.recon.model.UserBean;
import com.recon.util.SearchData;
import com.recon.util.demo;

@Component
@SuppressWarnings({"unchecked"})
public class LoginDaoImpl extends JdbcDaoSupport implements LoginDao {

	/**
	 * Input Field Constants.
	 */
	private static final String I_USER_ID = "i_user_id";
	private static final String I_IP_ADDRESS = "i_ip_address";
	private static final String I_SESSION_ID = "i_session_id";
	ResultSet rs;
	PreparedStatement ps;
	Connection con;

	/**
	 * Output Field Constants
	 */
	private static final String O_LOGIN_COUNT = "o_login_count";
	private static final String O_ERROR_CODE = "o_error_code";
	private static final String O_ERROR_MESSAGE = "o_error_message";
	
	private static final String O_USER_CUR = "o_user_cur";
	private static final String O_USER_COUNT = "o_user_count";

	StringBuilder error_string = new StringBuilder();

	@Override
	public LoginBean getUserDetail(LoginBean loginBean) throws Exception {
		try {
			logger.info("***** LoginDaoImpl.getUserDetail Start ****");

			Map<String, Object> inParams = new HashMap<String, Object>();
			inParams.put(I_USER_ID, loginBean.getUser_id());
			//inParams.put(I_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress());
			inParams.put(I_IP_ADDRESS, loginBean.getIp_address());
			inParams.put(I_SESSION_ID, loginBean.getSession_id());

			LoginDetailProc loginDetailProc = new LoginDetailProc(getJdbcTemplate());
			Map<String, Object> outParams = loginDetailProc.execute(inParams);
			
			logger.info("outParams.get(O_ERROR_CODE)=="+outParams.get(O_ERROR_CODE));
			logger.info("outParams.get(O_ERROR_MESSAGE)=="+outParams.get(O_ERROR_MESSAGE));
			
			if (outParams.get(O_ERROR_CODE) != null && Integer.parseInt(String.valueOf(outParams.get(O_ERROR_CODE))) != 0) {
				throw new Exception(outParams.get(O_ERROR_MESSAGE).toString());
			}
			
			String getUserDetails = "select user_id, user_name, user_status, last_login, entry_dt, updt_dt from login_master where "
						+"upper(user_id) = upper('"+loginBean.getUser_id()+"')";
			
			int userCount = getJdbcTemplate().queryForObject("select count(*) from login_master where "
					+"upper(user_id) = upper('"+loginBean.getUser_id()+"')", new Object[] {},Integer.class);
			
			loginBean = getJdbcTemplate().query(getUserDetails, new Object[] {}, new ResultSetExtractor<LoginBean>(){
				public LoginBean extractData(ResultSet rs)throws SQLException {
					LoginBean beanObj = new LoginBean();
					while (rs.next()) {
						beanObj.setUser_id(rs.getString("user_id"));
						beanObj.setUser_name(rs.getString("user_name"));
						beanObj.setUser_status(rs.getString("user_status"));
						beanObj.setLast_login(rs.getString("last_login"));
						beanObj.setEntry_dt(rs.getString("ENTRY_DT"));
						beanObj.setUpdt_dt(rs.getString("UPDT_DT"));
					}
					return beanObj;
				}
			});
			
			logger.info("outParams.get(O_LOGIN_COUNT)=="+outParams.get(O_LOGIN_COUNT));
			
			//if (((List<LoginBean>) outParams.get(O_LOGIN_CUR)).size() == 0) {
			if(userCount == 0)
			{
				//throw new Exception("Invalid User Name and/or Password.");
				throw new Exception("User Not Added");
			}

			if (Integer.parseInt(String.valueOf(outParams.get(O_LOGIN_COUNT))) > 0) {
				error_string.setLength(0);
				error_string.append("User already logged in.<br/>");
				error_string.append("Click <a id='endSession' style='color:teal;border-bottom: 1px dotted orange; cursor: pointer;text-decoration:none'>here</a> to begin a new session.");
				throw new Exception(error_string.toString());
			}

			//loginBean = ((List<LoginBean>) outParams.get(O_LOGIN_CUR)).get(0);
			
			logger.info("***** LoginDaoImpl.getUserDetail End ****");
			
		} catch (Exception e) {
			//demo.logSQLException(e, "LoginDaoImpl.getUserDetail");
			//logger.error(" error in LoginDaoImpl.getUserDetail", new Exception("LoginDaoImpl.getUserDetail",e));
			throw e;
		}
		return loginBean;
	}

	private class LoginDetailProc extends StoredProcedure {
		private static final String view_login_proc = "view_user_login_detail";

		public LoginDetailProc(JdbcTemplate jdbcTemplate) {
			super(jdbcTemplate, view_login_proc);
			setFunction(false);
			declareParameter(new SqlParameter(I_USER_ID, Types.VARCHAR));
			declareParameter(new SqlParameter(I_IP_ADDRESS, Types.VARCHAR));
			declareParameter(new SqlParameter(I_SESSION_ID, Types.VARCHAR));
			declareParameter(new SqlOutParameter(O_LOGIN_COUNT, Types.INTEGER));
			declareParameter(new SqlOutParameter(O_ERROR_CODE, Types.INTEGER));
			declareParameter(new SqlOutParameter(O_ERROR_MESSAGE, Types.VARCHAR));
			compile();
		}

		private class LoginDetailMapper implements RowMapper<LoginBean> {

			@Override
			public LoginBean mapRow(ResultSet rs, int rowNum) throws SQLException {
				LoginBean loginBean = new LoginBean();

				loginBean.setUser_id(rs.getString("user_id"));
				loginBean.setUser_name(rs.getString("user_name"));
				//loginBean.setUser_type(rs.getString("user_type"));
				loginBean.setUser_status(rs.getString("user_status"));
				loginBean.setLast_login(rs.getString("last_login"));
				loginBean.setEntry_dt(rs.getString("entry_dt"));
				loginBean.setEntry_by(rs.getString("entry_by"));
				loginBean.setUpdt_dt(rs.getString("updt_dt"));
				loginBean.setUpdt_by(rs.getString("updt_by"));

				return loginBean;
			}
		}
	}

	@Override
	public void invalidateUser(LoginBean loginBean) throws Exception {
		try {
			logger.info("***** LoginDaoImpl.validateUser End ****");
			Map<String, Object> inParams = new HashMap<String, Object>();
			inParams.put(I_USER_ID, loginBean.getUser_id());

			InvalidateUserProc invalidateUserProc = new InvalidateUserProc(getJdbcTemplate());
			Map<String, Object> outParams = invalidateUserProc.execute(inParams);
			if (outParams.get(O_ERROR_CODE) != null && Integer.parseInt(String.valueOf(outParams.get(O_ERROR_CODE))) != 0) {
				throw new Exception(outParams.get(O_ERROR_MESSAGE).toString());
			}
			
			logger.info("***** LoginDaoImpl.validateUser End ****");
			
		} catch (Exception e) {
			demo.logSQLException(e, "LoginDaoImpl.validateUser");
			logger.error(" error in LoginDaoImpl.validateUser", new Exception("LoginDaoImpl.validateUser",e));
			throw e;
		}

	}

	private class InvalidateUserProc extends StoredProcedure {
		private static final String invalidate_user_proc = "LOG_OUT";

		public InvalidateUserProc(JdbcTemplate jdbcTemplate) {
			super(jdbcTemplate, invalidate_user_proc);
			setFunction(false);
			declareParameter(new SqlParameter(I_USER_ID, Types.VARCHAR));
			declareParameter(new SqlOutParameter(O_ERROR_CODE, Types.INTEGER));
			declareParameter(new SqlOutParameter(O_ERROR_MESSAGE, Types.VARCHAR));
			compile();
		}
	}

@Override
public boolean checkIp(LoginBean loginBean) throws Exception{
		try {
			//Map<String, Object> inParams = new HashMap<String, Object>();
			//inParams.put(I_EMPLY_CD, provisionalInterestBean.getEmply_cd());

			int i =  getJdbcTemplate().queryForObject(CHECK_IP, new Object[] { loginBean.getIp_address() },Integer.class);
			
			if(i==1)
			{
				
				return true;
			}
			else 
				return false;	
			
			
		} catch (Exception e) {
			throw e;
		}


}

@Override
public Map<String, Object> getAllSession(LoginBean loginBean) throws Exception{
	try
	{
		Map<String, Object> session_map = new HashMap<>();
		List<LoginBean> user_details = new ArrayList<>();
		Map<String, Object> inParams = new HashMap<String, Object>();
		inParams.put(I_IP_ADDRESS, loginBean.getIp_address());

		ViewAllSessions viewAllSessions = new ViewAllSessions(getJdbcTemplate());
		
		Map<String, Object> outParams = viewAllSessions.execute(inParams);
		
		if(outParams.get(O_ERROR_MESSAGE) != null && Integer.parseInt(String.valueOf(outParams.get(O_ERROR_CODE))) != 0){
			throw new Exception(outParams.get(O_ERROR_MESSAGE).toString());
		}
		
		user_details = viewAllSessions.getUser_list();
		session_map.put("USER_LIST", user_details);
		
		return session_map;
	}
	catch(Exception e)
	{
		throw e;
	}
}

private class ViewAllSessions extends StoredProcedure {
	private static final String view_all_sessions_proc = "VIEW_ALL_SESSIONS";
	
	List<LoginBean> user_list = new ArrayList<LoginBean>();

	/**
	 * @return the trans_list
	 */
	public List<LoginBean> getUser_list() {
		return user_list;
	}

	public ViewAllSessions(JdbcTemplate jdbcTemplate) {
		super(jdbcTemplate, view_all_sessions_proc);
		setFunction(false);
		declareParameter(new SqlParameter(I_IP_ADDRESS, Types.VARCHAR));
		declareParameter(new SqlOutParameter(O_USER_CUR, Types.REF_CURSOR, new SessionDetails()));
		//declareParameter(new SqlOutParameter(O_USER_COUNT, OracleTypes.INTEGER));
		declareParameter(new SqlOutParameter(O_ERROR_CODE, Types.INTEGER));
		declareParameter(new SqlOutParameter(O_ERROR_MESSAGE, Types.VARCHAR));
		compile();
	}

	private class SessionDetails implements RowCallbackHandler {

		@Override
		public void processRow(ResultSet rs) throws SQLException {
			
			LoginBean loginBean = new LoginBean();
			loginBean.setUser_id(rs.getString("user_id"));
			loginBean.setIn_time(rs.getString("in_time"));
			loginBean.setIp_address(rs.getString("ip_address"));
			/*ContributionBean.setEmply_cd(rs.getInt(ContributionValidationBean.EMPLY_CD));
			ContributionBean.setOffc_cd(rs.getInt(ContributionValidationBean.OFFC_CD));
			ContributionBean.setEffctv_dt(rs.getString(ContributionValidationBean.EFFCTV_DT));
			ContributionBean.setRecords(rs.getInt(ContributionValidationBean.RECORDS));
			ContributionBean.setEmp_amt(rs.getBigDecimal(ContributionValidationBean.EMP_AMT));
			ContributionBean.setBnk_amt(rs.getBigDecimal(ContributionValidationBean.BNK_AMT));
			ContributionBean.setAdd_amt(rs.getBigDecimal(ContributionValidationBean.ADD_AMT));
			ContributionBean.setRec_amt(rs.getBigDecimal(ContributionValidationBean.REC_AMT));
			ContributionBean.setLoan_amt(rs.getBigDecimal(ContributionValidationBean.LOAN_AMT));*/

			user_list.add(loginBean);
		
		}

	}
}

@Override
public void closeSession(LoginBean loginBean) throws Exception
{
	try {
		Map<String, Object> inParams = new HashMap<String, Object>();
		inParams.put(I_USER_ID, loginBean.getUser_id());

		CloseSessionProc closeSessionProc = new CloseSessionProc(getJdbcTemplate());
		Map<String, Object> outParams = closeSessionProc.execute(inParams);
		if (outParams.get(O_ERROR_CODE) != null && Integer.parseInt(String.valueOf(outParams.get(O_ERROR_CODE))) != 0) {
			throw new Exception(outParams.get(O_ERROR_MESSAGE).toString());
		}
	} catch (Exception e) {
		throw e;
	}

}

private class CloseSessionProc extends StoredProcedure {
	private static final String invalidate_user_proc = "CLOSE_SESSION";

	public CloseSessionProc(JdbcTemplate jdbcTemplate) {
		super(jdbcTemplate, invalidate_user_proc);
		setFunction(false);
		declareParameter(new SqlParameter(I_USER_ID, Types.VARCHAR));
		declareParameter(new SqlOutParameter(O_ERROR_CODE, Types.INTEGER));
		declareParameter(new SqlOutParameter(O_ERROR_MESSAGE, Types.VARCHAR));
		compile();
	}
}

@SuppressWarnings("null")
@Override
public List<ProcessDtlBean> getProcessdtls(String flag) {
	
	try{
	List<ProcessDtlBean> processDtlBeans =new ArrayList<>();

/*		String query = "SELECT a.d_cat,"
=======
		String query ="SELECT a.d_cat,"
>>>>>>> .r110
			+ " CASE WHEN d_filename LIKE '%CBS%'  THEN TO_CHAR (a.filedate, 'dd/mm/yyyy') ELSE 'NA' END cbs,"
			+ " CASE WHEN d_filename LIKE '%SWITCH%' THEN TO_CHAR (a.filedate, 'dd/mm/yyyy') ELSE 'NA' END swtch,"
			//+ " CASE WHEN NOT REGEXP_LIKE (d_filename, '^(CBS|SWITCH)') THEN TO_CHAR (a.filedate, 'dd/mm/yyyy')  ELSE 'NA' END incoming "
			+" CASE WHEN NOT REGEXP_LIKE (d_filename, '^(CBS|SWITCH)') THEN "
			+"  CASE WHEN d_cat like 'MASTERCARD-ISSUER' then "
			+"      case when d_filename LIKE '%POS%' then "
             +"        to_char (a.filedate, 'dd/mm/yyyy') "
              +"       ELSE 'NA' "
              +"       end "
              + "		ELSE to_char (a.filedate, 'dd/mm/yyyy')"
              +"       end "
              +"   when d_cat not like 'MASTERCARD-ISSUER' then   TO_CHAR (a.filedate, 'dd/mm/yyyy') "
              +"         ELSE  'NA' "
              +"  END incoming "
			+ "  FROM (SELECT   MAX (filedate) filedate, "
			+ "       CASE   WHEN NOT REGEXP_LIKE (os1.file_subcategory, '^(-)')"
			+ "             THEN os1.CATEGORY ||'-'||os1.file_subcategory "
			+ "          ELSE os1.CATEGORY    END  d_cat,  os1.file_subcategory d_subcat, os2.filename d_filename"
			+ "  		FROM main_file_upload_dtls os1 INNER JOIN main_filesource os2"
			+ "		    ON os1.fileid = os2.fileid "
			+ "			 where os1."+flag+" ='Y'  "
			+ "  		 GROUP BY os1.CATEGORY, os2.filename, os1.file_subcategory"
			+ "		     ORDER BY CATEGORY ASC) a"
<<<<<<< .mine
			+ "			 order by d_cat";*/

		//	+ "			 order by d_cat";

	
	String query = " select D_CAT,CBS,SWTCH, decode ( INCOMING ,null,'NA',INCOMING)INCOMING,filename from (" +
	" SELECT a.d_cat," +
	"              CASE WHEN d_filename LIKE '%CBS%'  THEN TO_CHAR (a.filedate, 'dd/mm/yyyy') ELSE 'NA' END cbs," +
	"              CASE WHEN d_filename LIKE '%SWITCH%' THEN TO_CHAR (a.filedate, 'dd/mm/yyyy') ELSE 'NA' END swtch," +
	"              CASE WHEN NOT REGEXP_LIKE (d_filename, '^(CBS|SWITCH)') THEN " +
	"               CASE WHEN d_cat like 'MASTERCARD-ISSUER' then " +
	"                   case when d_filename LIKE '%POS%' then " +
	"                      to_char (a.filedate, 'dd/mm/yyyy') " +
	"                      ELSE 'NA' " +
	"                      end " +
	"                      ELSE  TO_CHAR (a.filedate, 'dd/mm/yyyy') " +
	"                   end " +
	"                 END incoming , d_filename filename" +
	"               FROM (SELECT   MAX (filedate) filedate, " +
	"                    CASE   WHEN NOT REGEXP_LIKE (os1.file_subcategory, '^(-)')" +
	"                          THEN os1.CATEGORY ||'-'||os1.file_subcategory " +
	"                       ELSE os1.CATEGORY    END  d_cat,  os1.file_subcategory d_subcat, os2.filename d_filename" +
	"                       FROM main_file_upload_dtls os1 INNER JOIN main_filesource os2" +
	"                         ON os1.fileid = os2.fileid " +
	"                          where os1."+flag+" ='Y' AND OS2.FILEID IN ('15','17','7','1','2','3') " +
	"                        GROUP BY os1.CATEGORY, os2.filename, os1.file_subcategory" +
	"                          ORDER BY CATEGORY ASC) a" +
	"                         order by ( case  when filename = 'SWITCH' then 2 when filename ='CBS' then 1  end)" +
	"                           )  order by d_cat ,( case  when filename = 'SWITCH' then 1 when filename ='CBS' then 2  end)" ;
	 
	
	 	con = getConnection();
	 	System.out.println("query"+query);
	 	ps= con.prepareStatement(query);
	 	rs = ps.executeQuery();
	 	String category="";
	 	String subcat="";
	 	ProcessDtlBean data =null;
	 	while(rs.next()) {
	 		
	 		System.out.println(rs.getString(1)+rs.getString(2)+rs.getString(3)+rs.getString(4)+rs.getString(5));
	 		//subcat = rs.getString(2)=="-"?null:rs.getString(2);
	 		
	 		if(category.contains(rs.getString(1))) {
	 			
	 			data.setCategory(rs.getString(1));
	 			
	 			if(!(rs.getString(2).equals("NA"))) {
 					data.setCbs_date(rs.getString(2));
 					
 					
 				} if(!(rs.getString(3).equals("NA"))) {
 					
 					data.setSwitch_date(rs.getString(3));
 					
 				} if(!(rs.getString(4).equals("NA"))) {
 					
 					data.setNetwork_date(rs.getString(4));
 				} 
 				
	 			
	 			
	 			
	 		} else {
	 			
	 			/*System.out.println(rs.getString(5));*/
	 			category = category+rs.getString(1);
	 			if(data==null) {
	 				
	 				data = new ProcessDtlBean();
	 				data.setCategory(rs.getString(1));
		 			
	 				
	 				if(!(rs.getString(2).equals("NA"))) {
	 					data.setCbs_date(rs.getString(2));
	 					
	 					
	 				} if(!(rs.getString(3).equals("NA"))) {
	 					
	 					data.setSwitch_date(rs.getString(3));
	 					
	 				} if(!(rs.getString(4).equals("NA"))) {
	 					
	 					data.setNetwork_date(rs.getString(4));
	 				} 
	 				
	 			} else {
	 				
	 				processDtlBeans.add(data);
	 				data= new ProcessDtlBean();
	 				data.setCategory(rs.getString(1));
		 			
	 				
	 				if(!(rs.getString(2).equals("NA"))) {
	 					data.setCbs_date(rs.getString(2));
	 					
	 					
	 				} if(!(rs.getString(3).equals("NA"))) {
	 					
	 					data.setSwitch_date(rs.getString(3));
	 					
	 				} if(!(rs.getString(4).equals("NA"))) {
	 					
	 					data.setNetwork_date(rs.getString(4));
	 				} 
	 				
	 			}
	 		}
	 		
	 		
	 		
	 	}
	 	processDtlBeans.add(data);
	
	
	
	return processDtlBeans;
	}catch(Exception ex) {
		
		logger.error(ex);
		ex.printStackTrace();
		return null;
	}
}

@Override
public List<ProcessDtlBean> getDetails() {
 
	
	
	
	
	
	return null;
}






}
