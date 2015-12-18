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
    private int userIDToPredict = -1;

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
                        case "userIDToPredict":
                            userIDToPredict = Integer.parseInt(valor);
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
                    fileItems = "to-csv-content.csv";
                    bw.append("fileItems = " + fileItems + " // Name of fileItems file. It will be located in the resources directory.");
                    bw.newLine();
                }

                if (fileRatings == null) {
                    fileRatings = "to-csv-ratings.csv";
                    bw.append("fileRatings = " + fileRatings + " // Name of fileRatings file. It will be located in the resources directory.");
                    bw.newLine();
                }

                if (fileUsers == null) {
                    fileUsers = "to-csv-users.csv";
                    bw.append("fileUsers = " + fileUsers + " // Name of fileUsers file. It will be located in the resources directory.");
                    bw.newLine();
                }

                return false;
            } catch (IOException ex) {
                System.err.println(ex);
                return true;
            }

        }

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

}
