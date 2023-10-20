package interfaces.eo3;

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
import utils.sql.SQLUtil;

public class EO3_ValidaProcesamientoArchivoErroneo extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
		 ********************************************************************************************************************************************/
		// Tabla ORAFIN/EBS
		SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA);

		/**
		 * ALM
		 * Validar el procesamiento de un archivo erroneo
		 * NOTA: falta validar archivos por ftp
		 * 
		 */

		/*
		 * Variables
		 ******************************************************************************************************************************************/
		// Paso 1
		String ValidFech = "SELECT movto_date, COUNT(1) AS pendientes " + "FROM wmuser.wm_televisa_inbound_docs "
				+ "WHERE status != 'F'  " + "AND movto_date >= TRUNC(SYSDATE - 6)  GROUP BY movto_date";
	
		// Paso 4

		String ValidLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER " + "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'EO3_EXTRACT'  " + "AND START_DT >= TRUNC(SYSDATE) "
				+ "AND STATUS = 'E'  ORDER BY START_DT DESC";
	
		// Paso 5
		String ValidReg = "SELECT id,status,doc_type,doc_name, run_id_receive  "
				+ "FROM wmuser.wm_televisa_inbound_docs  " + "WHERE run_id_receive= '%s'  " + "AND DOC_TYPE ='MUL' 	"
				+ "AND status = 'F'";
		



		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ " FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE='EO3_EXTRACT' "
				+ " ORDER BY START_DT DESC) where rownum <=1 ";

// *****************************************PASO 1**************************************************************************		

		addStep("Validar que no existan fechas con informacion procesada con una antig�edad de 7 d�as.");

		System.out.println(ValidFech);
		SQLResult ExecValidFech = dbEbs.executeQuery(ValidFech);

		boolean ValidFechRes = ExecValidFech.isEmpty();

		if (!ValidFechRes) {

			testCase.addQueryEvidenceCurrentStep(ExecValidFech);
		}

		System.out.println(ValidFechRes);

		assertFalse("No se encontraron registros a procesar ", ValidFechRes);

		// ******************************* PASO 2********************************************************************************

		/**
		 * Validar que en el FileSystem del servidor estan presentes los archivos de la
		 * plaza a procesar. 
		 * Ruta: /u01/BATCH/EO3/working 
		 * Host: 10.80.20.11 
		 * Port:21
		 * User: wmuser Pass: ?
		 */

		// ******************************* PASO 3
		// ********************************************************************************

		addStep("Ejecutar el servicio EO3.Pub:runExtract. El servicio sera invocado con el job execEO3Extract");

		// Utileria

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);

		String status1 = is.getData(0, "STATUS");// guarda el run id de la
													// ejecuci�n

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se
																// encuentra en
																// estatus R

		while (valuesStatus) {

			status1 = is.getData(0, "STATUS");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(3);
		}

		// *******************************PASO 4********************************************************************************
	
		addStep("Validar que el registro de la tabla wm_log_run termine en estatus 'E'.");

		String RUN_ID = "";
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(ValidLog);
		SQLResult ExecLog = dbLog.executeQuery(ValidLog);

		boolean LogRequest = ExecLog.isEmpty();

		if (!LogRequest) {
			RUN_ID = ExecLog.getData(0, "RUN_ID");
			System.out.println("RUN_ID: " + RUN_ID);
			testCase.addQueryEvidenceCurrentStep(ExecLog);
		}

		System.out.println(LogRequest);
		assertFalse(LogRequest, "No se muestra  la informacion.");

//		********************************Paso 5 ******************************************************************************

		addStep("Validar el registro de los archivos descargados en la tabla wm_televisa_inbound_docs y con estatus igual a 'F'.");
		String ID = "";
		String doc_name = "";
		System.out.println(GlobalVariables.DB_HOST_Ebs);
		String ValidRegFormat = String.format(ValidReg, RUN_ID);
		System.out.println(ValidRegFormat);

		SQLResult ValidRegFormatExec = dbEbs.executeQuery(ValidRegFormat);

		boolean ValidRegFormatRes = ValidRegFormatExec.isEmpty();

		if (!ValidRegFormatRes) {

			testCase.addQueryEvidenceCurrentStep(ValidRegFormatExec);
			ID = ValidRegFormatExec.getData(0, "ID");
			System.out.println("ID: " + ID);

			doc_name = ValidRegFormatExec.getData(0, "doc_name");
			System.out.println("doc_name: " + doc_name);
		}

		System.out.println(ValidRegFormatRes);

		assertFalse(
				"No hay registro de los archivos descargados en la tabla wm_televisa_inbound_docs y con estatus igual a 'L'. ",
				ValidRegFormatRes);

		// ******************************************** Paso 6  ******************************************************************
		
		addStep("Validar que el archivo descargado [wm_televisa_inbound_docs.doc_name] sea eliminado del directorio de trabajo 'working'.");

		/**
		 * Validar que en el FileSystem del servidor estan presentes los archivos de la
		 * plaza a procesar. 
		 * Ruta: /u01/BATCH/EO3/working 
		 * Host: 10.80.20.11 
		 * Port:21
		 * User: wmuser 
		 * Pass: ?
		 */
		String pass = "";// Se desconoce contrase�a
		FTPUtil ftp = new FTPUtil("10.80.20.11", 21, "wmuser", pass);

		String ruta = "/u01/BATCH/EO3/working/" + doc_name;
		System.out.println("Ruta: " + ruta);

		if (ftp.fileExists(ruta)) {

			testCase.addTextEvidenceCurrentStep("Se encontro archivo en la ruta: " + ruta);

		} else {

			System.out.println("No Existe");

		}

		assertFalse(ftp.fileExists(ruta), "Existen archivos en la ruta FTP: " + ruta);

//		************************* Paso 7 ***************************************************************
		
		/**
		 * Validar que el archivo descargado sea respaldado en la carpeta 'reject',
		 *  el nombre debe tener concatenada la fecha actual: [wm_televisa_inbound_docs.doc_name] + 'yyyyMMddhhmmss' + .zip
		 * Ruta:/u01/BATCH/EO3/working 
		 * Host: 10.80.20.11 
		 * Port:21 
		 * User: wmuser 
		 * Pass: ?
		 */

	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "EO3_ValidaProcesamientoArchivoErroneo";
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

