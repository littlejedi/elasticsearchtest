package elasticsearchclient;

import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ElasticSearchClient {

    private Client client;
    private ObjectMapper MAPPER = new ObjectMapper();

    public ElasticSearchClient() {

        // on startup
        this.client = new TransportClient()
                .addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
    }

    public void indexSensorDataDocument(SensorDataDocument doc) throws Exception {
        System.out.println(doc);
        String json = MAPPER.writeValueAsString(doc);
        IndexResponse response = client
                .prepareIndex("stem", "sensordata", doc.getId())
                .setSource(json).execute().actionGet();
        boolean created = response.isCreated();
        System.out.println("created:" + created);
    }
    
    public void indexMultipleSensorDataDocuments(List<SensorDataDocument> docs) throws Exception {
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        for (SensorDataDocument doc : docs) {
            String json = MAPPER.writeValueAsString(doc);
            bulkRequest.add(client.prepareIndex("stem", "sensordata", doc.getId())
                    .setSource(json));
        }
        BulkResponse response = bulkRequest.execute().actionGet();
        if (response.hasFailures()) {
            Iterator<BulkItemResponse> iter = response.iterator();
            while (iter.hasNext()) {
                BulkItemResponse r = iter.next();
                System.out.println("failure: " + r.getFailureMessage());
            }
        }
    }
    
    public void queryDataBySensorId(String sensorId) throws Exception {
        //QueryBuilder qb = QueryBuilders.matchQuery("sensorId", sensorId);
        SearchResponse response = client.prepareSearch("stem")
                .setTypes("sensordata")
                .addSort("sensorDataTimestamp", SortOrder.ASC)
                .setQuery(QueryBuilders.matchQuery("sensorId", sensorId))
                .setFrom(0)
                .setSize(10000)
                //.setFrom(0).setSize(50).setExplain(true)
                .execute()
                .actionGet();
        SearchHits hits = response.getHits();
        int i = 1;
        if (hits.getTotalHits() > 0) {
            for (SearchHit h : hits) {
                //h.getInnerHits()
                System.out.println(h.getSourceAsString());
                SensorDataDocument doc = MAPPER.readValue(h.getSourceAsString(), SensorDataDocument.class);
                DateTime dt = new DateTime(doc.getSensorDataTimestamp(), DateTimeZone.forTimeZone(TimeZone.getTimeZone("PST")));
                System.out.println(dt.toString());
                System.out.println(i);
                i++;
            }
        }
    }
    
    public void queryDataByDateRange(long start, long end) throws Exception {
        FilterBuilder filter = FilterBuilders.andFilter(
                FilterBuilders.rangeFilter("sensorDataTimestamp").from(start).to(end));
        SearchResponse response = client.prepareSearch("stem")
                .setTypes("sensordata")
                .addSort("sensorDataTimestamp", SortOrder.ASC)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.rangeQuery("sensorDataTimestamp").from(start).to(end))
                .setFrom(0)
                .setSize(10000)
                //.setPostFilter(filter)
                //.setFrom(0).setSize(50).setExplain(true)
                .execute()
                .actionGet();
        SearchHits hits = response.getHits();
        int i = 1;
        if (hits.getTotalHits() > 0) {
            for (SearchHit h : hits) {
                //h.getInnerHits()
                System.out.println(h.getSourceAsString());
                SensorDataDocument doc = MAPPER.readValue(h.getSourceAsString(), SensorDataDocument.class);
                DateTime dt = new DateTime(doc.getSensorDataTimestamp(), DateTimeZone.forTimeZone(TimeZone.getTimeZone("PST")));
                System.out.println(dt.toString());
                System.out.println(i);
                i++;
            }
        }
    }

}
