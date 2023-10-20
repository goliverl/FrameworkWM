package adapterAutomatationIS.om;

import java.util.HashMap;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import utils.selenium.SeleniumUtil;

public class IntegrationServerView {

	//utilerias   
	
		SeleniumUtil u;
		By serverNameValueBy    = 	By.xpath("/html/body/form/table[1]/tbody/tr[9]/td[1]");
		By userValueBy    = 	By.xpath("/html/body/form/table[1]/tbody/tr[10]/td[1]");
		By dataBaseNameValueBy    = 	By.xpath("/html/body/form/table[1]/tbody/tr[12]");
		By otherPropertiesValueBy    = 	By.xpath("/html/body/form/table[1]/tbody/tr[15]/td[1]");
	

		public IntegrationServerView(SeleniumUtil u) {
			super();
			this.u = u;
		}
	
	public HashMap<String, String> getDetails() {

		HashMap<String, String> details = new HashMap<String, String>();
			
			String serverNameValue = u.getText(serverNameValueBy);
			String userValue = u.getText(userValueBy);
			String baseNameValue = u.getText(dataBaseNameValueBy);
			String otherValue = u.getText(otherPropertiesValueBy);
			
			
			details.put("serverNa", serverNameValue);
			details.put("user", userValue);
			details.put("baseName", baseNameValue);
			details.put("otherName", otherValue);
			return details;
		}
		
		
		
	
	
}
