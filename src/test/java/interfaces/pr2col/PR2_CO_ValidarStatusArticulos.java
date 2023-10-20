package interfaces.pr2col;

import static org.testng.Assert.assertFalse;

import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import om.PR2_CO;
import util.GlobalVariables;
import utils.sql.SQLUtil;
import utils.sql.SQLResult;

public class PR2_CO_ValidarStatusArticulos  extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_006_PR2_COL_Validar_Status_Articulos(HashMap<String, String> data) throws Exception {
		
/*
 * Utilerías *********************************************************************/

		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_EBS_COL, GlobalVariables.DB_USER_EBS_COL, GlobalVariables.DB_PASSWORD_EBS_COL);
		//SQLUtil dbRms = new SQLUtil(GlobalVariables.DB_HOST_RMS_COL, GlobalVariables.DB_USER_RMS_COL, GlobalVariables.DB_PASSWORD_RMS_COL);
		PR2_CO pr2_coUtil = new PR2_CO(data, testCase, dbPos);
		
/**
* Variables ******************************************************************************************
* 
* 
*/
		String tdcQueryMaestro = "SELECT ORACLE_CR, ORACLE_CR_SUPERIOR, ORACLE_CR_TYPE, ESTADO, RETEK_CR "
				+ " FROM  XXFC_MAESTRO_DE_CRS_V "
				+ " WHERE  ESTADO = 'A' "
				+ " AND ORACLE_CR_SUPERIOR = '" + data.get("SUPERIOR") +"'"
				+ " AND ORACLE_CR = '" + data.get("ORACLE_CR") +"'"
				+ " AND ORACLE_CR_TYPE = 'T'";
		
		String tdcQueryItem = "SELECT ITEM, LOC, STATUS, LOCAL_ITEM_DESC "
				+ " FROM XXFC_ITEM_LOC_VIEW"
				+ " WHERE ITEM = " + data.get("ITEM") +""
				+ " AND  LOC = %s"
				+ " AND STATUS = 'A'";
		
		String tdcQueryTransaction = "SELECT * from (Select ID, OPERATION, START_DT, STATUS, RESPONSE "
				+ " FROM POSUSER.TSF_TRANSACTION_COL "
				+ " WHERE START_DT >= TRUNC(SYSDATE)"
				+ " AND STATUS = 'S' "
				+ "AND OPERATION = 'PR2_CO_validSKU'"
				+ " order by start_dt desc)"
				+ " where rownum = 1";
		
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//Paso 1 *************************	
		addStep("Consultar que existan los datos de la Tienda en la vista: XXFC_MAESTRO_DE_CRS_V  de la BD de ORAFIN.");
		System.out.println(GlobalVariables.DB_HOST_EBS_COL);
		
		SQLResult maestroResult = executeQuery(dbEbs, tdcQueryMaestro);
		String retek = maestroResult.getData(0, "RETEK_CR");
		System.out.println(tdcQueryMaestro);
		
		boolean maestro = maestroResult.isEmpty();
		
		if (!maestro) {
			
			testCase.addQueryEvidenceCurrentStep(maestroResult);
		}
		
		System.out.println(maestro);
		
/*paso 2 **************************
		addStep("Verificar que exista el artículo en la vista XXFC_ITEM_LOC_VIEW de la BD de RETEK.");
		System.out.println(GlobalVariables.DB_HOST_RMS_COL);
		
		String itemFormat = String.format(tdcQueryItem, retek);
		SQLResult itemResult = executeQuery(dbRms, itemFormat);
		System.out.println(itemFormat);
		
		boolean item = itemResult.isEmpty();
		
		if (!item) {
			
			testCase.addQueryEvidenceCurrentStep(itemResult);
		}
		
		System.out.println(item);
		
		assertFalse(item, "No se obtiene información de la consulta");
		
//paso 3 ************************
		addStep("Se envia una petición por HTTP al servicio: PR2_CO.Pub:runValidSku:");
		
		pr2_coUtil.ejecutarRunValidSKU(); */
		
//paso 4 ************************
		addStep("Comprobar que se registre la transacción en la tabla TSF_TRANSACTION_COL de la BD POSUSER para la operación PR2_CO_validSKU.");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryTransaction);
		
		SQLResult transactionResult = executeQuery(dbPos, tdcQueryTransaction);
		String responsexml = "";
		
		boolean transaction = transactionResult.isEmpty();
		
		if (!transaction) {
			responsexml = transactionResult.getData(0, "RESPONSE");
			testCase.addQueryEvidenceCurrentStep(transactionResult);
		}
		
		System.out.println(transaction);
		
		assertFalse(transaction, "No se obtiene información de la consulta");
		
//paso 5 ********************
		addStep("Validar que se retorne un XML con los articulos validos.");
		
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
		return "Construido. Validar el status de los artículos (validSKU).";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_006_PR2_COL_Validar_Status_Articulos";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
