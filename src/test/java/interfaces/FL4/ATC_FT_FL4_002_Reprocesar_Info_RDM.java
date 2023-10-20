package interfaces.FL4;

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

public class ATC_FT_FL4_002_Reprocesar_Info_RDM extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_FL4_002_Reprocesar_Info_RDM_test(HashMap<String, String> data) throws Exception {
		
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

		String validInf = "SELECT WM_STATUS, CEDIS, CRPLAZA,TIPO_DISTRO,RUTA_ORIGINAL,TSF_NO, CR_TIENDA  "
				+ "FROM WMUSER.WM_RUTAS_PROP_FL_FAILED " + "WHERE CEDIS = '"+data.get("CEDIS")+"' AND WM_STATUS = 'R'";

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ "FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE LIKE '%FL4_REPROC%' "
				+ "ORDER BY START_DT DESC) where rownum <=1";

		String ValidEstatus = "select * from (SELECT  RUN_ID,INTERFACE,START_DT,STATUS,SERVER"
				+ " FROM WMLOG.WM_LOG_RUN " + "WHERE INTERFACE = 'FL4_REPROC' " + "AND STATUS = 'S' "
				+ "AND TRUNC(START_DT) = TRUNC(SYSDATE)) where rownum <=1";

		String ValidInserc = "select * from (SELECT WM_RUN_ID,CEDIS,CRPLAZA,TIPO_DISTRO,RUTA_ORIGINAL,RUTA_PROPUESTA_FL FROM XXFC_RUTAS_PROP_FL "
				+ "WHERE CEDIS = '"+data.get("CEDIS")+ "' AND WM_RUN_ID = %s " + "AND TRUNC(WM_SENT_DATE) = TRUNC(SYSDATE) "
				+ "ORDER BY CREATE_DATE DESC) where rownum <=1";

		String ValidActualiz = "SELECT WM_RUN_ID,WM_STATUS, CEDIS, CRPLAZA,TIPO_DISTRO,RUTA_ORIGINAL,TSF_NO, CR_TIENDA "
				+ "FROM WMUSER.WM_RUTAS_PROP_FL_FAILED " + "WHERE CEDIS = '"+data.get("CEDIS")+  "' AND WM_RUN_ID = %s "
				+ "AND WM_STATUS = 'E'";

		/**
		 * PASOS DEL CASO DE PRUEBA
		 *********************************************************************/

		/* PASO 1 *********************************************************************/

		addStep("Validar que se tiene informacion con estatus R en la tabla XXFC_RUTAS_PROP_FL_FAILED de WMUSER.");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(validInf);
		SQLResult queryInf = dbPos.executeQuery(validInf);

		boolean infEs = queryInf.isEmpty();
		if (!infEs) {
			testCase.addQueryEvidenceCurrentStep(queryInf);
		}

		System.out.println(infEs);
		assertFalse(infEs, "No se obtiene informacion de la consulta");

		/* PASO 2 *********************************************************************/

		addStep("Ejecutar el servicio: FL4.Pub:runReproc.");

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

		addStep("Validar que la interfaz se ejecute correctamente con estatus S en WMLOG.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		SQLResult queryStatusLogexe = dbLog.executeQuery(ValidEstatus);
		String fcwS = queryStatusLogexe.getData(0, "STATUS");
		System.out.println(fcwS);

		boolean Bolstatus = queryStatusLogexe.isEmpty();

		if (!Bolstatus) {
			testCase.addQueryEvidenceCurrentStep(queryStatusLogexe);

		}

		assertFalse(Bolstatus, "El registro de ejecucion de la plaza y tienda no fue exitoso");

		/* PASO 4 *********************************************************************/

		addStep("Validar que la informacion se inserto correctamente en la tabla XXFC_RUTAS_PROP_FL de RDM.");

		System.out.println(GlobalVariables.DB_HOST_RDM);
		String ValdiFormat = String.format(ValidInserc, run_id);
		System.out.println(ValdiFormat);
		SQLResult Validacion = dbRDM.executeQuery(ValdiFormat);

		boolean validinf = Validacion.isEmpty();
		if (!validinf) {
			testCase.addQueryEvidenceCurrentStep(Validacion);
		}

		System.out.println(validinf);
		assertFalse(validinf, "No se obtiene informacion de la consulta");

		/* PASO 5 *********************************************************************/

		addStep("Validar que se actualizo el campo WM_STATUS en la tabla XXFC_RUTAS_PROP_FL_FAILED a E correctamente.");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String ValdiAcFormat = String.format(ValidActualiz, run_id);
		System.out.println(ValdiAcFormat);
		SQLResult Query = dbPos.executeQuery(ValdiAcFormat);

		boolean tsc = Query.isEmpty();

		if (!tsc) {
			testCase.addQueryEvidenceCurrentStep(Query);
		}

		System.out.println(tsc);
		assertFalse(tsc, "No se obtiene informacion de la consulta");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Validar que se re-procese la informaci�n con estatus R desde WMUSER a RDM";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo automatizacion";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
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
