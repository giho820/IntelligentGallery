package kr.ac.korea.intelligentgallery.data;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.Serializable;

import kr.ac.korea.intelligentgallery.util.FileUtil;

/**
 * Created by kiho on 2015. 12. 10..
 */
public class ImageFile implements Serializable {

    private Integer id;
    private String path;
    private String name;
    private String size;
    private boolean isDirectory;
    private boolean isChecked;
    private Integer categoryId;
    private String categoryName;
    private Integer rank;
    private Float score;
    private String recentImageFile;//category 대표이미지 path
    private Integer recentImageFileID;//category 대표이미지 아이디
    private String date_taken;
    private String date_added;
    private String date_modified;
    private Integer orientation; //회전을 위함
    private String latitude; //위도
    private String longitude; //경도


    public ImageFile() {

    }

    public ImageFile(String path, boolean isDirectory) {
        this.path = path;
//        this.name = FileUtil.getFileNameFromPath(path);
        this.isDirectory = isDirectory;
        this.isChecked = false;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }


    public void setPath(String path) {
        this.path = path;
    }

    public void setPath(Context context, Integer id) {
        this.path = FileUtil.getImagePath(context, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + id));
    }

    public String getParentPath() {
        File cFile = new File(path);
        String parentPath = cFile.getParentFile().getPath();
        return parentPath;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setIsDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
    }

    public boolean getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getRecentImageFile() {
        return recentImageFile;
    }

    public void setRecentImageFile(String recentImageFile) {
        this.recentImageFile = recentImageFile;
    }

    public Integer getRecentImageFileID() {
        return recentImageFileID;
    }

    public void setRecentImageFileID(Integer recentImageFileID) {
        this.recentImageFileID = recentImageFileID;
    }

    public String getDate_added() {
        return date_added;
    }

    public void setDate_added(String date_added) {
        this.date_added = date_added;
    }

    public String getDate_modified() {
        return date_modified;
    }

    public void setDate_modified(String date_modified) {
        this.date_modified = date_modified;
    }

    public String getDate_taken() {
        return date_taken;
    }

    public void setDate_taken(String date_taken) {
        this.date_taken = date_taken;
    }

    public Integer getOrientation() {
        return orientation;
    }

    public void setOrientation(Integer orientation) {
        this.orientation = orientation;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

}
