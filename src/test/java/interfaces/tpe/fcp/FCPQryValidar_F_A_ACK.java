package interfaces.tpe.fcp;

import java.sql.ResultSet;
import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.TPE_FCP;
import util.GlobalVariables;
import util.RequestUtil;

import static org.testng.Assert.assertTrue;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class FCPQryValidar_F_A_ACK extends BaseExecution {
	//faltan datos reales para el invoke del trn02
	
	/*
	 * 
	 * @cp1 FRQ_Validar la generacion del folio único para la transaccion
	 * @cp2 FRQ_Validar la Solicitud de Autorizacion
	 * @cp3 FRQ_Validar la Confirmacion ACK
	 * 
	 */

	@Test(dataProvider = "data-provider")
	public void ATC_FT_TPE_FCP_008_QryValidar_F_A_ACK(HashMap<String, String> data) throws Exception {
		
	/* Utilerias *********************************************************************/
//		SqlUtil db = new SqlUtil(GlobalVariables.DB_USERPE3,GlobalVariables.DB_PASSWORDPE3, GlobalVariables.DB_HOSTPE3);
		SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);


		TPE_FCP rtpUtil = new TPE_FCP(data, testCase, db);
		
		/* Variables *********************************************************************/
		String wmCodeToValidateQRY01 = "100";
		String wmCodeRequestTRN02="100";

		String genFolioQuery = "SELECT APPLICATION,ENTITY,OPERATION,folio, SOURCE,PLAZA,TIENDA,CREATION_DATE,WM_CODE,WM_DESC  FROM TPEUSER.TPE_FR_TRANSACTION WHERE APPLICATION = 'FCP' AND ENTITY = 'FRQ' AND OPERATION = 'TRN01' and folio = %s";
		//String tpeKeyQuery = "select folio,llave,tienda,plaza from RTPUSER.POS_RTP_KEY_LOG where  folio = %s";
		//"SELECT * FROM TPEUSER.TPE_FR_TRANSACTION WHERE APPLICATION = 'FCP' AND ENTITY = 'FRQ' AND OPERATION = 'QRY01' and folio = %s";
		String folio;
		String wmCodeRequestQRY01;
		String wmCodeDbQRY01;
		//String wmCodeDbTRN02;

		/* ***********************************************************************************************
		 * Folio TRN01
		 * ***********************************************************************************************/
		//Paso 1
		addStep("Ejecutar el servicio TPE.RPT.Pub:request para realizar la transaccion de Solicitud de Llaves.");
		
			String respuestaQRY01 = rtpUtil.generacionFolioTransaccion2();
			
			System.out.println("Respuesta: " + respuestaQRY01);
			
			folio = RequestUtil.getFolioXml(respuestaQRY01);
			wmCodeRequestQRY01 = RequestUtil.getWmCodeXml(respuestaQRY01);
		
			boolean validationRequestQRY01 = wmCodeRequestQRY01.equals(wmCodeToValidateQRY01);
			System.out.println(validationRequestQRY01 + " - wmCode request: " + wmCodeRequestQRY01);
		
			assertTrue(validationRequestQRY01);
		
		//Paso 2
		 addStep("Verificar datos insertados en TPUSER.TPE_FR_TRANSACTION");
					SQLUtil db1 = new SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);

				 String querys = String.format(genFolioQuery, folio);
				 SQLResult result2 = executeQuery(db1, querys);
				 System.out.println("Respuesta: FOLIO"+ folio);
				 System.out.println("verQuery 	"+querys);
				
				 //db1.executeQueryAndGetData(querys);
				// String[] columnNames1 = db1.getResultColumnNames();
				// System.out.println("Respuesta:column "+ columnNames1[0]);
				
				 wmCodeDbQRY01 = result2.getData(0, "WM_CODE");
				 System.out.println("consulta wm_code:"+wmCodeDbQRY01);
				
			boolean validationDbTRN01 = result2.isEmpty();
			if(!validationDbTRN01) {
				testCase.addQueryEvidenceCurrentStep(result2);
			}
			
			assertTrue(!validationDbTRN01);
		

		/* ***********************************************************************************************
		 *  TRN02
		 * ***********************************************************************************************/
	
		
		addStep("Ejecutar el servicio TPE.RPT.Pub:request para realizar la transaccion de Solicitud de Llaves.");
		
			String respuestaQRY02 = rtpUtil.generacionFolioTransaccion1(folio);
	
			System.out.println("Respuesta  folio: " + respuestaQRY02);
			
			//folio = RequestUtil.getFolioXml(respuestaQRY02);
			//wmCodeRequestQRY01 = RequestUtil.getWmCodeXml(respuestaQRY02);
			wmCodeRequestTRN02 = RequestUtil.getWmCodeXml(respuestaQRY02);
			String validationDbTRN02TOvalidate="000";
			boolean validationDbTRN02 = wmCodeRequestTRN02.equals(validationDbTRN02TOvalidate);
		//	assertTrue(validationDbTRN02);
			System.out.println(validationDbTRN02TOvalidate + " - wmCode db: " + wmCodeRequestTRN02 );
			
			
			
		testCase.passStep();
		
		//Paso 2
		String Query2="SELECT APPLICATION , ENTITY ,OPERATION ,FOLIO FROM TPEUSER.TPE_FR_TRANSACTION WHERE APPLICATION = 'FCP'" + 
				"AND ENTITY = 'FRQ'" + 
				"AND OPERATION = 'TRN02'" + 
				"AND FOLIO = '"+folio+"'";
		System.out.println("resultado para ver folio "
				+ ": "+ Query2 );

//	  SqlUtil db2 = new SqlUtil( GlobalVariables.DB_USERPE3, GlobalVariables.DB_PASSWORDPE3, GlobalVariables.DB_HOSTPE3);
		SQLUtil db2 = new SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);

	       

		SQLResult result = executeQuery(db2, Query2);
     //List<String[]> queryData = db.getResultData();

		String a = result.getData(0, "FOLIO");
		
  	      System.out.println("resultado: " + a );
		
          if(a!= null) {
		
		  System.out.println("resultado: " + a );
		  
		  testCase.passStep();
		  
		  }  
          
          else {
        	  
        	  testCase.failStep();
        	  
        	  }
   // testCase.passStep();
    
    
   
          String querty="SELECT PLAZA_ID, TIENDA_ID,FOLIO_ID,ACCOUNT_ID, EMISSION_ID  FROM FCPUSER.FQY_LOG_REWARD " + 
	   		" WHERE PLAZA_ID = '" + data.get("plaza") + "'"+
	   		" AND TIENDA_ID = '" + data.get("tienda") + "'"+
	   		" AND FOLIO_ID ='"+folio+"'"+ 
	   		" AND ACCOUNT_ID = '" + data.get("id") + "'"+
	   		" AND EMISSION_ID = '" + data.get("emision") + "'";
	
          System.out.println("resultado ultima consulta validacion: "+ querty);
//    SqlUtil db3 = new SqlUtil( GlobalVariables.DB_USERPE3, GlobalVariables.DB_PASSWORDPE3, GlobalVariables.DB_HOSTPE3);
          SQLUtil db3 = new SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);


    

          SQLResult quertyResult = executeQuery(db3, querty);
	
	      //List<String[]> queryData = db.getResultData();


          String v = quertyResult.getData(0, "FOLIO_ID");
	
	
          System.out.println("resultado: "+ v );
	
          if(v!= null) {
        	  
        	  System.out.println("resultado: "+ v );
        	         	  
	       	  testCase.passStep();
	       	  
          }  
          
          else {
        	  
        	  testCase.failStep();
        	  
        	  }
          
			testCase.passStep();

	/* ***********************************************************************************************
	 * Folio TRN03 ACK
	 * ***********************************************************************************************/
    addStep("Ejecutar el servicio TPE.RPT.Pub:request para realizar la transaccion de Solicitud de Llaves.");
	
	String respuestaQRY03 = rtpUtil.generacionACK(folio);

	System.out.println("Respuesta  : " + respuestaQRY03);
	
	folio = RequestUtil.getFolioXml(respuestaQRY03);
	//wmCodeRequestQRY01 = RequestUtil.getWmCodeXml(respuestaQRY02);
	String wmCodeRequestTRN03 = RequestUtil.getWmCodeXml(respuestaQRY03);
	
	String wmCodeRequestTRN03tovalidate="101";

	boolean validationDbTRN03 = wmCodeRequestTRN03tovalidate.equals(wmCodeRequestTRN03);

	System.out.println(wmCodeRequestTRN03tovalidate + " - wmCode db: " + wmCodeRequestTRN03 );
	assertTrue(validationDbTRN03);
testCase.passStep();
//Se registra la transaccion de confirmacion ACK en la tabla TPE_FR_TRANSACTION de TPEUSER.


String querty2="SELECT APPLICATION, ENTITY,OPERATION, FOLIO  FROM TPEUSER.TPE_FR_TRANSACTION " + 
		" WHERE APPLICATION = 'FCP' " + 
		"AND ENTITY = 'FRQ'" + 
		"AND OPERATION = 'TRN03'" + 
		"AND FOLIO = '"+folio+"'";

System.out.println("resultado ultima consulta ACK: "+querty2);
//SqlUtil db4 = new SqlUtil(  GlobalVariables.DB_USERPE3, GlobalVariables.DB_PASSWORDPE3, GlobalVariables.DB_HOSTPE3);
SQLUtil db4 = new SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);


		SQLResult querty2Result = executeQuery(db4, querty2);
        //List<String[]> queryData = db.getResultData();
   		
		String v2 = querty2Result.getData(0, "FOLIO");
        System.out.println("resultado: " + v2 );

        if(v2!= null) {

        System.out.println("resultado: "+v2 );

             testCase.passStep();}  else {testCase.failStep();}
	}
	


	
	
	@Override
	public String setTestFullName() {
		return "ATC_FT_TPE_FCP_008_QryValidar_F_A_ACK";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Validar la generacion del folio unico para la transaccion\r\n"
				+ "Validar la Solicitud de Autorizacion\r\n"
				+ "Validar el cierre de la transaccion al recibir el mensaje ACK\r\n";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
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

}
