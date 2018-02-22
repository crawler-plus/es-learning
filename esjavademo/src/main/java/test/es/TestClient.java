package test.es;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;

/**
 * java链接es测试
 */
public class TestClient {

    // 集群名称
    private static String CLUSTER_NAME = "elasticsearch";

    // 服务器IP
    private static String HOST_IP = "127.0.0.1";

    // 端口
    private static int TCP_PORT = 9300;

    public static void main(String[] args) throws Exception {
        Settings settings = Settings.builder().put("cluster.name", CLUSTER_NAME).build();
        TransportClient client = new PreBuiltTransportClient(settings).addTransportAddress(
                new InetSocketTransportAddress(InetAddress.getByName(HOST_IP), TCP_PORT)
        );
        GetResponse getResponse = client.prepareGet("books", "IT", "1").get();
        System.out.println(getResponse.getSourceAsString());
    }
}
