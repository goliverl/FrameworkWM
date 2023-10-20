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

public class PR11_CL_SincronizacionDeInventarios extends BaseExecution {
	
	
@Test(dataProvider = "data-provider")
public void ATC_FT_PR11_CL_001_SincronizacionDeInventarios(HashMap<String, String> data) throws Exception {
	
/* Utilerías *********************************************************************/	
	
	SQLUtil dbLogCh = new SQLUtil(GlobalVariables.DB_HOST_LogChile, GlobalVariables.DB_USER_LogChile, GlobalVariables.DB_PASSWORD_LogChile);
	SQLUtil dbPosCh = new SQLUtil(GlobalVariables.DB_HOST_PosUserChile, GlobalVariables.DB_USER_PosUserChile, GlobalVariables.DB_PASSWORD_PosUserChile);
	SQLUtil dbRmsCh = new SQLUtil(GlobalVariables.DB_HOST_RMSWMUSERChile, GlobalVariables.DB_USER_RMSWMUSERChile, GlobalVariables.DB_PASSWORD_RMSWMUSERChile);
	
/**
*  Variables ******************************************************************************************
* 
* 
*/	
	
	
	String tdcQueryDocProcesar ="SELECT DISTINCT PE.PV_CR_PLAZA, PE.PV_CR_TIENDA, PID.ID, PID.PE_ID, PID.STATUS, TO_CHAR(PD.FECHA_ADM,'YYYYMMDD') FECHA_MVT  \r\n" + 
			" FROM POSUSER.POS_ENVELOPE PE \r\n" + 
			" JOIN POSUSER.POS_INBOUND_DOCS PID \r\n" + 
			" ON PID.PE_ID = PE.ID \r\n" + 
			" JOIN POSUSER.POS_INV_DETL PD \r\n" + 
			" ON  PID.ID = PD.PID_ID \r\n" + 
			" WHERE PID.DOC_TYPE = 'INV' \r\n" + 
			" AND PID.STATUS = 'I' \r\n" + 
			" AND PID.PARTITION_DATE>=TRUNC(SYSDATE-2) \r\n" + 
			" AND PE.PARTITION_DATE>=TRUNC(SYSDATE-2) \r\n" +
			" AND PE.PV_CR_PLAZA = '" + data.get("plaza") +"' \r\n";
	
	
	String tdcQueryStatusLog = "SELECT run_id,interface,start_dt,status,server \r\n "
			+ " FROM WMLOG.wm_log_run \r\n"
			+ " WHERE interface = 'PR11_CL' \r\n "
			+ " and status= 'S' \r\n"
			+ " and start_dt >= trunc(sysdate) \r\n" // FCWMLQA 
			+ " ORDER BY start_dt DESC \r\n";


	String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2,att3 \r\n"
			+ " FROM WMLOG.wm_log_thread \r\n"
			+ " WHERE parent_id = %s \r\n" ; //FCWMLQA 

	
	String tdcQueryValidateRe = "select 'Existen Registros' from dual where EXISTS ( \r\n" + 
			" SELECT  CR_PLAZA,CR_TIENDA,FECHA_MVT,PROCESSED_DATE \r\n" + 
			" FROM XXFC.XXFC_ITEM_LOC_POS \r\n" + 
			" WHERE 1=1 \r\n" + 
			" and CR_PLAZA = '%s' \r\n" + 
			" AND CR_TIENDA = '%s') \r\n";
	
	String tdcQueryUpdateStatus = "SELECT DOC_TYPE,STATUS,ID \r\n " +
			" FROM POSUSER.POS_INBOUND_DOCS \r\n" + 
			" WHERE DOC_TYPE = 'INV' \r\n" + 
			" AND STATUS = 'E' \r\n" + 
			" AND ID = '%s' \r\n";//[POS_INBOUND_DOCS.ID]
	


	//utileria

	String user = data.get("user");
	String ps = PasswordUtil.decryptPassword( data.get("ps"));
	String server = data.get("server");

	/**
	 * 
	 * **********************************Pasos del caso de Prueba
	 * *****************************************
	 * 
	 * 
	 * 
	 */

//					Paso 1	************************

	addStep("Validar que exista documentos de tipo DOC_TYPE='INV' pendiente de procesar en la tabla POS_INBOUND_DOCS de POSUSER con STATUS = 'I'");

	System.out.println(GlobalVariables.DB_HOST_PosUserChile);

	System.out.println(tdcQueryDocProcesar);

	SQLResult paso1 = executeQuery(dbPosCh, tdcQueryDocProcesar);

	String  id = paso1.getData(0, "ID");

	boolean docI = paso1.isEmpty();

	if (!docI) {

		testCase.addQueryEvidenceCurrentStep(paso1);
	}

	System.out.println(docI);

	assertFalse(docI, "No se obtiene informacion de la consulta");

//					Paso 2	***********************
	addStep("Ejecutar el servicio PR11.Pub:");

	SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
	PakageManagment pok = new PakageManagment(u, testCase);

	String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
	u.get(contra);

	 pok.runIntefaceWM(data.get("interfase"), data.get("servicio"), null);


//			Paso 3	************************

	addStep("Validar que se inserte el detalle de la ejecución de la interface PR11 en la tabla WM_LOG_RUN de WMLOG con STATUS = 'S'.\n");
			
	System.out.println(GlobalVariables.DB_HOST_LogChile);
	System.out.println(tdcQueryStatusLog);
	SQLResult paso3 = executeQuery(dbLogCh, tdcQueryStatusLog);
	String parentid = paso3.getData(0, "RUN_ID");
	boolean ValidacionPaso3 = paso3.isEmpty();
	if (!ValidacionPaso3) {

		testCase.addQueryEvidenceCurrentStep(paso3);
	}
	
	assertFalse(ValidacionPaso3, "La ejecución de la interfaz no fue exitosa");

//			Paso 4	************************

	addStep("Validar que se inserte el detalle de la ejecución de los threads lanzados por la interface PR11 en la tabla WM_LOG_THREAD de WMLOG con STATUS = 'S'.\n");
			
	System.out.println(GlobalVariables.DB_HOST_LogChile);
	String queryStatusThread = String.format(tdcQueryStatusThread, parentid);
	System.out.println(queryStatusThread);
	SQLResult paso4 = dbLogCh.executeQuery(queryStatusThread);
	boolean ValidacionPaso4 = paso4.isEmpty();
	if (!ValidacionPaso4) {

		testCase.addQueryEvidenceCurrentStep(paso4);
	}

	String plaza = paso4.getData(0, "ATT1");
	String tienda = paso4.getData(0, "ATT2");

	assertFalse(ValidacionPaso4, "El registro de ejecución de la plaza y tienda no fue exitoso");

//			Paso 5	************************

	addStep("Validar que se inserte la información de los documentos procesados en la tabla XXFC.XXFC_ITEM_LOC_POS de RETEK.");

	System.out.println(GlobalVariables.DB_HOST_RMSWMUSERChile);
	String Paso5Formato = String.format(tdcQueryValidateRe, plaza, tienda);
	System.out.println(Paso5Formato);
	SQLResult paso5 = executeQuery(dbRmsCh, Paso5Formato);
	boolean ValidacionPaso5 = paso5.isEmpty();

	if (!ValidacionPaso5) {

		testCase.addQueryEvidenceCurrentStep(paso5);

	}

	System.out.println(ValidacionPaso5);

	assertFalse(ValidacionPaso5, " No se muestran registros a procesar ");

//			Paso 6	************************

	addStep("Validar que se actualice el estatus de los documentos procesados en la tabla POS_INBOUND_DOCS de POSUSER a STATUS = 'E'");

	System.out.println(GlobalVariables.DB_HOST_PosUserChile);
	String FormatoPaso6 = String.format(tdcQueryUpdateStatus, id);
	SQLResult paso6 = executeQuery(dbPosCh, FormatoPaso6);
	boolean ValidacionPaso6 = paso6.isEmpty();
	System.out.println(FormatoPaso6);
	if (!ValidacionPaso6) {
		testCase.addQueryEvidenceCurrentStep(paso6);
	}

	System.out.println(ValidacionPaso6);

	assertFalse(ValidacionPaso6, " No se muestran registros a procesar ");

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
	return "ATC_FT_PR11_CL_001_SincronizacionDeInventarios";
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
