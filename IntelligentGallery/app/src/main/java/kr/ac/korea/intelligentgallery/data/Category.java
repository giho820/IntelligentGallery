package kr.ac.korea.intelligentgallery.data;

import java.util.ArrayList;

/**
 * Created by kiho on 2016. 1. 26..
 */
public class Category {
    private Integer cID;
    private String cName;
    private ArrayList<ImageFile> containingImages;
    private Integer count;
    private Integer coverImageId;
    private String coverImagePath;

    public Integer getcID() {
        return cID;
    }

    public void setcID(Integer cID) {
        this.cID = cID;
    }

    public String getcName() {
        return cName;
    }

    public void setcName(String cName) {
        this.cName = cName;
    }

    public ArrayList<ImageFile> getContainingImages() {
        return containingImages;
    }

    public void setContainingImages(ArrayList<ImageFile> containingImages) {
        this.containingImages = containingImages;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getCoverImageId() {
        return coverImageId;
    }

    public void setCoverImageId(Integer coverImageId) {
        this.coverImageId = coverImageId;
    }

    public String getCoverImagePath() {
        return coverImagePath;
    }

    public void setCoverImagePath(String coverImagePath) {
        this.coverImagePath = coverImagePath;
    }
}
