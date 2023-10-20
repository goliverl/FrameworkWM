package interfaces.rp06;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.Override;
import java.lang.String;
import java.sql.ResultSet;
import java.util.HashMap;
import modelo.BaseExecution;
import util.GetRequestFile;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import utils.webmethods.GetRequest;
import utils.webmethods.ReadRequest;

import org.testng.annotations.Test;
import org.w3c.dom.Document;

import integrationServer.om.PakageManagment;

public class Validarproceso_decopia_464_Portaldeproveedores extends BaseExecution {

	
	private String validarRPY;


	@Test(dataProvider = "data-provider")
	public void ATC_FT_005_rp06_Validaproceso_decopia_464_Portaldeproveedores(HashMap<String, String> data) throws Exception {
	//	SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST, GlobalVariables.DB_USER, GlobalVariables.DB_PASSWORD);

		String host = data.get("host");
		
/** UTILERIA *********************************************************************/
        
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
	//	utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS, GlobalVariables.DB_USER_EBS, GlobalVariables.DB_PASSWORD_EBS);

		SeleniumUtil u;
		PakageManagment pok;

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id;
		String status = "S";
		
/** VARIABLES *********************************************************************/

		// Paso 1
		String SelectS = "SELECT * FROM sups WHERE sup_status = 'A' AND supplier = [supplier];";
		
				
		//paso 3
				String validarRPY2 ="SELECT * FROM wm_log_run WHERE interface = 'RP06' AND status = 'S' AND start_dt >= trunc(SYSDATE) ORDER BY run_id DESC;";

		// Paso 4
				String validarRPY1 = "SELECT * FROM sups WHERE supplier = [supplier];\r\n";
				
		// Paso 5
				String validarRPY3 = "SELECT * FROM PDP_CARGA_BITACORA WHERE CONVERT(VARCHAR, FECHAFIN_EJECUCION,103) = CONVERT(VARCHAR, GETDATE(),103) ORDER BY FECHAFIN_EJECUCION DESC;\r\n";
		
/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
				
				
		/* PASO 1 *********************************************************************/	

				addStep("Validar que el Proveedor existe en RETEK");
		 
				// Primera consulta
				System.out.println(GlobalVariables.DB_HOST_Puser);
				System.out.println(SelectS);
				SQLUtil dbPos1 = null;
				@SuppressWarnings("null")
				SQLResult paso1_qry1_Result = dbPos1.executeQuery(SelectS);		
				String targetID = paso1_qry1_Result.getData(0, "TARGET_ID");
				System.out.println("SAP_INBOUND_DOCS.TARGET_ID= " + targetID); // imprime la primera

				boolean paso1_qry1_valida = paso1_qry1_Result.isEmpty(); // checa que el string contenga datos

				if (!paso1_qry1_valida) {
					testCase.addQueryEvidenceCurrentStep(paso1_qry1_Result); // Si no esta vacio, lo agrega a la evidencia
				}

				System.out.println(paso1_qry1_valida);
				assertFalse("No se encontraron registros a procesar ", paso1_qry1_valida); // Si esta vacio, imprime mensaje
				
		
		/* PASO 2 *********************************************************************/	

				addStep("Ejecutar el servicio RP06.Pub:run. La interfaz será invocada con el job runRP06 desde Ctrl-M.");
				
				u = new SeleniumUtil(new ChromeTest(), true);
				pok = new PakageManagment(u, testCase);

				System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
				String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
				System.out.println(contra);
				u.get(contra);
		       
				String dateExecution = pok.runIntefaceWmOneButton(data.get("interface"), data.get("servicio"));
				System.out.println("Respuesta dateExecution " + dateExecution);
				String tdcIntegrationServerFormat = null;
				System.out.println(tdcIntegrationServerFormat);
				SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
				run_id = is.getData(0, "RUN_ID");
				String status1 = is.getData(0, "STATUS");
				System.out.println("RUN_ID = " + run_id + "\t Status: " + status1 );

				boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R

				while (valuesStatus) {			
					status1 = is.getData(0, "STATUS");
					run_id = is.getData(0, "RUN_ID");
					valuesStatus = status1.equals(searchedStatus);
					u.hardWait(2);			
				}
		
		/* PASO 3 *********************************************************************/

				addStep("Validar el registro de ejecución de la interfaz en la base de datos del WMLOG.\r\n.");

				boolean validateStatus = status.equals(status1);
				System.out.println("VALIDACION DE STATUS = S - " + validateStatus);
				assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa ");

				boolean av2 = is.isEmpty();
				
				if (av2 == false) {
					testCase.addQueryEvidenceCurrentStep(is);
				} else {
					testCase.addQueryEvidenceCurrentStep(is);
				}

				System.out.println("El registro en WM_LOG_RUN esta vacio " + av2);
			
		
		
		// Paso 4******************************

            addStep("Validar la inserción del proveedor en la base de datos del Portal de Proveedores.");
            
            String paso4_format = String.format(validarRPY,targetID);
            System.out.println(paso4_format);
            SQLResult paso4_Result = dbPos.executeQuery(paso4_format);

            boolean paso4_valida = paso4_Result.isEmpty(); // checa que el string contenga datos

             if (!paso4_valida) {
	        testCase.addQueryEvidenceCurrentStep(paso4_Result); // Si no esta vacio, lo agrega a la evidencia
}

               System.out.println(paso4_valida);
               assertFalse("No se encontraron registros ", paso4_valida); // Si esta vacio, imprime mensaje


	
	     /* PASO 5 ********************************************************/

              addStep("Validar la inserción de un nuevo registro en la bitácora de la base de datos de INFORCEDIS.");
              String paso5_format = String.format(validarRPY,targetID);
              System.out.println(paso5_format);
          SQLResult paso5_Result = dbPos.executeQuery(paso5_format);

           boolean paso5_valida = paso5_Result.isEmpty(); // checa que el string contenga datos

            if (!paso5_valida) {
          testCase.addQueryEvidenceCurrentStep(paso5_Result); // Si no esta vacio, lo agrega a la evidencia
}

                System.out.println(paso5_valida);
                assertFalse("No se encontraron registros ", paso5_valida); // Si esta vacio, imprime mensaje
   
   }


  @Override
  public String setTestFullName() {
    return "ATC_FT_005_rp06_Validaproceso_decopia_464_Portaldeproveedores" ;
  }

  @Override
  public String setTestDescription() {
    return "Construido. Enviar solicitud de folio para una transacción de pago" ;
  }

  @Override
  public String setTestDesigner() {
    return "tbd" ;
  }

  @Override
  public String setTestInstanceID() {
    return "-1" ;
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