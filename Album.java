import javax.swing.*;

/*
* This class represents a single Album.
 */
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

    /**
     * Returns a string representation of album cover.
     *
     * @return  String  album cover code
     */
    @Override
    public String toString() {
        return albumCover.toString();
    }
}
