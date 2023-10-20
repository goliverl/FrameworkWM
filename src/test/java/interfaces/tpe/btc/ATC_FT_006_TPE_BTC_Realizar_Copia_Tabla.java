package interfaces.tpe.btc;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import om.TPE_TSF;
import util.GlobalVariables;
import util.RequestUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

import utils.sql.SQLUtil;
import utils.sql.SQLResult;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ATC_FT_006_TPE_BTC_Realizar_Copia_Tabla extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_006_TPE_BTC_Realizar_Copia_Tabla_Test(HashMap<String, String> data) throws Exception {
		
/** UTILERIA *********************************************************************/	

		utils.sql.SQLUtil dbTPE = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		utils.sql.SQLUtil dbTPEREP = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA);
		
		SeleniumUtil u;
		PakageManagment pok;	
				
/** VARIABLES *********************************************************************/	
				
		String status = "S"; // status exitoso
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id;
					
		String tdcTablaCopia = "SELECT  * " + 
				" FROM FROM TPEREP.TPE_TABLAS_COPIA" + 
				" WHERE TABLA = '"+ data.get("tabla")+"'";
		
		String tdcTransaccionesFechaACopiar = "SELECT * " + 
				" FROM TPEUSER."+ data.get("tabla")+" " + 
				" WHERE CREATION_DATE BETWEEN TO_DATE('<FECHA_COPIADA>') -30 " + 
				" AND TO_DATE('<FECHA_COPIADA>') +1" + 
				" ORDER BY CREATION_DATE ASC";
		
		String tdcControlCopia = "SELECT * " + 
				" FROM TPE_CONTROL_COPIA " + 
				" WHERE RUN_ID='%s'";
			
		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server "
				+ "FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE LIKE '%    %' "
				+ "ORDER BY START_DT DESC) where rownum <=1";
		
		String consultaError1 = "select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";

		String consultaError2 = "select * from (select description,MESSAGE "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";

		String consultaError3 = "select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1"; //FCWMLQA 
		
				
/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
		
		/* PASO 1 *********************************************************************/

		addStep("Revisar los datos de configuracion del proceso de la tabla a copiar en TPEREP.TPE_TABLAS_COPIA");

		System.out.println(tdcTablaCopia);
		SQLResult paso1 = executeQuery(dbTPEREP, tdcTablaCopia);
		
		boolean validationDb = paso1.isEmpty();
		
		if (!validationDb) {				
			testCase.addQueryEvidenceCurrentStep(paso1);
		}	

		assertFalse(validationDb, "No se encontró un registro en la tabla TPEREP.TPE_TABLAS_COPIA.");
		
		/* PASO 2 *********************************************************************/

		addStep("Validar que la tabla cuente con las transacciones a copiar, que sean menor a 1 dia mas de la ultima ejecucion y mayor de 30 dias a esta fecha(FECHA_COPIADA +1).");

		System.out.println(tdcTransaccionesFechaACopiar);
		SQLResult paso2 = executeQuery(dbTPE, tdcTransaccionesFechaACopiar);
		
		boolean validation2 = paso2.isEmpty();
		
		if (!validation2) {				
			testCase.addQueryEvidenceCurrentStep(paso2);
		}	

		assertFalse(validation2, "No se encontró un registro en la tabla " + data.get("tabla"));
		
		/* PASO 3 *********************************************************************/
		
		addStep("Ejecutar el servicio runTPECopia.");
		
		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWM(data.get("interfase"), data.get("servicio"), null);
		System.out.println("Respuesta dateExecution " + dateExecution);
		System.out.println(tdcIntegrationServerFormat);
		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");
		System.out.println("RUN_ID = " + run_id + "\t Status: " + status1 );

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R

		while (valuesStatus) {			
			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);
			u.hardWait(2);			
		}

		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S		

		if (!successRun) {
			String error = String.format(consultaError1, run_id);
			String error1 = String.format(consultaError2, run_id);
			String error2 = String.format(consultaError3, run_id);
	
			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();
			
			if (!emptyError) {
				testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
				testCase.addQueryEvidenceCurrentStep(errorr);
				
				SQLResult errorIS = dbLog.executeQuery(error1);
				testCase.addQueryEvidenceCurrentStep(errorIS);
				
				SQLResult errorIS2 = dbLog.executeQuery(error2);
				testCase.addQueryEvidenceCurrentStep(errorIS2);
			}
		}	
		    
		/* PASO 4 *********************************************************************/		    
		    
	    addStep("Verificar el estatus con el cual fue terminada la ejecución de la interface en la tabla WM_LOG_RUN del usuario WMLOG.");		
		
		boolean validateStatus = status.equals(status1);
		System.out.println("VALIDACION DE STATUS = S - " + validateStatus);
	
		testCase.addQueryEvidenceCurrentStep(is);					
		assertFalse(!validateStatus, "La ejecución de la interfaz no fue exitosa");
		
		/* PASO 5 *********************************************************************/

		addStep("Revisar los datos de configuracion del proceso de la tabla a copiar en TPEREP.TPE_TABLAS_COPIA");

		String query = String.format(tdcControlCopia, run_id);
		System.out.println(query);
		SQLResult paso4 = executeQuery(dbTPEREP, query);

		boolean validation4 = paso4.isEmpty();
		
		if (!validation4) {				
			testCase.addQueryEvidenceCurrentStep(paso4);
		}	

		assertFalse(validation4, "No se encontró un registro en la tabla TPEREP.TPE_CONTROL_COPIA.");
		
		/* PASO 6 *********************************************************************/

		addStep("Validar que la tabla cuente con los datos copiados");

		System.out.println(tdcTransaccionesFechaACopiar);
		SQLResult paso6 = executeQuery(dbTPEREP, tdcTransaccionesFechaACopiar);
		
		boolean validation6 = paso6.isEmpty();
		
		if (!validation6) {				
			testCase.addQueryEvidenceCurrentStep(paso6);
		}	

		assertFalse(validation6, "No se encontron los registros en la tabla TPEUSER.POS_TRANSACTION");

	}	
	
	@Override
	public String setTestFullName() {
		return "ATC_FT_006_TPE_BTC_Realizar_Copia_Tabla";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Realizar el copiado de la tabla de la BD FCTPEQA hacia la BD TPEREPQA con la informacion que tengan mas de 30 dias de haberse creado en la tabla.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
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

