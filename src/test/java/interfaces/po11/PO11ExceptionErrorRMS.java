package interfaces.po11;

import java.util.ArrayList;
import java.util.HashMap;

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

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

public class PO11ExceptionErrorRMS extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_006_PO11_Excepcion_Error_RMS(HashMap<String, String> data) throws Exception {
		
/** UTILERIA *********************************************************************/	
		
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		
		SeleniumUtil u;
		PakageManagment pok;			

/** VARIABLES *********************************************************************/	
		
		String tdcQueryRecepcion = "SELECT ID, PE_ID, PV_DOC_ID, STATUS, DOC_TYPE, PV_DOC_NAME, RECEIVED_DATE"
				+ " FROM POSUSER.POS_INBOUND_DOCS "
				+ " WHERE RECEIVED_DATE >= sysdate-7 "
				+ " AND DOC_TYPE IN ('DCI') "
				+ " and status = 'I'";
		
		String tdcQueryDELT = "SELECT * " 
				+ " FROM POSUSER.POS_DCI_DETL "
				+ " WHERE PID_ID = '%s'";
		
		String tdcQueryValidar = "SELECT substr(pv_doc_name,4,5) AS plaza, COUNT(*) AS RECORDS"
				+ " FROM posuser.pos_inbound_docs A, posuser.plazas b"
				+ " WHERE A.doc_type='DCI'"
				+ " AND A.status = 'I'"
				+ " AND substr(A.pv_doc_name,4,5) = b.cr_plaza"
				+ " AND b.pais= 'MEX'"
				+ " AND TRUNC(A.partition_date) >= TRUNC(sysdate-7)"
				+ " GROUP BY substr(A.pv_doc_name,4,5)"
				+ " ORDER BY RECORDS DESC";
		
		String tdcQueryProfundidad = "SELECT INTERFASE, ENTIDAD, OPERACION, CATEGORIA, DESCRIPCION"
				+ " FROM WMUSER.wm_interfase_config"
				+ " WHERE interfase = 'PO11'"
				+ " AND operacion = 'DEPTH_DAYS'";
		
		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'PO11'"
				+ " and  start_dt >= TRUNC(SYSDATE)"
			    + " order by start_dt desc)"
				+ " where rownum = 1";
		
		String tdcQueryErrorId ="SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID= '%s'"
				+ " and rownum = 1";
		
		String tdcQueryDescripcion = "SELECT error_id, run_id, error_date, description"
				+ " FROM WMLOG.WM_LOG_ERROR"
				+ " WHERE RUN_ID = '%s'";
		
		String tdcQueryProcesados ="  SELECT A.ID, A.PE_ID, A.PV_DOC_ID, A.STATUS, A.DOC_TYPE, A.PV_DOC_NAME, A.RECEIVED_DATE" + 
				"  FROM POSUSER.POS_INBOUND_DOCS A, POSUSER.PLAZAS B" + 
				"  WHERE A.DOC_TYPE='DCI'" + 
				"  AND A.STATUS = 'E'" + 
				"  AND SUBSTR(A.PV_DOC_NAME,4,5) = B.CR_PLAZA" + 
				"  AND B.PAIS= 'MEX'" + 
				"  AND TRUNC(A.PARTITION_DATE) >= TRUNC(SYSDATE-7)" + 
				"  ORDER BY PV_DOC_NAME";
		
		String pv_doc = "", pid_id = "";
		String status = "E"; // status error
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id;
		 
		//testCase.setFullTestName(data.get("casoDePrueba"));
		//testCase.setProject_Name("I16072  Cajas Inteligentes");
		//testCase.setTest_Description(data.get("Description"));	
		
		
/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
		
		/* PASO 1 *********************************************************************/
		
		addStep("Ejecutar la siguiente consulta en la conexión FCWM6QA   para validar la recepción del archivo.");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryRecepcion);		
		SQLResult recepcionResult = executeQuery(dbPos, tdcQueryRecepcion);				
		boolean validar = recepcionResult.isEmpty();
		
		if (!validar) {			
			pid_id = recepcionResult.getData(0, "ID");
			testCase.addQueryEvidenceCurrentStep(recepcionResult);
		}	
		
		System.out.println(validar);		
		assertFalse(validar, "No se obtiene información de la consulta");
		
		/* PASO 2 *********************************************************************/
		
		addStep("Ejecutar la siguiente consulta en la conexión FCWM6QA para validar el tipo de movimiento, en este caso validaremos movimientos en Pesos,Dolares y TCCI (DEP2,DEP3,TCCI1).");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryDELT);		
		String deltFormat = String.format(tdcQueryDELT, pid_id);
		SQLResult validarResultDELT = executeQuery(dbPos, deltFormat);				
		boolean validarDELT = validarResultDELT.isEmpty();
		
		if (!validarDELT) {			
			testCase.addQueryEvidenceCurrentStep(validarResultDELT);
		}	
		
		System.out.println(validarDELT);		
		assertFalse(validarDELT, "No se obtiene información de la consulta");
		
	    /* PASO 3 *********************************************************************/	
		
		addStep("Ejecutar el siguiente query en la conexión FCWM6QA  para validar si hay registros a procesar.");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryValidar);		
		SQLResult validarResult = executeQuery(dbPos, tdcQueryValidar);
		boolean valRes = validarResult.isEmpty();
		
		if (!valRes) {			
			testCase.addQueryEvidenceCurrentStep(validarResult);
		}
		
		System.out.println(valRes);		
		assertFalse(valRes, "No se obtiene información de la consulta");		
		
		/* PASO 3 *********************************************************************/	
		
		addStep("Se valida la configuración de profundidad en la conexión FCWM6QA.");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryProfundidad);		
		SQLResult profundidadResult = executeQuery(dbPos, tdcQueryProfundidad);
		boolean prof = profundidadResult.isEmpty();
		
		if (!prof) {			
			testCase.addQueryEvidenceCurrentStep(profundidadResult);
		}
		
		System.out.println(prof);		
		assertFalse(prof, "No se obtiene información de la consulta");				
		
		
		/* PASO 4 *********************************************************************/
		
		addStep("Conectarse a IS del servidor de FCWINTQA3 y apagar el adapter DBS_RETEK_NT.");		
		
		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555/WmRoot/adapter-index.dsp?url=%2FWmART%2FListResources.dsp%3FadapterTypeName%3DJDBCAdapter%26dspName%3D.LISTRESOURCES&adapter=JDBCAdapter&text=webMethods+Adapter+for+JDBC&help=true";
		u.get(contra);
		
		pok.selectAdapter();
		
		/* PASO 5 *********************************************************************/
		
		addStep("Conectarse a IS del servidor de FCWINTQA3 y ejecutar el servicio: PO11.Pub:run.");		
		
		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		
		pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));		
		System.out.println(tdcQueryIntegrationServer);
		SQLResult is = dbLog.executeQuery(tdcQueryIntegrationServer);
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
		    
//		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S		
//
//		if (!successRun) {
//			String error = String.format(tdcQueryErrorId, run_id);	
//			SQLResult errorr = dbLog.executeQuery(error);
//			boolean emptyError = errorr.isEmpty();
//			
//			if (!emptyError) {
//				testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
//				testCase.addQueryEvidenceCurrentStep(errorr);
//			}
//		}	
		
		/* PASO 6 *********************************************************************/		    
		    
	    addStep("Verificar el estatus con el cual fue terminada la ejecución de la interface en la tabla WM_LOG_RUN del usuario WMLOG.");		
		
		boolean validateStatus = status.equals(status1);
		System.out.println("VALIDACION DE STATUS = E - " + validateStatus);
	
		testCase.addQueryEvidenceCurrentStep(is);					
		assertFalse(!validateStatus, "La ejecución de la interfaz fue exitosa");
		
		/* PASO 7 *********************************************************************/
		    
		addStep("Validar la descripción del error en la tabla WM_LOG_ERROR.");
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		
		String descripcionFormat = String.format(tdcQueryDescripcion, run_id);
		SQLResult descripcionResult = executeQuery(dbLog, descripcionFormat);
		System.out.println(descripcionFormat);		
		boolean descripcion = descripcionResult.isEmpty();
		
		if (!descripcion) {			
			testCase.addQueryEvidenceCurrentStep(descripcionResult);
		}
		
		System.out.println(descripcion);		
		assertFalse(descripcion, "No se obtiene información de la consulta en la tabla WM_LOG_ERROR.");		
		
		/* PASO 8 *********************************************************************/
		
		addStep("validar los archivos procesados y el ID  de la póliza.");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryProcesados);		
		
		//agregar un format y poner la variable pid id del primer query para ver que ese mismo dato se proceso :)
		
		SQLResult validarProcesados = executeQuery(dbPos, tdcQueryProcesados);
		boolean valProcesados = validarProcesados.isEmpty();
		
		if (!valProcesados) {			
			testCase.addQueryEvidenceCurrentStep(validarProcesados);
		}
		
		System.out.println(valProcesados);		
		assertFalse(valProcesados, "No se obtiene información de la consulta");	
		
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
		return "Terminado. Se valida error de excepcion al no poder conectar con BD de RMS para obtener informacion de las cuentas";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_006_PO11_Excepcion_Error_RMS";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	

}
