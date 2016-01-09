package recommender;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;
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
        DoubleMatrix1D originalUserRatings = ratingIndex.viewRow(idUser);
        DoubleMatrix1D userRatings = ratingIndex.viewRow(idUser).copy();
        double userMean = userRatings.zSum() / userRatings.cardinality();
        DBReader dbreader = DBReader.getInstance();

        ArrayList<ComparableTriDouble<Integer>> similitude = new ArrayList<>(userQuantity());

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
                similitude.add(new ComparableTriDouble(i, 0.0, 0.0));
            } else {
                if (itemCount < dbreader.getSimilitudeAdjustValue() && dbreader.isSimilitudeAdjust()) {
                    similitude.add(new ComparableTriDouble(i,
                            (sumRatings1 / (sqrt(sumRatings2) * sqrt(sumRatings3))) * itemCount / dbreader.getSimilitudeAdjustValue(),
                            sumRatings1 / (sqrt(sumRatings2) * sqrt(sumRatings3))));
                } else {
                    similitude.add(new ComparableTriDouble(i,
                            sumRatings1 / (sqrt(sumRatings2) * sqrt(sumRatings3)),
                            sumRatings1 / (sqrt(sumRatings2) * sqrt(sumRatings3))));
                }
            }
        }

        Collections.sort(similitude);
        Collections.reverse(similitude);

        //Include similitude checking of neighborhood here.
        System.out.println("Vecindario del usuario --");
        System.out.println();
        if (dbreader.isSimilitudeAdjust()) {
            for (int i = 0; i < dbreader.getNeighborhoodSize(); i++) {
                System.out.println("Usuario: " + (similitude.get(i).first + 1)
                        + " con similitud ajustada " + similitude.get(i).second
                        + " y similitud no ajustada " + similitude.get(i).third);
            }
        } else {
            for (int i = 0; i < dbreader.getNeighborhoodSize(); i++) {
                System.out.println("Usuario: " + (similitude.get(i).first + 1)
                        + " con similitud " + similitude.get(i).second);
            }
        }
        System.out.println();
        System.out.println();

        for (int i = 0; i < movieQuantity(); i++) {
            if (originalUserRatings.getQuick(i) != 0) {
                continue;
            }
            int neighbourCount = 0;
            double sumRating = 0;
            double sumSimilitude = 0;
            if (dbreader.isDebugRatings() && dbreader.getDebugRatingId() == (i + 1)) {
                System.out.println("Debug messages for prediction in item " + dbreader.getDebugRatingId() + " --");
                System.out.println();
            }
            for (int j = 0; j < dbreader.getNeighborhoodSize(); j++) {
                DoubleMatrix1D otherUserRatings = ratingIndex.viewRow(similitude.get(j).first);
                double otherUserValue = otherUserRatings.getQuick(i);
                if (otherUserValue == 0) {
                    continue;
                }
                double otherUserMean = otherUserRatings.zSum() / otherUserRatings.cardinality();
                if (dbreader.isDebugRatings() && dbreader.getDebugRatingId() == (i + 1)) {
                    System.out.print("Using rating " + otherUserValue + " from neighbour ID " + (similitude.get(j).first + 1) + " with similitude ");
                    if (dbreader.isUseAdjusted()) {
                        System.out.println(similitude.get(j).second);
                    } else {
                        System.out.println(similitude.get(j).third);
                    }
                    System.out.println("This neighbour has a rating mean of " + otherUserMean);
                    System.out.println("Effective rating is " + (otherUserValue - otherUserMean));
                    System.out.print("Rating used together with neighbour similitude, makes a effective rating of ");
                    if (dbreader.isUseAdjusted()) {
                        System.out.println((otherUserValue - otherUserMean) * similitude.get(j).second);
                    } else {
                        System.out.println((otherUserValue - otherUserMean) * similitude.get(j).third);
                    }
                    System.out.println();
                }
                if (dbreader.isUseAdjusted()) {
                    sumRating += (otherUserValue - otherUserMean) * similitude.get(j).second;
                    sumSimilitude += similitude.get(j).second;
                } else {
                    sumRating += (otherUserValue - otherUserMean) * similitude.get(j).third;
                    sumSimilitude += similitude.get(j).third;
                }
                neighbourCount++;
            }
            if (dbreader.isDebugRatings() && dbreader.getDebugRatingId() == (i + 1)) {
                System.out.println("We used " + neighbourCount + " predictions from the neighbourhood.");
            }
            if (neighbourCount == 0) {
                userRatings.setQuick(i, 0.0);
            } else {
                double prediction = userMean + sumRating / sumSimilitude;
                if (dbreader.isDebugRatings() && dbreader.getDebugRatingId() == (i + 1)) {
                    System.out.println("User prediction mean is " + userMean);
                    System.out.println("Sum of all ratings for this item is " + sumRating);
                    System.out.println("Sum of all similitude of our neighbours (with predictions for this item) is " + sumSimilitude);
                    System.out.println("Our prediction is \"prediction = userMean + SumRating/sumSimilitude\" and the result is " + prediction);
                    System.out.println();
                }
                if (prediction > 5.0) {
                    prediction = 5.0;
                } else if (prediction < 1.0) {
                    prediction = 1.0;
                }
                userRatings.setQuick(i, prediction);
            }

        }

        ArrayList<ComparableTriDouble<Integer>> finalRatingList = new ArrayList(movieQuantity());

        for (int i = 0; i < movieQuantity(); i++) {
            if (originalUserRatings.getQuick(i) != 0) {
                continue;
            }
            finalRatingList.add(new ComparableTriDouble(i, userRatings.getQuick(i), userRatings.getQuick(i)));
        }
        Collections.sort(finalRatingList);
        Collections.reverse(finalRatingList);

        int ratingListSize;

        if (finalRatingList.size() < dbreader.getResultSize()) {
            ratingListSize = finalRatingList.size();
        } else {
            ratingListSize = dbreader.getResultSize();
        }

        for (int i = 0; i < ratingListSize; i++) {
            System.out.println((finalRatingList.get(i).first + 1) + " - "
                    + searchMovie(finalRatingList.get(i).first).getName()
                    + " - Score: " + finalRatingList.get(i).second);
        }

    }

    public int userQuantity() {
        return userDictionary.size();
    }

    public int movieQuantity() {
        return movieDictionary.size();
    }

}
