import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scroogemcfawk.Color;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The {@code Wepwawet} class is used to automatically update stock prices of my mom's
 * MS Excel table, because seeing manual updating is quite depressive.
 *
 * @author scroogemcfawk
 * @version v0.5.1-alpha
 */
public class App
{
    /* TODO:
     *  - make PriceParser interface | maybe inherit PriceParser from Parser idk
     *  - make Asset abstract then inherit Stock, refactor
     * */
    static private final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws IOException
    {
        XCellProcessor.initProcessor("C9",
                                     "C45",
                                     "F",
                                     "C:\\Users\\scroo\\IdeaProjects\\Wepwawet\\io\\FinanceWorkbook.xlsx");
        ArrayList<Asset> assets = XCellProcessor.fetch();
        var startTime = System.nanoTime();

        // TODO: for some reason 6 threads work much faster than 8 or 5
        try (ExecutorService threads = Executors.newFixedThreadPool(6))
        {
            for (Asset asset: assets)
            {
                double prev = asset.getPrice();

                threads.execute(() -> {
                    try
                    {
                        double curr = asset.setPrice(Parser.parse(asset));

                        System.out.print(Color.cyan(String.format("%14s", asset.getTicker())) + ": " +
                                         String.format("%.2f", prev));
                        if (prev > curr)
                        {
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
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
        catch (Exception e)
        {
            logger.info("Thread exception");
        }
        var endTime = System.nanoTime();
        System.out.printf("Took %.2fs to update\n", (endTime - startTime) / 1_000_000_000.0);
        System.out.print("Save changes? (y/n): ");
        Scanner in = new Scanner(System.in);
        String ans = in.nextLine().strip();
        ans = ans.substring(0, 1).toLowerCase();
        if (ans.equals("y"))
        {
            try
            {
                XCellProcessor.insert(assets);
            }
            catch (Exception e)
            {
                System.out.println("Can not save changes");
            }
        }
        else if (ans.equals("n"))
        {
            System.out.println("Changes not saved");
        }
        else
        {
            System.out.println("Unsupported input, considered as \"no\"");
        }
    }
}