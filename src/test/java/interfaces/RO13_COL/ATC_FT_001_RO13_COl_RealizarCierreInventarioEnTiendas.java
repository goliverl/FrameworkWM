package interfaces.RO13_COL;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import util.GlobalVariables;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import utils.sql.SQLUtil;
import utils.sql.SQLResult;


public class ATC_FT_001_RO13_COl_RealizarCierreInventarioEnTiendas extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_RO13_COl_RealizarCierreDeInventarioEnTiendas_test(HashMap<String, String> data) throws Exception {
		
		/*
		 * Utilerias
		 ***********************************************************************************************/
		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_COL_QAVIEW,GlobalVariables.DB_USER_RMS_COL_QAVIEW, GlobalVariables.DB_PASSWORD_RMS_COL_QAVIEW);
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,GlobalVariables. DB_PASSWORD_AVEBQA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		/**
		 * Variables
		 * ***********************************************************************************************
		 * 
		 * 
		 */
		
		
		//Fecha
				Date fecha = new Date();
				SimpleDateFormat formatterfecha = new SimpleDateFormat("dd-MMM-yy HH:mm:ss"); 
				formatterfecha.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));	
				String date = formatterfecha.format(fecha);
				System.out.println(date);
		
		String InsertCierre="Insert into XXFC.XXFC_CO_INVENTARIOS_FISICOS "
				+ "(ID_INVENTARIO_FISICO,MOVIMIENTO,RETEK_CR,FECHA,TECLA1,TECLA2,TECLA3,TECLA4,TECLA5,TECLA6,"
				+ "IEPS,IVA_TECLA3,IVA_TECLA4,IVA_TECLA5,IVA_TECLA6,SUBTOTAL,GRAN_TOTAL,CONCEPTO,ATRIBUTO1,"
				+ "ATRIBUTO2,ATRIBUTO3,ATRIBUTO4,ATRIBUTO5,ACTUALIZADO,ESTADO,CREATED_BY,CREATION_DATE,"
				+ "LAST_UPDATED_BY,LAST_UPDATE_DATE,LAST_UPDATE_LOGIN,IMPEST) "
				+ "values ("+data.get("ID_INVENTARIO_FISICO")+","
				+data.get("MOVIMIENTO")+",'"
				+data.get("RETEK_CR")+"',"
				+data.get("FECHA")+","
				+data.get("TECLA1")+","+
				"-"+data.get("TECLA2")+","
				+data.get("TECLA3")+","
				+data.get("TECLA4")+","
				+data.get("TECLA5")+","
				+data.get("TECLA6")+","
				+data.get("IEPS")+","
				+data.get("IVA_TECLA3")+","
				+data.get("IVA_TECLA4")+","
				+data.get("IVA_TECLA5")+","
				+data.get("IVA_TECLA6")+","
				+data.get("SUBTOTAL")+","
				+data.get("GRAN_TOTAL")+",'"
				+data.get("CONCEPTO")+"',"
				+data.get("ATRIBUTO1")+","
				+data.get("ATRIBUTO2")+","
				+data.get("ATRIBUTO3")+","
				+data.get("ATRIBUTO4")+","
				+data.get("ATRIBUTO5")+",'"
				+data.get("ACTUALIZADO")+"',"
				+data.get("ESTADO")+","
				+data.get("CREATED_BY")+","
				+data.get("CREATION_DATE")+","
				+data.get("LAST_UPDATED_BY")+","
				+data.get("LAST_UPDATE_DATE")+","
				+data.get("LAST_UPDATE_LOGIN")+","
				+data.get("IMPEST")+")";
		
		
		String ValidMinTrandate ="SELECT a.RETEK_CR, "
				+ "TO_CHAR(GREATEST( NVL(c.maxFechaCierre,b.maxFechaInvFis),NVL(b.maxFechaInvFis,c.maxFechaCierre)),'DDMMYYYY') MIN_TRANDATE "
				+ "FROM  xxfc_maestro_de_crs_v a,  "
				+ "(SELECT MAX(fecha) maxFechaInvFis "
				+ "FROM xxfc_co_inventarios_fisicos "
				+ "WHERE ESTADO IS NULL AND retek_cr='"+data.get("RETEK_CR")+"') b,  "
				+ "(SELECT oracle_plaza, LAST_DAY(MAX(TO_DATE(periodo,'MON-RRRR','NLS_DATE_LANGUAGE = SPANISH'))) maxFechaCierre "
				+ "FROM xxfc_bitacora_de_cierres  "
				+ "WHERE ESTATUS='C' GROUP BY oracle_plaza ) c "
				+ "WHERE a.RETEK_CR= '"+data.get("RETEK_CR")+", "
				+ "AND a.ORACLE_CR_TYPE='T' "
				+ "AND a.ESTADO='A' "
				+ "AND a.oracle_cr_superior =  c.oracle_plaza (+)";
		
		
		String ValidInserc="SELECT STORE,MIN_TRANDATE,CREATED_DATE,LAST_UPDATE_DATE,MIN_TRANDATE_TYPE "
				+ "FROM WMUSER.WM_STORE_MINTRANDATE "
				+ "WHERE STORE = '"+data.get("RETEK_CR")+"' "
				+ "AND TO_CHAR(MIN_TRANDATE, 'DDMMYYYY') = '%s' ";
		
		String LogStatus = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER FROM WMLOG.WM_LOG_RUN "
				+ " WHERE INTERFACE = 'RO13_COL_I' "
				+ "	AND STATUS = 'S' "
				+ "	AND START_DT > TRUNC(SYSDATE) "
				+ "	ORDER BY START_DT DESC ";

		
	
				/**
				 * 
				 * **********************************Pasos del caso de Prueba *******************************************
				 * ******************************************************************************************************
				 * 
				 * 
				 */

//				*************************** Paso 1	************************		

				addStep("Ejecutar el Insert de cierre de inventario de la tienda "+ data.get("tienda")+" de Colombia en "
						+ "la tabla XXFC_CO_INVENTARIOS_FISICOS, con ESTADO = NULL. ");
				
				int resultInsertRegister = dbEbs.executeUpdate(InsertCierre);

				boolean launcherEmpty = true;
				if(resultInsertRegister == 0)
					launcherEmpty = false;
							
				assertTrue(launcherEmpty, "No existe el registro de la ejecucion de la interface.");


//				Paso 2	**************************************************************************************************************************************************

				addStep("Validar la correcta ejecucion de la interface RO13_COL en la tabla WM WM_LOG_RUN de WMLOG.");
				
					System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
					SQLResult RunLog = executeQuery(dbLog, LogStatus);
					System.out.println(LogStatus);

					boolean LogRequest = RunLog.isEmpty();
					
					if (LogRequest) {

							testCase.addBoldTextEvidenceCurrentStep("La ejecucion de la interface RO13_COL no fue correcta, "
									+ "no se encontraron registros de una ejecucion exitosa con la fecha actual");
					} else {
						testCase.addBoldTextEvidenceCurrentStep("Se encontraron registros con estatus S");
					}
					
					testCase.addQueryEvidenceCurrentStep( RunLog);
					System.out.println(LogRequest);
					assertFalse(LogRequest, "No se obtiene informacion de la consulta");
				
			

			
//				Paso 3	************************	
				
				addStep("Consultar la fecha más reciente de operación (MIN_TRANDATE) para la tienda. ");

				System.out.println(GlobalVariables.DB_HOST_EBS_COL);
				SQLResult RunMin = executeQuery(dbEbs, ValidMinTrandate);
				System.out.println(RunMin);
				String MinDate="";
				
				boolean MinRequest = RunMin.isEmpty();
				
				if (MinRequest) {

						testCase.addBoldTextEvidenceCurrentStep("no se encontraron registros de la fecha más reciente de operación "
								+ "(MIN_TRANDATE) para la tienda.");
				} else {
					MinDate = RunMin.getData(0, "MIN_TRANDATE");
					testCase.addBoldTextEvidenceCurrentStep("Se encontraron registros en la BD: ");
				}
				
				testCase.addQueryEvidenceCurrentStep(RunMin);
				
				System.out.println(MinRequest);
				assertFalse(MinRequest, "No se obtiene informacion de la consulta");
				

				// Paso 4 ************************
				addStep("Validar la actualizacion/inserción de la fecha de cierre de inventario de la tienda en RETEK en la tabla WM_STORE_MINTRANDATE. ");

			System.out.println(GlobalVariables.DB_HOST_RMS_COL);
			String FormatValidInser = String.format(ValidInserc, MinDate);
			SQLResult RunValid = executeQuery(dbRms, FormatValidInser);
			System.out.println(FormatValidInser);
		
			
			boolean ValidRequest = RunValid.isEmpty();
			
			if (ValidRequest) {

					testCase.addBoldTextEvidenceCurrentStep("no se encontraron la actualización/inserción de la fecha de cierre "
							+ "de inventario de la tienda en RETEK en la tabla WM_STORE_MINTRANDATE");
			} else {
		
				testCase.addBoldTextEvidenceCurrentStep("Se encontraron registros en la BD: ");
			}
			
			testCase.addQueryEvidenceCurrentStep(RunValid);
			
			System.out.println(ValidRequest);
			assertFalse(ValidRequest, "No se obtiene informacion de la consulta");
			
		
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
		return "Realizar el cierre de inventario de la tienda";
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


}
