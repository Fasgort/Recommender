package recommender;

import java.util.ArrayList;

/**
 *
 * @author Fasgort
 */
public class MovieDictionary extends Dictionary<IndexedMovie> {

    private static volatile MovieDictionary instance = null;

    private MovieDictionary() {
        ArrayList<IndexedMovie> movieIDs = null;

        if (movieIDs == null) {
            entryIDs = new ArrayList(2000);
        } else {
            entryIDs = movieIDs;
        }

    }

    protected static MovieDictionary getInstance() {
        if (instance == null) {
            instance = new MovieDictionary();
        }
        return instance;
    }

    @Override
    protected int add(IndexedMovie newMovie) {
        int idMovie = newMovie.getID();
        entryIDs.add(idMovie, newMovie);
        return idMovie;
    }

}
