package interfaces.pb3;

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

public class PB3_ValidarQueDatosSeEnvienA_BD_BI extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PB3_001_Valida_Envio_de_Datos_A_BI(HashMap<String, String> data) throws Exception {

		
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
		
		String ValidReg = "SELECT  A.ID,A.PE_ID,A.PV_DOC_ID,A.STATUS,A.DOC_TYPE,A.PV_DOC_NAME,B.PID_ID, to_char (C.ADMIN_DATE,'DD/MON/YYYY') AS ADMIN_DATE "
				+ "FROM POSUSER.POS_INBOUND_DOCS A, POSUSER.POS_ROP_DETL b, POSUSER.POS_ROP C "
				+ "WHERE A.DOC_TYPE='ROP' "
				+ "AND A.STATUS='I' "
				+ "AND SUBSTR(A.PV_DOC_NAME, 4,5)='"+data.get("Plaza")+"' "
				+ "AND b.PID_ID=A.ID "
				+ "AND A.ID = C.PID_ID "
				+ "AND C.ADMIN_DATE > SYSDATE-60";


//		Paso 3
		
		String ValidLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER " + "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'PB3'  " + "AND START_DT >= TRUNC(SYSDATE) "
				+ "AND STATUS = 'S'  order by start_dt desc";

	
//	*********
//		Paso 4
	String 	PV_CR_TIENDA ="";
	String ADMIN_DATE = "";
			String ValidStatus =	"SELECT ID, TARGET_ID, SUBSTR(PV_DOC_NAME, 4,5) PV_CR_PLAZA, SUBSTR(PV_DOC_NAME, 9,5) PV_CR_TIENDA, STATUS, to_char (C.ADMIN_DATE,'DD/MON/YYYY') AS ADMIN_DATE , C.PARTITION_DATE  "
					+ "FROM POSUSER.POS_INBOUND_DOCS A, POSUSER.POS_ROP C  "
					+ "WHERE SUBSTR(PV_DOC_NAME, 4,5) ='"+data.get("Plaza")+"'  "
					+ "AND  TARGET_ID = '%s' "
					+ "AND DOC_TYPE='ROP' "
					+ "AND STATUS='E' "
					+ "AND A.ID = C.PID_ID "
					+ "AND C.ADMIN_DATE > SYSDATE-30 "
					+ "order by ADMIN_DATE desc";

//	Paso 5

			String ValidResCtrl=	"SELECT * "
					+ "FROM BIODSMKT.TEMP_RES_OP_CTRL "
					+ "WHERE PV_CR_PLAZA = '"+data.get("Plaza")+"' "
					+ "AND PV_CR_TIENDA = '%s' "
					+ "ORDER BY ULTIMA_FECHA_RO DESC";	
				
			//Paso 6
			String ValidTempResOper= "SELECT * "
					+ "FROM BIODSMKT.TEMP_RES_OPERATIVO  "
					+ "WHERE PV_CR_PLAZA = '"+data.get("Plaza")+"' "
					+ "AND PV_CR_TIENDA = '%s' "
					+ "AND TRUNC(WM_SENT_DATE) = TRUNC(SYSDATE) "
					+ "AND WM_RUN_ID ='%s' ";
		
//****	

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ " FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE='PB3' "
				+ " ORDER BY START_DT DESC) where rownum <=1 ";

//********************************************************************************************************************************************************************************		

		/* Pasos */

//************************************************Paso 1********************* ***********************************************************************************************************		
		

		addStep("Que exista al menos un registro con STATUS I, DOC_TYPE ROP para la plaza "+data.get("Plaza")+ " "
				+ "en las tablas POS_INBOUND_DOCS, "
				+ "POS_ROP_DETL y POS_ROP  y que la fecha administrativa sea mayor a la de los ultimos 30 dias en la BD de POSUSER.");
			
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		System.out.println(ValidReg);

		SQLResult ValidRegRes = dbPos.executeQuery(ValidReg);

		boolean ValidaBool = ValidRegRes.isEmpty();

		if (!ValidaBool) {
			
			testCase.addQueryEvidenceCurrentStep(ValidRegRes); 
		}

		System.out.println(ValidaBool); 
		assertFalse(ValidaBool,"No existen registros"); 

//*************************************************Paso 2***********************************************************************************************************************
		
		addStep("Invocar el servicio PB3.Pub:run. mediante la ejecucion del job runPB3");

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
		
		addStep("Se valida el STATUS = S en la tabla WM_LOG_RUN de la BD WMLOG, donde INTERFACE = PB3.");
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
	
		addStep("Se valida el STATUS = E y target_id = "+RunID+" para la plaza "+data.get("Plaza")+" en la tabla POS_INBOUND_DOCS de la BD POSUSER.");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String ValidStatusFormat = String.format(ValidStatus, RunID);

		System.out.println(ValidStatusFormat);

		SQLResult ValidStatusEjec = dbPos.executeQuery(ValidStatusFormat);

		boolean ValidStatusRes = ValidStatusEjec.isEmpty();

		if (!ValidStatusRes) {
			ADMIN_DATE = ValidStatusEjec.getData(0, "ADMIN_DATE");
			System.out.println("ADMIN_DATE: "+ADMIN_DATE);
			PV_CR_TIENDA = ValidStatusEjec.getData(0, "PV_CR_TIENDA");
			System.out.println("PV_CR_TIENDA: "+PV_CR_TIENDA);
			testCase.addQueryEvidenceCurrentStep(ValidStatusEjec);

		}

		System.out.println(ValidStatusRes);
		assertFalse(ValidStatusRes,"No se muestran resultados con status E");

//*********************************Paso 5*********************************************************

		addStep("Comprobar que se inserten los datos en la tabla temp_res_op_ctrl de la BD BI.");
		System.out.println(GlobalVariables.DB_HOST_BI);
		
	String ValidResCtrlFormat = String.format(ValidResCtrl,PV_CR_TIENDA, ADMIN_DATE);
	System.out.println(ValidResCtrlFormat);
	SQLResult ExecValidResCtrlFormat = dbBi.executeQuery(ValidResCtrlFormat);
	boolean ExecValidResCtrlFormatReq = ExecValidResCtrlFormat.isEmpty();
	System.out.println(ExecValidResCtrlFormatReq);
	
	if (!ExecValidResCtrlFormatReq) {

		testCase.addQueryEvidenceCurrentStep(ExecValidResCtrlFormat);

	}
	
	assertFalse(ExecValidResCtrlFormatReq,"No se actualizo la tabla BIODSMKT.TEMP_RES_OP_CTRL");
//********************************Paso 6************************************************************************************

	addStep("Se valida que los datos se hayan enviado a la tabla TEMP_RES_OPERATIVO de la BD BI.");
	System.out.println(GlobalVariables.DB_HOST_BI);
	
	String ValidTempResOperFormat = String.format(ValidTempResOper,PV_CR_TIENDA, RunID);
		System.out.println(ValidTempResOperFormat);
		SQLResult ExecValidTempResOperFormat = dbBi.executeQuery(ValidTempResOperFormat);
		boolean ExecValidTempResOperFormatReq = ExecValidTempResOperFormat.isEmpty();
		System.out.println(ExecValidTempResOperFormat);

		if(!ExecValidTempResOperFormatReq) {
			testCase.addQueryEvidenceCurrentStep(ExecValidTempResOperFormat);
		}
		
		assertFalse(ExecValidTempResOperFormatReq,"No se actualizo la tabla BIODSMKT.TEMP_RES_OPERATIVO");





	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PB3_001_Valida_Envio_de_Datos_A_BI";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminado.Validar que los datos se envien a la BD BI";
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


