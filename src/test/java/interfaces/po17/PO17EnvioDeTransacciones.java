package interfaces.po17;

import static org.testng.Assert.assertFalse;

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

public class PO17EnvioDeTransacciones extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PO17_001_EnvioDeTransacciones(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/		
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbTpe = new SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,GlobalVariables.DB_PASSWORD_AVEBQA);
		
/**
* Variables ******************************************************************************************
* 
* 
*/		
		String tdcQueryFecha = "SELECT TO_CHAR( TRUNC(LAST_DATE), 'RRRR-MM-DD HH24:MI:SS'), TO_CHAR( TRUNC(SYSDATE-1), 'RRRR-MM-DD' ) || ' 23:59:59'"
				+ " FROM (SELECT MAX(LAST_EXEC_DATE) AS LAST_DATE "
				+ " FROM posuser.WM_TSF_CONTROL )";
		
		String tdcQueryTpe = "SELECT COUNT(APPLICATION) "
				+ " FROM TPEUSER.TPE_FR_TRANSACTION "
				+ " WHERE APPLICATION = 'TSF' "
				+ " AND CREATION_DATE >= TO_DATE( '%s', 'RRRR-MM-DD HH24:MI:SS')"
//				+ " AND CREATION_DATE <= TO_DATE( SYSDATE-1, 'RRRR-MM-DD HH24:MI:SS' ) "
				+ " AND OPERATION = 'TRN03'  "
				+ " AND WM_CODE = '101' "
				+ " ORDER BY CREATION_DATE DESC";
		
		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'PO17'"
				+ " and  start_dt >= TRUNC(SYSDATE)"
			    + " order by start_dt desc)"
				+ " where rownum = 1";
		
		String tdcQueryErrorId =" SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID= %s"
				+ " and rownum = 1"; 
		
		String tdcQueryDetalle = "SELECT TRANSINT_ID, TIPO_PROCESO, PLAZA, WM_RUN_ID "
				+ " FROM xxfc.XXFC_TRANS_INTERMEDIARIO_ENC "
				+ " WHERE WM_RUN_ID = %s "
				+ " ORDER BY TRANSINT_ID DESC";
		
		String tdcQueryDet = "SELECT DET_TRANSINT_ID, TRANSINT_ID, PLAZA, TIENDA, TIPO_MOVIMIENTO "
				+ " FROM xxfc.XXFC_TRANS_INTERMEDIARIO_DET"
				+ " WHERE TRANSINT_ID IN (%s)"
				+ "ORDER BY DET_TRANSINT_ID DESC";
		
		String tdcQueryEjecucion = "SELECT RUN_ID, LAST_EXEC_DATE, LAST_EXEC_USER "
				+ " FROM posuser.WM_TSF_CONTROL "
				+ " WHERE RUN_ID = %s"
				+ " AND TRUNC(LAST_EXEC_DATE) = TO_DATE(SYSDATE) "
				+ " ORDER BY LAST_EXEC_DATE DESC";
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//Paso 1 *************************			
		addStep("Comprobar la fecha de última ejecución registrada en la tabla WM_TSF_CONTROL del POS en la BD POS.");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryFecha);
		
		SQLResult fechaResult = executeQuery(dbPos, tdcQueryFecha);
		String fecha = fechaResult.getData(0, "TO_CHAR(TRUNC(LAST_DATE),'RRRR-MM-DDHH24:MI:SS')");
		
		
		boolean info = fechaResult.isEmpty();
		
		if (!info) {
			
			testCase.addQueryEvidenceCurrentStep(fechaResult);
		}
		
		System.out.println(info);
		
		assertFalse(info, "No se obtiene información de la consulta");

//Paso 2 *************************			
		addStep("Validar que en la tabla TPE_FR_TRANSACTION exista informacion para procesar en la BD TPE.");
		
		System.out.println(GlobalVariables.DB_HOST_FCTPE);
		
		String tpeFormat = String.format(tdcQueryTpe, fecha);
		SQLResult tpeResult = executeQuery(dbTpe, tpeFormat);
		System.out.println(tpeFormat);
		
		
		boolean tpe = tpeResult.isEmpty();
		
		if (!tpe) {
			
			testCase.addQueryEvidenceCurrentStep(tpeResult);
		}
		
		System.out.println(tpe);
		
		assertFalse(tpe, "No se obtiene información de la consulta");		
		
// paso 3 *************************
		addStep("Ejecutar el servicio: PO17.Pub:run.");
		String status = "S";
		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
		PakageManagment pok = new PakageManagment(u, testCase);
		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword( data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id ;
		String contra =   "http://"+user+":"+ps+"@"+server+":5555";
		u.get(contra);
		
		pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));		
		
// paso 4 *************************
		addStep("Validar en la BD WMLOG que la interfaz terminó sin errores.");
		
		SQLResult query = executeQuery(dbLog, tdcQueryIntegrationServer);	
		String status1 = query.getData(0, "STATUS");
		run_id = query.getData(0, "RUN_ID");
		System.out.println(tdcQueryIntegrationServer);
		
		boolean valuesStatus = status1.equals(searchedStatus);//Valida si se encuentra en estatus R
		while (valuesStatus) {
			
			query = executeQuery(dbLog, tdcQueryIntegrationServer);	
			status1 = query.getData(0, "STATUS");
			run_id = query.getData(0, "RUN_ID");
		 
		 u.hardWait(2);
		 
		}
		
		boolean successRun = status1.equals(status);//Valida si se encuentra en estatus S
		    if(!successRun){
		   
		   String error = String.format(tdcQueryErrorId, run_id);
		   SQLResult paso2 = executeQuery(dbLog, error);
		   
		   boolean emptyError = paso2.isEmpty();
		   
		   if(!emptyError){  
		   
		    testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
		   
		    testCase.addQueryEvidenceCurrentStep(paso2);
		   
		   }
		}
		    testCase.addQueryEvidenceCurrentStep(query);		

//Paso 5 *************************			
		addStep("Verificar los encabezados insertados a nivel plaza—Tabla XXFC_TRANS_INTERMEDIARIO_ENC.");
		
		System.out.println(GlobalVariables.DB_HOST_Ebs);
		
		String detalleFormat = String.format(tdcQueryDetalle, run_id);
		SQLResult detalleResult = executeQuery(dbEbs, detalleFormat);
		System.out.println(detalleFormat);
		String transint="";
		
		boolean detalle = detalleResult.isEmpty();
		
		if (!detalle) {
			 transint = detalleResult.getData(0, "TRANSINT_ID");
			testCase.addQueryEvidenceCurrentStep(detalleResult);
		}
		
		System.out.println(detalle);
		
		assertFalse(detalle, "No se obtiene información de la consulta");	
		
//Paso 6 ***********************
		addStep("Verificar el detalle insertado a nivel tienda-- Tabla XXFC_TRANS_INTERMEDIARIO_DET.");
		System.out.println(GlobalVariables.DB_HOST_Ebs);
		
		String detFormat = String.format(tdcQueryDet, transint);
		SQLResult detResult = executeQuery(dbEbs, detFormat);
		System.out.println(detalleFormat);
		
		
		boolean det = detResult.isEmpty();
		
		if (!det) {
			
			testCase.addQueryEvidenceCurrentStep(detResult);
		}
		
		System.out.println(det);
		
		assertFalse(det, "No se obtiene información de la consulta");	
		
//Paso 7 ************************
		addStep("Consultar que se insertó registro de la ultima ejecución de la interfaz.");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryFecha);
		
		String ejecucionFormat = String.format(tdcQueryEjecucion, run_id);
		SQLResult ejecucionResult = executeQuery(dbPos, ejecucionFormat);
				
		boolean ejecucion = ejecucionResult.isEmpty();
		
		if (!ejecucion) {
			
			testCase.addQueryEvidenceCurrentStep(ejecucionResult);
		}
		
		System.out.println(ejecucion);
		
		assertFalse(ejecucion, "No se obtiene información de la consulta");
		

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
		return "Construido. Validar el envío de diferentes transacciones y enviarlas  a la base de datos de Finanzas.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PO17_001_EnvioDeTransacciones";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
