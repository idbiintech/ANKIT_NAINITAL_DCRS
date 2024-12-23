

function setfilename(e) {
	debugger;




	if (e.value == "ntsl" || e.value == "dsr") {
		document.getElementById("excelFileType").style.display = '';
	} else {
		document.getElementById("excelFileType").style.display = 'none';

	}
}

function showLoader(location) {

	$("#Loader").show();
}

function hideLoader(location) {

	$("#Loader").hide();
}

function validateupload() {

debugger ;
	var filename = document.getElementById("filename").value;
	var fileType = document.getElementById("fileType").value;
	var datepicker = document.getElementById("datepicker").value;
	var msg = "";

	if (datepicker == "") {

		msg = msg + "Please Select Date for File.\n";
	} if (filename == "") {

		msg = msg + "Please select File Name.\n";
	}

	if (msg != "") {

		alert(msg);
		return false;
	} else {

		document.getElementById("upload").disabled = "disabled";



		return true;
	}


}



function datadelete() {
	debugger;

	alert("afak_sir")

	var frm = $('#uploadform');

	var filename = document.getElementById("filename").value;
	var fileType = document.getElementById("fileType").value;
	var CSRFToken = $('[name=CSRFToken]').val();
	var fileDate = document.getElementById("datepicker").value;
	var oMyForm = new FormData();

	oMyForm.append('filename', filename);
	oMyForm.append('fileType', fileType);
	oMyForm.append('fileDate', fileDate);
	oMyForm.append('CSRFToken', CSRFToken);
	if (validateupload()) {
		$.ajax({
			type: "POST",
			url: "datadelete.do",
			enctype: "multipart/form-data",
			data: oMyForm,
			processData: false,
			contentType: false,
			//type : 'POST',
			beforeSend: function() {
				showLoader();
			},
			complete: function(data) {
				document.getElementById("upload").disabled = "";
				hideLoader();
			},
			success: function(response) {
				debugger;
				hideLoader();
				alert(response);
				document.getElementById("fileType").value = "ONLINE";
			},
			error: function(err) {
				alert(err);
			}
		});

	}

}