package interfaces.pe3_col;



import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

import java.util.HashMap;

import org.testng.annotations.Test;
import modelo.BaseExecution;
import om.PE3_COL;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class PE3_COL_Solicitudes  extends BaseExecution  {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_PE3_COL_Solicitudes(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 ********************************************************************************************************************************************/
		/*
		 * LA CONEXION A LA BD NO SE PUEDE HACER POR QUE FALTA SABER CONTRASEÑA PARA EL USUARIO TPECOUSER
		 * SI SE USA USUARIO DIFERENTE EN LA CONEXION, NO SE PUEDE VER LA TABLA UTILIZADA PARA ESTA PRUEBA
		 * LA CONEXION NECESARIA ES:
		 * USER: TPECOUSER
		 * PASS: ???? DSCONOCIDO
		 * HOST: 10.184.80.120:1521/FCTPEQA.FEMCOM.NET
		 */
		
		SQLUtil dbFCTPE_COL = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE_CO, GlobalVariables.DB_USER_FCTPE_CO,
				GlobalVariables.DB_PASSWORD_FCTPE_CO);

		
		PE3_COL PE3_COLUtil = new PE3_COL(data, testCase, null);

		/*
		 * Variables
		 ******************************************************************************************************************************************/

		String wmCodeToValidate="100";
		String wmCodeToValidateAuth="000";
		String wmCodeToValidateAck = "101";

		String TransactionQuery="SELECT FOLIO, CREATION_DATE, UPC,CARD_NO, AMOUNT, WM_CODE "
				+ "FROM TPECOUSER.gif_col_transaction "
				+ "WHERE folio = '%s' AND wm_code = '%s'";
		

//********************************************************************************************************************************************************************************		

		/* Pasos */

//		Paso 1	************************	
	
			addStep("Ejecutar el servicio de Solicitud de Folio con los parámetros requeridos vía http. ");
			String folio ="";
			String wmCodeRequest ="";
			String respuesta = PE3_COLUtil.SolicitudFolio();
			System.out.print("Doc: " + respuesta);

			boolean validationResponse = true;

			if (respuesta != null) {
				validationResponse = false;
				testCase.addTextEvidenceCurrentStep("Response: \n" + respuesta);
				folio = getSimpleDataXml(respuesta, "folio");
				wmCodeRequest = getSimpleDataXml(respuesta, "wmCode");
			}

			assertFalse(validationResponse,"No se obtuvo el resultado esperado");
//		Paso 2	************************		

			addStep("Verificar la respuesta generada por el servicio");

			boolean validaRequest = wmCodeRequest.equals(wmCodeToValidate);

			System.out.println(validaRequest + " - wmCode request: " + wmCodeRequest);
			
			if(validaRequest==true) {
			testCase.addTextEvidenceCurrentStep("Response: \n" + respuesta);
			}
			assertTrue(validaRequest, "El campo wmCode no es el esperado");

//		Paso 3	************************
		/*
		 * LA CONEXION A LA BD NO SE PUEDE HACER POR QUE FALTA SABER CONTRASEÑA PARA EL USUARIO TPECOUSER
		 * SI SE USA USUARIO DIFERENTE EN LA CONEXION, NO SE PUEDE VER LA TABLA UTILIZADA PARA ESTA PRUEBA
		 * LA CONEXION NECESARIA ES:
		 * USER: TPECOUSER
		 * PASS: ???? DSCONOCIDO
		 * HOST: 10.184.80.120:1521/FCTPEQA.FEMCOM.NET
		 */
			
			addStep("Validar que se ha creado un registro del folio en la tabla tpeuser.gif_col_transaction");
			String query = String.format(TransactionQuery, folio, wmCodeToValidate);
			System.out.println(query);
			SQLResult resultQuery = dbFCTPE_COL.executeQuery(query);

			boolean resultQueryRes = resultQuery.isEmpty();

			if (!resultQueryRes) {


				testCase.addQueryEvidenceCurrentStep(resultQuery);
				
			}
			
			assertFalse(resultQueryRes, "No se encontraron datos en la consulta");

			/**************************************************************************************************
			 * Solicitud de autoriazación
			 *************************************************************************************************/

			/*
			 * Paso 4
			 *****************************************************************************************/
				
			addStep("Llamar al servico PE3_COL.Pub:runGetActivation");

			String respuestaActiv = PE3_COLUtil.SolicitudActiv(folio);
			System.out.print("Res Activ: " + respuestaActiv);

			
			validationResponse = true;
			
			if (respuestaActiv != null) {
				validationResponse = false;
				testCase.addTextEvidenceCurrentStep("Response Activacion: \n" + respuestaActiv);
				folio = getSimpleDataXml(respuestaActiv, "folio");
				wmCodeRequest = getSimpleDataXml(respuestaActiv, "wmCode");
			}

			assertFalse(validationResponse,"No se obtuvo el resultado esperado");
			
			/*
			 * Paso 5
			 *****************************************************************************************/
	
			addStep("Verificar la respuesta generada por el servicio");

			boolean validaRequestActiv = wmCodeRequest.equals(wmCodeToValidateAuth);
			
			System.out.println("Request Activacion: "+validaRequestActiv + " - wmCode request: " + wmCodeRequest);

			
			if(validaRequestActiv==true) {
				testCase.addTextEvidenceCurrentStep(respuestaActiv);
				}
			
			assertTrue(validaRequestActiv, "El campo wmCode no es el esperado");

			/*
			 * Paso 6
			 *****************************************************************************************/
					 
			addStep("Validar actualización del campo WM_CODE = ‘000’ al ser exitosa la ejecución de la interfaz");
			String UpdWmCodeActiv = String.format(TransactionQuery, folio, wmCodeToValidate);
			System.out.println(UpdWmCodeActiv);
			SQLResult ExecUpdWmCodeActiv = dbFCTPE_COL.executeQuery(UpdWmCodeActiv);

			boolean UpdWmCodeActivRes = ExecUpdWmCodeActiv.isEmpty();

			if (!UpdWmCodeActivRes) {


				testCase.addQueryEvidenceCurrentStep(ExecUpdWmCodeActiv);
				
			}
			
			assertFalse(UpdWmCodeActivRes, "No se encontraron datos en la consulta");
			
			
			/**************************************************************************************************
			 * Solicitud de autoriazación ACK
			 *************************************************************************************************/
			/*
			 * Paso 7
			 *****************************************************************************************/
			
			addStep("Ejecutar el servicio de Solicitud ACK con los parámetros requeridos. ");

			String respuestaAck = PE3_COLUtil.RunGetAck(folio);
			System.out.print("Res Ack: " + respuestaAck);
			
			validationResponse = true;
			
			if (respuestaAck != null) {
				validationResponse = false;
				testCase.addTextEvidenceCurrentStep("Response Ack: \n" + respuestaAck);
				folio = getSimpleDataXml(respuestaAck, "folio");

				wmCodeRequest = getSimpleDataXml(respuestaAck, "wmCode");
			}

			assertFalse(validationResponse,"No se obtuvo el resultado esperado");

			/*
			 * Paso 8
			 *****************************************************************************************/

			addStep("Verificar la respuesta generada por el servicio");

			boolean validationRequestAck = wmCodeRequest.equals(wmCodeToValidateAck);
			
			System.out.println("Request Ack: "+validationRequestAck + " - wmCode request: " + wmCodeRequest);
			
			if(validationRequestAck==true) {
				testCase.addTextEvidenceCurrentStep("Response ack \n" + respuestaAck);
				}
			

			assertTrue(validationRequestAck, "El campo wmCode no es el esperado");
			

			/*
			 * Paso 9
			 *****************************************************************************************/

			addStep("Se valida que el campo wm_code fue actualizado a 101 al ser una transacción exitosa.");

			String queryAck = String.format(TransactionQuery, folio, wmCodeToValidate);
			System.out.println(queryAck);
			SQLResult ExecqueryAck = dbFCTPE_COL.executeQuery(queryAck);

			boolean queryAckRes = ExecqueryAck.isEmpty();

			if (!queryAckRes) {


				testCase.addQueryEvidenceCurrentStep(ExecqueryAck);
				
			}
			
			assertFalse(queryAckRes, "No se encontraron datos en la consulta");

		
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_PE3_COL_Solicitudes";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "PE3_COL_Solicitudes";
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
