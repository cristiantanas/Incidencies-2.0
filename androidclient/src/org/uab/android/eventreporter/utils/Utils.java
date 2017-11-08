package org.uab.android.eventreporter.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Utils {
	
	/**
	 * Constants
	 */
	public static final String INFORMATION_OK = "Informació rebuda correctament";
	public static final String CONFIRMED_EVENT = "Ja va notificar aquesta incidència " +
			"anteriorment.";
	public static final String FORMAT_ERROR = "Format incorrecte!";
	public static final String XML_MALFORMED = "Codificació XML incorrecta!";
	public static final String SERVER_RECEIVE_FAIL = 
		"No es pot guardar l'informe per un problema amb el servidor. " + 
		"Per favor, torni a intentar-ho més tard...";
	public static final String SERVER_CONNECTION_FAIL = 
		"Ho sentim, però sembla que el servidor no està disponible. " + 
		"Per favor, torni a intentar-ho més tard...";
	
	public static final String T11_PICKED_UP_NOTIFICATION = "Et comuniquem que l'última T11 que vas deixar " +
			"la va aprofitar un altre usuari. Gràcies per la teva col·laboració.";
	
	public static final int DATAGRAM_VERSION = 3;
	public static final int DATAGRAM_PT_TYPE = 1;
	public static final int DATAGRAM_GN_TYPE = 2;
	public static final int CONNECTION_ACCEPTED = 1;
	
	public static final int DATAGRAM_DEFAULT_OPTIONS = 0;
	public static final int DATAGRAM_T11_PICKED_UP_OPTION = 1;
	
	public static final int INCIDENT_NOTIFICATION = 10;
	public static final int INCIDENT_CONFIRMATION = 15;
	public static final int AUTHENTICATE_USER = 11;
	public static final int GET_EVENTS = 12;
	public static final int GET_LAT_LNG = 13;
	public static final int GET_INCIDENTS_FOR_LINE = 14;
	public static final int GET_COMMENTS_FOR_INCIDENT = 16;
	public static final int UPDATE_USER_ACCOUNT = 17;
	public static final int GET_SINGLE_INCIDENT = 18;
	public static final int GET_GENERAL_INCIDENTS = 19;
	public static final int DATA_RECEIVED_CORECTLY = 21;
	public static final int USER_ACCESS_GRANTED = 22;
	public static final int EVENT_INFORMATION_RECEIVED = 23;
	public static final int LATLNG_INFORMATION_RECEIVED = 24;
	public static final int LINE_INCIDENTS_INFO_RCVD = 25;
	public static final int COMMENTS_FOR_INCIDENT_RCVD = 26;
	public static final int ACCOUNT_CORECTLY_UPDATED = 27;
	public static final int SINGLE_INCIDENT_RCVD = 28;
	public static final int GENERAL_INC_INFO_RCVD = 29;
	public static final int EVENT_CONFIRMATION = 31;
	public static final int ALREADY_CONFIRMED_EVENT = 32;
	public static final int BAD_INPUT_FORMAT = 41;
	public static final int INVALID_XML_FORMAT = 42;
	public static final int USER_UNAUTHORIZED = 43;
	public static final int USER_PASSWORD_FAILED = 44;
	public static final int SERVER_ERROR = 50;
	
	public static final int T11_NOTIFICATION = 60;
	public static final int T11_RECEIVED_CORECTLY = 61;
	public static final int T11_GET = 62;
	public static final int T11_GET_DONE = 63;
	public static final int T11_PICK_UP = 64;
	public static final int T11_CORRECTLY_UPDATED = 65;
	
	public static final int ROUTE_NOTIFICATION = 70;
	public static final int END_OF_ROUTE_NOTIFICATION = 71;
	public static final int END_OF_ROUTE_RESPONSE = 72;
	
	public static final String HEADER_SEPARATOR = ";";
	public static final String BODY_SEPARATOR = "#";
	public static final String DATAGRAM_SEPARATOR = "##";
	
	public static final String NO_NETWORK_CONNECTION = "No s'ha pogut accedir a Internet. Per favor, " +
			"asseguris de tenir habilitada una connexió a Internet.";
	public static final String NO_SERVER_CONNECTION = "Sembla que hi ha un problema amb el servidor. " +
			"Per favor, torni a intentar-ho més tard!";
	public static final String CONFIRM = "Confirmar";
	public static final String TAKE = "Agafar";
	public static final String OK = "D\'acord";
	public static final String CANCEL = "Cancel·lar";
	public static final String DISCARD = "Descartar";
	public static final String LOADING = "Carregant informació...";
	
	public static final String NO_LOCATION_PROVIDER = "Sembla que no té activat el GPS. " +
			"Desitja activar-ho ara o seleccionar una altra acció?";
	public static final String NO_LOCATION_AVAILABLE = "Per reportar incidències generals es " +
			"necessita tenir habilitat el GPS. Desitja activar-ho ara?";
	public static final String NO_DESCRIPTION_AVAILABLE = "Per reportar incidències generals " +
			"es necessari introduïr una descripció!";
	
	public static final int ALL_INCIDENTS = 9999;
	public static final int GENERAL_INCIDENTS = 99;
	public static final int RSS_FEEDS = 10000;
	public static final int RENFE_INCIDENTS = 0;
	public static final int FGC_INCIDENTS = 1;
	public static final int METRO_INCIDENTS = 2;
	public static final int UNKNOWN = -1;
	
	public static final int MIN_TO_MILIS = 60000;
	
	public static final int UPDATE_MIN_TIME = 60000;
	public static final int UPDATE_MIN_DISTANCE = 10;
	
	public static final int CLEAR_STACK = 666;
	
	/**
	 * Function that returns an MD5 hash for a given string
	 * @param s
	 * @return hash of the string s
	 */
	public static final String md5(String s) {
		MessageDigest md = null;
		
		try { md = MessageDigest.getInstance("MD5"); }
		catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
		
		md.update(s.getBytes(), 0, s.length());
		return new BigInteger(1, md.digest()).toString(16);
	}
	
	public static ArrayList<Incident> quicksort(ArrayList<Incident> incidents) {
		if ( incidents.size()<=1 ) {
			return incidents;
			
		} else {
			Incident pivot = incidents.get( incidents.size()/2 );
			ArrayList<Incident> lesser = new ArrayList<Incident>();
			ArrayList<Incident> greater = new ArrayList<Incident>();
			ArrayList<Incident> equals = new ArrayList<Incident>();
			
			for ( Incident i : incidents ) {
				if ( i.getLastUpdateTime()>pivot.getLastUpdateTime() ) {
					greater.add(i);			}
				
				else if ( i.getLastUpdateTime()<pivot.getLastUpdateTime() ) {
					lesser.add(i);			}
				
				else {
					equals.add(i);			}
			}
			
			lesser = quicksort(lesser);			
			greater = quicksort(greater);
			ArrayList<Incident> sorted = new ArrayList<Incident>();
			for ( Incident i : greater ) 
				sorted.add(i);
			
			for ( Incident i : equals ) 
				sorted.add(i);
			
			for ( Incident i : lesser )
				sorted.add(i);
			
			return sorted;
		}
	}
	
	public static ArrayList<GeneralNotification> sortByTime(ArrayList<GeneralNotification> incidents) {
		if ( incidents.size()<=1 ) {
			return incidents;
			
		} else {
			GeneralNotification pivot = incidents.get( incidents.size()/2 );
			ArrayList<GeneralNotification> lesser = new ArrayList<GeneralNotification>();
			ArrayList<GeneralNotification> greater = new ArrayList<GeneralNotification>();
			ArrayList<GeneralNotification> equals = new ArrayList<GeneralNotification>();
			
			for ( GeneralNotification i : incidents ) {
				if ( i.getLastUpdateTime()>pivot.getLastUpdateTime() ) {
					greater.add(i);			}
				
				else if ( i.getLastUpdateTime()<pivot.getLastUpdateTime() ) {
					lesser.add(i);			}
				
				else {
					equals.add(i);			}
			}
			
			lesser = sortByTime(lesser);			
			greater = sortByTime(greater);
			ArrayList<GeneralNotification> sorted = new ArrayList<GeneralNotification>();
			for ( GeneralNotification i : greater ) 
				sorted.add(i);
			
			for ( GeneralNotification i : equals ) 
				sorted.add(i);
			
			for ( GeneralNotification i : lesser )
				sorted.add(i);
			
			return sorted;
		}
	}
	
	public static final String typeToCharSeq(int type) {
		switch (type) {
		case RENFE_INCIDENTS:
			return "Rodalies";
			
		case FGC_INCIDENTS:
			return "FGC";
			
		case METRO_INCIDENTS:
			return "Metro";
			
		default:
			return "Desconegut";
		}
	}
	
	public static final String stateToCharSeq(int state) {
		switch (state) {
		case 0:
			return "Pendent de confirmar";
			
		case 1: 
			return "Confirmada";
			
		default:
			return "Sense determinar";
		}
	}
	
	public static final String eventToCharSeq(int event) {
		switch (event) {
		case 0:
			return "Retard";
			
		case 1:
			return "Convoy avariat";
			
		case 2: 
			return "Incidència subministrament elèctric";
			
		case 3:
			return "Definida per l'usuari";
			
		default:
			return "Sense determinar";
		}
	}
	
	public static String escape(String s) {
//		return s.replace("\'", "\\'");
		return s;
	}
}
