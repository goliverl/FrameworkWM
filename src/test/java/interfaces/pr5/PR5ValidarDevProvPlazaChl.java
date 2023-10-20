package interfaces.pr5;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.FTPUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class PR5ValidarDevProvPlazaChl extends BaseExecution {

	@Test(dataProvider = "data-provider")

	public void ATC_FT_PR5_002_Validar_Dev_Prov_Plaza_Chl(HashMap<String, String> data) throws Exception {

	
/* Utilerías *********************************************************************/
		
	
		utils.sql.SQLUtil dbLogCh = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_LogChile,GlobalVariables.DB_USER_LogChile, GlobalVariables.DB_PASSWORD_LogChile);
		utils.sql.SQLUtil dbPosCh = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_PosUserChile,GlobalVariables.DB_USER_PosUserChile,GlobalVariables.DB_PASSWORD_PosUserChile);
	
/**
 * Variables ******************************************************************************************
* pr5
* 
*/
		String tdcPaso1 = " Select * from  WMUSER.WM_FTP_CONNECTIONS \r\n "
				+ " WHERE  ftp_conn_id = 'PR50POS' \r\n ";			
		
		String tdcPaso2 = " SELECT DISTINCT a.* \r\n"
				+ " FROM  POSUSER.POS_INBOUND_DOCS a \r\n"
				+ " INNER JOIN POS_RTV b ON b.PID_ID = a.ID \r\n"
				+ " INNER JOIN POS_RTV_DETL c ON b.PID_ID = c.PID_ID \r\n"
				+ " WHERE a.DOC_TYPE = 'RTV' \r\n"
				+ " AND a.STATUS = 'I' \r\n"
				+ " AND a.PARTITION_DATE > SYSDATE - 7 \r\n"
				+ " AND SUBSTR(a.PV_DOC_NAME,4,5) = '"+ data.get("plaza") +"'\n";
				
		String tdcPaso4 = "SELECT * FROM WMLOG.WM_LOG_RUN \r\n"
				+ " WHERE INTERFACE = 'PR5CL' \r\n"
				+ " AND STATUS = 'S' \r\n"
				+ " AND TRUNC(START_DT) = TRUNC(SYSDATE) \r\n"
				+ " ORDER BY START_DT DESC \r\n";
						
		String tdcPaso5 = " SELECT * FROM WMLOG.WM_LOG_THREAD \r\n"
				+ " WHERE PARENT_ID =  %s \r\n";
				 
		String tdcPaso7 = " SELECT * FROM POSUSER.POS_INBOUND_DOCS \r\n"
				+ " WHERE DOC_TYPE = 'RTV' \r\n"
				+ " AND PARTITION_DATE > SYSDATE - 7 \r\n"
				+ " AND STATUS = 'E' \r\n"
				+ " AND ID = %s \r\n"
				+ " AND SUBSTR(PV_DOC_NAME,4,5) = '" + data.get("plaza") + "' \r\n";

		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 * 
		 */

		/*
		 * Paso 1
		 **********************/

		System.out.println("Paso 1");

		addStep(" Validar la configuración del servidor FTP en la tabla WM_FTP_CONNECTIONS de WMUSER.\n");

		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(tdcPaso1);

		SQLResult Paso1 = dbPosCh.executeQuery(tdcPaso1);
		boolean validaPaso1 = Paso1.isEmpty();
		if (validaPaso1 == false) {

			testCase.addQueryEvidenceCurrentStep(Paso1);

		}
		System.out.println(validaPaso1);
		assertFalse(validaPaso1, "Los datos no se encuentran configurados correctamente.");

		/*
		 * paso 2.-
		 *****************************************************/

		System.out.println("Paso 2");

		addStep("Validar que exista información de documentos RTV pendientes por procesar en la tabla POS_INBOUND_DOCS de POSUSER para la plaza.\n");

		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(tdcPaso2);

		SQLResult Paso2 = dbPosCh.executeQuery(tdcPaso2);
		boolean validaPaso2 = Paso2.isEmpty();

		String id = "";
		String doc = "";

		if (validaPaso2 == false) {

			id = Paso2.getData(0, "ID");
			doc = Paso2.getData(0, "PV_DOC_NAME");
			testCase.addQueryEvidenceCurrentStep(Paso2);

		}
		System.out.println(validaPaso2);
		assertFalse(validaPaso2, "No se tiene informacion en la tabla.");

		/*
		 * paso 3.
		 *****************************************************/

		System.out.println("Paso 3");

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		addStep(" Ejecutar el servicio PR5.Pub:run desde el Job PR5 de Ctrl-M para procesar la información de los documentos RTV de la plaza.\n");

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		System.out.println(contra);
		String dateExecution = pok.runIntefaceWM(data.get("interfase"), data.get("servicio"), null);
		System.out.println("Respuesta dateExecution" + dateExecution);

		/*
		 * paso 4.
		 *****************************************************/

		System.out.println("Paso 4");

		addStep(" Validar que se inserte el detalle de la ejecución de la interface en la tabla WM_LOG_RUN de WMLOG con STATUS = 'S' ");

		System.out.println(GlobalVariables.DB_HOST_LogChile);
		System.out.println(tdcPaso4);

		SQLResult Paso4 = dbPosCh.executeQuery(tdcPaso4);

		String runid = "";

		boolean validaPaso4 = Paso4.isEmpty();
		if (validaPaso4 == false) {

			runid = Paso4.getData(0, "RUN_ID");
			testCase.addQueryEvidenceCurrentStep(Paso4);

		}
		System.out.println(validaPaso4);
		assertFalse(validaPaso4, " No se econtro informacion en la base de datos.");

		/*
		 * paso 5.
		 *****************************************************/

		System.out.println("Paso 5");

		addStep(" Validar que se inserte el detalle de la ejecución de los Threads lanzados por la interface en la tabla WM_LOG_THREAD de WMLOG con STATUS = 'S'.\n");

		System.out.println(GlobalVariables.DB_HOST_LogChile);

		String FormatoPaso5 = String.format(tdcPaso5, runid);

		System.out.println(FormatoPaso5);

		SQLResult Paso5 = dbPosCh.executeQuery(FormatoPaso5);
		boolean validaPaso5 = Paso5.isEmpty();
		if (validaPaso5 == false) {

			testCase.addQueryEvidenceCurrentStep(Paso5);

		}
		System.out.println(validaPaso5);
		assertFalse(!validaPaso5, " No se econtro informacion en la base de datos.");

		/*
		 * paso 6.
		 *****************************************************/

		System.out.println("Paso 6");
		addStep(" Validar el envío del documento RTV al servidor FTP configurado para la ejecución de la interface en la tabla WM_FTP_CONNECTIONS de WMUSER.");

		FTPUtil ftp = new FTPUtil("10.184.80.24", 21, "posuser", "posuser");
		String ruta = "/u01/posuser/FEMSA_OXXO/POS/CL" + data.get("plaza") + "/working/" + doc;

		System.out.println(ruta);

		if (ftp.fileExists(ruta)) {

			System.out.println("Existe");
			testCase.addTextEvidenceCurrentStep("Se encontro archivo en la ruta" + ruta);

		} else {

			System.out.println("No existe");

		}
		assertFalse(!ftp.fileExists(ruta), "No se obtiene el archivo por FTP.");

		/*
		 * paso 7.
		 *****************************************************/

		System.out.println("Paso 7");
		addStep(" Validar que se actualize el status de los documentos RTV procesados en la tabla POS_INBOUND_DOCS de POSUSER a STATUS = 'E'.\n");

		System.out.println(GlobalVariables.DB_HOST_PosUserChile);

		String FormatoPaso7 = String.format(tdcPaso7, id);

		System.out.println(FormatoPaso7);

		SQLResult Paso7 = dbPosCh.executeQuery(FormatoPaso7);
		boolean validaPaso7 = Paso7.isEmpty();
		if (validaPaso7 == false) {

			testCase.addQueryEvidenceCurrentStep(Paso7);

		}

		System.out.println(validaPaso7);
		assertFalse(validaPaso7, " No se econtro informacion en la base de datos.");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub

		return "Construido. Validar la devolución a proveedores directos para la plaza (Chl).";

	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub

		return "ATC_FT_PR5_002_Validar_Dev_Prov_Plaza_Chl";

	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return null;
	}

}
