package ch.epfl.javions.aircraft;

import java.util.Enumeration;
import java.util.Objects;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class AircraftDatabase {

    private final String fileName;

    public AircraftDatabase(String fileName) {
        Objects.requireNonNull(fileName);
        this.fileName = fileName;
    }

    public AircraftData get(IcaoAddress address) throws IOException {
        String database = getClass().getResource(fileName).getFile();
        try (ZipFile zipFile = new ZipFile(database)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().endsWith(".csv")) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] fields = line.split(",");
                            IcaoAddress currentAddress = new IcaoAddress(fields[0]);
                            if (currentAddress.string().compareTo(address.string()) == 0) {
                                AircraftRegistration registration = new AircraftRegistration(fields[1]);
                                AircraftTypeDesignator typeDesignator = new AircraftTypeDesignator(fields[2]);
                                String model = fields[3];
                                AircraftDescription description = new AircraftDescription(fields[4]);
                                WakeTurbulenceCategory wakeTurbulenceCategory = WakeTurbulenceCategory.of(fields[5]);
                                return new AircraftData(registration,typeDesignator,model,description,wakeTurbulenceCategory);
                            } else if (currentAddress.string().compareTo(address.string()) > 0) {
                                break;
                            }
                        }
                    } catch (IOException e) {
                        throw new IOException("Erreur lors de la lecture du fichier "+ entry.getName());
                    }
                }
            }
        } catch (IOException e){
            throw new IOException("Erreur lors de l'ouverture du fichier ZIP");
        }
        return null;
    }
}
