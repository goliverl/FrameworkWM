package interfaces.pr2col;

import static org.testng.Assert.assertFalse;

import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import om.PR2_CO;
import util.GlobalVariables;
import utils.sql.SQLUtil;
import utils.sql.SQLResult;

public class PR2_CO_SolicitudDeTransferencias extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_PR2_COL_Solicitud_Transferencias(HashMap<String, String> data) throws Exception {
		
/*
 * Utilerías *********************************************************************/

		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		PR2_CO pr2_coUtil = new PR2_CO(data, testCase, dbPos);
		
/**
* Variables ******************************************************************************************
* 
* 
*/	
		String tdcQueryHead = "SELECT ID, PV_DOC_ID, SENDER_ID, RECEIVER_ID "
				+ " FROM posuser.POS_TSF_ONLINE_HEAD"
				+ " WHERE RECEIVER_ID = '" + data.get("RECEIVER") +"' "
				+ " AND STATUS = 'I'";
		
		String tdcQueryDetl = "SELECT ID, ITEM, SHIP_QTY "
				+ " FROM posuser.POS_TSF_ONLINE_DETL "
				+ " WHERE ID = %s";
		
		String tdcQueryTransaction = "SELECT * from (Select ID, OPERATION, START_DT, STATUS, RESPONSE "
				+ " FROM POSUSER.TSF_TRANSACTION_COL "
				+ " WHERE START_DT >= TRUNC(SYSDATE)"
				+ " AND STATUS = 'S' "
				+ "AND OPERATION = 'PR2_CO_Outbound'"
				+ " order by start_dt desc)"
				+ " where rownum = 1";
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//Paso 1 *************************	
		addStep("Comprobar que existan registros en la tabla POS_TSF_ONLINE_HEAD, POS_TSF_ONLINE_DETL en la BD POSUSER.");
		System.out.println(GlobalVariables.DB_HOST_Puser);
		
		SQLResult headResult = executeQuery(dbPos, tdcQueryHead);
		String id = headResult.getData(0, "ID");
		System.out.println(tdcQueryHead);
		
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
		
//paso 2 ******************************
		addStep("Se envia una petición por HTTP al servicio: PR2_CO.Pub:runOutbound:");
		
		pr2_coUtil.ejecutarRunOutbound();
		
//paso 3 ******************************
		addStep("Comprobar que se registre la transacción en la tabla TSF_TRANSACTION_COL de la BD POSUSER para la operación PR2_CO_Outbound.");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryTransaction);
		
		SQLResult transactionResult = executeQuery(dbPos, tdcQueryTransaction);
		String responsexml = transactionResult.getData(0, "RESPONSE");
		
		boolean transaction = transactionResult.isEmpty();
		
		if (!transaction) {
			
			testCase.addQueryEvidenceCurrentStep(transactionResult);
		}
		
		System.out.println(transaction);
		
		assertFalse(transaction, "No se obtiene información de la consulta");
	
//paso 4 *****************************
		addStep("Validar que se devuelve el XML de respuesta.");
		
		testCase.addTextEvidenceCurrentStep(responsexml);
			

		
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
		return "Construido. ATC_FT_004_PR2_COL_Solicitud_Transferencias";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_004_PR2_COL_Solicitud_Transferencias";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
