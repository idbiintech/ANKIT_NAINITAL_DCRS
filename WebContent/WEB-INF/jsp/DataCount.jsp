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

<link href="css/jquery-ui.min.css" media="all" rel="stylesheet"
	type="text/css" />

 
<script type="text/javascript" src="js/jquery-ui.min.js"></script>
<script type="text/javascript" src="js/DataCount.js"></script>
<script type="text/javascript">

$(document).ready(function() {
	debugger;
	 $("#datepicker").datepicker({dateFormat:"dd/mm/yy", maxDate:0});
   
    });
    
window.history.forward();
function noBack() { window.history.forward(); }


</script>
<div class="content-wrapper">
	<!-- Content Header (Page header) -->
	<section class="content-header">
		<h1>
			${category} Data Count 
			 
		</h1>
		<ol class="breadcrumb">
			<li><a href="#"> Home</a></li>
			 
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
					 

					<div class="box-body" id="subcat">

					 
						<div class="form-group">
							<label for="exampleInputPassword1">Date</label> <input
								class="form-control" name="fileDate" readonly="readonly"
								id="datepicker" placeholder="dd/mm/yyyy" title="dd/mm/yyyy" />
							<!-- <img alt="" src="images/listbtn.png" title="Last Uploaded File" onclick="getupldfiledetails();" style="vertical-align:middle; height: 20px; width: 20px;"> -->


						</div>
						
						</div>

					
					<!-- /.box-body -->

					<div class="box-footer" style="text-align: center">
						<a onclick="CountData();" class="btn btn-primary">COUNT</a>
					</div>
					</div>
					<div id="processTbl"></div>
					<%-- </form> --%>
				</div>

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

 
<script>
function CallDollar()
{
	debugger;
	alert("sas");
	}
</script>