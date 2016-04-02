package kr.ac.korea.intelligentgallery.act;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import kr.ac.korea.intelligentgallery.R;
import kr.ac.korea.intelligentgallery.adapter.SearchResultActImageAdapter;
import kr.ac.korea.intelligentgallery.common.ExpandableHeightGridView;
import kr.ac.korea.intelligentgallery.common.ParentAct;
import kr.ac.korea.intelligentgallery.data.ImageFile;
import kr.ac.korea.intelligentgallery.util.DebugUtil;
import kr.ac.korea.intelligentgallery.util.FileUtil;

/**
 * Created by kiho on 2016. 3. 7..
 */
public class SearchResultAct extends ParentAct {
    public static SearchResultAct searchResultAct;

    ArrayList<Integer> keywordSearchResults = new ArrayList<>();
    public ArrayList<ImageFile> keywordSearchResultImages;
    public SearchResultActImageAdapter keywordSearchResultimageAdapter;
    private ExpandableHeightGridView gridViewKeywordSearch;
    private TextView textViewKeywordSearchCount;

    ArrayList<Integer> symanticSearchResults = new ArrayList<>();
    public ArrayList<ImageFile> symanticSearchResultImages;
    public SearchResultActImageAdapter symanticSearchResultimageAdapter;
    public ExpandableHeightGridView gridViewSymanticSearchResult;
    private TextView textViewSymanticSearchCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        searchResultAct = this;

        //키워드 검색
        textViewKeywordSearchCount = (TextView) findViewById(R.id.textViewKeywordSearchCount);
        gridViewKeywordSearch = (ExpandableHeightGridView) findViewById(R.id.gridViewKeywordSearch);
        gridViewKeywordSearch.setNumColumns(MainAct.GridViewFolderNumColumns);
        gridViewKeywordSearch.setExpanded(true);

        keywordSearchResultImages = new ArrayList<>();
        keywordSearchResults = new ArrayList<>();
        keywordSearchResults = getIntent().getIntegerArrayListExtra("keywordSearchResult");
        DebugUtil.showDebug("SearchResultAct, keywordSearchResult::" + keywordSearchResults.size());
        for(Integer keywordSearchResultImageId : keywordSearchResults){
            ImageFile imageFile = new ImageFile();
            imageFile.setId(keywordSearchResultImageId);
            imageFile.setPath(FileUtil.getImagePath(this, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + keywordSearchResultImageId)));
            imageFile.setRecentImageFileID(keywordSearchResultImageId);
            keywordSearchResultImages.add(imageFile);
        }

        if(keywordSearchResultImages != null && keywordSearchResultImages.size() >= 0) {
            keywordSearchResultimageAdapter = new SearchResultActImageAdapter(SearchResultAct.this, keywordSearchResultImages);
            DebugUtil.showDebug("keywordSearchResultimageAdapter : " + keywordSearchResultimageAdapter.getCount());
            textViewKeywordSearchCount.setText("" + keywordSearchResultimageAdapter.getCount());
            gridViewKeywordSearch.setAdapter(keywordSearchResultimageAdapter);
        }




        //시맨틱 검색
        textViewSymanticSearchCount = (TextView) findViewById(R.id.textViewSymanticSearchCount);
        gridViewSymanticSearchResult = (ExpandableHeightGridView) findViewById(R.id.gridViewSemanticSearchResult);
        gridViewSymanticSearchResult.setNumColumns(MainAct.GridViewFolderNumColumns);
        gridViewSymanticSearchResult.setExpanded(true);

        symanticSearchResultImages = new ArrayList<>();
        symanticSearchResults = getIntent().getIntegerArrayListExtra("searchResult");
        Set<Integer> ids = new HashSet<>();
        ids.addAll(symanticSearchResults);
        for(Integer searchResultImageId : ids) {
            ImageFile imageFileTmp = new ImageFile();
            imageFileTmp.setId(searchResultImageId);
            imageFileTmp.setPath(FileUtil.getImagePath(this, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + searchResultImageId)));
            imageFileTmp.setRecentImageFileID(searchResultImageId);
            symanticSearchResultImages.add(imageFileTmp);
        }

        if(symanticSearchResultImages != null && symanticSearchResultImages.size() >= 0) {
            symanticSearchResultimageAdapter = new SearchResultActImageAdapter(SearchResultAct.this, symanticSearchResultImages);
            DebugUtil.showDebug("imageAdapter : "  + symanticSearchResultimageAdapter.getCount());
            textViewSymanticSearchCount.setText("" + symanticSearchResultimageAdapter.getCount());
            gridViewSymanticSearchResult.setAdapter(symanticSearchResultimageAdapter);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
