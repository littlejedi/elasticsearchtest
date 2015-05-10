package elasticsearchclient;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class ElasticSearchTest {

	public static void main(String[] args) throws Exception {
		ElasticSearchClient client = new ElasticSearchClient();
		client.indexSensorDataDocument(getSensorDataDocument());
		List<SensorDataDocument> docs = new ArrayList<SensorDataDocument>();
		for (int i = 0; i < 50; i++) {
		    docs.add(getSensorDataDocument());
		}
		//client.indexMultipleSensorDataDocuments(docs);
		DateTime start = new DateTime(DateTimeZone.UTC).minusMinutes(30);
		DateTime end = new DateTime(DateTimeZone.UTC);
		client.queryDataByDateRange(start.getMillis(), end.getMillis());
	}
	
	public static SensorDataDocument getSensorDataDocument() {
        UUID uuid = UUID.randomUUID();
        DateTime now = DateTime.now(DateTimeZone.UTC);
        SensorDataDocument doc = new SensorDataDocument();
        doc.setId(uuid.toString());
        doc.setSensorId("TestSensorId");
        doc.setDeviceId("TestDeviceId");
        doc.setUserId(1);
        doc.setSensorDataType(SensorDataType.IMAGE.getValue());
        doc.setSensorDataValue(String.valueOf(new Random().nextDouble()));
        doc.setSensorDataTimestamp(now.getMillis());
        return doc;
	}

}
