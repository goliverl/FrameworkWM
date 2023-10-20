package interfaces.pe7;

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

public class pe7Serviciosdetesoreria extends BaseExecution {

	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PE7_pe7Serviciosdetesoreria(HashMap<String, String> data) throws Exception {
		
		//SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST, GlobalVariables.DB_USER, GlobalVariables.DB_PASSWORD);

		//String host = data.get("host");
		
/** UTILERIA *********************************************************************/
        
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS, GlobalVariables.DB_USER_EBS, GlobalVariables.DB_PASSWORD_EBS);

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
		String SelectS = "SELECT * FROM TPE_OLS_GDF_PAYMENT_METHODS\r\n"
				+ "WHERE LN_TPE_OLS_GDF_PAYMENT_ID_PK = 2;\r\n";
		
		//paso 2
				String validarRPY = "SELECT CODE, VALUE1 FROM TPE_FR_CONFIG\r\n"
						+ "WHERE APPLICATION = 'PE7'\r\n"
						+ "AND ENTITY = 'GDF' \r\n"
						+ "AND OPERATION = 'CONF' \r\n"
						+ "AND CATEGORY = 'CONFIG' \r\n"
						+ "AND CODE IN ('FILESYSTEM', 'SENDFTP', 'FTPPATH', 'FTPIP', 'FTPPORT', 'FTPUSER', 'FNAMEFTP', 'FTPPASS', 'PRYMARYKEY', 'USERKNOWNHOSTFILE');\r\n"
						+ "";
		//paso 3
				String validarRPY1 = "SELECT CODE, VALUE1 FROM TPE_FR_CONFIG\r\n"
						+ "WHERE APPLICATION = 'PE7'\r\n"
						+ "AND ENTITY = 'GDF' \r\n"
						+ "AND OPERATION = 'CONF' \r\n"
						+ "AND CATEGORY = 'CONFIG' \r\n"
						+ "AND CODE IN ('FILESYSTEM', 'EMAILFILE1', 'HOSTFILE1', 'SUBJECTFILE1', 'BODYFILE1');\r\n"
						+ "";
				
		//paso 4
				String validarRPY2 = "SELECT CODE, VALUE1 FROM TPE_FR_CONFIG\r\n"
						+ "WHERE APPLICATION = 'PE7'\r\n"
						+ "AND ENTITY = 'GDF' \r\n"
						+ "AND OPERATION = 'CONF' \r\n"
						+ "AND CATEGORY = 'CONFIG' \r\n"
						+ "AND CODE IN ('FILESYSTEM', 'EMAILFILE2', 'HOSTFILE1', 'SUBJECTFILE2', 'BODYFILE2')";

		//paso 7
				String tdcQueryPorProcesar = "SELECT * FROM WM_LOG_RUN\n"
						+ "WHERE INTERFACE = 'PE7'\n"
						+ "AND STATUS = 'S'\n"
						+ "AND TRUNC(START_DT) = TRUNC(SYSDATE)\n"
						+ "ORDER BY START_DT DESC;\n";
		
		
/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
				
		
		/* PASO 1 *********************************************************************/	

		addStep("Verificar que existan polizas a procesar para OXXO en la BD SAPUSER.");
 
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

		addStep("Verificar que la información se haya insertado correctamente en la tabla gl_interface BD GL de ORAFINCOL.");
        
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

		addStep("Verificar que la información se haya insertado correctamente en la tabla gl_interface BD GL de ORAFINCOL.");
        
		String consultafor1 = "", thread_id1, thread_status1;
		SQLResult resultfor1;
		System.out.println(GlobalVariables.DB_HOST_EBS);		
		String docnum1 = "";
		String id1 = "";

		boolean registro1 = false;
		
		int i1 = 0;
		do  {
			String t_id = paso1_qry1_Result.getData(i1, "TARGET_ID");
			String consultaGL = null;
			consultafor1 = String.format(consultaGL, t_id); //Consulta si hay resultados en GL en status diferente a NEW del target id
			if (i1==0) System.out.println(consultafor1);
			SQLUtil dbEbs1 = null;
			resultfor1 = executeQuery(dbEbs1, consultafor1);			
			boolean foav = resultfor1.isEmpty(); 	//Si no esta vacio, se pasan los datos del thread a una nueva variable para no ser sobreescritas en el ciclo y se activa la bandera.
			System.out.println("ESTA VACIO EL reference 6 (target id): " + t_id + " en la tabla GL? "+ foav);

			if (!foav) {
				targetID = paso1_qry1_Result.getData(i1, "TARGET_ID");
				docnum1 = paso1_qry1_Result.getData(i1, "DOCNUM");
				id1 = paso1_qry1_Result.getData(i1, "ID");
				registro1 = true;
			}			
			i1++;
		} while ((i1 < paso1_qry1_Result.getRowCount() && registro1 == false));
		
		System.out.println("Hay dato en GL? " + registro1 + " es el Target id: " + targetID);

		testCase.addQueryEvidenceCurrentStep(resultfor1);

		assertTrue(registro1, "No se encontraron datos validos en EBS en la tabla GL_INTERFACE");

		
		/* PASO 4 *********************************************************************/

		addStep("Verificar que la información se haya insertado correctamente en la tabla gl_interface BD GL de ORAFINCOL.");
        
		String consultafor2 = "", thread_id2, thread_status2;
		SQLResult resultfor2;
		System.out.println(GlobalVariables.DB_HOST_EBS);		
		String docnum2 = "";
		String id2 = "";

		boolean registro2 = false;
		
		int i2 = 0;
		do  {
			String t_id = paso1_qry1_Result.getData(i, "TARGET_ID");
			String consultaGL = null;
			consultafor2 = String.format(consultaGL, t_id); //Consulta si hay resultados en GL en status diferente a NEW del target id
			if (i==0) System.out.println(consultafor2);
			SQLUtil dbEbs1 = null;
			resultfor = executeQuery(dbEbs1, consultafor2);			
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
		
		
		/* PASO 6 *********************************************************************/	
		
		addStep("Ejecutar el servicio SO1.Pub:run.");
		
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

		// Paso 7 ******************************

				addStep("Confirmar que se insertaron correctamente los registros de los tickets en la "
						+ "tablas TPE_FCP_ACTIVITY y TPE_FCP_ITEM de la BD TPEUSER.");

				System.out.println(GlobalVariables.DB_HOST_FCTPE);
				System.out.println(tdcQueryPorProcesar);

				SQLUtil dbTPE = null;
				SQLResult procesarResult = executeQuery(dbTPE, tdcQueryPorProcesar);
				
				boolean procesar = procesarResult.isEmpty();

				if (!procesar) {

					testCase.addQueryEvidenceCurrentStep(procesarResult);

				}

				System.out.println(procesar);

				assertFalse(procesar, "No se obtiene informacion de la consulta");

		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S

		if (successRun) {

			String consultaError1 = null;
			String error = String.format(consultaError1, run_id);
			String consultaError2 = null;
			String error1 = String.format(consultaError2, run_id);
			String consultaError3 = null;
			String error2 = String.format(consultaError3, run_id);

			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();
			
			if (!emptyError) {
				testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
				testCase.addQueryEvidenceCurrentStep(errorr);
			}

			SQLResult errorIS = dbLog.executeQuery(error1);
			boolean emptyError1 = errorIS.isEmpty();

			if (!emptyError1) {
				testCase.addQueryEvidenceCurrentStep(errorIS);
			}

			SQLResult errorIS2 = dbLog.executeQuery(error2);
			boolean emptyError2 = errorIS2.isEmpty();

			if (!emptyError2) {
				testCase.addQueryEvidenceCurrentStep(errorIS2);
			}
		}

		}
		
	//paso 8 y paso 9 pendiente porque no hay información

  @Override
  public String setTestFullName() {
    return "ATC_FT_001_PE7_pe7Serviciosdetesoreria";
  }

  @Override
  public String setTestDescription() {
    return "Construida. FEMSA_PE7_pe7Serviciosdetesoreria " ;
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