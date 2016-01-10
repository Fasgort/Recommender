package recommender;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Fasgort
 */
public class DBReader {

    private static volatile DBReader instance = null;
    private boolean read = false;
    private String dirResources = null;
    private String fileItems = null;
    private String fileRatings = null;
    private String fileUsers = null;
    private String fileUser23 = null;
    private int userIDToPredict = -1;
    private int neighborhoodSize = -1;
    private boolean similitudeAdjust = false;
    private boolean similitudeAdjustRead = false;
    private int similitudeAdjustValue = -1;
    private boolean useAdjusted = false;
    private boolean useAdjustedRead = false;
    private boolean debugSimilitude = false;
    private boolean debugSimilitudeRead = false;
    private int debugSimilitudeId = -1;
    private boolean debugRatings = false;
    private boolean debugRatingsRead = false;
    private int debugRatingId = -1;
    private int resultSize = -1;

    private DBReader(String stringConfData) {

        File confData = new File(stringConfData);

        try (FileReader fr = new FileReader(confData);
                BufferedReader br = new BufferedReader(fr)) {
            Pattern comment = Pattern.compile("^([\\w/.]+) = ((?:\".*\")|(?:[\\w/.]+))");
            Matcher m;
            String linea;
            while ((linea = br.readLine()) != null) {
                m = comment.matcher(linea);
                if (m.find()) {
                    String atributo;
                    String valor;

                    atributo = m.group(1).trim();
                    valor = m.group(2).trim();

                    if (valor.startsWith("\"") && valor.endsWith("\"")) {
                        valor = valor.substring(1, valor.length() - 1);
                    }

                    switch (atributo) {
                        case "dirResources":
                            dirResources = valor;
                            break;
                        case "fileItems":
                            fileItems = valor;
                            break;
                        case "fileRatings":
                            fileRatings = valor;
                            break;
                        case "fileUsers":
                            fileUsers = valor;
                            break;
                        case "fileUser23":
                            fileUser23 = valor;
                            break;
                        case "userIDToPredict":
                            userIDToPredict = Integer.parseInt(valor);
                            break;
                        case "neighborhoodSize":
                            neighborhoodSize = Integer.parseInt(valor);
                            break;
                        case "similitudeAdjust":
                            similitudeAdjust = Boolean.parseBoolean(valor);
                            similitudeAdjustRead = true;
                            break;
                        case "similitudeAdjustValue":
                            similitudeAdjustValue = Integer.parseInt(valor);
                            break;
                        case "resultSize":
                            resultSize = Integer.parseInt(valor);
                            break;
                        case "useAdjusted":
                            useAdjusted = Boolean.parseBoolean(valor);
                            useAdjustedRead = true;
                            break;
                        case "debugSimilitude":
                            debugSimilitude = Boolean.parseBoolean(valor);
                            debugSimilitudeRead = true;
                            break;
                        case "debugSimilitudeId":
                            debugSimilitudeId = Integer.parseInt(valor);
                            break;
                        case "debugRatings":
                            debugRatings = Boolean.parseBoolean(valor);
                            debugRatingsRead = true;
                            break;
                        case "debugRatingId":
                            debugRatingId = Integer.parseInt(valor);
                            break;
                    }
                }
            }
            br.close();
            read = true;
        } catch (IOException ex) {
            System.err.println(ex);
        }

    }

    public static DBReader getInstance(String stringConfData) {
        if (instance == null) {
            instance = new DBReader(stringConfData);
        }
        return instance;
    }

    public static DBReader getInstance() {
        if (instance == null) {
            return null;
        }
        return instance;
    }

    public boolean fail(String stringConfData) {
        if (read == false) {
            return true;
        } else {
            File confData = new File(stringConfData);
            try (FileWriter fw = new FileWriter(confData, true);
                    BufferedWriter bw = new BufferedWriter(fw)) {

                if (userIDToPredict == -1) {
                    userIDToPredict = 23;
                    bw.append("userIDToPredict = " + userIDToPredict + " // ID of the ratings user we want to predict.");
                    bw.newLine();
                }

                if (dirResources == null) {
                    dirResources = "./resources/";
                    bw.append("dirResources = " + dirResources + " // Directory where resources are found.");
                    bw.newLine();
                }

                if (fileItems == null) {
                    fileItems = "items.csv";
                    bw.append("fileItems = " + fileItems + " // Name of fileItems file. It will be located in the resources directory.");
                    bw.newLine();
                }

                if (fileRatings == null) {
                    fileRatings = "ratings.csv";
                    bw.append("fileRatings = " + fileRatings + " // Name of fileRatings file. It will be located in the resources directory.");
                    bw.newLine();
                }

                if (fileUsers == null) {
                    fileUsers = "users.csv";
                    bw.append("fileUsers = " + fileUsers + " // Name of fileUsers file. It will be located in the resources directory.");
                    bw.newLine();
                }

                if (fileUser23 == null) {
                    fileUser23 = "user23.csv";
                    bw.append("fileUser23 = " + fileUser23 + " // Name of fileUser23 file. It will be located in the resources directory.");
                    bw.newLine();
                }

                if (neighborhoodSize == -1) {
                    neighborhoodSize = 20;
                    bw.append("neighborhoodSize = " + neighborhoodSize + " // Size of neighborhood.");
                    bw.newLine();
                }

                if (resultSize == -1) {
                    resultSize = 50;
                    bw.append("resultSize = " + resultSize + " // Size of the result list, ordered from highest rating to lowest.");
                    bw.newLine();
                }

                if (similitudeAdjustRead == false) {
                    similitudeAdjust = true;
                    bw.append("similitudeAdjust = " + similitudeAdjust + " // Activate or deactivate use of similitude adjusting.");
                    bw.newLine();
                }

                if (similitudeAdjustValue == -1) {
                    similitudeAdjustValue = 20;
                    bw.append("similitudeAdjustValue = " + similitudeAdjustValue + " // Minimum number of ratings needed for a full similitude value.");
                    bw.newLine();
                }

                if (useAdjustedRead == false) {
                    useAdjusted = true;
                    bw.append("useAdjusted = " + useAdjusted + " // Use adjusted similitude for rating predicting aswell.");
                    bw.newLine();
                }

                if (debugSimilitudeRead == false) {
                    debugSimilitude = false;
                    bw.append("debugSimilitude = " + debugSimilitude + " // Will include debug messages for calculating similitude with users.");
                    bw.newLine();
                }

                if (debugSimilitudeId == -1) {
                    debugSimilitudeId = 57;
                    bw.append("debugSimilitudeId = " + debugSimilitudeId + " // If debugSimilitude is true, it will debug the similitude of the user choosed here.");
                    bw.newLine();
                }

                if (debugRatingsRead == false) {
                    debugRatings = false;
                    bw.append("debugRatings = " + debugRatings + " // Will include debug messages for calculating a predicted rating.");
                    bw.newLine();
                }

                if (debugRatingId == -1) {
                    debugRatingId = 114;
                    bw.append("debugRatings = " + debugRatings + " // If debugRatings is true, it will debug the rating choosed here.");
                    bw.newLine();
                }

                return false;
            } catch (IOException ex) {
                System.err.println(ex);
                return true;
            }

        }

    }

    public boolean isDebugSimilitude() {
        return debugSimilitude;
    }

    public int getDebugSimilitudeId() {
        return debugSimilitudeId;
    }

    public int getUserIDToPredict() {
        return userIDToPredict;
    }

    public String getDirResources() {
        return dirResources;
    }

    public String getFileItems() {
        return fileItems;
    }

    public String getFileRatings() {
        return fileRatings;
    }

    public String getFileUsers() {
        return fileUsers;
    }

    public String getFileUser23() {
        return fileUser23;
    }

    public int getNeighborhoodSize() {
        return neighborhoodSize;
    }

    public boolean isSimilitudeAdjust() {
        return similitudeAdjust;
    }

    public int getSimilitudeAdjustValue() {
        return similitudeAdjustValue;
    }

    public boolean isUseAdjusted() {
        return useAdjusted;
    }

    public int getResultSize() {
        return resultSize;
    }

    public boolean isDebugRatings() {
        return debugRatings;
    }

    public int getDebugRatingId() {
        return debugRatingId;
    }

}
