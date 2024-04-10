$(document).ready(function() {
	$("#upi_RRN").addClass('active');
	$("#fileUpload").hide();
	$("#viewRecon").hide();
	$("#singleUpiTable").hide();
	$("#singleUpiTable2").hide();
	$("#singleUpiTable3").hide();

	$("#rrn2").click(function() {
		$("#fileUpload").show();
		$("#viewRecon").hide();

	});

	$("#rrn1").click(function() {
		$("#viewRecon").show();
		$("#fileUpload").hide();
	});

	$('#downLoadSingleUpi').click(function() {

		$("#singleUpiTableBody").empty();
		$("#singleUpiTable2Body").empty();
		$("#singleUpiTable3Body").empty();
		
		$('#downLoadSingleUpi').text('View');
		$("#singleUpiTableDiv").hide();
		$("#singleUpiTable2Div").hide();
		$("#singleUpiTable3Div").hide();

		$.ajax({

			url : 'downloadSingleUpi.do',
			type : 'POST',
			async : false,
			data : {
				rrnNo : $('#rrnNo').val(),
				"task" : "switch",
				"CSRFToken" : $("meta[name='_csrf']").attr("content")

			},
			success : function(data) {

				$('.datarow').remove();
				var obj = data;
				console.log(data);
				displaySwitchRecord(data);

			},
			error : function(xhr) {
				alert(xhr.responseText);
			}
		});
	});

	$('#multipleRrnBtn').click(function() {
		// alert("helloooo");
		debugger;
		$('.datarow').remove();
		// document.getElementById("emptyResponse").style.visibility = "hidden";
		$('#multipleRrnBtn').text('Export to Excel');
		$('#downloadForm')[0].action = "rrnStatusReport.do";
		$('#downloadForm').submit();

	});

});

function displaySwitchRecord(data) {
	var obj = data;

	if (!$.trim(obj)) {
		// document.getElementById("emptyResponse").style.visibility =
		// "visible";
		//alert("No Data Found for Switch!!");
	} else {

		var tableBody = "";

		for ( var i in data) {

			var tableRow = "";
			tableRow += "<td align='center'>" + (data[i].rrn) + "</td>";
			tableRow += "<td align='center'>" + (data[i].amount) + "</td>";

			tableRow += "<td align='center'>" + (data[i].cusrefno) + "</td>";
			tableRow += "<td align='center' >" + (data[i].file_NAME) + "</td>";
			tableRow += "<td align='center'>" + (data[i].customeraccount)
					+ "</td>";
			tableRow += "<td align='center' >" + (data[i].payee_TYPE) + "</td>";
			tableRow += "<td align='center' >" + (data[i].remmobile) + "</td>";
			tableRow += "<td align='center' >" + (data[i].remname) + "</td>";
			tableRow += "<td align='center' >" + (data[i].status) + "</td>";
			tableRow += "<td align='center' >" + (data[i].toacc) + "</td>";
			tableRow += "<td align='center' >" + (data[i].txndate) + "</td>";
			tableRow += "<td align='center' >" + (data[i].txnid) + "</td>";

			tableBody = tableBody + "<tr id='datarow' class='datarow'>"
					+ tableRow + "</tr>";

		}
		$('#singleUpiTableBody').append(tableBody);
		$('#singleUpiTable').show();
		$("#singleUpiTableDiv").show();
	}
	// start
	$.ajax({
		url : 'downloadSingleUpi.do',
		type : 'POST',
		async : false,
		data : {
			rrnNo : $('#rrnNo').val(),
			"task" : "cbs",
			"CSRFToken" : $("meta[name='_csrf']").attr("content")

		},
		success : function(data) {

			// $('.datarow').remove();
			var obj = data;
			console.log(data);
			displayCbsRecord(data);
		},
		error : function(xhr) {
			alert(xhr.responseText);
		}
	});
	// end

}

function displayCbsRecord(data) {
	var obj = data;

	if (!$.trim(obj)) {
		// document.getElementById("emptyResponse").style.visibility =
		// "visible";
		//alert("No Data Found for CBS!!");
	} else {

		var tableBody = "";

		for ( var i in data) {

			var tableRow = "";
			tableRow += "<td align='center' >" + (data[i].rrn) + "</td>";
			tableRow += "<td align='center' >" + (data[i].att2) + "</td>";
			tableRow += "<td align='center' >" + (data[i].fromacc) + "</td>";
			tableRow += "<td align='center' >" + (data[i].fromaccbr) + "</td>";
			tableRow += "<td align='center' >" + (data[i].fromaccname)
					+ "</td>";
			tableRow += "<td align='center' >" + (data[i].ref_SYS_TR_AUD_NO)
					+ "</td>";
			tableRow += "<td align='center' >" + (data[i].toacc) + "</td>";
			tableRow += "<td align='center' >" + (data[i].toaccbr) + "</td>";
			tableRow += "<td align='center' >" + (data[i].toaccname) + "</td>";
			tableRow += "<td align='center' >" + (data[i].amount) + "</td>";
			tableRow += "<td align='center' >" + (data[i].post_DATE) + "</td>";
			tableRow += "<td align='center' >" + (data[i].amount_DRCR)
					+ "</td>";

			tableBody = tableBody + "<tr id='datarow' class='datarow'>"
					+ tableRow + "</tr>";

		}
		$('#singleUpiTable2Body').append(tableBody);
		$('#singleUpiTable2').show();
		
		$("#singleUpiTable2Div").show();
	}
	$.ajax({
		url : 'downloadSingleUpi.do',
		type : 'POST',
		async : false,
		data : {
			rrnNo : $('#rrnNo').val(),
			"task" : "npci",
			"CSRFToken" : $("meta[name='_csrf']").attr("content")

		},
		success : function(data) {

			// $('.datarow').remove();
			var obj = data;
			console.log(data);
			displayNpciRecord(data);

		},
		error : function(xhr) {
			alert(xhr.responseText);
		}
	});

}
function displayNpciRecord(data) {
	var obj = data;

	if (!$.trim(obj)) {
		// document.getElementById("emptyResponse").style.visibility =
		// "visible";
		//alert("No Data Found for NPCI!!");
	} else {

		var tableBody = "";

		for ( var i in data) {

			var tableRow = "";

			tableRow += "<td align='center' >"
					+ (data[i].customer_REFERNCE_NUMBER) + "</td>";
			tableRow += "<td align='center' >" + (data[i].upi_TRANSACTION_ID)
					+ "</td>";
			tableRow += "<td align='center' >" + (data[i].cycle) + "</td>";
			tableRow += "<td align='center' >" + (data[i].from_ACCOUNT_TYPE)
					+ "</td>";
			tableRow += "<td align='center' >"
					+ (data[i].merchant_CATEGORY_CODE) + "</td>";
			tableRow += "<td align='center' >" + (data[i].payee_PSP_CODE)
					+ "</td>";
			tableRow += "<td align='center' >" + (data[i].payer_PSP_CODE)
					+ "</td>";
			tableRow += "<td align='center' >" + (data[i].processed) + "</td>";
			tableRow += "<td align='center' >" + (data[i].reserved2) + "</td>";
			tableRow += "<td align='center' >" + (data[i].response_CODE)
					+ "</td>";
			tableRow += "<td align='center' >" + (data[i].to_ACCOUNT_TYPE)
					+ "</td>";
			tableRow += "<td align='center' >" + (data[i].transaction_AMOUNT)
					+ "</td>";
			tableRow += "<td align='center' >" + (data[i].transaction_DATE)
					+ "</td>";
			tableRow += "<td align='center' >" + (data[i].transaction_TIME)
					+ "</td>";
			tableRow += "<td align='center' >" + (data[i].transaction_TYPE)
					+ "</td>";
			tableRow += "<td align='center' >" + (data[i].file_NAME) + "</td>";

			tableBody = tableBody + "<tr id='datarow' class='datarow'>"
					+ tableRow + "</tr>";

		}
		$('#singleUpiTable3Body').append(tableBody);
		$('#singleUpiTable3').show();
		$("#singleUpiTable3Div").show();

	}
}