package interfaces.Eo05;

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

public class Respaldarcomisiones_Banamex_Chihuahua extends BaseExecution {

	
	private Object targetID;


	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_Eo05_Respaldarcomisiones_Banamex_Chihuahua(HashMap<String, String> data) throws Exception {
	//	SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST, GlobalVariables.DB_USER, GlobalVariables.DB_PASSWORD);

		String host = data.get("host");
		
/** UTILERIA *********************************************************************/
        
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
	//	utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS, GlobalVariables.DB_USER_EBS, GlobalVariables.DB_PASSWORD_EBS);

		/**
		 * ALM
		 * Respaldar comisiones de monedero de Banamex, plaza Chihuaha
		 */
		
		SeleniumUtil u;
		PakageManagment pok;

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id;
		String status = "S";
		
/** VARIABLES *********************************************************************/

		
		//paso 2
				String validarRPY = "SELECT * FROM wmlog.wm_log_run WHERE interface = 'EO05' AND TRUNC (start_dt) = TRUNC (SYSDATE) AND status = 'S' ORDER BY 3 DESC;\r\n";
				
		//paso 3
				String validarRPY2 ="SELECT * FROM wmuser.wm_eo05_archivos WHERE nombre_archivo = [fileName] AND creation_date = TRUNC (SYSDATE);"
						+ "";

		// Paso 4
				String validarRPY1 = "SELECT * FROM xxfc.xxfc_comisiones_monedero WHERE cr_plaza = '10AGC' AND TRUNC (creation_date) = TRUNC (SYSDATE);";
				

		
/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
				
		
		/* PASO 1 *********************************************************************/	

				addStep("Ejecutar la interface mediante la URL http://host:5555/invoke/EO05.Pub/run\r\n.");
				
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

				addStep("Validar que la interface se ejecut� correctamente en wm_log_run de WMLOG.");

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

            addStep("Validar que se insert� la referencia del nombre del archivo procesado en la tabla wm_eo05_archivos de WMUSER.\r\n.");
            
            String paso3_format = String.format(validarRPY,targetID);
            System.out.println(paso3_format);
            SQLResult paso3_Result = dbPos.executeQuery(paso3_format);

            boolean paso3_valida = paso3_Result.isEmpty(); // checa que el string contenga datos

             if (!paso3_valida) {
	        testCase.addQueryEvidenceCurrentStep(paso3_Result); // Si no esta vacio, lo agrega a la evidencia
}

               System.out.println(paso3_valida);
               assertFalse("No se encontraron registros ", paso3_valida); // Si esta vacio, imprime mensaje


	
	     /* PASO 4********************************************************/

              addStep("Validar que las l�neas de detalle del archivo fueron insertadas en xxfc_comisiones_monedero de ORAFIN.\r\n.");
              String paso5_format = String.format(validarRPY,targetID);
              System.out.println(paso5_format);
          SQLResult paso4_Result = dbPos.executeQuery(paso5_format);

           boolean paso4_valida = paso4_Result.isEmpty(); // checa que el string contenga datos

            if (!paso4_valida) {
          testCase.addQueryEvidenceCurrentStep(paso4_Result); // Si no esta vacio, lo agrega a la evidencia
}

                System.out.println(paso4_valida);
                assertFalse("No se encontraron registros ", paso4_valida); // Si esta vacio, imprime mensaje
   
   }

	// paso 5 Verificar que el archivo haya sido eliminado del FTP origen.
	// paso 6 Por �ltimo validar la recepci�n del envi� del correo que se hizo como reporte al grupo de ORAFIN

  @Override
  public String setTestFullName() {
    return "ATC_FT_002_Eo05_Respaldarcomisiones_Banamex_Chihuahua" ;
  }

  @Override
  public String setTestDescription() {
    return "Construida. Enviar solicitud de folio para una transacci�n de pago" ;
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