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

public class TPE_TIC_ValidarInsertTickets extends BaseExecution{
	@Test(dataProvider = "data-provider")
	
	/**
	 * 
	 * @author Ultima modificacion Mariana Vives
	 * @date 28/02/2023
	 */
	
	public void TPE_TIC_ValidarInsertTickets_test(HashMap<String, String> data) throws Exception {
		
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

		String InfoTick="select ACCOUNT_ID,ACTIVITY_ID,PLAZA,TIENDA,CREATION_DATE,CODE "
				+ "from TPEUSER.tpe_fcp_activity "
				+ "where plaza = '"+data.get("plaza")+"'";

	
		
		
		String ValidaTpe="select APPLICATION,ENTITY,OPERATION,PLAZA,TIENDA,FOLIO,CREATION_DATE  "
				+ "from TPEUSER.tpe_fr_transaction "
				+ "where application = 'TIC' "
				+ "and entity = 'POS' "
				+ "and operation = 'QRY01' "
				+ "and plaza = '"+data.get("plaza")+"'"
				+ "and trunc(creation_date) = trunc(sysdate)"; 
		

//********************************************************************************************************************************************************************************		
		
		/* Pasos */

//		Paso 1	************************	
		
			addStep("Validar que existe información del ticket en la tabla: tpe_fcp_activity de TPEMON. ");

			
			System.out.println(GlobalVariables.DB_HOST_FCTPE);
			String Tienda="";
			String Code="";
			System.out.println(InfoTick);

		    SQLResult InfoTickExec = executeQuery(dbFCTPEQA, InfoTick);
		       
		       boolean InfoTickRes = InfoTickExec.isEmpty();
		       if(!InfoTickRes) {
		    	   testCase.addQueryEvidenceCurrentStep(InfoTickExec);  
		    	   Tienda = InfoTickExec.getData(0, "TIENDA");
		    	   Code = InfoTickExec.getData(0, "CODE");
		       }
		       System.out.println(InfoTickRes);
		       assertFalse(InfoTickRes,"No se obtuvo el resultado esperado");
			
//			Paso 2 ******************************************************************
		      
		    addStep("Ejecutar el servicio: TPE.TIC.Pub:request e ingresar el archivo XML con la información en el campo de entrada: xmlln.");
			String respuesta = TPE_TICUtil.RunTPE_TIC_Tickets(Tienda,Code);
			System.out.print("Doc: " + respuesta);

			boolean validationResponse = true;

			if (respuesta != null) {
				validationResponse = false;
				testCase.addTextEvidenceCurrentStep("Response: \n" + respuesta);
	
			}

			assertFalse(validationResponse,"No se obtuvo el resultado esperado");
			
//		Paso 3	************************		
			
			addStep("Validar que el código en el XML de respuesta sea el de Transacción Exitosa (WM_CODE 101).");
			String wmCode="101";
			
			String wmCodeRequest= RequestUtil.getWmCodeXml(respuesta);	
			System.out.print("WmCode: "+wmCodeRequest);
			
			
			boolean validationWmcode= wmCodeRequest.equals(wmCode);

			if (validationWmcode!=false) {
				
				testCase.addTextEvidenceCurrentStep("El wmCode ha sido: "+ wmCodeRequest + "\nResponse: \n" + respuesta);
	
			}

			assertTrue(validationWmcode,"No se obtuvo el resultado esperado");
			
//			Paso 4 **************************************************************

			addStep("Validar que la información se insertó correctamente en la tabla tpe_fr_transaction de TPEMON.");
			
			System.out.println(GlobalVariables.DB_HOST_FCTPE);
			System.out.println(ValidaTpe);

		    SQLResult ValidaTpeExec = executeQuery(dbFCTPEQA, ValidaTpe);
		       
		       boolean validationRes = ValidaTpeExec.isEmpty();
		       if(!validationRes) {
		    	   testCase.addQueryEvidenceCurrentStep(ValidaTpeExec);   
		       }
		       System.out.println(validationRes);
		       assertFalse(validationRes,"No se obtuvo el resultado esperado");
	}
	
	



	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "TPE_TIC_ValidarInsertTickets_test";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "TPE_TIC_ValidarInsertTickets";
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
