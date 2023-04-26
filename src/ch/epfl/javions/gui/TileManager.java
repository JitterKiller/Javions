package ch.epfl.javions.gui;

import ch.epfl.javions.Preconditions;
import javafx.scene.image.Image;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TileManager {

    private static final int MEMORY_CACHE_SIZE = 100;
    private final Path diskCachePath;
    private final String severName;
    private final Map<TileId, Image> memoryCache = new LinkedHashMap<>(MEMORY_CACHE_SIZE);

    public TileManager(Path diskCachePath, String serverName) {
        this.diskCachePath = diskCachePath;
        this.severName = serverName;
    }

    public Image imageForTileAt(TileId id) throws IOException {

        if(memoryCache.containsKey(id)) return memoryCache.get(id);

        Path tilePath = Path.of(String.valueOf(diskCachePath),String.valueOf(id.zoom),
                String.valueOf(id.X),String.valueOf(id.Y),".png");

        if(Files.exists(tilePath)) {
            return new Image(String.valueOf(tilePath.toUri()));
        } else {
            if (!Files.isDirectory(tilePath.getParent())) {
                Files.createDirectory(tilePath.getParent());
            }
            return load(id, tilePath);
        }

    }

    private Image load(TileId id, Path potentialImageDirectory) throws IOException {
        URL u = new URL("https://"+severName+"/"+id.zoom+"/"+id.X+"/"+id.Y+"/");
        URLConnection c = u.openConnection();
        c.setRequestProperty("User-Agent", "Javions");
        try (InputStream i = c.getInputStream();
             OutputStream o = new FileOutputStream(new File(potentialImageDirectory.toUri()))) {
            byte[] imageBytes = i.readAllBytes();
            o.write(imageBytes);
            Image image = new Image(new ByteArrayInputStream(imageBytes));
            if (memoryCache.keySet().size() == MEMORY_CACHE_SIZE) {
                Iterator<TileId> it = memoryCache.keySet().iterator();
                memoryCache.remove(it.next());
            }
            memoryCache.put(id, image);
            return image;
        }
    }

    private record TileId(int zoom, int X, int Y) {
        public TileId {
            Preconditions.checkArgument(isValid(zoom,X,Y));
        }
        public static boolean isValid(int zoom, int X, int Y) {

            double maxXY = Math.scalb(1d, 8 + zoom) / 256;

            return (0 <= X && X < maxXY) && (0 <= Y && Y < maxXY);
        }
    }

}
