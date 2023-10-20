package interfaces.po11;

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


public class PO11_GeneracionDePolizaAlEjecutar extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PO11_Generacion_Poliza_Al_Ejecutar_Interfaz(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/		
		
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,	GlobalVariables.DB_PASSWORD_AVEBQA);

/**
* Variables ******************************************************************************************
* 
* 
*/
		String tdcQueryValidar = "SELECT a.id,substr(pv_doc_name,4,5) AS plaza, COUNT(*) AS RECORDS" 
				+ " FROM posuser.pos_inbound_docs A, posuser.plazas b"
				+ " WHERE A.doc_type='DCI'"
				+ " AND A.status = 'I'"
				+ " AND substr(A.pv_doc_name,4,5) = b.cr_plaza"
				+ " AND b.pais= 'MEX'"
				+ "AND PV_DOC_NAME = '" + data.get("documento") + "'"
				+ " AND TRUNC(A.partition_date) >= TRUNC(sysdate-7)"
				+ " GROUP BY substr(A.pv_doc_name,4,5), a.id "
				+ " ORDER BY RECORDS DESC";
		
		String tdcQueryProfundidad = "SELECT INTERFASE, ENTIDAD, OPERACION, CATEGORIA, DESCRIPCION"
				+ " FROM WMUSER.wm_interfase_config"
				+ " WHERE interfase = 'PO11'"
				+ " AND operacion = 'DEPTH_DAYS'";
		
		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'PO11'"
				+ "AND status = 'S'"
				+ " and  start_dt >= TRUNC(SYSDATE)"
			    + " order by start_dt desc)"
				+ " where rownum = 1";
		
		String tdcQueryErrorId =" SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"
				+ " and rownum = 1";
		
		String tdcQueryPoliza = "SELECT A.ID, A.PE_ID, A.PV_DOC_ID, A.STATUS, A.DOC_TYPE, B.CR_PLAZA, B.PAIS, A.TARGET_ID"
				+ " FROM POSUSER.POS_INBOUND_DOCS A, POSUSER.PLAZAS B"
				+ " WHERE A.DOC_TYPE='DCI'"
				+ " AND A.STATUS = 'E'"
				+ " AND A.ID = '%s'"
				+ " AND SUBSTR(A.PV_DOC_NAME,4,5) = B.CR_PLAZA"
				+ " AND B.PAIS= 'MEX'"
				+ " AND TRUNC(A.PARTITION_DATE) >= TRUNC(SYSDATE-7)"
				+ " ORDER BY PV_DOC_NAME";
		
		String tdcQueryFinanzas = "SELECT REFERENCE6 POLIZA,STATUS,USER_JE_CATEGORY_NAME,GROUP_ID, SEGMENT4 NUM_CTA_GRAL,"
				+ " SEGMENT1||'.'||SEGMENT2||'.'||SEGMENT3||'.'||SEGMENT4||'.'||SEGMENT5||'.'||SEGMENT7 AS CUENTA_DETALLE"
				+ " FROM GL.GL_INTERFACE"
				+ " WHERE REFERENCE6 = '%s'"
				+ " ORDER BY DATE_CREATED DESC";
		
		String tdcQueryFinanzas2 = "SELECT ENTERED_CR, ENTERED_DR, REFERENCE10,DATE_CREATED,CURRENCY_CODE"
				+ " FROM GL.GL_INTERFACE"
				+ " WHERE REFERENCE6 = '%s'"
				+ " ORDER BY DATE_CREATED DESC";
		

		testCase.setProject_Name("I16072  Cajas Inteligentes");
		
/**
 * **************************************      Pasos del caso de Prueba		 *******************************************/
					
//Paso 1 *************************		
		addStep("Ejecutar el siguiente query para validar si hay registros a procesar.");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryValidar);
		
		SQLResult validarResult = executeQuery(dbPos, tdcQueryValidar);
		
		
		String id = "";
		
		System.out.println( "ID: "+ id);
		
		boolean validar = validarResult.isEmpty();
		
		if (!validar) {
			id = validarResult.getData(0, "ID");
			testCase.addQueryEvidenceCurrentStep(validarResult);
		}
		
		System.out.println(validar);
		
		assertFalse(validar, "No se obtiene información de la consulta");
		
//Paso 2 *************************		
		addStep("Se valida la configuracion de profundidad.");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryProfundidad);
		
		SQLResult profundidadResult = executeQuery(dbPos, tdcQueryProfundidad);
		boolean prof = profundidadResult.isEmpty();
		
		if (!prof) {
			
			testCase.addQueryEvidenceCurrentStep(profundidadResult);

		}
		
		System.out.println(prof);
		
		assertFalse(prof, "No se obtiene información de la consulta");		
		
//Paso 3 *************************		
		addStep("Ejecutar el servicio: PO11.Pub:run.");		
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
		addStep("Ejecutar el siguiente query para consultar la última ejecución.");
		
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
		    
// paso 5 *********************************
		addStep("Ejecutar el siguiente query para validar los archivos procesados y el ID  de la poliza");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		
		String tdcQueryPolizaFormat = String.format(tdcQueryPoliza, id);
		
		System.out.println(tdcQueryPolizaFormat);
		
		SQLResult polizaResult = executeQuery(dbPos, tdcQueryPolizaFormat);
		
		String numPoliza = polizaResult.getData(0, "TARGET_ID");
		
		boolean poliza = polizaResult.isEmpty();
		
		if (!poliza) {
			
			testCase.addQueryEvidenceCurrentStep(polizaResult);
		}
		
		System.out.println(poliza);
		
		assertFalse(poliza, "No se obtiene información de la consulta");	
		
// paso 6 *********************************		
		addStep("Ejecutar el siguiente Query utilizando el numero de poliza para validar que se hayan insertados los datos en finanzas.");
		
		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		
		String finanzasFormat = String.format(tdcQueryFinanzas, numPoliza);
		String finanzasFormat2 = String.format(tdcQueryFinanzas2, numPoliza);
		System.out.println(finanzasFormat);
		
		SQLResult finanzasResult = executeQuery(dbEbs, finanzasFormat);
		SQLResult finanzasResult2 = executeQuery(dbEbs, finanzasFormat2);
		
		boolean finanzas = finanzasResult.isEmpty();
		
		if (!finanzas) {
			
			testCase.addQueryEvidenceCurrentStep(finanzasResult);
			testCase.addQueryEvidenceCurrentStep(finanzasResult2);
		}
		
		System.out.println(finanzas);
		
		assertFalse(finanzas, "No se obtiene información de la consulta");
		
		
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
		return "Terminado. Se validara que al ejecutar la interfaz PO11 genere la Poliza";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_PO11_Generacion_Poliza_Al_Ejecutar_Interfaz";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}

