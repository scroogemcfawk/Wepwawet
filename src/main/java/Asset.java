/**
 * The {@code Asset} class represents some market stock that has its name, ticker and price.
 *
 * @since v0.0.1
 */
@SuppressWarnings("unused")
public class Asset
{

    final private String ticker;
    private Market market;

    private double price;

    Asset(String ticker, Market market, double price)
    {
        this.ticker = ticker.strip();
        this.market = market;
        this.price = price;
    }

    Asset(String ticker, double price) {
        this(ticker, Market.Unknown, price);
    }

    Asset(String ticker)
    {
        this(ticker, Market.Unknown, Double.NaN);
    }

    public String getTicker()
    {
        return ticker;
    }

    public String getMarket()
    {
        return market.toString();
    }

    public double getPrice()
    {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double parsePrice(Parser p)
    {
        return 0;
    }

    @Override
    public String toString() {
        return new String(ticker.toString() + ": " + price);
    }

    public static Asset[] fromArray(String[] assets)
    {
        Asset[] result = new Asset[assets.length];
        for (int i = 0; i < assets.length; i++) {
            result[i] = new Asset(assets[i]);
        }
        return result;
    }
}
