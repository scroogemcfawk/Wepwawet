import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.*;

/** The {@code Parser} class is intended to get the price value of a given {@link Asset}
 *
 * @since  0.0.1
 */
public class Parser {

    final private static String[] urls = new String[] {"https://www.tinkoff.ru/invest/recommendations/?query=%s",
            "https://www.tinkoff.ru/invest/stocks/%s/",};

    public static int ping(URL url) throws IOException {
        var connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("HEAD");
        return connection.getResponseCode();
    }

    public static double parse(String url, Asset asset) throws IOException {
        URL target = new URL(url.formatted(asset.getTicker()));
//        System.out.println(target);
        if (ping(target) == 200) {
            Document doc = Jsoup.connect(target.toString()).get();
            Elements container = doc.select(".SecurityInvitingScreen__price_FSP8P");

            if (container.size() == 1) {
                for (Element element : container) {
                    String rep = element.text();
                    String res = rep.substring(0, rep.length() - 2).replace(',', '.');
                    res = res.replaceAll("\\s","");
//                    System.out.println(Logging.yellow(res));
                    return Double.parseDouble(res);
                }
            } else {
                System.out.println(Logging.red("Container element count is not 1"));
            }
        }
        throw new IOException();
    }


    public static void main(String[] args) throws IOException {
        String[] tickers = new String[] {
                "SBERP",
                "FLWS",
                "DDD",
                "TSVT",
                "NVR",
                "AZO",
                "Y",
                "CABO",
                "NFLX",
        };
        var assets = Asset.fromArray(tickers);
        for (var a : assets) {
            System.out.println(a.getTicker() + ": " + parse(urls[1], a));
        }
    }
}
