package interfaces.sb_wo1;

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

public class SB_WO1_VerificarProcesoSincronizarConveniosStatus extends BaseExecution{

	/**
	 * Desc: Verificar proceso de la interfaz modulo - Sincronizar Convenios Status Web Con Oracle.
	 * @author Ultima modificacion Mariana Vives
	 * @date 27/02/2023
	 */
	
	@Test(dataProvider = "data-provider")
	public void SB_WO1_VerificarProcesoSincronizarConveniosStatus_test(HashMap<String, String> data) throws Exception {

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

		String VerifDatos="	SELECT numero_convenio,estatus_convenio "
				+ "FROM XXBN.xxbn_convenios  "
				+ "WHERE wm_status IS NULL  "
				+ "AND estatus_convenio IS NOT NULL";
	
		

		
//Paso 3		
		String ValidLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER " + "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'SB_WO1'  " + "AND START_DT >= TRUNC(SYSDATE) "
//				+ "AND STATUS = 'S' "
				+ " order by start_dt desc";


//	Paso 4
		String VerifUpdt="SELECT NUMERO_CONVENIO,TIPO_CONVENIO_LOOKUP,WM_STATUS,ESTATUS_CONVENIO "
				+ " FROM XXBN.xxbn_convenios "
				+ " WHERE wm_status = 'S' "
				+ " and numero_convenio='%s'";
	
//	PASO 5			
				String ValidStatuss=	"SELECT NUMERO_CONVENIO,TIPO_CONVENIO,ESTATUS_CONVENIO  "
						+ "FROM XXFC.xxfc_convenios "
						+ "WHERE numero_convenio = '%s' "
						+ "AND estatus_convenio = '%s'";
			


//****	
		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ " FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE='SB_WO1' "
				+ " ORDER BY START_DT DESC) where rownum <=1 ";

//********************************************************************************************************************************************************************************		

		/* Pasos */

//************************************************Paso 1********************* ***********************************************************************************************************		
	
		addStep("Verificar que existan datos a procesar para la ejecución de la interfaz.");
		String NUMERO_CONVENIO="";
		System.out.println(GlobalVariables.DB_HOST_FCIASQA);

		System.out.println(VerifDatos);

		SQLResult ExecVerifDatos = dbFciasqa.executeQuery(VerifDatos);

		boolean ValidaBool = ExecVerifDatos.isEmpty();

		if (!ValidaBool) {
			
			NUMERO_CONVENIO = ExecVerifDatos.getData(0, "NUMERO_CONVENIO");
			testCase.addQueryEvidenceCurrentStep(ExecVerifDatos);
		}

		System.out.println(ValidaBool);
		assertFalse(ValidaBool, "No se devuelve informacion" );

//*************************************************Paso 2***********************************************************************************************************************
		
		addStep("Ejecutar la interfaz por medio del servicio SB_WO1.Pub:run."
				+ "El servicio será invocado con el Job runSB_WO1.");

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

		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
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
		

//********************************Paso 3***********************************************************
		
		addStep("Validar que la interfaz haya finalizado correctamente en el WMLOG.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(ValidLog);
		SQLResult ExecLog = dbLog.executeQuery(ValidLog);

		boolean LogRequest = ExecLog.isEmpty();

		if (!LogRequest) {
			testCase.addQueryEvidenceCurrentStep(ExecLog);

		}

		System.out.println(LogRequest);
		assertFalse(LogRequest, "No se muestra la información del log.");
	
		
		
////		********************Paso 4***************************************************************
	
		addStep("Verificar la actualización del status a S de los convenios en la tabla XXBN_CONVENIOS.");
		String ESTATUS_CONVENIO="";
		System.out.println(GlobalVariables.DB_HOST_FCIASQA);
		String VerifUpdtFormat = String.format(VerifUpdt, NUMERO_CONVENIO);
		System.out.println(VerifUpdtFormat);	
		SQLResult ExecVerifUpdtFormat = dbFciasqa.executeQuery(VerifUpdtFormat);
		boolean VerifUpdtFormatRes = ExecVerifUpdtFormat.isEmpty();

		if (!VerifUpdtFormatRes) {
			ESTATUS_CONVENIO = ExecVerifUpdtFormat.getData(0, "ESTATUS_CONVENIO");
	testCase.addQueryEvidenceCurrentStep(ExecVerifUpdtFormat);
		}

		System.out.println(VerifUpdtFormatRes);
		assertFalse(VerifUpdtFormatRes, "No se actualizo el status" );

////	********************Paso 5***************************************************************
		
	addStep("Verificar la actualización del status de los convenios en la tabla XXFC_CONVENIOS.");

	System.out.println(GlobalVariables.DB_HOST_AVEBQA);
	String ValidStatussFormat = String.format(ValidStatuss, NUMERO_CONVENIO, ESTATUS_CONVENIO);
	System.out.println(ValidStatussFormat);	
	SQLResult ExecValidStatussFormat = dbAvebqa.executeQuery(ValidStatussFormat);
	boolean ExecValidStatussFormatRes = ExecValidStatussFormat.isEmpty();

	if (!ExecValidStatussFormatRes) {

testCase.addQueryEvidenceCurrentStep(ExecValidStatussFormat);
	}

	System.out.println(ExecValidStatussFormatRes);
	assertFalse(ExecValidStatussFormatRes, "No se actualizo el estatus en la tabla XXFC_CONVENIOS. " );
	
		
	}


	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "SB_WO1_VerificarProcesoSincronizarConveniosStatus_test";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "SB_WO1_VerificarProcesoSincronizarConveniosStatus";
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



