 function CountData() {
	alert("HELLO1");
	var datepicker = document.getElementById("datepicker").value;
	var rectyp = document.getElementById("rectyp").value;
	if(ValidateData())
	{
		
var fileDate = document.getElementById("datepicker").value;
  var oMyForm = new FormData();
oMyForm.append('datepicker',fileDate);
		$.ajax({
			type: 'POST',
			url : 'DataCount.do',
			enctype:"multipart/form-data",
			 
		    data : oMyForm ,
            processData : false,
		   	contentType : false,
			 
		
		   beforeSend : function() {
					showLoader();
				},
				
			complete : function(data) {
					/*document.getElementById("upload").disabled="";*/
					console.log(data);
					hideLoader();
				},

			data:{filedate:datepicker,category:rectyp},
			success:function(response){

				alert(response);
				/* document.getElementById("stSubCategory").value="-";
				 document.getElementById("dollar_val").value="";*/
				 //document.getElementById("datepicker");
				alert("success alert: "+datepicker)

			},error: function(){

				alert("Error Occured");

			},

		});
	}
	
}

	function ValidateData() {
	 
	var datepicker = document.getElementById("datepicker").value;
	 
	debugger;
	 
	if(datepicker == "")
	{
		alert("Please select date for processing");
		return false;
	}
	
	return true;
}


function hideLoader(location) {
	
	$("#Loader").hide();
}

function showLoader(location) {
	
	$("#Loader").show();
}