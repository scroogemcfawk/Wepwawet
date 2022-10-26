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
        logger.debug("XCellFetcher is loaded");
    }

    /**
     * @return all filed values
     */
    public static String getState()
    {
        return "XCellProcessor[file=%s,tickerColumn=%s,priceColumn=%s,startRow=%d,endRow=%d,isInit=%b]".formatted(file,
                                                                                                                  tickerColumn,
                                                                                                                  priceColumn,
                                                                                                                  startRow,
                                                                                                                  endRow,
                                                                                                                  isInit);
    }

    /**
     * Manual {@code XCellProcessor} initialization
     * @throws IOException on IO exception
     */
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

    /**
     * Inits the starting values and working file of {@code XCellProcessor}.
     * @param startCell start cell address (e.g. C418)
     * @param endCell end cell address (e.g. C418)
     * @param priceColumn price column address (e.g. F)
     * @param inputFileAbsolutePath absolute path of file
     * @throws IOException if bad inputs given or file IO exception occurs
     */
    public static void initProcessor(String startCell, String endCell, String priceColumn, String inputFileAbsolutePath)
            throws IOException
    {
        if (startCell.matches("[a-zA-Z]+\\d+") && endCell.matches("[a-zA-Z]+\\d+") && priceColumn.matches("[a-zA-Z]+"))
        {
            if (!startCell.replaceAll("\\d+", "").equals(endCell.replaceAll("\\d+", "")))
            {
                throw new XCellProcException("Can not process cell range");
            }
            XCellProcessor.startRow = Integer.parseInt(startCell.replaceAll("[a-zA-Z]+", ""));
            XCellProcessor.endRow = Integer.parseInt(endCell.replaceAll("[a-zA-Z]+", ""));

            if (endRow - startRow < 1)
            {
                throw new XCellProcException("Bad cell range given");
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
            logger.debug("XCellFetcher is initialized: " + getState());
        }
        catch (FileNotFoundException e)
        {
            throw new IOException("Could not find input file");
        }
    }


    /**
     * Fetches assets from Excel file.
     * @since 0.1.1
     * @throws XCellProcException if any exception occurs
     */
    public static @NotNull ArrayList<Asset> fetch() throws XCellProcException
    {
        if (!isInit)
        {
            throw new XCellProcException("Fetcher is not initialized");
        }
        try (FileInputStream in = new FileInputStream(file))
        {
            logger.debug("Fetching" + getState());
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
            }
            return res;
        }
        catch (IOException e)
        {
            XCellProcException rethrow = new XCellProcException("File exception at fetching");
            rethrow.initCause(e);
            throw rethrow;
        }
    }

    /**
     * Inserts assets with new price into Excel file.
     * @since 0.4.0
     * @param assets Assets with updated price
     * @throws XCellProcException if any exception occurs
     */
    public static void insert(ArrayList<Asset> assets) throws XCellProcException
    {
        if (!isInit)
        {
            throw new XCellProcException("Fetcher is not initialized");
        }
        try (FileInputStream in = new FileInputStream(file))
        {
            logger.debug("Inserting" + getState());
            Workbook wb = new XSSFWorkbook(in);
            Sheet sh = wb.getSheetAt(0);
            try (FileOutputStream out = new FileOutputStream(file))
            {
                for (Asset asset: assets)
                {
                    logger.debug("Inserting: " + asset);
                    for (int i = startRow; i < endRow; i++)
                    {
                        logger.debug("Trying row " + i);
                        Row r = sh.getRow(i);
                        Cell c = r.getCell(CellReference.convertColStringToIndex(tickerColumn));
                        logger.debug(c.getStringCellValue());
                        if (c.getStringCellValue().equals(asset.getTicker()))
                        {
                            r.getCell(CellReference.convertColStringToIndex(priceColumn)).setCellValue(asset.getPrice());
                            break;
                        }
                    }
                }
                wb.write(out);
            }
        }
        catch (IOException e)
        {
            XCellProcException rethrow = new XCellProcException("File exception at inserting");
            rethrow.initCause(e);
            throw rethrow;
        }
    }

    public static void main(String[] args) throws IOException
    {
        logger.trace("" + logger.isTraceEnabled()); // LOL XD BRO

        initProcessor("C9", "C45", "F", "C:\\Users\\scroo\\IdeaProjects\\Wepwawet\\io\\FinanceWorkbook.xlsx");
        if (XCellProcessor.isInit)
        {
            ArrayList<Asset> assets = new ArrayList<>(1);
            assets.add(0, new Asset("AFLT", 1.0));

            for (Asset asset: assets)
            {
                System.out.println(asset);
            }

            XCellProcessor.insert(assets);
        }
        else
        {
            logger.info("Processor is not initialized");
        }
    }
}

