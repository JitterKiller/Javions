package ch.epfl.javions.gui;

import javafx.scene.image.Image;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TileManager {
    private final Path path;
    private final String severName;

    private final Map<TileId, Image> cacheMemoryMap = new LinkedHashMap<>(100);

    public TileManager(Path path, String serverName) {
        this.path = path;
        this.severName = serverName;
    }

    public Image imageForTileAt(TileId id) throws IOException {

        for(TileId idStored: cacheMemoryMap.keySet()) {
            if(idStored.equals(id)) {
                return cacheMemoryMap.get(idStored);
            }
        }

        Path potentialImageDirectory = Path.of(String.valueOf(path),String.valueOf(id.zoom),
                String.valueOf(id.X),String.valueOf(id.Y),".png");


        if(Files.exists(potentialImageDirectory)) {
            return new Image(potentialImageDirectory.toString());
        } else {
            if (!Files.isDirectory(potentialImageDirectory.getParent())) {
                Files.createDirectory(potentialImageDirectory.getParent());
            }
            return getImage(id, potentialImageDirectory);
        }

    }

    private Image getImage(TileId id, Path potentialImageDirectory) throws IOException {
        URL u = new URL(severName+"/"+id.zoom+"/"+id.X+"/"+id.Y+"/");
        URLConnection c = u.openConnection();
        c.setRequestProperty("User-Agent", "Javions");
        try (InputStream i = c.getInputStream();
             OutputStream o = new FileOutputStream(new File(potentialImageDirectory.toUri()))) {
            byte[] imageBytes = i.readAllBytes();
            o.write(imageBytes);
            Image image = new Image(new ByteArrayInputStream(imageBytes));
            Iterator<TileId> it = cacheMemoryMap.keySet().iterator();
            cacheMemoryMap.remove(it.next());
            cacheMemoryMap.put(id,image);
            return image;
        }
    }

    private record TileId(int zoom, int X, int Y) {
        public static boolean isValid(int zoom, int X, int Y) {

            double maxXY = Math.scalb(1d, 8 + zoom);

            return (0 <= X && X < maxXY) && (0 <= Y && Y < maxXY);
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof TileId) && (zoom == ((TileId) obj).zoom) && (X == ((TileId) obj).X)
                    && (Y == ((TileId) obj).Y);
        }
    }

}
