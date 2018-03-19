import javax.swing.*;
import java.awt.*;

class Album {
    String albumName;
    String artistName;
    String genre;
    ImageIcon albumCover;


    Album(String newAlbumName, String newArtistName, String newGenre, ImageIcon newAlbumCover) {
        albumName = newAlbumName;
        artistName = newArtistName;
        genre = newGenre;
        albumCover = newAlbumCover;
    }

    @Override
    public String toString() {
        return albumCover.toString();
    }
}
