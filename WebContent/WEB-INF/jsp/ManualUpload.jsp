<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<meta http-equiv="X-UA-Compatible" content="IE=10">
<link href="css/jquery-ui.min.css" media="all" rel="stylesheet"
	type="text/css" />
<!--<link href="css/jquery-ui1.css" media="all" rel="stylesheet" type="text/css" /> -->
<%
response.setHeader("Cache-Control", "no-cache");
response.setHeader("Cache-Control", "no-store");
response.setDateHeader("Expires", 0);
response.setHeader("Pragma", "no-cache");
response.setHeader("X-Frame-Options", "deny");
%>

<script type="text/javascript" src="js/jquery-ui.min.js"></script>
<script type="text/javascript" src="js/manualCompare.js"></script>





<script type="text/javascript">
	$(document).ready(function() {

		//alert("click");

		/*  $("#datepicker").datepicker({dateFormat:"dd/mm/yy", maxDate:0});
		 });
		 */
		$("#datepicker").datepicker({
			dateFormat : "dd/M/yy",
			maxDate : 0
		});
	});
</script>


<div class="content-wrapper">
	<!-- Content Header (Page header) -->
	<section class="content-header">
		<h1>
			File Upload
			<!-- <small>Version 2.0</small> -->
		</h1>
		<ol class="breadcrumb">
			<li><a href="#"> Home</a></li>
			<li class="active">File Upload</li>
		</ol>
	</section>

	<!-- Main content -->
	<section class="content">
		<div class="row">
			<!-- left column -->
			<div class="col-md-4"></div>
			<div class="col-md-4">
				<!-- general form elements -->
				<div class="box box-primary">
					<!-- <div class="box-header">
                  <h3 class="box-title">Quick Example</h3>
                </div> -->
					<!-- /.box-header -->
					<!-- form start -->
					<form:form id="uploadform" action="manualUploadFile.do"
						method="POST" commandName="CompareSetupBean"
						enctype="multipart/form-data">
						<div class="box-body">
							<div class="form-group">
								<label for="exampleInputEmail1">File Name</label>
								<!-- <input type="email" class="form-control" id="exampleInputEmail1" placeholder=""> -->
								<input type="hidden" name="CSRFToken" id="CSRFToken"
									value="${CSRFToken }">
								<form:select class="form-control" path="filename" id="filename"
									onchange="setfilename(this);">
									<form:option value="0">---Select---</form:option>
									<c:forEach var="configfilelist" items="${configBeanlist}">
										<c:if
											test="${configfilelist.stFileName == 'SWITCH' or configfilelist.stFileName == 'CBS' or configfilelist.stFileName == 'NFS' or configfilelist.stFileName == 'RUPAY' or configfilelist.stFileName == 'REV_REPORT'}">
											<form:option id="${configfilelist.stFileName}"
												value="${configfilelist.stFileName}">${configfilelist.stFileName}</form:option>
										</c:if>
									</c:forEach>
									<!-- or configfilelist.stFileName == 'CBS' -->
									<%-- <form:option id="ctf" value="CTF" >CTF</form:option> --%>
								</form:select>
								<!-- <img alt="" src="images/listbtn.png" title="Last Uploaded File" onclick="getfiledetails();" style="vertical-align:middle; height: 20px; width: 20px;"> -->
								<input type="hidden" id="headerlist" value="">

								<form:hidden path="stFileName" id="stFileName" />
								<!-- <select class="form-control" id="fileuploadTTUM" style="" placeholder="Select Your Favorite" data-search="true">
						<option value="Dispute">Dispute</option>
						<option value="opt2">option 2</option>
						<option value="opt3">option 3</option>
					  </select> -->
							</div>

							<div class="form-group" id="excelFileType" style="display: none">
								<label for="exampleInputEmail1">Taxation Type</label>
								<form:select class="form-control" path="fileType" id="excelType">
									<form:option value="ATM">ATM</form:option>
									<form:option value="ECOM">E-COM POS</form:option>
								</form:select>
							</div>


							<div class="form-group" id="trfileType" style="display: none">
								<label for="exampleInputEmail1">File Type</label>
								<!-- <input type="email" class="form-control" id="exampleInputEmail1" placeholder=""> -->
								<form:select class="form-control" path="fileType" id="fileType">
									<form:option value="ONLINE">ONLINE</form:option>
									<%-- <form:option value="MANUAL">MA NUAL</form:option> --%>
								</form:select>
							</div>

							<div class="form-group" id="trcategory" style="display: none">
								<label for="exampleInputEmail1">Category</label>
								<!-- <input type="email" class="form-control" id="exampleInputEmail1" placeholder=""> -->
								<form:select class="form-control" path="category" id="category"
									onchange="getSubCategory(this)">
									<option value="">--SELECT--</option>
									<!-- <option value="ONUS">ONUS</option> -->
									<option value="RUPAY">RUPAY</option>
									<!-- <option value="AMEX">AMEX</option> -->
									<!-- <option value="VISA">VISA</option> -->
									<option value="NFS">NFS</option>
									<!-- <option value="CASHNET">CASHNET</option>
									<option value="MASTERCARD">MASTERCARD</option>
									<option value="CARDTOCARD">CARD TO CARD</option>
									<option value="POS">ONUS POS</option>
									<option value="WCC">WCC</option> -->
									<!-- <option value="POS">POS</option> -->
								</form:select>
							</div>

							<div class="form-group" id="trsubcat" style="display: none">
								<label for="exampleInputEmail1">SubCategory</label>
								<!-- <input type="email" class="form-control" id="exampleInputEmail1" placeholder=""> -->


								<form:select class="form-control" path="stSubCategory"
									id="stSubCategory">
									<form:option value="-">--Select -</form:option>
									<form:option value="ACQUIRER">ACQUIRER</form:option>
									<form:option value="ISSUER">ISSUER</form:option>
									<option value="CARDTOCARD">CARD TO CARD</option>
								</form:select>
							</div>


							<div class="form-group">
								<label for="exampleInputPassword1">Date</label>
								<form:input path="fileDate" class="form-control"
									readonly="readonly" id="datepicker" placeholder="dd/mm/yyyy" />
							</div>
							<div class="form-group">
								<label for="exampleInputFile">File Upload</label> <input
									type="file" name="file" id="dataFile1" title="Upload File" />
								</td>
								<!-- <p class="help-block">Example block-level help text here.</p> -->
							</div>
							

						</div>
						<!-- /.box-body -->

						<div class="box-footer">
							<button type="button" value="UPLOAD" id="upload"
								onclick="return processFileUpload();" class="btn btn-primary">Upload</button>

							
						</div>
						
						<div class="box-footer"> 
						
						
							 <button type="button" value="VIEW" id="view"
								onclick="return viewFileUpload();" class="btn btn-primary">View uploaded files </button> 
								</div>
						<div class="box-footer"> 
						
						
							 <button type="button" value="DELETE" id="DELETE"
								onclick="return DeleteUploadedFiles();" class="btn btn-primary">Delete uploaded files </button> 
								</div>
						<div class="box-footer" style="display: none">
							<input type="text" id="dummy" value="012">
						</div>





					</form:form>
				</div>

				<!-- /.box -->



			</div>
			<!--/.col (left) -->

			


		</div>
		<!-- /.row -->
		
		<div class="row">
		  <div class="col-md-1"></div>			
			<div class="col-md-10">
			<div class="box box-primary">
			<div class="box-body">
			<table id="viewuloadedfile" cellpadding="2" cellspacing="0" border="0" width="100%" class="table table-bordered">
	  <tr class="footerBtns">
		
		<th class="leftSolid">File name</th>
		
		<!-- <th class="leftSolid">File Date </th> -->
		<th class="leftSolid">Count</th>
		<!-- <th class="leftSolid">Action</th> -->
		<!-- <th class="leftSolid" colspan="2">Action</th> -->
	   </tr>
		
			</table>
			</div>	
			
			</div>
			</div>
		
		
		
		</div>
		
	</section>
</div>
<!-- /.content-wrapper -->

<div align="center" id="Loader"
	style="background-color: #ffffff; position: fixed; opacity: 0.7; z-index: 99999; height: 100%; width: 100%; left: 0px; top: 0px; display: none">

	<img style="margin-left: 20px; margin-top: 200px;"
		src="images/unnamed.gif" alt="loader">

</div>
