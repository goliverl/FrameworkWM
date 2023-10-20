package interfaces.tpe.fac;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.TPE_FAC;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;

public class FAC_QRY29 extends BaseExecution {
	
	/*
	 * 
	 * @cp Verificar la operacion QRY29 entity BANK
	 * 
	 */

	@Test(dataProvider = "data-provider")
	public void ATC_FT_005_TPE_FAC_QRY29(HashMap<String, String> data) throws Exception {
	
/* Utilerias *********************************************************************/
//		SqlUtil db = new SqlUtil(GlobalVariables.DB_USERPE1, GlobalVariables.DB_PASSWORDPE1, GlobalVariables.DB_HOSTPE1);
		utils.sql.SQLUtil db = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCACQA,GlobalVariables.DB_USER_FCACQA, GlobalVariables.DB_PASSWORD_FCACQA);
		TPE_FAC facUtil = new TPE_FAC(data, testCase, db);
//		SqlUtil dbUser = new SqlUtil(GlobalVariables.DB_USER_FAC, GlobalVariables.DB_PASSWORD_FAC, GlobalVariables.DB_HOST_FAC);
//		utils.sql.SQLUtil dbUser = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FAC,GlobalVariables.DB_USER_FAC, GlobalVariables.DB_PASSWORD_FAC);
//		TPE_FAC facUtilUser = new TPE_FAC(data, testCase, dbUser);
	
		
/* Variables *********************************************************************/
			
	
		
    String wmCodeQRY29 = "101";	
	String tdcQRY29 = "SELECT WM_CODE,application, ENTITY, OPERATION,FOLIO FROM TPEUSER.TPE_FR_TRANSACTION "
					+ "WHERE folio = %s AND APPLICATION = 'FAC' AND ENTITY = 'BANK' AND OPERATION = 'QRY29'"
					+ " AND CREATION_DATE >= (SYSDATE-1)";

	
	String tdcQRY29user = "SELECT USER_ID, LOGIN_ID, PLAZA, TIENDA, LOGIN_DATE, APP, LOGOUT_DATE FROM TPEUSER.FAC_LOGIN_USERS "
			+ "WHERE PLAZA = '" + data.get("plaza")+"'" 
			+ " AND TIENDA = '" + data.get("tienda")+"'"
			+ " AND USER_ID  = '" + data.get("idusuario")+"'"
			+ "AND TRUNC(LOGIN_DATE) = TRUNC(SYSDATE)";
	
		
	String wmCodeRequestQRY29;					
	String folio;
	String wmCodeRequest;
	String wmCodeDb;	
		
/* Paso 1 *****************************************************************************************/
	
//Paso 1
	addStep("Llamar al servicio de consulta QRY0");
				
		String respuestaQRY29 = facUtil.QRY29();
				
		System.out.println("Respuesta: " + respuestaQRY29);
				
				
 //   	folio = getSimpleDataXml(respuestaQRY29, "folio");
		
		folio = RequestUtil.getFolioXml(respuestaQRY29);
		
		wmCodeRequestQRY29 = RequestUtil.getWmCodeXml(respuestaQRY29);
				
//	   wmCodeRequestQRY29 = getSimpleDataXml(respuestaQRY29, "wmCode");

				
     testCase.passStep();
				
				
/*Paso 2 *********************************************************/
//Paso 2


addStep("Verificar la respuesta generada por el servicio");
				
					
	boolean validationRequest = wmCodeQRY29.equals(wmCodeRequestQRY29);
	System.out.println(validationRequest + " - wmCode request: " );
				
							
	assertTrue(validationRequest , "wmCodeQRY09 no es Igual a wmCodeRequestQRY09");



/*Paso 3 *********************************************************/
//Paso 3
addStep("Validar que se ha creado un registro en la tabla tdc_transaction de TPEUSER");
					
		String queryUSER = String.format(tdcQRY29, folio);
		System.out.println(queryUSER);
		Thread.sleep(6000);

		SQLResult Transaction = executeQuery(db, queryUSER);
			
		wmCodeDb = Transaction.getData(0, "WM_CODE");
		System.out.println(wmCodeDb);

						
//		wmCodeDb = facUtil.getWmCodeQuery(query);
						
		boolean validationDb = wmCodeQRY29.equals(wmCodeDb);
						
		System.out.println(validationDb + " - wmCode db: ");
															
		assertTrue(validationDb , "wmCodeDb no es Igual a wmCodeQRY09");
				
/*Paso 3 *********************************************************/
//Paso 3
					
				
addStep("Validar la actualizacion del ultimo login del usuario (LOGOUT_DATE=SYSDATE) en la tabla FAC_LOGIN_USERS de TPEUSER.");
																		
    SQLResult executequeryUSER = executeQuery(db, tdcQRY29user);
    System.out.println(tdcQRY29user);
	Thread.sleep(6000);

//	String LoginID = executequeryUSER.getData(0, "LOGIN_ID");

    boolean Logout = executequeryUSER.isEmpty();
//	wmCodeDb = facUtil.getWmCodeQuery(query);
											
	assertFalse(Logout , "El Registro no existe");
																																								
//testCase.passStep();			
									
		}
			
			
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Verificar la operacion QRY29 entity BANK";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo de Automatizacion";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_005_TPE_FAC_QRY29";
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
