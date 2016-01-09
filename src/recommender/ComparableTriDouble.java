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
public class ComparableTriDouble<T1> implements Comparable<ComparableTriDouble<T1>> {

    T1 first;
    double second;
    double third;

    ComparableTriDouble(T1 first, double second, double third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    @Override
    public int compareTo(ComparableTriDouble w) {
        if(this.second == w.second) return 0;
        return this.second > w.second ? 1 : -1;
    }

}
