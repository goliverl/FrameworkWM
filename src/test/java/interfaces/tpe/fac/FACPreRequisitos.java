package interfaces.tpe.fac;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.TPE_FAC;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
public class FACPreRequisitos extends BaseExecution {
	
@Test(dataProvider = "data-provider")
	
	public void test(HashMap<String, String> data) throws Exception {

	SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST_FCACQA, GlobalVariables.DB_USER_FCACQA, GlobalVariables.DB_PASSWORD_FCACQA);


	TPE_FAC facUtil = new TPE_FAC(data, testCase, db);

	
	String query = "select USER_ID, PLAZA, TIENDA, STATUS, EXPIRED_DATE "
			+ " from tpeuser.fac_users" 
			+ " where USER_ID='&s'" 
			+ " and EXPIRED_DATE >= TRUNC(SYSDATE)";
	
	String act = "A";
	String inact = "I";
	String nuevo = "N";
	String bloqueado = "B";
	String reset = "R";
	
	
	
	//Pasos------------------------------------------------------------------------------
	addStep("RESUMEN");
	//*******************Paso 1 *************************************************************************************************************
	
	testCase.addBoldTextEvidenceCurrentStep("Validar que la fecha de expiracion en el usuario en la tabla fac_users este vigente y validar el estatus del usuario.");
	
	SQLResult result1 = executeQuery(db, query);
	
	
	String status = result1.getData(0, "STATUS");
	
	boolean expiredDate = result1.isEmpty();
	
	if(!expiredDate){
		
		testCase.addTextEvidenceCurrentStep("-El usuario "+data.get("user_id")+" no ha expirado");
		if(status.equals(act)) {
			testCase.addTextEvidenceCurrentStep("El estatus del usuario es Activo");
		}
		if(status.equals(inact)) {
			testCase.addTextEvidenceCurrentStep("El estatus del usuario es Inactivo");
		}
		if(status.equals(nuevo)) {
			testCase.addTextEvidenceCurrentStep("El estatus del usuario es Nuevo");
		}
		if(status.equals(bloqueado)) {
			testCase.addTextEvidenceCurrentStep("El estatus del usuario es Bloquado");
		}
		
		if(status.equals(reset)) {
			testCase.addTextEvidenceCurrentStep("El estatus del usuario es Reset");
		}
			
		
	}else {
		testCase.addTextEvidenceCurrentStep("-El usuario "+data.get("user_id")+" ha expirado");
		if(status.equals(act)) {
			testCase.addTextEvidenceCurrentStep("El estatus del usuario es Activo");
		}
		if(status.equals(inact)) {
			testCase.addTextEvidenceCurrentStep("El estatus del usuario es Inactivo");
		}
		if(status.equals(nuevo)) {
			testCase.addTextEvidenceCurrentStep("El estatus del usuario es Nuevo");
		}
		if(status.equals(bloqueado)) {
			testCase.addTextEvidenceCurrentStep("El estatus del usuario es Bloquado");
		}
		
		if(status.equals(reset)) {
			testCase.addTextEvidenceCurrentStep("El estatus del usuario es Reset");
		}
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
	return null;
}

@Override
public String setTestDesigner() {
	// TODO Auto-generated method stub
	return null;
}

@Override
public String setTestFullName() {
	// TODO Auto-generated method stub
	return null;
}

@Override
public String setTestInstanceID() {
	// TODO Auto-generated method stub
	return null;
}
				
	

}
