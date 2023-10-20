package interfaces.Tpe_Loy;

import static org.testng.Assert.assertFalse;
import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import om.Tpe_Loy;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class Tpe_Loy_TRN01 extends BaseExecution {

	@Test(dataProvider = "data-provider")

	public void ATC_FT_Tpe_002_Loy_TRN01(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST_FCACQA, GlobalVariables.DB_USER_FCACQA,
				GlobalVariables.DB_PASSWORD_FCACQA);

		Tpe_Loy TPELoyUTIL = new Tpe_Loy(data, testCase, db);

		/*
		 * Variables
		 *********************************************************************/

		String wmCodeQRY01 = "101";

		String tdcPaso1 = " SELECT APPLICATION, ENTITY, OPERATION, LOWER(CODE) CODE, CATEGORY, VALUE1, VALUE2, VALUE3,  \r\n"
				+ " TO_CHAR(DATE1, 'YYYYMMDDHH24MiSS') DATE1, DESCRIPTION, CREATION_DATE \r\n"
				+ " FROM TPE_FR_CONFIG \r\n" + " WHERE APPLICATION = '" + data.get("application") + "' \r\n"
				+ " AND ENTITY = '" + data.get("entity") + "' \r\n" + " AND OPERATION = '" + data.get("operation")
				+ "' \r\n";

		String tdcPaso4= " Select * from tpe_fr_transaction \r\n"
				+ " where folio = %s \r\n"
				+ " and application = 'LOY' \r\n";

		
		
		/*
		 * Paso 1
		 *****************************************************************************************/

		 /*addStep("Se tiene que validar los datos de la configuracion");

		System.out.println(tdcPaso1);

		SQLResult Paso1 = executeQuery(db, tdcPaso1);

		boolean ValidaPaso1 = Paso1.isEmpty();
		if (!ValidaPaso1) {

			testCase.addQueryEvidenceCurrentStep(Paso1);
		}

		System.out.println(ValidaPaso1);

		assertFalse(ValidaPaso1, "No se obtiene información de la consulta");/

		/*
		 * Paso 2
		 *****************************************************************************************/

		addStep("Ejecutar via HTTP la interface con el XML indicado");

		String respuestaQry01 = TPELoyUTIL.TRN01();

		System.out.println("Respuesta: " + respuestaQry01);

		String WmCodigo = RequestUtil.getWmCodeXml(respuestaQry01);
		System.out.println(WmCodigo);
		String Wmfolio = RequestUtil.getFolioXml(respuestaQry01);
		System.out.println(Wmfolio);

		testCase.passStep();

		/* Paso 3 *********************************************************/

		addStep("Verificar xmlResponse");

		boolean validationRequest = wmCodeQRY01.equals(WmCodigo);

		System.out.println(validationRequest + " - wmCode request: " + WmCodigo);

		testCase.validateStep(validationRequest);

		
		/* Paso 4 *********************************************************/

		addStep("Validar que se encuntre la informacion en la base de datos");

		String wmcode ;
		
		String FormatoPaso4 = String.format(tdcPaso4, Wmfolio);
		
		System.out.println(FormatoPaso4);

		SQLResult Paso1 = executeQuery(db, FormatoPaso4);

		boolean ValidaPaso1 = Paso1.isEmpty();
		if (!ValidaPaso1) {

			wmcode= Paso1.getData(0, "WM_CODE");
			
			boolean validacionCode = wmcode.equals(WmCodigo);
			
			if(!validacionCode) {
				
				testCase.validateStep(validacionCode);
			}
			
			testCase.addQueryEvidenceCurrentStep(Paso1);
		}

		System.out.println(ValidaPaso1);

		assertFalse(ValidaPaso1, "No se obtiene información de la consulta");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construccion.Tpe_Loy_TRN01";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_Tpe_002_Loy_TRN01";
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
