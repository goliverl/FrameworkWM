package interfaces.pb2;

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

public class PB2_ValidaQueDatosEnvienA_BD_BI extends BaseExecution  {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PB2_001_Valida_Envio_de_Datos_A_BI(HashMap<String, String> data) throws Exception {
		
		/*
		 * Utilerías
		 ********************************************************************************************************************************************/
		
		
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
				
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,
				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		
		utils.sql.SQLUtil dbBi = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_BI,
				GlobalVariables.DB_USER_BI,GlobalVariables.DB_PASSWORD_BI);


		/*
		 * Variables
		 ******************************************************************************************************************************************/

		// Paso 1
		String ADMIN_DATE = "";
		String ValidReg = "SELECT  A.ID,A.PE_ID,A.PV_DOC_ID,A.STATUS,A.DOC_TYPE,A.PV_DOC_NAME,B.PID_ID, to_char (C.ADMIN_DATE,'DD/MON/YYYY') AS ADMIN_DATE "
				+ "FROM POSUSER.POS_INBOUND_DOCS A, POSUSER.POS_IOD_DETL b, POSUSER.POS_IOD C "
				+ "WHERE A.DOC_TYPE='IOD' "
				+ "AND A.STATUS='I' "
				+ "AND SUBSTR(A.PV_DOC_NAME, 4,5)='"+data.get("Plaza")+"' "
				+ "AND b.PID_ID=A.ID "
				+ "AND A.ID = C.PID_ID "
				+ "AND C.ADMIN_DATE > SYSDATE-70";
		
//		Paso 3
		
		String ValidLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER " + "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'PB2main'  " + "AND START_DT >= TRUNC(SYSDATE) "
				+ "AND STATUS = 'S'  order by start_dt desc";

	
//	*********
//		Paso 4

			String ValidStatus =	"SELECT DISTINCT TARGET_ID, SUBSTR(b.PV_DOC_NAME, 4,5) PV_CR_PLAZA, SUBSTR(b.PV_DOC_NAME, 9,5) PV_CR_TIENDA, STATUS  "
					+ "FROM posuser.POS_INBOUND_DOCS b "
					+ "WHERE SUBSTR(b.PV_DOC_NAME, 4,5)='"+data.get("Plaza")+"'  "
					+ "AND  b.TARGET_ID = '%s' "
					+ "AND DOC_TYPE='IOD' AND STATUS='E'";

//	Paso 5
	
		
			String ValidTempCtrl=	"SELECT PV_CR_PLAZA, PV_CR_TIENDA, to_char (ULTIMA_FECHA_IO,'dd/mm/rrrr') as ULTIMA_FECHA_IO "
					+ "FROM BIODSMKT.temp_inv_op_ctrl "
					+ "WHERE PV_CR_PLAZA = '"+data.get("Plaza")+"' "
					+ "AND ULTIMA_FECHA_IO = '%s' ";
				
				String ValidTempItem= "SELECT PV_CR_PLAZA, PV_CR_TIENDA, to_char (ULTIMA_FECHA_IO,'dd/mm/rrrr') as ULTIMA_FECHA_IO "
						+ "FROM BIODSMKT.temp_inv_op_ctrl_item "
						+ "WHERE PV_CR_PLAZA = '"+data.get("Plaza")+"' "
						+ "AND ULTIMA_FECHA_IO = '%s' ";
				
	
				String ValidTempOper =	"SELECT PV_CR_PLAZA,PV_CR_TIENDA,ADMIN_DATE,ITEM, WM_RUN_ID,WM_SENT_DATE "
						+ "FROM BIODSMKT.TEMP_INV_OPERATIVO "
						+ "WHERE PV_CR_PLAZA = '"+data.get("Plaza")+"' "
						+ "AND wm_run_id = '%s'";
		
//****	

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ " FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE='PB2main' "
				+ " ORDER BY START_DT DESC) where rownum <=1 ";

//********************************************************************************************************************************************************************************		

		/* Pasos */

//************************************************Paso 1********************* ***********************************************************************************************************		
		addStep("Que exista al menos un registro con STATUS I, DOC_TYPE IOD para la plaza "+data.get("Plaza")+" en las tablas POS_INBOUND_DOCS, "
				+ "POS_IOD_DETL y POS_IOD  y que la fecha administrativa sea mayor a la de los ultimos 30 dias en la BD de POSUSER.");
			
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		System.out.println(ValidReg);

		SQLResult ValidRegRes = dbPos.executeQuery(ValidReg);

		boolean ValidaBool = ValidRegRes.isEmpty();

		if (!ValidaBool) {
			ADMIN_DATE = ValidRegRes.getData(0, "ADMIN_DATE");
			System.out.println("ADMIN_DATE: "+ADMIN_DATE);
			testCase.addQueryEvidenceCurrentStep(ValidRegRes); 
		}

		System.out.println(ValidaBool); 
		assertFalse(ValidaBool,"No existen registros"); 

//*************************************************Paso 2***********************************************************************************************************************
		
		addStep("Se ejecuta el proceso FEMSA_PB2.Pub:run.");

		// Utileria

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String status = "S";

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		
		String dateExecution = pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
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

	
//**********************************************************Paso 3*************************************************************************************************************		
		
		addStep("Se valida el STATUS = S en la tabla WM_LOG_RUN de la BD WMLOG, donde INTERFACE = PB2main.");
		String RunID = "";
		String ThreadID = "";
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(ValidLog);
		SQLResult ExecLog = dbLog.executeQuery(ValidLog);

		boolean LogRequest = ExecLog.isEmpty();

		if (!LogRequest) {
			RunID = ExecLog.getData(0, "RUN_ID");

			testCase.addQueryEvidenceCurrentStep(ExecLog);

		}

		System.out.println(LogRequest);
		assertFalse(LogRequest,"No se muestra la información del log.");
		
		
		
		
//		*************************************Paso 4 **************************************************
		
		addStep("Se valida el STATUS = 'E' y target_id = "+RunID+" en la tabla POS_INBOUND_DOCS de la BD POSUSER.");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String ValidStatusFormat = String.format(ValidStatus, RunID);

		System.out.println(ValidStatusFormat);

		SQLResult ValidStatusEjec = dbPos.executeQuery(ValidStatusFormat);

		boolean ValidStatusRes = ValidStatusEjec.isEmpty();

		if (!ValidStatusRes) {

			testCase.addQueryEvidenceCurrentStep(ValidStatusEjec);

		}

		System.out.println(ValidStatusRes);
		assertFalse(ValidStatusRes,"No se muestran resultados con status E");

//*********************************Paso 5*********************************************************
		 
		addStep("Se comprueba que los registros fueron insertados o actualizados correctamente en las tablas BIODSMKT."
				+ "temp_inv_op_ctrl,BIODSMKT.temp_inv_op_ctrl_item y BIODSMKT.temp_inv_operativo, donde la Plaza sea "
			    +data.get("Plaza")+" en la BD BI");
		System.out.println(GlobalVariables.DB_HOST_BI);
		
	String ValidTempCtrlFormat = String.format(ValidTempCtrl, ADMIN_DATE);
	System.out.println(ValidTempCtrlFormat);
	SQLResult ExecValidTempCtrlFormat = dbBi.executeQuery(ValidTempCtrlFormat);
	boolean ExecValidTempCtrlFormatReq = ExecValidTempCtrlFormat.isEmpty();
	System.out.println(ExecValidTempCtrlFormatReq);
	

		String ValidTempItemFormat = String.format(ValidTempItem, ADMIN_DATE);
		System.out.println(ValidTempItemFormat);
		SQLResult ExecValidTempItemFormat = dbBi.executeQuery(ValidTempItemFormat);
		boolean ExecValidTempItemFormatReq = ExecValidTempItemFormat.isEmpty();
		System.out.println(ExecValidTempItemFormatReq);
		 

		String ValidTempOperFormat = String.format(ValidTempOper, RunID);
		System.out.println(ValidTempOperFormat);
		SQLResult ExecValidTempOperFormat = dbBi.executeQuery(ValidTempOperFormat);
		boolean ExecValidTempOperFormatReq = ExecValidTempOperFormat.isEmpty();
		System.out.println(ExecValidTempOperFormatReq);


		
		if (!ExecValidTempCtrlFormatReq) {

			testCase.addQueryEvidenceCurrentStep(ExecValidTempCtrlFormat);

		}
		
		if(!ExecValidTempItemFormatReq) {
			testCase.addQueryEvidenceCurrentStep(ExecValidTempItemFormat);
		}
		
		if(!ExecValidTempOperFormatReq) {
			testCase.addQueryEvidenceCurrentStep(ExecValidTempOperFormat);
			
		}

		assertFalse(ExecValidTempCtrlFormatReq,"No se actualizo la tabla BIODSMKT.temp_inv_op_ctrl");
		assertFalse(ExecValidTempItemFormatReq,"No se actualizo la tabla BIODSMKT.temp_inv_op_ctrl_item");
		assertFalse(ExecValidTempOperFormatReq,"No se actualizo la tabla BIODSMKT.TEMP_INV_OPERATIVOc");





	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PB2_001_Valida_Envio_de_Datos_A_BI";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminado. procesar información de los tickets de las tiendas almacenados en el sistema del POS, y enviarla al sistema de BI.";
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


