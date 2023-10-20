package interfaces.SW50;

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

public class ATC_FT_003_SW50_ValidaEnvioDocPRYaSAP extends BaseExecution {

	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_SW50_ValidaEnvioDocPRYaSAP_test(HashMap<String, String> data) throws Exception {
		//SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST, GlobalVariables.DB_USER, GlobalVariables.DB_PASSWORD);

		String host = data.get("host");
		
/** UTILERIA *********************************************************************/
        
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		

		/**
		 * ALM
		 * Validar envio de multiples documentos de tipo PRY a SAP con exito y con error
		 */
		
		
/** VARIABLES *********************************************************************/

		// Paso 1
		String SelectS = "SELECT * FROM RTP_PROVEEDOR WHERE DESCRIPTION = 'TDE_ZAL'\r\n"
				+ "AND ISACTIVE = ";
		
		//paso 2
				String validarRPY = "SELECT * FROM wm_log_run \r\n"
						+ "WHERE interface = 'SW50' \r\n"
						+ "and start_dt >= trunc(sysdate) \r\n"
						+ "ORDER BY start_dt DESC;\r\n";


				// Paso 3
				String validarRPY1 = "SELECT * FROM WM_LOG_THREAD \r\n"
						+ "WHERE PARENT_ID = [WM_LOG_RUN.RUN_ID]";
				
				
				
				//paso 4
				String validarRPY2 ="SELECT * FROM sap_inbound_docs \r\n"
						+ "WHERE status = 'L' \r\n"
						+ "AND doc_type = 'PYR' \r\n"
						+ "AND create_date >= TRUNC(SYSDATE);\r\n";
				
				//paso 5
				String validarRPY11 = "SELECT * FROM sap_pyr_header\r\n"
						+ "WHERE sid_id = [sap_inbound_docs.id]";
				
				// Paso 6
				String validarRPY3 = "SELECT * FROM sap_pyr_detl \r\n"
						+ "WHERE sid_id = [sap_inbound_docs.id] \r\n"
						+ "AND doc_type = 'PYR' \r\n"
						+ "AND status_row = 'L'";
		
/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
				
		
		/* PASO 1 *********************************************************************/	

				addStep("Ejecutar el servicio: SW50.pub:runSendPayrollFromSAPtoOracle.\r\n.");
				
				SeleniumUtil u;
				PakageManagment pok;

				String user = data.get("user");
				String ps = PasswordUtil.decryptPassword(data.get("ps"));
				String server = data.get("server");
				String searchedStatus = "R";
				String run_id;
				String status = "S";
				
				
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
		
		/* PASO 2 *********************************************************************/

				addStep("Validar que  la ejecuci�n de la interface se registra en la tabla wm_log_run.\r\n");

				boolean validateStatus = status.equals(status1);
				System.out.println("VALIDACION DE STATUS = S - " + validateStatus);
				assertTrue(validateStatus, "La ejecuci�n de la interfaz no fue exitosa ");

				boolean av2 = is.isEmpty();
				
				if (av2 == false) {
					testCase.addQueryEvidenceCurrentStep(is);
				} else {
					testCase.addQueryEvidenceCurrentStep(is);
				}

				System.out.println("El registro en WM_LOG_RUN esta vacio " + av2);
				
				
	 	

		
		
		// Paso 3******************************

            addStep("Validar que se generaron lo THREADS correspondientes a los documentos procesados por la interfaz..");
            
            Object targetID = null;
			String paso4_format = String.format(validarRPY,targetID);
            System.out.println(paso4_format);
            SQLResult paso4_Result = dbPos.executeQuery(paso4_format);

            boolean paso4_valida = paso4_Result.isEmpty(); // checa que el string contenga datos

             if (!paso4_valida) {
	        testCase.addQueryEvidenceCurrentStep(paso4_Result); // Si no esta vacio, lo agrega a la evidencia
}

               System.out.println(paso4_valida);
               assertFalse("No se encontraron registros ", paso4_valida); // Si esta vacio, imprime mensaje
               
               
               
               /*PASO 4 ******************************/
               
				addStep("Validar que los errores se registraron correctamente en la tabla WM_LOG_ERROR.");

				boolean validateStatus1 = status.equals(status1);
				System.out.println("VALIDACION DE STATUS = S - " + validateStatus1);
				assertTrue(validateStatus1, "La ejecuci�n de la interfaz no fue exitosa ");

				boolean av21 = is.isEmpty();
				
				if (av21 == false) {
					testCase.addQueryEvidenceCurrentStep(is);
				} else {
					testCase.addQueryEvidenceCurrentStep(is);
				}

				System.out.println("El registro en WM_LOG_RUN esta vacio " + av21);
				
				
		        /* PASO 4 *********************************************************************/
				
                addStep("Validar el registro del documento con GSBER OC48 se insert� en la tabla sap_inbound_docs.");
                // Primer consulta
	              System.out.println(GlobalVariables.DB_HOST_Puser);
	              System.out.println(SelectS);
	              SQLResult paso1_qry1_Result = dbPos.executeQuery(SelectS);		
	              String targetID1 = paso1_qry1_Result.getData(0, "TARGET_ID");
	              System.out.println("SAP_INBOUND_DOCS.TARGET_ID= " + targetID1); // imprime la primera

	           boolean paso1_qry1_valida = paso1_qry1_Result.isEmpty(); // checa que el string contenga datos

	         if (!paso1_qry1_valida) {
		      testCase.addQueryEvidenceCurrentStep(paso1_qry1_Result); // Si no esta vacio, lo agrega a la evidencia
	}

	           System.out.println(paso1_qry1_valida);
	           assertFalse("No se encontraron registros a procesar ", paso1_qry1_valida); // Si esta vacio, imprime mensaje
	           
	           
	        // Paso 5******************************

	            addStep("Validar la inserci�n de los datos del header en la tabla sap_pyr_header..");
	            
	            String paso5_format = String.format(validarRPY,targetID1);
	            System.out.println(paso5_format);
	            SQLResult paso5_Result = dbPos.executeQuery(paso5_format);

	            boolean paso5_valida = paso5_Result.isEmpty(); // checa que el string contenga datos

	             if (!paso5_valida) {
		        testCase.addQueryEvidenceCurrentStep(paso4_Result); // Si no esta vacio, lo agrega a la evidencia
	}

	               System.out.println(paso5_valida);
	               assertFalse("No se encontraron registros ", paso5_valida); // Si esta vacio, imprime mensaje
	

	
	     /* PASO 6 ********************************************************/

              addStep("Validar la inserci�n de los registros de detalle en la tabla sap_pyr_detl..");
              String paso6_format = String.format(validarRPY,targetID1);
              System.out.println(paso6_format);
          SQLResult paso6_Result = dbPos.executeQuery(paso6_format);

           boolean paso6_valida = paso6_Result.isEmpty(); // checa que el string contenga datos

            if (!paso6_valida) {
          testCase.addQueryEvidenceCurrentStep(paso6_Result); // Si no esta vacio, lo agrega a la evidencia
}

                System.out.println(paso6_valida);
                assertFalse("No se encontraron registros ", paso5_valida); // Si esta vacio, imprime mensaje
   
   }


  @Override
  public String setTestFullName() {
    return null ;
  }

  @Override
  public String setTestDescription() {
    return "Validar envio de multiples documentos de tipo PRY a SAP con exito y con error" ;
  }

  @Override
  public String setTestDesigner() {
    return null;
  }

  @Override
  public String setTestInstanceID() {
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