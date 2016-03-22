package kr.ac.korea.intelligentgallery.data;

import java.io.Serializable;

/**
 * Created by kiho on 2016. 2. 16..
 */
public class Album implements Serializable{
    private String id; //앨범 고유 아이디, 미디어 스토리지 테이블에 사진마다 존재하는 Buket_Id에 해당
    private String path; //앨범의 시스템 상의 경로
    private String name;
    public Integer count; //앨범 내의 이미지 개수
    private Integer coverID; //앨범의 커버이미지 아이디
    private String coverImagePath;
    private boolean isChecked; //체크 여부
    private boolean isHide; //숨기기 여부 체크

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public Integer getCoverID() {
        return coverID;
    }

    public void setCoverID(Integer coverID) {
        this.coverID = coverID;
    }

    public String getCoverImagePath() {
        return coverImagePath;
    }

    public void setCoverImagePath(String coverImagePath) {
        this.coverImagePath = coverImagePath;
    }

    public boolean isHide() {
        return isHide;
    }

    public void setIsHide(boolean isHide) {
        this.isHide = isHide;
    }
}
