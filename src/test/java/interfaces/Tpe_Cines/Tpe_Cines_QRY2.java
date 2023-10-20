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

public class Tpe_Cines_QRY2 extends BaseExecution  {
	
@Test(dataProvider = "data-provider")
	
	public void ATC_FT_Tpe_003_Cines_QRY2 (HashMap<String, String> data) throws Exception {
				
/* Utilerías *********************************************************************/

		SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST_FCACQA, GlobalVariables.DB_USER_FCACQA, GlobalVariables.DB_PASSWORD_FCACQA);


		Tpe_Cines TPECinesUTIL = new Tpe_Cines(data, testCase, db);
	
		
		
/* Variables *********************************************************************/
					
    String wmCodeQry02 = "101";	
	
    String tdcPaso1 = " SELECT APPLICATION, ENTITY, OPERATION, LOWER(CODE) CODE, CATEGORY, VALUE1, VALUE2, VALUE3,  \r\n"
			+ " TO_CHAR(DATE1, 'YYYYMMDDHH24MiSS') DATE1, DESCRIPTION, CREATION_DATE \r\n"
			+ " FROM TPE_FR_CONFIG \r\n"
			+ " WHERE APPLICATION = '"+data.get("application")+"' \r\n"
			+ " AND ENTITY = '"+data.get("entity")+"' \r\n"
			+ " AND OPERATION = '" +data.get("operation")+ "' \r\n";

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

	String respuestaQry02 = TPECinesUTIL.QRY02();

	System.out.println("Respuesta: " + respuestaQry02);

	String wmcodigo = RequestUtil.getWmCodeXml(respuestaQry02);
	System.out.println(wmcodigo);

	testCase.passStep();

	/* Paso 3 *********************************************************/

	addStep("Verificar xmlResponse");

	boolean validationRequest = wmCodeQry02.equals(wmcodigo);

	System.out.println(validationRequest + " - wmCode request: " + wmcodigo);

	testCase.validateStep(validationRequest);


	
}

@Override
public void beforeTest() {
	// TODO Auto-generated method stub

}

@Override
public String setTestDescription() {
	// TODO Auto-generated method stub
	return "Contruccion.Tpe_Cines_Ciudad_Qry02";
}

@Override
public String setTestDesigner() {
	// TODO Auto-generated method stub
	return "QA Automation";
}

@Override
public String setTestFullName() {
	// TODO Auto-generated method stub
	return "ATC_FT_Tpe_003_Cines_QRY2";
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
