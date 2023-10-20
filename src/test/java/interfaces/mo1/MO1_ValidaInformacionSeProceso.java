package interfaces.mo1;

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

public class MO1_ValidaInformacionSeProceso extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_MO1_001_ValidaInfoProcesada(HashMap<String, String> data) throws Exception {

		
		
		/*
		 * Utilerías
		 ********************************************************************************************************************************************/
				   
	    utils.sql.SQLUtil dbFciasqa = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCIASQA,
				GlobalVariables.DB_USER_FCIASQA, GlobalVariables.DB_PASSWORD_FCIASQA);
	    
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		utils.sql.SQLUtil dbAvebqa = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA,
				GlobalVariables.DB_USER_AVEBQA,GlobalVariables.DB_PASSWORD_AVEBQA);
		


		/*
		 * Variables
		 ******************************************************************************************************************************************/

		// Paso 1
		String ValidPeriod = "SELECT periodo_id, anio, mes_id, estado "
				+ "FROM XXSADEL.ct_periodos "
				+ "WHERE periodo_id = '"+data.get("Periodo")+"' ";
//Paso 2
	
		String ValidInf =	"Select * from (SELECT PERIODO_ID, RFC, PORC_DEDUCIBILIDAD, NUMERO_EMPLEADOS, NUMERO_EMPLEADOS_MIN, INGRESO_NETO_PREOPERATIVO, CR_PLAZA, CR_TIENDA "
				+ "FROM XXMOF.VW_DEDUCIBILIDAD "
				+ "WHERE PERIODO_ID = '"+data.get("Periodo")+"') where rownum <= 10";
			
//		Paso 4

		String ValidLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER " + "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'MO1'  " + "AND START_DT >= TRUNC(SYSDATE) "
				+ "AND STATUS = 'S'  order by start_dt desc";

//		Paso 5
		String ValidInsert = "SELECT DISTINCT(P.PERIODO_ID), P.MES, P.ESTADO, P.CREATION_DATE  "
				+ "FROM XXCO.XXCO_MOF_PERIODOS P, XXCO.XXCO_MOF_DEDUCIBILIDAD D  "
				+ "WHERE P.PERIODO_ID = D.PERIODO_ID  "
				+ "AND P.PERIODO_ID = '"+data.get("Periodo")+"' "  
				+ "AND TRUNC(D.CREATION_DATE) = TRUNC(SYSDATE)";
	



//****	

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ " FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE='MO1' "
				+ " ORDER BY START_DT DESC) where rownum <=1 ";

//********************************************************************************************************************************************************************************		

		/* Pasos */

//************************************************Paso 1********************* ***********************************************************************************************************		
		
		addStep("Validar que no exista información en la tabla ct_periodos de BD PORTAL del perido_id a procesar");

		System.out.println(GlobalVariables.DB_HOST_FCIASQA);

		System.out.println(ValidPeriod);

		SQLResult ExecValidPeriod = dbFciasqa.executeQuery(ValidPeriod);

		boolean ValidaBool = ExecValidPeriod.isEmpty();

		if (!ValidaBool) {
			
			testCase.addQueryEvidenceCurrentStep(ExecValidPeriod);
		}

		System.out.println(ValidaBool);
		assertFalse(ValidaBool, "No existen registros para procesar" );

//*************************************************Paso 2***********************************************************************************************************************
		 
		addStep("Validar que exista información en la tabla VW_DEDUCIBILIDAD de la BD PORTAL para el periodo_id a procesar");
    
		System.out.println(GlobalVariables.DB_HOST_FCIASQA);
		System.out.println(ValidInf);
		SQLResult ExecValidInf = dbFciasqa.executeQuery(ValidInf);

		boolean ExecValidInfRes = ExecValidInf.isEmpty();
		
		if (!ExecValidInfRes) {

			testCase.addQueryEvidenceCurrentStep(ExecValidInf);
		}

		System.out.println(ExecValidInfRes);
		assertFalse(ExecValidInfRes, "No existe información en la tabla VW_DEDUCIBILIDAD de la BD PORTAL para el periodo_id a procesar" );
		
//		********************Paso 3***************************************************************
		addStep("Ejecutar la interface MO1.Pub:run por HTTP indicado el periodo_id");

		// Utileria

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);


		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWmWithInput10(data.get("interfase"), data.get("servicio"),data.get("Periodo"),"periodo_id");
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		String run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");// guarda el run id de la
													// ejecución

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se
																// encuentra en
																// estatus R

		while (valuesStatus) {

			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(2);

		}
		
		u.close();

//**********************************************************Paso 4*************************************************************************************************************			
			
		addStep("Validar el registro de ejecución de la interface MO1 en la tabla WM_LOG_RUN de "
				+ "WMLOG el estatus de esta deberá ser S.");
		String RunID = "";
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(ValidLog);
		SQLResult ExecLog = dbLog.executeQuery(ValidLog);

		boolean LogRequest = ExecLog.isEmpty();

		if (!LogRequest) {
			RunID = ExecLog.getData(0, "RUN_ID");

			testCase.addQueryEvidenceCurrentStep(ExecLog);

		}

		System.out.println(LogRequest);
		assertFalse(LogRequest, "No se muestra la información del log.");

//		*************************************Paso 5 **************************************************
	
		addStep("Validar que la información haya sido insertada en las tablas de ORAFIN");
		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		System.out.println(ValidInsert);
		SQLResult ExecValidInsert = dbAvebqa.executeQuery(ValidInsert);

		boolean ExecValidInsertRes = ExecValidInsert.isEmpty();

		if (!ExecValidInsertRes) {
			testCase.addQueryEvidenceCurrentStep(ExecValidInsert);

		}

		System.out.println(ExecValidInsertRes);
		assertFalse(ExecValidInsertRes, "La información no ha sido insertada en las tablas de ORAFIN");


		
		
	}


	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_MO1_001_ValidaInfoProcesada";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Validar que la informacion se haya procesado para el periodo";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
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


