package hanamanu;

import java.io.DataOutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public class HanamanuClient {
	
	public class HanamanuClientException extends Exception
	{
		public static final long serialVersionUID = 1;
		
		public HanamanuClientException(String message)
	    {
			super(message);
	    }
	
	    public HanamanuClientException(String message, Exception cause)
	    {
	    	super(message, cause);
	    }
	}
	
	private static final String HANAMANU_END_POINT_URL = "http://api.hanamanu.com/v1/events";
    private static final String HTTP_METHOD_POST = "POST";
    private static final String REQUEST_CONTENT_TYPE_HEADER_NAME = "ContentType";
    private static final String REQUEST_CONTENT_TYPE = "application/json";
    private static final String AUHTHORIZATION_HEADER_NAME = "Authorization";
    private static final String REQUEST_CONTENT_LENGTH_HEADER_NAME = "ContentLength";
    
    private static final Charset ASCII_CHARSET = Charset.forName("ASCII");
    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    
    private static final byte[] REQUEST_PART_1 = (new String("{\"MetricName\":\"")).getBytes(ASCII_CHARSET);
    private static final byte[] REQUEST_PART_2 = (new String("\",\"Value\":")).getBytes(ASCII_CHARSET);
    private static final byte[] REQUEST_PART_3 = (new String("}")).getBytes(ASCII_CHARSET);
    
    private static final int REQUEST_BASE_LENGTH = REQUEST_PART_1.length + REQUEST_PART_2.length + REQUEST_PART_3.length;
	
	private String appSecretKey;

	public HanamanuClient(String appSecretKey) {
		if (appSecretKey == null || appSecretKey.trim().isEmpty()) throw new IllegalArgumentException("appSecretKey cannot be null");
		
		this.appSecretKey = appSecretKey.trim();
	}
	
	public void SendEvent(String metricName, BigDecimal value) throws HanamanuClientException {
		
		if (metricName == null || metricName.trim().isEmpty()) throw new IllegalArgumentException("metricName cannot be null");
		if (value == null) throw new IllegalArgumentException("value cannot be null");

		HttpURLConnection connection = null;
		int statusCode = 0;
		String statusDescription = null;
		
		try {
			byte[] bytesMetricName = metricName.getBytes(UTF8_CHARSET);
	        byte[] bytesValue = value.toString().getBytes(ASCII_CHARSET);
	        
	        int contentLength = REQUEST_BASE_LENGTH + bytesMetricName.length + bytesValue.length;
			
			connection = (HttpURLConnection)(new URL(HANAMANU_END_POINT_URL)).openConnection();
			
			connection.setRequestMethod(HTTP_METHOD_POST);
			
			connection.setRequestProperty(REQUEST_CONTENT_TYPE_HEADER_NAME, REQUEST_CONTENT_TYPE);
			connection.setRequestProperty(AUHTHORIZATION_HEADER_NAME, this.appSecretKey);
			connection.setRequestProperty(REQUEST_CONTENT_LENGTH_HEADER_NAME, Integer.toString(contentLength));
			
			connection.setUseCaches(false);
		    connection.setDoOutput(true);
		    
		    DataOutputStream wr = new DataOutputStream (connection.getOutputStream());
		    
	        wr.write(REQUEST_PART_1);
	        wr.write(bytesMetricName);
	        wr.write(REQUEST_PART_2);
	        wr.write(bytesValue);
	        wr.write(REQUEST_PART_3);
	        
	        wr.close();
	        
	        statusCode = connection.getResponseCode();
	        statusDescription = connection.getResponseMessage();
		}
		catch (Exception ex) {
			throw new HanamanuClientException("Hanamanu Client Error!", ex);
		}
		
		if (statusCode != 0 && statusCode / 100 != 2) {
			throw new HanamanuClientException(String.format("Hanamanu server returned error: %d -> %s", statusCode, statusDescription));
        }
	}
}
