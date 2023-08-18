function seerule(e) {
	
	document.getElementById("fileValue").value=e;
	
	window.open("../DebitCard_Recon/SeeRule.do" , 'SeeRule', 'width=1000,height=500,location=no,toolbar=no,menubar=no,scrollbars=yes,resizable=no');
}


function showLoader(location) {
	
	$("#Loader").show();
}

function hideLoader(location) {
	
	$("#Loader").hide();
}

function Validation() {
	debugger;
	var date = $("#datepicker").val();
	if (date == "") {
		alert("Select file Date ");
		return false;
	}
	return true;
}

function processCashAtPOS() {
	debugger;
	var fileDate = document.getElementById("datepicker").value;
	if (Validation()) {
		var oMyForm = new FormData();
		oMyForm.append('fileDate',fileDate);
		$.ajax({
			type : "POST",
			url : "CashAtPOSRecon.do",
			data :oMyForm ,
			processData : false,
			contentType : false,
			beforeSend : function() {
				showLoader();
			},
			success : function(response) {
				debugger;
				hideLoader();
				alert (response); 
			},
			error : function(err) {
				hideLoader();
				alert("Error Occurred");
			},
			complete : function(data) {
				hideLoader();
			},
		});
	}
}


function reconProcess() {
	debugger;
	var fileDate = document.getElementById("datepicker").value;
	if (Validation()) {
		var oMyForm = new FormData();
		oMyForm.append('fileDate',fileDate);
		$.ajax({
			type : "POST",
			url : "PresentmentRecon.do",
			data :oMyForm ,
			processData : false,
			contentType : false,
			beforeSend : function() {
				showLoader();
			},
			success : function(response) {
				debugger;
				hideLoader();
				alert (response); 
			},
			error : function(err) {
				hideLoader();
				alert("Error Occurred");
			},
			complete : function(data) {
				hideLoader();
			},
		});
	}
}

function downloadCashAtPOS() {
	debugger;
	var fileDate = document.getElementById("datepicker").value;
	if (Validation()) {
		document.getElementById("cashatposform").submit();
	}
}
function downloadReport() {
	debugger;
	var fileDate = document.getElementById("datepicker").value;
	if (Validation()) {
		document.getElementById("presentmentform").submit();
	}
}

function presentmentFileUpload() {
	debugger;
	var frm = $('#uploadform');
		var userfile = document.getElementById("dataFile1");
	//	alert(fileDate);
		var fileDate = document.getElementById("datepicker").value;
		var subcategory = document.getElementById("stSubCategory").value;
		
	//	if(ValidateData())  {
			
			var oMyForm = new FormData();
			oMyForm.append('file',userfile.files[0])
			oMyForm.append('subcategory',subcategory);
			oMyForm.append('fileDate',fileDate);
			$.ajax({
				type : "POST",
				url : "PresentmentFileUpload.do",
				enctype:"multipart/form-data",
				data :oMyForm ,

				processData : false,
				contentType : false,
				beforeSend : function() {
					showLoader();
				},
				complete : function(data) {
					document.getElementById("upload").disabled="";
					hideLoader();

				},
				success : function(response) {
					debugger;
					hideLoader();
					alert (response);
				},
				
				error : function(err) {
					hideLoader();
					alert("Error Occurred");
				}
			});
	//	}	
}
function ValidateData()
{
	var subcategory = document.getElementById("stSubCategory").value;
	var fileDate = document.getElementById("datepicker").value;
//	var fileType = document.getElementById("fileType").value;
	var fileSelected = document.getElementById("fileName").value;
	var userfile = document.getElementById("dataFile1");
	var dataFile= document.getElementById("dataFile1").value;
	var leng= dataFile.length - 3;
	var fileExten = dataFile.substring(leng,dataFile.length);
	debugger;
	
	if(fileSelected == "0")
	{
		alert("Please select file ");
		return false;
	}
	if(fileDate == "")
	{
		alert("Please Select file Date ");
		return false;
	}
	if(dataFile==""){

		alert("Please Upload File.");
		return false;
	}
	if(fileExten != 'xlsx' && fileExten != 'xls')
	{
		alert("Upload Text file format");
		return false;
	}
	
	if(subcategory == "-")
	{
		alert("Please select subcategory");
		return false;
	}
	if(subcategory == "INTERNATIONAL" && fileSelected == "SETTLEMENT")
	{
		alert("Please don't upload Settement File in International");
		return false;
	}
	
	return true;

}

function processFileUpload() {
	debugger;
	var frm = $('#uploadform');
		var filename = document.getElementById("fileName").value;
		var userfile = document.getElementById("dataFile1");
	//	var  stSubCategory =document.getElementById("stSubCategory").value;
		//var fileType = document.getElementById("fileType").value;
	//	alert(fileDate);
		var fileDate = document.getElementById("datepicker").value;
		var subcategory = document.getElementById("stSubCategory").value;
		
		//alert(userfile.files.length);
		var files = [];
		for(var i= 0 ; i< userfile.files.length ; i++)
		{
			files[i] = userfile.files[i];
		}
		
		if(ValidateData())  {
			
			for(var i= 0 ; i< userfile.files.length ; i++)
			{
			
			var oMyForm = new FormData();
			oMyForm.append('file',files[i])
			oMyForm.append('fileName', filename);
			oMyForm.append('subcategory',subcategory);
			oMyForm.append('fileDate',fileDate);
		//	oMyForm.append('fileType',fileType);
			$.ajax({
				type : "POST",
				url : "RupayFileUpload.do",
				enctype:"multipart/form-data",
				data :oMyForm ,

				processData : false,
				contentType : false,
				beforeSend : function() {
					showLoader();
				},
				complete : function(data) {
					document.getElementById("upload").disabled="";
					hideLoader();

				},
				success : function(response) {
					debugger;
					hideLoader();
					if(i == userfile.files.length)
						alert (response); 
					//document.getElementById("fileName").value="0";
					// alert("2");
//					document.getElementById("stSubCategory").value="-";
					// alert("4");
					//document.getElementById("dataFile1").value="";
					//document.getElementById("fileType").value="0";
					// alert("8");
					//document.getElementById("datepicker").value="";
					//document.getElementById("date").style.display = 'none';

				},
				
				error : function(err) {
					hideLoader();
					alert("Error Occurred");
				}
			});
			}
		}
	
	
}


function FileNameChange(e)
{
	if(e.value == "TAD")
	{
		//document.getElementById("type").style.display = '';
		document.getElementById("date").style.display = 'none';
	}
	else
	{
		//document.getElementById("type").style.display = 'none';
		document.getElementById("date").style.display = '';
	}
	
}

function RupaySettFileRollback() {
	debugger;
	var frm = $('#uploadform');
		var filename = document.getElementById("fileName").value;
		var fileDate = document.getElementById("datepicker").value;
		var subcategory = document.getElementById("stSubCategory").value;
		var cycle = document.getElementById("cycle").value;
		
		
		if(ValidateRollbackData())  {
			
			var oMyForm = new FormData();
			oMyForm.append('fileName', filename);
			oMyForm.append('subcategory',subcategory);
			oMyForm.append('fileDate',fileDate);
			oMyForm.append('cycle',cycle);
			$.ajax({
				type : "POST",
				url : "RupaySettFileRollback.do",
				enctype:"multipart/form-data",
				data :oMyForm ,

				processData : false,
				contentType : false,
				beforeSend : function() {
					showLoader();
				},
				complete : function(data) {
					document.getElementById("upload").disabled="";
					hideLoader();

				},
				success : function(response) {
					debugger;
					hideLoader();
						alert (response); 

				},
				
				error : function(err) {
					hideLoader();
					alert("Error Occurred");
				}
			});
		}
}

function ValidateRollbackData()
{
	var subcategory = document.getElementById("stSubCategory").value;
	var fileDate = document.getElementById("datepicker").value;
	var cycle = document.getElementById("cycle").value;
	var fileSelected = document.getElementById("fileName").value;
	debugger;
	
	if(fileSelected == "0")
	{
		alert("Please select file ");
		return false;
	}
	if(fileDate == "")
	{
		alert("Please Select file Date ");
		return false;
	}
	if(cycle=="0"){

		alert("Please Select cycle.");
		return false;
	}
	if(subcategory == "-")
	{
		alert("Please select subcategory");
		return false;
	}
	if(subcategory == "INTERNATIONAL" && fileSelected == "SETTLEMENT")
	{
		alert("Please don't upload Settement File in International");
		return false;
	}
	
	return true;

}