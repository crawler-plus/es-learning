package test.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class TestDocument {

    // 集群名称
    private static String CLUSTER_NAME = "elasticsearch";

    // 服务器IP
    private static String HOST_IP = "127.0.0.1";

    // 端口
    private static int TCP_PORT = 9300;

    public static void main(String[] args) throws Exception {
        // 使用map方式将文档插入索引
        Map<String, Object> doc = new HashMap<>();
        doc.put("user", "kimchy");
        doc.put("postDate", "2013-01-30");
        doc.put("message", "try out elasticsearch");
        IndexResponse indexResponse = getClient().prepareIndex("twitter", "tweet", "1").setSource(doc).get();
        System.out.println(indexResponse.status());

        // 使用elasticsearch帮助类插入索引
        XContentBuilder docX = jsonBuilder()
                .startObject()
                .field("user", "zhangsan")
                .field("postDate", "2013-01-31")
                .field("message", "hi, i am zhangsan")
                .endObject();
        System.out.println(docX.string());
        IndexResponse indexResponse1 = getClient().prepareIndex("twitter", "tweet", "2").setSource(docX).get();
        System.out.println(indexResponse1.status());

        // 使用XContentBuilder构造复杂文档示例：
        XContentBuilder builder = jsonBuilder()
                .startObject()
                .field("name", "tom")
                .field("age", "12")
                .startArray("scores")
                .startObject()
                .field("Math", "80")
                .endObject()
                .startObject()
                .field("English", "85")
                .endObject()
                .endArray()
                .field("address")
                .startObject()
                .field("country", "China")
                .field("city", "beijing")
                .endObject()
                .endObject();
        System.out.println(builder.string());

        // 使用ObjectMapper序列化javabean方式构造
        User user = new User("lisi", new Date(), "i am lisi");
        ObjectMapper mapper = new ObjectMapper();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        mapper.setDateFormat(sdf);
        byte[] bytes = mapper.writeValueAsBytes(user);
        IndexResponse indexResponse2 = getClient().prepareIndex("twitter", "tweet", "3").setSource(bytes).execute().actionGet();
        System.out.println(indexResponse2.status());

        // 获取文档
        GetResponse getResponse = getClient().prepareGet("twitter", "tweet", "1").get();
        String sourceAsString = getResponse.getSourceAsString();
        System.out.println(sourceAsString);

        // 删除文档
        DeleteResponse deleteResponse = getClient().prepareDelete("twitter", "tweet", "1").get();
        System.out.println(deleteResponse.status());

        // 更新文档,添加一个gender的字段
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.index("twitter").type("tweet").id("2").doc(jsonBuilder().startObject().field("gender", "male").endObject());
        getClient().update(updateRequest).get();

        // 使用prepareUpdate方式更新文档
        getClient().prepareUpdate("twitter", "tweet", "2").setDoc(jsonBuilder().startObject().field("objectKey", "objectValue").endObject()).get();

        // upsert操作，如果文档存在就更新，不存在就创建
        IndexRequest indexRequest = new IndexRequest("twitter", "tweet", "1")
                .source(jsonBuilder().startObject().field("name", "JOE").field("gender", "male").endObject());
        UpdateRequest ur = new UpdateRequest("twitter", "tweet", "1")
                .doc(jsonBuilder().startObject().field("gender", "male").endObject()).upsert(indexRequest);
        getClient().update(ur).actionGet();

        // 根据条件查询并删除
        BulkByScrollResponse bulkByScrollResponse = DeleteByQueryAction.INSTANCE.newRequestBuilder(getClient()).filter(QueryBuilders.matchQuery("message", "lisi")).source("twitter").get();
        System.out.println(bulkByScrollResponse.getDeleted());

        // 批量获取
        MultiGetResponse multiGetItemResponses = getClient().prepareMultiGet().add("twitter", "tweet", "1", "2").get();
        for(MultiGetItemResponse itemResponse : multiGetItemResponses) {
            GetResponse response = itemResponse.getResponse();
            if(response != null && response.isExists()) {
                String json = response.getSourceAsString();
                System.out.println(json);
            }
        }

        // 批量更新，删除，添加文档操作
        BulkRequestBuilder bulkRequestBuilder = getClient().prepareBulk();
        IndexRequestBuilder indexRequestBuilder = getClient().prepareIndex("twitter", "tweet", "5").setSource(jsonBuilder().startObject().field("user", "wangwu")
                .field("postDate", new Date()).field("message", "i am wangwu").endObject());
        DeleteRequestBuilder deleteRequestBuilder = getClient().prepareDelete("twitter", "tweet", "1");
        UpdateRequestBuilder updateRequestBuilder = getClient().prepareUpdate("twitter", "tweet", "2").setDoc(jsonBuilder().startObject().field("message", "updated").endObject());
        bulkRequestBuilder.add(indexRequestBuilder).add(deleteRequestBuilder).add(updateRequestBuilder).execute().actionGet();
    }


    private static TransportClient getClient() throws Exception {
        Settings settings = Settings.builder().put("cluster.name", CLUSTER_NAME).build();
        TransportClient client = new PreBuiltTransportClient(settings).addTransportAddress(
                new InetSocketTransportAddress(InetAddress.getByName(HOST_IP), TCP_PORT)
        );
        return client;
    }
}
