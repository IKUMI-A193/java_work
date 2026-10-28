import com.sun.net.httpserver.HttpServer; // Webサーバーの機能を使います
import java.net.InetSocketAddress; // 待ち受ける番号を指定するために使います
import java.net.URLDecoder; // 送られたTodoの文字を元に戻すために使います
import java.nio.charset.StandardCharsets; // 日本語をUTF-8として読むために使います
import java.util.ArrayList; // Todoを入れるリストを作るために使います
import java.util.List; // Todoを入れるリストの型を使います
public class App { // Appという名前のプログラムです
    static List<Todo> todos = new ArrayList<>(); // ★変更
    static int nextId = 1; // ★変更

    public static void main(String[] args) throws Exception { // ここから実行を始めます
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0); // 8081番でサーバーを用意します
        todos.add(new Todo(nextId++, "牛乳を買う")); // ★変更
        Todo egg = new Todo(nextId++, "卵を買う"); // ★変更
        egg.setDone(true); // ★変更
        todos.add(egg); // ★変更
        server.createContext("/", exchange -> { // 「/」へのアクセスを処理します
            String path = exchange.getRequestURI().getPath(); // アクセスされたパスを取り出します
            String method = exchange.getRequestMethod(); // GETかPOSTかを取り出します
            String message;
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 文字をUTF-8として返す設定です
            if (path.equals("/add") && method.equals("POST")) { // Todoの追加先へのPOSTか比べます
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); // 送られた中身を読みます
                String value = body.substring(5); // 「todo=」の後ろを取り出します
                String title = URLDecoder.decode(value, StandardCharsets.UTF_8); // ★変更
                if (!title.isEmpty()) { // ★変更
                    todos.add(new Todo(nextId, title)); // ★変更
                    nextId++; // ★変更
                } // 空の入力かどうかの確認を終えます
                exchange.getResponseHeaders().set("Location", "/"); // 戻り先を「/」にします
                exchange.sendResponseHeaders(303, -1); // 「/」へ移動する応答を返します
                exchange.close(); // 応答を閉じます
                return; // 追加の処理をここで終えます
            } else if (path.equals("/done") && method.equals("GET")) { // ★追加
                String query = exchange.getRequestURI().getQuery(); // ★追加
                if (query != null && query.startsWith("id=") && query.length() > 3) { // ★追加
                    try { // ★追加
                        int id = Integer.parseInt(query.substring(3)); // ★追加
                        for (Todo todo : todos) { // ★追加
                            if (todo.getId() == id) { // ★追加
                                todo.setDone(true); // ★追加
                                break; // ★追加
                            } // ★追加
                        } // ★追加
                    } catch (NumberFormatException e) { // ★追加
                    } // ★追加
                } // ★追加
                exchange.getResponseHeaders().set("Location", "/"); // ★追加
                exchange.sendResponseHeaders(303, -1); // ★追加
                exchange.close(); // ★追加
                return; // ★追加
            } else if (path.equals("/delete") && method.equals("GET")) { // ★追加
                String query = exchange.getRequestURI().getQuery(); // ★追加
                if (query != null && query.startsWith("id=") && query.length() > 3) { // ★追加
                    try { // ★追加
                        int id = Integer.parseInt(query.substring(3)); // ★追加
                        todos.removeIf(todo -> todo.getId() == id); // ★追加
                    } catch (NumberFormatException e) { // ★追加
                    } // ★追加
                } // ★追加
                exchange.getResponseHeaders().set("Location", "/"); // ★追加
                exchange.sendResponseHeaders(303, -1); // ★追加
                exchange.close(); // ★追加
                return; // ★追加
            } else if (path.equals("/")) { // 「/」でフォームと一覧を表示します
                String html = "<!doctype html><html><head><meta charset='UTF-8'><title>わたしのTodo</title>"
                        + "<style>body{max-width:640px;margin:24px auto;padding:0 16px;font-size:16px}h1{font-size:24px}</style>"
                        + "</head><body><h1>わたしのTodo</h1>"
                        + "<form method='post' action='/add'><input name='todo'><button>追加</button></form>"; // Todoの入力フォームを作ります
                if (todos.isEmpty()) {
                    html += "<p>やることは、いまゼロです</p>";
                }
                html += "<ul>"; // HTMLの箇条書きを始めます
                for (Todo todo : todos) { // ★変更
                    String mark = ""; // ★変更
                    if (todo.isDone()) { // ★変更
                        mark = " ✔"; // ★変更
                    } // ★変更
                    html += "<li>" + todo.getTitle() + mark + " <a href='/done?id=" + todo.getId() + "'>完了</a> <a href='/delete?id=" + todo.getId() + "'>削除</a></li>"; // ★追加
                } // Todoを全部取り出したら繰り返しを終えます
                html += "</ul></body></html>"; // HTMLの箇条書きを閉じます
                message = html; // 作ったHTMLを返す文字にします
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); // このパスだけHTMLとして返します
            } else {
                message = "ページが見つかりません";
            }
            byte[] body = message.getBytes("UTF-8"); // 文字を送信用のデータに変えます
            exchange.sendResponseHeaders(200, body.length); // 正常に返すこととデータの長さを伝えます
            exchange.getResponseBody().write(body); // データを送ります
            exchange.getResponseBody().close(); // 送信を終えます
        }); // アクセス時の処理を閉じます
        server.start(); // サーバーを起動します
        System.out.println("サーバー起動: http://localhost:8081 （止めるときは Ctrl+C）"); // 起動したことを表示します
    } // 実行部分を閉じます
} // プログラムを閉じます

class Todo { // ★変更
    private final int id; // ★変更
    private final String title; // ★変更
    private boolean done; // ★変更

    Todo(int id, String title) { // ★変更
        this.id = id; // ★変更
        this.title = title; // ★変更
        this.done = false; // ★変更
    } // ★変更

    int getId() { // ★変更
        return id; // ★変更
    } // ★変更

    String getTitle() { // ★変更
        return title; // ★変更
    } // ★変更

    boolean isDone() { // ★変更
        return done; // ★変更
    } // ★変更

    void setDone(boolean done) { // ★変更
        this.done = done; // ★変更
    } // ★変更
} // ★変更
