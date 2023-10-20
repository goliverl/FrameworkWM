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

public class ATC_FT_001_SW50_ErrorConexionNodeHOM_SAPInactiva extends BaseExecution {

	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_SW50_ErrorConexionNodeHOM_SAPInactiva_test(HashMap<String, String> data) throws Exception {
		//SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST, GlobalVariables.DB_USER, GlobalVariables.DB_PASSWORD);

		String host = data.get("host");
		
/** UTILERIA *********************************************************************/
        
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		utils.sql.SQLUtil dbEbs= new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA,GlobalVariables.DB_USER_AVEBQA , GlobalVariables.DB_PASSWORD_AVEBQA);

		/**
		 * ALM
		 * Validar error en la interface cuando la conexion connNode_HOM_SAP no esta activa
		 */
		
/** VARIABLES *********************************************************************/

		// Paso 1
		String SelectS = "SELECT * FROM RTP_PROVEEDOR WHERE DESCRIPTION = 'TDE_ZAL'\r\n"
				+ "AND ISACTIVE = ";
		
				
		//paso 2
				String validarRPY2 ="SELECT * FROM WM_LOG_ERROR \r\n"
						+ "WHERE RUN_ID=[WM_LOG_RUN.RUN_ID];\r\n";
		
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
				
				addStep("Verificamos que los errores sean insertados en WM_LOG_ERROR."); 
				boolean validateStatus = status.equals(status);
				System.out.println("VALIDACION DE STATUS = S - " + validateStatus);
				assertTrue(validateStatus, "La ejecuci�n de la interfaz no fue exitosa ");

				boolean av2 = is.isEmpty();
				
				if (av2 == false) {
					testCase.addQueryEvidenceCurrentStep(is);
				} else {
					testCase.addQueryEvidenceCurrentStep(is);
				}

				System.out.println("El registro en WM_LOG_RUN esta vacio " + av2);
	}


  @Override
  public String setTestFullName() {
    return null ;
  }

  @Override
  public String setTestDescription() {
    return "Validar error en la interface cuando la conexion connNode_HOM_SAP no esta activa" ;
  }

  @Override
  public String setTestDesigner() {
    return null;
  }

  @Override
  public String setTestInstanceID() {
    return null ;
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