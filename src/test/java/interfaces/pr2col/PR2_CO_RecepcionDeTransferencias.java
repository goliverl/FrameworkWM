package interfaces.pr2col;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import om.PR2_CO;
import util.GlobalVariables;

import utils.sql.SQLUtil;
import utils.sql.SQLResult;

public class PR2_CO_RecepcionDeTransferencias extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_PR2_COL_Recepcion_Transferencias(HashMap<String, String> data) throws Exception {
		
/*
 * Utilerías *********************************************************************/

		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		PR2_CO pr2_coUtil = new PR2_CO(data, testCase, dbPos);
		
/**
* Variables ******************************************************************************************
* 
* 
*/
	
		String tdcQueryTSF = "SELECT COUNT(ID) FROM posuser.POS_TSF_ONLINE_HEAD "
				+ " WHERE PV_DOC_ID = " + data.get("PV_DOC_ID") +" "
				+ " AND SENDER_ID = '" + data.get("SENDER") +"'"
				+ " AND TRUNC(RECEIVED_DATE) = TRUNC(SYSDATE) "
				+ " AND STATUS = 'R'";
		
		String tdcQueryTransaction = "SELECT * from (Select ID, OPERATION, START_DT, STATUS, RESPONSE "
				+ " FROM POSUSER.TSF_TRANSACTION_COL "
				+ " WHERE START_DT >= TRUNC(SYSDATE)"
				+ " AND STATUS = 'S' "
				+ " AND OPERATION = 'PR2_CO_Inbound'"
				+ " order by start_dt desc)"
				+ " where rownum = 1";
			
		String tdcQueryHead = "SELECT ID, PV_DOC_ID, SENDER_ID, RECEIVED_DATE, STATUS, RUN_ID "
				+ " FROM posuser.POS_TSF_ONLINE_HEAD "
				+ " WHERE RUN_ID = %s"
				+ " AND PV_DOC_ID = " + data.get("PV_DOC_ID") +""
				+ " AND SENDER_ID = '" + data.get("SENDER") +"'"
				+ " AND RECEIVED_DATE >= TRUNC(SYSDATE)"
				+ " AND STATUS = 'R'";
		
		String tdcQueryDetl = "SELECT ID, ITEM, SHIP_QTY "
				+ " FROM POSUSER.POS_TSF_ONLINE_DETL"
				+ " WHERE ID = %s"
				+ " AND PARTITION_DATE >= TRUNC(SYSDATE);";
		
		
		
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//Paso 1 *************************		
		addStep("Comprobar que No existan registros en la tabla POS_TSF_ONLINE_HEAD en la BD POSUSER.");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryTSF);
		
		SQLResult tsfResult = executeQuery(dbPos, tdcQueryTSF);

		
		boolean tsf = tsfResult.isEmpty();
		
		if (!tsf) {
			
			testCase.addQueryEvidenceCurrentStep(tsfResult);
		}
		
		System.out.println(tsf);
		
		assertFalse(tsf, "No se obtiene información de la consulta");
		
//paso 2 **************************
		addStep("Se envia una petición por HTTP al servicio: PR2_CO.Pub:runInbound:");
		
		pr2_coUtil.ejecutarRunInbound();		
		
		
//paso 3 **************************
		addStep("Comprobar que se registre la transaccion en la tabla TSF_TRANSACTION_COL de la BD POSUSER para la operación PR2_CO_Inbound");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryTransaction);
		
		SQLResult transactionResult = executeQuery(dbPos, tdcQueryTransaction);
		String run_id = "";
		String responsexml = "";
		
		
		boolean transaction = transactionResult.isEmpty();
		
		if (!transaction) {
			run_id = transactionResult.getData(0, "ID");
			responsexml = transactionResult.getData(0, "RESPONSE");
			testCase.addQueryEvidenceCurrentStep(transactionResult);
		}
		
		System.out.println(transaction);
		
		assertFalse(transaction, "No se obtiene información de la consulta");		
		
//paso 4 ****************************
		addStep("Validar que se insertó correctamente la información en las tablas POS_TSF_ONLINE_HEAD, POS_TSF_ONLINE_DETL de la BD POSUSER con status igual a 'R':");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		String headFormat = String.format(tdcQueryHead, run_id);
		SQLResult headResult = executeQuery(dbPos, headFormat);
		String id = headResult.getData(0, "ID");
		System.out.println(headFormat);
		
		boolean tsf_head = headResult.isEmpty();
		
		if (!tsf_head) {
			
			testCase.addQueryEvidenceCurrentStep(headResult);
		}
		
		System.out.println(tsf_head);
		
		assertFalse(tsf_head, "No se obtiene información de la consulta");
		
		String detlFormat = String.format(tdcQueryDetl, id);
		SQLResult detlResult = executeQuery(dbPos, detlFormat);
		System.out.println(detlFormat);
		
		boolean detl = detlResult.isEmpty();
		
		if (!detl) {
			
			testCase.addQueryEvidenceCurrentStep(detlResult);
		}
		
		System.out.println(detl);
		
		assertFalse(detl, "No se obtiene información de la consulta");
		
		
		
//paso 5 ******************************	
		addStep("Validar que se devuelve un XML de respuesta con estatus igual a 'I'.");
		
		String statusResponse = getSimpleDataXml(responsexml, "STATUS");

		String statusToValidate = "I";
		
		boolean validationRequest = statusResponse.equals(statusToValidate);
		
		if(validationRequest) {
			
			System.out.println("\n" + validationRequest + " - status request: " + statusResponse + "\n");
			
			testCase.addTextEvidenceCurrentStep(responsexml);
			testCase.addTextEvidenceCurrentStep("El STATUS es igual a 'I'.");
			
		} 
		
		else
			{testCase.addTextEvidenceCurrentStep(responsexml);}

		
		assertTrue(validationRequest, "El STATUS no es el esperado: " + statusToValidate);
			
		
	
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
		return "Construido. Recepción de transferencias de Salida (Inbound).";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_003_PR2_COL_Recepcion_Transferencias";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
