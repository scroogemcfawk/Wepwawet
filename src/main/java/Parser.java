import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scroogemcfawk.Color;

import java.io.IOException;
import java.net.*;

/**
 * The {@code Parser} class is intended to get the price value of a given {@link Asset}
 *
 * @since 0.0.1
 */

public class Parser
{
    private static final Logger logger = LoggerFactory.getLogger(Parser.class);

    final private static String[] urls = new String[] {
            "https://www.tinkoff.ru/invest/recommendations/?query=%s"
            , "https://www.tinkoff.ru/invest/",};

    public static int ping(URL url) throws IOException
    {
        var connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("HEAD");
        return connection.getResponseCode();
    }

    public static double parse(Asset asset) throws IOException
    {
        String url = urls[1];
        if (asset.getTicker().matches("FX..+"))
        {
            url += "etfs/";
        }
        else if (asset.getTicker().matches("\\w+\\d+.*"))
        {
            url += "bonds/";
        }
        else
        {
            url += "stocks/";
        }
        url += asset.getTicker() + "/";

        URL target = new URL(url);
        int answer;
        if ((answer = ping(target)) == 200)
        {
            try
            {
                Document doc = Jsoup.connect(target.toString()).get();
                Elements container = doc.select(".SecurityInvitingScreen__price_FSP8P");

                if (container.size() == 1)
                {
                    for (Element element: container)
                    {
                        String rep = element.text();
                        String res = rep.substring(0, rep.length() - 2).replace(',', '.');
                        res = res.replaceAll("\\s", "");

                        logger.debug(res);

                        return Double.parseDouble(res);
                    }
                }
                else
                {
                    System.out.println(Color.red("Container element count is not 1"));
                }
            }
            catch (IOException e)
            {
                ParsingException rethrow = new ParsingException("Couldn't parse " + asset.getTicker() + " at " + url);
                rethrow.initCause(e);
                throw rethrow;
            }
        }
        throw new HttpStatusException("Bad server response", answer, target.toString());
    }


    public static void main(String[] args) throws IOException
    {
        String[] tickers = new String[] {"SBERP", "FLWS", "DDD", "TSVT", "NVR", "AZO", "Y", "CABO", "NFLX",};
        var assets = Asset.fromArray(tickers);
        for (Asset asset: assets)
        {
            Parser.parse(asset);
        }

    }
}
