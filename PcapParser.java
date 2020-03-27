import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class PcapParser {
    private static int start_time = -1;
    private static int end_time = -1;
    private static Integer[] IPs = new Integer[2];
    private static Short[] ports = new Short[2];  // (ports[0]&0xFFFF)避免输出负数
    private static String app = "";
    private static String action = "";
    private static int total_package_number = 0;
    private static long total_byte_number = 0;  // 平均每包字节数可由这两个字段求得

    public static void main(String[] args) {
        // {"ad_DouYin_V10.2_live_1.cap", "ad_Hangouts_V32.0_videocall_1.cap", "ad_Hangouts_V32.0_videocallon_1.cap"}
        String filepathStr = "ad_DouYin_V10.2_live_1.cap";
        if (args.length > 0) filepathStr = args[0];
        String[] tmp = filepathStr.split("_");
        app = tmp[1];
        action = tmp[3];
        boolean foundBySYN = false;
        try (RandomAccessFile file = new RandomAccessFile(filepathStr, "r")) {
            FileChannel channel = file.getChannel();
            channel.position(24);
            ByteBuffer buf = ByteBuffer.allocate(16);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            ByteBuffer data = ByteBuffer.allocate(1514);
            while (channel.read(buf) != -1) {
                buf.flip();
                end_time = buf.getInt();  // s
                buf.getInt();  // ns
                if (start_time == -1) start_time = end_time;
                int caplen = buf.getInt();
                int len = buf.getInt();
                assert caplen == len;  // -ea
                total_byte_number += len;
                total_package_number++;
                data.limit(len);
                channel.read(data);
                data.flip();
                byte protocol = data.get(23);
                //0x06 TCP协议, 0x11 UDP
                byte flags = protocol == 0x06 ? data.get(47) : 0; // 14 + 20 + 13,
                if (flags == 0x02 || ports[0] == null) {  // SYN 0x02 // ACK SYC 0x12
                    foundBySYN = flags == 0x02;
                    data.position(26);
                    IPs[0] = data.getInt(); // 14 + 12,
                    IPs[1] = data.getInt();
                    ports[0] = data.getShort();
                    ports[1] = data.getShort();
                }
                data.clear();
                buf.clear();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        if (!foundBySYN) {
            correct();
        }
        System.out.println(start_time + "," + (end_time - start_time) + "," + convert(IPs[0]) + "," + (ports[0]&0xFFFF) + "," + convert(IPs[1])
                + "," + (ports[1]&0xFFFF) + "," + app + "," + action + "," + total_package_number + "," + total_byte_number + ","
                + String.format("%.2f", total_package_number == 0 ? 0 : 1.0 * total_byte_number / total_package_number));
    }

    private static <T> void swap(T[] ary) {
        T tmp = ary[0];
        ary[0] = ary[1];
        ary[1] = tmp;
    }

    private static void correct() {  // 按照老师的要求区分客户端和服务器端
        if (ports[0] > ports[1]) {
            swap(ports);
            swap(IPs);
        } else if (ports[0] == ports[1]) {
            int hostOrder0 = getHostOrder(IPs[0]);
            int hostOrder1 = getHostOrder(IPs[1]);
            if (hostOrder0 > hostOrder1) {
                swap(ports);
                swap(IPs);
            }
        }
    }

    private static String convert(int ip) {
        return String.format("%d.%d.%d.%d", (ip & 0xFF000000) >>> 24, (ip & 0xFF0000) >> 16, (ip & 0xFF00) >> 8, ip & 0xFF);
    }

    private static int getHostOrder(int ip) {
        if ((ip >>> 23) == 0) { // A类IP
            return ip & 0xFFFFFF;
        } else if ((ip >>> 22) == 0b10) { // B类IP
            return ip & 0xFFFF;
        } else if ((ip >>> 21) == 0b110) { // C类IP
            return ip & 0xFF;
        }
        return -1; // D类、E类IP没有主机号
    }
}
