import java.util.List;
import java.time.LocalDate;

final class BoardView {
    private static final String[] PAPERS = {
            "yellow", "pink", "blue", "kraft", "green", "lavender"
    };

    private BoardView() {}

    static String render(List<Todo> todos, Todo editing) {
        int openCount = 0;
        int doneCount = 0;
        for (Todo todo : todos) {
            if (todo.isDone()) doneCount++;
            else openCount++;
        }

        StringBuilder html = new StringBuilder();
        html.append("<!doctype html><html lang='ja'><head><meta charset='UTF-8'>")
                .append("<meta name='viewport' content='width=device-width, initial-scale=1'>")
                .append("<title>わたしのTodo | コルクボード</title><style>")
                .append(CSS)
                .append("</style></head><body><main class='page'>")
                .append("<header class='page-header'><h1>わたしのTodo</h1>")
                .append("<div class='summary' aria-label='TODOの件数'><span>これから <strong>")
                .append(openCount).append("</strong></span><span>できた <strong>")
                .append(doneCount).append("</strong></span></div></header>")
                .append("<form class='add-form' method='post' action='/add'>")
                .append("<label for='todo'>新しいメモ</label><div class='add-form__row'>")
                .append("<input id='todo' name='todo' type='text' placeholder='やることを書いてください' required>")
                .append("<button type='submit'>＋ メモを貼る</button></div></form>")
                .append("<div class='board'><section class='board-section' aria-labelledby='open-title'>")
                .append("<div class='section-heading'><h2 id='open-title'>これからのメモ</h2><span>")
                .append(openCount).append(" 枚</span></div><div class='notes'>");
        for (Todo todo : todos) {
            if (!todo.isDone()) appendNote(html, todo);
        }
        if (openCount == 0) {
            html.append("<p class='empty'>やることは、いまゼロです。新しいメモを貼ってみましょう。</p>");
        }
        html.append("</div></section><section class='board-section board-section--done' aria-labelledby='done-title'>")
                .append("<div class='section-heading'><h2 id='done-title'>できたメモ</h2><span>")
                .append(doneCount).append(" 枚</span></div><div class='notes'>");
        for (Todo todo : todos) {
            if (todo.isDone()) appendNote(html, todo);
        }
        if (doneCount == 0) {
            html.append("<p class='empty'>完了したメモは、ここに並びます。</p>");
        }
        html.append("</div></section></div>");
        if (editing != null) {
            html.append("<div class='modal-backdrop'><section class='edit-modal' role='dialog' aria-modal='true' aria-labelledby='edit-title'>")
                    .append("<h2 id='edit-title'>メモを編集</h2>")
                    .append("<form method='post' action='/save'><input type='hidden' name='id' value='")
                    .append(editing.getId()).append("'>")
                    .append("<label for='edit-text'>TODOの内容</label>")
                    .append("<textarea id='edit-text' name='title' rows='8' required autofocus>")
                    .append(escapeHtml(editing.getTitle())).append("</textarea>")
                    .append("<div class='edit-fields'><label for='edit-progress'>進捗")
                    .append("<select id='edit-progress' name='progress'>")
                    .append(option("NONE", "未選択", editing.getProgress()))
                    .append(option("IN_PROGRESS", "進行中", editing.getProgress()))
                    .append(option("DONE", "済", editing.getProgress()))
                    .append("</select></label>")
                    .append("<label for='edit-created'>作成日<input id='edit-created' name='createdDate' type='date' value='")
                    .append(date(editing.getCreatedDate())).append("'></label>")
                    .append("<label for='edit-due'>期限日<input id='edit-due' name='dueDate' type='date' value='")
                    .append(date(editing.getDueDate())).append("'></label>")
                    .append("<label for='edit-completed'>完了日<input id='edit-completed' name='completedDate' type='date' value='")
                    .append(date(editing.getCompletedDate())).append("'></label></div>")
                    .append("<p class='date-hint'>「済」に変更すると完了日を今日の日付で設定します。後から変更できます。</p>")
                    .append("<div class='edit-actions'><a href='/'>キャンセル</a><button type='submit'>保存</button></div>")
                    .append("</form></section></div>");
        }
        return html.append("</main></body></html>").toString();
    }

    private static void appendNote(StringBuilder html, Todo todo) {
        String paper = PAPERS[(todo.getId() - 1) % PAPERS.length];
        html.append("<article class='note note--").append(paper);
        if (todo.isDone()) html.append(" note--done");
        html.append("'><span class='pin' aria-hidden='true'></span>")
                .append("<a class='note__open' href='/?edit=").append(todo.getId())
                .append("' aria-label='メモ ").append(todo.getId()).append(" を編集'></a>")
                .append("<div class='note__top'><span>No. ")
                .append(todo.getId()).append("</span>");
        if (todo.isDone()) html.append("<span class='done-badge'>✅ 済</span>");
        else if (todo.getProgress() == Todo.Progress.IN_PROGRESS) {
            html.append("<span class='progress-badge'>進行中</span>");
        }
        html.append("</div><a class='note__text' href='/?edit=").append(todo.getId()).append("'>")
                .append(escapeHtml(todo.getTitle()))
                .append("</a><dl class='note__dates'>");
        appendDateRow(html, "作成日", todo.getCreatedDate(), false);
        if (todo.getDueDate() != null) {
            boolean overdue = !todo.isDone() && todo.getDueDate().isBefore(LocalDate.now());
            appendDateRow(html, "期限日", todo.getDueDate(), overdue);
        }
        if (todo.isDone()) appendDateRow(html, "完了日", todo.getCompletedDate(), false);
        html.append("</dl>");
        html.append("<div class='note__actions'>");
        html.append("<form method='post' action='/delete' onsubmit=\"return confirm('このTODOを削除しますか？')\">")
                .append("<input type='hidden' name='id' value='").append(todo.getId()).append("'>")
                .append("<button type='submit' aria-label='メモ ").append(todo.getId()).append(" を削除'>削除</button></form>")
                .append("</div></article>");
    }

    private static String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static String date(LocalDate value) {
        return value == null ? "" : value.toString();
    }

    private static void appendDateRow(StringBuilder html, String label, LocalDate value, boolean overdue) {
        html.append("<div class='note__date");
        if (overdue) html.append(" note__due--overdue");
        html.append("'><dt>").append(label).append("</dt><dd>");
        if (overdue) html.append("⚠ ");
        if (value == null) html.append("未記録");
        else html.append("<time datetime='").append(date(value)).append("'>")
                .append(date(value)).append("</time>");
        html.append("</dd></div>");
    }

    private static String option(String value, String label, Todo.Progress selected) {
        return "<option value='" + value + "'" +
                (selected.name().equals(value) ? " selected" : "") + ">" + label + "</option>";
    }

    private static final String CSS = """
            :root{font-family:'Yu Gothic',Meiryo,sans-serif;color:#48372a;background:#e9dfcf}
            *{box-sizing:border-box}
            body{margin:0;min-width:280px;background:repeating-linear-gradient(95deg,#fff5 0 1px,transparent 2px 8px),linear-gradient(130deg,#e8ddca,#f6efe3 50%,#e5d6bd)}
            .page{max-width:1440px;margin:auto;padding:15px clamp(12px,2.5vw,34px) 38px}
            .page-header{display:flex;justify-content:space-between;align-items:baseline;gap:15px;margin:0 5px 12px}
            h1{margin:0;font-size:clamp(19px,2.4vw,27px);font-weight:800;letter-spacing:.04em;color:#63472e}
            .summary{display:flex;gap:16px;flex-wrap:wrap;color:#715d49;font-size:12px}
            .summary span{white-space:nowrap}.summary strong{font-size:15px;margin-left:3px;color:#6d4428}
            .add-form{position:relative;width:min(100%,610px);margin:0 0 10px 16px;padding:12px 18px 15px;background:repeating-linear-gradient(0deg,#fff9ef 0 27px,#e8d4bf55 28px 29px);box-shadow:1px 5px 9px #5a361f48;transform:rotate(-.5deg)}
            .add-form:before{content:'';position:absolute;width:68px;height:21px;top:-12px;left:43%;transform:rotate(3deg);background:#e9d6a3ad;border-left:1px solid #bba77c88;border-right:1px solid #bba77c88;box-shadow:0 1px 2px #5b3f2622}
            .add-form label{display:block;font-size:12px;font-weight:800;margin-bottom:7px;color:#6d5541}
            .add-form__row{display:flex;gap:9px}
            .add-form input{flex:1;min-width:0;border:0;border-bottom:1px solid #ad9b84;background:#fff9;padding:8px 9px;font:inherit;font-size:14px;color:#3e3026}
            .add-form input:focus{outline:2px solid #966d48;outline-offset:2px}
            .add-form button{border:1px solid #65452d;border-radius:3px;background:linear-gradient(#916847,#765135);color:#fff9ee;padding:8px 15px;font:inherit;font-size:13px;font-weight:800;cursor:pointer;white-space:nowrap;box-shadow:1px 2px 2px #60412755}
            .add-form button:hover{background:#67452d}
            .board{position:relative;isolation:isolate;min-height:65vh;border:14px ridge #a66e3b;padding:clamp(20px,3vw,44px);background-color:#b88958;background-image:radial-gradient(circle at 20% 40%,#f3d5a366 0 1px,transparent 2px),radial-gradient(circle at 60% 15%,#613b1d77 0 1px,transparent 2px),radial-gradient(circle at 75% 65%,#f1cc8a77 0 1px,transparent 2px),radial-gradient(circle at 45% 78%,#5f341d66 0 1px,transparent 2px),repeating-linear-gradient(28deg,#5e341b0c 0 2px,#e7bc7b0d 3px 7px,transparent 8px 14px),linear-gradient(125deg,#c69661,#b17b4b 55%,#c2935f);background-size:13px 17px,21px 19px,27px 31px,33px 25px,auto,auto;box-shadow:inset 0 0 0 3px #dfad72,inset 0 0 38px #58321699,0 12px 22px #60442770}
            .board:before{content:'';position:absolute;inset:0;z-index:-1;pointer-events:none;background:repeating-radial-gradient(circle at 10% 12%,#fff8 0 1px,transparent 2px 5px);opacity:.12}
            .board-section+.board-section{margin-top:34px;padding-top:25px;border-top:2px dashed #f2d2a28c}
            .section-heading{display:flex;align-items:center;gap:10px;margin-bottom:24px;color:#fff8e8;text-shadow:1px 2px 3px #5a371f}
            .section-heading h2{font-size:clamp(16px,2vw,20px);margin:0;letter-spacing:.07em}
            .section-heading span{font-size:12px}
            .notes{display:grid;grid-template-columns:repeat(auto-fill,minmax(min(100%,220px),1fr));gap:30px 26px;align-items:start}
            .note{position:relative;display:flex;flex-direction:column;height:240px;min-width:0;padding:31px 18px 15px;color:#3e332b;background:#fff6cc;filter:drop-shadow(1px 2px 1px #35201255) drop-shadow(5px 9px 5px #3e241a70);transform:rotate(-1.2deg)}
            .note:nth-child(2n){transform:rotate(1.3deg)}.note:nth-child(3n){transform:rotate(-.5deg)}
            .pin{position:absolute;top:7px;left:50%;z-index:2;width:20px;height:20px;border-radius:50%;transform:translateX(-50%);background:radial-gradient(circle at 31% 26%,#fffde5 0 9%,#f5a18c 18%,#bd493b 55%,#641e1c 100%);border:1px solid #6d2722;box-shadow:2px 4px 3px #40241899,inset -2px -3px 3px #4c171b88,inset 1px 1px 2px #fff9}
            .pin:after{content:'';position:absolute;top:20px;left:13px;width:12px;height:5px;border-radius:50%;background:#4e25184d;filter:blur(2px);transform:rotate(-25deg)}
            .pin{pointer-events:none}
            .note__open{position:absolute;inset:0;z-index:1}
            .note__open:focus-visible{outline:3px solid #65452d;outline-offset:3px}
            .note__top{display:flex;justify-content:space-between;align-items:center;gap:6px;min-height:20px;color:#67594d;font-size:11px;font-weight:800;letter-spacing:.04em}
            .note__text{position:relative;z-index:2;display:block;flex:1;min-height:0;overflow:auto;margin:10px 0 6px;color:inherit;font-size:17px;font-weight:700;line-height:1.45;text-decoration:none;white-space:pre-wrap;overflow-wrap:anywhere}
            .note__dates{display:grid;gap:2px;margin:0 0 7px;font-size:11px;font-weight:700;line-height:1.3;color:#56483c}
            .note__date{display:flex;justify-content:space-between;gap:5px}
            .note__date dt,.note__date dd{margin:0}
            .note__date dd{white-space:nowrap;font-variant-numeric:tabular-nums}
            .note__due--overdue{color:#a12626}
            .note__actions{position:relative;z-index:2;display:flex;gap:15px;align-items:center;border-top:1px solid #6a4d323d;padding-top:10px;font-size:13px;font-weight:800}
            .note__actions a{color:inherit;text-decoration:none;border-bottom:1px solid currentColor;padding-bottom:2px}
            .note__actions form{margin-left:auto}
            .note__actions button{border:0;border-bottom:1px solid currentColor;padding:0 0 2px;background:none;color:inherit;font:inherit;cursor:pointer}
            .note__actions a:hover,.note__actions button:hover{color:#9b4936}
            .done-badge{border-radius:3px;padding:3px 6px;background:#fff9;color:#3b6641;font-size:11px;letter-spacing:0;white-space:nowrap}
            .progress-badge{border-radius:3px;padding:3px 6px;background:#fff9;color:#705225;font-size:11px;white-space:nowrap}
            .note--done{filter:drop-shadow(1px 2px 1px #35201255) drop-shadow(4px 7px 4px #3e241a66)}
            .note--yellow{background:#fff4c9;box-shadow:inset 0 -12px 18px #d9bd6a14}
            .note--pink{background:#f9dce1;clip-path:polygon(0 3%,5% 2%,9% 4%,14% 1%,19% 3%,25% 0,31% 3%,38% 1%,44% 3%,51% 0,58% 3%,65% 1%,72% 4%,80% 1%,87% 3%,93% 1%,100% 3%,100% 95%,94% 98%,89% 96%,83% 100%,77% 97%,71% 99%,65% 96%,59% 100%,53% 97%,47% 99%,41% 96%,35% 100%,29% 97%,23% 99%,17% 96%,11% 100%,5% 97%,0 99%)}
            .note--blue{background:repeating-linear-gradient(to bottom,#ecf7fa 0 29px,#d8e9ed 30px,#ecf7fa 31px)}
            .note--kraft{color:#36291f;background:radial-gradient(circle,#6d482218 0 .7px,transparent 1.5px),#d8bd96;background-size:17px 19px,auto;box-shadow:inset 0 0 18px #79542c16;clip-path:polygon(0 1%,96% 0,100% 3%,99% 98%,94% 100%,3% 99%,0 96%)}
            .note--green{background:linear-gradient(#b8d2b347 1px,transparent 1px),linear-gradient(90deg,#b8d2b347 1px,transparent 1px),#e8f3df;background-size:20px 20px,20px 20px,auto;border-left:7px solid #dcebd2}
            .note--lavender{border:1px solid #f8f1fb;border-radius:19px;background:#eee5f3;box-shadow:inset 0 0 0 3px #ffffff55}
            .empty{grid-column:1/-1;margin:0;padding:25px 18px;text-align:center;color:#fff9e9;background:#5d38295c;border:1px dashed #f7dfbaaa;font-size:14px}
            .modal-backdrop{position:fixed;inset:0;z-index:10;display:flex;align-items:center;justify-content:center;padding:18px;background:#2f211ac9}
            .edit-modal{width:min(100%,660px);max-height:calc(100vh - 36px);overflow:auto;padding:clamp(22px,4vw,42px);background:#fff7df;box-shadow:8px 14px 25px #1d120d99;transform:rotate(-.3deg)}
            .edit-modal h2{margin:0 0 22px;font-size:25px;color:#4b3528}
            .edit-modal label{display:block;margin-bottom:8px;font-size:14px;font-weight:800}
            .edit-modal textarea{display:block;width:100%;min-height:210px;resize:vertical;border:1px solid #c8b599;background:#fffdf6;padding:14px;font:inherit;font-size:18px;line-height:1.5;color:#332a25}
            .edit-modal textarea:focus{outline:2px solid #906c46;outline-offset:2px}
            .edit-fields{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:14px;margin-top:18px}
            .edit-fields label{margin:0;font-size:13px}
            .edit-fields input,.edit-fields select{display:block;width:100%;min-height:39px;margin-top:6px;border:1px solid #c8b599;background:#fffdf6;padding:6px 8px;color:#332a25;font:inherit}
            .date-hint{font-size:12px;color:#69584b;line-height:1.5}
            .edit-actions{display:flex;justify-content:flex-end;align-items:center;gap:22px;margin-top:22px}
            .edit-actions a{color:#5c4638;font-weight:700;text-decoration:underline}
            .edit-actions button{border:1px solid #65452d;border-radius:3px;background:#765135;color:white;padding:10px 25px;font:inherit;font-weight:800;cursor:pointer}
            @media(max-width:600px){.page{padding:10px 7px 25px}.page-header{padding:0 5px}.add-form{margin-left:8px}.board{border-width:9px;padding:19px}.notes{gap:24px 17px}}
            @media(max-width:410px){.page-header{display:block}.summary{margin-top:4px}.add-form__row{flex-direction:column}.add-form button{width:100%}.notes{grid-template-columns:1fr}.note{height:230px}}
            @media(max-width:410px){.edit-fields{grid-template-columns:1fr}}
            """;
}
