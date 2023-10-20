package interfaces.fl3;

import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class FL3_Valida_insercion_de_informacion_en_RDM_e_INFOCEDIS extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_FL3_001_Info_Embarques_a_RDM_e_INFOCEDIS(HashMap<String, String> data) throws Exception {

		/**
		 * UTILERIA
		 *********************************************************************/

		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,
				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPuser = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,
				GlobalVariables.DB_PASSWORD_Puser);
		utils.sql.SQLUtil dbRDM = new SQLUtil(GlobalVariables.DB_HOST_RDM, GlobalVariables.DB_USER_RDM,
				GlobalVariables.DB_PASSWORD_RDM);
		SeleniumUtil u;
		PakageManagment pok;

		String status = "S"; // status exitoso
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";

		/**
		 * VARIABLES
		 *********************************************************************/
		String IdViaje="";
		String TSF="";
		String BolNumber="";
		String InfPend=	"SELECT FL_TSF,FL_CEDIS,FL_GRUPO,FL_ID_VIAJE,FL_BOL_NUMBER,FL_NUM_CHOFER  "
				+ "FROM WMUSER.WM_CHOFER_TRANSFER "
				+ "WHERE WM_STATUS = 'L' "
				+ "AND FL_CEDIS = '"+data.get("CEDIS") + "' ";
		
		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ "FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE LIKE '%FL3_Send%' "
				+ "ORDER BY START_DT DESC) where rownum <=1";

		String ValidEstatus = "select * from (SELECT  RUN_ID,INTERFACE,START_DT,STATUS,SERVER"
				+ " FROM WMLOG.WM_LOG_RUN " + "WHERE INTERFACE = 'FL3_Send' " + "AND STATUS = 'S' "
				+ " AND TRUNC(START_DT) = TRUNC(SYSDATE)) where rownum <=1";
		
		String	ValidInserc = "SELECT FL_TSF,FL_CEDIS,FL_GRUPO,FL_ID_VIAJE,FL_BOL_NUMBER,FL_NUM_CHOFER, WM_STATUS "
				+ " FROM WMUSER.WM_CHOFER_TRANSFER "
				+ "WHERE FL_CEDIS = '"+data.get("CEDIS") + "' "
				+ "AND FL_ID_VIAJE= '%s' "
				+ "AND WM_STATUS = 'E'";
		
		String ValidActualiz = "SELECT CEDIS,TSF_NO,FL_GRUPO,FL_ID_VIAJE,FL_UNIDAD,FL_NUM_CHOFER,FL_NOMBRE_CHOFER,FL_BOL "
				+ "FROM RDM100.XXFC_FL_VIAJE "
				+ "WHERE TSF_NO = ´%s' "
				+ "AND FL_BOL = '%s'";
		
//		****

		

		/**
		 * PASOS DEL CASO DE PRUEBA
		 *********************************************************************/

		/* PASO 1 *********************************************************************/

		addStep("Validar que existe información pendiente de procesar en la tabla: WM_CHOFER_TRANSFER de WMUSER con estatus L.");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(InfPend);
		SQLResult queryInf = dbPos.executeQuery(InfPend);

		boolean infEs = queryInf.isEmpty();
		if (!infEs) {
			
			IdViaje = queryInf.getData(0, "FL_ID_VIAJE");
			testCase.addQueryEvidenceCurrentStep(queryInf);
		}

		System.out.println(infEs);
		assertFalse(infEs, "No se obtiene informacion de la consulta");

		/* PASO 2 *********************************************************************/

		addStep("Ejecutar el servicio FL3.Pub:run.");

		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWM(data.get("interfase"), data.get("servicio"), null);
		System.out.println("Respuesta dateExecution " + dateExecution);
		System.out.println(tdcIntegrationServerFormat);
		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		String run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");
		System.out.println("RUN_ID = " + run_id + "\t Status: " + status1);

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R

		while (valuesStatus) {
			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);
			u.hardWait(3);
		}

		/* PASO 3 *********************************************************************/

		addStep("Validar que la interface se ejecutó correctamente con estatus S en WMLOG.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		SQLResult queryStatusLogexe = dbLog.executeQuery(ValidEstatus);
		

		boolean Bolstatus = queryStatusLogexe.isEmpty();

		if (!Bolstatus) {
			String fcwS = queryStatusLogexe.getData(0, "STATUS");
			System.out.println(fcwS);
			testCase.addQueryEvidenceCurrentStep(queryStatusLogexe);

		}

		assertFalse(Bolstatus, "El registro de ejecución de la plaza y tienda no fue exitoso");

		/* PASO 4 *********************************************************************/
		
		addStep("Validar que se actualizaron los campos wm_status a E, wm_run_id con el id de la tabla wm_log_run \n"
				+ "y wm_sent_date con la fecha de ejecución de la interfaz en la tabla WM_CHOFER_TRANSFER de WMUSER.");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String ValdiFormat = String.format(ValidInserc, IdViaje);
		System.out.println(ValdiFormat);
		SQLResult Validacion = dbPos.executeQuery(ValdiFormat);

		boolean validinf = Validacion.isEmpty();
		if (!validinf) {
			 TSF = queryStatusLogexe.getData(0, "FL_TSF");
			 BolNumber = queryStatusLogexe.getData(0, "FL_BOL_NUMBER"); 
			testCase.addQueryEvidenceCurrentStep(Validacion);
		}

		System.out.println(validinf);
		assertFalse(validinf, "No se obtiene informacion de la consulta");

		/* PASO 5 *********************************************************************/

		addStep("Validar que se insertó la información correctamente en la tabla XXFC_FL_VIAJE de RDM.");

		System.out.println(GlobalVariables.DB_HOST_RDM);
		String ValdiAcFormat = String.format(ValidActualiz, TSF,BolNumber);
		System.out.println(ValdiAcFormat);
		SQLResult Query = dbRDM.executeQuery(ValdiAcFormat);

		boolean tsc = Query.isEmpty();

		if (!tsc) {
			testCase.addQueryEvidenceCurrentStep(Query);
		}

		System.out.println(tsc);
		assertFalse(tsc, "No se obtiene informacion de la consulta");

	}
	
//	PASO 6 ********************************************************************************+++
	
//	EN ESPERA DE BD RDMINFOCEDI

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminada.Validar que se inserte la informacion de embarques del CEDIS en RDM y en INFOCEDIS";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QAautomation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_FL3_001_Info_Embarques_a_RDM_e_INFOCEDIS";
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

