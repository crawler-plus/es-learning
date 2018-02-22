package test.es;

import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;

/**
 * 索引管理
 */
public class TestIndex {

    // 集群名称
    private static String CLUSTER_NAME = "elasticsearch";

    // 服务器IP
    private static String HOST_IP = "127.0.0.1";

    // 端口
    private static int TCP_PORT = 9300;

    public static void main(String[] args) throws Exception {
        IndicesAdminClient indices = getClient().admin().indices();
        IndicesExistsResponse existsResponse = indices.prepareExists("indexName").get();
        System.out.println(existsResponse.isExists()); // 打印indexName索引名是否存在

        TypesExistsResponse typesExistsResponse = indices.prepareTypesExists("books").setTypes("IT").get();
        System.out.println(typesExistsResponse.isExists()); // 判断books索引中是否存在IT的type

        // 创建一个索引
        CreateIndexResponse indexName = indices.prepareCreate("myindex").get();
        System.out.println(indexName.isAcknowledged());

        // 创建索引并设置settings
        indices.prepareCreate("twitter").setSettings(Settings.builder().put("index.number_of_shards", 3)
        .put("index.number_of_replicas",2)).get();

        // 更新副本
        indices.prepareUpdateSettings("twitter").setSettings(Settings.builder().put("index.number_of_replicas", 0)).get();

        // 获取settings
        GetSettingsResponse getSettingsResponse = indices.prepareGetSettings("twitter").get();
        ImmutableOpenMap<String, Settings> indexToSettings = getSettingsResponse.getIndexToSettings();
        for(ObjectObjectCursor<String, Settings> cursor : indexToSettings) {
            String index = cursor.key;
            Settings settings = cursor.value;
            Integer shards = settings.getAsInt("index.number_of_shards", null);
            Integer replicas = settings.getAsInt("index.number_of_replicas", null);
            System.out.println(shards + " and " +replicas);
        }

        // 添加一个索引为tweet，并设置一个type为twe，设置mapping
        indices.prepareCreate("tweet").addMapping("twe", XContentFactory.jsonBuilder()
        .startObject()
        .startObject("properties")
        .startObject("name")
        .field("type", "keyword")
        .endObject()
        .endObject()
        .endObject()).get();

        // 获取mapping
        GetMappingsResponse getMappingsResponse = indices.prepareGetMappings("tweet").get();
        ImmutableOpenMap<String, MappingMetaData> dataImmutableOpenMap = getMappingsResponse.getMappings().get("tweet");
        MappingMetaData twe = dataImmutableOpenMap.get("twe");
        System.out.println(twe.getSourceAsMap());

        // 刷新索引
        indices.prepareRefresh().get();
        indices.prepareRefresh("tweet").get();

        // 关闭索引
        indices.prepareClose("tweet").get();

        // 打开索引
        indices.prepareOpen("tweet").get();

        // 设置别名
        indices.prepareAliases().addAlias("tweet", "mytweet").get();

        // 获取别名
        indices.prepareGetAliases().get();

        // 删除索引
        DeleteIndexResponse deleteIndexResponse = indices.prepareDelete("tweet").get();
        System.out.println(deleteIndexResponse.isAcknowledged());
    }

    private static TransportClient getClient() throws Exception {
        Settings settings = Settings.builder().put("cluster.name", CLUSTER_NAME).build();
        TransportClient client = new PreBuiltTransportClient(settings).addTransportAddress(
                new InetSocketTransportAddress(InetAddress.getByName(HOST_IP), TCP_PORT)
        );
        return client;
    }
}
