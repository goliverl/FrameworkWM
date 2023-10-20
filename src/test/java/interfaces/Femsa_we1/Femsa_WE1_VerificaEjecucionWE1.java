package interfaces.Femsa_we1;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.Override;
import java.lang.String;
import java.sql.ResultSet;
import java.util.HashMap;
import modelo.BaseExecution;
import om.TPE_TSF;
import util.GetRequestFile;
import util.GlobalVariables;
import util.RequestUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import utils.webmethods.GetRequest;
import utils.webmethods.ReadRequest;

import org.openqa.selenium.By;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import integrationServer.om.PakageManagment;

public class Femsa_WE1_VerificaEjecucionWE1 extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_WE1_VerificaEjecucionWE1(HashMap<String, String> data) throws Exception {

		/**
		 * UTILERIA
		 *********************************************************************/

		utils.sql.SQLUtil db = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA, GlobalVariables.DB_USER_FCTDCQA,GlobalVariables.DB_PASSWORD_FCTDCQA);
//		SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST, GlobalVariables.DB_USER, GlobalVariables.DB_PASSWORD);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,GlobalVariables.DB_PASSWORD_Puser);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);

		String host = data.get("host");

		SeleniumUtil u;
		PakageManagment pok;

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id;
		String status = "S";

		/**
		 * ALM
		 * Verificar ejecuciOn de la interfaz WE1 para la 10TIJ
		 */
		
		/**
		 * VARIABLES
		 *********************************************************************/

	
		String tdcIntegrationServerFormat = "	select * from "
				+ "(SELECT Tbl.run_id,interface, start_dt, end_dt, status, server " + 
				"FROM WMLOG.WM_LOG_RUN Tbl "
				+ "WHERE INTERFACE = 'WE1' "
				+ "AND STATUS = 'S' " + 
				"ORDER BY START_DT DESC) "
				+ "where rownum <=1";

		// paso 2
		String validarRPY = "SELECT * FROM SUBPROPERTY "
				+ "		WHERE PROPERTYNUM ='"+data.get("PROPERTYNUM")+"' ";

		String validarRPY2 = "SELECT * FROM DIRSTRUCT "
				+ "WHERE DIRID='%s'";

		String tdcQueryWMLOG=" SELECT run_id,interface, start_dt, end_dt, status, server"
				+ " FROM WMLOG.WM_LOG_RUN "
				+ "WHERE "
				+ "INTERFACE='WE1' "
				+ "AND STATUS='S' ORDER BY start_dt DESC";
		
		String validDoc="SELECT * FROM IMAGEDATA WHERE SUBSTR(FILEPATH,0,5)='"+data.get("plaza")+"' "
				+ "AND IMAGETYPE = 'HTML' "
				+ "AND TRUNC(DATA19)=TRUNC(SYSDATE)";
	
		String validDoc2="	SELECT * FROM INVENTORY "
				+ "WHERE IMGDATAID='%s' "
				+ "AND TRUNC(TIMESTAMP)=TRUNC(SYSDATE)";
		

		/**
		 * PASOS DEL CASO DE PRUEBA
		 *********************************************************************/

		/* PASO 1 *********************************************************************/

		addStep("Validar que exista el archivo de configuraci�n CONFIG_WE1.xml y que se tenga configurado los siguientes valores: FTP y URL_PORTAL.\r\n");

		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		System.out.println(contra);
		u.get(contra);
       
		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interface"), data.get("serv1"));
		System.out.println("Respuesta dateExecution " + dateExecution);
		String DIRID= u.getText(By.xpath("/html/body/table/tbody/tr[2]/td[2]/table/tbody/tr[3]/td[2]/table/tbody/tr[3]/td[2]"));
		System.out.println(DIRID);		
				
		
		/* PASO 2 *********************************************************************/

		addStep("Validar que existan los datos de los parametros en las tablas: SUBPROPERTY y DIRSTRUCT");
		
		// Primera consulta
				System.out.println(GlobalVariables.DB_HOST);
				System.out.println(validarRPY);
				SQLResult paso1_qry1_Result = db.executeQuery(validarRPY);		
			
				
				boolean paso1_qry1_valida = paso1_qry1_Result.isEmpty(); // checa que el string contenga datos

				if (!paso1_qry1_valida) {
					testCase.addQueryEvidenceCurrentStep(paso1_qry1_Result); // Si no esta vacio, lo agrega a la evidencia
				}

				System.out.println(paso1_qry1_valida);
				
				// Segunda consulta
				System.out.println("Segunda consulta");
				System.out.println(GlobalVariables.DB_HOST);
				String Cons2Format= String.format(validarRPY2,DIRID);
				System.out.println(Cons2Format);
				SQLResult paso1_qry2_Result = db.executeQuery(Cons2Format);		
			
				
				boolean paso1_qry2_valida = paso1_qry2_Result.isEmpty(); // checa que el string contenga datos

				if (!paso1_qry2_valida) {
					testCase.addQueryEvidenceCurrentStep(paso1_qry2_Result);  // Si no esta vacio, lo agrega a la evidencia
				}

				
				System.out.println(paso1_qry2_valida);
				
				
				assertFalse("No se encontraron registros a procesar ", paso1_qry1_valida); // Si esta vacio, imprime mensaje
				assertFalse("No se encontraron registros a procesar ", paso1_qry2_valida);

		/* PASO 3 *********************************************************************/

		addStep("Ejecutar  el servicio WE1.Pub:run");

		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String contra1 = "http://" + user + ":" + ps + "@" + server + ":5555";
		System.out.println(contra1);
		u.get(contra1);

		String dateExecution1 = pok.runIntefaceWmOneButton10(data.get("interface"), data.get("servicio"));
		System.out.println("Respuesta dateExecution " + dateExecution1);
		System.out.println(tdcIntegrationServerFormat);
		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");
		System.out.println("RUN_ID = " + run_id + "\t Status: " + status1);

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R

		while (valuesStatus) {
			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);
			u.hardWait(2);
		}

		// Paso 4******************************

		addStep("Validar registro de la ejecuci�n de la interfaz en las tablas de WMLOG.");
		
		
		System.out.println(tdcQueryWMLOG);
		SQLResult connectionResult = executeQuery(dbLog, tdcQueryWMLOG);

		String run_id_ = "";

		boolean connection = connectionResult.isEmpty();

		if (!connection) {
			run_id_ = connectionResult.getData(0, "RUN_ID");
			testCase.addQueryEvidenceCurrentStep(connectionResult);
		}

		System.out.println(connection);

		assertFalse(connection, "La tabla no contiene informaci�n.");
	
	// los siguentes dos pasos quedan pendientes por informacion

	/* PASO 5 ********************************************************/

	/* PASO 6 *******************************************************/
		
		// Paso 5
			
			addStep("Validar registro del documento en la BD de E-Paper.");
				
				// Primera consulta
						System.out.println(GlobalVariables.DB_HOST);
						System.out.println(validDoc);
						SQLResult qry1_Result = db.executeQuery(validDoc);		
						String IMGDATAID="";
						
						boolean qry1_valida = qry1_Result.isEmpty(); // checa que el string contenga datos

						if (!qry1_valida) {
							IMGDATAID = qry1_Result.getData(0, "IMGDATAID");
							System.out.println("IMGDATAID: "+IMGDATAID);
							testCase.addQueryEvidenceCurrentStep(qry1_Result); // Si no esta vacio, lo agrega a la evidencia
						}

						System.out.println(qry1_valida);
						
						// Segunda consulta
						System.out.println("Segunda consulta");
						System.out.println(GlobalVariables.DB_HOST);
						String qry2Format= String.format(validDoc2,IMGDATAID);
						System.out.println(qry2Format);
						SQLResult qry2_Result = db.executeQuery(qry2Format);		
					
						
						boolean qry2_valida = qry2_Result.isEmpty(); // checa que el string contenga datos

						if (!qry2_valida) {
							testCase.addQueryEvidenceCurrentStep(qry2_Result);  // Si no esta vacio, lo agrega a la evidencia
						}

						
						System.out.println(qry2_valida);
						
						
						assertFalse("No se encontraron registros a procesar ", qry1_valida); // Si esta vacio, imprime mensaje
						assertFalse("No se encontraron registros a procesar ", qry2_valida);
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_001_WE1_VerificaEjecucionWE1";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Verificar ejecuci�n de la interfaz WE1";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
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
