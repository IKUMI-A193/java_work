import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class TodoCsv {
    private TodoCsv() {}

    static List<Todo> load(Path path) throws IOException {
        String csv = Files.readString(path, StandardCharsets.UTF_8);
        List<Todo> result = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        // 0: field start, 1: plain field, 2: quoted field, 3: closing quote
        int state = 0;
        for (int i = 0; i < csv.length(); i++) {
            char ch = csv.charAt(i);
            if (state == 2) {
                if (ch == '"') {
                    if (i + 1 < csv.length() && csv.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        state = 3;
                    }
                } else {
                    field.append(ch);
                }
            } else if (ch == ',') {
                fields.add(field.toString());
                field.setLength(0);
                state = 0;
            } else if (ch == '\n' || ch == '\r') {
                fields.add(field.toString());
                addRow(result, ids, fields);
                fields.clear();
                field.setLength(0);
                state = 0;
                if (ch == '\r' && i + 1 < csv.length() && csv.charAt(i + 1) == '\n') i++;
            } else if (state == 0 && ch == '"') {
                state = 2;
            } else if (state == 3 || ch == '"') {
                throw new IOException("CSVの引用符が不正です (位置 " + i + ")");
            } else {
                field.append(ch);
                state = 1;
            }
        }
        if (state == 2) throw new IOException("CSVの引用符が閉じられていません");
        if (state != 0 || !fields.isEmpty() || field.length() > 0) {
            fields.add(field.toString());
            addRow(result, ids, fields);
        }
        return result;
    }

    private static void addRow(List<Todo> result, Set<Integer> ids, List<String> fields) throws IOException {
        if (fields.size() != 3) throw new IOException("CSVの列数が3ではありません (行 " + (result.size() + 1) + ")");
        int id;
        try {
            id = Integer.parseInt(fields.get(0));
        } catch (NumberFormatException e) {
            throw new IOException("CSVのIDが不正です (行 " + (result.size() + 1) + ")", e);
        }
        if (id <= 0 || id == Integer.MAX_VALUE || !ids.add(id)) {
            throw new IOException("CSVのIDが無効または重複しています: " + id);
        }
        String doneText = fields.get(2);
        if (!doneText.equals("true") && !doneText.equals("false")) {
            throw new IOException("CSVの完了状態が不正です (ID " + id + ")");
        }
        Todo todo = new Todo(id, fields.get(1));
        todo.setDone(Boolean.parseBoolean(doneText));
        result.add(todo);
    }

    static void save(Path path, List<Todo> todos) throws IOException {
        Path parent = path.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        StringBuilder csv = new StringBuilder();
        for (Todo todo : todos) {
            csv.append(todo.getId()).append(',').append('"')
                    .append(todo.getTitle().replace("\"", "\"\""))
                    .append('"').append(',').append(todo.isDone()).append('\n');
        }
        Path temp = Files.createTempFile(parent, "todos-", ".tmp");
        try {
            Files.writeString(temp, csv, StandardCharsets.UTF_8);
            Files.move(temp, path.toAbsolutePath(), StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}
