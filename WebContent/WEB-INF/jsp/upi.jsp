<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<script src="js/upi.js"></script>
<div class="content-wrapper">
	<!-- Content Header (Page header) -->
	<section class="content-header">
		<h1>
			RRN DETAIL FETCH
			<!-- <small>Version 2.0</small> -->
		</h1>
		<ol class="breadcrumb">
			<li><a href="#">Home</a></li>
			<li class="active">Support</li>
		</ol>
	</section>

	<!-- Main content -->


	<section class="content">
		<div class="row">
			<div class="col-md-12"></div>
			<div class="col-md-12">

				<div class="box box-primary">
					<!-- form start -->

					<div class="box-body">
						<div style="display: inline-block; margin-right: 10px;">
							<input type="radio" id="rrn1" name="process" class="process">
							SINGLE-RRN
						</div>
						<!-- <div style="display: inline-block">
							<input type="radio" id="rrn2" name="process" class="process">
							MULTIPLE-RRN
						</div> -->
						<br> <br>
						<div class="row">
							<div class="col-md-6" id="viewRecon">
								<div class="form-group">
									<label for="rrnNo">RRN No.</label> <input type="text"
										id="rrnNo" name="rrnNo" class="form-control">
								</div>
								<div class="">
									<button class="btn btn-primary" id="downLoadSingleUpi">
										View</button>
								</div>
							</div>
						</div>

						<!-- <h4 id="emptyResponse" style="color:red; visibility: hidden;">No Data Found!!</h4> -->

						<div class="row">
							<div class="col-md-6" id="fileUpload">
								<form method="POST" id="downloadForm"
									enctype="multipart/form-data">
									<div class="form-group">
										<label for="rrnInputFile"> Upload File:</label> <input
											type="file" id="attachment" name="attachment"
											class="form-control" multiple="multiple">
									</div>
								</form>
								<div class="box-footer">
									<button class="btn btn-primary" id="multipleRrnBtn">
										Export to Excel<span id="multiplerrn"></span>
									</button>
								</div>
							</div>
						</div>
						<div class="row" id="singleUpiTableDiv">
							<div class="col-md-12">
								<div style="overflow-x: auto; margin-top: 10px;">

									<table id="singleUpiTable" class='table table-bordered'>
										<thead>
										<tr>
											<th>Switch Data</th>
										</tr>
										<tr>
											<td width="100px" align="center"><b>RRN</b></td>
											<td width="100px" align="center"><b>AMOUNT</b></td>
											<td width="100px" align="center"><b>CUSREFNO</b></td>
											<td width="100px" align="center"><b>FILE_NAME</b></td>
											<td width="100px" align="center"><b>CUSTOMERACCOUNT</b></td>
											<td width="100px" align="center"><b>PAYEE_TYPE</b></td>
											<td width="100px" align="center"><b>REMMOBILE</b></td>
											<td width="100px" align="center"><b>REMNAME</b></td>
											<td width="100px" align="center"><b>STATUS</b></td>
											<td width="100px" align="center"><b>TOACC</b></td>
											<td width="100px" align="center"><b>TXNDATE</b></td>
											<td width="100px" align="center"><b>TXNID</b></td>
											<td><b>TYPE</b></td>

										</tr>
										</thead>
										<tbody id="singleUpiTableBody"></tbody>
									</table>
								</div>
							</div>
						</div>

						<div class="row" id="singleUpiTable2Div">
							<div class="col-md-12">
								<div style="overflow-x: auto; margin-top: 10px;">

									<table id="singleUpiTable2" class='table table-bordered'>
										<thead>
										<tr>
											<th>CBS Data</th>
										</tr>
										<tr>

											<td width="100px" align="center"><b>RRN</b></td>
											<td><b>ATT2</b></td>
											<td width="100px" align="center"><b>FROMACC</b></td>
											<td width="100px" align="center"><b>FROMACCBR</b></td>
											<td width="100px" align="center"><b>FROMACCNAME</b></td>
											<td width="100px" align="center"><b>REF_SYS_TR_AUD_NO</b></td>
											<td width="100px" align="center"><b>TOACC</b></td>
											<td width="100px" align="center"><b>TOACCBR</b></td>
											<td width="100px" align="center"><b>TOACCNAME</b></td>
											<td width="100px" align="center"><b>AMOUNT</b></td>
											<td width="100px" align="center"><b>POST_DATE</b></td>
											<td width="100px" align="center"><b>AMOUNT_DRCR</b></td>

										</tr>
										</thead>
										<tbody id="singleUpiTable2Body">
										
										</tbody>
									</table>
								</div>
							</div>
						</div>


						<div class="row" id="singleUpiTable3Div">
							<div class="col-md-12">
								<div style="overflow-x: auto; margin-top: 10px;">

									<table id="singleUpiTable3" class='table table-bordered'>
										<thead>
										<tr>
											<th>NPCI Data</th>
										</tr>
										<tr>

											<td><b>RRN</b></td>
											<td width="100px" align="center"><b>UPI_TRANSACTION_ID</b></td>
											<td width="100px" align="center"><b>CYCLE</b></td>
											<td width="100px" align="center"><b>FROM_ACCOUNT_TYPE</b></td>
											<td width="100px" align="center"><b>MERCHANT_CATEGORY_CODE</b></td>
											<td width="100px" align="center"><b>PAYEE_PSP_CODE</b></td>
											<td width="100px" align="center"><b>PAYER_PSP_CODE</b></td>
											<td width="100px" align="center"><b>PROCESSED</b></td>
											<td width="100px" align="center"><b>RESERVED2</b></td>
											<td width="100px" align="center"><b>RESPONSE_CODE</b></td>
											<td width="100px" align="center"><b>TO_ACCOUNT_TYPE</b></td>
											<td width="100px" align="center"><b>TRANSACTION_AMOUNT</b></td>
											<td width="100px" align="center"><b>TRANSACTION_DATE</b></td>
											<td width="100px" align="center"><b>TRANSACTION_TIME</b></td>
											<td width="100px" align="center"><b>TRANSACTION_TYPE</b></td>
											<td width="100px" align="center"><b>FILE NAME</b></td>

										</tr>
										</thead>
										<tbody id="singleUpiTable3Body">
										
										</tbody>
									</table>
								</div>
							</div>
						</div>
						<!--  <div class="form-group">
                      <label for="custRef"> Transaction Type:</label>
                      <input type="text" id="custRef" name="custRef" class="form-control" >
                    </div> -->
					</div>
					<!-- /.box-body -->
				</div>
				<!-- /.box -->
			</div>
		</div>
	</section>
	<div align="center" id="Loader"
		style="background-color: #ffffff; position: fixed; opacity: 0.8; z-index: 99999; height: 100%; width: 100%; left: 0px; top: 0px; display: none">

		<img style="margin-left: 20px; margin-top: 200px;"
			src="images/hourglass.gif" alt="loader">
		<div style="font-size: 17px; color: #695fa9;"></div>
		
		<!-- 
		       <button type="button" id="NpciFiles"
								onclick="getNpciUploadedFiles()" class="btn btn-primary">View
								Uploaded Files</button> 
								
			<div class="row" style="padding-left: -50%">
				<div class="col-md-12">
					<div style="overflow-x: auto; margin-top: 10px;">

						<table id="viewNpciFiles" class='table table-bordered'>
							<tr>
								<td width="100px" td align="center"><b>COUNT</b></td>
								<td width="100px" td align="center"><b>FILE_NAME</b></td>
								<td width="100px" td align="center"><b>FILEDATE</b></td>
								<td width="100px" td align="center"><b>NETWORK</b></td>
								<td width="100px" td align="center"><b>SELECT</b></td>
							</tr>
						</table>



					</div>
					<div style="padding-left: 47%;">
						<input style='text-align: center;' type='button'
							class="btn btn-danger" value='Delete' id='deleteId'
							onclick='getSelectedDeleteValues();' name='deleteId' />
					</div>

				</div>
			</div>				
			
			//////////////////////////////////////////////////////////////
			function getNpciUploadedFiles() {
	// alert("rabi");
	$.ajax({
		url: 'getNpciFiles.do',
		type: 'POST',
		async: false,
		data: {

			"CSRFToken": $("meta[name='_csrf']").attr("content")

		},
		success: function(data) {

			// console.log("rabuuu");
			console.log(data);

			$('.datarow').remove();
			var obj = data;
			// console.log("rabuuu 1");
			console.log(data);
			displayNpciRecord(data);

		},
		error: function(xhr) {
			alert(xhr.responseText);
		}
	});

}

function displayNpciRecord(data) {
	var obj = data;
	// console.log(data) ;
	if (!$.trim(obj)) {
		// document.getElementById("emptyResponse").style.visibility =
		// "visible";
		alert("No Data Found for NPCI!!");
	} else {

		var tableBody = "";
		for (var i in data) {
			var tableRow = "";
			tableRow += "<td style='text-align:center;' >" + (data[i].count) + "</td>";
			tableRow += "<td style='text-align:center;'>" + (data[i].file_name) + "</td>";
			tableRow += "<td style='text-align:center;'>" + (data[i].filedate) + "</td>";
			tableRow += "<td style='text-align:center;'>" + (data[i].network) + "</td>";

			tableRow += "<td style='text-align:center;'> <input type='checkbox' class='deleteCheckBox' id='" + data[i].file_name + i + "' value='" + data[i].file_name + "'> </td>";






			tableBody = tableBody + "<tr id='datarow' class='datarow'>"
				+ tableRow + "</tr>";


		}
		$('#viewNpciFiles').append(tableBody);
		$('#viewNpciFiles').show();
	}

	function newFunction() {
		return " value=";
	}




}
function getSelectedDeleteValues() {
	const checkboxes = document.querySelectorAll('.deleteCheckBox');

	

	//querySelectorAll('.deleteCheckBox');
	const selectedValues = [];
	var oMyForm = new FormData();


	checkboxes.forEach(function(checkboxes) {



		if (checkboxes.checked == true) {
			selectedValues.push(checkboxes.value);
			alert(JSON.stringify("selectedValues = "+selectedValues))
		}


		oMyForm.append('data', selectedValues);
		oMyForm.append('CSRFToken', CSRFToken);



	})

	$.ajax({
		url: 'deleteDataFiles.do',
		type: 'POST',
		data: oMyForm,
		async: false,
		 
		processData: false,
		contentType: false,
		success: function(data) {
			alert('here inside the success');
			$('.datarow').remove();
			var obj = data;
			console.log(data);
			getNpciUploadedFiles();
		},
		error: function(xhr) {
			alert(xhr.responseText);
		}
	});



}
			//////////////////////////////////////////////////////////////	  -->
		
		
	</div>
</div>


