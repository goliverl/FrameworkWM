package interfaces.tpe.fcp;

import static org.testng.Assert.assertTrue;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.TPE_FCP;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;


public class FCPUpd03Tpe extends BaseExecution{
	
	/*
	 * 
	 * @cp TPE_Realizar la actualizacion de los datos frecuentes de clientes
	 * 
	 */
	
		@Test(dataProvider = "data-provider")
		public void ATC_FT_TPE_FCP_010_Upd03Tpe(HashMap<String, String> data) throws Exception {
			
			/* Utilerias *********************************************************************/
//			SqlUtil db = new SqlUtil(GlobalVariables.DB_USERPE3, GlobalVariables.DB_PASSWORDPE3, GlobalVariables.DB_HOSTPE3);
			SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);

			TPE_FCP fcpUtil = new TPE_FCP(data, testCase, db);	
			
	        /* Variables *********************************************************************/		
			String queryValidation01 = "SELECT CODE,ACCOUNT_ID,STATUS,DESCRIPTION,CREATION_DATE,ACTUALIZATION_DATE "
					+ "FROM TPEUSER.TPE_FCP_FREQ_DATA "
					+ "WHERE APPLICATION = 'FCP' AND ENTITY = 'TPE' AND CATEGORY = 'TPE_LIST' AND CODE = '%s'";
			
			String queryValidation02 = "SELECT OPERATION,FOLIO,CREATION_DATE,PLAZA,TIENDA,WM_CODE,WM_DESC "
					+ "FROM TPEUSER.TPE_FR_TRANSACTION "
					+ "WHERE APPLICATION = 'FCP' AND ENTITY = 'TPE' AND OPERATION = 'OPR01' AND FOLIO = %s";
			
			String queryValidation03 = "SELECT CODE,ACCOUNT_ID,STATUS,DESCRIPTION,VALUE1,VALUE2,CREATION_DATE,ACTUALIZATION_DATE "
					+ "FROM TPEUSER.TPE_FCP_FREQ_DATA "
					+ "WHERE APPLICATION = 'FCP' AND ENTITY = 'TPE' AND CATEGORY = 'TPE_LIST' AND CODE = '%s'";
			
	 		String folio;
			String wmCodeRequestTRN01, dateDbTRN01;
			String wmCodeToValidateTRN01 = "101";
			
			/* Pasos ************************************************************************************************/
			// Paso 1		
			addStep("Validar que exista informacion de los datos frecuentes para el cardId.");
			
			String query01 = String.format(queryValidation01,data.get("code"));
			System.out.println(query01);		
			
			SQLResult rs = executeQuery(db, query01); //ejecutar query
			
			boolean isEmpty = rs.isEmpty(); //valida si la consulta regresa registros
			if(!isEmpty) {
				
				testCase.addQueryEvidenceCurrentStep(rs);
				
			}
	
			System.out.println("El resultado de la consulta es null: " + isEmpty);	
			

			assertTrue(!isEmpty);
			  
		    // Paso 2
		    //NO SE TIENE XML PARA REALIZAR EL INVOKE, POR LO QUE NO SE SABE SI FUNCIONA ESTE PASO
			addStep("Ejecutar el servicio TPE.FCP.Pub:request para realizar la actualizacion de los datos frecuentes.");
			
			String respuestaTRN01 = fcpUtil.ejecutarOPR01();
			System.out.println("Respuesta:\n" + respuestaTRN01);
			folio = RequestUtil.getFolioXml(respuestaTRN01);
			wmCodeRequestTRN01 = RequestUtil.getWmCodeXml(respuestaTRN01);
			
			testCase.passStep();
			
			// Paso 3
			addStep("Verificar que la interface retorna un XML con el estatus de ejecucion.");
			
			boolean validationRequestTRN01 = wmCodeRequestTRN01.equals(wmCodeToValidateTRN01);
			System.out.println(validationRequestTRN01 + " - wmCode response: " + wmCodeRequestTRN01);
			testCase.addTextEvidenceCurrentStep("Folio: " + folio);
			testCase.addTextEvidenceCurrentStep("Wm_Code: " + wmCodeRequestTRN01);
			
			testCase.passStep();
			
			// Paso 4 
			addStep("Validar que se registre la transaccion de actualizacion en la tabla TPE_FR_TRANSACTION de TPEUSER.");
			
			String query02 = String.format(queryValidation02, folio);
			
			SQLResult wmCodeDbTRN01 = executeQuery(db, query02);
			
			boolean validationDb02 = wmCodeDbTRN01.isEmpty();
			if(!validationDb02) {
				
				testCase.addQueryEvidenceCurrentStep(wmCodeDbTRN01);
				
			}

			System.out.println(validationDb02);
			
			assertTrue(!validationDb02);
			
			// Paso 4
			//NO SE SABE QUE DATOS MODIFICA, POR LO QUE SOLAMENTE SE ESTA VALIDANDO LA FECHA DE ACTUALIZACION
			addStep("Validar que se actualicen los datos frecuentes del cliente en la tabla TPE_FCP_FREQ_DATA de TPEUSER.");
			
			String query03 = String.format(queryValidation03,data.get("code")); //8114975671
			System.out.println(query03);
			Date actualDate = new Date();//obtener fecha del sistema	
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy"); //formato de la fecha
			
			//try {////indexOutOfBoundsException
	
			SQLResult result = executeQuery(db, query03);

			dateDbTRN01 = result.getData(0, "ACTUALIZATION_DATE"); //obtener fecha de actualizacion en la bd  
		    //}catch(Exception e){e.printStackTrace();}
			        
			boolean validationDb03 = dateDbTRN01.equals(actualDate); //validar que sean iguales
			System.out.println(validationDb03 + " - fecha de actualizacion db: " + dateDbTRN01);

			testCase.addQueryEvidenceCurrentStep(result);
			
			testCase.validateStep(validationDb03);
			
			
         }


		
		@Override
		public void beforeTest() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String setTestDescription() {
			// TODO Auto-generated method stub
			return "Realizar la actualizacion de los datos frecuentes de clientes";
		}

		@Override
		public String setTestDesigner() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String setTestFullName() {
			return "ATC_FT_TPE_FCP_010_Upd03Tpe";
		}

	
		@Override
		public String setTestInstanceID() {
			// TODO Auto-generated method stubreturn null;
            return null;
		}



		@Override
		public String setPrerequisites() {
			// TODO Auto-generated method stub
			return null;
		}
}