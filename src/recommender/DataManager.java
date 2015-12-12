package recommender;

import cern.colt.matrix.tint.impl.SparseIntMatrix2D;

/**
 *
 * @author Fasgort
 */
public class DataManager {

    private static volatile DataManager instance = null;
    final private UserDictionary userDictionary;
    final private MovieDictionary movieDictionary;
    SparseIntMatrix2D ratingIndex;

    protected DataManager() {
        userDictionary = UserDictionary.getInstance();
        movieDictionary = MovieDictionary.getInstance();

        SparseIntMatrix2D _ratingIndex = null;

        if (_ratingIndex == null) {
            ratingIndex = new SparseIntMatrix2D(1024, 2048);
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

    public int userQuantity() {
        return userDictionary.size();
    }

    public int movieQuantity() {
        return movieDictionary.size();
    }

}
