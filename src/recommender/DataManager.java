package recommender;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.tint.IntMatrix1D;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Fasgort
 */
public class DataManager {

    private static volatile DataManager instance = null;
    final private UserDictionary userDictionary;
    final private MovieDictionary movieDictionary;
    SparseDoubleMatrix2D ratingIndex;

    protected DataManager() {
        userDictionary = UserDictionary.getInstance();
        movieDictionary = MovieDictionary.getInstance();

        SparseDoubleMatrix2D _ratingIndex = null;

        if (_ratingIndex == null) {
            ratingIndex = new SparseDoubleMatrix2D(1024, 2048);
        } else {
            ratingIndex = _ratingIndex;
        }
    }

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public IndexedUser searchUser(int idUser) {
        return userDictionary.search(idUser);
    }

    public int addUser(String name, short age, boolean male, String occupation, String zipCode) {
        IndexedUser iU = new IndexedUser(name, age, male, occupation, zipCode);
        int idUser = userDictionary.add(iU);
        return idUser;
    }

    public IndexedMovie searchMovie(int idMovie) {
        return movieDictionary.search(idMovie);
    }

    public int addMovie(String name, boolean action, boolean adventure,
            boolean animation, boolean children, boolean comedy, boolean crime,
            boolean documentary, boolean drama, boolean fantasy, boolean noir,
            boolean horror, boolean musical, boolean mystery, boolean romance,
            boolean scifi, boolean thriller, boolean unknown, boolean war,
            boolean western) {
        IndexedMovie iM = new IndexedMovie(name, action, adventure, animation,
                children, comedy, crime, documentary, drama, fantasy, noir,
                horror, musical, mystery, romance, scifi, thriller, unknown,
                war, western);
        int idMovie = movieDictionary.add(iM);
        return idMovie;
    }

    public synchronized void addRating(int idUser, int idMovie, int rating) {
        ratingIndex.setQuick(idUser, idMovie, rating);
    }

    public void predictRating(int idUser) {
        DoubleMatrix1D userRatings = ratingIndex.viewRow(idUser);
        double userMean = userRatings.zSum() / userRatings.cardinality();

        ArrayList<ComparablePairDouble<Integer>> similitude = new ArrayList<>(userQuantity());

        for (int i = 0; i < userQuantity(); i++) {
            if (i == idUser) {
                continue;
            }
            DoubleMatrix1D otherUserRatings = ratingIndex.viewRow(i);
            double otherUserMean = otherUserRatings.zSum() / otherUserRatings.cardinality();
            double sumRatings1 = 0;
            double sumRatings2 = 0;
            double sumRatings3 = 0;
            int itemCount = 0;
            for (int j = 0; j < movieQuantity(); j++) {
                double userValue = userRatings.get(j);
                double otherUserValue = otherUserRatings.get(j);
                if (userValue == 0.0 || otherUserValue == 0.0) {
                    continue;
                }
                userValue -= userMean;
                otherUserValue -= otherUserMean;
                sumRatings1 += userValue * otherUserValue;
                sumRatings2 += pow(userValue, 2);
                sumRatings3 += pow(otherUserValue, 2);
                itemCount++;
            }
            if (itemCount == 0 || sumRatings1 == 0.0) {
                similitude.add(new ComparablePairDouble(i, 0.0));
            } else {
                if (itemCount < 20) {
                    similitude.add(new ComparablePairDouble(i, (sumRatings1 / (sqrt(sumRatings2) * sqrt(sumRatings3))) * itemCount / 20));
                } else {
                    similitude.add(new ComparablePairDouble(i, sumRatings1 / (sqrt(sumRatings2) * sqrt(sumRatings3))));
                }
            }
        }

        Collections.sort(similitude);
        Collections.reverse(similitude);

        for (int i = 0; i < movieQuantity(); i++) {
            if (userRatings.get(i) != 0) {
                continue;
            }
            int neighbourCount = 0;
            double sumRating = 0;
            double sumSimilitude = 0;
            for (int j = 0; j < 20; j++) {
                DoubleMatrix1D otherUserRatings = ratingIndex.viewRow(similitude.get(j).first);
                double otherUserValue = otherUserRatings.get(i);
                if (otherUserValue == 0) {
                    continue;
                }
                double otherUserMean = otherUserRatings.zSum() / otherUserRatings.cardinality();
                sumRating += (otherUserValue - otherUserMean) * similitude.get(j).second;
                sumSimilitude += similitude.get(j).second;
                neighbourCount++;
            }
            if (neighbourCount == 0) {
                userRatings.setQuick(i, 0.0);
            } else {
                double prediction = userMean + sumRating / sumSimilitude;
                userRatings.setQuick(i, prediction);
            }

        }

        ArrayList<ComparablePairDouble<Integer>> finalRatingList = new ArrayList(movieQuantity());

        for (int i = 0; i < movieQuantity(); i++) {
            finalRatingList.add(new ComparablePairDouble(i, userRatings.getQuick(i)));
        }
        Collections.sort(finalRatingList);
        Collections.reverse(finalRatingList);
        for (int i = 0; i < finalRatingList.size(); i++) {
            System.out.println((finalRatingList.get(i).first + 1) + " - " + searchMovie(finalRatingList.get(i).first).getName() + " - Score: " + finalRatingList.get(i).second);
        }

    }

    public int userQuantity() {
        return userDictionary.size();
    }

    public int movieQuantity() {
        return movieDictionary.size();
    }

}
