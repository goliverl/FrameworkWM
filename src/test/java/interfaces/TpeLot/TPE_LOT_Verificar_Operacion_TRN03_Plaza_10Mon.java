package interfaces.TpeLot;

import static org.junit.Assert.assertFalse;
import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import om.TPE_LOT;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class TPE_LOT_Verificar_Operacion_TRN03_Plaza_10Mon extends BaseExecution {
		
	/*
	 * 
	 * @cp Verificar la operacion TRN03 - Plaza 10MON
	 * 
	 */

	@Test(dataProvider = "data-provider")

	public void ATC_FT_004_TPE_LOT_TPE_LOT_Verificar_Operacion_TRN03_Plaza_10Mon(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
		 *********************************************************************/

		SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST_TPE_LOT, GlobalVariables.DB_USER_TPE_LOT,
				GlobalVariables.DB_PASSWORD_TPE_LOT);

		TPE_LOT TPELOTUtil = new TPE_LOT(data, testCase, db);

		/*
		 * Variables
		 *********************************************************************/

		String wmCodeTRN03 = "101";
		String tdcTRN03 = "SELECT APPLICATION,ENTITY,OPERATION,FOLIO,CREATION_DATE,PLAZA,TIENDA,MTI,"
				+ " PROC_CODE,WM_CODE,WM_DESC  "
				+ " FROM TPEUSER.TPE_FR_TRANSACTION "
				+ " WHERE APPLICATION='LOT' AND "
				+ " ENTITY='MLJ' AND "
				+ " OPERATION='TRN03' AND"
				//+ " TRUNC(CREATION_DATE)=TRUNC(SYSDATE) AND "
				+ " PLAZA='10MON'"
				+ " AND FOLIO= %s";

		String wmCodeRequestTRN03;

		String folio;

		/*
		 * Paso 1
		 *****************************************************************************************/

//Paso 1
		addStep("Llamar al servicio de consulta TRN03");

		String respuestaTRN03 = TPELOTUtil.TRN03();

		System.out.println("Respuesta: " + respuestaTRN03);
		folio = RequestUtil.getFolioXml(respuestaTRN03);
		wmCodeRequestTRN03 = RequestUtil.getWmCodeXml(respuestaTRN03);

		testCase.passStep();

		/* Paso 2 *********************************************************/
//Paso 2
		addStep("Verificar la respuesta generada por el servicio");

		boolean validationRequest = wmCodeTRN03.equals(wmCodeRequestTRN03);

		System.out.println(validationRequest + " - wmCode request: " + wmCodeRequestTRN03);

		testCase.validateStep(validationRequest);

		/* Paso 3 *********************************************************/

		addStep("Validar que se ha creado un registro en la tabla tdc_transaction de TPEUSER");

		String query = String.format(tdcTRN03, folio);
		System.out.println(query);

		SQLResult wmCodeDb = executeQuery(db, query);

		boolean validationDb = wmCodeDb.isEmpty();
		if (!validationDb) {

			testCase.addQueryEvidenceCurrentStep(wmCodeDb);

		}

		System.out.println(validationDb);

		assertFalse(validationDb);

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Verificar la operacion TRN03(Envio de ACK)";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_004_TPE_LOT_TPE_LOT_Verificar_Operacion_TRN03_Plaza_10Mon";
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
