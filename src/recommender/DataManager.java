package recommender;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;
import com.csvreader.CsvReader;
import java.io.FileNotFoundException;
import java.io.IOException;
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
            if (dbreader.isDebugSimilitude() && i == (dbreader.getDebugSimilitudeId() - 1)) {
                System.out.println("Debug messages for similitude with user " + dbreader.getDebugSimilitudeId() + " --");
                System.out.println();
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
                if (dbreader.isDebugSimilitude() && i == (dbreader.getDebugSimilitudeId() - 1)) {
                    System.out.println("Reading ratings for item " + (j + 1));
                    System.out.println("User " + dbreader.getUserIDToPredict() + " rated it with " + userValue);
                    System.out.println("User " + dbreader.getDebugSimilitudeId() + " rated it with " + otherUserValue);
                    System.out.println();
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
                    if (dbreader.isDebugSimilitude() && i == (dbreader.getDebugSimilitudeId() - 1)) {
                        System.out.println("There were " + itemCount + " ratings between the users.");
                        System.out.println("Rating mean of user " + dbreader.getUserIDToPredict() + " is " + userMean);
                        System.out.println("Rating mean of user " + dbreader.getDebugSimilitudeId() + " is " + otherUserMean);
                        System.out.println("If we add all the ratings of each user, substracting rating mean of each, and then multiply them, we get " + sumRatings1);
                        System.out.println("Adding each rating of user "
                                + dbreader.getUserIDToPredict()
                                + " squared, we get " + sumRatings2
                                + ", which turns to be " + sqrt(sumRatings2)
                                + " once we do the square root");
                        System.out.println("Adding each rating of user "
                                + dbreader.getDebugSimilitudeId()
                                + " squared, we get " + sumRatings3
                                + ", which turns to be " + sqrt(sumRatings3)
                                + " once we do the square root");
                        System.out.println("All together, we get a final similitude value of " + (sumRatings1 / (sqrt(sumRatings2) * sqrt(sumRatings3))));
                        System.out.println("Since we only got " + itemCount + " ratings between the users, we adjust similitude to " + ((itemCount / dbreader.getSimilitudeAdjustValue()) * 100) + "% of its value.");
                        System.out.println();
                        System.out.println();
                    }
                } else {
                    similitude.add(new ComparableTriDouble(i,
                            sumRatings1 / (sqrt(sumRatings2) * sqrt(sumRatings3)),
                            sumRatings1 / (sqrt(sumRatings2) * sqrt(sumRatings3))));
                    if (dbreader.isDebugSimilitude() && i == (dbreader.getDebugSimilitudeId() - 1)) {
                        System.out.println("There were " + itemCount + " ratings between the users.");
                        System.out.println("Rating mean of user " + dbreader.getUserIDToPredict() + " is " + userMean);
                        System.out.println("Rating mean of user " + dbreader.getDebugSimilitudeId() + " is " + otherUserMean);
                        System.out.println("If we add all the ratings of each user, substracting rating mean of each, and then multiply them, we get " + sumRatings1);
                        System.out.println("Adding each rating of user "
                                + dbreader.getUserIDToPredict()
                                + " squared, we get " + sumRatings2
                                + ", which turns to be " + sqrt(sumRatings2)
                                + " once we do the square root");
                        System.out.println("Adding each rating of user "
                                + dbreader.getDebugSimilitudeId()
                                + " squared, we get " + sumRatings3
                                + ", which turns to be " + sqrt(sumRatings3)
                                + " once we do the square root");
                        System.out.println("All together, we get a final similitude value of " + (sumRatings1 / (sqrt(sumRatings2) * sqrt(sumRatings3))));
                        System.out.println();
                        System.out.println();
                    }
                }
            }
        }

        Collections.sort(similitude);
        Collections.reverse(similitude);

        //Include similitude checking of neighborhood here.
        System.out.println("User " + dbreader.getUserIDToPredict() + " neighborhood --");
        System.out.println();
        if (dbreader.isSimilitudeAdjust()) {
            for (int i = 0; i < dbreader.getNeighborhoodSize(); i++) {
                System.out.println("User " + (similitude.get(i).first + 1)
                        + " has adjusted similitude " + similitude.get(i).second
                        + " and not-adjusted similitude " + similitude.get(i).third);
            }
        } else {
            for (int i = 0; i < dbreader.getNeighborhoodSize(); i++) {
                System.out.println("User " + (similitude.get(i).first + 1)
                        + " has similitude " + similitude.get(i).second);
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

        SparseDoubleMatrix1D user23Ratings = new SparseDoubleMatrix1D(2048);

        try {
            DBReader dbReader = DBReader.getInstance();
            CsvReader user23File = new CsvReader(dbReader.getDirResources() + dbReader.getFileUser23(), ',');
            user23File.readHeaders();

            while (user23File.readRecord()) {
                int idItem = Integer.parseInt(user23File.get(0)) - 1;
                double rating = Double.parseDouble(user23File.get(1));
                user23Ratings.setQuick(idItem, rating);
            }

        } catch (FileNotFoundException ex) {
            // do nothing
        } catch (IOException ex) {
            // do nothing
        }

        System.out.println("Predictions for user " + dbreader.getUserIDToPredict() + " --");
        System.out.println();
        for (int i = 0; i < ratingListSize; i++) {
            System.out.print((finalRatingList.get(i).first + 1) + " - "
                    + searchMovie(finalRatingList.get(i).first).getName()
                    + " - Score: " + finalRatingList.get(i).second);
            if (idUser == (23 - 1)) {
                System.out.println("; Expected from User 23: " + user23Ratings.get(searchMovie(finalRatingList.get(i).first).getID()));
            } else {
                System.out.println();
            }
        }

    }

    public int userQuantity() {
        return userDictionary.size();
    }

    public int movieQuantity() {
        return movieDictionary.size();
    }

}
