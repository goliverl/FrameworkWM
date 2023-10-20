package interfaces.CP01_CO;

import static org.testng.Assert.assertFalse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLUtil;
import utils.sql.SQLResult;

public class CP01_validaCambiosCol extends BaseExecution{
	
	@Test(dataProvider = "data-provider") 
	public void ATC_FT_002_CP01_validaCambiosCol(HashMap<String, String> data) throws Exception {
		
/* Utiler�as *********************************************************************/		
		
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbCNT = new SQLUtil(GlobalVariables.DB_HOST_FCIASQA, GlobalVariables.DB_USER_FCIASQA, GlobalVariables.DB_PASSWORD_FCIASQA);
		
		/**
		 * ALM
		 * Validar que se envien los cambios al Buzon
		 * (es el mismo que el de mexico pero con BD de colombia)
		 */
	
/**
* Variables ******************************************************************************************
* 
*/
	    //Paso 1
		String ValidaPOS_CAMBIOSL = "SELECT PLAZA_CR, TIENDA_CR, WM_STATUS "
				+ "FROM WMUSER.POS_CAMBIOS  WHERE WM_STATUS = 'L' AND PLAZA_CR = '" + data.get("plaza") +"' AND TIENDA_CR = '" + data.get("tienda") +"'";
		
		//Paso 2
		String validaEmail = "SELECT EMAIL, A.GROUP_ID, GROUP_NAME, INTERFACE_NAME "
				+ "FROM WMLOG.WM_LOG_GROUP A, WMLOG.WM_LOG_USER_GROUP B, WMLOG.WM_LOG_USER C "
				+ "WHERE A.GROUP_ID = B.GROUP_ID "
				+ "AND B.USER_ID = C.USER_ID "
				+ "AND A.GROUP_NAME = 'CP01' "
				+ "AND A.INTERFACE_NAME = 'CP01' "
				+ "AND C.PLAZA IS NULL";
		//Paso 3
		// consultas de error
				String consultaError1 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
						+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1"; // dbLog
				String consultaError2 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
						+ "where RUN_ID='%s')WHERE rownum <= 1"; // dbLog
				String consultaError3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
						+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1"; // dbLog
			      
	   //Paso 4
				
		      String  tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 \r\n" + 
		      		"FROM WMLOG.WM_LOG_RUN Tbl WHERE INTERFACE = 'CP01' AND STATUS='S' " + 
		      		"ORDER BY START_DT DESC) where rownum <=1";
		      
	 //Paso 5
		      
		      String ValidaPOS_CAMBIOSE = "SELECT WM_RUN_ID, PLAZA_CR, TIENDA_CR, WM_STATUS, WM_SEND_DT \r\n" + 
		      		"FROM WMUSER.POS_CAMBIOS \r\n" + 
		      		"WHERE WM_STATUS = 'E' \r\n" + 
		      		"AND TRUNC(WM_SEND_DT) =  to_date('%s', 'dd/MM/yyyy') \r\n" + 
		      		"AND PLAZA_CR = '" + data.get("plaza") +"' \r\n" + 
		      		"AND TIENDA_CR = '" + data.get("tienda") +"' \r\n" + 
		      		"AND WM_RUN_ID = '%s'";
		
		      
		      
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//		Paso 1	************************ 
		addStep("Que exista registro de la Plaza " + data.get("plaza") +" Y TIENDA  " + data.get("tienda") +" con STATUS L en la tabla POS_CAMBIOS de la BD CNT. ");
		System.out.println(GlobalVariables.DB_HOST_FCIASQA);
		System.out.println(ValidaPOS_CAMBIOSL);
		
		SQLResult ValidaPOS_CAMBIOSL_Res = executeQuery(dbCNT, ValidaPOS_CAMBIOSL);
		
		boolean validaStatusL = ValidaPOS_CAMBIOSL_Res.isEmpty();
		
			if (!validaStatusL) {
		
			testCase.addQueryEvidenceCurrentStep(ValidaPOS_CAMBIOSL_Res);
			
						} 
		
		System.out.println(validaStatusL);

		assertFalse(validaStatusL, "No se encontro registro con las tiendas correspondientes");
		
//		Paso 2	************************ 
		
		addStep("Comprobar la direccion de email para recibir las natificaciones en las tablas WM_LOG_GROUP, WM_LOG_USER_GROUP y WM_LOG_USER para el GROUP_NAME igual a CP01 y INTERFACE_NAME igual a CP01 en la BD WMLOG.");
	
		System.out.println(validaEmail);
		
		SQLResult validaEmail_Res = executeQuery(dbLog, validaEmail);
		
		boolean validaEmailRes = validaEmail_Res.isEmpty();
		
			if (!validaEmailRes) {
		
			testCase.addQueryEvidenceCurrentStep(validaEmail_Res);
			
						} 
		
		System.out.println(validaEmailRes);

		assertFalse(validaEmailRes, "No se conoce la direcci�n de e-mail al cual llegar� el correo de notificaci�n");
		
//**********************************************Paso 3 ****************************************************************************************************************
		
		addStep("Se ejecuta el proceso CP01.Pub:run ");

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
 
		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interface"), data.get("servicio"));
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

			u.hardWait(2);

		}

		boolean successRun = status1.equals(status);// Valida si se encuentra en
		
		System.out.println(successRun);
													// estatus S

		if (!successRun) {

			String error = String.format(consultaError1, run_id);
			String error1 = String.format(consultaError2, run_id);
			String error2 = String.format(consultaError3, run_id);

			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontr� un error en la ejecuci�n de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(errorr);

			}

			SQLResult errorIS = dbLog.executeQuery(error1);

			boolean emptyError1 = errorIS.isEmpty();

			if (!emptyError1) {

				testCase.addQueryEvidenceCurrentStep(errorIS);

			}

			SQLResult errorIS2 = dbLog.executeQuery(error2);
			boolean emptyError2 = errorIS2.isEmpty();

			if (!emptyError2) {

				testCase.addQueryEvidenceCurrentStep(errorIS2);

			}
		}
//*******************************************************Paso 4************************************************************************
		
		addStep("Se valida que el STATUS sea igual a S en la tabla WM_LOG_RUN  de la BD WM_LOG  donde INTERFACE = CP01.");
		
		System.out.println(tdcIntegrationServerFormat);
		
		SQLResult ValidaStatusS = executeQuery(dbLog, tdcIntegrationServerFormat);
		
		boolean validaStatus = ValidaStatusS.isEmpty();
		
			if (!validaStatus) {
		
			testCase.addQueryEvidenceCurrentStep(ValidaStatusS);
			
						} 
		
		System.out.println(validaStatus);

		assertFalse(validaStatus, "Error en la ejecucion de la interfaz");
		
//**************************************************Paso 5 **************************************************************************
		
addStep("Se valida que el STATUS sea igual a S en la tabla WM_LOG_RUN  de la BD WM_LOG  donde INTERFACE = CP01.");

          Date fecha = new Date();// obtener fecha del sistema
          
         SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy"); // formato
																		// fecha
        String date = formatter.format(fecha);     

        String formatoFecha = date.substring(0, 10);
        
        System.out.println(formatoFecha);
        
        

         String ValidaPOS_CAMBIOSEFormat = String.format(ValidaPOS_CAMBIOSE, formatoFecha,run_id);
		
		System.out.println(ValidaPOS_CAMBIOSEFormat);
		
		
		
		SQLResult ValidaPOS_CAMBIOSERes = executeQuery(dbCNT,ValidaPOS_CAMBIOSEFormat);
		
		boolean validaCambiosE = ValidaPOS_CAMBIOSERes.isEmpty();
		
			if (!validaCambiosE) {
		
			testCase.addQueryEvidenceCurrentStep(ValidaPOS_CAMBIOSERes);
			
						} 
		
		System.out.println(validaCambiosE);

		assertFalse(validaCambiosE, "Los campos de wm_status, wm_send_dt y wm_run_id no  fueron actualizados.");
		

		
		
		
		
		
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

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminada. Esta interfaz mantiene sincronizada la informaci�n del CNT con el POS de la tienda";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_CP01_validaCambiosCol";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}
