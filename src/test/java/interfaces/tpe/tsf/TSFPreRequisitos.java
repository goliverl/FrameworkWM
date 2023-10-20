 	package interfaces.tpe.tsf;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.TPE_FAC;
import om.TPE_TSF;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class TSFPreRequisitos extends BaseExecution{
	
@Test(dataProvider = "data-provider")
	
	public void ATC_FT_007_TSF_PreRequisitos(HashMap<String, String> data) throws Exception {
	
	SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
	TPE_TSF tsfUtil = new TPE_TSF(data, testCase, db);
	
	//Utileria
	
	String ValidateMGToken = "SELECT * FROM TPEUSER.TPE_TOKEN_ACCESS WHERE TOKEN = 'TEST' "
			+ " AND PLAZA ="+"'" + data.get("PLAZA")+"'"+
			" AND TIENDA = "+"'"+ data.get("TIENDA")+"'";
	
	String ValidateMG = "SELECT APPLICATION,ENTITY,OPERATION,CODE,CATEGORY,VALUE1,DESCRIPTION FROM TPEUSER.TPE_FR_CONFIG WHERE ENTITY = 'MGI' AND CODE = 'url' AND OPERATION = 'QRY01'";
	
	String validateTNC = "SELECT APPLICATION,ENTITY,OPERATION,CODE,CATEGORY,VALUE1,DESCRIPTION FROM TPEUSER.TPE_FR_CONFIG WHERE ENTITY = 'TNC' AND CODE = 'url' AND OPERATION IN ('QRY01','QRY02','TRN02','TRN03','TRN04')";
	
	String ValidateWU = "SELECT APPLICATION,ENTITY,OPERATION,CODE,CATEGORY,VALUE1,DESCRIPTION FROM TPEUSER.TPE_FR_CONFIG WHERE APPLICATION='TSF' AND ENTITY = 'WU' AND CODE = 'URL' AND OPERATION IN ('QRY01','QRY02','QRY03','TRN02')";
	
	String plaza = data.get("PLAZA");
	String tienda = data.get("TIENDA");
	//Pasos *******************************************************************************************************************************************
	
	testCase.setProject_Name("Configuracion de ambientes.");
	addStep("Resumen");
	
	// ************************************************ PASO 1 *******************************************************************************************
		testCase.addBoldTextEvidenceCurrentStep("Verificar que el servicio Money Gram este apuntando a simulador");
		
		SQLResult ValidaMG = executeQuery(db, ValidateMG);
		String MN = ValidaMG.getData(0, "VALUE1");
		boolean queryMGEmpty = ValidaMG.isEmpty();
		
		boolean MGQ01 = MN.contains(data.get("MN")); 
		if(!queryMGEmpty) {
			if(MGQ01) {
				testCase.addTextEvidenceCurrentStep("-El Servicio MoneyGram esta apuntando a DevTest");
			}{
				testCase.addTextEvidenceCurrentStep("-El Servicio MoneyGram esta apuntando a Proveedor");
			}
					}else {
		}
		
		
		// ************************************************ PASO 2 *******************************************************************************************

		testCase.addBoldTextEvidenceCurrentStep("Verificar que el servicio Trans Network este apuntando a simulador");

		SQLResult ValidaTNC = executeQuery(db, validateTNC);
		String tncQ01 = ValidaTNC.getData(0, "VALUE1");
		String tncQ02 = ValidaTNC.getData(1, "VALUE1");
		String tncT02 = ValidaTNC.getData(2, "VALUE1");
		String tncT03 = ValidaTNC.getData(3, "VALUE1");
		String tncT04 = ValidaTNC.getData(4, "VALUE1");
		
		boolean queryTNCEmpty = ValidaTNC.isEmpty();
		boolean TNCQ01 = tncQ01.contains(data.get("TNC")); 
		boolean TNCQ02 = tncQ02.contains(data.get("TNC")); 
		boolean TNCT02 = tncT02.contains(data.get("TNC")); 
		boolean TNCT03 = tncT03.contains(data.get("TNC")); 
		boolean TNCT04 = tncT04.contains(data.get("TNC")); 

		
		if(!queryTNCEmpty) {
			if(TNCQ01) {
				testCase.addTextEvidenceCurrentStep("-QRY01 esta apuntando a simulador");
			}else {
				testCase.addTextEvidenceCurrentStep("-QRY01 esta apuntando a Proveedor");
			}
			
			if(TNCQ02) {
				testCase.addTextEvidenceCurrentStep("-QRY02 esta apuntando a simulador");
			}else {
				testCase.addTextEvidenceCurrentStep("-QRY02 esta apuntando a Proveedor");
			}
			
			if(TNCT02) {
				testCase.addTextEvidenceCurrentStep("-TRN02 esta apuntando a simulador");
			}else {
				testCase.addTextEvidenceCurrentStep("-TRN02 esta apuntando a Proveedor");
			}
			
			if(TNCT03) {
				testCase.addTextEvidenceCurrentStep("-TRN03 esta apuntando a simulador");
			}else {
				testCase.addTextEvidenceCurrentStep("-TRN03 esta apuntando a Proveedor");
			}
			
			if(TNCT04) {
				testCase.addTextEvidenceCurrentStep("-TRN04 esta apuntando a simulador");
			}else {
				testCase.addTextEvidenceCurrentStep("-TRN04 esta apuntando a Proveedor");
			}
			
		}else {
 			testCase.addTextEvidenceCurrentStep("-El Servicio TransNetwork no se encuentra configurado");
		}
		
		// ************************************************ PASO 3 *******************************************************************************************

		testCase.addBoldTextEvidenceCurrentStep("Verificar que el servicio Waster Union este apuntando a simulador");

		SQLResult ValidaWU = executeQuery(db, ValidateWU);
		String WUQ01 = ValidaWU.getData(0, "VALUE1");
		String WUQ02 = ValidaWU.getData(1, "VALUE1");
		String WUQ03 = ValidaWU.getData(2, "VALUE1");
		String QUT02 = ValidaWU.getData(3, "VALUE1");
		
		
	
		boolean VWUQ01 = WUQ01.contains(data.get("WU")); 
		boolean VWUQ02 = WUQ02.contains(data.get("WU")); 
		boolean VWUQ03 = WUQ03.contains(data.get("WU")); 
		boolean VWUT02 = QUT02.contains(data.get("WU")); 
	
		boolean queryWUEmpty = ValidaWU.isEmpty();
		
		if(!queryWUEmpty) {
			//testCase.addQueryEvidenceCurrentStep(ValidaWU);
			if(VWUQ01) {
				testCase.addTextEvidenceCurrentStep("-QRY01 esta apuntando a simulador");
			}else {
				testCase.addTextEvidenceCurrentStep("-QRY01 esta apuntando a Proveedor");
			}
			if(VWUQ02) {
				testCase.addTextEvidenceCurrentStep("-QRY02 esta apuntando a simulador");
			}else {
				testCase.addTextEvidenceCurrentStep("-QRY02 esta apuntando a Proveedor");
			}
			if(VWUQ03) {
				testCase.addTextEvidenceCurrentStep("-QRY03 esta apuntando a simulador");
			}else {
				testCase.addTextEvidenceCurrentStep("-QRY03 esta apuntando a Proveedor");
			}
			if(VWUT02) {
				testCase.addTextEvidenceCurrentStep("-TRN02 esta apuntando a simulador");
			}else {
				testCase.addTextEvidenceCurrentStep("-TRN02 esta apuntando a Proveedor");
			}
		}else {
			//testCase.addQueryEvidenceCurrentStep(ValidaWU);
			testCase.addTextEvidenceCurrentStep("-El Servicio  Waster Union no se encuentra configurado");
		}
		
		
		// ************************************************ PASO 4 *******************************************************************************************
		
		testCase.addBoldTextEvidenceCurrentStep("Verificar que el token este dado de alta en la Tienda "+tienda+" y Plaza"+plaza+"");

		SQLResult VMGToken = executeQuery(db, ValidateMGToken);
			
			boolean MGEmpty = VMGToken.isEmpty();
			
			if(!MGEmpty) {
				//testCase.addQueryEvidenceCurrentStep(VMGToken);
				testCase.addTextEvidenceCurrentStep("-El token se encuentra dado de alta en la Tienda "+tienda+" y Plaza"+plaza+"");
			}else {
				//testCase.addQueryEvidenceCurrentStep(VMGToken);
				testCase.addTextEvidenceCurrentStep("-El token no se encuentra dado de alta en la Tienda "+tienda+" y Plaza"+plaza+"");
			}
			
			
			// *******************************************************************************************************************************************

			testCase.addBoldTextEvidenceCurrentStep("                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              Envidencias");
			//EVIDENCIA
			
			// *******************************************************************************************************************************************
			
	// ************************************************ PASO 1 *******************************************************************************************
	addStep("Verificar que el servicio Money Gram este apuntando a simulador");
	
	 ValidaMG = executeQuery(db, ValidateMG);
	
	 queryMGEmpty = ValidaMG.isEmpty();
	  MGQ01 = MN.contains(data.get("MN")); 
		if(!queryMGEmpty) {
			testCase.addQueryEvidenceCurrentStep(ValidaMG);
			if(MGQ01) {
				testCase.addTextEvidenceCurrentStep("-El Servicio MoneyGram esta apuntando a DevTest");
			} 	else {		testCase.addTextEvidenceCurrentStep("-El Servicio MoneyGram esta apuntando a Proveedor");
			}
					}else {
						testCase.addTextEvidenceCurrentStep("-El Servicio  Money Gram no se encuentra configurado");
		}
		
	
	
	// ************************************************ PASO 2 *******************************************************************************************

	addStep("Verificar que el servicio Trans Network este apuntando a simulador");

	 ValidaTNC = executeQuery(db, validateTNC);
	
	 queryTNCEmpty = ValidaTNC.isEmpty();
	 				
		if(!queryTNCEmpty) {
			testCase.addQueryEvidenceCurrentStep(ValidaTNC);
			if(TNCQ01) {
				testCase.addTextEvidenceCurrentStep("-QRY01 esta apuntando a simulador");
			}else {
				testCase.addTextEvidenceCurrentStep("-QRY01 esta apuntando a Proveedor");
			}
			
			if(TNCQ02) {
				testCase.addTextEvidenceCurrentStep("-QRY02 esta apuntando a simulador");
			}else {
				testCase.addTextEvidenceCurrentStep("-QRY02 esta apuntando a Proveedor");
			}
			
			if(TNCT02) {
				testCase.addTextEvidenceCurrentStep("-TRN02 esta apuntando a simulador");
			}else {
				testCase.addTextEvidenceCurrentStep("-TRN02 esta apuntando a Proveedor");
			}
			
			if(TNCT03) {
				testCase.addTextEvidenceCurrentStep("-TRN03 esta apuntando a simulador");
			}else {
				testCase.addTextEvidenceCurrentStep("-TRN03 esta apuntando a Proveedor");
			}
			
			if(TNCT04) {
				testCase.addTextEvidenceCurrentStep("-TRN04 esta apuntando a simulador");
			}else {
				testCase.addTextEvidenceCurrentStep("-TRN04 esta apuntando a Proveedor");
			}
			
		}else {
			testCase.addTextEvidenceCurrentStep("-El Servicio TransNetwork no se encuentra configurado");
		}
	
	// ************************************************ PASO 3 *******************************************************************************************

	addStep("Verificar que el servicio Waster Union este apuntando a simulador");

	 ValidaWU = executeQuery(db, ValidateWU);
	 
	 System.out.print(ValidateWU);
	 queryWUEmpty = ValidaWU.isEmpty();
	
		if(!queryWUEmpty) {
			testCase.addQueryEvidenceCurrentStep(ValidaWU);
			if(VWUQ01) {
				testCase.addTextEvidenceCurrentStep("-QRY01 esta apuntando a simulador");
			}else {
				testCase.addTextEvidenceCurrentStep("-QRY01 esta apuntando a Proveedor");
			}
			if(VWUQ02) {
				testCase.addTextEvidenceCurrentStep("-QRY02 esta apuntando a simulador");
			}else {
				testCase.addTextEvidenceCurrentStep("-QRY02 esta apuntando a Proveedor");
			}
			if(VWUQ03) {
				testCase.addTextEvidenceCurrentStep("-QRY03 esta apuntando a simulador");
			}else {
				testCase.addTextEvidenceCurrentStep("-QRY03 esta apuntando a Proveedor");
			}
			if(VWUQ01) {
				testCase.addTextEvidenceCurrentStep("-TRN02 esta apuntando a simulador");
			}else {
				testCase.addTextEvidenceCurrentStep("-TRN02 esta apuntando a Proveedor");
			}
		}else {
			testCase.addQueryEvidenceCurrentStep(ValidaWU);
			testCase.addTextEvidenceCurrentStep("-El Servicio  Waster Union no se encuentra configurado");
		}
		
	
	// ************************************************ PASO 4 *******************************************************************************************
	
	addStep("Verificar que el token este dado de alta en la Tienda "+tienda+" y Plaza"+plaza+"");

	 VMGToken = executeQuery(db, ValidateMGToken);
		
		 MGEmpty = VMGToken.isEmpty();
		
		if(!MGEmpty) {
			testCase.addQueryEvidenceCurrentStep(VMGToken);
			testCase.addTextEvidenceCurrentStep("-El token se encuentra dado de alta en la Tienda "+tienda+" y Plaza"+plaza+"");
		}else {
			testCase.addQueryEvidenceCurrentStep(VMGToken);
			testCase.addTextEvidenceCurrentStep("-El token no se encuentra dado de alta en la Tienda "+tienda+" y Plaza"+plaza+"");
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
	return "Validación de la Configuración de los servicios TNC, WU, MG y el token dado de alta de MG.";
}

@Override
public String setTestDesigner() {
	return "tbd";
}

@Override
public String setTestFullName() {
	// TODO Auto-generated method stub
	return "ATC_FT_007_TSF_PreRequisitos";
}

@Override
public String setTestInstanceID() {
	// TODO Auto-generated method stub
	return null;
}
	

}
