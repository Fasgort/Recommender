/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recommender;

/**
 *
 * @author Fasgort
 * @param <T1>
 */
public class ComparablePairDouble<T1> implements Comparable<ComparablePairDouble<T1>> {

    T1 first;
    double second;

    ComparablePairDouble(T1 first, double second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public int compareTo(ComparablePairDouble w) {
        return this.second > w.second ? 1 : -1;
    }

}
