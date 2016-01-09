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
import java.util.logging.Level;
import java.util.logging.Logger;

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
                if (itemCount < 20) {
                    similitude.add(new ComparableTriDouble(i,
                            (sumRatings1 / (sqrt(sumRatings2) * sqrt(sumRatings3))) * itemCount / 20,
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

        for (int i = 0; i < movieQuantity(); i++) {
            if (userRatings.get(i) != 0) {
                continue;
            }
            int neighbourCount = 0;
            double sumRating = 0;
            double sumSimilitude = 0;
            for (int j = 0; j < dbreader.getNeighborhoodSize(); j++) {
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
            finalRatingList.add(new ComparableTriDouble(i, userRatings.getQuick(i), userRatings.getQuick(i)));
        }
        Collections.sort(finalRatingList);
        Collections.reverse(finalRatingList);

        // Implementación RMSE sobre los datos del usuario 23
        if (idUser == (23 - 1)) {

            try {
                DBReader dbReader = DBReader.getInstance();
                CsvReader user23File = new CsvReader(dbReader.getDirResources() + dbReader.getFileUser23(), ',');
                user23File.readHeaders();
                SparseDoubleMatrix1D user23Ratings = new SparseDoubleMatrix1D(2048);

                while (user23File.readRecord()) {
                    int idItem = Integer.parseInt(user23File.get(0)) - 1;
                    double rating = Double.parseDouble(user23File.get(1));
                    user23Ratings.setQuick(idItem, rating);
                }

                int count = 0;

                for (int i = 0; i < finalRatingList.size(); i++) {

                    if (finalRatingList.get(i).second != user23Ratings.get(searchMovie(finalRatingList.get(i).first).getID()) 
                            && (finalRatingList.get(i).second == 0.0 || user23Ratings.get(searchMovie(finalRatingList.get(i).first).getID()) == 0.0)) {
                        System.out.println((finalRatingList.get(i).first + 1)
                                + "; " + searchMovie(finalRatingList.get(i).first).getName()
                                + "; Score: " + finalRatingList.get(i).second
                                + "; Expected from User 23: " + user23Ratings.get(searchMovie(finalRatingList.get(i).first).getID()));
                        count++;
                    }
                }

                System.out.println("Errors: " + count);

                double errorSum = 0.0;
                int itemCount = 0;
                for (int i = 0; i < movieQuantity(); i++) {
                    double userValue = userRatings.get(i);
                    double user23Value = user23Ratings.get(i);
                    if (userValue == 0.0 || user23Value == 0.0) {
                        continue;
                    }
                    errorSum += pow(userValue - user23Value, 2);
                    itemCount++;
                }
                double RMSEValue = sqrt(errorSum / itemCount);
                System.out.println("RMSE value: " + RMSEValue);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Recommender.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Recommender.class.getName()).log(Level.SEVERE, null, ex);
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
