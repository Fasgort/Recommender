package recommender;

import com.csvreader.CsvReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Fasgort
 */
public class Recommender {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // Lectura de configuración
        DBReader dbReader = DBReader.getInstance(args[0]);
        if (dbReader.fail(args[0])) {
            return;
        }

        // Estructuras de datos
        DataManager dataManager = DataManager.getInstance();

        // Lectura de ficheros  
        try {
            CsvReader userReader = new CsvReader(dbReader.getDirResources() + dbReader.getFileUsers(), ',');
            userReader.readHeaders();
            while (userReader.readRecord()) {
                dataManager.addUser(userReader.get(1),
                        Short.parseShort(userReader.get(2).replace(".0", "")),
                        userReader.get(3).equalsIgnoreCase("M"),
                        userReader.get(4), userReader.get(5));
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Recommender.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Recommender.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            CsvReader itemReader = new CsvReader(dbReader.getDirResources() + dbReader.getFileItems(), ',');
            itemReader.readHeaders();
            while (itemReader.readRecord()) {
                dataManager.addMovie(itemReader.get(1),
                        itemReader.get(2).equalsIgnoreCase("1"),
                        itemReader.get(3).equalsIgnoreCase("1"),
                        itemReader.get(4).equalsIgnoreCase("1"),
                        itemReader.get(5).equalsIgnoreCase("1"),
                        itemReader.get(6).equalsIgnoreCase("1"),
                        itemReader.get(7).equalsIgnoreCase("1"),
                        itemReader.get(8).equalsIgnoreCase("1"),
                        itemReader.get(9).equalsIgnoreCase("1"),
                        itemReader.get(10).equalsIgnoreCase("1"),
                        itemReader.get(11).equalsIgnoreCase("1"),
                        itemReader.get(12).equalsIgnoreCase("1"),
                        itemReader.get(13).equalsIgnoreCase("1"),
                        itemReader.get(14).equalsIgnoreCase("1"),
                        itemReader.get(15).equalsIgnoreCase("1"),
                        itemReader.get(16).equalsIgnoreCase("1"),
                        itemReader.get(17).equalsIgnoreCase("1"),
                        itemReader.get(18).equalsIgnoreCase("1"),
                        itemReader.get(19).equalsIgnoreCase("1"),
                        itemReader.get(20).equalsIgnoreCase("1"));
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Recommender.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Recommender.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            CsvReader ratingReader = new CsvReader(dbReader.getDirResources() + dbReader.getFileRatings(), ',');
            ratingReader.readHeaders();
            while (ratingReader.readRecord()) {
                dataManager.addRating(Integer.parseInt(ratingReader.get(0)) - 1, 
                        Integer.parseInt(ratingReader.get(1)) - 1, 
                        Integer.parseInt(ratingReader.get(2)));
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Recommender.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Recommender.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Al turrón
        dataManager.predictRating(dbReader.getUserIDToPredict() - 1);

    }

}
