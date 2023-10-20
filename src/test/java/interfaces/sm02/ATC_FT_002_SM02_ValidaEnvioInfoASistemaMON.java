package interfaces.sm02;

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

public class ATC_FT_002_SM02_ValidaEnvioInfoASistemaMON extends BaseExecution {

	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_SM02_ValidaEnvioInfoASistemaMON_test(HashMap<String, String> data) throws Exception {
		//SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST, GlobalVariables.DB_USER, GlobalVariables.DB_PASSWORD);

		String host = data.get("host");
		
/** UTILERIA *********************************************************************/
        
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		utils.sql.SQLUtil dbEbs= new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA,GlobalVariables.DB_USER_AVEBQA , GlobalVariables.DB_PASSWORD_AVEBQA);

		/**
		 * ALM
		 * Validar que la informacion Plaza Tax se envie correctamente al sistema MOF
		 */
		
		
/** VARIABLES *********************************************************************/

		// Paso 1
		String SelectS = " SELECT * FROM WMLOG.WM_LOG_RUN \r\n"
				+ "WHERE INTERFACE = 'SM02' \r\n"
				+ "AND STATUS = 'S' \r\n"
				+ "AND TRUNC (START_DT) = TRUNC (SYSDATE) \r\n"
				+ "ORDER BY RUN_ID DESC "
				+ "and rownum= 1";
		
		//paso 2
				String validarRPY = "SELECT * FROM XXMOF.MOF_NOMINA_EMPLEADO \r\n"
						+ "WHERE TRUNC (CREATION_DATE) = TRUNC (SYSDATE) \r\n"
						+ "AND IDPERIODO = [XXMOF.CT_PERIODOS.PERIODOID] \r\n"
						+ "AND IDSEMANA = [XXMOF.CT_SEMANAS.SEMANA_ID];\r\n";
				
		 // Paso 3
				String validarF1 = "SELECT * FROM pos_rtp_trans \r\n"
						+ "WHERE lrt_id = [POS_RTP_LOTES.LTR_ID] \r\n"
						+ "AND creation_date > TRUNC(sysdate-3) \r\n"
						+ "AND wm_status = 'PROCESADO'";
		
		
/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
				
		
		/* PASO 1 *********************************************************************/	

		addStep("Validar que exista el archivo de configuraci�n CONFIG_WE1.xml y que se tenga configurado los siguientes valores: FTP y URL_PORTAL.\r\n");
 
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

		addStep("Validar que existan los datos de los parametros en las tablas: SUBPROPERTY y DIRSTRUCT\r\n.");
        
		String consultafor = "", thread_id, thread_status;
		SQLResult resultfor;
		System.out.println(GlobalVariables.DB_HOST_EBS);		
		String docnum = "";
		String id = "";

		boolean registro = false;
		
		int i = 0;
		do  {
			String t_id = paso1_qry1_Result.getData(i, "TARGET_ID");
			String consultaGL = null;
			consultafor = String.format(consultaGL, t_id); //Consulta si hay resultados en GL en status diferente a NEW del target id
			if (i==0) System.out.println(consultafor);
			SQLUtil dbEbs1 = null;
			resultfor = executeQuery(dbEbs1, consultafor);			
			boolean foav = resultfor.isEmpty(); 	//Si no esta vacio, se pasan los datos del thread a una nueva variable para no ser sobreescritas en el ciclo y se activa la bandera.
			System.out.println("ESTA VACIO EL reference 6 (target id): " + t_id + " en la tabla GL? "+ foav);

			if (!foav) {
				targetID = paso1_qry1_Result.getData(i, "TARGET_ID");
				docnum = paso1_qry1_Result.getData(i, "DOCNUM");
				id = paso1_qry1_Result.getData(i, "ID");
				registro = true;
			}			
			i++;
		} while ((i < paso1_qry1_Result.getRowCount() && registro == false));
		
		System.out.println("Hay dato en GL? " + registro + " es el Target id: " + targetID);

		testCase.addQueryEvidenceCurrentStep(resultfor);

		assertTrue(registro, "No se encontraron datos validos en EBS en la tabla GL_INTERFACE");
		
        /* PASO 3 *********************************************************************/	
		
		addStep("Ejecutar la interfaz por medio del servicio PE5.Pub:run.");
		
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
		
		// Paso 4******************************

		addStep("Validar registro de la ejecuci�n de la interfaz en las tablas de WMLOG...");

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
		
		
	 }
		
	//los siguentes dos pasos quedan pendientes por informacion
	
	     /* PASO 5 ********************************************************/
					
		/* PASO 6 *******************************************************/


  @Override
  public String setTestFullName() {
    return null;
  }

  @Override
  public String setTestDescription() {
    return "Validar que la informacion Plaza Tax se envie correctamente al sistema MOF";
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