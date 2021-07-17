import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;

import java.io.IOException;

public class HBaseTest {
    public static void main(String[] args) {
        // 配置hbase的主节点ip
        String master = "cc-keybrl-node0";
        // 配置hbase的zookeeper节点ip
        String zookeeper = "cc-keybrl-node0,cc-keybrl-node1,cc-keybrl-node2";

        Configuration hbaseConf = new Configuration();
        hbaseConf.set("hbase.master", master);
        hbaseConf.set("hbase.zookeeper.quorum", zookeeper);

        try {
            Connection connection = ConnectionFactory.createConnection(hbaseConf);
            Admin admin = connection.getAdmin();
            TableName table = TableName.valueOf("test_table");
            if (admin.tableExists(table)) {

                admin.disableTable(table);
                admin.deleteTable(table);
                System.out.println("表 'test_table' 已存在，已删除之！");

            }
            else {

                HTableDescriptor hTableDescriptor = new HTableDescriptor(table);
                HColumnDescriptor hColumnDescriptor = new HColumnDescriptor("what");
                hTableDescriptor.addFamily(hColumnDescriptor);
                admin.createTable(hTableDescriptor);

                System.out.println("表 'test_table' 不存在，已创建之！");

            }
            admin.close();
            connection.close();

        } catch (IOException e) {
            System.out.println("某些诡异的错误：" + e.getMessage());
        }
    }
}
