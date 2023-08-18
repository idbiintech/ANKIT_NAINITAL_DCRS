<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
	
	<%
response.setHeader("Cache-Control","no-cache");
response.setHeader("Cache-Control","no-store");
response.setDateHeader("Expires", 0);
response.setHeader("Pragma","no-cache");
response.setHeader("X-Frame-Options","deny");
%>


<link href="css/jquery-ui.min.css" media="all" rel="stylesheet"	type="text/css" />

<!--<link href="css/jquery-ui1.css" media="all" rel="stylesheet" type="text/css" /> -->

<script type="text/javascript" src="js/jquery-ui.min.js"></script>

<!--  <script type="text/javascript" src="js/jquery.ui.datepicker.js"></script>
<link href="css/jquery-ui1.css" media="all" rel="stylesheet" type="text/css" />
<link href="css/jquery.ui.datepicker.css" media="all" rel="stylesheet" type="text/css" />   -->

<script type="text/javascript" src="js/ReadEPFile.js"></script>
<script type="text/javascript">
$(document).ready(function() {
	debugger;
	//$('#dollar_field').hide();
  
     /* $("#datepicker").datepicker({dateFormat:"dd-M-yy", maxDate:0}); */
     
     $("#datepicker").datepicker({dateFormat:"yy/mm/dd", maxDate:0});
   
   
/* $("#monthlyDate").datepicker({
	dateFormat:"MM yy", 
	changeMonth: true,
changeYear: true,
//showButtonPanel: true,

onClose: function(){
	var iMonth = $('#ui-datpicker-div .ui-datepicker-month :selected').val();
	var iYear = $('#ui-datepicker-div .ui-datepicker-year :selected').val();
	$(this).datepicker('setDate',new Date(iYear,iMonth,1));
}
	}); */
});

window.history.forward();
function noBack() { window.history.forward(); }


</script>
<div class="content-wrapper">
	<!-- Content Header (Page header) -->
	<section class="content-header">
		<h1>
			EP File Upload
			<!-- <small>Version 2.0</small> -->
		</h1>
		<ol class="breadcrumb">
			<li><a href="#"> Home</a></li>
			<li class="active">EP File Upload</li>
		</ol>
	</section>

	<!-- Main content -->
	<section class="content">
		<div class="row">
		<div class="col-md-4"></div>
			<!-- left column -->
			<div class="col-md-4">
				<!-- general form elements -->
				<div class="box box-primary">
					<!-- <div class="box-header">
                  <h3 class="box-title">Quick Example</h3>
                </div> -->
					<!-- /.box-header -->
					<!-- form start -->
					<%-- <form role="form"> --%>
<form:form id="uploadform"  action="NTSLFileUpload.do" method="POST"  commandName="visaUploadBean" enctype="multipart/form-data" >

                   <div class="box-body">
                   
                   
                     <div class="form-group" style="display:${display}">
							<label for="exampleInputEmail1" >File Type</label> 
							<form:select class="form-control" path="fileName" id="fileName">
								<option value="0">--Select --</option>
									<option value="EP">EP</option>
									<!-- <option value="JV">JV</option> -->
							</form:select>
						</div>
					
					<div class="form-group" style="display: ${display}">
							<label for="exampleInputEmail1" >File Type</label> 
							<form:select class="form-control" path="fileType" id="fileType">
								<option value="0">--Select --</option>
									<option value="DOMESTIC">Domestic</option>
									<option value="INTERNATIONAL">International</option>
							</form:select>
						</div>	
						
						
						
                     <div class="form-group">
							<label for="exampleInputPassword1">Date</label> <input
								class="form-control" name="fileDate" readonly="readonly"
								id="datepicker" placeholder="dd/mm/yyyy" title="dd/mm/yyyy" />
					</div>	 
						
						 <div class="form-group">
                      <label for="exampleInputFile">File Upload</label>
                      <input type="file" name="dataFile1" id="dataFile1" title="Upload File" multiple="multiple"/></td>
                    </div>
                    

					<div class="box-footer" style="text-align: center">
						<a onclick="FileUpload();" class="btn btn-primary">Process</a>
						<a onclick="Eprollback();" class="btn btn-primary">Rollback</a>
					</div>
					<!-- <div id="processTbl"></div> -->
</form:form>
</div>
				</div>
				<!-- /.box -->



			</div>
			<!--/.col (left) -->

		</div>
		<!-- /.row -->
<!-- /.content-wrapper -->
</section>
</div>
<div align="center" id="Loader"
	style="background-color: #ffffff; position: fixed; opacity: 0.7; z-index: 99999; height: 100%; width: 100%; left: 0px; top: 0px; display: none">

	<img style="margin-left: 20px; margin-top: 200px;"
		src="images/unnamed.gif" alt="loader">

</div>
<script>

</script>