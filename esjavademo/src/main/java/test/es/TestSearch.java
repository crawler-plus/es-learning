package test.es;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;

public class TestSearch {

    // 集群名称
    private static String CLUSTER_NAME = "elasticsearch";

    // 服务器IP
    private static String HOST_IP = "127.0.0.1";

    // 端口
    private static int TCP_PORT = 9300;

    public static void main(String[] args) throws Exception {
       // 查询案例
        QueryBuilder matchQuery = QueryBuilders.matchQuery("title", "python").operator(Operator.AND);
        HighlightBuilder highlightBuilder = new HighlightBuilder().field("title").preTags("<span style=\"color:red\">").postTags("</span>");
        SearchResponse books = getClient().prepareSearch("books").setQuery(matchQuery).highlighter(highlightBuilder).setSize(100).get();
        SearchHits hits = books.getHits();
        System.out.println("共搜索到：" + hits.totalHits + "条数据");
        for(SearchHit hit : hits) {
            System.out.println(hit.getSourceAsString());
            System.out.println(hit.getSource());
            Text[] titles = hit.getHighlightFields().get("title").getFragments();
            if(titles != null) {
                for(Text str : titles) {
                    System.out.println(str.string());
                }
            }
        }
    }


    private static TransportClient getClient() throws Exception {
        Settings settings = Settings.builder().put("cluster.name", CLUSTER_NAME).build();
        TransportClient client = new PreBuiltTransportClient(settings).addTransportAddress(
                new InetSocketTransportAddress(InetAddress.getByName(HOST_IP), TCP_PORT)
        );
        return client;
    }
}
