package org.uab.android.incidencies.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.logging.LogFactory;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HTMLParserListener;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableBody;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;

public class TempsTrajecteHelper {
	
	WebClient webClient;
	String charset = "UTF-8";
	String URL_Rodalies = "http://www20.gencat.cat/portal/site/rodalies/menuitem.d42ab4a34a43f10413724f10b0c0e1a0/" +
		"?vgnextoid=63d326112ceff210VgnVCM2000009b0c1e0aRCRD" +
		"&vgnextchannel=63d326112ceff210VgnVCM2000009b0c1e0aRCRD" +
		"&vgnextfmt=detall&";

	public TempsTrajecteHelper() {
		
		webClient = new WebClient();
		warningsLevelOff(webClient);
		
	}

	public void warningsLevelOff(WebClient webClient) {
		
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");

	    java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF); 
	    java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);

	    webClient.setCssEnabled(false);

	    webClient.setIncorrectnessListener(new IncorrectnessListener() {

	        @Override
	        public void notify(String arg0, Object arg1) {
	            // TODO Auto-generated method stub

	        }
	    });
	    webClient.setCssErrorHandler(new ErrorHandler() {

	        @Override
	        public void warning(CSSParseException exception) throws CSSException {
	            // TODO Auto-generated method stub

	        }

	        @Override
	        public void fatalError(CSSParseException exception) throws CSSException {
	            // TODO Auto-generated method stub

	        }

	        @Override
	        public void error(CSSParseException exception) throws CSSException {
	            // TODO Auto-generated method stub

	        }
	    });
	    webClient.setJavaScriptErrorListener(new JavaScriptErrorListener() {

	        @Override
	        public void timeoutError(HtmlPage arg0, long arg1, long arg2) {
	            // TODO Auto-generated method stub

	        }

	        @Override
	        public void scriptException(HtmlPage arg0, ScriptException arg1) {
	            // TODO Auto-generated method stub

	        }

	        @Override
	        public void malformedScriptURL(HtmlPage arg0, String arg1, MalformedURLException arg2) {
	            // TODO Auto-generated method stub

	        }

	        @Override
	        public void loadScriptError(HtmlPage arg0, URL arg1, Exception arg2) {
	            // TODO Auto-generated method stub

	        }
	    });
	    webClient.setHTMLParserListener(new HTMLParserListener() {

	        @Override
	        public void warning(String arg0, URL arg1, int arg2, int arg3, String arg4) {
	            // TODO Auto-generated method stub

	        }

	        @Override
	        public void error(String arg0, URL arg1, int arg2, int arg3, String arg4) {
	            // TODO Auto-generated method stub

	        }
	    });

	    webClient.setThrowExceptionOnFailingStatusCode(false);
	    webClient.setThrowExceptionOnScriptError(false);
	}
	
	public int recuperaTrajecteRodalies(int origen, int desti, long tempsIniciViatge) throws FailingHttpStatusCodeException, MalformedURLException, IOException, ParseException {
		
		String URL_Rodalies_params = "origen=%s&desti=%s&dataViatge=%s";
		
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		DateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(tempsIniciViatge);
		
		String dataViatge = dateFormat.format(calendar.getTime());
		calendar.add(Calendar.HOUR, -1);
		//int horaInici = calendar.get(Calendar.HOUR_OF_DAY);
		
		String params = String.format(URL_Rodalies_params, 
				URLEncoder.encode(String.valueOf(origen), charset),
				URLEncoder.encode(String.valueOf(desti), charset),
				URLEncoder.encode(dataViatge, charset));
		
		calendar.add(Calendar.HOUR, 1);
		
		HtmlPage htmlPage = webClient.getPage(URL_Rodalies + params);
		HtmlElement schedule = htmlPage.getElementById("schedule");
		
		Iterator<HtmlElement> it = schedule.getChildElements().iterator();
		HtmlTable scheduleTable = (HtmlTable) it.next();
		HtmlTableBody scheduleTableBody = scheduleTable.getBodies().get(0);
		
		Date abansHoraViatge = new Date();
		int abansHoraViatgeDurada = 0;
		Date despresHoraViatge = new Date();
		int despresHoraViatgeDurada = 0;
		
		for ( HtmlTableRow row : scheduleTableBody.getRows() ) {
			
			List<HtmlTableCell> rowCells = row.getCells();
			if ( rowCells.size() == 4 ) {
				Date d = dateTimeFormat.parse(dataViatge + " " + rowCells.get(1).getTextContent());
				
				if ( !d.after(calendar.getTime()) ) {
					
					abansHoraViatge = d;
					abansHoraViatgeDurada = parseTimeString(rowCells.get(3).getTextContent());
				
				} else {
					
					despresHoraViatge = d;
					despresHoraViatgeDurada = parseTimeString(rowCells.get(3).getTextContent());
					break;
					
				}
			}
			
		}
		
		double duradaAnterior = Math.abs(calendar.getTimeInMillis() - abansHoraViatge.getTime());
		//System.out.println(abansHoraViatge.toString() + "  " + abansHoraViatgeDurada);
		double duradaPosterior = Math.abs(despresHoraViatge.getTime() - calendar.getTimeInMillis());
		//System.out.println(despresHoraViatge.toString() + "  " + despresHoraViatgeDurada);
		
		int duradaTrajecte = duradaAnterior < duradaPosterior ? abansHoraViatgeDurada : despresHoraViatgeDurada;
		
		return duradaTrajecte;
		
	}
	
	public int parseTimeString(String time) {
		
		String[] hourMinute = time.split(":");
		return (Integer.parseInt(hourMinute[0]) * 60 + Integer.parseInt(hourMinute[1]));
		
	}
}
