package interfaces.pr2col;

import static org.testng.Assert.assertFalse;
import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import om.PR2_CO;
import util.GlobalVariables;
import utils.sql.SQLUtil;
import utils.sql.SQLResult;

public class PR2_CO_TransferenciasAffect extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_005_PR2_COL_Transferencias_Affect(HashMap<String, String> data) throws Exception {
		
/*
 * Utilerías *********************************************************************/

		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		PR2_CO pr2_coUtil = new PR2_CO(data, testCase, dbPos);
		
/**
* Variables ******************************************************************************************
* 
* 
*/		
		String tdcQueryTSF = "SELECT PV_DOC_ID, SENDER_ID, RECEIVER_ID, EXT_REF_NO, CTRL_CODE, NO_RECORDS, STATUS "
				+ " FROM posuser.POS_TSF_ONLINE_HEAD"
				+ " WHERE SENDER_ID = '" + data.get("SENDER") +"'"
				+ " AND RECEIVER_ID = '" + data.get("RECEIVER") +"'"
				+ " AND EXT_REF_NO = " + data.get("EXT_REF_NO") +" "
				+ " AND PV_DOC_ID = " + data.get("PV_DOC_ID") +" "
				+ " AND STATUS = 'R'";
		
		String tdcQueryTransaction = "SELECT * from (Select ID, OPERATION, START_DT, STATUS " //REQUEST, RESPONSE
				+ " FROM POSUSER.TSF_TRANSACTION_COL "
				+ " WHERE START_DT >= TRUNC(SYSDATE)"
				+ " AND STATUS = 'S' "
				+ " AND OPERATION = 'PR2_CO_Affect'"
				+ " order by start_dt desc)"
				+ " where rownum = 1";
		
		String tdcQueryAffect = "SELECT PV_DOC_ID, SENDER_ID, RECEIVER_ID, EXT_REF_NO, CTRL_CODE, MOV_TYPE, RECEIVED_DATE "
				+ " FROM posuser.POS_TSF_ONLINE_AFFECT"
				+ " WHERE PV_DOC_ID = %s"
				+ " AND RECEIVED_DATE >= TRUNC(SYSDATE)"
				+ " AND MOV_TYPE = '" + data.get("MOV_TYPE") +"'";
		
		String tdcQueryStatus = "SELECT PV_DOC_ID, SENDER_ID, RECEIVER_ID, EXT_REF_NO, CTRL_CODE, NO_RECORDS, STATUS "
				+ " FROM posuser.POS_TSF_ONLINE_HEAD"
				+ " WHERE PV_DOC_ID = %s"
				+ " AND STATUS = '" + data.get("STATUS") +"'"
				+ " AND TRUNC(AFFECTED_DATE) = TRUNC(SYSDATE)";
		
		
		
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//Paso 1 *************************		
		addStep("Comprobar que existan registros en la tabla POS_TSF_ONLINE_HEAD en la BD POSUSER, con estatus = 'R' (Recibido).");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryTSF);
		
		SQLResult tsfResult = executeQuery(dbPos, tdcQueryTSF);
		String pvdoc = "";
		
		
		boolean tsf = tsfResult.isEmpty();
		
		if (!tsf) {
			pvdoc = tsfResult.getData(0, "PV_DOC_ID");
			testCase.addQueryEvidenceCurrentStep(tsfResult);
		}
		
		System.out.println(tsf);
		
		assertFalse(tsf, "No se obtiene información de la consulta");
		
//paso 2 **************************
		addStep("Se envia una petición por HTTP al servicio: PR2_CO.Pub:runAffect:");
		
		pr2_coUtil.ejecutarRunAffect();
		
//paso 3 **************************
		addStep("Comprobar que se registre la transaccion en la tabla TSF_TRANSACTION_COL de la BD POSUSER para la operación PR2_CO_Affect.");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryTransaction);
		
		SQLResult transactionResult = executeQuery(dbPos, tdcQueryTransaction);
		
		
		boolean transaction = transactionResult.isEmpty();
		
		if (!transaction) {
			
			testCase.addQueryEvidenceCurrentStep(transactionResult);
		}
		
		System.out.println(transaction);
		
		assertFalse(transaction, "No se obtiene información de la consulta");
		
//paso 4 *************************
		addStep("Comprobar que se inserte la informacion en la tabla POS_TSF_ONLINE_AFFECT de la BD POSUSER.");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		String affectFormat = String.format(tdcQueryAffect, pvdoc);
		SQLResult affectResult = executeQuery(dbPos, affectFormat);
		System.out.println(affectFormat);
		
		
		boolean affect = affectResult.isEmpty();
		
		if (!affect) {
			
			testCase.addQueryEvidenceCurrentStep(affectResult);
		}
		
		System.out.println(affect);
		
		assertFalse(affect, "No se obtiene información de la consulta");
		
//Paso 5*************************
				
		String status = "C";
		
		if(data.get("STATUS").equals(status))
		{
			addStep("Comprobar que se actualicen los registros en la tabla POS_TSF_ONLINE_HEAD de la BD POSUSER con status igual a 'C' (Transferencia CANCELADAS).");
			System.out.println(GlobalVariables.DB_HOST_Puser);
			String canceladaFormat = String.format(tdcQueryStatus, pvdoc);
			SQLResult canceladaResult = executeQuery(dbPos, canceladaFormat);
			System.out.println(canceladaFormat);
			
			boolean cancelada = canceladaResult.isEmpty();
			
			if (!cancelada) {
				
				testCase.addQueryEvidenceCurrentStep(canceladaResult);
			}
			
			System.out.println(cancelada);
			
			assertFalse(cancelada, "No se obtiene información de la consulta");
			
			
		}
		
		else {
			
			addStep("Comprobar que se actualicen los registros en la tabla POS_TSF_ONLINE_HEAD de la BD POSUSER con status igual a 'I' (Transferencia CONFIRMADA).");
			System.out.println(GlobalVariables.DB_HOST_Puser);
			String confirmadoFormat = String.format(tdcQueryStatus, pvdoc);
			SQLResult confirmadoResult = executeQuery(dbPos, confirmadoFormat);
			System.out.println(confirmadoFormat);
			
			boolean confirmado = confirmadoResult.isEmpty();
			
			if (!confirmado) {
				
				testCase.addQueryEvidenceCurrentStep(confirmadoResult);
			}
			
			System.out.println(confirmado);
			
			assertFalse(confirmado, "No se obtiene información de la consulta");
			
		}
			
		
		
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
		return "Construido. Marcar transferencias.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_005_PR2_COL_Transferencias_Affect";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
