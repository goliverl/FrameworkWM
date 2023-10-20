package interfaces.ei1;

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


public class EI1_InsercionInformacionTN extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_EI1_003_Insercion_Informacion_TN(HashMap<String, String> data) throws Exception {
		
/* Utilerias *********************************************************************/		
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);

		/**
		 * ALM
		 * Inserción de información para plaza 10AGS recibida vía Servidor TN
		 */
/**
* Variables ******************************************************************************************
* 
* 
*/		
		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'EI1-Receive' "
				+ " and  start_dt >= TRUNC(SYSDATE)"
			    + " order by start_dt desc)"
				+ " where rownum = 1";
		
		String tdcQueryErrorId = "SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"
				+ " and rownum = 1"; 
		
		String tdcQueryEnvelope = "SELECT id, receiver, doc_name, run_id_receive, interface_id "
				+ " FROM wmuser.EDI_ENVELOPE"
				+ " WHERE INTERFACE_ID = 'EI1' "
				+ " AND RUN_ID_RECEIVE = %s";
		
		String tdcQueryInbound = "SELECT EE_ID, ID, STATUS, DOC_TYPE, TARGET_ID, RUN_ID_SENT "
				+ " FROM wmuser.EDI_INBOUND_DOCS"
				+ " WHERE STATUS = 'L' "
				+ " AND DOC_TYPE = 'ORD'"
				+ " AND  EE_ID = %s";
		
		String tdcQueryEdiOrd = "SELECT EID_ID, CR_PLAZA, CR_TIENDA, ORDER_NUMBER, SUPPLIER"
				+ " FROM wmuser.EDI_ORD"
				+ " WHERE CR_PLAZA = '" + data.get("plaza") +"' "
				+ " AND EID_ID = %s";
		
		String tdcQueryOrdDtl = "SELECT EID_ID, ITEM, QTY "
				+ " FROM wmuser.EDI_ORD_DETL "
				+ " WHERE EID_ID = %s";
		
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//Paso 1 *************************	

		addStep("Se ejecuta el servicio: EI1.pub:runReceivetn");
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
		
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
			
//Paso 2 *********************
		addStep("Validar que el documento se env�a y la interfaz se ejecuta correctamente.");
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
		   
		    testCase.addTextEvidenceCurrentStep("Se encontr� un error en la ejecuci�n de la interfaz en la tabla WM_LOG_ERROR");
		   
		    testCase.addQueryEvidenceCurrentStep(paso2);
		   
		   }
		}
		    testCase.addQueryEvidenceCurrentStep(query);	
		    
//Paso 3 ***********************
	    addStep("Validar que la informaci�n se inserta correctamente en la tabla: EDI_ENVELOPE de WMUSER.");
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		
		String envelopeFormat = String.format(tdcQueryEnvelope, run_id);
		SQLResult envelopeResult = dbPos.executeQuery(envelopeFormat);
		
		String id = envelopeResult.getData(0, "ID");
		System.out.println(envelopeFormat);
		
		boolean envelope = envelopeResult.isEmpty();
		
		if (!envelope) {
			
			testCase.addQueryEvidenceCurrentStep(envelopeResult);
			
		}
		
		System.out.println(envelope);
		
		assertFalse(envelope, "No se obtiene informaci�n de la consulta");
		
//Paso 4 **********************
		addStep("Validar que se insert� correctamente la informaci�n en la tabla: EDI_INBOUND_DOCS de WMUSER.");
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		
		String inboundFormat = String.format(tdcQueryInbound, id);
		SQLResult inboundResult = dbPos.executeQuery(inboundFormat);
		
		String id2 = inboundResult.getData(0, "ID");
		System.out.println(inboundFormat);
		
		boolean inbound = inboundResult.isEmpty();
		
		if (!inbound) {
			
			testCase.addQueryEvidenceCurrentStep(inboundResult);
			
		}
		
		System.out.println(inbound);
		
		assertFalse(inbound, "No se obtiene informaci�n de la consulta");
		
//Paso 5 **********************
		addStep("Validar que se insert� la informaci�n de la orden en la tabla EDI_ORD.");
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		
		String ordFormat = String.format(tdcQueryEdiOrd, id2);
		SQLResult ordResult = dbPos.executeQuery(ordFormat);
		System.out.println(ordFormat);
		
		boolean edi_ord = ordResult.isEmpty();
		
		if (!edi_ord) {
			
			testCase.addQueryEvidenceCurrentStep(ordResult);
			
		}
		
		System.out.println(edi_ord);
		
		assertFalse(edi_ord, "No se obtiene informaci�n de la consulta");		
		
//Paso 6 **********************
		addStep("Validar que se insert� la informaci�n de la orden en la tabla EDI_ORD_DETL de WMUSER.");
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		
		String detlFormat = String.format(tdcQueryOrdDtl, id2);
		SQLResult detlResult = dbPos.executeQuery(detlFormat);
		System.out.println(detlFormat);
		
		boolean detl = detlResult.isEmpty();
		
		if (!detl) {
			
			testCase.addQueryEvidenceCurrentStep(detlResult);
			
		}
		
		System.out.println(detl);
		
		assertFalse(detl, "No se obtiene informaci�n de la consulta");			    
		    
		   
		    
		
		
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
		return "Construido. Validar que se inserte la informaci�n correctamente en WMUSER al recibir un archivo EDI por TN.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_EI1_003_Insercion_Informacion_TN";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
