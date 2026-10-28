import com.sun.net.httpserver.HttpServer; // Webサーバーの機能を使います
import java.net.InetSocketAddress; // 待ち受ける番号を指定するために使います
import java.net.URLDecoder; // 送られたTodoの文字を元に戻すために使います
import java.nio.charset.StandardCharsets; // 日本語をUTF-8として読むために使います
import java.util.ArrayList; // Todoを入れるリストを作るために使います
import java.util.List; // Todoを入れるリストの型を使います
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
public class App { // Appという名前のプログラムです
    static List<Todo> todos = new ArrayList<>(); // ★変更
    static int nextId = 1; // ★変更
    static final Path CSV_PATH = Path.of("data", "todos.csv");

    public static void main(String[] args) throws Exception { // ここから実行を始めます
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0); // 8081番でサーバーを用意します
        if (!Files.notExists(CSV_PATH)) {
            try {
                todos.addAll(TodoCsv.load(CSV_PATH));
            } catch (IOException e) {
                System.err.println("CSVの読み込みに失敗しました: " + CSV_PATH.toAbsolutePath());
                throw e;
            }
            for (Todo todo : todos) {
                if (todo.getId() >= nextId) nextId = todo.getId() + 1;
            }
        } else {
            todos.add(new Todo(nextId++, "牛乳を買う"));
            Todo egg = new Todo(nextId++, "卵を買う");
            egg.setDone(true);
            todos.add(egg);
            TodoCsv.save(CSV_PATH, todos);
        }
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
                    List<Todo> updated = new ArrayList<>(todos);
                    updated.add(new Todo(nextId, title));
                    if (!saveOrReport(exchange, updated)) return;
                    todos = updated;
                    nextId++;
                } // 空の入力かどうかの確認を終えます
                exchange.getResponseHeaders().set("Location", "/"); // 戻り先を「/」にします
                exchange.sendResponseHeaders(303, -1); // 「/」へ移動する応答を返します
                exchange.close(); // 応答を閉じます
                return; // 追加の処理をここで終えます
            } else if (path.equals("/save") && method.equals("POST")) {
                Map<String, String> form = readForm(exchange);
                int id;
                try {
                    id = Integer.parseInt(form.getOrDefault("id", ""));
                } catch (NumberFormatException e) {
                    sendText(exchange, 400, "IDが不正です");
                    return;
                }
                String title = form.getOrDefault("title", "");
                if (title.isBlank()) {
                    sendText(exchange, 400, "TODOの内容を入力してください");
                    return;
                }
                Todo.Progress progress;
                LocalDate createdDate;
                LocalDate dueDate;
                LocalDate completedDate;
                try {
                    progress = form.containsKey("progress")
                            ? Todo.Progress.valueOf(form.get("progress")) : null;
                    createdDate = parseDate(form.get("createdDate"));
                    dueDate = parseDate(form.get("dueDate"));
                    completedDate = parseDate(form.get("completedDate"));
                } catch (IllegalArgumentException e) {
                    sendText(exchange, 400, "進捗または日付が不正です");
                    return;
                }
                List<Todo> updated = new ArrayList<>();
                boolean found = false;
                for (Todo todo : todos) {
                    if (todo.getId() == id) {
                        Todo edited = todo.copy();
                        edited.setTitle(title);
                        if (form.containsKey("createdDate")) edited.setCreatedDate(createdDate);
                        if (form.containsKey("dueDate")) edited.setDueDate(dueDate);
                        if (progress != null) edited.setProgress(progress);
                        if (edited.isDone() && form.containsKey("completedDate")) {
                            if (todo.isDone() || completedDate != null) {
                                edited.setCompletedDate(completedDate);
                            }
                        }
                        updated.add(edited);
                        found = true;
                    } else {
                        updated.add(todo);
                    }
                }
                if (!found) {
                    sendText(exchange, 404, "TODOが見つかりません");
                    return;
                }
                if (!saveOrReport(exchange, updated)) return;
                todos = updated;
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(303, -1);
                exchange.close();
                return;
            } else if (path.equals("/delete") && method.equals("POST")) {
                String idValue = readForm(exchange).get("id");
                if (idValue != null) {
                    try { // ★追加
                        int id = Integer.parseInt(idValue);
                        List<Todo> updated = new ArrayList<>(todos);
                        if (updated.removeIf(todo -> todo.getId() == id)) {
                            if (!saveOrReport(exchange, updated)) return;
                            todos = updated;
                        }
                    } catch (NumberFormatException e) { // ★追加
                    } // ★追加
                } // ★追加
                exchange.getResponseHeaders().set("Location", "/"); // ★追加
                exchange.sendResponseHeaders(303, -1); // ★追加
                exchange.close(); // ★追加
                return; // ★追加
            } else if (path.equals("/")) { // 「/」でフォームと一覧を表示します
                Todo editing = null;
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.startsWith("edit=")) {
                    try {
                        int id = Integer.parseInt(query.substring(5));
                        for (Todo todo : todos) {
                            if (todo.getId() == id) { editing = todo; break; }
                        }
                    } catch (NumberFormatException e) {
                        // 不正なIDでは通常の一覧を表示します。
                    }
                }
                message = BoardView.render(todos, editing);
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

    private static boolean saveOrReport(com.sun.net.httpserver.HttpExchange exchange, List<Todo> updated) throws IOException {
        try {
            TodoCsv.save(CSV_PATH, updated);
            return true;
        } catch (IOException e) {
            System.err.println("CSVの保存に失敗しました: " + e.getMessage());
            byte[] body = "CSVの保存に失敗しました".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
            return false;
        }
    }

    private static Map<String, String> readForm(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> result = new HashMap<>();
        for (String part : body.split("&")) {
            String[] pair = part.split("=", 2);
            String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
            String value = URLDecoder.decode(pair.length == 2 ? pair[1] : "", StandardCharsets.UTF_8);
            result.put(key, value);
        }
        return result;
    }

    private static void sendText(com.sun.net.httpserver.HttpExchange exchange, int status, String message) throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(e);
        }
    }
} // プログラムを閉じます

class Todo { // ★変更
    enum Progress { NONE, IN_PROGRESS, DONE }
    private final int id; // ★変更
    private String title;
    private Progress progress = Progress.NONE;
    private LocalDate createdDate;
    private LocalDate dueDate;
    private LocalDate completedDate;

    Todo(int id, String title) { // ★変更
        this.id = id; // ★変更
        this.title = title; // ★変更
        this.createdDate = LocalDate.now();
    } // ★変更

    int getId() { // ★変更
        return id; // ★変更
    } // ★変更

    String getTitle() { // ★変更
        return title; // ★変更
    } // ★変更

    void setTitle(String title) { this.title = title; }

    Progress getProgress() { return progress; }
    LocalDate getCreatedDate() { return createdDate; }
    LocalDate getDueDate() { return dueDate; }
    LocalDate getCompletedDate() { return completedDate; }
    void setCreatedDate(LocalDate date) { createdDate = date; }
    void setDueDate(LocalDate date) { dueDate = date; }
    void setCompletedDate(LocalDate date) { completedDate = date; }

    void setProgress(Progress value) {
        if (value == Progress.DONE && progress != Progress.DONE) completedDate = LocalDate.now();
        if (value != Progress.DONE && progress == Progress.DONE) completedDate = null;
        progress = value;
    }

    void setLegacyDone(boolean done) { progress = done ? Progress.DONE : Progress.NONE; }

    Todo copy() {
        Todo copy = new Todo(id, title);
        copy.progress = progress;
        copy.createdDate = createdDate;
        copy.dueDate = dueDate;
        copy.completedDate = completedDate;
        return copy;
    }

    boolean isDone() { // ★変更
        return progress == Progress.DONE;
    } // ★変更

    void setDone(boolean done) { // ★変更
        setProgress(done ? Progress.DONE : Progress.NONE);
    } // ★変更
} // ★変更
