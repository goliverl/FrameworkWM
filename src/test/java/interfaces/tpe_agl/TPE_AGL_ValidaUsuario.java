package interfaces.tpe_agl;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;

import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class TPE_AGL_ValidaUsuario extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_TPE_AGL_Valida_Usuario(HashMap<String, String> data) throws Exception {
		
		/*
		 * Utileria
		 * 
		 */
		
		SQLUtil dbFCTPEQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE,
				GlobalVariables.DB_PASSWORD_FCTPE);
		
		
		/*
		 * Variables
		 ******************************************************************************************************************************************/

		String ConfSchema="SELECT VER_PLANTILLA,SERVICE,SCHEMA "
				+ "FROM TPEUSER.TPE_AGL_SCHEMA "
				+ "WHERE VER_PLANTILLA='1.0' "
				+ "AND SERVICE ='USR'";
	
		
		
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
		
			addStep("Contar con una configuración en la tabla TPE_AGL_SCHEMA para el Servicio = USR y la versión de plantilla = 1.0. ");
		
			System.out.println(GlobalVariables.DB_HOST_FCTPE);
			
			System.out.println(ConfSchema);

		    SQLResult ConfSchemaExec = executeQuery(dbFCTPEQA, ConfSchema);
		       
		       boolean ConfSchemaRes = ConfSchemaExec.isEmpty();
		       if(!ConfSchemaRes) {
		    	   testCase.addQueryEvidenceCurrentStep(ConfSchemaExec);  
	
		       }
		       System.out.println(ConfSchemaRes);
		       assertFalse(ConfSchemaRes,"No se obtuvo el resultado esperado de la configuracion de schema");
			
		       
		       
	/*
	 * VALIDACION DE USUARIO ADMINISTRADOR	 
	 * CODIGO DE RETORNO 00      
	 */
		       
//			Paso 2 ******************************************************************
		      
		    addStep("Ejecutar el servicio: TPE.AGL.Pub requestUser, con usuario admin valido ");
		    
		    SeleniumUtil u;
		    PakageManagment pok;
		    u = new SeleniumUtil(new ChromeTest(), true);
			pok = new PakageManagment(u, testCase);

			 String RequestUser="http://AutoPruebasIrving:pruebas.202@%s/invoke/TPE.AGL.Pub/requestUser?Usuario=%s&Password=%s";
			 String url = String.format(RequestUser, data.get("Server"), data.get("usuarioAdmin"),data.get("passwordAdmin"));
			 System.out.println(url);
			 u.get(url);
			

			
//		Paso 3	************************		
			
			addStep("Validar que el código de respuesta sea el esperado: 00");
			String Code="00";

			String Codigo = pok.GetText("/html/body/table/tbody/tr[1]/td[2]");
			
			System.out.print("Codigo Res: "+Codigo);
			
			
			boolean validationCode= Codigo.equals(Code);

			if (validationCode!=false) {
				
				testCase.addTextEvidenceCurrentStep("El Codigo devuelto ha sido: "+ Codigo );

			}
			
			assertTrue(validationCode,"No se obtuvo el resultado esperado");
			
			

			/*
			 * Validación de usuario administrador, Formato inválido de petición
			 *  
			 * CODIGO DE RETORNO 12      
			 */
				       
//					Paso 4 ******************************************************************
				      
				    addStep("Ejecutar el servicio: TPE.AGL.Pub requestUser, con usuario admin invalido");
				    
				  
					 String urlInvalid = String.format(RequestUser, data.get("Server"), data.get("usuarioAdminInv"),data.get("passwordAdminInv"));
					 System.out.println(urlInvalid);
					 u.get(urlInvalid);
					

					
//				Paso 5	************************		
					
					addStep("Validar que el código de respuesta sea el esperado: 12");
					 Code="12";

					 Codigo = pok.GetText("/html/body/table/tbody/tr[1]/td[2]");
					
					 System.out.print("Codigo Res: "+Codigo);
						
						
						boolean validationCode12= Codigo.equals(Code);

						if (validationCode12!=false) {
							
							testCase.addTextEvidenceCurrentStep("El Codigo devuelto ha sido: "+ Codigo );

						}
						
						assertTrue(validationCode12,"No se obtuvo el resultado esperado");
					

					/*
					 * Validación de usuario no administrador, Formato válido de petición
					 *  
					 * CODIGO DE RETORNO 16      
					 */
						       
//							Paso 6 ******************************************************************
						      
						    addStep("Ejecutar el servicio: TPE.AGL.Pub requestUser, con usuario No admin ");
						    
						  
							 String urlnoadmin = String.format(RequestUser, data.get("Server"), data.get("usuarioNoAdmin"),data.get("passwordNoAdmin"));
							 System.out.println(urlnoadmin);
							 u.get(urlnoadmin);
							

							
//						Paso 7	************************		
							
							addStep("Validar que el código de respuesta sea el esperado: 16");
							 Code="16";

							 Codigo = pok.GetText("/html/body/table/tbody/tr[1]/td[2]");
							
							System.out.print("Codigo Res: "+Codigo);
					
					
					boolean validationCode16= Codigo.equals(Code);

					if (validationCode16!=false) {
						
						testCase.addTextEvidenceCurrentStep("El Codigo devuelto ha sido: "+ Codigo );

					}
					u.close();
					assertTrue(validationCode16,"No se obtuvo el resultado esperado");
					
		
			
	}
	



	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_TPE_AGL_Valida_Usuario";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Valida usuario valido.";
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
