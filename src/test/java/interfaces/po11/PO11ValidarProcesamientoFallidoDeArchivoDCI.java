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


public class PO11ValidarProcesamientoFallidoDeArchivoDCI extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_011_PO11_Validar_Procesamiento_Fallido_DCI(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/		
		
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

/**
* Variables ******************************************************************************************
* 
* 
*/
		String tdcQueryValidar = "SELECT substr(pv_doc_name,4,5) AS plaza, COUNT(*) AS RECORDS" 
				+ " FROM posuser.pos_inbound_docs A, posuser.plazas b"
				+ " WHERE A.doc_type='DCI'"
				+ " AND A.status = 'I'"
				+ " AND substr(A.pv_doc_name,4,5) = b.cr_plaza"
				+ " AND b.pais= 'MEX'"
				+ " AND TRUNC(A.partition_date) >= TRUNC(sysdate-7)"
				+ " GROUP BY substr(A.pv_doc_name,4,5)"
				+ " ORDER BY RECORDS DESC";
		
		String tdcQueryDocName = "SELECT A.PV_DOC_NAME"
				+ " FROM POSUSER.POS_INBOUND_DOCS A, POSUSER.PLAZAS B"
				+ " WHERE A.DOC_TYPE='DCI'"
				+ " AND A.STATUS = 'I'"
				+ " AND SUBSTR(A.PV_DOC_NAME,4,5) = B.CR_PLAZA"
				+ " AND B.PAIS= 'MEX'"
				+ " AND TRUNC(A.PARTITION_DATE) >= TRUNC(SYSDATE-7)";
		
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
		
		String tdcQueryErrorId =" SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"
				+ " and rownum = 1";
		
		String tdcQueryPoliza = "SELECT A.ID, A.PE_ID, A.PV_DOC_ID, A.STATUS, A.DOC_TYPE, B.CR_PLAZA, B.PAIS, A.TARGET_ID"
				+ " FROM POSUSER.POS_INBOUND_DOCS A, POSUSER.PLAZAS B"
				+ " WHERE A.DOC_TYPE='DCI'"
				+ " AND A.STATUS = 'E'"
				+ " AND SUBSTR(A.PV_DOC_NAME,4,5) = B.CR_PLAZA"
				+ " AND B.PAIS= 'MEX'"
				+ " AND TRUNC(A.PARTITION_DATE) >= TRUNC(SYSDATE-7)"
				+ " ORDER BY PV_DOC_NAME";
		
		String tdcQueryArchivo = "SELECT ID, PE_ID, PV_DOC_ID, STATUS, DOC_TYPE, PV_DOC_NAME, TARGET_ID"
				+ " FROM POSUSER.POS_INBOUND_DOCS"
				+ " WHERE PV_DOC_NAME = '%s'";
		
		
		

		testCase.setProject_Name("Cajas Inteligentes");

	
		
/**
 * **************************************      Pasos del caso de Prueba		 *******************************************/
					
//Paso 1 *************************		
		addStep("Ejecutar el siguiente query en la conexión FCWM6QA para validar si hay registros a procesar.");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryValidar);
		
		SQLResult validarResult = executeQuery(dbPos, tdcQueryValidar);
		SQLResult docNameResult = executeQuery(dbPos, tdcQueryDocName);
		String docName = "";
		
		boolean validar = validarResult.isEmpty();
		
		if (!validar) {
			docName = docNameResult.getData(0, "PV_DOC_NAME");
			testCase.addQueryEvidenceCurrentStep(validarResult);
		}
		
		System.out.println(validar);
		
		assertFalse(validar, "No se obtiene información de la consulta");
		
//Paso 2 *************************		
		addStep("Se valida la configuración de profundidad en la conexión FCWM6QA con la siguiente consulta:");
		
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
		addStep("Ejecutar el siguiente query en la conexión FCWMLQA para consultar la última ejecución.");
		
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
		addStep("Ejecutar la siguiente consulta en la conexión FCWM6QA para validar el estatus del archivo.");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		
		String archivoFormat = String.format(tdcQueryArchivo, docName);
		SQLResult archivoResult = dbPos.executeQuery(archivoFormat);
		
		System.out.println(archivoFormat);
		
		boolean archivo = archivoResult.isEmpty();
		
		if (!archivo) {
			
			testCase.addQueryEvidenceCurrentStep(archivoResult);
		}
		
		System.out.println(archivo);
		
		assertFalse(archivo, "No se obtiene información de la consulta");
	
		
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
		return "Terminado. Se validara que al ejecutar la interfaz PO11, que no genere la Poliza por traer movimientos negativos y se envie notificacion de error por correo";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_011_PO11_Validar_Procesamiento_Fallido_DCI";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
