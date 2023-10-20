package util;
import java.sql.SQLException;
import utils.password.PasswordUtil;

public class GlobalVariables {
	  
	
	    
	public static final String HOST_QA9 = PasswordUtil.decryptPassword("0ECEDE5C0CB1B29AA651F5740523DBCF922BD823772E99F2F5FF0E6BD63EC5B8");	
	public static final String HOST_QA10_VIRTUAL = PasswordUtil.decryptPassword("727217CB5A1AAC85170D4AE70C1BC550C3B6B3D6A5A106622353563A266DD74E"); 
	public static final String HOST = PasswordUtil.decryptPassword("29A2094820E0289621958423760938E336BF9BBAD8AF44E915521C6E35BBD484");	
	public static final String HOST_OLS = PasswordUtil.decryptPassword("8015BD39B842348D6CC6865F8CD0030DCF49885A6D645B5229C7D1E6043845A4");
	public static final String HOST_FAC = PasswordUtil.decryptPassword("29A2094820E0289621958423760938E3B8CB6D4D516D56D91F21CB67609C1221");
	public static final String HOST_FAC_2 = PasswordUtil.decryptPassword("AFFB124986714F4CA22216628884620CCBF16BE31AF52CD2F3A45E1331A1794E");
	public static final String HOST_RTP = PasswordUtil.decryptPassword("688ED399E2F293E247D3E4EC66703FFD957364B5BC4D9511A432FBBF42AF661F");
	public static final String HOST_PR2 = PasswordUtil.decryptPassword("7E007AAD84985F95EE8C2ABCAD5D7E9F6963A5146A4E4B410B1403E32AF5C3E9");
	public static final String HOST_PR2_CO = "10.182.32.22:7797";
	public static final String HOST_PR50v2 = PasswordUtil.decryptPassword("3A9C8BAE1396BD20ACE4D803F081EC15922BD823772E99F2F5FF0E6BD63EC5B8");
	public static final String HOST_RH = PasswordUtil.decryptPassword("8015BD39B842348D6CC6865F8CD0030DD132306926AD6E0A61CB73606655B2BC");
	public static final String HOST_FTC = PasswordUtil.decryptPassword("E66B1FDA691DD8C82BB18C8B284F288B01393D82DD17AD8F9854B3D1D741356A");
	
	
	//FCIAS/
	public static final String DB_USER_IAS = "portal_view";
	public static final String DB_PASSWORD_IAS = PasswordUtil.decryptPassword("FC040844B14947D896CF6A93CAFA66E0");
	public static final String DB_HOST_IAS = PasswordUtil.decryptPassword("2B07C799DA60299CFAB5022371DD455BAA58B31C17DDB7489D01E819AB4D30C16717993EFECB5015E24E9A5CE874293F");

	//TPEUSER(PE)/FCTDC
	public static final String DB_USER = "wmview";
	public static final String DB_PASSWORD = PasswordUtil.decryptPassword("1959B95F48820415E1D2A66FAB851DB7");
	public static final String DB_HOST = PasswordUtil.decryptPassword("EC0A9E38554ABEA867741A048B57522499C48D25355B4E57D97615334A85DEEE6717993EFECB5015E24E9A5CE874293F");
	
	//PE1/FCTAE
	public static final String DB_USERPE1 = "wmview";
	public static final String DB_PASSWORDPE1 =PasswordUtil.decryptPassword("1959B95F48820415E1D2A66FAB851DB7");
	public static final String DB_HOSTPE1 = PasswordUtil.decryptPassword("EC0A9E38554ABEA867741A048B575224D79CF7217416310C7AFFF14226AAD21E6717993EFECB5015E24E9A5CE874293F");
	
	//PE3
	
	//PE6
	public static final String DB_USERPE6 = "QAVIEW";
	public static final String DB_PASSWORDPE6 = PasswordUtil.decryptPassword("A5544A354006EE384994CACA76D6289B");
	public static final String DB_HOSTPE6 = PasswordUtil.decryptPassword("92A9B1C1830A9C104662425C28C91152E15F2576DA9C0041B0CC41540D20A9CB6717993EFECB5015E24E9A5CE874293F");
	
	//TPE OLS
	public static final String DB_USERPE3 = "wmview";
	public static final String DB_PASSWORDPE3 = PasswordUtil.decryptPassword("1959B95F48820415E1D2A66FAB851DB7");
	public static final String DB_HOSTPE3 = PasswordUtil.decryptPassword("EC0A9E38554ABEA867741A048B575224C2A35DD643DA19B696DAA38A568A15EA6717993EFECB5015E24E9A5CE874293F");
	
	//peru OIWMQA.FEMCOM.NET----------------scripts
	public static final String DB_USER_Oiwmqa = "WMVIEW";
	public static final String DB_PASSWORD_Oiwmqa = PasswordUtil.decryptPassword("D1814B879E2E546C68B5F126DBC7C5CD");
	public static final String DB_HOST_Oiwmqa = "10.184.40.134:1521/OIWMQA.FEMCOM.NET"; 
	
	
	//TABLAS DE USER FAC 
	public static final String DB_USER_FAC = "tpeuser";
	public static final String DB_PASSWORD_FAC = PasswordUtil.decryptPassword("F8B0444F186AADE9448BDB3C81BFE630");
	public static final String DB_HOST_FAC = PasswordUtil.decryptPassword("EC0A9E38554ABEA867741A048B575224EEA8E66C1C824471743A54C8C34B83BE9E41FA08A730B16BB0393CCE9709A415");
	
	public static final String DB_USER_FacQ = "TPEUSER";
	public static final String DB_PASSWORD_FacQ = PasswordUtil.decryptPassword("B1078AF4CE1BF0B567FA583C3F13FA73");
	public static final String DB_HOST_FacQ = PasswordUtil.decryptPassword("8F913BB49AD9F060EEFFB8596C3E27C71F44FC6BE133764097F901BCD1762E48");
	
	
	//Tablas de LOGWM PR2
	
	public static final String DB_USER_LOG = "wmlog";
	public static final String DB_PASSWORD_LOG = PasswordUtil.decryptPassword("A0EBA344FE443EEFCBC8C462E60E340E");
	public static final String DB_HOST_LOG = "10.80.1.119:1521/FCWMLQA";
	
	public static final String DB_USER_Data = "posuser";
	public static final String DB_PASSWORD_Data = PasswordUtil.decryptPassword("25AF5CE3DDE8A89AAE9DFCC87ED144A5");
	public static final String DB_HOST_Data = PasswordUtil.decryptPassword("8F913BB49AD9F060EEFFB8596C3E27C7DB687953290CED79479D18EEFE2D9074");
	
	public static final String DB_USER_DataWmuser = "wmuser";
	public static final String DB_PASSWORD_DataWmuser = PasswordUtil.decryptPassword("4CC61E5F09F4E484DAE869B7B058B2F8");
	public static final String DB_HOST_DataWmuser = PasswordUtil.decryptPassword("8F913BB49AD9F060EEFFB8596C3E27C7DB687953290CED79479D18EEFE2D9074");	
	
	//BDPOSUSER20
	public static final String DB_USER_Puser = "QAVIEW";
	public static final String DB_PASSWORD_Puser = PasswordUtil.decryptPassword("A5544A354006EE384994CACA76D6289B");
	public static final String DB_HOST_Puser = PasswordUtil.decryptPassword("92A9B1C1830A9C104662425C28C91152761EE7F4954ACC6256E7B2BD21B2D0AC6717993EFECB5015E24E9A5CE874293F");
	
	public static final String DB_USER_FCWMQA_UPD ="Hexaware30";
    public static final String DB_PASSWORD_FCWMQA_UPD =  PasswordUtil.decryptPassword("A5D4E206919F323F53429EC8AEA1C18B");
    public static final String DB_HOST_FCWMQA_UPD = "10.184.48.216:1535/FCWM6QA.FEMCOM.NET";
	
	//rms
	public static final String DB_USER_Rms = "RMS100";
	public static final String DB_PASSWORD_Rms = PasswordUtil.decryptPassword("5EED5CABD9338AE6EA71917A17F75E3F");
	public static final String DB_HOST_Rms = PasswordUtil.decryptPassword("04432E3694F0A229E7C95DEDB85D211509297D9B66AE4BF1C2790B50FB55B6CB");
	
	//rmsruebasPR1FCRMSCUT
	public static final String DB_USER_RmsP = "RMS100";
	public static final String DB_PASSWORD_RmsP = PasswordUtil.decryptPassword("5EED5CABD9338AE6EA71917A17F75E3F");
	public static final String DB_HOST_RmsP = "10.184.56.124:1525/FCRMSCUT.FEMCOM.NET";
    
	//EBSPruebaFCFINQA
	public static final String DB_USER_EBS = "wmuser";
	public static final String DB_PASSWORD_EBS =PasswordUtil.decryptPassword("4CC61E5F09F4E484DAE869B7B058B2F8");
	public static final String DB_HOST_EBS = "10.184.56.95:1521/FCFINQA.FEMCOM.NET";
	
	 //EBS FCEBC4Vante 
    public static final String DB_USER_EBS_FCEBC4= "WMUSER";
    public static final String DB_PASSWORD_EBS_FCEBC4= PasswordUtil.decryptPassword("82862214493D97A5794DF801269CBF9F");  
    public static final String DB_HOST_EBS_FCEBC4= "10.80.3.159:1602/fcebc4";
		
	
	
	//wmlostatusRunPR1FCWMLQA
	public static final String DB_USER_FCWMLQA = "QAVIEW";
	public static final String DB_PASSWORD_FCWMLQA =PasswordUtil.decryptPassword("A5544A354006EE384994CACA76D6289B");
	public static final String DB_HOST_FCWMLQA = "10.184.48.217:1535/FCWMLQA.FEMCOM.NET";
	
	//wmlostatusRunPR1FCWMLQA
	public static final String DB_USER_FCWMLQA2 = "QAVIEW";
	public static final String DB_PASSWORD_FCWMLQA2 =PasswordUtil.decryptPassword("A5544A354006EE384994CACA76D6289B");
	public static final String DB_HOST_FCWMLQA2 = "10.184.48.215:1535/FCWMLQA.FEMCOM.NET";
	

	//Tablas RMS mÃƒÂ©xico RMSVIEW
		public static final String DB_USER_MEX ="RMSVIEW";
		public static final String DB_PASSWORD_MEX = PasswordUtil.decryptPassword("6920831A3426764BB7E688FAA3F3F63F");
		public static final String DB_HOST_MEX = "10.184.56.124:1525/FCRMSCUT.FEMCOM.NET";
		

		
	//Tablas FCWMQA_NUEVA (host diferente)
		public static final String DB_USER_FCWMQA_NUEVA ="QAVIEW";
		public static final String DB_PASSWORD_FCWMQA_NUEVA =  PasswordUtil.decryptPassword("A5544A354006EE384994CACA76D6289B");
		public static final String DB_HOST_FCWMQA_NUEVA = "10.184.48.216:1535/fcwm6QA.femcom.net";
	
		
	//Tablas Chile RMs100 	
	public static final String DB_USER_RMSWMUSERChile ="RMS100"; //wmuser
	public static final String DB_PASSWORD_RMSWMUSERChile =  PasswordUtil.decryptPassword("1CD10F979189DA0768DA27AFB5970122"); //E023A372AD3B3237F098A0EC4D27ECE7
	public static final String DB_HOST_RMSWMUSERChile = "10.80.2.222:1541/BDCHRMSQ.FEMCOM.NET";	
	
	   //Tabla RMS100 COLOMBIA
	   
    public static final String DB_USER_RMS_COL="RMS100";
    public static final String DB_PASSWORD_RMS_COL =PasswordUtil.decryptPassword("E294EC6DCC58CB4368980D8CCDD201BD");
    public static final String DB_HOST_RMS_COL = "10.185.48.24:1547/BDRMSCQA.FEMCOM.NET";
    
  //RMS de COL QAVIEW
    public static final String DB_USER_RMS_COL_QAVIEW="QAVIEW";
    public static final String DB_PASSWORD_RMS_COL_QAVIEW =PasswordUtil.decryptPassword("A5544A354006EE384994CACA76D6289B");
    public static final String DB_HOST_RMS_COL_QAVIEW = "10.185.48.24:1547/BDRMSCQA.FEMCOM.NET";
    
  //FCWMLQA_WMLOG_COL
  	public static final String DB_USER_FCWMLQA_WMLOG_COL = "Wmlog";
  	public static final String DB_PASSWORD_FCWMLQA_WMLOG_COL =PasswordUtil.decryptPassword("06DB86BC1C7C3D11C9B9F98F2E5686BE");
  	public static final String DB_HOST_FCWMLQA_WMLOG_COL = "10.184.48.217:1535/FCWMLQA.FEMCOM.NET";

   
    //Tabla RMS100 MEXICO SID: FCRMSQA
   
    public static final String DB_USER_RMS_MEX="QAVIEW";
    public static final String DB_PASSWORD_RMS_MEX =PasswordUtil.decryptPassword("A5544A354006EE384994CACA76D6289B");
    //public static final String DB_HOST_RMS_MEX = "10.184.37.12:1521/FCRMSQA";
    public static final String DB_HOST_RMS_MEX = "10.184.37.12:1521/FCRMSQA.FEMCOM.NET";
	
    //Tabla EBS COLOMBIA SID: FCEBSCO2
    
    public static final String DB_USER_EBS_COL="wmuser";
    public static final String DB_PASSWORD_EBS_COL =PasswordUtil.decryptPassword("4CC61E5F09F4E484DAE869B7B058B2F8");
    public static final String DB_HOST_EBS_COL = "10.80.2.184:1531/FCEBSCO2";
    
	
	//Tablas chile wmLog--
   
	public static final String DB_USER_LogChile ="Wmlog";
	public static final String DB_PASSWORD_LogChile = PasswordUtil.decryptPassword("B8BC60B8008B4BF2A04AA0F2EB9211AB");
	 public static final String DB_HOST_LogChile ="10.186.13.26:1521/OCHWMQA.FEMCOM.NET";
	//Tablas chile posuser--
	
	public static final String DB_USER_PosUserChile ="Posuser";
	public static final String DB_PASSWORD_PosUserChile =PasswordUtil.decryptPassword("2C7E5D93EAF7FF1140C6F1328B7A491A");
	public static final String DB_HOST_PosUserChile = "10.186.13.26:1533/OCHWMQA.FEMCOM.NET";	
	
	//Tabla ORAFIN/EBS
	
	public static final String DB_USER_Ebs ="APPSVIEW";
	public static final String DB_PASSWORD_Ebs =PasswordUtil.decryptPassword("92C4986E2465EDA27D5D04B227BE5FDE");
	public static final String DB_HOST_Ebs = "10.184.56.95:1521/FCFINQA.FEMCOM.NET";	

	//TPUSER
    public static final String DB_USER_FCTPE = "QAVIEW";
    public static final String DB_PASSWORD_FCTPE =PasswordUtil.decryptPassword("A5544A354006EE384994CACA76D6289B");
    public static final String DB_HOST_FCTPE = "10.184.80.120:1521/FCTPEQA.FEMCOM.NET";
    
//    //Temp Rob
//    public static final String DB_USER_FCWMLTAQ_MTY = "QAVIEW";
//    public static final String DB_PASSWORD_FCWMLTAQ_MTY = PasswordUtil.decryptPassword("A5544A354006EE384994CACA76D6289B");
//    public static final String DB_HOST_FCWMLTAQ_MTY = "10.184.80.120:1521/FCWMLTAQ.FEMCOM.NET";
    
    //Servicio electronicos 
    public static final String DB_USER_FCTAEQA = "QAVIEW";
    public static final String DB_PASSWORD_FCTAEQA = PasswordUtil.decryptPassword("A5544A354006EE384994CACA76D6289B");
    public static final String DB_HOST_FCTAEQA = "10.184.80.120:1521/FCTAEQA.FEMCOM.NET";
    
    public static final String DB_USER_FCSWQA = "swview";
    public static final String DB_PASSWORD_FCSWQA = PasswordUtil.decryptPassword("7AE65940311F261353C76806C270E990");
    public static final String DB_HOST_FCSWQA = "10.184.80.120:1521/FCSWQA.FEMCOM.NET";

    public static final String DB_USER_FCTDCQA = "qaview";
    public static final String DB_PASSWORD_FCTDCQA = PasswordUtil.decryptPassword("A5544A354006EE384994CACA76D6289B");
    public static final String DB_HOST_FCTDCQA = "10.184.80.120:1521/FCTDCQA.FEMCOM.NET";

    //FCWMLTAEQA MTY
    public static final String DB_USER_FCWMLTAEQA_QAVIEW = "qaview";
    public static final String DB_PASSWORD_FCWMLTAEQA_QAVIEW = PasswordUtil.decryptPassword("A5544A354006EE384994CACA76D6289B");  
    //Usuario wmlog
    public static final String DB_USER_FCWMLTAEQA = "wmlog";
    public static final String DB_PASSWORD_FCWMLTAEQA = PasswordUtil.decryptPassword("A0EBA344FE443EEFCBC8C462E60E340E");
    //Usuario QAVIEW
    public static final String DB_USER_FCWMLTAEQA_MTY = "QAVIEW";  
    public static final String DB_HOST_FCWMLTAEQA = "10.184.80.120:1521/FCWMLTAQ.FEMCOM.NET";
    
    //FCMFSQA
    public static final String DB_USER_FCMFS = "qaview";
    public static final String DB_PASSWORD_FCMFS = PasswordUtil.decryptPassword("A5544A354006EE384994CACA76D6289B");
    public static final String DB_HOST_FCMFS = "10.184.32.19:1521/FCMFSQA.FEMCOM.NET";
    
    //FCSWPRD 
    public static final String DB_USER_FCSWPRD_CRECI = "SWVIEW";
    public static final String DB_PASSWORD_FCSWPRD_CRECI= PasswordUtil.decryptPassword("CD418B1D0D38D7E401BDCC705BC09A7E");
    public static final String DB_HOST_FCSWPRD_CRECI = "10.182.56.97:1535/FCSWPRD";
    
				
    //FAC
    public static final String DB_USER_FCACQA = "qaview";
    public static final String DB_PASSWORD_FCACQA  = PasswordUtil.decryptPassword("A5544A354006EE384994CACA76D6289B");
    public static final String DB_HOST_FCACQA = "10.184.80.120/FCACQA.FEMCOM.NET";
    
    //Queretaro
    public static final String DB_USER_FCSWQA_QRO = "swview";
    public static final String DB_PASSWORD_FCSWQA_QRO = PasswordUtil.decryptPassword("7AE65940311F261353C76806C270E990");
    public static final String DB_HOST_FCSWQA_QRO = "10.184.40.120:1521/FCSWQA.FEMCOM.NET";
    
    public static final String DB_USER_FCACQA_QRO = "qaview";
    public static final String DB_PASSWORD_FCACQA_QRO = PasswordUtil.decryptPassword("A5544A354006EE384994CACA76D6289B");
    public static final String DB_HOST_FCACQA_QRO = "10.184.40.120/FCACQA.FEMCOM.NET";
 
    
    public static final String DB_USER_FCWMLTAEQA_QRO = "wmlog";
    public static final String DB_PASSWORD_FCWMLTAEQA_QRO = PasswordUtil.decryptPassword("A0EBA344FE443EEFCBC8C462E60E340E");
 
    public static final String DB_HOST_FCWMLTAEQA_QRO = "10.184.40.120:1521/FCWMLTAQ.FEMCOM.NET";
    public static final String DB_USER_FCWMLTAEQA_QA = "qaview";
    public static final String DB_PASSWORD_FCWMLTAEQA_QRO_QA = PasswordUtil.decryptPassword("0641623006468195175693CCCA34F71E");
    
    public static final String DB_USER_FCTDCQA_QRO = "QAVIEW";
    public static final String DB_PASSWORD_FCTDCQA_QRO = PasswordUtil.decryptPassword("A5544A354006EE384994CACA76D6289B");
    public static final String DB_HOST_FCTDCQA_QRO = "10.184.40.120:1521/FCTDCQA.FEMCOM.NET";
    
    public static final String DB_USER_FCTPE_QRO = "QAVIEW";
    public static final String DB_PASSWORD_FCTPE_QRO =PasswordUtil.decryptPassword("A5544A354006EE384994CACA76D6289B");
    public static final String DB_HOST_FCTPE_QRO = "10.184.40.120:1521/FCTPEQA.FEMCOM.NET";
     
    //MTY
    public static final String DB_HOST_FCWMLTAQ_MTY = "10.184.80.120:1521/FCWMLTAQ.FEMCOM.NET";
    public static final String DB_USER_FCWMLTAQ_MTY = "QAVIEW";
    public static final String DB_PASSWORD_FCWMLTAQ_MTY = PasswordUtil.decryptPassword("0641623006468195175693CCCA34F71E");
    
 
    //RDM 01CU
    public static final String DB_USER_RDM = "rdm100";
    public static final String DB_PASSWORD_RDM = PasswordUtil.decryptPassword("C459C0FBA9A4F8462A94D95D24E976DB");
    public static final String DB_HOST_RDM = "10.180.56.86:1535/FCRD01CU.FEMCOM.NET";
    //RDM RDM63CU
    public static final String DB_USER_RDM_63 = "rdm100";
    public static final String DB_PASSWORD_RDM_63 = PasswordUtil.decryptPassword("35B0A9998A32FE5AE0581E6F785E2F80");
    public static final String DB_HOST_RDM_63 = "10.186.13.15:1521/FCRD63CU.FEMCOM.NET";


    //BIETL
    public static final String DB_USER_BI = "BIETL";
    public static final String DB_PASSWORD_BI = PasswordUtil.decryptPassword("299D18CA0A0E2BFB4975E98E28A81B61");
    public static final String DB_HOST_BI = "10.184.80.26:1535/FCDW4QA.FEMCOM.NET";
    
  //EBS AVANTE
    public static final String DB_USER_AVEBQA = "appsview";
    public static final String DB_PASSWORD_AVEBQA =PasswordUtil.decryptPassword("92C4986E2465EDA27D5D04B227BE5FDE");
    public static final String DB_HOST_AVEBQA = "10.184.48.60:1532/AVEBQA";
    
	public static final String DB_USER_AVEBQA_UPD = "wmuser";
	public static final String DB_PASSWORD_AVEBQA_UPD = PasswordUtil
			.decryptPassword("4CC61E5F09F4E484DAE869B7B058B2F8");
	public static final String DB_HOST_AVEBQA_UPD = "10.184.48.60:1532/AVEBQA";
   
	//SINERGIA- CNT
	public static final String DB_USER_FCIASQA = "qaview";
	public static final String DB_PASSWORD_FCIASQA =PasswordUtil.decryptPassword("A5544A354006EE384994CACA76D6289B");
	public static final String DB_HOST_FCIASQA = "10.184.32.194:1535/FCIASQA.FEMCOM.NET";


    //EBS OIEBSBDQ Peru y chile 
    public static final String DB_USER_OIEBSBDQ = "appsview";
    public static final String DB_PASSWORD_OIEBSBDQ =PasswordUtil.decryptPassword("92C4986E2465EDA27D5D04B227BE5FDE");
    public static final String DB_HOST_OIEBSBDQ = "10.186.13.28:1522/OIEBSBDQ";				
    
  //EBSPruebaFCFINQA Usuario con privilegio consultas
  	public static final String DB_USER_EBS_Consulta = "APPSVIEW";
  	public static final String DB_PASSWORD_EBS_Consulta =PasswordUtil.decryptPassword("3F5BB92144C6B52AEEDEBEA236941122");
    
	//RMS_Mex servidor nuevo solo lectura
  	public static final String DB_USER_RmsMexNu = "NREADER";
	public static final String DB_PASSWORD_RmsMexNu = PasswordUtil.decryptPassword("9BB58DB9D3CE096DE4F53936E3831007");
	public static final String DB_HOST_RmsMexNu = "10.184.37.84:1521/FCRMSSIT";
	
	//Funciona para la TPE_LOT
	public static final String DB_USER_TPE_LOT = "QAVIEW";
	public static final String DB_PASSWORD_TPE_LOT = PasswordUtil.decryptPassword("A5544A354006EE384994CACA76D6289B");
	public static final String DB_HOST_TPE_LOT = "10.184.40.120:1521/FCTPEQA.FEMCOM.NET";
	
	//DBS_BI_LT FCTICQA BI TICKET
	public static final String DB_USER_BiTicket = "QAVIEW";
	public static final String DB_PASSWORD_BiTicket = PasswordUtil.decryptPassword("A5544A354006EE384994CACA76D6289B");
	public static final String DB_HOST_BiTicket = "10.184.80.26:1535/FCTICQA.FEMCOM.NET";
    
	//DBS_BI_LT FCWMREQA 
	public static final String DB_USER_FCWMREQA = "QAVIEW";
	public static final String DB_PASSWORD_FCWMREQA = PasswordUtil.decryptPassword("A5544A354006EE384994CACA76D6289B");
	public static final String DB_HOST_FCWMREQA = "10.185.177.17:1521/FCWMREQA.FEMCOM.NET";
	
	//CTNCHILE se agrego 10/21/2021
	
	public static final String DB_USER_CNTCHILE = "6006028";
	public static final String DB_PASSWORD_CNTCHILE = PasswordUtil.decryptPassword("51D38AD642AE42ABC70F62CE4245FB6D");
	public static final String DB_HOST_CNTCHILE = "10.186.13.27:1547/OCHCNTQ.FEMCOM.NET";
	
	// se agrego FCTDAQA 10/21/2021
	
	public static final String DB_USER_FCTDAQA = "QAVIEW";
	public static final String DB_PASSWORD_FCTDAQA = PasswordUtil.decryptPassword("A5544A354006EE384994CACA76D6289B");
	public static final String DB_HOST_FCTDAQA = "10.184.32.19:1521/FCTDAQA.FEMCOM.NET";
	
	// se agrego Posuser_peru 15/12/2021
	
	public static final String DB_USER_PosuserPeru = "posuser";
	public static final String DB_PASSWORD_PosuserPeru = PasswordUtil.decryptPassword("25AF5CE3DDE8A89AAE9DFCC87ED144A5");
	public static final String DB_HOST_PosuserPeru = "10.184.40.134:1521/OIWMQA.FEMCOM.NET";
	
	// se agrego RMS_Peru 16/12/2021
	
	public static final String DB_USER_RMSWMUSERPeru ="RMS100"; //wmuser
	public static final String DB_PASSWORD_RMSWMUSERPeru =  PasswordUtil.decryptPassword("5F83B010547B5DA3D27275203D3B31DA"); //E023A372AD3B3237F098A0EC4D27ECE7
	public static final String DB_HOST_RMSWMUSERPeru = "10.80.2.222:1521/BDPRMSQ.FEMCOM.NET";	
		
	//FCTPEQA_TPECOUSER
    public static final String DB_USER_FCTPE_CO = "TPECOUSER";
    public static final String DB_PASSWORD_FCTPE_CO =PasswordUtil.decryptPassword("C94BD0CC1BB6883BC6F6F971AE99995F");
    public static final String DB_HOST_FCTPE_CO = "10.184.80.120:1521/FCTPEQA.FEMCOM.NET";
    
    public static final String DB_USER_FCTPE_UPD = "Hexaware30";
	public static final String DB_PASSWORD_FCTPE_UPD = "Hexaware30";
	public static final String DB_HOST_FCTPE_UPD = "10.184.80.120:1521/FCTPEQA.FEMCOM.NET";

	//WMlog
	public static final String DB_USER_FCWMLQA_WMLOG = "Wmlog";
	public static final String DB_PASSWORD_FCWMLQA_WMLOG  =PasswordUtil.decryptPassword("06DB86BC1C7C3D11C9B9F98F2E5686BE");
	public static final String DB_HOST_FCWMLQA_WMLOG  = "10.184.48.215:1535/FCWMLQA.FEMCOM.NET";

	//Bases de datos desarrollo
	public static final String DB_USER_POSUSER_DEV = "posuser";
	public static final String DB_PASSWORD_POSUSER_DEV = "posuser";
	public static final String DB_HOST_POSUSER_DEV = "10.184.48.184:1535/FCWM6DEV";
	
	public static final String DB_USER_EBS_DEV= "wmuser";
	public static final String DB_PASSWORD_EBS_DEV = "wmuser";
	public static final String DB_HOST_EBS_DEV = "10.184.48.83:1536/FCEBSD";
	
	public static final String DB_USER_WMLOG_DEV = "wmlog";
	public static final String DB_PASSWORD_WMLOG_DEV = "wmlog";
	public static final String DB_HOST_WMLOG_DEV = "10.184.48.184:1535/FCWMLDES";
	
	
	//Bases de datos nucleo
		public static final String DB_USER_FCRMSMGR = "nreader";
		public static final String DB_PASSWORD_FCRMSMGR =PasswordUtil.decryptPassword("9BB58DB9D3CE096DE4F53936E3831007");; 
		public static final String DB_HOST_FCRMSMGR = "10.184.37.14:1521/FCRMSMGR";
		
		public static final String DB_USER_FCEBSSIT = "nreader";
		public static final String DB_PASSWORD_FCEBSSIT =PasswordUtil.decryptPassword("9BB58DB9D3CE096DE4F53936E3831007");; 
		public static final String DB_HOST_FCEBSSIT = "10.184.17.27:1521/FCEBSSIT";
		
		public static final String DB_USER_fcwmesit = "nreader";
		public static final String DB_PASSWORD_fcwmesit =PasswordUtil.decryptPassword("9BB58DB9D3CE096DE4F53936E3831007");; 
		public static final String DB_HOST_fcwmesit = "10.184.17.27:1521/fcwmesit";
		
		public static final String DB_USER_FCIASSIT = "nreader";
		public static final String DB_PASSWORD_FCIASSIT =PasswordUtil.decryptPassword("9BB58DB9D3CE096DE4F53936E3831007");; 
		public static final String DB_HOST_FCIASSIT = "10.184.17.42:1535/FCIASSIT";
		
		

		// RR08
		
		public static final String DB_USER_FCWMFSIT = "nreader";
		public static final String DB_PASSWORD_FCWMFSIT = PasswordUtil.decryptPassword("9BB58DB9D3CE096DE4F53936E3831007");
		public static final String DB_HOST_FCWMFSIT = "10.184.17.27:1521/FCWMFSIT";
		
		public static final String DB_USER_FCWMESIT = "nreader";
		public static final String DB_PASSWORD_FCWMESIT = PasswordUtil.decryptPassword("9BB58DB9D3CE096DE4F53936E3831007");
		public static final String DB_HOST_FCWMESIT = "10.184.17.27:1521/FCWMESIT";
		
		public static final String DB_USER_FCRDMSIT_RDM_MTY_RDMVIEW = "nreader";
		public static final String DB_PASSWORD_FCRDMSIT_RDM_MTY_RDMVIEW = PasswordUtil.decryptPassword("9BB58DB9D3CE096DE4F53936E3831007");
		public static final String DB_HOST_FCRDMSIT_RDM_MTY_RDMVIEW = "10.184.37.66:1535/FCRDMSIT";
		
		public static final String DB_USER_FCMOMUAT = "nreader";
		public static final String DB_PASSWORD_FCMOMUAT = PasswordUtil.decryptPassword("9BB58DB9D3CE096DE4F53936E3831007");
		public static final String DB_HOST_FCMOMUAT = "10.184.37.47:1521/FCMOMUAT";
		
		public static final String DB_USER_FCDASUAT = "nreader";
		public static final String DB_PASSWORD_FCDASUAT = PasswordUtil.decryptPassword("9BB58DB9D3CE096DE4F53936E3831007");
		public static final String DB_HOST_FCDASUAT = "10.184.37.67:1521/FCDASUAT";
		
		public static final String DB_USER_FCMOMSIT = "nreader"; // Se agrego user real
		public static final String DB_PASSWORD_FCMOMSIT = PasswordUtil.decryptPassword("9BB58DB9D3CE096DE4F53936E3831007");// Se agrego password real
		public static final String DB_HOST_FCMOMSIT = "10.184.37.55:1521/FCMOMSIT_RMS16_NGA"; // Se agrego host real
		
		public static final String DB_USER_FCMOMPFR_RMS = "nreader";
		public static final String DB_PASSWORD_FCMOMPFR_RMS = PasswordUtil.decryptPassword("9BB58DB9D3CE096DE4F53936E3831007");
		public static final String DB_HOST_FCMOMPFR_RMS = "10.184.47.51:1537/FCMOMPFR";
    //RMS Peru

    public static final String DB_USER_RMS_QAVIEW_Peru ="QAVIEW";
    public static final String DB_PASSWORD_RMS_QAVIEW_Peru = PasswordUtil.decryptPassword("71611C46E0F816ED8968D3B412F086AE"); //E023A372AD3B3237F098A0EC4D27ECE7
    public static final String DB_HOST_RMS_QAVIEW_Peru = "10.80.2.222:1521/BDPRMSQ.FEMCOM.NET";

//    FCIMXQA - OEIMX
    public static final String DB_USER_FCIMXQA ="wmuser";
    public static final String DB_PASSWORD_FCIMXQA =  PasswordUtil.decryptPassword("4CC61E5F09F4E484DAE869B7B058B2F8");
    public static final String DB_HOST_FCIMXQA = "10.80.2.64:1521/FCIMXQA";

    //OXTPEQA
    public static final String DB_USER_OXTPEQA ="tpeview";
    public static final String DB_PASSWORD_OXTPEQA = PasswordUtil.decryptPassword("D4AE1E24D991C337B5AC61C752BB8DBB7AB3A50C19B6F1C7DFCDCAE9E6F1ADF7");
    public static final String DB_HOST_OXTPEQA = "10.188.18.20:1521/OXTPEQA.femcom.net";
	
	//Se agrgeo BD1
    public static final String DB_USER_FCTAEQA_QRO = "QAVIEW";
    public static final String DB_PASSWORD_FCTAEQA_QRO = PasswordUtil.decryptPassword("A5544A354006EE384994CACA76D6289B");
    public static final String DB_HOST_FCTAEQA_QRO = "10.184.40.120:1521/FCTAEQA.FEMCOM.NET";
	
	//FCWMLQA con host diferente (2)
    public static final String DB_USER_FCWMLQA_WMLOG2 = "wmlog";
    public static final String DB_PASSWORD_FCWMLQA_WMLOG2 = PasswordUtil.decryptPassword("4FE8F6491B2AC658E1798FAFC1E1DE26");
    public static final String DB_HOST_FCWMLQA_WMLOG2 = "10.184.48.216:1535/FCWMLQA.FEMCOM.NET";





}
