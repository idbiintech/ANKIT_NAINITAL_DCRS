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
<script type="text/javascript" src="js/datadelete.js"></script>





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
			Data Delete
			<!-- <small>Version 2.0</small> -->
		</h1>
		<ol class="breadcrumb">
			<li><a href="#"> Home</a></li>
			<li class="active">Data Delete</li>
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
								<label for="exampleInputEmail1">File Name</label> <input
									type="hidden" name="CSRFToken" id="CSRFToken"
									value="${CSRFToken }"> <select class="form-control"
									path="filename" id="filename" onchange="setfilename(this);">
									<option value="swatm">SWITCH ATM</option>
									<option value="swecopos">SWITCH ECOM/POS</option>
									<option value="cbs">CBS</option>
									<option value="atmissraw">ATM ISSUER RAWDATA 4 CYCLES</option>
									<option value="ecoposraw">ECOM POS RAWDATA 4 CYCLES</option>
									<option value="adj">ATM ADJUSTMENT FILE</option>

									<option value="ntsl">NFS NTSL FILE</option>
									<option value="dsr">ECOM POS DSR FILE</option>





								</select>

							</div>

							<div class="form-group" id="excelFileType" style="display: none">
								<label for="exampleInputEmail1">Cycle</label>
								<form:select class="form-control" path="fileType" id="fileType"
									name="fileType">
									<form:option value="1">1</form:option>
									<form:option value="2">2</form:option>
									<form:option value="3">3</form:option>
									<form:option value="4">4</form:option>
								</form:select>
							</div>

							<div class="form-group">
								<label for="exampleInputPassword1">Date</label>
								<form:input path="fileDate" class="form-control"
									readonly="readonly" id="datepicker" placeholder="dd/mm/yyyy" />
							</div>


						</div>
						<!-- /.box-body -->

						<div class="box-footer">
							<button type="button" value="UPLOAD" id="upload"
								onclick="return datadelete();" class="btn btn-primary">Delete</button>
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
	</section>
</div>
<!-- /.content-wrapper -->

<div align="center" id="Loader"
	style="background-color: #ffffff; position: fixed; opacity: 0.7; z-index: 99999; height: 100%; width: 100%; left: 0px; top: 0px; display: none">

	<img style="margin-left: 20px; margin-top: 200px;"
		src="images/unnamed.gif" alt="loader">

</div>
