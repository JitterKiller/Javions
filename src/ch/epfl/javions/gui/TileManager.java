package ch.epfl.javions.gui;

import ch.epfl.javions.Preconditions;
import javafx.scene.image.Image;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * La classe TileManager du sous-paquetage gui, représente un gestionnaire de tuiles OSM.
 * Son rôle est d'obtenir les tuiles depuis un serveur de tuile et de les stocker dans un cache mémoire
 * et dans un cache disque.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public final class TileManager {

    /**
     * La taille de côté d'une tuile OSM (256 pixels).
     */
    public static final int TILE_SIDE = 256;
    private static final int MEMORY_CACHE_SIZE = 100;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final String DELIMITER = "/";
    private static final String PREFIX = "https://";
    private static final String PNG = ".png";
    private final Path diskCachePath;
    private final String severName;
    private final Map<TileId, Image> memoryCache =
            new LinkedHashMap<>(MEMORY_CACHE_SIZE, DEFAULT_LOAD_FACTOR, true);

    /**
     * Constructeur de la classe TileManager.
     *
     * @param diskCachePath Le chemin d'accès au dossier contenant le cache disque.
     * @param serverName    Le nom du serveur de tuile.
     * @throws IllegalArgumentException Si le nom du serveur est vide.
     * @throws NullPointerException     Si le chemin d'accès au cache disque est nul.
     */
    public TileManager(Path diskCachePath, String serverName) {
        Preconditions.checkArgument(!serverName.isEmpty());

        this.diskCachePath = Objects.requireNonNull(diskCachePath);
        this.severName = serverName;
    }

    /**
     * Seule méthode publique de TileManager prenant en argument l'identité d'une tuile (de type TileId)
     * et retourne son image (de type Image de la bibliothèque JavaFX).
     *
     * @param id L'identité de la tuile.
     * @return L'image correspondant à la tuile de type Image de la bibliothèque JavaFX.
     * @throws IOException Si une erreur d'entrée/sortie se produit.
     */
    public Image imageForTileAt(TileId id) throws IOException {

        /* Si l'image est contenue dans le cache mémoire, on la retourne directement.*/
        if (memoryCache.containsKey(id)) return memoryCache.get(id);

        Path tilePath = Path.of(String.valueOf(diskCachePath), String.valueOf(id.zoom),
                String.valueOf(id.X), id.Y + PNG);

        /* Sinon, on retourne l'image si elle est contenue dans le cache disque
         * Sinon on la télécharge depuis le serveur de tuile et on la retourne avec la méthode load()
         * (en s'assurant de bien la placée dans le cache disque et le cache mémoire. */
        if (Files.exists(tilePath)) {
            Image image = new Image(String.valueOf(tilePath.toUri()));
            addToMemoryCache(id, image);
            return image;
        } else {
            if (!Files.isDirectory(tilePath.getParent())) {
                Files.createDirectories(tilePath.getParent());
            }
            return load(id, tilePath);
        }

    }

    /**
     * Méthode utilisée lorsque la tuile ne se trouve ni dans le cache mémoire, ni dans le cache disque.
     * Cette méthode s'occupe donc de télécharger et de stocker la tuile dans le cache disque/mémoire.
     *
     * @param id       L'identité de la tuile à télécharger.
     * @param tilePath Le chemin d'accès de la tuile stockée dans le cache disque.
     * @return L'image de type Image (JavaFX) téléchargée du serveur de tuile.
     * @throws IOException Si une erreur d'entrée/sortie se produit.
     */
    private Image load(TileId id, Path tilePath) throws IOException {
        StringJoiner imageURL = new StringJoiner(DELIMITER, PREFIX, PNG);
        imageURL.add(severName).add(String.valueOf(id.zoom)).add(String.valueOf(id.X)).add(String.valueOf(id.Y));
        URL u = new URL(imageURL.toString());
        URLConnection c = u.openConnection();
        c.setRequestProperty("User-Agent", "Javions");
        try (InputStream i = c.getInputStream();
             OutputStream o = new FileOutputStream(new File(tilePath.toUri()))) {
            byte[] imageBytes = i.readAllBytes();
            o.write(imageBytes);
            Image image = new Image(new ByteArrayInputStream(imageBytes));
            addToMemoryCache(id, image);
            return image;
        }
    }

    /**
     * Méthode qui permet d'ajouter une image au cache mémoire.
     * Si la taille du cache mémoire est égale à 100, on supprime l'Image la moins utilisée du cache.
     *
     * @param id    L'identité de la tuile à télécharger.
     * @param image L'image correspondant à la tuile.
     */
    private void addToMemoryCache(TileId id, Image image) {
        if (memoryCache.keySet().size() == MEMORY_CACHE_SIZE) {
            Iterator<TileId> it = memoryCache.keySet().iterator();
            memoryCache.remove(it.next());
        }
        memoryCache.put(id, image);
    }

    /**
     * L'enregistrement TileId, imbriqué dans la classe TileManager représente l'identité d'une tuile OSM.
     *
     * @param zoom Le niveau de zoom de la tuile.
     * @param X    L'index X de la tuile.
     * @param Y    L'index Y de la tuile.
     */
    record TileId(int zoom, int X, int Y) {

        /**
         * Constructeur compact de l'enregistrement TileID, vérifie si les
         * arguments zoom, X et Y sont valides à l'aide de la méthode isValid().
         *
         * @throws IllegalArgumentException Si les arguments zoom, X et Y ne sont pas valides.
         *                                  (ne constituent pas une tuile valide).
         */
        public TileId {
            Preconditions.checkArgument(isValid(zoom, X, Y));
        }

        /**
         * Méthode publique et statique retournant vrai si et seulement si les attributs zoom, X et Y
         * constituent une tuile valide.
         *
         * @param zoom Le niveau de zoom de la tuile a vérifié.
         * @param X    L'index X de la tuile a vérifié.
         * @param Y    L'index Y de la tuile a vérifié.
         * @return Vrai si les trois attributs constituent une tuile valide, sinon faux.
         */
        public static boolean isValid(int zoom, int X, int Y) {
            double maxXY = 1 << zoom;
            return (0 <= X && X < maxXY) && (0 <= Y && Y < maxXY);
        }
    }

}