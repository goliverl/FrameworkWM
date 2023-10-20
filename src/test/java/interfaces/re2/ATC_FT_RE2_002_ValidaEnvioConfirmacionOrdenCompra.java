package interfaces.re2;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.FTPUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class ATC_FT_RE2_002_ValidaEnvioConfirmacionOrdenCompra extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_RE2_002_ValidaEnvioConfirmacionOrdenCompra_test(HashMap<String, String> data) throws Exception {

		
		/*
		 * Utilerias
		 ********************************************************************************************************************************************/

		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,
				GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,
				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		
		/**
		 * ALM
		 * Validar el envio de las Confirmaciones de las Ordenes de compra (runConfirm)
		 */

		/*
		 * Variables
		 ******************************************************************************************************************************************/

		// Paso 1
		String supplier = "";
		
		String ConsReg = " SELECT VENDOR_ORDER_NO,SUPPLIER,LOCATION,LOC_TYPE,WM_STATUS "
				+ "FROM RMS100.FEM_VMI_ORDERS_HEAD  "
				+ "WHERE WM_STATUS IN ('A', 'R')";

		// Paso 2
		String IdMapSupp="";
		String CompReg =	"SELECT *  "
				+ "FROM WMUSER.WM_EDI_MAP_SUPPLIER S "
				+ "INNER JOIN WMUSER.WM_EDI_INTERCHANGE I ON S.SUPPLIER = I.SUPPLIER  "
				+ "AND S.STATUS = I.STATUS_TYPE  "
				+ "WHERE S.SUPPLIER = '%s'";
		
		
//	Paso 3
 
		String CompExist = "SELECT * "
		+ "FROM POSUSER.WM_EDI_FUNCTIONS A, POSUSER.WM_EDI_SUPPLIER_FUNCTION B "
		+ "WHERE A.ID = B.FUNCTION_ID "
		+ " AND B.SUP_ID = '%s' "
		+ "AND A.FUNCTION = 'ASN_OUT'";
		
		
//		*******
//		Paso 4
		String ValidLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER " + "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'RE2_Confirm'  " + "AND START_DT >= TRUNC(SYSDATE) "
				+ "AND STATUS = 'S'  ORDER BY RUN_ID DESC";

		String validThread = "SELECT THREAD_ID,PARENT_ID,NAME,START_DT,STATUS,ATT1,ATT2  " + "FROM WMLOG.WM_LOG_THREAD "
				+ "WHERE PARENT_ID = '%s' ";


//	Paso 6 
		String ValidInsert = "SELECT VENDOR_ORDER_NO,SUPPLIER,LOCATION,LOC_TYPE,WM_RUN_ID,WM_STATUS,WM_SENT_DATE,DOC_NAME  "
				+ "FROM RMS100.FEM_VMI_ORDERS_HEAD  "
				+ "WHERE WM_STATUS = 'C'  "
				+ "AND WM_RUN_ID = '%s'";

		
		

//	Paso  
		String ValidUpdate = "SELECT  DOC_NAME, DOC_TYPE,EDI_CONTROL,SUPPLIER,STATUS,DATE_SENT,RUN_ID  "
				+ "FROM WMUSER.EDI_OUTBOUND_DOCS "
				+ " WHERE STATUS = 'E'  "
				+ "AND RUN_ID = '%s' "
				+ "AND DOC_NAME = '%s'";

//****	

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ " FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE='RE2_Confirm' "
				+ " ORDER BY START_DT DESC) where rownum <=1 ";

//********************************************************************************************************************************************************************************		

		/* Pasos */

//************************************************Paso 1********************* ***********************************************************************************************************
	
		addStep("Consultar que existan registros en la tabla FEM_VMI_ORDERS_HEAD en la BD RETEK, donde WM_STATUS es igual a 'A' o 'R'.");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);

		System.out.println(ConsReg);

		SQLResult ConsRegRes = dbRms.executeQuery(ConsReg);

		boolean ValidaDatBool = ConsRegRes.isEmpty(); // checa que el string contenga datos

		if (!ValidaDatBool) {
			supplier = ConsRegRes.getData(0, "SUPPLIER");

			System.out.println("supplier= " + supplier);

			testCase.addQueryEvidenceCurrentStep(ConsRegRes); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(ValidaDatBool); // Si no, imprime la fechas
		assertFalse("No hay registros en la tabla FEM_VMI_ORDERS_HEAD en la BD RETEK, donde WM_STATUS es igual a 'A' o 'R'", ValidaDatBool);
																						

//*************************************************Paso 2***********************************************************************************************************************

		addStep("Comprobar que existen registros en las tablas WM_EDI_MAP_SUPPLIER y WM_EDI_INTERCHANGE de la BD RETEK");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);// RMS

		String VerifiConfFormat = String.format(CompReg, supplier);

		System.out.println(VerifiConfFormat);

		SQLResult verif = dbRms.executeQuery(VerifiConfFormat);

		boolean validRes = verif.isEmpty();

		if (!validRes) {
			IdMapSupp = verif.getData(0, "ID");
			System.out.println("IdMapSupp= " + IdMapSupp);
			testCase.addQueryEvidenceCurrentStep(verif);

		}

		System.out.println(validRes);
		assertFalse("No se muestran registros en las tablas WM_EDI_MAP_SUPPLIER y WM_EDI_INTERCHANGE", validRes);

//**********************************************************Paso 3*************************************************************************************************************		

		addStep("Comprobar que existen registros en las tablas WM_EDI_FUNCTIONS y WM_EDI_SUPPLIER_FUNCTION de la BD POSUSER.");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);// Pos

		String VerifiCompExist = String.format(CompExist, IdMapSupp);

		System.out.println(VerifiCompExist);

		SQLResult verifComp = dbPos.executeQuery(VerifiCompExist);

		boolean validResComp = verifComp.isEmpty();

		if (!validResComp) {

			testCase.addQueryEvidenceCurrentStep(verifComp);

		}

		System.out.println(validResComp);
		assertFalse("No se muestran registros en las tablas WM_EDI_FUNCTIONS y WM_EDI_SUPPLIER_FUNCTION", validResComp);
		
//*********************************************************Paso 4**************************************************************************************************
		addStep("Se ejecuta el servicio: RE2.Pub:runConfirm, solicitando el job: runRE2_Confirm");

		// Utileria

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String status = "S";

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWM(data.get("interfase"), data.get("servicio"), null);
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		String run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");// guarda el run id de la
													// ejecuci�n

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se
																// encuentra en
																// estatus R

		while (valuesStatus) {

			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(3);

		}

//*********************************Paso 5*********************************************************
	
		addStep("Verificar que la ejecucion sea registrada en las tablas de la BD WMLOG.");
		String RunID = "";
		String ThreadID = "";
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(ValidLog);
		SQLResult ExecLog = dbLog.executeQuery(ValidLog);

		boolean LogRequest = ExecLog.isEmpty();

		if (!LogRequest) {
			RunID = ExecLog.getData(0, "RUN_ID");

			String ValidThreadFormat = String.format(validThread, RunID);
			System.out.println(ValidThreadFormat);
			SQLResult ExecThread = dbLog.executeQuery(ValidThreadFormat);

			ThreadID = ExecThread.getData(0, "THREAD_ID");
			testCase.addQueryEvidenceCurrentStep(ExecLog);
			testCase.addQueryEvidenceCurrentStep(ExecThread);

		}

		System.out.println(LogRequest);
		assertFalse("No se muestra  la informaci�n.", LogRequest);

//********************************************Paso 6**************************************************************************************************************************

		addStep("Verificar que se actualizaron los datos en la tabla FEM_VMI_ORDERS_HEAD de la BD RETEK.");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);// RMS
		String Doc_Name ="";
		String ValidInsertFormat = String.format(ValidInsert, ThreadID);
		System.out.println(ValidInsertFormat);
		SQLResult ExecuteValidIns = dbRms.executeQuery(ValidInsertFormat);

		boolean ExecuteValidInsReq = ExecuteValidIns.isEmpty();

		if (!ExecuteValidInsReq) {
			Doc_Name = ExecuteValidIns.getData(0, "DOC_NAME");
			System.out.println("DOC NAME: "+ Doc_Name);
			testCase.addQueryEvidenceCurrentStep(ExecuteValidIns);

		}

		System.out.println(ExecuteValidInsReq);
		assertFalse("No se muestran datos insertados", ExecuteValidInsReq);

//*********************************************************Paso 7 **********************************************************************************************

		addStep("Verificar que se insert� el documento en la tabla EDI_OUTBOUND_DOCS de la BD RETEK.");
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		String ValidUpdtFormat = String.format(ValidUpdate, RunID, Doc_Name);
		System.out.println(ValidUpdtFormat);
		SQLResult ExecuteUpdtFormat = dbRms.executeQuery(ValidUpdtFormat);

		boolean ExecuteValidUpdtReq = ExecuteUpdtFormat.isEmpty();

		if (!ExecuteValidUpdtReq) {

			testCase.addQueryEvidenceCurrentStep(ExecuteUpdtFormat);

		}

		System.out.println(ExecuteValidUpdtReq);
		assertFalse("No se muestran datos insertados", ExecuteValidUpdtReq);
		
//		************************************************Paso 8**************************
		//Pendiente hasta tener contrase�a para conexion al servidor ftp
//		FTP_SERVERHOST: 10.80.1.108
//		FTP_SERVERPORT: 21
//		FTP_USERNAME: posuser
//		FTP_PASSWORD: ???????(Desconocido)
		
		
//		addStep("Comprobar que se envio el documento al servidor FTP: /u01/posuser/FEMSA_OXXO/TNDES/"
//				+ data.get("cr_plaza") + "/working/" + DOC);
//
//		FTPUtil ftp = new FTPUtil("10.182.92.13", 21, "posuser", "posuser");
//
//		String ruta = "/u01/posuser/FEMSA_OXXO/POS/" + data.get("cr_plaza") + "/working/" + DOC;
//		System.out.println("Ruta: " + ruta);
//
//		if (ftp.fileExists(ruta)) {
//
//			testCase.addTextEvidenceCurrentStep("Se encontro archivo en la ruta: /u01/posuser/FEMSA_OXXO/POS/"
//					+ data.get("cr_plaza") + "/working/" + DOC);
//
//		} else {
//
//			System.out.println("No Existe");
//
//		}
//
//		assertFalse(!ftp.fileExists(ruta), "No Existen archivos en la ruta FTP: " + ruta);


	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Validar el envio de las Confirmaciones de las Ordenes de compra (runConfirm)";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return null;
	}
}
