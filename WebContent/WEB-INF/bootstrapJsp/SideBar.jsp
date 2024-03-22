<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@page import="com.recon.model.*"%>
<aside class="main-sidebar">
	<!-- sidebar: style can be found in sidebar.less -->
	<section class="sidebar">
		<ul class="sidebar-menu">
			<li class="header"><img src="dist/img/cub_logo_intbg.png"
				style="width: 100%; margin-top: 10px;"></li>
			<li class="active"><a href="Menu.do"> <i class="fa fa-th"></i>
					<span>Dashboard</span> <!-- <small class="label pull-right bg-green">new</small> -->
			</a></li>

			<%
			String sk = ((LoginBean) request.getSession().getAttribute("loginBean")).getUser_name();
			if (sk.equals("ADMIN")) {
			%>

			<li class="treeview"><a href="#"> <i class="fa fa-dashboard"></i>
					<span>Administrator</span> <i class="fa fa-angle-left pull-right"></i>
			</a>
				<ul class="treeview-menu">
					<li><a href="UserManager.do"><i class="fa fa-angle-right"></i>
							User Manager</a></li>
					<!-- <li><a href="RoleManager.do"><i class="fa fa-angle-right"></i> Role Manager</a></li> -->
				</ul></li>
			<%
			}
			%>




			<li class="treeview"><a href="ManualUpload.do"> <i
					class="fa fa-upload"></i> <span>File Upload</span> <!-- <i class="fa fa-angle-left pull-right"></i> -->
			</a></li>

			<li class="treeview"><a href="CbsDatafetch.do"> <i
					class="fa fa-upload"></i> <span>CBS Data Fetch</span>
			</a></li>

			<li class="treeview"><a href="#"> <i class="fa fa-wifi"></i>
					<span>Networks</span> <i class="fa fa-angle-left pull-right"></i>
			</a>
				<ul class="treeview-menu">
					<li><a href="#"><i class="fa fa-circle-o"></i><img
							src="dist/img/cardicons/rupay.jpg" alt="rupay"> E-COM POS <i
							class="fa fa-angle-left pull-right"></i></a>
						<ul class="treeview-menu">
							<li><a href="ReconProcess.do?category=RUPAY"><i
									class="fa fa-angle-right"></i> Process</a></li>
							<li><a href="getRupayUnmatchedTTUM.do?category=RUPAY"><i
									class="fa fa-angle-right"></i> Generate TTUM </a> <!--    <a href="RupayInternationalTTUM.do?category=RUPAY"><i class="fa fa-angle-right"></i> Generate International TTUM </a> -->
							</li>
							<!-- <li>
                      <a href="UploadTTUM.do?category=RUPAY"><i class="fa fa-angle-right"></i> Upload TTUM </a>
                    </li> -->
							<li><a href="DownloadDhanaReports1.do?category=RUPAY"><i
									class="fa fa-angle-right"></i>Reports</a></li>
							<!-- <li>
                      <a href="DownloadReports.do?category=RUPAY"><i class="fa fa-angle-right"></i> Generate Reports </a>
                    </li> -->
							<li><a href="manulRollBack.do?category=RUPAY"><i
									class="fa fa-angle-right"></i> Recon RollBack</a></li>

							<!--   <li><a href="manualKnockoff.do?category=RUPAY"><i class="fa fa-angle-right"></i> Knockoff Records</a></li> -->

							<li><a href="rupayNetworkAdjustment.do?"><i
									class="fa fa-angle-right"></i>Adjustment File Upload</a></li>

							<li><a href="AdjustmentTTUM.do?"><i
									class="fa fa-angle-right"></i>Adjustment TTUM</a></li></li></li>

			<li><a href="#"><i class="fa fa-circle-o"></i><img
					src="dist/img/cardicons/rupay.jpg" alt="rupay"> ECOM - POS
					SETTLEMENT <i class="fa fa-angle-left pull-right"></i></a>
				<ul class="treeview-menu">
					<li><a href="RupayFileUpload.do?category=RUPAY"><i
							class="fa fa-angle-right"></i> Settlement File Upload</a></li>

					<!-- <li><a href="NCMCFileUpload.do?category=RUPAY"><i class="fa fa-angle-right"></i> NCMC Settlement File Upload</a></li> -->

					<li><a href="RupaySettlementProcess.do?category=RUPAY"><i
							class="fa fa-angle-right"></i> Settlement Processing</a></li>

					<li><a href="RupaySettlementProcessTTUM.do?category=RUPAY"><i
							class="fa fa-angle-right"></i> Settlement TTUM</a></li>
					<li><a href="RupaySettFileRollback.do?category=CASHNET"><i
							class="fa fa-angle-right"></i>Settlement File Rollback</a></li>
					<li><a href="PresentmentFileUpload.do?category=RUPAY"><i
							class="fa fa-angle-right"></i>PresentMent File Upload</a></li>
					<li><a href="CashAtPos.do?category=RUPAY"><i
							class="fa fa-angle-right"></i>Cash At POS</a></li>
				</ul></li>

		</ul>
		</li>





		<li>
		<li><a href="#"><i class="fa fa-circle-o"></i><img
				src="dist/img/cardicons/nfs.jpg" alt="rupay"> ATM <i
				class="fa fa-angle-left pull-right"></i></a>
			<ul class="treeview-menu">
				<li><a href="ReconProcess.do?category=NFS"><i
						class="fa fa-angle-right"></i> Process</a></li>
				<li><a href="getNFSUnmatchedTTUM.do?category=NFS"><i
						class="fa fa-angle-right"></i> Generate TTUM </a></li>

				<li><a href="DownloadDhanaReports.do?category=NFS"><i
						class="fa fa-angle-right"></i>Reports</a></li>

				<li><a href="manulRollBack.do?category=NFS"><i
						class="fa fa-angle-right"></i> Recon RollBack</a></li></li>

		<li><a href="NTSLFileUpload.do?category=NFS"><i
				class="fa fa-angle-right"></i> NTSL Upload</a></li>
		<li><a href="NTSLRollback.do?category=NFS"><i
				class="fa fa-angle-right"></i> NTSL Upload Rollback</a></li>

		<li><a href="AdjustmentFileUpload.do?category=NFS"><i
				class="fa fa-angle-right"></i>Adjustment Report Upload</a></li>

		<li><a href="NFSSettlement.do?category=NFS"><i
				class="fa fa-angle-right"></i>Settlement Report</a></li>




		<li><a href="NFSSettVoucher.do?category=NFS"><i
				class="fa fa-angle-right"></i>Settlement Rectification</a></li>

		<li><a href="NFSSettlementVouc.do?category=NFS"><i
				class="fa fa-angle-right"></i>Settlement TTUM</a></li>

		<li><a href="NFSRawRollback.do?category=NFS"><i
				class="fa fa-angle-right"></i>RawFile Rollback</a></li>

		<li><a href="NFSAdjustmentTTUM.do?category=NFS"><i
				class="fa fa-angle-right"></i>Adjustment TTUM</a></li>

		<li><a href="LateRevTtum.do?category=RUPAY"><i
				class="fa fa-angle-right"></i>Late Reversal TTUM</a></li>



		</ul>
		</li>
		</li>


		</ul>
		</li>

		<li class="treeview"><a href="Logout.do"> <i
				class="fa fa-sign-out"></i> <span>Signout</span>

		</a></li>

		</ul>
	</section>
</aside>


<script>
	var selector = '.sidebar-menu li';

	$(selector).on('click', function() {

		$(selector).removeClass('active');
		$(this).addClass('active');
	});
</script>