import com.sun.net.httpserver.HttpServer; // Webサーバーの機能を使います
import java.net.InetSocketAddress; // 待ち受ける番号を指定するために使います
public class App { // Appという名前のプログラムです
    public static void main(String[] args) throws Exception { // ここから実行を始めます
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0); // 8081番でサーバーを用意します
        server.createContext("/", exchange -> { // 「/」へのアクセスを処理します
            String message = "Hello, Server!"; // 返す文字を決めます
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 文字をUTF-8として返す設定です
            byte[] body = message.getBytes("UTF-8"); // 文字を送信用のデータに変えます
            exchange.sendResponseHeaders(200, body.length); // 正常に返すこととデータの長さを伝えます
            exchange.getResponseBody().write(body); // データを送ります
            exchange.getResponseBody().close(); // 送信を終えます
        }); // アクセス時の処理を閉じます
        server.start(); // サーバーを起動します
        System.out.println("サーバー起動: http://localhost:8081 （止めるときは Ctrl+C）"); // 起動したことを表示します
    } // 実行部分を閉じます
} // プログラムを閉じます
