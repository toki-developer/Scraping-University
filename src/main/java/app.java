import java.util.Map;
import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.github.cdimascio.dotenv.Dotenv;

class App {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        String id = dotenv.get("USER_ID");
        String password = dotenv.get("PASSWORD");
        String loginUrl = dotenv.get("LOGIN_URL");
        String mypageUrl = dotenv.get("MYPAGE_URL");

        try {
            Connection.Response response = Jsoup.connect(loginUrl)
                    .method(Connection.Method.GET).execute();

            String logintoken = response.parse().getElementsByAttributeValue("name", "logintoken").get(0).attr("value");
            response = Jsoup.connect(loginUrl).cookies(response.cookies())
                    .data("logintoken", logintoken).data("username", id).data("password", password)
                    .method(Connection.Method.POST).followRedirects(false).execute();

            Map<String, String> cookies = response.cookies();

            //               ホームを開く
//            cookies.remove("MOODLEID1_");
//            while (response.statusCode() == 303) {
//                response = Jsoup.connect(response.header("Location")).cookies(cookies)
//                        .method(Connection.Method.GET).followRedirects(false).execute();
//            }
//            String html = response.parse().outerHtml();
//            Document homeDoc = Jsoup.parseBodyFragment(html);
//
//            // 選択科目一覧
//            Elements subjectList = homeDoc.select("li.type_course.depth_3.contains_branch p a");
//            for (Element subject : subjectList) {
//                System.out.println("title: " + subject.ownText() + ",  href: " + subject.absUrl("href"));
//            }

            // マイページ
            Document myPageDoc = Jsoup.connect(mypageUrl)
                    .cookies(response.cookies())
                    .get();

            // 選択科目一覧
            Elements eventList = myPageDoc.select("div.event");
            for (Element event : eventList) {

                // タイトル
                String title = event.getElementsByTag("a").get(0).ownText();

                // 日付
                Element data = event.getElementsByClass("date").get(0);
                String dataYYMMdd = data.child(0).ownText();
                String timeHHMM = data.ownText();

                System.out.println(title + " : " + dataYYMMdd + " " + timeHHMM);
            }

        } catch (IOException e) {
            System.out.println("Moodle接続時にエラーが発生しました（接続エラー）。");
            e.printStackTrace();
        }
    }
}