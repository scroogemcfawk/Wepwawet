import scroogemcfawk.Color;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

/**
 * The {@code Wepwawet} class is used to automatically update stock prices of my mom's
 * MS Excel table, because seeing manual updating is quite depressive.
 *
 * @author scroogemcfawk
 * @version v0.4.2
 */
public class App
{
    public static void main(String[] args) throws IOException
    {
        XCellProcessor.initProcessor("C9",
                                     "C45",
                                     "F",
                                     "C:\\Users\\scroo\\IdeaProjects\\Wepwawet\\io\\FinanceWorkbook.xlsx");
        ArrayList<Asset> assets = XCellProcessor.fetch();
        var startTime = System.nanoTime();
        for (Asset asset: assets)
        {
            double prev = asset.getPrice();
            asset.setPrice(Parser.parse(asset));
            double curr = asset.getPrice();
            System.out.print(Color.cyan(String.format("%14s", asset.getTicker())) + ": " + String.format("%.2f",
                                                                                                         prev));
            if (prev > curr) {
                System.out.println(Color.red(String.format(" => %.2f", curr)));
            }
            else if (prev == curr)
            {
                System.out.println(Color.yellow(String.format(" => %.2f", curr)));
            }
            else
            {
                System.out.println(Color.green(String.format(" => %.2f", curr)));
            }
        }
        var endTime = System.nanoTime();
        System.out.printf("Took %.2fs to update", (endTime - startTime) / 1_000_000_000.0);
//        XCellProcessor.insert(assets);
    }
}
