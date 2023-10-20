package interfaces.pr11_CL;

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
import utils.sql.SQLResultExcel;

public class PR11_CL_SincronizacionDeInventariosExcel extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PR11_CL_002_SincronizacionDeInventariosExcel(HashMap<String, String> data) throws Exception { 
		
/* Utilerías *********************************************************************/	
		

		SQLUtil dbLogCh = new SQLUtil(GlobalVariables.DB_HOST_LogChile, GlobalVariables.DB_USER_LogChile, GlobalVariables.DB_PASSWORD_LogChile);
		SQLUtil dbPosCh = new SQLUtil(GlobalVariables.DB_HOST_PosUserChile, GlobalVariables.DB_USER_PosUserChile, GlobalVariables.DB_PASSWORD_PosUserChile);
		SQLUtil dbRmsCh = new SQLUtil(GlobalVariables.DB_HOST_RMSWMUSERChile, GlobalVariables.DB_USER_RMSWMUSERChile, GlobalVariables.DB_PASSWORD_RMSWMUSERChile);
		
/**
*  Variables ******************************************************************************************
* 
* 
*/	
			
		String tdcQueryDocProcesar =" SELECT DISTINCT PE.PV_CR_PLAZA, PE.PV_CR_TIENDA, PID.ID, PID.PE_ID, PID.STATUS, TO_CHAR(PD.FECHA_ADM,'YYYYMMDD') FECHA_MVT \r\n" 
				 + " FROM POSUSER.POS_ENVELOPE PE \r\n" 
				 + " JOIN POSUSER.POS_INBOUND_DOCS PID \r\n"  
				 + " ON PID.PE_ID = PE.ID \r\n" 
				 + " JOIN POSUSER.POS_INV_DETL PD \r\n"  
				 + " ON  PID.ID = PD.PID_ID \r\n" 
				 + " WHERE PID.DOC_TYPE = 'INV' \r\n"  
				 + " AND PID.STATUS = 'I' \r\n" 
				 + " AND PID.PARTITION_DATE>=TRUNC(SYSDATE-2) \r\n"  
				 + " AND PE.PARTITION_DATE>=TRUNC(SYSDATE-2) \r\n" 
				 + " AND PE.PV_CR_PLAZA = '" + data.get("plaza") +"' \r\n";
		
		
		String tdcQueryStatusLog = " SELECT run_id,interface,start_dt,status,server \r\n"
				+ " FROM WMLOG.wm_log_run \r\n"
				+ " WHERE interface = 'PR11_CL' \r\n"
				+ " and status= 'S' \r\n"
				+ " and start_dt >= trunc(sysdate) \r\n" // FCWMLQA 
				+ " ORDER BY start_dt DESC \r\n";


		String tdcQueryStatusThread = " SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 \r\n"
				+ " FROM WMLOG.wm_log_thread \r\n"
				+ " WHERE parent_id = %s \r\n"; //FCWMLQA 

		
		String tdcQueryValidateRe = " SELECT  CR_PLAZA,CR_TIENDA,FECHA_MVT,PROCESSED_DATE \r\n"  
				+ " FROM XXFC.XXFC_ITEM_LOC_POS \r\n" 
				+ " WHERE 1=1 \r\n" 
				+ " and CR_PLAZA = '%s' \r\n"  
				+ " AND CR_TIENDA = '%s' \r\n"
				+ " AND TRUNC(PROCESSED_DATE) = TRUNC(SYSDATE) \r\n";
		
		String tdcQueryUpdateStatus = "SELECT DOC_TYPE,STATUS,ID " 
				+ " FROM POSUSER.POS_INBOUND_DOCS"  
				+ " WHERE DOC_TYPE = 'INV'"  
				+ " AND STATUS = 'E'"  
				+ " AND ID = '%s'";
		

		//utileria

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 * 
		 * 
		 */

//						Paso 1	************************

		addStep("Validar que exista documentos de tipo DOC_TYPE='INV' pendiente de procesar en la tabla POS_INBOUND_DOCS de POSUSER con STATUS = 'I'");

		System.out.println(GlobalVariables.DB_HOST_PosUserChile);

		System.out.println(tdcQueryDocProcesar);

		SQLResult paso1 = executeQuery(dbPosCh, tdcQueryDocProcesar);

		String id = paso1.getData(0, "ID");

		boolean docI = paso1.isEmpty();

		if (!docI) {

			testCase.addQueryEvidenceCurrentStep(paso1);

		}

		System.out.println(docI);

		assertFalse(docI, "No se obtiene informacion de la consulta");

//						Paso 2	***********************

		addStep("Ejecutar el servicio PR11.Pub:");
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));

//				Paso 3	************************

		addStep("Comprobar que se registra la ejecucion en WMLOG");

		System.out.println(GlobalVariables.DB_HOST_LogChile);
		SQLResult paso3 = executeQuery(dbLogCh, tdcQueryStatusLog);
		System.out.println(tdcQueryStatusLog);
		String parent = paso3.getData(0, "RUN_ID");
		boolean validacionPaso3 = paso3.isEmpty();
		if (!validacionPaso3) {

			testCase.addQueryEvidenceCurrentStep(paso3);
		}

		assertFalse(validacionPaso3, "La ejecución de la interfaz no fue exitosa");

//				Paso 4	************************

		addStep("Se valida la generacion de thread");

		System.out.println(GlobalVariables.DB_HOST_LogChile);
		String queryStatusThread = String.format(tdcQueryStatusThread, parent);
		System.out.println(tdcQueryStatusThread);
		SQLResult Paso4 = dbLogCh.executeQuery(queryStatusThread);

		String plaza = Paso4.getData(0, "ATT1");
		String tienda = Paso4.getData(0, "ATT2");

		boolean ValidacionPaso4 = Paso4.isEmpty();
		if (!ValidacionPaso4) {

			testCase.addQueryEvidenceCurrentStep(Paso4);
		}

		assertFalse(ValidacionPaso4, "El registro de ejecución de la plaza y tienda no fue exitoso");

//				Paso 5	************************

		addStep("Validar que se inserte la información de los documentos procesados en la tabla XXFC.XXFC_ITEM_LOC_POS de RETEK");

		System.out.println(GlobalVariables.DB_HOST_RMSWMUSERChile);
		String FormatoPaso5 = String.format(tdcQueryValidateRe, plaza, tienda);
		System.out.println(FormatoPaso5);
		SQLResultExcel Paso5 = executeQueryExcel(dbRmsCh, FormatoPaso5);
		boolean ValidarPaso5 = Paso5.isEmpty();

		if (!ValidarPaso5) {

			testCase.addDocumentEvidence(Paso5.getRelativePath(), "Documentos procesados");

		}

		System.out.println(ValidarPaso5);

		assertFalse(ValidarPaso5, " No se muestran registros a procesar ");

//				Paso 6	************************

		addStep("Validar que se actualice el estatus de los documentos procesados en la tabla POS_INBOUND_DOCS de POSUSER a STATUS = 'E'");

		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		String FormatoPaso6 = String.format(tdcQueryUpdateStatus, id);
		SQLResult paso6 = executeQuery(dbPosCh, FormatoPaso6);

		boolean ValidadPaso6 = paso6.isEmpty();
		System.out.println(ValidadPaso6);

		if (!ValidadPaso6) {

			testCase.addQueryEvidenceCurrentStep(paso6);

		}

		System.out.println(ValidadPaso6);

		assertFalse(ValidadPaso6, " No se muestran registros a procesar ");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Sincronizacion de Inventarios";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QAautomation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PR11_CL_002_SincronizacionDeInventariosExcel";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return null;
	}

}
