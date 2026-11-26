package com.bnagritech.tradesphere.imagekit.dto;

/**
 * The only folders a file may go to. The app sends one of these names, never a raw path,
 * so nobody can write files anywhere else in the media library.
 *
 * Folder in ImageKit = app.imagekit.root-folder + "/" + path, e.g. /tradesphere/VisitImages
 */
public enum MediaFolder {

    /** Daily photos a user takes during visits at outlets */
    VISIT_IMAGES("VisitImages"),

    /** Photos of the outlet itself */
    OUTLET_IMAGES("OutletImages"),

    /** User profile picture */
    PROFILE_PICTURE("Profilepicture");

    private final String path;

    MediaFolder(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }
}
