import java.util.List;

final class BoardView {
    private static final String[] PAPERS = {
            "yellow", "pink", "blue", "kraft", "green", "lavender"
    };

    private BoardView() {}

    static String render(List<Todo> todos) {
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
                .append("<header class='page-header'><div><p class='eyebrow'>MY LITTLE TO-DO BOARD</p>")
                .append("<h1>わたしのTodo<span class='heading-pin' aria-hidden='true'>✦</span></h1>")
                .append("<p class='intro'>今日のやることを、ひとつずつ貼っていこう。</p></div>")
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
        return html.append("</div></section></div></main></body></html>").toString();
    }

    private static void appendNote(StringBuilder html, Todo todo) {
        String paper = PAPERS[(todo.getId() - 1) % PAPERS.length];
        html.append("<article class='note note--").append(paper);
        if (todo.isDone()) html.append(" note--done");
        html.append("'><span class='pin' aria-hidden='true'></span>")
                .append("<div class='note__top'><span>MEMO · ")
                .append(todo.getId()).append("</span>");
        if (todo.isDone()) html.append("<span class='done-badge'>✅済</span>");
        html.append("</div><p class='note__text'>")
                .append(escapeHtml(todo.getTitle()))
                .append("</p><div class='note__actions'>");
        if (!todo.isDone()) {
            html.append("<a href='/done?id=").append(todo.getId()).append("'>✓ 完了</a>");
        }
        html.append("<a href='/delete?id=").append(todo.getId())
                .append("' aria-label='メモ ").append(todo.getId()).append(" を削除'>削除</a>")
                .append("</div></article>");
    }

    private static String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static final String CSS = """
            :root{font-family:'Hiragino Kaku Gothic ProN','Yu Gothic',Meiryo,sans-serif;color:#3e3026;background:#f3efe6}
            *{box-sizing:border-box}
            body{margin:0;min-width:280px;background:radial-gradient(circle at 10% 5%,#fff9e9 0,transparent 32%),radial-gradient(circle at 90% 90%,#e8ddcb 0,transparent 35%),#f3efe6}
            .page{max-width:1280px;margin:auto;padding:36px clamp(16px,4vw,48px) 64px}
            .page-header{display:flex;justify-content:space-between;align-items:end;gap:24px;margin:0 4px 26px}
            .eyebrow{margin:0 0 7px;color:#9b7257;font-size:11px;font-weight:800;letter-spacing:.22em}
            h1{margin:0;font-size:clamp(29px,4vw,42px);line-height:1.2;letter-spacing:.04em}
            .heading-pin{display:inline-block;color:#b76554;font-size:21px;margin-left:10px;vertical-align:top;transform:rotate(16deg)}
            .intro{margin:9px 0 0;color:#806d60;font-size:14px}
            .summary{display:flex;gap:8px;flex-wrap:wrap}
            .summary span{background:#fffaf0;border:1px solid #e5d4b9;border-radius:999px;padding:9px 14px;font-size:12px;white-space:nowrap;box-shadow:0 2px 7px #63462a12}
            .summary strong{color:#9f5c37;font-size:17px;margin-left:5px}
            .add-form{background:#fffaf0;border:1px solid #e7d9c5;border-radius:14px;padding:18px 22px;margin-bottom:27px;box-shadow:0 5px 17px #57412b16}
            .add-form label{display:block;font-size:13px;font-weight:800;margin-bottom:9px;color:#675343}
            .add-form__row{display:flex;gap:10px}
            .add-form input{flex:1;min-width:0;border:1px solid #d9cbb9;border-radius:8px;background:#fff;padding:12px 14px;font:inherit;font-size:15px;color:#3e3026}
            .add-form input:focus{outline:2px solid #b77a51;outline-offset:2px}
            .add-form button{border:0;border-radius:8px;background:#8d573d;color:white;padding:11px 19px;font:inherit;font-size:14px;font-weight:800;cursor:pointer;white-space:nowrap}
            .add-form button:hover{background:#75442e}
            .board{position:relative;isolation:isolate;overflow:hidden;border:12px solid #a8784b;border-radius:8px;padding:clamp(18px,3.5vw,42px);background-color:#bc8555;background-image:radial-gradient(circle at 18% 23%,#e5b57d99 0 1px,transparent 2px),radial-gradient(circle at 77% 66%,#663b2244 0 1px,transparent 2px),radial-gradient(circle at 44% 71%,#efc69888 0 1px,transparent 2px),repeating-linear-gradient(35deg,#ffffff08 0 2px,#512b1a12 3px 5px,transparent 7px 12px),linear-gradient(130deg,#c99564,#aa7247 55%,#bd8758);background-size:19px 17px,23px 21px,29px 25px,auto,auto;box-shadow:inset 0 0 0 3px #e0ad78,inset 0 0 32px #70421d75,0 16px 38px #5e392852}
            .board:before{content:'';position:absolute;inset:0;z-index:-1;pointer-events:none;background:repeating-radial-gradient(circle at 12% 31%,#fff7 0 1px,transparent 2px 5px);opacity:.08}
            .board-section+.board-section{margin-top:36px;padding-top:29px;border-top:2px dashed #f7dfba99}
            .section-heading{display:flex;align-items:center;gap:10px;margin-bottom:20px;color:#fffaf2;text-shadow:0 2px 4px #5a361f88}
            .section-heading h2{font-size:clamp(18px,2.5vw,23px);margin:0;letter-spacing:.05em}
            .section-heading span{font-size:12px;border-radius:99px;padding:3px 9px;background:#5f382b66}
            .notes{display:grid;grid-template-columns:repeat(auto-fill,minmax(min(100%,220px),1fr));gap:24px;align-items:start}
            .note{position:relative;display:flex;flex-direction:column;height:240px;min-width:0;padding:27px 18px 15px;color:#44382f;filter:drop-shadow(3px 8px 5px #4a291d77);background:#fff3ab;transform:rotate(-.6deg)}
            .note:nth-child(2n){transform:rotate(.75deg)}
            .note:nth-child(3n){transform:rotate(-.3deg)}
            .pin{position:absolute;top:8px;left:50%;z-index:2;width:17px;height:17px;border-radius:50%;transform:translateX(-50%);background:radial-gradient(circle at 34% 28%,#fff9 0 14%,#e48979 28%,#b4423a 68%,#732824 100%);box-shadow:1px 3px 3px #4d281d99,inset -2px -2px 2px #631d1766}
            .note__top{display:flex;justify-content:space-between;align-items:center;gap:6px;min-height:20px;color:#705e4f;font-size:10px;font-weight:800;letter-spacing:.12em}
            .note__text{flex:1;min-height:0;overflow:auto;margin:12px 0;color:inherit;font-size:17px;font-weight:700;line-height:1.65;white-space:pre-wrap;overflow-wrap:anywhere}
            .note__actions{display:flex;gap:15px;align-items:center;border-top:1px solid #6a4d3229;padding-top:10px;font-size:12px;font-weight:800}
            .note__actions a{color:inherit;text-decoration:none;border-bottom:1px solid currentColor;padding-bottom:2px}
            .note__actions a:last-child{margin-left:auto;opacity:.7}
            .note__actions a:hover{opacity:1;color:#9b4936}
            .done-badge{border-radius:99px;padding:3px 7px;background:#ffffffa8;color:#406444;font-size:11px;letter-spacing:0;white-space:nowrap}
            .note--done{filter:drop-shadow(2px 5px 4px #4a291d66);opacity:.86}
            .note--yellow{background:linear-gradient(135deg,#fff8bc,#ffeba2 80%,#e8ca76);box-shadow:inset 0 0 28px #fff9}
            .note--yellow:after{content:'';position:absolute;right:0;bottom:0;width:29px;height:29px;background:linear-gradient(135deg,#d4ad62 0 49%,#fff9cf 51%);box-shadow:-2px -2px 3px #9d7b4633}
            .note--pink{background:radial-gradient(circle at 18% 20%,#fff8 0 1px,transparent 2px),linear-gradient(120deg,#ffe1e5,#f6bccb);background-size:12px 12px,auto;clip-path:polygon(0 0,100% 0,100% 96%,96% 98%,92% 96%,88% 99%,84% 97%,80% 100%,76% 97%,72% 99%,68% 96%,64% 99%,60% 97%,56% 100%,52% 97%,48% 99%,44% 96%,40% 99%,36% 97%,32% 100%,28% 97%,24% 99%,20% 96%,16% 99%,12% 97%,8% 100%,4% 97%,0 99%)}
            .note--blue{background:linear-gradient(90deg,transparent 0 28px,#e6a8a977 29px 30px,transparent 31px),repeating-linear-gradient(to bottom,#eaf7fb 0 27px,#a7d6e3 28px,#eaf7fb 29px);padding-left:38px}
            .note--kraft{color:#3c3025;background:radial-gradient(circle,#67452820 0 1px,transparent 1.5px),radial-gradient(circle,#fff9 0 1px,transparent 2px),linear-gradient(140deg,#d7b98c,#bd9469);background-size:11px 13px,19px 17px,auto;box-shadow:inset 0 0 20px #70452227}
            .note--green{background:linear-gradient(#90bba655 1px,transparent 1px),linear-gradient(90deg,#90bba655 1px,transparent 1px),linear-gradient(145deg,#f0f8df,#dceccb);background-size:20px 20px,20px 20px,auto}
            .note--lavender{border-radius:18px;background:radial-gradient(circle at 85% 12%,#fff9,transparent 38%),linear-gradient(145deg,#eee5fa,#d9c7ed);box-shadow:inset 0 0 0 1px #fff9,inset 0 -10px 22px #9273b01c}
            .empty{grid-column:1/-1;margin:0;padding:28px 20px;text-align:center;color:#fff9e9;background:#5d38295c;border:1px dashed #f7dfbaaa;border-radius:10px;font-size:14px}
            @media(max-width:600px){.page{padding:20px 12px 40px}.page-header{display:block;margin-bottom:20px}.summary{margin-top:17px}.add-form{padding:15px}.board{border-width:8px;padding:18px}.notes{grid-template-columns:repeat(auto-fill,minmax(min(100%,220px),1fr));gap:18px}}
            @media(max-width:410px){.add-form__row{flex-direction:column}.add-form button{width:100%}.notes{grid-template-columns:1fr}.note{height:225px}}
            """;
}
