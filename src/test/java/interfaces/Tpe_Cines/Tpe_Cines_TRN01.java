package interfaces.Tpe_Cines;

import static org.testng.Assert.assertFalse;
import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import om.Tpe_Cines;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class Tpe_Cines_TRN01 extends BaseExecution  {
	
@Test(dataProvider = "data-provider")
	
	public void ATC_FT_Tpe_004_Cines_TRN01 (HashMap<String, String> data) throws Exception {
				
/* Utilerías *********************************************************************/

		SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST_FCACQA, GlobalVariables.DB_USER_FCACQA, GlobalVariables.DB_PASSWORD_FCACQA);


		Tpe_Cines TPECinesUTIL = new Tpe_Cines(data, testCase, db);
	
		
		
/* Variables *********************************************************************/
					
    String wmCodeTRN01 = "100";	
	
    String tdcPaso1 = " SELECT APPLICATION, ENTITY, OPERATION, LOWER(CODE) CODE, CATEGORY, VALUE1, VALUE2, VALUE3,  \r\n"
			+ " TO_CHAR(DATE1, 'YYYYMMDDHH24MiSS') DATE1, DESCRIPTION, CREATION_DATE \r\n"
			+ " FROM TPEUSER.TPE_FR_CONFIG \r\n"
			+ " WHERE APPLICATION = '"+data.get("application")+"' \r\n"
			+ " AND ENTITY = '"+data.get("entity")+"' \r\n"
			+ " AND OPERATION = '" +data.get("operation")+ "' \r\n";
    
    String tdcPaso3 = "Select * from TPEUSER.TPE_FR_TRANSACCION where folio = %s";
    

	/*
	 * Paso 1
	 *****************************************************************************************/

	addStep("Se tiene que validar los datos de la configuracion");

	System.out.println(tdcPaso1);

	SQLResult Paso1 = executeQuery(db, tdcPaso1);

	boolean ValidaPaso1 = Paso1.isEmpty();
	if (!ValidaPaso1) {

		testCase.addQueryEvidenceCurrentStep(Paso1);
	}

	System.out.println(ValidaPaso1);

	assertFalse(ValidaPaso1, "No se obtiene información de la consulta");

	/*
	 * Paso 2
	 *****************************************************************************************/

	addStep("Ejecutar via HTTP la interface con el XML indicado");

	String respuestaQry01 = TPECinesUTIL.QRY01();

	System.out.println("Respuesta: " + respuestaQry01);

	String wmcodigo = RequestUtil.getWmCodeXml(respuestaQry01);
	System.out.println(wmcodigo);
	String folio = RequestUtil.getFolioXml(respuestaQry01);
	System.out.println(folio);

	testCase.passStep();

	/* Paso 2 *********************************************************/

	addStep("Verificar xmlResponse");

	boolean validationRequest = wmCodeTRN01.equals(wmcodigo);

	System.out.println(validationRequest + " - wmCode request: " + wmcodigo);

	testCase.validateStep(validationRequest);

	/* Paso 3 *********************************************************/

	
	addStep("Se tiene que validar los datos de la configuracion");

	String FormatoPaso3  = String.format(tdcPaso3, folio);
	
	System.out.println(FormatoPaso3);

	SQLResult Paso3 = executeQuery(db, FormatoPaso3);

	boolean ValidaPaso3 = Paso3.isEmpty();
	if (!ValidaPaso3) {

		testCase.addQueryEvidenceCurrentStep(Paso3);
	}

	System.out.println(ValidaPaso3);

	assertFalse(ValidaPaso3, "No se obtiene información de la consulta");
	
}

@Override
public void beforeTest() {
	// TODO Auto-generated method stub

}

@Override
public String setTestDescription() {
	// TODO Auto-generated method stub
	return "Construccion.Tpe_Cines TRN01";
}

@Override
public String setTestDesigner() {
	// TODO Auto-generated method stub
	return "QA Automation";
}

@Override
public String setTestFullName() {
	// TODO Auto-generated method stub
	return "ATC_FT_Tpe_004_Cines_TRN01";
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
