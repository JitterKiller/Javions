package ch.epfl.javions.aircraft;

import java.io.*;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * La classe AircraftDatabase représente une base de données d'aéronefs stockée dans un fichier ZIP contenant des
 * fichiers CSV.
 * La base de données peut être interrogée avec une adresse ICAO, ce qui renvoie les informations de l'aéronef
 * (AircraftData) associées à cette adresse.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 *
 */
public final class AircraftDatabase {

    /**
     * Le nom du fichier ZIP contenant la base de données.
     */
    private final String fileName;

    /**
     * Construit une base de données d'aéronefs à partir du nom de fichier spécifié.
     *
     * @param fileName
     *          le nom du fichier ZIP contenant la base de données
     * @throws NullPointerException si le nom de fichier est nul
     */
    public AircraftDatabase(String fileName) {
        this.fileName = Objects.requireNonNull(fileName, "Le fichier ne peut pas être nul");
    }

    /**
     * Récupère les informations d'un aéronef à partir de son adresse ICAO.
     *
     * @param address
     *          l'adresse ICAO de l'aéronef à rechercher
     * @return les informations de l'aéronef (AircraftData) associées à l'adresse ICAO spécifiée, null si aucune
     * information n'a été trouvée.
     * @throws IOException si une erreur se produit lors de l'accès au fichier ZIP ou à un des fichiers
     * CSV de la base de données
     * @throws NullPointerException si l'adresse ICAO est nulle
     */

    public AircraftData get(IcaoAddress address) throws IOException {
        Objects.requireNonNull(address, "L'adresse ICAO ne peut pas être nulle");

        try (ZipFile zipFile = new ZipFile(fileName)) {

            String entry = address.string().substring(address.string().length()-2) + ".csv";
            /* Ouvre le fichier CSV correspondant à l'adresse courante dans le fichier ZIP */
            try (InputStream inputStream = zipFile.getInputStream(zipFile.getEntry(entry));
                 Reader reader = new InputStreamReader(inputStream, UTF_8);
                 BufferedReader bufferedReader = new BufferedReader(reader)) {
                String line;

                /* Parcourir toutes les lignes du fichier CSV */
                while ((line = bufferedReader.readLine()) != null) {
                    String[] fields = line.split(",", -1);
                    IcaoAddress currentAddress = new IcaoAddress(fields[0]);

                    /* Si l'adresse ICAO correspond, créer un objet AircraftData avec les informations de
                    l'aéronef et le retourner */
                    if (currentAddress.string().compareTo(address.string()) == 0) {
                        AircraftRegistration registration = new AircraftRegistration(fields[1]);
                        AircraftTypeDesignator typeDesignator = new AircraftTypeDesignator(fields[2]);
                        String model = fields[3];
                        AircraftDescription description = new AircraftDescription(fields[4]);
                        WakeTurbulenceCategory wakeTurbulenceCategory = WakeTurbulenceCategory.of(fields[5]);
                        return new AircraftData(registration, typeDesignator, model, description, wakeTurbulenceCategory);
                    }
                    /* Si l'adresse ICAO recherchée est inférieure à l'adresse courante, sortir de la boucle */
                    else if (currentAddress.string().compareTo(address.string()) > 0) {
                        break;
                    }
                }
            } catch (IOException e){
                throw new IOException("Erreur lors de la lecture du fichier "+ entry);
            }
        } catch (IOException e) {
            throw new IOException("Erreur lors de l'ouverture du fichier ZIP", e);
        }
        return null;
    }
}