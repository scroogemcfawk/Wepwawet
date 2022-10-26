import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import scroogemcfawk.Color;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

import org.slf4j.Logger;


/**
 * This piece of shit (MS Excel) is finally can be read by my program! <br><br>
 * {@code XCellFetcher} reads the tickers from your Excel workbook and makes an {@code ArrayList<Asset>} of them.
 *
 * @author scroogemcfawk
 * @since v0.0.2
 */

public class XCellProcessor
{
    private static final Logger logger = LoggerFactory.getLogger(XCellProcessor.class);

    // Should be absolute path
    private static String file;

    private static String tickerColumn;
    private static String priceColumn;
    private static int startRow;
    private static int endRow;

    private static final int maxRow = 45;

    private static boolean isInit = false;

    static
    {
        logger.info("XCellFetcher is loaded");
    }


    public static String getState()
    {
        return "XCellProcessor[file=%s,tickerColumn=%s,priceColumn=%s,startRow=%d,endRow=%d,isInit=%b]".formatted(
                file,
                tickerColumn,
                priceColumn,
                startRow,
                endRow,
                isInit
        );
    }

    public static void initProcessor() throws IOException
    {
        Scanner in = new Scanner(System.in);

        System.out.println("Start cell address (e.g C418): ");
        String start = in.nextLine().strip();

        System.out.println("End cell address (e.g C418): ");
        String end = in.nextLine().strip();

        System.out.println("Price column (e.g F): ");
        String price = in.nextLine().strip();

        System.out.println("Absolute path to input file: (e.g " + System.getProperty("user.dir") + "): ");
        String path = in.nextLine().strip();

        try
        {
            initProcessor(start, end, price, path);
        }
        catch (IOException e)
        {
            logger.info(Color.red("Failed to init the fetcher"));
            var rethrow = new IOException("initFetcher(String, String, String, String) failed");
            rethrow.initCause(e);
            throw rethrow;
        }
    }

    public static void initProcessor(String startCell, String endCell, String priceColumn, String inputFileAbsolutePath)
            throws IOException
    {
        if (startCell.matches("[a-zA-Z]+\\d+") &&
            endCell.matches("[a-zA-Z]+\\d+") &&
            priceColumn.matches("[a-zA-Z]+"))
        {
            if (!startCell.replaceAll("\\d+", "").equals(
                    endCell.replaceAll("\\d+", "")))
            {
                throw new FetchingException("Can not process cell range");
            }
            XCellProcessor.startRow = Integer.parseInt(startCell.replaceAll("[a-zA-Z]+", ""));
            XCellProcessor.endRow = Integer.parseInt(endCell.replaceAll("[a-zA-Z]+", ""));

            if (endRow - startRow < 1) {
                throw new FetchingException("Bad cell range given");
            }

            XCellProcessor.tickerColumn = startCell.replaceAll("\\d+", "");

            XCellProcessor.priceColumn = priceColumn;
        }
        else
        {
            throw new IOException("Bad input format given while initializing XCellFetcher");
        }
        try
        {
            new FileInputStream(inputFileAbsolutePath).close();
            XCellProcessor.file = inputFileAbsolutePath;
            isInit = true;
        }
        catch (FileNotFoundException e)
        {
            throw new IOException("Could not find input file");
        }
    }


    public static @NotNull ArrayList<Asset> fetch() throws FetchingException
    {
        if (!isInit)
        {
            throw new FetchingException("Fetcher is not initialized");
        }
        try (FileInputStream in = new FileInputStream(file))
        {
            logger.info("Fetching" + getState());
            ArrayList<Asset> res = new ArrayList<>(endRow - startRow);
            Workbook wb = new XSSFWorkbook(in);
            Sheet sh = wb.getSheetAt(0);
            for (int i = startRow; i < endRow; i++)
            {
                Row row = sh.getRow(i);
                // Getting ticker and price cells
                Cell tickerCell = row.getCell(CellReference.convertColStringToIndex(tickerColumn));
                Cell priceCell = row.getCell(CellReference.convertColStringToIndex(priceColumn));
                // Getting ticker and price values
                String ticker = tickerCell.toString();
                double price = priceCell.getNumericCellValue();

                if (!ticker.isBlank())
                {
                    res.add(new Asset(ticker, price));
                }
            } return res;
        }
        catch (IOException e)
        {
            FetchingException rethrow = new FetchingException("File exception at fetching");
            rethrow.initCause(e);
            throw rethrow;
        }
    }

    public static void insert(ArrayList<Asset> assets) throws FetchingException
    {
        if (!isInit)
        {
            throw new FetchingException("Fetcher is not initialized");
        }
        try (FileOutputStream out = new FileOutputStream(file))
        {
            
        }
        catch (IOException e)
        {
            FetchingException rethrow = new FetchingException("File exception at insert");
            rethrow.initCause(e);
            throw rethrow;
        }
    }

    public static void main(String[] args) throws IOException
    {
        logger.trace("" + logger.isTraceEnabled()); // LOL XD BRO

        initProcessor("C9", "C45", "F", "C:\\Users\\scroo\\IdeaProjects\\Wepwawet\\io\\FinanceWorkbook.xlsx");
        if (XCellProcessor.isInit) {
            logger.info("YeeHaw");
            ArrayList<Asset> assets = fetch();
            logger.info("" + assets.size());

            for (Asset asset: assets) {
                System.out.println(asset);
            }
        } else {
            logger.info("Processor is not initialized");
        }
    }
}

