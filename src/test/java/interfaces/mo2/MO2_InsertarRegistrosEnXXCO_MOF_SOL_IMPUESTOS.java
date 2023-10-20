package interfaces.mo2;

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

public class MO2_InsertarRegistrosEnXXCO_MOF_SOL_IMPUESTOS extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_MO2_004_Insertar_Regs_En_XXCO_MOF_SOL_IMPUESTOS(HashMap<String, String> data) throws Exception {

		
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
		String ConsRegs="SELECT WM_STATUS, WM_RUN_ID, WM_RETRIES, WM_FECHA, WM_ERROR "
				+ "FROM XXSADEL.TBL_SOL_IMPUESTOS "
				+ "WHERE PERIODO_ID = '"+data.get("Periodo")+ "' "
				+ "AND DESPACHO_ID = '"+data.get("Despacho")+"'";

		
//		Paso 3

		String ValidLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER " + "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'MO2'  " + "AND START_DT >= TRUNC(SYSDATE) "
				+ "AND STATUS = 'S'  order by start_dt desc";


//		Paso 4
	
		String CompDatos =	"SELECT MOF_SOL_IMPUESTOS_ID,PERIODO_ID,DESPACHO_ID,CREATION_DATE  "
				+ "FROM XXCO.XXCO_MOF_SOL_IMPUESTOS  "
				+ "WHERE TRUNC(CREATION_DATE) = TRUNC(SYSDATE) "
				+ "AND PERIODO_ID = '"+data.get("Periodo")+"' AND DESPACHO_ID = '"+data.get("Despacho")+"' ";
//****	

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ " FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE='MO2' "
				+ " ORDER BY START_DT DESC) where rownum <=1 ";

//********************************************************************************************************************************************************************************		

		/* Pasos */

//************************************************Paso 1********************* ***********************************************************************************************************		
		
		addStep("Consultar que existan registros en la tabla TBL_SOL_IMPUESTOS de la BD PORTAL");

		System.out.println(GlobalVariables.DB_HOST_FCIASQA);

		System.out.println(ConsRegs);

		SQLResult ExecConsRegs = dbFciasqa.executeQuery(ConsRegs);

		boolean ValidaBool = ExecConsRegs.isEmpty();

		if (!ValidaBool) {
			
			testCase.addQueryEvidenceCurrentStep(ExecConsRegs);
		}

		System.out.println(ValidaBool);
		assertFalse(ValidaBool, "No existen registros para procesar" );

//*************************************************Paso 2***********************************************************************************************************************
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

		String dateExecution = pok.runIntefaceWmWithDosInput(data.get("interfase"), data.get("servicio"),
				data.get("Periodo"),"periodo_id",
				data.get("Despacho"),"despacho_id");
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);

		String status1 = is.getData(0, "STATUS");// guarda el run id de la
													// ejecución

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se
																// encuentra en
																// estatus R

		while (valuesStatus) {

			status1 = is.getData(0, "STATUS");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(2);

		}
		

////		********************Paso 3***************************************************************
	
		addStep("Comprobar que existe registro de la ejecucion correcta en la tabla WM_LOG_RUN de la "
				+ "BD WMLOG, donde INTERFACE es igual a 'MO2' y STATUS es igual a 'S'.");

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
	
//**********************************************************Paso 4*************************************************************************************************************			

		addStep("Comprobar que los datos fueron insertados en la tabla "
				+ "XXCO_MOF_SOL_IMPUESTOS de la BD ORAFIN, donde CREATION_DATE es igual a la fecha actual");
		
		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		String CompDatosFormat = String.format(CompDatos, RunID);
		System.out.println(CompDatosFormat);
		
		SQLResult ExecCompDatos = dbAvebqa.executeQuery(CompDatosFormat);

		boolean ExecCompDatosRes = ExecCompDatos.isEmpty();

		if (!ExecCompDatosRes) {
			testCase.addQueryEvidenceCurrentStep(ExecCompDatos);

		}

		System.out.println(ExecCompDatosRes);
		assertFalse(ExecCompDatosRes, "La información no ha sido insertada");




		
		
	}


	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_MO2_004_Insertar_Regs_En_XXCO_MOF_SOL_IMPUESTOS";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Completado. Insertar correctamente los registros en la tabla XXCO_MOF_SOL_IMPUESTOS.";
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



