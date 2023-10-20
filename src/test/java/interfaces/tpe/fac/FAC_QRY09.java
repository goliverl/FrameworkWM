package interfaces.tpe.fac;

import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.TPE_FAC;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;

public class FAC_QRY09 extends BaseExecution {
	
	
	/*
	 * 
	 * @cp Verificar la operacion QRY09 entity BANK
	 * 
	 */


	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_TPE_FAC_QRY09(HashMap<String, String> data) throws Exception {
	
/* Utilerias *********************************************************************/
	//SqlUtil db = new SqlUtil(GlobalVariables.DB_USERPE3, GlobalVariables.DB_PASSWORDPE3, GlobalVariables.DB_HOSTPE3);		
		utils.sql.SQLUtil db = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCACQA,GlobalVariables.DB_USER_FCACQA, GlobalVariables.DB_PASSWORD_FCACQA);
		TPE_FAC facUtil = new TPE_FAC(data, testCase, db);
	//	SqlUtil dbUser = new SqlUtil(GlobalVariables.DB_USER_FAC, GlobalVariables.DB_PASSWORD_FAC, GlobalVariables.DB_HOST_FAC);
	//	utils.sql.SQLUtil dbUser = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FAC,GlobalVariables.DB_USER_FAC, GlobalVariables.DB_PASSWORD_FAC);
	//	TPE_FAC facUtilUser = new TPE_FAC(data, testCase, dbUser);
	
		
/* Variables *********************************************************************/
			
	
		
    String wmCodeQRY09 = "101";	
	String tdcQRY09 = "SELECT wm_code, APPLICATION,ENTITY,OPERATION,FOLIO FROM TPEUSER.TPE_FR_TRANSACTION"
			+ " WHERE APPLICATION = 'FAC'"					
			+ " AND ENTITY = 'BANK' "
			+ " AND OPERATION = 'QRY09'"
			+ " AND folio = %s "
			+ " AND CREATION_DATE >= (SYSDATE-1)";

	
	String tdcQRY09user = "SELECT login_id,user_id,plaza,tienda,modulo FROM FAC_LOGIN_USERS" + 
							" WHERE PLAZA =" + data.get("plaza") 
							+" AND TIENDA ="+ data.get("tienda") 
							+" AND USER_ID =" + data.get("idusuario")
							+ " AND CREATION_DATE >= (SYSDATE-1)";

		
	String wmCodeRequestQRY09;
						
	String folio;
	String wmCodeRequest;
	String wmCodeDb;

/**	
 * Se debera probar las consultas a la tabla ya que el servicio no esta disponible 
 */
	
/* Paso 1 *****************************************************************************************/
	
//Paso 1
	addStep("Llamar al servicio de consulta QRY0");
				
		String respuestaQRY09 = facUtil.QRY09();
				
		System.out.println("Respuesta: " + respuestaQRY09);
				
				
				
		folio = RequestUtil.getFolioXml(respuestaQRY09);
	//	idusuario = RequestUtil.getFolioXml(respuestaQRY09);
				
		wmCodeRequestQRY09 = RequestUtil.getWmCodeXml(respuestaQRY09);
						
				
/*Paso 2 *********************************************************/
//Paso 2


		addStep("Verificar la respuesta generada por el servicio");
				
					
	boolean validationRequest = wmCodeQRY09.equals(wmCodeRequestQRY09);
	System.out.println(validationRequest + " - wmCode request: " );
				
				
			
	assertTrue(validationRequest , "wmCodeQRY09 no es Igual a wmCodeRequestQRY09");


///*Paso 3 *********************************************************/
////Paso 3
 addStep("Validar que se ha creado un registro en la tabla tdc_transaction de TPEUSER");
					
		String query = String.format(tdcQRY09, folio);
		System.out.println(query);
		Thread.sleep(6000);

	    SQLResult Transaction = executeQuery(db, query);
				
		wmCodeDb = Transaction.getData(0, "WM_CODE");
						
		boolean validationDb = wmCodeQRY09.equals(wmCodeDb);
						
		System.out.println(validationDb + " - wmCode db: ");
														
		assertTrue(validationDb , "wmCodeDb no es Igual a wmCodeQRY09");
				
///*Paso 4 *********************************************************/
////Paso 4
					
					
/*addStep("Validar que se ha creado un registro en la tabla tdc_transaction de TPEUSER");
										
	String queryUSER = String.format(tdcQRY09user, idusuario);
	System.out.println(query);
			
    SQLResult executequeryUSER = executeQuery(db, queryUSER);

//	idusuario = facUtil.getColumn(testCase, dbUser, query, "user_id");
	
    String UserID = executequeryUSER.getData(0, "user_id");
	
	boolean validationUser = UserID.equals(idusuario);
	
	System.out.println(validationUser);
											
											
	System.out.println(validationUser + " - user_id");
													
														
	assertTrue(validationUser , "UserID no es Igual a idusuario");*/
									
		}
			
			
		
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Verificar la operacion QRY09 entity BANK";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo de Automatizacion";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_004_TPE_FAC_QRY09";
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
