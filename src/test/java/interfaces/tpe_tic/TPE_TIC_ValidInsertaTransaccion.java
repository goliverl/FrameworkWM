package interfaces.tpe_tic;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


import java.util.HashMap;

import org.testng.annotations.Test;


import modelo.BaseExecution;
import om.TPE_TIC;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;


public class TPE_TIC_ValidInsertaTransaccion extends BaseExecution{
	@Test(dataProvider = "data-provider")
	
	/**
	 * 
	 * @author Ultima modificacion Mariana Vives
	 * @date 28/02/2023
	 */
	
	public void TPE_TIC_ValidInsertaTransaccion_test(HashMap<String, String> data) throws Exception {
		
		/*
		 * Utileria
		 * 
		 */
		
		SQLUtil dbFCTPEQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE,
				GlobalVariables.DB_PASSWORD_FCTPE);
		
		TPE_TIC TPE_TICUtil = new TPE_TIC(data, testCase, null);

		/*
		 * Variables
		 ******************************************************************************************************************************************/

		

		String ValidaTpe="select APPLICATION,ENTITY,OPERATION,PLAZA,TIENDA,FOLIO,CREATION_DATE  "
				+ "from TPEUSER.tpe_fr_transaction "
				+ "where application = 'TIC' "
				+ "and entity = 'POS' "
				+ "and operation = 'QRY02' "
				+ "and plaza = '"+data.get("plaza")+"'"; 
		
		
		String ValidaFCP= "SELECT ACCOUNT_ID,PLAZA,TIENDA,CREATION_DATE "
				+ "FROM TPEUSER.TPE_FCP_ACTIVITY  "
				+ " WHERE plaza = '"+data.get("plaza")+"' "
				+ "ORDER BY CREATION_DATE DESC";
		

//********************************************************************************************************************************************************************************		
		
		/* Pasos */

//		Paso 1	************************	
		
			addStep("Ejecutar el servicio: TPE.TIC.Pub:request e ingresar la informaci�n del XML de entrada para el campo: xmlln.");
			
			String respuesta = TPE_TICUtil.RunTPE_TIC_request();
			System.out.print("Doc: " + respuesta);

			boolean validationResponse = true;

			if (respuesta != null) {
				validationResponse = false;
				testCase.addTextEvidenceCurrentStep("Response: \n" + respuesta);
	
			}

			assertFalse(validationResponse,"No se obtuvo el resultado esperado");
			
//		Paso 2	************************		
			addStep("Validar que el c�digo en el XML de respuesta sea el de Transacci�n Exitosa (WM_CODE 101).");
			String wmCode="101";
			
			String wmCodeRequest= RequestUtil.getWmCodeXml(respuesta);	
			System.out.print("WmCode: "+wmCodeRequest);
			
			
			boolean validationWmcode= wmCodeRequest.equals(wmCode);

			if (validationWmcode!=false) {
				
				testCase.addTextEvidenceCurrentStep("El wmCode ha sido: "+ wmCodeRequest + "\nResponse: \n" + respuesta);
	
			}

			assertTrue(validationWmcode,"No se obtuvo el resultado esperado");
			
//			Paso 3 **************************************************************
	
			addStep("Validar que la informaci�n se insert� correctamente en la tabla tpe_fr_transaction");
			
			System.out.println(GlobalVariables.DB_HOST_FCTPE);
			System.out.println(ValidaTpe);

		    SQLResult ValidaTpeExec = executeQuery(dbFCTPEQA, ValidaTpe);
		       
		       boolean validationRes = ValidaTpeExec.isEmpty();
		       if(!validationRes) {
		    	   testCase.addQueryEvidenceCurrentStep(ValidaTpeExec);   
		       }
		       System.out.println(validationRes);
		       assertFalse(validationRes,"No se obtuvo el resultado esperado");
//				Paso 4 **************************************************************
		   	
				addStep("Validar que la informaci�n se insert� correctamente en la tabla tpe_fcp_activity de TPEMON.");
			
				System.out.println(GlobalVariables.DB_HOST_FCTPE);
				System.out.println(ValidaFCP);

			    SQLResult ValidaFCPExec = executeQuery(dbFCTPEQA, ValidaFCP);
			       
			       boolean ValidaFCPRes = ValidaFCPExec.isEmpty();
			       if(!ValidaFCPRes) {
			    	   testCase.addQueryEvidenceCurrentStep(ValidaFCPExec);   
			       }
			       System.out.println(ValidaFCPRes); 
		       
			       assertFalse(ValidaFCPRes,"No se obtuvo el resultado esperado");
		       
		       
	}
	
	



	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "TPE_TIC_ValidInsertaTransaccion_test";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "TPE_TIC_ValidInsertaTransaccion";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
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
