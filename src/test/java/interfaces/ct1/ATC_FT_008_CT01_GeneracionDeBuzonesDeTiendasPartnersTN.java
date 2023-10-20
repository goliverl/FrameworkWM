package interfaces.ct1;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;

import java.util.HashMap;

import org.testng.annotations.Test;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import static org.testng.Assert.assertTrue;
import org.json.JSONObject;
import utils.controlm.ControlM;
import utils.controlm.pageObject.Control_mInicio;
/**
 * Proyecto: "Actualizacion tecnologica Webmethods"
 * ID: 1
 * Este script cubre los siguientes casos de prueba:
*Prueba de regresion  para comprobar la no afectacion en la funcionalidad principal de la interface 
* FEMSA_CT01 para generar los buzones de las tiendas y parthner de  tranding Network en My Web Methods Server, 
* al ser migrada la interface de  WM9.9 a WM10.5
 * @author Mariana Vives
 * @date   2023/01/30
 */

public class ATC_FT_008_CT01_GeneracionDeBuzonesDeTiendasPartnersTN extends BaseExecution {

	@Test(dataProvider="data-provider")
	public void ATC_FT_008_CT01_GeneracionDeBuzonesDeTiendasPartnersTN_test(HashMap<String, String> data) throws Exception {

		/*  Este script cubre los siguientes casos de prueba:
		  -Prueba de regresion  para comprobar la no afectacion en la funcionalidad principal de la interface 
		  FEMSA_CT01 para generar los buzones de las tiendas y parthner de  tranding Network en My Web Methods Server, 
		  al ser migrada la interface de  WM9.9 a WM10.5.
		  */
/* Utilerias *********************************************************************/		
		
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_Oiwmqa, GlobalVariables.DB_USER_Oiwmqa, GlobalVariables.DB_PASSWORD_Oiwmqa);
		SQLUtil dbCNT = new SQLUtil(GlobalVariables.DB_HOST_FCIASQA, GlobalVariables.DB_USER_FCIASQA, GlobalVariables.DB_PASSWORD_FCIASQA);
		//Falta DB  CNT_Peru

		/**
		* Variables ******************************************************************************************
		* 
		*/
			    //Paso 1
				String ValidaSatusL = "SELECT ID, CR_PLAZA, CR_TIENDA, WM_STATUS_BUZON\r\n" + 
						"FROM WMUSER.WM_BUZONES_T_TIENDAS \r\n" + 
						"WHERE WM_STATUS_BUZON = 'L' \r\n" + 
						"AND CR_PLAZA = '" + data.get("plaza") +"' \r\n" + 
						"AND CR_TIENDA = '" + data.get("tienda") +"'\r\n" + 
						"ORDER BY CREATION_DATE";
				
				//Paso 6
				String  ValidaStatusS = "SELECT RUN_ID, START_DT, STATUS, SERVER FROM WMLOG.WM_LOG_RUN \r\n" +
						"WHERE INTERFACE = 'CT01_BUZON \r\n" +
						"AND INTERFACE = 'CT01_BUZON' \r\n" + 
						"AND STATUS = 'S' \r\n" +
						"ORDER BY 1 DESC";
				
				//Paso 7
				String ValidaThreads = "SELECT THREAD_ID, PARENT_ID, NAME, START_DT, STATUS FROM WMLOG.WM_LOG_THREAD \r\n" +
						"WHERE PARENT ID = '%s' \r\n" +
						"AND STATUS = 'S'";
				
				//Paso 8
				String ValidaError = "SELECT THREAD_ID, PARENT_ID, NAME, START_DT, STATUS FROM WMLOG.WM_LOG_THREAD \r\n" +
						"WHERE RUN_ID='%s'";
			      
				//Paso 10  -> revisar el schema del query
				String ValidaStatusE = "SELECT * FROM WM_BUZONES_T_TIENDAS \r\n" +
						"WHERE WM_STATUS_BUZON = 'E' \r\n" +
						"AND CR_PLAZA ="+ data.get("plaza") + "' \r\n" + 
						"AND CR_TIENDA=" + data.get("tienda") +"'\r\n" + 
						"AND TRUNC(WM_FECHA_PROC)=TRUNC(SYSDATE) \r\n" +
						"AND WM_RUN_ID= '%s' \r\n" +
						"ORDER BY CREATION_DATE";
				
				//Paso 16
				String ConsultaStatusS = "SELECT RUN_ID, INTERFACE, START_DT, STATUS, SERVER FROM WMLOG.WM_LOG_RUN \r\n"
						+ " WHERE INTERFACE = 'CT01_TN' \r\n"
						+ "     AND START_DT >= TRUNC(SYSDATE)  \r\n"
						+ "     AND STATUS = 'S' \r\n"
						+ " ORDER BY 1 DESC";
						
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
//**********************************************Paso 1	****************************************************************************
	//Establecer conexion con la BD *XXCNT_WMUSER* -> Pendiente el acceso a DB PERU
	
//**********************************************Paso 2	****************************************************************************
			addStep("Verificar que existan  la plaza y la tienda en la tabla WM_BUZONES_T_TIENDAS con WM_STATUS_BUZON = 'L' en la BD *XXCNT_WMUSER*. ");
			
			SQLResult ValidaValidaSatusL_Res = executeQuery(dbCNT, ValidaSatusL);
			
			boolean validaStatusL = ValidaValidaSatusL_Res.isEmpty();	
			if (!validaStatusL) {
					testCase.addBoldTextEvidenceCurrentStep("SE Verifico que existe la plaza y la tienda en la tabla WM_BUZONES_T_TIENDAS con WM_STATUS_BUZON = 'L'");
					} 
			testCase.addQueryEvidenceCurrentStep(ValidaValidaSatusL_Res);
			System.out.println(validaStatusL);

			assertFalse(validaStatusL, "No se encontro registro de la plaza y tienda");
	
//**********************************************Paso 3-4	****************************************************************************
						
			addStep("Ejecutar el JOB runCT1Buzon desde Control M para la ejecucion del job runCT1Buzon ");
			
			SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
			
			JSONObject obj = new JSONObject(data.get("job"));

			testCase.addBoldTextEvidenceCurrentStep("Jobs en  Control M ");
			Control_mInicio CM = new Control_mInicio(u, data.get("user"), data.get("ps"));
			//testCase.addPaso("Paso con addPaso");
			testCase.addBoldTextEvidenceCurrentStep("Login");
			u.get(data.get("server"));
			u.hardWait(40);
			u.waitForLoadPage();
			CM.logOn(); 

			//Buscar del job
			testCase.addBoldTextEvidenceCurrentStep("Inicio de job ");
			ControlM control = new ControlM(u, testCase, obj);
			boolean flag = control.searchJob();
			assertTrue(flag);
			
			//Ejecucion
			String resultado = control.executeJob();
			System.out.println("Resultado de la ejecucion -> " + resultado);

			u.hardWait(30);
			
			//Valor del output 
			

			String Res2 = control.getNewStatus();
			

			System.out.println ("Valor de output getNewStatus:" +Res2);
			
			
			//String output = control.getOutput();
			//System.out.println ("Valor de output control:" +output);
			
			testCase.addTextEvidenceCurrentStep("Status de ejecucion: "+Res2);
			//testCase.addTextEvidenceCurrentStep("Output de ejecucion: "+output);

			//Validacion del caso
			Boolean casoPasado = false;
			if(Res2.equals("Ended OK")) {
			casoPasado = true;
			}		
			
			control.closeViewpoint();
			u.close();
			assertTrue(casoPasado);
			//assertNotEquals("Failure",resultado);
			
//**********************************************Paso 5	****************************************************************************
//	Establecer conexi贸n con BD "FCWMLQA*
			
//**********************************************Paso 6	****************************************************************************
			addStep("Validar que la interface CT01 haya finalizado correctamente en la tabla WM_LOG_RUN en la BD *FCMWQA_PERU*");
			
			System.out.println(ValidaStatusS);
			SQLResult ValidaStatusS_Res = executeQuery(dbLog, ValidaStatusS);
			
			String run_id="";
			boolean validaStatusS_Res = ValidaStatusS_Res.isEmpty();
			if(!validaStatusS_Res) {
				run_id = ValidaStatusS_Res.getData(0, run_id);
				System.out.println("run_id: "+run_id);
				testCase.addBoldTextEvidenceCurrentStep("Se valida que la interface finaliz贸 correctamente en la tabla WM_LOG_RUN en la BD FCMWQA_PERU");
			}
			testCase.addQueryEvidenceCurrentStep(ValidaStatusS_Res);
			assertFalse(validaStatusS_Res, "No se valido que la interface finaliz贸 correctamente en la tabla WM_LOG_RUN");
	
//**********************************************Paso 7	****************************************************************************
			addStep("Verificar que los threads de la interfaz finalizaron correctamente.");
			System.out.println("Consulta Threads : " + ValidaThreads);
			
			String consulta1 = String.format(ValidaThreads, run_id); 
			SQLResult consultaThread1 = dbLog.executeQuery(consulta1);
			
			boolean threads = consultaThread1.isEmpty();
			if (!threads) {
				testCase.addBoldTextEvidenceCurrentStep("Se valida que los threads de la interface terminaron correctamente.");
			}
			testCase.addQueryEvidenceCurrentStep(consultaThread1);
			assertFalse(threads,"No se generaron threads en la tabla");
			
//**********************************************Paso 8	****************************************************************************
			addStep("Validar que no se hayan generado errores de la ejecucion de la interface CT01 en la tabla WM_LOG_ERROR de la BD *FCMWQA_Peru*.");
			String validaError = String.format(ValidaError, run_id);
			SQLResult ValidaError_Res = dbLog.executeQuery(validaError);
			
			boolean validacionError = ValidaError_Res.isEmpty();
			
			if(!validacionError) {
				testCase.addBoldTextEvidenceCurrentStep("Se valida que no se generaron errores en la ejecucion de la interface CT01.");
			}
			testCase.addQueryEvidenceCurrentStep(ValidaError_Res);
			assertTrue(validacionError, "Se encontro un error en la ejecucion de la interfaz");

			
//**********************************************Paso 9	****************************************************************************
			/* Establecer la conexi贸n con filezilla con el servidor FTP de los buzones de tienda.
			 * PATH=/u01/posuser/FEMSA_OXXO/POS/[PLAZA]/[TIENDA]/ DIRECTORIO=backup,working,recovery,outbox,duplicate.
			 * Preguntar info acerca de cual es el directorio que buscamos en el filesystem y credenciales de acceso.
			 * */
//**********************************************Paso 11	****************************************************************************
			
			addStep("Verificar que se actualice el registro de la tabla WM_BUZONES_T_TIENDAS con Status E.");
			
			String verificarStatusE = String.format(ValidaStatusE, run_id);
			SQLResult ValidaStatusE_Res = dbCNT.executeQuery(verificarStatusE);
			
			boolean validaStatusE = ValidaStatusE_Res.isEmpty();
			if (!validaStatusE) {
				testCase.addBoldTextEvidenceCurrentStep("Se verifica que el registro de la tabla se actualizo correctamente.");
			}
			testCase.addQueryEvidenceCurrentStep(ValidaStatusE_Res);
			assertFalse(validaStatusE, "No se actualizo el regustro en la tabla WM_Buzones_T_Tiendas.");
			
//**********************************************Paso 12	****************************************************************************

addStep("Verificar que existan  la plaza y la tienda en la tabla WM_BUZONES_T_TIENDAS con WM_STATUS_BUZON = 'L' en la BD *XXCNT_WMUSER*. ");
			
			SQLResult ValidaValidaSatusL_Res2 = executeQuery(dbCNT, ValidaSatusL);
			
			boolean validaStatusL2 = ValidaValidaSatusL_Res2.isEmpty();	
			if (!validaStatusL2) {
					testCase.addBoldTextEvidenceCurrentStep("Se Verifico que existe la plaza y la tienda en la tabla WM_BUZONES_T_TIENDAS con WM_STATUS_BUZON = 'L'");
					} 
			testCase.addQueryEvidenceCurrentStep(ValidaValidaSatusL_Res2);
			System.out.println(validaStatusL2);

			assertFalse(validaStatusL2, "No se encontro registro de la plaza y tienda");
			
//**********************************************Paso 13-14	****************************************************************************
	
	addStep("Ejecutar el JOB runCT1Buzon desde Control M para la ejecucion del job runCT1Buzon ");
			
			testCase.addBoldTextEvidenceCurrentStep("Jobs en Control M ");
			//testCase.addPaso("Paso con addPaso");
			testCase.addBoldTextEvidenceCurrentStep("Login");
			u.get(data.get("server"));
			u.hardWait(40);
			u.waitForLoadPage();
			CM.logOn(); 
			System.out.println("Log on completed");

			//Buscar del job
			testCase.addBoldTextEvidenceCurrentStep("Inicio de job ");
			assertTrue(flag);
			
			//Ejecucion
			String resultado2 = control.executeJob();
			System.out.println("Resultado de la ejecucion -> " + resultado2);
			u.hardWait(30);
			
			//Valor del output 
			String Res2b= control.getNewStatus();
			System.out.println ("Valor de output getNewStatus:" +Res2b);
		
			//Validacion del caso
			Boolean casoPasado2 = false;
			if(Res2.equals("Ended OK")) {
			casoPasado2 = true;
			}		
			
			control.closeViewpoint();
			u.close();
			assertTrue(casoPasado2);
			assertNotEquals(resultado2,"Failure");
			
//**********************************************Paso 16	****************************************************************************
			addStep("Validar que la interface  CT01 haya finalizado correctamente en la tabla WM_LOG_RUN en la BD *FCMWQA_PERU*.");
			
			SQLResult ConsultaStatusS_Res = executeQuery(dbLog, ConsultaStatusS);
			boolean consultaStatusS = ConsultaStatusS_Res.isEmpty();
			String run_id2="";
			if(!consultaStatusS) {
				run_id2 = ConsultaStatusS_Res.getData(0, run_id);
				System.out.println("RUN_ID2 : " + run_id2);
				testCase.addBoldTextEvidenceCurrentStep("Se valida que la interface CT01 finalizo correctamente en la tabla WM_LOG_RUN");
			}
			testCase.addQueryEvidenceCurrentStep(ConsultaStatusS_Res);
			assertFalse(consultaStatusS, "La interface CT01 no finalizo correctamente");
			
//**********************************************Paso 17	****************************************************************************
			
			addStep("Verificar que los threads de la interfaz finalizaron correctamente.");
			System.out.println("Consulta Threads : " + ValidaThreads);
			
			String consulta2 = String.format(ValidaThreads, run_id2); 
			SQLResult consultaThread2 = dbLog.executeQuery(consulta2);
			
			boolean threads2 = consultaThread2.isEmpty();
			if (!threads2) {
				testCase.addBoldTextEvidenceCurrentStep("Se valida que los threads de la interface terminaron correctamente.");
			}
			testCase.addQueryEvidenceCurrentStep(consultaThread2);
			assertFalse(threads2,"No se generaron threads en la tabla");
		
//**********************************************Paso 18	****************************************************************************
			addStep("Validar que no se hayan generado errores de la ejecucion de la interface CT01 en la tabla WM_LOG_ERROR de la BD *FCMWQA_Peru*.");
			String validaError2 = String.format(ValidaError, run_id2);
			SQLResult ValidaError_Res2 = dbLog.executeQuery(validaError2);
			
			boolean validacionError2 = ValidaError_Res2.isEmpty();
			
			if(!validacionError2) {
				testCase.addBoldTextEvidenceCurrentStep("Se valida que no se generaron errores en la ejecucion de la interface CT01.");
			}
			testCase.addQueryEvidenceCurrentStep(ValidaError_Res2);
			assertTrue(validacionError2, "Se encontro un error en la ejecucion de la interfaz");
			
//**********************************************Paso 19	****************************************************************************
			addStep("Verificar que se actualice el registro de la tabla WM_BUZONES_T_TIENDAS, WM_STATUS = E,WM_RUN_ID =[WM_LOG_THREAD.THREAD_ID], WM_FECHA_PROC=SYSDATE.");
			
			String verificarStatusE2 = String.format(ValidaStatusE, run_id2);
			SQLResult ValidaStatusE_Res2 = dbCNT.executeQuery(verificarStatusE2);
			
			boolean validaStatusE2 = ValidaStatusE_Res2.isEmpty();
			if (!validaStatusE2) {
				testCase.addBoldTextEvidenceCurrentStep("Se verifica que el registro de la tabla se actualizo correctamente.");
			}
			testCase.addQueryEvidenceCurrentStep(ValidaStatusE_Res2);
			assertFalse(validaStatusE2, "No se actualizo el regustro en la tabla WM_Buzones_T_Tiendas.");
	
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

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "MTC-FT-001 CT01 Generaci髇 de buzones de tiendas y Parthners de Tranding Network a traves de la interface FEMSA_CT01";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "Equipo automatizacion";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
