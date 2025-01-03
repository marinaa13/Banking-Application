package org.poo.main;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a graph of exchange rates between different currencies.
 * <p>
 * This class stores the exchange rates between currencies in a graph (as an adjacency matrix),
 * and provides methods to calculate the rates between any two currencies,
 * including indirect conversions using the Floyd-Warshall algorithm.
 */
@Getter @Setter
public class ExchangeRatesGraph {
    private final List<String> currencies;
    private double[][] graph;

    /**
     * Constructs an {@link ExchangeRatesGraph} from a list of {@link ExchangeRate} objects.
     * The currencies are extracted from the exchange rates,
     * and the adjacency matrix is initialized.
     *
     * @param exchangeRates a list of exchange rates between currencies
     */
    public ExchangeRatesGraph(final List<ExchangeRate> exchangeRates) {
        this.currencies = new ArrayList<>(getCurrencies(exchangeRates));
        graph = new double[currencies.size()][currencies.size()];
    }

    /**
     * Extracts the unique set of currencies from the provided list of exchange rates.
     *
     * @param exchangeRates a list of exchange rates
     * @return a set of unique currencies used in the exchange rates
     */
    private Set<String> getCurrencies(final List<ExchangeRate> exchangeRates) {
        Set<String> currenciesSet = new HashSet<>();
        for (ExchangeRate rate : exchangeRates) {
            currenciesSet.add(rate.getFrom());
            currenciesSet.add(rate.getTo());
        }
        return currenciesSet;
    }

    /**
     * Builds the graph (adjacency matrix) based on the provided list of exchange rates.
     * The matrix is filled with the exchange rates for each currency pair and is also populated
     * with the reciprocal rates.
     * <p>
     * After constructing the graph, the Floyd-Warshall algorithm is called to compute
     * indirect conversions between all currencies.
     *
     * @param exchangeRates a list of exchange rates to initialize the graph
     */
    public void makeGraph(final List<ExchangeRate> exchangeRates) {
        for (ExchangeRate rate : exchangeRates) {
            int fromIndex = currencies.indexOf(rate.getFrom());
            int toIndex = currencies.indexOf(rate.getTo());
            graph[fromIndex][toIndex] = rate.getRate();
            graph[toIndex][fromIndex] = 1 / rate.getRate();
        }

        for (int i = 0; i < currencies.size(); i++) {
            graph[i][i] = 1;
        }

        floydWarshall();
    }

    /**
     * Applies the Floyd-Warshall algorithm to compute the shortest path (maximum rate)
     * between all pairs of currencies. This ensures that indirect exchange rates are calculated.
     */
    public void floydWarshall() {
        for (int k = 0; k < currencies.size(); k++) {
            for (int i = 0; i < currencies.size(); i++) {
                for (int j = 0; j < currencies.size(); j++) {
                    if (graph[i][j] < graph[i][k] * graph[k][j]) {
                        graph[i][j] = graph[i][k] * graph[k][j];
                    }
                }
            }
        }
    }

    /**
     * Retrieves the exchange rate between two currencies.
     *
     * @param from the currency to convert from
     * @param to the currency to convert to
     * @return the exchange rate from the "from" currency to the "to" currency
     */
    public double getRate(final String from, final String to) {
        int fromIndex = currencies.indexOf(from);
        int toIndex = currencies.indexOf(to);
        return graph[fromIndex][toIndex];
    }
}
