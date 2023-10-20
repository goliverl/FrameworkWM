package interfaces.pe2;

import java.io.IOException;
import java.util.HashMap;
import org.testng.annotations.Test;

import com.jcraft.jsch.JSchException;

import modelo.BaseExecution;
import om.PE6;
import util.GlobalVariables;
import util.SSHConnector;
import utils.password.PasswordUtil;
import utils.sql.SQLResult;


public class PE2PreRequisitos extends BaseExecution {
	 private static final String USERNAME = PasswordUtil.decryptPassword("B154EF642C53C68266902033F242CC52");
	    private static final String HOST = "10.184.80.19";
	    private static final int PORT = 22;
	    private static final String PASSWORD =  PasswordUtil.decryptPassword("E1D7C6C1F1080EC9B01E3EC18DF1BC77");
	@Test(dataProvider = "data-provider")
	public void ATC_FT_009_PE2_Valida_Configuracion_Ambientes(HashMap<String, String> data) throws Exception {
		utils.sql.SQLUtil dbFCTDCQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA,GlobalVariables.DB_USER_FCTDCQA, GlobalVariables.DB_PASSWORD_FCTDCQA);
	
		PE6 pe6Util = new PE6(data, testCase, null);
		boolean resumen=false;
		// consultas
		String consultaBin = "SELECT DISTINCT PROM_TYPE, BIN,BANK,ACQUIRER,entry_mode, track1, card_type FROM TPEUSER.TDC_TRANSACTION WHERE track1 = '%s'";//FCTDCQA
		String consultaAPPLY_DEP = "SELECT BIN, STATUS, BANK_ID, BANK_DESC, BANK_CODE, APPLY_DEP,CASHBACK from  TPEUSER.TDC_BIN where bin='%s'";//FCTDCQA
		String consultaProc_code = "SELECT * FROM TPEUSER.TDC_ROUTING WHERE PROC_CODE='PAY' and BIN = '%s'";//FCTDCQA
		String tiposDeTransacciones ="select distinct prom_type from TPEUSER.TDC_TRANSACTION where bin='%s' and prom_type is not null and track1 = '%s'";//FCTDCQA
		String consultaTarjetaBloqueada = "select * from TPEUSER.TDC_BLACK_LIST where track1='%s'";//FCTDCQA
		String consultaCuentaBloqueada = "select CREATION_DATE,CARD_NO,BLOQUED_TIME,PLAZA from TPEUSER.TDC_BLOQUED_ACCOUNT where card_no='%s'";//FCTDCQA
		String consultaTiendaBloqueada = "SELECT creation_date,store, card_no, bloqued_time FROM TPEUSER.TDC_BLOQUED_STORE WHERE card_no = '%s'";//FCTDCQA
		// consulta para verificar que no se registraron errores en la transaccion
	
		boolean tipo =pe6Util.tipoDePrueba(dbFCTDCQA);
		//testCase.setProject_Name("Configuracion de ambientes.");
		addStep("RESUMEN");
		//paso 1
		testCase.addBoldTextEvidenceCurrentStep("Encontrar los tipos de transacciones que el Bin de la tarjeta= '"+data.get("cardNo")+"'en la tabla TPEUSER.TDC_TRANSACTION.");
				String cardNo=data.get("cardNo");
				String executeConsultaBin = String.format(consultaBin, cardNo);
				System.out.println(executeConsultaBin);
				SQLResult resultadoBin = dbFCTDCQA.executeQuery(executeConsultaBin);
		        String bin = resultadoBin.getData(0, "BIN");
				boolean validaBin = resultadoBin.isEmpty();

				System.out.println(validaBin);

				if (!validaBin) {
					testCase.addTextEvidenceCurrentStep("Nota: El bin son los primeros 6 digitos de la tarjeta introducida.");
					testCase.addTextEvidenceCurrentStep("-Bin encontrado: "+bin);
					//testCase.addQueryEvidenceCurrentStep(resultadoBin);
				}else {
					testCase.addTextEvidenceCurrentStep("-Bin no encontrado");                                

				}
				//paso1.2

				String executeConsultaTiposTransaccion = String.format(tiposDeTransacciones, bin,cardNo);
				System.out.println(executeConsultaTiposTransaccion);
				SQLResult resultadoTransaccionesBin = dbFCTDCQA.executeQuery(executeConsultaTiposTransaccion);
				String tipoTrans="";
				for(int x=0;x<resultadoTransaccionesBin.getRowCount();x++) {
					tipoTrans=resultadoTransaccionesBin.getData(x, "prom_type");
					if(tipoTrans.equals("PAY")) {
						testCase.addTextEvidenceCurrentStep("-Este bin esta configurado para corresponsalias.");
					}else if(tipoTrans.equals("PTS")) {
						testCase.addTextEvidenceCurrentStep("-Este bin puede realizar transacciones con puntos.");
					}else if(tipoTrans.equals("CAB")) {
						testCase.addTextEvidenceCurrentStep("-Este bin esta configurado para retiros.");
					}else if(tipoTrans.equals("REG")) {
						testCase.addTextEvidenceCurrentStep("-Este bin  esta configurado para compras con tarjeta.");

					}
				}
				//paso 2
				testCase.addBoldTextEvidenceCurrentStep("Validar que la tarjeta puede realizar depositos y retiros, verificando que el campo 'App_dep' y 'cashback' esten en Y en la tabla TPEUSER.TDC_BIN.");
				String executeconsultaAPPLY_DEP = String.format(consultaAPPLY_DEP, bin);
				System.out.println(executeconsultaAPPLY_DEP);

				SQLResult resultadoExecuteConsultaAPPLY_DEP = dbFCTDCQA.executeQuery(executeconsultaAPPLY_DEP);
		        String APPLY_DEP = resultadoExecuteConsultaAPPLY_DEP.getData(0, "APPLY_DEP");
				boolean validaApply_dep = APPLY_DEP.equals("Y");
				String CASHBACK=resultadoExecuteConsultaAPPLY_DEP.getData(0, "CASHBACK");
				boolean validaCASHBACK = CASHBACK.equals("Y");
				System.out.println(validaApply_dep);


				if (validaApply_dep) {
					testCase.addTextEvidenceCurrentStep("-La tarjeta puede realizar depositos.");                             
					//testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaAPPLY_DEP);

				}else {
					testCase.addTextEvidenceCurrentStep("-La tarjeta no puede realizar depositos.");                                
					//testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaAPPLY_DEP);

				}
				if (validaCASHBACK) {
					testCase.addTextEvidenceCurrentStep("-La tarjeta puede realizar retiros.");                             

				}else {
					testCase.addTextEvidenceCurrentStep("-La tarjeta no puede realizar retiros.");                                

				}
				//paso 3
				testCase.addBoldTextEvidenceCurrentStep("Valida que la tarjeta pueda realizar pagos, verificando que el campo 'Proc_code' este en PAY y verificar si la tarjeta aplica en todas las plazas y tiendas comprobando "
						+ "que los campos 'PLAZA' Y 'TIENDA' contengan un '*' en la tabla TPEUSER.TDC_ROUTING.");
				String executeconsultaProc_code = String.format(consultaProc_code, bin);
				System.out.println(executeconsultaProc_code);
				SQLResult resultadoExecuteConsultaProc_code = dbFCTDCQA.executeQuery(executeconsultaProc_code);
		        String Proc_code = resultadoExecuteConsultaProc_code.getData(0, "Proc_code");
				boolean validaProc_code = Proc_code.equals("PAY");
		        String plaza = resultadoExecuteConsultaProc_code.getData(0, "PLAZA");
				boolean validaPlaza = plaza.equals("*");
				String tienda = resultadoExecuteConsultaProc_code.getData(0, "PLAZA");
				boolean validaTienda = tienda.equals("*");
		        String banco = resultadoExecuteConsultaProc_code.getData(0, "ENTITY");

				System.out.println(validaProc_code);
				
				if (validaProc_code) {
					testCase.addTextEvidenceCurrentStep("-La tarjeta esta configurada para corresponsalias.");                                
					//testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaProc_code);
		 
				}else {
					testCase.addTextEvidenceCurrentStep("-La tarjeta no esta configurada para corresponsalias.");                                
					//testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaProc_code);

				}
				if (validaPlaza) {
					testCase.addTextEvidenceCurrentStep("-La tarjeta aplica en todas las plazas.");                                
		 
				}else {
					testCase.addTextEvidenceCurrentStep("-La tarjeta no aplica en todas las plazas.");                                

				}
				if (validaTienda) {
					testCase.addTextEvidenceCurrentStep("-La tarjeta aplica en todas las tiendas.");                                
		 
				}else {
					testCase.addTextEvidenceCurrentStep("-La tarjeta no aplica en todas las tiendas.");                                

				}
		
				if(tipo) {		
					System.out.println("La tarjeta no esta bloqueada");
					ssh(banco,false);
					
		}else {
					System.out.println("La tarjeta esta bloqueada");
		
					// paso 4
					testCase.addBoldTextEvidenceCurrentStep("Valida que la tarjeta esta en la lista negra en la tabla TPEUSER.TDC_BLACK_LIST.");
					String executeConsultaTarjetaBloqueada = String.format(consultaTarjetaBloqueada, cardNo);
					System.out.println(executeConsultaTarjetaBloqueada);
					SQLResult resultadoExecuteConsultaTarjetaBloqueada = dbFCTDCQA.executeQuery(executeConsultaTarjetaBloqueada);
					boolean validaTarjeta = resultadoExecuteConsultaTarjetaBloqueada.isEmpty();
					System.out.println(validaTarjeta);

					if (!validaTarjeta) {
						testCase.addTextEvidenceCurrentStep("-La tarjeta esta registrada en la lista negra por lo tanto esta bloqueada.");
						//testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaTarjetaBloqueada);

					} else {
						testCase.addTextEvidenceCurrentStep("-La tarjeta no esta registrada en la lista negra por lo tanto no esta bloqueada.");
						//testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaTarjetaBloqueada);

					}
					// paso 5
					testCase.addBoldTextEvidenceCurrentStep("Valida que la tarjeta ha sido bloqueada en las tiendas en la tabla TPEUSER.TDC_BLOQUED_STORE.");
					String executeCuentaTiendaBloqueada = String.format(consultaTiendaBloqueada, cardNo);
					System.out.println(executeCuentaTiendaBloqueada);
					SQLResult resultadoExecuteConsultaTiendaBloqueada = dbFCTDCQA.executeQuery(executeCuentaTiendaBloqueada);
					boolean validaTiendaBloqueada = resultadoExecuteConsultaTiendaBloqueada.isEmpty();
					System.out.println(validaTiendaBloqueada);

					if (!validaTiendaBloqueada) {
						testCase.addTextEvidenceCurrentStep("-La tarjeta ha sido registrada como bloqueada en las tiendas.");
						//testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaTiendaBloqueada);

					} else {
						testCase.addTextEvidenceCurrentStep("-La tarjeta no ha sido registrada como bloqueada en las tiendas.");
						//testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaTiendaBloqueada);

					}
					// paso 6
					testCase.addBoldTextEvidenceCurrentStep("Valida que la tarjeta esta registrada con una cuenta bloqueada en la tabla TPEUSER.TDC_BLOQUED_ACCOUNT.");
					String executeCuentaTarjetaBloqueada = String.format(consultaCuentaBloqueada, cardNo);
					System.out.println(executeCuentaTarjetaBloqueada);
					SQLResult resultadoExecuteConsultaCuentaBloqueada = dbFCTDCQA.executeQuery(executeCuentaTarjetaBloqueada);
					boolean validaCuenta = resultadoExecuteConsultaCuentaBloqueada.isEmpty();
					System.out.println(validaCuenta);

					if (!validaCuenta) {
						testCase.addTextEvidenceCurrentStep("-La tarjeta esta registrada con una cuenta bloqueada.");
						//testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaCuentaBloqueada);

					} else {
						testCase.addTextEvidenceCurrentStep("-La tarjeta no esta registrada con una cuenta bloqueada.");
						//testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaCuentaBloqueada);

					}
					ssh(banco,false);
					
		}
				testCase.addBoldTextEvidenceCurrentStep("Evidencia: ");

				//******************************************************FINAL RESUMEN***************************************************************************
				resumen=true;
				addStep("Conectarse a la base de datos FCTDCQA.");

				testCase.addTextEvidenceCurrentStep("Conexion a bd correcta: " + GlobalVariables.DB_HOST_FCTDCQA + " usuario: "
						+ GlobalVariables.DB_USER_FCTDCQA);
				testCase.addTextEvidenceCurrentStep("Conexion exitosa: " + dbFCTDCQA.getConn());
				testCase.setProject_Name("Configuracion de ambientes.");
				//paso 1
						addStep("Encontrar los tipos de transacciones que el Bin de la tarjeta= '"+data.get("cardNo")+"' puede realizar mediante la siguiente consulta en la tabla TPEUSER.TDC_TRANSACTION.");
					

						System.out.println(validaBin);

						if (!validaBin) {
							testCase.addTextEvidenceCurrentStep("Nota: El bin son los primeros 6 digitos de la tarjeta introducida.");
							testCase.addTextEvidenceCurrentStep("-Bin encontrado: "+bin);
							testCase.addQueryEvidenceCurrentStep(resultadoBin);
						}else {
							testCase.addTextEvidenceCurrentStep("-Bin no encontrado");                                

						}
						//paso1.2

						for(int x=0;x<resultadoTransaccionesBin.getRowCount();x++) {
							tipoTrans=resultadoTransaccionesBin.getData(x, "prom_type");
							if(tipoTrans.equals("PAY")) {
								testCase.addTextEvidenceCurrentStep("-Este bin esta configurado para corresponsalias.");
							}else if(tipoTrans.equals("PTS")) {
								testCase.addTextEvidenceCurrentStep("-Este bin puede realizar transacciones con puntos.");
							}else if(tipoTrans.equals("CAB")) {
								testCase.addTextEvidenceCurrentStep("-Este bin esta configurado para retiros.");
							}else if(tipoTrans.equals("REG")) {
								testCase.addTextEvidenceCurrentStep("-Este bin  esta configurado para compras con tarjeta.");

							}
						}
						//paso 2
						addStep("Validar que la tarjeta puede realizar depositos y retiros, verificando que el campo 'App_dep' y 'cashback' esten en Y en la tabla TPEUSER.TDC_BIN.");
					


						if (validaApply_dep) {
							testCase.addTextEvidenceCurrentStep("-La tarjeta puede realizar depositos.");                             
							testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaAPPLY_DEP);

						}else {
							testCase.addTextEvidenceCurrentStep("-La tarjeta no puede realizar depositos.");                                
							testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaAPPLY_DEP);

						}
						if (validaCASHBACK) {
							testCase.addTextEvidenceCurrentStep("-La tarjeta puede realizar retiros.");                             

						}else {
							testCase.addTextEvidenceCurrentStep("-La tarjeta no puede realizar retiros.");                                

						}
						//paso 3
						addStep("Valida que la tarjeta pueda realizar pagos, verificando que el campo 'Proc_code' este en PAY y verificar si la tarjeta aplica en todas las plazas y tiendas comprobando "
								+ "que los campos 'PLAZA' Y 'TIENDA' contengan un '*' en la tabla TPEUSER.TDC_ROUTING.");
						

						System.out.println(validaProc_code);
						
						if (validaProc_code) {
							testCase.addTextEvidenceCurrentStep("-La tarjeta esta configurada para corresponsalias.");                                
							testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaProc_code);
				 
						}else {
							testCase.addTextEvidenceCurrentStep("-La tarjeta no esta configurada para corresponsalias.");                                
							testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaProc_code);

						}
						if (validaPlaza) {
							testCase.addTextEvidenceCurrentStep("-La tarjeta aplica en todas las plazas.");                                
				 
						}else {
							testCase.addTextEvidenceCurrentStep("-La tarjeta no aplica en todas las plazas.");                                

						}
						if (validaTienda) {
							testCase.addTextEvidenceCurrentStep("-La tarjeta aplica en todas las tiendas.");                                
				 
						}else {
							testCase.addTextEvidenceCurrentStep("-La tarjeta no aplica en todas las tiendas.");                                

						}
				
						if(tipo) {		
							System.out.println("La tarjeta no esta bloqueada");
							ssh(banco,resumen);
							
				}else {
							System.out.println("La tarjeta esta bloqueada");
				
							// paso 4
							addStep("Valida que la tarjeta esta en la lista negra con la siguiente consulta en la tabla TPEUSER.TDC_BLACK_LIST.");
							String executeConsultaTarjetaBloqueada = String.format(consultaTarjetaBloqueada, cardNo);
							System.out.println(executeConsultaTarjetaBloqueada);
							SQLResult resultadoExecuteConsultaTarjetaBloqueada = dbFCTDCQA.executeQuery(executeConsultaTarjetaBloqueada);
							boolean validaTarjeta = resultadoExecuteConsultaTarjetaBloqueada.isEmpty();
							System.out.println(validaTarjeta);

							if (!validaTarjeta) {
								testCase.addTextEvidenceCurrentStep("-La tarjeta esta registrada en la lista negra por lo tanto esta bloqueada.");
								testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaTarjetaBloqueada);

							} else {
								testCase.addTextEvidenceCurrentStep("-La tarjeta no esta registrada en la lista negra por lo tanto no esta bloqueada.");
								testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaTarjetaBloqueada);

							}
							// paso 5
							addStep("Valida que la tarjeta ha sido bloqueada en las tiendas con la siguiente consulta en la tabla TPEUSER.TDC_BLOQUED_STORE.");
							String executeCuentaTiendaBloqueada = String.format(consultaTiendaBloqueada, cardNo);
							System.out.println(executeCuentaTiendaBloqueada);
							SQLResult resultadoExecuteConsultaTiendaBloqueada = dbFCTDCQA.executeQuery(executeCuentaTiendaBloqueada);
							boolean validaTiendaBloqueada = resultadoExecuteConsultaTiendaBloqueada.isEmpty();
							System.out.println(validaTiendaBloqueada);

							if (!validaTiendaBloqueada) {
								testCase.addTextEvidenceCurrentStep("-La tarjeta ha sido registrada como bloqueada en las tiendas.");
								testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaTiendaBloqueada);

							} else {
								testCase.addTextEvidenceCurrentStep("-La tarjeta no ha sido registrada como bloqueada en las tiendas.");
								testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaTiendaBloqueada);

							}
							// paso 6
							addStep("Valida que la tarjeta esta registrada con una cuenta bloqueada con la siguiente consulta en la tabla TPEUSER.TDC_BLOQUED_ACCOUNT.");
							String executeCuentaTarjetaBloqueada = String.format(consultaCuentaBloqueada, cardNo);
							System.out.println(executeCuentaTarjetaBloqueada);
							SQLResult resultadoExecuteConsultaCuentaBloqueada = dbFCTDCQA.executeQuery(executeCuentaTarjetaBloqueada);
							boolean validaCuenta = resultadoExecuteConsultaCuentaBloqueada.isEmpty();
							System.out.println(validaCuenta);

							if (!validaCuenta) {
								testCase.addTextEvidenceCurrentStep("-La tarjeta esta registrada con una cuenta bloqueada.");
								testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaCuentaBloqueada);

							} else {
								testCase.addTextEvidenceCurrentStep("-La tarjeta no esta registrada con una cuenta bloqueada.");
								testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaCuentaBloqueada);

							}
							ssh(banco,resumen);
							
				}
	}
	
public void ssh(String banco,boolean resumen) {
	if(resumen==true) {
		addStep("Establecer conexion con sevicio SSH para validar configuracion del channel.");

	}else {
		testCase.addBoldTextEvidenceCurrentStep("Establecer conexion con sevicio SSH para validar configuracion del channel.");

	}
	String bancoToLowerCase="";
	bancoToLowerCase = banco.toLowerCase();	
	//testCase.addTextEvidenceCurrentStep("Banco a buscar: '"+bancoToLowerCase+"'");
	System.out.println(bancoToLowerCase);

//ssh
 try {
     SSHConnector sshConnector = new SSHConnector();
     sshConnector.connect(USERNAME, PASSWORD, HOST, PORT);
     if(sshConnector.isConected()) {
			testCase.addTextEvidenceCurrentStep("-La conexion ssh a la direccion "+HOST+" fue exitosa. ");
			if(resumen==true) {
				addStep("Buscar la configuracion del channel del banco '"+banco+"'.");

			}else {
				testCase.addBoldTextEvidenceCurrentStep("Buscar la configuracion del channel del banco '"+banco+"'.");

			}
		    String result = sshConnector.executeCommand("cd /u01/wmuser/webMethods9/IntegrationServer/instances/default/jpos/deploy; cat 10_channel_"+bancoToLowerCase+".xml");
			System.out.println("respuesta= "+result);
			 if(result=="") {
					testCase.addTextEvidenceCurrentStep("-No se encuentra el channel del banco configurado.");
					testCase.addTextEvidenceCurrentStep("-El documento 10_channel_"+bancoToLowerCase+".xml no existe");

			 }else {
					testCase.addTextEvidenceCurrentStep("-Channel encontrado: ");
					testCase.addTextEvidenceCurrentStep("10_channel_"+bancoToLowerCase+".xml.-");

					testCase.addTextEvidenceCurrentStep(result);

			 }
		        sshConnector.disconnect();

     }else{
			testCase.addTextEvidenceCurrentStep("-La conexion ssh a la direccion "+HOST+" no fue exitosa, verifica que el dispositivo esta disponible o si estas autorizado para conectarte.");

     }
 
 } catch (JSchException ex) {
     ex.printStackTrace();
      
     System.out.println(ex.getMessage());
 } catch (IllegalAccessException ex) {
     ex.printStackTrace();
      
     System.out.println(ex.getMessage());
 } catch (IOException ex) {
     ex.printStackTrace();
      
     System.out.println(ex.getMessage());
 }
}
		
	@Override
	public String setTestFullName() {
		return "ATC_FT_009_PE2_Valida_Configuracion_Ambientes";
	}

	@Override
	public String setTestDescription() {
		return "Terminado. Valida la configuracion de ambientes para pruebas con tarjetas";
	}

	@Override
	public String setTestDesigner() {
		return "tbd";
	}

	@Override
	public String setTestInstanceID() {
		return "-1";                               
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
