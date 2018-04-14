package chapter05.$5_3_4;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 5.3.4 清单 5-6 使用同步 API 获取子节点列表
 */
public class ZooKeeperGetChildrenAPISyncUsage {

    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
    private static ZooKeeper zooKeeper = null;

    public static void main(String[] args) throws Exception {

        zooKeeper = new ZooKeeper("localhost:2181", 5000,
                event -> {

                    System.out.println(event);

                    if (Watcher.Event.KeeperState.SyncConnected == event.getState()) {
                        // 成功连接服务器
                        if (Watcher.Event.EventType.None == event.getType() && null == event.getPath()) {
                            // 解除阻塞
                            connectedSemaphore.countDown();
                            // 子节点变更
                        } else if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                            try {
                                // 重新获取子节点
                                System.out.println("重新获取子节点：" + zooKeeper.getChildren(event.getPath(), true));
                            } catch (Exception e) {
                            }
                        }
                    }
                });

        // 阻塞，成功连接服务器后再解除。
        connectedSemaphore.await();

        /*
        * 在服务器创建测试节点 create /zk-test Hello
        * 程序运行中创建子节点
        * create /zk-test/c1 Hello
        * create /zk-test/c2 Hello
        * 观察子节点变化通知
        * */
        String path = "/zk-test";
        List<String> childrenList = zooKeeper.getChildren(path, true);
        System.out.println("第一次获取子节点：" + childrenList);

        // 阻塞，不要让程序结束，因为要监听子节点的变化。
        Thread.sleep(Integer.MAX_VALUE);
    }
}