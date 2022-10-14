public class Asset {

    final private String ticker;
    private Market market;

    Asset(String stock, Market market) {
        this.ticker = stock.strip();
        this.market = market;
    }

    Asset(String stock) {
        this(stock, Market.Unknown);
    }

    public String getTicker() {
        return ticker;
    }

    public String getMarket() {
        return market.toString();
    }


    public double parsePrice(Parser p) {
        return 0;
    }

    public static Asset[] fromArray(String[] assets) {
        Asset[] result = new Asset[assets.length];
        for (int i = 0; i < assets.length; i++) {
            result[i] = new Asset(assets[i]);
        }
        return result;
    }
}
