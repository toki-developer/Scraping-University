import java.util.Map;
import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.github.cdimascio.dotenv.Dotenv;

class App {
    public static Dotenv dotenv = Dotenv.load();
    public static void main(String[] args) {
        String id = dotenv.get("USER_ID");
        String password = dotenv.get("PASSWORD");
        String mypageUrl = dotenv.get("MYPAGE_URL");
        try {
            Map<String, String> cookies = getCookies(id, password);

            // マイページ
            Document myPageDoc = Jsoup.connect(mypageUrl)
                    .cookies(cookies)
                    .get();
            // 選択科目一覧
            Elements eventList = myPageDoc.select("div.event");
            for (Element event : eventList) {
                // タイトルとリンク
                Element titleDoc = event.getElementsByTag("a").get(0);
                String title = titleDoc.ownText();
                String href = titleDoc.absUrl("href");

                // 日付
                Element data = event.getElementsByClass("date").get(0);
                String dataYYMMDD = data.child(0).ownText();
                String timeHHMM = data.ownText();

                Document eventModalDoc = Jsoup.connect(href)
                        .cookies(cookies)
                        .get();

                Elements eventCardDoc = eventModalDoc.getElementsByClass("description");
                String subjectName = eventCardDoc.select("div:last-child div:last-child a").get(0).ownText();
                System.out.println(subjectName + " : " + title + " : " + dataYYMMDD + " " + timeHHMM);
            }

        } catch (IOException e) {
            System.out.println("Moodle接続時にエラーが発生しました（接続エラー）。");
            e.printStackTrace();
        }
    }

    public static Map<String, String> getCookies(String id, String password) {
        String loginUrl = dotenv.get("LOGIN_URL");
        try {
            Connection.Response response = Jsoup.connect(loginUrl)
                    .method(Connection.Method.GET).execute();

            String logintoken = response.parse().getElementsByAttributeValue("name", "logintoken").get(0).attr("value");
            response = Jsoup.connect(loginUrl).cookies(response.cookies())
                    .data("logintoken", logintoken).data("username", id).data("password", password)
                    .method(Connection.Method.POST).followRedirects(false).execute();
            //               ホームを開く
//            cookies.remove("MOODLEID1_");
//            while (response.statusCode() == 303) {
//                response = Jsoup.connect(response.header("Location")).cookies(cookies)
//                        .method(Connection.Method.GET).followRedirects(false).execute();
//            }
//            String html = response.parse().outerHtml();
//            Document homeDoc = Jsoup.parseBodyFragment(html);
//
            return response.cookies();
        } catch (IOException e) {
            System.out.println("Moodle接続時にエラーが発生しました（接続エラー）。");
            e.printStackTrace();
        }
        return null;
    }
}