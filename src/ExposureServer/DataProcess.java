/**
 * @author Ian Campbell iancamp@udel.edu Linkedin.com/in/iancamp
 */

package ExposureServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;



//import javax.json.Json;
//import javax.json.JsonArray;
//import javax.json.JsonObject;
//import javax.json.JsonReader;


import org.json.JSONObject;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.MessageIterator;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Service;
import com.bloomberglp.blpapi.Session;
import com.bloomberglp.blpapi.SessionOptions;


public class DataProcess {

	private static final boolean DEBUG = true;

	//private static String d_serverHost = "NOTASERVER";
	private static final String d_serverHost = "10.8.8.1";
	private static final int d_serverPort = 8194;
	private static final String service = "//blp/refdata";


	public static void main(String[] args) throws Exception {
		FinanceData data = null;
		data = run("tesla");
		System.out.println("LAST PRICE: " + data.getLastPrice());


	}

	public static FinanceData run(String company) throws Exception
	{
		FinanceData data = new FinanceData(getCompany(company), getStockSymbol(company), -1f);
		SessionOptions sessionOptions = new SessionOptions();

		try{
			sessionOptions.setServerHost(d_serverHost);
			sessionOptions.setServerPort(d_serverPort);
		}
		catch(Exception eip){
			//ignore
		}


		Session session = new Session(sessionOptions);


		//connect to the server
		if(!session.start())
		{
			System.out.println("Could not start a session\n\tServer: " + d_serverHost + "\n\tPort: " + d_serverPort);
			System.exit(1);
		}
		else if(DEBUG)
		{
			System.out.println("Server: SUCCESS!");
		}

		//open service
		if(!session.openService(service))
		{
			System.out.println("Could not open service: " + service);
			System.exit(1);
		}
		else if(DEBUG)
		{
			System.out.println("Service: SUCCESS!");
		}

		//generate a request
		CorrelationID requestID = new CorrelationID(1);

		Service refDataServ = session.getService(service);

		Request req = refDataServ.createRequest("ReferenceDataRequest");
		req.append("securities", data.getSym() + " US Equity");
		req.append("fields", "PX_LAST"); //last price
		//req.append("fields", "NW011"); //News sentiment
		req.append("fields", "DY785"); //Peers
		session.sendRequest(req, requestID); // send the request

		//wait for the server to process the request
		boolean waiting = true;
		while(waiting)
		{
			Event event  = session.nextEvent();

			//data comes in fragments, so don't end the loop until event type RESPONSE is received
			switch(event.eventType().intValue())
			{
			case Event.EventType.Constants.RESPONSE:
				waiting = false; //done waiting, end loop
			case Event.EventType.Constants.PARTIAL_RESPONSE:
				handleResponse(event, data); //results of the requested data
			default:
				handleStatus(event); //not the requested data
			}
		}

		if(DEBUG)
			System.out.println("THIS IS DATA: " + data.toString());
		return data;
	}


	/**
	 * Handle response messages 
	 * @param event: results of requested data
	 */
	private static void handleResponse(Event event, FinanceData data) throws Exception
	{
		if(DEBUG)
			System.out.println("Event type: " + event.eventType());
		MessageIterator iter = event.messageIterator();

		while(iter.hasNext())
		{
			//get the message for this event
			Message message = iter.next();
			if(DEBUG)
			{
				System.out.println("CorrelationID: " + message.correlationID());
				System.out.println("Message Type: " + message.messageType());
				System.out.print("Message data: \n\t");
				message.print(System.out);
			}

			//Get the data we want from the message (in this case, last price and top competitor). It's magic. Don't worry about how ridiculous it looks.
			float lastPrice = (float) message.asElement().getElement("securityData").getValueAsElement().getElement("fieldData").getElementAsFloat64("PX_LAST");
			String competitor = message.asElement().getElement("securityData").getValueAsElement().getElement("fieldData").getElement("DY785").getValueAsElement(0).getElementAsString("Peer Ticker");
			if(competitor.contains(" "))
				data.setCompetitor(getCompany(competitor.substring(0,competitor.indexOf(" "))));
			else
				data.setCompetitor(getCompany(competitor));

			data.setLastPrice(lastPrice);
			if(DEBUG)
				System.out.println("THIS IS TEST: " + lastPrice);


		}
	}

	private static void handleStatus(Event event) throws Exception
	{
		if(DEBUG)
			System.out.println("Event Type: " + event.eventType());
		MessageIterator iter = event.messageIterator();
		while(iter.hasNext()) {
			Message message = iter.next();
			if(DEBUG)
			{
				System.out.println("correlationID: " + message.correlationID());
				System.out.println("messageType: " + message.messageType());
				System.out.print("Message data: \n\t:");
				message.print(System.out);
			}


			if(Event.EventType.Constants.SESSION_STATUS == event.eventType().intValue() && "SessionTerminated" == message.messageType().toString()) {
				System.out.println("Terminating: " + message.messageType());
				System.exit(1);
			}
		}

	}

	/**
	 * Queries Yahoo with a company name (full or partial).
	 * @param keyword: the company name to search for
	 * @throws Exception
	 * @return The stock symbol for the given company. Void if the company could not be found.
	 */
	private static String getStockSymbol(String keyword) throws Exception
	{
		URL url = new URL ("http://d.yimg.com/autoc.finance.yahoo.com/autoc?query=" + keyword + "&callback=YAHOO.Finance.SymbolSuggest.ssCallback");
		String sym = "";
		BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream())); //get the raw JSON data
		try
		{
			String data = reader.readLine().substring(39); //get rid of npn-JSON data
			if(DEBUG)
				System.out.println("test: " + data);

			JSONObject jsonObj = new JSONObject(data); //convert the raw text data to a JSON object

			//
			sym = jsonObj.getJSONObject("ResultSet").getJSONArray("Result").getJSONObject(0).getString("symbol");

			if(DEBUG)
				System.out.println("Symbol: " + sym); 
		}
		catch(Exception e)
		{
			//most likely a bad company name
			if(DEBUG)
				e.printStackTrace();
			throw new Exception();
		}
		return sym;
	}

	private static String getCompany(String sym) throws Exception
	{
		URL url = new URL ("http://d.yimg.com/autoc.finance.yahoo.com/autoc?query=" + sym + "&callback=YAHOO.Finance.SymbolSuggest.ssCallback");
		String company = "";
		BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream())); //get the raw JSON data
		try
		{
			String data = reader.readLine().substring(39); //get rid of npn-JSON data
			if(DEBUG)
				System.out.println("test: " + data);

			JSONObject jsonObj = new JSONObject(data); //convert the raw text data to a JSON object

			//
			company = jsonObj.getJSONObject("ResultSet").getJSONArray("Result").getJSONObject(0).getString("name");

			if(DEBUG)
				System.out.println("Company name: " + company); 
		}
		catch(Exception e)
		{
			//most likely a bad symbol
			if(DEBUG)
				e.printStackTrace();
		}

		return company;
	}
}
