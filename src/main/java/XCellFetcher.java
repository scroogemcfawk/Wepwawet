import org.apache.poi.ss.formula.functions.Column;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import scroogemcfawk.Logging;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This piece of shit (MS Excel) is finally can be read by my program! <br><br>
 * {@code XCellFetcher} reads the tickers from your Excel workbook and makes an {@code ArrayList<Asset>} of them.
 * @author scroogemcfawk
 * @since v0.0.2
 */

public class XCellFetcher
{
    public static ArrayList<Asset> fetch(String file) throws FetchingException
    {
        try (FileInputStream in = new FileInputStream(file)) {
            ArrayList<Asset> res = new ArrayList<>(20);
            Workbook wb = new XSSFWorkbook(in);
            Sheet sh = wb.getSheet("main");
            for (int i = 7; i < 45; i++) {
                Row r = sh.getRow(i);
                Cell c = r.getCell(2);
//                System.out.printf(c.toString());
                String t = c.toString();
                double p = r.getCell(5).getNumericCellValue();
//                System.out.printf("" + p);
                if (!t.isBlank()) {
                    res.add(new Asset(t, p));
                }
            }
            return res;
        } catch (IOException e) {
            FetchingException rethrow = new FetchingException("File exception in fetcher");
            rethrow.initCause(e);
            throw rethrow;
        }
    }

    public static void main(String[] args) throws IOException
    {
        Logger.getGlobal().setLevel(Level.OFF);
        ArrayList<Asset> assets = fetch("./io/FinanceWorkbook.xlsx");
        for (Asset asset: assets) {
            System.out.println(asset + " -> " + Logging.red("" + Parser.parse(asset)));
        }
    }
}

